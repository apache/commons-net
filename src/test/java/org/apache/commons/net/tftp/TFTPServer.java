/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.tftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Enumeration;
import java.util.HashSet;

import org.apache.commons.net.io.FromNetASCIIOutputStream;
import org.apache.commons.net.io.ToNetASCIIInputStream;

/**
 * A fully multi-threaded TFTP server. Can handle multiple clients at the same time. Implements RFC 1350 and wrapping block numbers for large file support.
 *
 * To launch, just create an instance of the class. An IOException will be thrown if the server fails to start for reasons such as port in use, port denied,
 * etc.
 *
 * To stop, use the shutdown method.
 *
 * To check to see if the server is still running (or if it stopped because of an error), call the isRunning() method.
 *
 * By default, events are not logged to stdout/stderr. This can be changed with the setLog and setLogError methods.
 *
 * <p>
 * Example usage is below:
 *
 * <code>
 * public static void main(String[] args) throws Exception {
 *      if (args.length != 1) {
 *          System.out.println("You must provide 1 argument - the base path for the server to serve from.");
 *          System.exit(1);
 *      }
 *
 *      try (TFTPServer ts = new TFTPServer(new File(args[0]), new File(args[0]), GET_AND_PUT)) {
 *        ts.setSocketTimeout(2000);
 *        System.out.println("TFTP Server running.  Press enter to stop.");
 *        new InputStreamReader(System.in).read();
 *      }
 *
 *      System.out.println("Server shut down.");
 *      System.exit(0);
 * }
 * </code>
 *
 * @since 2.0
 */
public class TFTPServer implements Runnable, AutoCloseable {

    public enum ServerMode {
        GET_ONLY, PUT_ONLY, GET_AND_PUT
    }

    /*
     * An ongoing transfer.
     */
    private class TFTPTransfer implements Runnable {
        private final TFTPPacket tftpPacket;

        private boolean shutdownTransfer;

        TFTP transferTftp;

        public TFTPTransfer(final TFTPPacket tftpPacket) {
            this.tftpPacket = tftpPacket;
        }

        /*
         * Makes sure that paths provided by TFTP clients do not get outside of the serverRoot directory.
         */
        private File buildSafeFile(final File serverDirectory, final String fileName, final boolean createSubDirs) throws IOException {
            final File temp = new File(serverDirectory, fileName).getCanonicalFile();

            if (!isSubdirectoryOf(serverDirectory, temp)) {
                throw new IOException("Cannot access files outside of TFTP server root.");
            }

            // ensure directory exists (if requested)
            if (createSubDirs) {
                createDirectory(temp.getParentFile());
            }

            return temp;
        }

        /*
         * Creates subdirectories recursively.
         */
        private void createDirectory(final File file) throws IOException {
            final File parent = file.getParentFile();
            if (parent == null) {
                throw new IOException("Unexpected error creating requested directory");
            }
            if (!parent.exists()) {
                // recurse...
                createDirectory(parent);
            }

            if (!parent.isDirectory()) {
                throw new IOException("Invalid directory path - file in the way of requested folder");
            }
            if (file.isDirectory()) {
                return;
            }
            final boolean result = file.mkdir();
            if (!result) {
                throw new IOException("Couldn't create requested directory");
            }
        }

        /*
         * Handles a tftp read request.
         */
        private void handleRead(final TFTPReadRequestPacket trrp) throws IOException, TFTPPacketException {
            if (mode == ServerMode.PUT_ONLY) {
                transferTftp
                        .bufferedSend(new TFTPErrorPacket(trrp.getAddress(), trrp.getPort(), TFTPErrorPacket.ILLEGAL_OPERATION, "Read not allowed by server."));
                return;
            }
            InputStream inputStream = null;
            try {
                try {
                    inputStream = new BufferedInputStream(new FileInputStream(buildSafeFile(serverReadDirectory, trrp.getFilename(), false)));
                } catch (final FileNotFoundException e) {
                    transferTftp.bufferedSend(new TFTPErrorPacket(trrp.getAddress(), trrp.getPort(), TFTPErrorPacket.FILE_NOT_FOUND, e.getMessage()));
                    return;
                } catch (final Exception e) {
                    transferTftp.bufferedSend(new TFTPErrorPacket(trrp.getAddress(), trrp.getPort(), TFTPErrorPacket.UNDEFINED, e.getMessage()));
                    return;
                }

                if (trrp.getMode() == TFTP.NETASCII_MODE) {
                    inputStream = new ToNetASCIIInputStream(inputStream);
                }

                final byte[] temp = new byte[TFTPDataPacket.MAX_DATA_LENGTH];

                TFTPPacket answer;

                int block = 1;
                boolean sendNext = true;

                int readLength = TFTPDataPacket.MAX_DATA_LENGTH;

                TFTPDataPacket lastSentData = null;

                // We are reading a file, so when we read less than the
                // requested bytes, we know that we are at the end of the file.
                while (readLength == TFTPDataPacket.MAX_DATA_LENGTH && !shutdownTransfer) {
                    if (sendNext) {
                        readLength = inputStream.read(temp);
                        if (readLength == -1) {
                            readLength = 0;
                        }

                        lastSentData = new TFTPDataPacket(trrp.getAddress(), trrp.getPort(), block, temp, 0, readLength);
                        sendData(transferTftp, lastSentData); // send the data
                    }

                    answer = null;

                    int timeoutCount = 0;

                    while (!shutdownTransfer && (answer == null || !answer.getAddress().equals(trrp.getAddress()) || answer.getPort() != trrp.getPort())) {
                        // listen for an answer.
                        if (answer != null) {
                            // The answer that we got didn't come from the
                            // expected source, fire back an error, and continue
                            // listening.
                            log.println("TFTP Server ignoring message from unexpected source.");
                            transferTftp.bufferedSend(
                                    new TFTPErrorPacket(answer.getAddress(), answer.getPort(), TFTPErrorPacket.UNKNOWN_TID, "Unexpected Host or Port"));
                        }
                        try {
                            answer = transferTftp.bufferedReceive();
                        } catch (final SocketTimeoutException e) {
                            if (timeoutCount >= maxTimeoutRetries) {
                                throw e;
                            }
                            // didn't get an ack for this data. need to resend
                            // it.
                            timeoutCount++;
                            transferTftp.bufferedSend(lastSentData);
                            continue;
                        }
                    }

                    if (answer == null || !(answer instanceof TFTPAckPacket)) {
                        if (!shutdownTransfer) {
                            logError.println("Unexpected response from tftp client during transfer (" + answer + ").  Transfer aborted.");
                        }
                        break;
                    }
                    // once we get here, we know we have an answer packet
                    // from the correct host.
                    final TFTPAckPacket ack = (TFTPAckPacket) answer;
                    if (ack.getBlockNumber() != block) {
                        /*
                         * The origional tftp spec would have called on us to resend the previous data here, however, that causes the SAS Syndrome.
                         * http://www.faqs.org/rfcs/rfc1123.html section 4.2.3.1 The modified spec says that we ignore a duplicate ack. If the packet was really
                         * lost, we will time out on receive, and resend the previous data at that point.
                         */
                        sendNext = false;
                    } else {
                        // send the next block
                        block++;
                        if (block > 65535) {
                            // wrap the block number
                            block = 0;
                        }
                        sendNext = true;
                    }
                }
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (final IOException e) {
                    // noop
                }
            }
        }

        /*
         * handle a TFTP write request.
         */
        private void handleWrite(final TFTPWriteRequestPacket twrp) throws IOException, TFTPPacketException {
            OutputStream bos = null;
            try {
                if (mode == ServerMode.GET_ONLY) {
                    transferTftp.bufferedSend(
                            new TFTPErrorPacket(twrp.getAddress(), twrp.getPort(), TFTPErrorPacket.ILLEGAL_OPERATION, "Write not allowed by server."));
                    return;
                }

                int lastBlock = 0;
                final String fileName = twrp.getFilename();

                try {
                    final File temp = buildSafeFile(serverWriteDirectory, fileName, true);
                    if (temp.exists()) {
                        transferTftp.bufferedSend(new TFTPErrorPacket(twrp.getAddress(), twrp.getPort(), TFTPErrorPacket.FILE_EXISTS, "File already exists"));
                        return;
                    }
                    bos = new BufferedOutputStream(new FileOutputStream(temp));

                    if (twrp.getMode() == TFTP.NETASCII_MODE) {
                        bos = new FromNetASCIIOutputStream(bos);
                    }
                } catch (final Exception e) {
                    transferTftp.bufferedSend(new TFTPErrorPacket(twrp.getAddress(), twrp.getPort(), TFTPErrorPacket.UNDEFINED, e.getMessage()));
                    return;
                }

                TFTPAckPacket lastSentAck = new TFTPAckPacket(twrp.getAddress(), twrp.getPort(), 0);
                sendData(transferTftp, lastSentAck); // send the data

                while (true) {
                    // get the response - ensure it is from the right place.
                    TFTPPacket dataPacket = null;

                    int timeoutCount = 0;

                    while (!shutdownTransfer
                            && (dataPacket == null || !dataPacket.getAddress().equals(twrp.getAddress()) || dataPacket.getPort() != twrp.getPort())) {
                        // listen for an answer.
                        if (dataPacket != null) {
                            // The data that we got didn't come from the
                            // expected source, fire back an error, and continue
                            // listening.
                            log.println("TFTP Server ignoring message from unexpected source.");
                            transferTftp.bufferedSend(
                                    new TFTPErrorPacket(dataPacket.getAddress(), dataPacket.getPort(), TFTPErrorPacket.UNKNOWN_TID, "Unexpected Host or Port"));
                        }

                        try {
                            dataPacket = transferTftp.bufferedReceive();
                        } catch (final SocketTimeoutException e) {
                            if (timeoutCount >= maxTimeoutRetries) {
                                throw e;
                            }
                            // It didn't get our ack. Resend it.
                            transferTftp.bufferedSend(lastSentAck);
                            timeoutCount++;
                            continue;
                        }
                    }

                    if (dataPacket instanceof TFTPWriteRequestPacket) {
                        // it must have missed our initial ack. Send another.
                        lastSentAck = new TFTPAckPacket(twrp.getAddress(), twrp.getPort(), 0);
                        transferTftp.bufferedSend(lastSentAck);
                    } else if (dataPacket == null || !(dataPacket instanceof TFTPDataPacket)) {
                        if (!shutdownTransfer) {
                            logError.println("Unexpected response from tftp client during transfer (" + dataPacket + ").  Transfer aborted.");
                        }
                        break;
                    } else {
                        final int block = ((TFTPDataPacket) dataPacket).getBlockNumber();
                        final byte[] data = ((TFTPDataPacket) dataPacket).getData();
                        final int dataLength = ((TFTPDataPacket) dataPacket).getDataLength();
                        final int dataOffset = ((TFTPDataPacket) dataPacket).getDataOffset();

                        if (block > lastBlock || lastBlock == 65535 && block == 0) {
                            // it might resend a data block if it missed our ack
                            // - don't rewrite the block.
                            bos.write(data, dataOffset, dataLength);
                            lastBlock = block;
                        }

                        lastSentAck = new TFTPAckPacket(twrp.getAddress(), twrp.getPort(), block);
                        sendData(transferTftp, lastSentAck); // send the data
                        if (dataLength < TFTPDataPacket.MAX_DATA_LENGTH) {
                            // end of stream signal - The tranfer is complete.
                            bos.close();

                            // But my ack may be lost - so listen to see if I
                            // need to resend the ack.
                            for (int i = 0; i < maxTimeoutRetries; i++) {
                                try {
                                    dataPacket = transferTftp.bufferedReceive();
                                } catch (final SocketTimeoutException e) {
                                    // this is the expected route - the client
                                    // shouldn't be sending any more packets.
                                    break;
                                }

                                if (dataPacket != null && (!dataPacket.getAddress().equals(twrp.getAddress()) || dataPacket.getPort() != twrp.getPort())) {
                                    // make sure it was from the right client...
                                    transferTftp.bufferedSend(new TFTPErrorPacket(dataPacket.getAddress(), dataPacket.getPort(), TFTPErrorPacket.UNKNOWN_TID,
                                            "Unexpected Host or Port"));
                                } else {
                                    // This means they sent us the last
                                    // datapacket again, must have missed our
                                    // ack. resend it.
                                    transferTftp.bufferedSend(lastSentAck);
                                }
                            }

                            // all done.
                            break;
                        }
                    }
                }
            } finally {
                if (bos != null) {
                    bos.close();
                }
            }
        }

        /*
         * recursively check to see if one directory is a parent of another.
         */
        private boolean isSubdirectoryOf(final File parent, final File child) {
            final File childsParent = child.getParentFile();
            if (childsParent == null) {
                return false;
            }
            if (childsParent.equals(parent)) {
                return true;
            }
            return isSubdirectoryOf(parent, childsParent);
        }

        @Override
        public void run() {
            try {
                transferTftp = newTFTP();

                transferTftp.beginBufferedOps();
                transferTftp.setDefaultTimeout(socketTimeout);

                transferTftp.open();

                if (tftpPacket instanceof TFTPReadRequestPacket) {
                    handleRead((TFTPReadRequestPacket) tftpPacket);
                } else if (tftpPacket instanceof TFTPWriteRequestPacket) {
                    handleWrite((TFTPWriteRequestPacket) tftpPacket);
                } else {
                    log.println("Unsupported TFTP request (" + tftpPacket + ") - ignored.");
                }
            } catch (final Exception e) {
                if (!shutdownTransfer) {
                    logError.println("Unexpected Error in during TFTP file transfer.  Transfer aborted. " + e);
                }
            } finally {
                try {
                    if (transferTftp != null && transferTftp.isOpen()) {
                        transferTftp.endBufferedOps();
                        transferTftp.close();
                    }
                } catch (final Exception e) {
                    // noop
                }
                synchronized (transfers) {
                    transfers.remove(this);
                }
            }
        }

        public void shutdown() {
            shutdownTransfer = true;
            try {
                transferTftp.close();
            } catch (final RuntimeException e) {
                // noop
            }
        }
    }

    private static final int DEFAULT_TFTP_PORT = 69;
    /* /dev/null output stream (default) */
    private static final PrintStream nullStream = new PrintStream(new OutputStream() {
        @Override
        public void write(final byte[] b) throws IOException {
        }

        @Override
        public void write(final int b) {
        }
    });

    private final HashSet<TFTPTransfer> transfers = new HashSet<>();
    private volatile boolean shutdownServer;
    private TFTP serverTftp;
    private File serverReadDirectory;
    private File serverWriteDirectory;
    private final int port;
    private final InetAddress localAddress;

    private Exception serverException;

    private final ServerMode mode;
    // don't have access to a logger api, so we will log to these streams, which
    // by default are set to a no-op logger
    private PrintStream log;

    private PrintStream logError;
    private int maxTimeoutRetries = 3;
    private int socketTimeout;

    private Thread serverThread;

    /**
     * Start a TFTP Server on the specified port. Gets and Puts occur in the specified directory.
     *
     * The server will start in another thread, allowing this constructor to return immediately.
     *
     * If a get or a put comes in with a relative path that tries to get outside of the serverDirectory, then the get or put will be denied.
     *
     * GET_ONLY mode only allows gets, PUT_ONLY mode only allows puts, and GET_AND_PUT allows both. Modes are defined as int constants in this class.
     *
     * @param serverReadDirectory  directory for GET requests
     * @param serverWriteDirectory directory for PUT requests
     * @param port                 The local port to bind to.
     * @param localAddress            The local address to bind to.
     * @param mode                 A value as specified above.
     * @param log                  Stream to write log message to. If not provided, uses System.out
     * @param errorLog             Stream to write error messages to. If not provided, uses System.err.
     * @throws IOException if the server directory is invalid or does not exist.
     */
    public TFTPServer(final File serverReadDirectory, final File serverWriteDirectory, final int port, final InetAddress localAddress, final ServerMode mode,
            final PrintStream log, final PrintStream errorLog) throws IOException {
        this.port = port;
        this.mode = mode;
        this.localAddress = localAddress;
        this.log = log == null ? nullStream : log;
        this.logError = errorLog == null ? nullStream : errorLog;
        launch(serverReadDirectory, serverWriteDirectory);
    }

    /**
     * Start a TFTP Server on the specified port. Gets and Puts occur in the specified directory.
     *
     * The server will start in another thread, allowing this constructor to return immediately.
     *
     * If a get or a put comes in with a relative path that tries to get outside of the serverDirectory, then the get or put will be denied.
     *
     * GET_ONLY mode only allows gets, PUT_ONLY mode only allows puts, and GET_AND_PUT allows both. Modes are defined as int constants in this class.
     *
     * @param serverReadDirectory  directory for GET requests
     * @param serverWriteDirectory directory for PUT requests
     * @param port                 the port to use
     * @param localiface           The local network interface to bind to. The interface's first address wil be used.
     * @param mode                 A value as specified above.
     * @param log                  Stream to write log message to. If not provided, uses System.out
     * @param errorLog             Stream to write error messages to. If not provided, uses System.err.
     * @throws IOException if the server directory is invalid or does not exist.
     */
    public TFTPServer(final File serverReadDirectory, final File serverWriteDirectory, final int port, final NetworkInterface localiface, final ServerMode mode,
            final PrintStream log, final PrintStream errorLog) throws IOException {
        this.mode = mode;
        this.port = port;
        InetAddress inetAddress = null;
        if (localiface != null) {
            final Enumeration<InetAddress> ifaddrs = localiface.getInetAddresses();
            if (ifaddrs != null && ifaddrs.hasMoreElements()) {
                inetAddress = ifaddrs.nextElement();
            }
        }
        this.log = log == null ? nullStream : log;
        this.logError = errorLog == null ? nullStream : errorLog;
        this.localAddress = inetAddress;
        launch(serverReadDirectory, serverWriteDirectory);
    }

    /**
     * Start a TFTP Server on the specified port. Gets and Puts occur in the specified directory.
     *
     * The server will start in another thread, allowing this constructor to return immediately.
     *
     * If a get or a put comes in with a relative path that tries to get outside of the serverDirectory, then the get or put will be denied.
     *
     * GET_ONLY mode only allows gets, PUT_ONLY mode only allows puts, and GET_AND_PUT allows both. Modes are defined as int constants in this class.
     *
     * @param serverReadDirectory  directory for GET requests
     * @param serverWriteDirectory directory for PUT requests
     * @param port                 the port to use
     * @param mode                 A value as specified above.
     * @param log                  Stream to write log message to. If not provided, uses System.out
     * @param errorLog             Stream to write error messages to. If not provided, uses System.err.
     * @throws IOException if the server directory is invalid or does not exist.
     */
    public TFTPServer(final File serverReadDirectory, final File serverWriteDirectory, final int port, final ServerMode mode, final PrintStream log,
            final PrintStream errorLog) throws IOException {
        this.port = port;
        this.mode = mode;
        this.log = log == null ? nullStream : log;
        this.logError = errorLog == null ? nullStream : errorLog;
        this.localAddress = null;
        launch(serverReadDirectory, serverWriteDirectory);
    }

    /**
     * Start a TFTP Server on the default port (69). Gets and Puts occur in the specified directories.
     *
     * The server will start in another thread, allowing this constructor to return immediately.
     *
     * If a get or a put comes in with a relative path that tries to get outside of the serverDirectory, then the get or put will be denied.
     *
     * GET_ONLY mode only allows gets, PUT_ONLY mode only allows puts, and GET_AND_PUT allows both. Modes are defined as int constants in this class.
     *
     * @param serverReadDirectory  directory for GET requests
     * @param serverWriteDirectory directory for PUT requests
     * @param mode                 A value as specified above.
     * @throws IOException if the server directory is invalid or does not exist.
     */
    public TFTPServer(final File serverReadDirectory, final File serverWriteDirectory, final ServerMode mode) throws IOException {
        this(serverReadDirectory, serverWriteDirectory, DEFAULT_TFTP_PORT, mode, null, null);
    }

    /**
     * Closes the TFTP server (and any currently running transfers) and release all opened network resources.
     *
     * @since 3.10.0
     */
    @Override
    public void close() {
        shutdownServer = true;

        synchronized (transfers) {
            transfers.forEach(TFTPTransfer::shutdown);
        }

        try {
            serverTftp.close();
        } catch (final RuntimeException e) {
            // noop
        }

        try {
            serverThread.join();
        } catch (final InterruptedException e) {
            // we've done the best we could, return
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }

    /**
     * Gets the current value for maxTimeoutRetries
     *
     * @return the max allowed number of retries
     */
    public int getMaxTimeoutRetries() {
        return maxTimeoutRetries;
    }

    /**
     * Gets the server port number
     *
     * @return the server port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the current socket timeout used during transfers in milliseconds.
     *
     * @return the timeout value
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * check if the server thread is still running.
     *
     * @return true if running, false if stopped.
     * @throws Exception throws the exception that stopped the server if the server is stopped from an exception.
     */
    public boolean isRunning() throws Exception {
        if (shutdownServer && serverException != null) {
            throw serverException;
        }
        return !shutdownServer;
    }

    /*
     * start the server, throw an error if it can't start.
     */
    private void launch(final File newServerReadDirectory, final File newServerWriteDirectory) throws IOException {
        log.println("Starting TFTP Server on port " + port + ".  Read directory: " + newServerReadDirectory + " Write directory: " + newServerWriteDirectory
                + " Server Mode is " + mode);

        this.serverReadDirectory = newServerReadDirectory.getCanonicalFile();
        if (!serverReadDirectory.exists() || !newServerReadDirectory.isDirectory()) {
            throw new IOException("The server read directory " + this.serverReadDirectory + " does not exist");
        }

        this.serverWriteDirectory = newServerWriteDirectory.getCanonicalFile();
        if (!this.serverWriteDirectory.exists() || !newServerWriteDirectory.isDirectory()) {
            throw new IOException("The server write directory " + this.serverWriteDirectory + " does not exist");
        }

        serverTftp = new TFTP();

        // This is the value used in response to each client.
        socketTimeout = serverTftp.getDefaultTimeout();

        // we want the server thread to listen forever.
        serverTftp.setDefaultTimeout(Duration.ZERO);

        if (localAddress != null) {
            serverTftp.open(port, localAddress);
        } else {
            serverTftp.open(port);
        }

        serverThread = new Thread(this);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    /*
     * Allow test code to customise the TFTP instance
     */
    TFTP newTFTP() {
        return new TFTP();
    }

    @Override
    public void run() {
        try {
            while (!shutdownServer) {
                final TFTPPacket tftpPacket;

                tftpPacket = serverTftp.receive();

                final TFTPTransfer tt = new TFTPTransfer(tftpPacket);
                synchronized (transfers) {
                    transfers.add(tt);
                }

                final Thread thread = new Thread(tt);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (final Exception e) {
            if (!shutdownServer) {
                serverException = e;
                logError.println("Unexpected Error in TFTP Server - Server shut down! + " + e);
            }
        } finally {
            shutdownServer = true; // set this to true, so the launching thread can check to see if it started.
            if (serverTftp != null && serverTftp.isOpen()) {
                serverTftp.close();
            }
        }
    }

    /*
     * Also allow customisation of sending data/ack so can generate errors if needed
     */
    void sendData(final TFTP tftp, final TFTPPacket data) throws IOException {
        tftp.bufferedSend(data);
    }

    /**
     * Set the stream object to log debug / informational messages. By default, this is a no-op
     *
     * @param log the stream to use for logging
     */
    public void setLog(final PrintStream log) {
        this.log = log;
    }

    /**
     * Set the stream object to log error messsages. By default, this is a no-op
     *
     * @param logError the stream to use for logging errors
     */
    public void setLogError(final PrintStream logError) {
        this.logError = logError;
    }

    /**
     * Set the max number of retries in response to a timeout. Default 3. Min 0.
     *
     * @param retries number of retries, must be &gt; 0
     * @throws IllegalArgumentException if {@code retries} is less than 0.
     */
    public void setMaxTimeoutRetries(final int retries) {
        if (retries < 0) {
            throw new IllegalArgumentException("Invalid Value");
        }
        maxTimeoutRetries = retries;
    }

    /**
     * Set the socket timeout in milliseconds used in transfers.
     * <p>
     * Defaults to the value {@link TFTP#DEFAULT_TIMEOUT}. Minimum value of 10.
     * </p>
     * @param timeout the timeout; must be equal to or larger than 10.
     * @throws IllegalArgumentException if {@code timeout} is less than 10.
     */
    public void setSocketTimeout(final int timeout) {
        if (timeout < 10) {
            throw new IllegalArgumentException("Invalid Value");
        }
        socketTimeout = timeout;
    }

    /**
     * Closes the TFTP server (and any currently running transfers) and release all opened network resources.
     *
     * @deprecated Use {@link #close()}.
     */
    @Deprecated
    public void shutdown() {
        close();
    }
}
