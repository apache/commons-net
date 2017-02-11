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

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.apache.commons.net.io.FromNetASCIIOutputStream;
import org.apache.commons.net.io.ToNetASCIIInputStream;

/***
 * The TFTPClient class encapsulates all the aspects of the TFTP protocol
 * necessary to receive and send files through TFTP.  It is derived from
 * the {@link org.apache.commons.net.tftp.TFTP} because
 * it is more convenient than using aggregation, and as a result exposes
 * the same set of methods to allow you to deal with the TFTP protocol
 * directly.  However, almost every user should only be concerend with the
 * the {@link org.apache.commons.net.DatagramSocketClient#open  open() },
 * {@link org.apache.commons.net.DatagramSocketClient#close  close() },
 * {@link #sendFile  sendFile() }, and
 * {@link #receiveFile  receiveFile() } methods.  Additionally, the
 * {@link #setMaxTimeouts  setMaxTimeouts() } and
 * {@link org.apache.commons.net.DatagramSocketClient#setDefaultTimeout setDefaultTimeout() }
 *  methods may be of importance for performance
 * tuning.
 * <p>
 * Details regarding the TFTP protocol and the format of TFTP packets can
 * be found in RFC 783.  But the point of these classes is to keep you
 * from having to worry about the internals.
 *
 *
 * @see TFTP
 * @see TFTPPacket
 * @see TFTPPacketException
 ***/

public class TFTPClient extends TFTP
{
    /***
     * The default number of times a receive attempt is allowed to timeout
     * before ending attempts to retry the receive and failing.  The default
     * is 5 timeouts.
     ***/
    public static final int DEFAULT_MAX_TIMEOUTS = 5;

    /*** The maximum number of timeouts allowed before failing. ***/
    private int __maxTimeouts;

    /*** The number of bytes received in the ongoing download. ***/
    private long totalBytesReceived = 0;

    /*** The number of bytes sent in the ongoing upload. ***/
    private long totalBytesSent = 0;

    /***
     * Creates a TFTPClient instance with a default timeout of DEFAULT_TIMEOUT,
     * maximum timeouts value of DEFAULT_MAX_TIMEOUTS, a null socket,
     * and buffered operations disabled.
     ***/
    public TFTPClient()
    {
        __maxTimeouts = DEFAULT_MAX_TIMEOUTS;
    }

    /***
     * Sets the maximum number of times a receive attempt is allowed to
     * timeout during a receiveFile() or sendFile() operation before ending
     * attempts to retry the receive and failing.
     * The default is DEFAULT_MAX_TIMEOUTS.
     *
     * @param numTimeouts  The maximum number of timeouts to allow.  Values
     *        less than 1 should not be used, but if they are, they are
     *        treated as 1.
     ***/
    public void setMaxTimeouts(int numTimeouts)
    {
        if (numTimeouts < 1) {
            __maxTimeouts = 1;
        } else {
            __maxTimeouts = numTimeouts;
        }
    }

    /***
     * Returns the maximum number of times a receive attempt is allowed to
     * timeout before ending attempts to retry the receive and failing.
     *
     * @return The maximum number of timeouts allowed.
     ***/
    public int getMaxTimeouts()
    {
        return __maxTimeouts;
    }


    /**
     * @return The number of bytes received in the ongoing download
     */
    public long getTotalBytesReceived() {
        return totalBytesReceived;
    }

    /**
     * @return The number of bytes sent in the ongoing download
     */
    public long getTotalBytesSent() {
        return totalBytesSent;
    }

    /***
     * Requests a named file from a remote host, writes the
     * file to an OutputStream, closes the connection, and returns the number
     * of bytes read.  A local UDP socket must first be created by
     * {@link org.apache.commons.net.DatagramSocketClient#open open()} before
     * invoking this method.  This method will not close the OutputStream
     * containing the file; you must close it after the method invocation.
     *
     * @param filename  The name of the file to receive.
     * @param mode   The TFTP mode of the transfer (one of the MODE constants).
     * @param output The OutputStream to which the file should be written.
     * @param host   The remote host serving the file.
     * @param port   The port number of the remote TFTP server.
     * @return number of bytes read
     * @throws IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     ***/
    public int receiveFile(String filename, int mode, OutputStream output,
                           InetAddress host, int port) throws IOException
    {
        int bytesRead = 0;
        int lastBlock = 0;
        int block = 1;
        int hostPort = 0;
        int dataLength = 0;

        totalBytesReceived = 0;

        if (mode == TFTP.ASCII_MODE) {
            output = new FromNetASCIIOutputStream(output);
        }

        TFTPPacket sent = new TFTPReadRequestPacket(host, port, filename, mode);
        TFTPAckPacket ack = new TFTPAckPacket(host, port, 0);

        beginBufferedOps();

        boolean justStarted = true;
        try {
            do { // while more data to fetch
                bufferedSend(sent); // start the fetch/send an ack
                boolean wantReply = true;
                int timeouts = 0;
                do { // until successful response
                    try {
                        TFTPPacket received = bufferedReceive();
                        // The first time we receive we get the port number and
                        // answering host address (for hosts with multiple IPs)
                        final int recdPort = received.getPort();
                        final InetAddress recdAddress = received.getAddress();
                        if (justStarted) {
                            justStarted = false;
                            if (recdPort == port) { // must not use the control port here
                                TFTPErrorPacket error = new TFTPErrorPacket(recdAddress,
                                        recdPort, TFTPErrorPacket.UNKNOWN_TID,
                                        "INCORRECT SOURCE PORT");
                                bufferedSend(error);
                                throw new IOException("Incorrect source port ("+recdPort+") in request reply.");
                            }
                            hostPort = recdPort;
                            ack.setPort(hostPort);
                            if(!host.equals(recdAddress))
                            {
                                host = recdAddress;
                                ack.setAddress(host);
                                sent.setAddress(host);
                            }
                        }
                        // Comply with RFC 783 indication that an error acknowledgment
                        // should be sent to originator if unexpected TID or host.
                        if (host.equals(recdAddress) && recdPort == hostPort) {
                            switch (received.getType()) {

                            case TFTPPacket.ERROR:
                                TFTPErrorPacket error = (TFTPErrorPacket)received;
                                throw new IOException("Error code " + error.getError() +
                                                      " received: " + error.getMessage());
                            case TFTPPacket.DATA:
                                TFTPDataPacket data = (TFTPDataPacket)received;
                                dataLength = data.getDataLength();
                                lastBlock = data.getBlockNumber();

                                if (lastBlock == block) { // is the next block number?
                                    try {
                                        output.write(data.getData(), data.getDataOffset(), dataLength);
                                    } catch (IOException e) {
                                        error = new TFTPErrorPacket(host, hostPort,
                                                                    TFTPErrorPacket.OUT_OF_SPACE,
                                                                    "File write failed.");
                                        bufferedSend(error);
                                        throw e;
                                    }
                                    ++block;
                                    if (block > 65535) {
                                        // wrap the block number
                                        block = 0;
                                    }
                                    wantReply = false; // got the next block, drop out to ack it
                                } else { // unexpected block number
                                    discardPackets();
                                    if (lastBlock == (block == 0 ? 65535 : (block - 1))) {
                                        wantReply = false; // Resend last acknowledgemen
                                    }
                                }
                                break;

                            default:
                                throw new IOException("Received unexpected packet type (" + received.getType() + ")");
                            }
                        } else { // incorrect host or TID
                            TFTPErrorPacket error = new TFTPErrorPacket(recdAddress, recdPort,
                                    TFTPErrorPacket.UNKNOWN_TID,
                                    "Unexpected host or port.");
                            bufferedSend(error);
                        }
                    } catch (SocketException e) {
                        if (++timeouts >= __maxTimeouts) {
                            throw new IOException("Connection timed out.");
                        }
                    } catch (InterruptedIOException e) {
                        if (++timeouts >= __maxTimeouts) {
                            throw new IOException("Connection timed out.");
                        }
                    } catch (TFTPPacketException e) {
                        throw new IOException("Bad packet: " + e.getMessage());
                    }
                } while(wantReply); // waiting for response

                ack.setBlockNumber(lastBlock);
                sent = ack;
                bytesRead += dataLength;
                totalBytesReceived += dataLength;
            } while (dataLength == TFTPPacket.SEGMENT_SIZE); // not eof
            bufferedSend(sent); // send the final ack
        } finally {
            endBufferedOps();
        }
        return bytesRead;
    }


    /***
     * Requests a named file from a remote host, writes the
     * file to an OutputStream, closes the connection, and returns the number
     * of bytes read.  A local UDP socket must first be created by
     * {@link org.apache.commons.net.DatagramSocketClient#open open()} before
     * invoking this method.  This method will not close the OutputStream
     * containing the file; you must close it after the method invocation.
     *
     * @param filename The name of the file to receive.
     * @param mode     The TFTP mode of the transfer (one of the MODE constants).
     * @param output   The OutputStream to which the file should be written.
     * @param hostname The name of the remote host serving the file.
     * @param port     The port number of the remote TFTP server.
     * @return number of bytes read
     * @throws IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     * @throws UnknownHostException  If the hostname cannot be resolved.
     ***/
    public int receiveFile(String filename, int mode, OutputStream output,
                           String hostname, int port)
    throws UnknownHostException, IOException
    {
        return receiveFile(filename, mode, output, InetAddress.getByName(hostname),
                           port);
    }


    /***
     * Same as calling receiveFile(filename, mode, output, host, TFTP.DEFAULT_PORT).
     *
     * @param filename The name of the file to receive.
     * @param mode     The TFTP mode of the transfer (one of the MODE constants).
     * @param output   The OutputStream to which the file should be written.
     * @param host     The remote host serving the file.
     * @return number of bytes read
     * @throws IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     ***/
    public int receiveFile(String filename, int mode, OutputStream output,
                           InetAddress host)
    throws IOException
    {
        return receiveFile(filename, mode, output, host, DEFAULT_PORT);
    }

    /***
     * Same as calling receiveFile(filename, mode, output, hostname, TFTP.DEFAULT_PORT).
     *
     * @param filename The name of the file to receive.
     * @param mode     The TFTP mode of the transfer (one of the MODE constants).
     * @param output   The OutputStream to which the file should be written.
     * @param hostname The name of the remote host serving the file.
     * @return number of bytes read
     * @throws IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     * @throws UnknownHostException  If the hostname cannot be resolved.
     ***/
    public int receiveFile(String filename, int mode, OutputStream output,
                           String hostname)
    throws UnknownHostException, IOException
    {
        return receiveFile(filename, mode, output, InetAddress.getByName(hostname),
                           DEFAULT_PORT);
    }


    /***
     * Requests to send a file to a remote host, reads the file from an
     * InputStream, sends the file to the remote host, and closes the
     * connection.  A local UDP socket must first be created by
     * {@link org.apache.commons.net.DatagramSocketClient#open open()} before
     * invoking this method.  This method will not close the InputStream
     * containing the file; you must close it after the method invocation.
     *
     * @param filename The name the remote server should use when creating
     *        the file on its file system.
     * @param mode     The TFTP mode of the transfer (one of the MODE constants).
     * @param input the input stream containing the data to be sent
     * @param host     The remote host receiving the file.
     * @param port     The port number of the remote TFTP server.
     * @throws IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     ***/
    public void sendFile(String filename, int mode, InputStream input,
                         InetAddress host, int port) throws IOException
    {
        int block = 0;
        int hostPort = 0;
        boolean justStarted = true;
        boolean lastAckWait = false;

        totalBytesSent = 0L;

        if (mode == TFTP.ASCII_MODE) {
            input = new ToNetASCIIInputStream(input);
        }

        TFTPPacket sent = new TFTPWriteRequestPacket(host, port, filename, mode);
        TFTPDataPacket data = new TFTPDataPacket(host, port, 0, _sendBuffer, 4, 0);

        beginBufferedOps();

        try {
            do { // until eof
                // first time: block is 0, lastBlock is 0, send a request packet.
                // subsequent: block is integer starting at 1, send data packet.
                bufferedSend(sent);
                boolean wantReply = true;
                int timeouts = 0;
                do {
                    try {
                        TFTPPacket received = bufferedReceive();
                        final InetAddress recdAddress = received.getAddress();
                        final int recdPort = received.getPort();
                        // The first time we receive we get the port number and
                        // answering host address (for hosts with multiple IPs)
                        if (justStarted) {
                            justStarted = false;
                            if (recdPort == port) { // must not use the control port here
                                TFTPErrorPacket error = new TFTPErrorPacket(recdAddress,
                                        recdPort, TFTPErrorPacket.UNKNOWN_TID,
                                        "INCORRECT SOURCE PORT");
                                bufferedSend(error);
                                throw new IOException("Incorrect source port ("+recdPort+") in request reply.");
                            }
                            hostPort = recdPort;
                            data.setPort(hostPort);
                            if (!host.equals(recdAddress)) {
                                host = recdAddress;
                                data.setAddress(host);
                                sent.setAddress(host);
                            }
                        }
                        // Comply with RFC 783 indication that an error acknowledgment
                        // should be sent to originator if unexpected TID or host.
                        if (host.equals(recdAddress) && recdPort == hostPort) {

                            switch (received.getType()) {
                            case TFTPPacket.ERROR:
                                TFTPErrorPacket error = (TFTPErrorPacket)received;
                                throw new IOException("Error code " + error.getError() +
                                                      " received: " + error.getMessage());
                            case TFTPPacket.ACKNOWLEDGEMENT:

                                int lastBlock = ((TFTPAckPacket)received).getBlockNumber();

                                if (lastBlock == block) {
                                    ++block;
                                    if (block > 65535) {
                                        // wrap the block number
                                        block = 0;
                                    }
                                    wantReply = false; // got the ack we want
                                } else {
                                    discardPackets();
                                }
                                break;
                            default:
                                throw new IOException("Received unexpected packet type.");
                            }
                        } else { // wrong host or TID; send error
                            TFTPErrorPacket error = new TFTPErrorPacket(recdAddress,
                                                        recdPort,
                                                        TFTPErrorPacket.UNKNOWN_TID,
                                                        "Unexpected host or port.");
                            bufferedSend(error);
                        }
                    } catch (SocketException e) {
                        if (++timeouts >= __maxTimeouts) {
                            throw new IOException("Connection timed out.");
                        }
                    } catch (InterruptedIOException e) {
                        if (++timeouts >= __maxTimeouts) {
                            throw new IOException("Connection timed out.");
                        }
                    } catch (TFTPPacketException e) {
                        throw new IOException("Bad packet: " + e.getMessage());
                    }
                    // retry until a good ack
                } while(wantReply);

                if (lastAckWait) {
                    break; // we were waiting for this; now all done
                }

                int dataLength = TFTPPacket.SEGMENT_SIZE;
                int offset = 4;
                int totalThisPacket = 0;
                int bytesRead = 0;
                while (dataLength > 0 &&
                        (bytesRead = input.read(_sendBuffer, offset, dataLength)) > 0) {
                    offset += bytesRead;
                    dataLength -= bytesRead;
                    totalThisPacket += bytesRead;
                }
                if( totalThisPacket < TFTPPacket.SEGMENT_SIZE ) {
                    /* this will be our last packet -- send, wait for ack, stop */
                    lastAckWait = true;
                }
                data.setBlockNumber(block);
                data.setData(_sendBuffer, 4, totalThisPacket);
                sent = data;
                totalBytesSent += totalThisPacket;
            } while (true); // loops until after lastAckWait is set
        } finally {
            endBufferedOps();
        }
    }


    /***
     * Requests to send a file to a remote host, reads the file from an
     * InputStream, sends the file to the remote host, and closes the
     * connection.  A local UDP socket must first be created by
     * {@link org.apache.commons.net.DatagramSocketClient#open open()} before
     * invoking this method.  This method will not close the InputStream
     * containing the file; you must close it after the method invocation.
     *
     * @param filename The name the remote server should use when creating
     *        the file on its file system.
     * @param mode     The TFTP mode of the transfer (one of the MODE constants).
     * @param input the input stream containing the data to be sent
     * @param hostname The name of the remote host receiving the file.
     * @param port     The port number of the remote TFTP server.
     * @throws IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     * @throws UnknownHostException  If the hostname cannot be resolved.
     ***/
    public void sendFile(String filename, int mode, InputStream input,
                         String hostname, int port)
    throws UnknownHostException, IOException
    {
        sendFile(filename, mode, input, InetAddress.getByName(hostname), port);
    }


    /***
     * Same as calling sendFile(filename, mode, input, host, TFTP.DEFAULT_PORT).
     *
     * @param filename The name the remote server should use when creating
     *        the file on its file system.
     * @param mode     The TFTP mode of the transfer (one of the MODE constants).
     * @param input the input stream containing the data to be sent
     * @param host     The name of the remote host receiving the file.
     * @throws IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     * @throws UnknownHostException  If the hostname cannot be resolved.
     ***/
    public void sendFile(String filename, int mode, InputStream input,
                         InetAddress host)
    throws IOException
    {
        sendFile(filename, mode, input, host, DEFAULT_PORT);
    }

    /***
     * Same as calling sendFile(filename, mode, input, hostname, TFTP.DEFAULT_PORT).
     *
     * @param filename The name the remote server should use when creating
     *        the file on its file system.
     * @param mode     The TFTP mode of the transfer (one of the MODE constants).
     * @param input the input stream containing the data to be sent
     * @param hostname The name of the remote host receiving the file.
     * @throws IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     * @throws UnknownHostException  If the hostname cannot be resolved.
     ***/
    public void sendFile(String filename, int mode, InputStream input,
                         String hostname)
    throws UnknownHostException, IOException
    {
        sendFile(filename, mode, input, InetAddress.getByName(hostname),
                 DEFAULT_PORT);
    }
}
