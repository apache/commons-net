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
 * <p>
 * <p>
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
     * <p>
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
     * <p>
     * @return The maximum number of timeouts allowed.
     ***/
    public int getMaxTimeouts()
    {
        return __maxTimeouts;
    }


    /***
     * Requests a named file from a remote host, writes the
     * file to an OutputStream, closes the connection, and returns the number
     * of bytes read.  A local UDP socket must first be created by
     * {@link org.apache.commons.net.DatagramSocketClient#open open()} before
     * invoking this method.  This method will not close the OutputStream
     * containing the file; you must close it after the method invocation.
     * <p>
     * @param filename  The name of the file to receive.
     * @param mode   The TFTP mode of the transfer (one of the MODE constants).
     * @param output The OutputStream to which the file should be written.
     * @param host   The remote host serving the file.
     * @param port   The port number of the remote TFTP server.
     * @exception IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     ***/
    public int receiveFile(String filename, int mode, OutputStream output,
                           InetAddress host, int port) throws IOException
    {
        int bytesRead, timeouts, lastBlock, block, hostPort, dataLength;
        TFTPPacket sent, received = null;
        TFTPErrorPacket error;
        TFTPDataPacket data;
        TFTPAckPacket ack = new TFTPAckPacket(host, port, 0);

        beginBufferedOps();

        dataLength = lastBlock = hostPort = bytesRead = 0;
        block = 1;

        if (mode == TFTP.ASCII_MODE) {
            output = new FromNetASCIIOutputStream(output);
        }

        sent =
            new TFTPReadRequestPacket(host, port, filename, mode);

_sendPacket:
        do
        {
            bufferedSend(sent);

_receivePacket:
            while (true)
            {
                timeouts = 0;
                do {
                    try
                    {
                        received = bufferedReceive();
                        break;
                    }
                    catch (SocketException e)
                    {
                        if (++timeouts >= __maxTimeouts)
                        {
                            endBufferedOps();
                            throw new IOException("Connection timed out.");
                        }
                        continue _sendPacket;
                    }
                    catch (InterruptedIOException e)
                    {
                        if (++timeouts >= __maxTimeouts)
                        {
                            endBufferedOps();
                            throw new IOException("Connection timed out.");
                        }
                        continue _sendPacket;
                    }
                    catch (TFTPPacketException e)
                    {
                        endBufferedOps();
                        throw new IOException("Bad packet: " + e.getMessage());
                    }
                } while (timeouts < __maxTimeouts); // __maxTimeouts >=1 so will always do loop at least once

                // The first time we receive we get the port number and
                // answering host address (for hosts with multiple IPs)
                if (lastBlock == 0)
                {
                    hostPort = received.getPort();
                    ack.setPort(hostPort);
                    if(!host.equals(received.getAddress()))
                    {
                        host = received.getAddress();
                        ack.setAddress(host);
                        sent.setAddress(host);
                    }
                }

                // Comply with RFC 783 indication that an error acknowledgment
                // should be sent to originator if unexpected TID or host.
                if (host.equals(received.getAddress()) &&
                        received.getPort() == hostPort)
                {

                    switch (received.getType())
                    {
                    case TFTPPacket.ERROR:
                        error = (TFTPErrorPacket)received;
                        endBufferedOps();
                        throw new IOException("Error code " + error.getError() +
                                              " received: " + error.getMessage());
                    case TFTPPacket.DATA:
                        data = (TFTPDataPacket)received;
                        dataLength = data.getDataLength();

                        lastBlock = data.getBlockNumber();

                        if (lastBlock == block)
                        {
                            try
                            {
                                output.write(data.getData(), data.getDataOffset(),
                                             dataLength);
                            }
                            catch (IOException e)
                            {
                                error = new TFTPErrorPacket(host, hostPort,
                                                            TFTPErrorPacket.OUT_OF_SPACE,
                                                            "File write failed.");
                                bufferedSend(error);
                                endBufferedOps();
                                throw e;
                            }
                            ++block;
                            if (block > 65535)
                            {
                                // wrap the block number
                                block = 0;
                            }

                            break _receivePacket;
                        }
                        else
                        {
                            discardPackets();

                            if (lastBlock == (block == 0 ? 65535 : (block - 1))) {
                                continue _sendPacket;  // Resend last acknowledgement.
                            }

                            continue _receivePacket; // Start fetching packets again.
                        }
                        //break;

                    default:
                        endBufferedOps();
                        throw new IOException("Received unexpected packet type.");
                    }
                }
                else
                {
                    error = new TFTPErrorPacket(received.getAddress(),
                                                received.getPort(),
                                                TFTPErrorPacket.UNKNOWN_TID,
                                                "Unexpected host or port.");
                    bufferedSend(error);
                    continue _sendPacket;
                }

                // We should never get here, but this is a safety to avoid
                // infinite loop.  If only Java had the goto statement.
                //break;
            }

            ack.setBlockNumber(lastBlock);
            sent = ack;
            bytesRead += dataLength;
        } // First data packet less than 512 bytes signals end of stream.

        while (dataLength == TFTPPacket.SEGMENT_SIZE);

        bufferedSend(sent);
        endBufferedOps();

        return bytesRead;
    }


    /***
     * Requests a named file from a remote host, writes the
     * file to an OutputStream, closes the connection, and returns the number
     * of bytes read.  A local UDP socket must first be created by
     * {@link org.apache.commons.net.DatagramSocketClient#open open()} before
     * invoking this method.  This method will not close the OutputStream
     * containing the file; you must close it after the method invocation.
     * <p>
     * @param filename The name of the file to receive.
     * @param mode     The TFTP mode of the transfer (one of the MODE constants).
     * @param output   The OutputStream to which the file should be written.
     * @param hostname The name of the remote host serving the file.
     * @param port     The port number of the remote TFTP server.
     * @exception IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     * @exception UnknownHostException  If the hostname cannot be resolved.
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
     * @exception IOException If an I/O error occurs.  The nature of the
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
     * @exception IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     * @exception UnknownHostException  If the hostname cannot be resolved.
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
     * <p>
     * @param filename The name the remote server should use when creating
     *        the file on its file system.
     * @param mode     The TFTP mode of the transfer (one of the MODE constants).
     * @param host     The remote host receiving the file.
     * @param port     The port number of the remote TFTP server.
     * @exception IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     ***/
    public void sendFile(String filename, int mode, InputStream input,
                         InetAddress host, int port) throws IOException
    {
        int bytesRead, timeouts, lastBlock, block, hostPort, dataLength, offset, totalThisPacket;
        TFTPPacket sent, received = null;
        TFTPErrorPacket error;
        TFTPDataPacket data =
            new TFTPDataPacket(host, port, 0, _sendBuffer, 4, 0);
        TFTPAckPacket ack;

        boolean justStarted = true;

        beginBufferedOps();

        dataLength = lastBlock = hostPort = bytesRead = totalThisPacket = 0;
        block = 0;
        boolean lastAckWait = false;

        if (mode == TFTP.ASCII_MODE) {
            input = new ToNetASCIIInputStream(input);
        }

        sent =
            new TFTPWriteRequestPacket(host, port, filename, mode);

_sendPacket:
        do
        {
            // first time: block is 0, lastBlock is 0, send a request packet.
            // subsequent: block is integer starting at 1, send data packet.
            bufferedSend(sent);

            // this is trying to receive an ACK
_receivePacket:
            while (true)
            {


                timeouts = 0;
                do {
                    try
                    {
                        received = bufferedReceive();
                        break;
                    }
                    catch (SocketException e)
                    {
                        if (++timeouts >= __maxTimeouts)
                        {
                            endBufferedOps();
                            throw new IOException("Connection timed out.");
                        }
                        continue _sendPacket;
                    }
                    catch (InterruptedIOException e)
                    {
                        if (++timeouts >= __maxTimeouts)
                        {
                            endBufferedOps();
                            throw new IOException("Connection timed out.");
                        }
                        continue _sendPacket;
                    }
                    catch (TFTPPacketException e)
                    {
                        endBufferedOps();
                        throw new IOException("Bad packet: " + e.getMessage());
                    }
                } // end of while loop over tries to receive
                while (timeouts < __maxTimeouts); // __maxTimeouts >=1 so will always do loop at least once


                // The first time we receive we get the port number and
                // answering host address (for hosts with multiple IPs)
                if (justStarted)
                {
                    justStarted = false;
                    hostPort = received.getPort();
                    data.setPort(hostPort);
                    if(!host.equals(received.getAddress()))
                    {
                        host = received.getAddress();
                        data.setAddress(host);
                        sent.setAddress(host);
                    }
                }

                // Comply with RFC 783 indication that an error acknowledgment
                // should be sent to originator if unexpected TID or host.
                if (host.equals(received.getAddress()) &&
                        received.getPort() == hostPort)
                {

                    switch (received.getType())
                    {
                    case TFTPPacket.ERROR:
                        error = (TFTPErrorPacket)received;
                        endBufferedOps();
                        throw new IOException("Error code " + error.getError() +
                                              " received: " + error.getMessage());
                    case TFTPPacket.ACKNOWLEDGEMENT:
                        ack = (TFTPAckPacket)received;

                        lastBlock = ack.getBlockNumber();

                        if (lastBlock == block)
                        {
                            ++block;
                            if (block > 65535)
                            {
                                // wrap the block number
                                block = 0;
                            }
                            if (lastAckWait) {

                              break _sendPacket;
                            }
                            else {
                              break _receivePacket;
                            }
                        }
                        else
                        {
                            discardPackets();

                            continue _receivePacket; // Start fetching packets again.
                        }
                        //break;

                    default:
                        endBufferedOps();
                        throw new IOException("Received unexpected packet type.");
                    }
                }
                else
                {
                    error = new TFTPErrorPacket(received.getAddress(),
                                                received.getPort(),
                                                TFTPErrorPacket.UNKNOWN_TID,
                                                "Unexpected host or port.");
                    bufferedSend(error);
                    continue _sendPacket;
                }

                // We should never get here, but this is a safety to avoid
                // infinite loop.  If only Java had the goto statement.
                //break;
            }

            // OK, we have just gotten ACK about the last data we sent. Make another
            // and send it

            dataLength = TFTPPacket.SEGMENT_SIZE;
            offset = 4;
            totalThisPacket = 0;
            while (dataLength > 0 &&
                    (bytesRead = input.read(_sendBuffer, offset, dataLength)) > 0)
            {
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
        }
        while ( totalThisPacket > 0 || lastAckWait );
        // Note: this was looping while dataLength == 0 || lastAckWait,
        // which was discarding the last packet if it was not full size
        // Should send the packet.

        endBufferedOps();
    }


    /***
     * Requests to send a file to a remote host, reads the file from an
     * InputStream, sends the file to the remote host, and closes the
     * connection.  A local UDP socket must first be created by
     * {@link org.apache.commons.net.DatagramSocketClient#open open()} before
     * invoking this method.  This method will not close the InputStream
     * containing the file; you must close it after the method invocation.
     * <p>
     * @param filename The name the remote server should use when creating
     *        the file on its file system.
     * @param mode     The TFTP mode of the transfer (one of the MODE constants).
     * @param hostname The name of the remote host receiving the file.
     * @param port     The port number of the remote TFTP server.
     * @exception IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     * @exception UnknownHostException  If the hostname cannot be resolved.
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
     * @param host     The name of the remote host receiving the file.
     * @exception IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     * @exception UnknownHostException  If the hostname cannot be resolved.
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
     * @param hostname The name of the remote host receiving the file.
     * @exception IOException If an I/O error occurs.  The nature of the
     *            error will be reported in the message.
     * @exception UnknownHostException  If the hostname cannot be resolved.
     ***/
    public void sendFile(String filename, int mode, InputStream input,
                         String hostname)
    throws UnknownHostException, IOException
    {
        sendFile(filename, mode, input, InetAddress.getByName(hostname),
                 DEFAULT_PORT);
    }
}
