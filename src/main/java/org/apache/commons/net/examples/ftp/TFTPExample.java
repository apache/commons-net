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

package org.apache.commons.net.examples.ftp;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.net.tftp.TFTP;
import org.apache.commons.net.tftp.TFTPClient;
import org.apache.commons.net.tftp.TFTPPacket;

/**
 * This is an example of a simple Java tftp client.
 * Notice how all of the code is really just argument processing and
 * error handling.
 * <p>
 * Usage: tftp [options] hostname localfile remotefile
 * hostname   - The name of the remote host, with optional :port
 * localfile  - The name of the local file to send or the name to use for
 *              the received file
 * remotefile - The name of the remote file to receive or the name for
 *              the remote server to use to name the local file being sent.
 * options: (The default is to assume -r -b)
 *        -s Send a local file
 *        -r Receive a remote file
 *        -a Use ASCII transfer mode
 *        -b Use binary transfer mode
 */
public final class TFTPExample
{
    static final String USAGE =
        "Usage: tftp [options] hostname localfile remotefile\n\n" +
        "hostname   - The name of the remote host [:port]\n" +
        "localfile  - The name of the local file to send or the name to use for\n" +
        "\tthe received file\n" +
        "remotefile - The name of the remote file to receive or the name for\n" +
        "\tthe remote server to use to name the local file being sent.\n\n" +
        "options: (The default is to assume -r -b)\n" +
        "\t-t timeout in seconds (default 60s)\n" +
        "\t-s Send a local file\n" +
        "\t-r Receive a remote file\n" +
        "\t-a Use ASCII transfer mode\n" +
        "\t-b Use binary transfer mode\n" +
        "\t-v Verbose (trace packets)\n"
        ;

    public static void main(final String[] args)
    {
        boolean receiveFile = true, closed;
        int transferMode = TFTP.BINARY_MODE, argc;
        String arg;
        final String hostname;
        final String localFilename;
        final String remoteFilename;
        final TFTPClient tftp;
        int timeout = 60000;
        boolean verbose = false;

        // Parse options
        for (argc = 0; argc < args.length; argc++)
        {
            arg = args[argc];
            if (arg.startsWith("-"))
            {
                if (arg.equals("-r")) {
                    receiveFile = true;
                } else if (arg.equals("-s")) {
                    receiveFile = false;
                } else if (arg.equals("-a")) {
                    transferMode = TFTP.ASCII_MODE;
                } else if (arg.equals("-b")) {
                    transferMode = TFTP.BINARY_MODE;
                } else if (arg.equals("-t")) {
                    timeout = 1000*Integer.parseInt(args[++argc]);
                } else if (arg.equals("-v")) {
                    verbose = true;
                } else {
                    System.err.println("Error: unrecognized option.");
                    System.err.print(USAGE);
                    System.exit(1);
                }
            } else {
                break;
            }
        }

        // Make sure there are enough arguments
        if (args.length - argc != 3)
        {
            System.err.println("Error: invalid number of arguments.");
            System.err.print(USAGE);
            System.exit(1);
        }

        // Get host and file arguments
        hostname = args[argc];
        localFilename = args[argc + 1];
        remoteFilename = args[argc + 2];

        // Create our TFTP instance to handle the file transfer.
        if (verbose) {
            tftp = new TFTPClient() {
                @Override
                protected void trace(final String direction, final TFTPPacket packet) {
                    System.out.println(direction + " " + packet);
                }
            };
        } else {
            tftp = new TFTPClient();
        }

        // We want to timeout if a response takes longer than 60 seconds
        tftp.setDefaultTimeout(timeout);

        // We haven't closed the local file yet.
        closed = false;

        // If we're receiving a file, receive, otherwise send.
        if (receiveFile)
        {
            closed = receive(transferMode, hostname, localFilename, remoteFilename, tftp);
        } else {
            // We're sending a file
            closed = send(transferMode, hostname, localFilename, remoteFilename, tftp);
        }

        System.out.println("Recd: "+tftp.getTotalBytesReceived()+" Sent: "+tftp.getTotalBytesSent());

        if (!closed) {
            System.out.println("Failed");
            System.exit(1);
        }

        System.out.println("OK");
    }

    private static boolean send(final int transferMode, final String hostname, final String localFilename,
            final String remoteFilename, final TFTPClient tftp) {
        final boolean closed;
        FileInputStream input = null;

        // Try to open local file for reading
        try
        {
            input = new FileInputStream(localFilename);
        }
        catch (final IOException e)
        {
            tftp.close();
            throw new RuntimeException("Error: could not open local file for reading.", e);
        }

        open(tftp);

        // Try to send local file via TFTP
        try
        {
            final String [] parts = hostname.split(":");
            if (parts.length == 2) {
                tftp.sendFile(remoteFilename, transferMode, input, parts[0], Integer.parseInt(parts[1]));
            } else {
                tftp.sendFile(remoteFilename, transferMode, input, hostname);
            }
        }
        catch (final UnknownHostException e)
        {
            System.err.println("Error: could not resolve hostname.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (final IOException e)
        {
            System.err.println("Error: I/O exception occurred while sending file.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
        finally
        {
            // Close local socket and input file
            closed = close(tftp, input);
        }

        return closed;
    }

    private static boolean receive(final int transferMode, final String hostname, final String localFilename,
            final String remoteFilename, final TFTPClient tftp) {
        final boolean closed;
        FileOutputStream output = null;
        final File file;

        file = new File(localFilename);

        // If file exists, don't overwrite it.
        if (file.exists())
        {
            System.err.println("Error: " + localFilename + " already exists.");
            return false;
        }

        // Try to open local file for writing
        try
        {
            output = new FileOutputStream(file);
        }
        catch (final IOException e)
        {
            tftp.close();
            throw new RuntimeException("Error: could not open local file for writing.", e);
        }

        open(tftp);

        // Try to receive remote file via TFTP
        try
        {
            final String [] parts = hostname.split(":");
            if (parts.length == 2) {
                tftp.receiveFile(remoteFilename, transferMode, output, parts[0], Integer.parseInt(parts[1]));
            } else {
                tftp.receiveFile(remoteFilename, transferMode, output, hostname);
            }
        }
        catch (final UnknownHostException e)
        {
            System.err.println("Error: could not resolve hostname.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
        catch (final IOException e)
        {
            System.err.println(
                "Error: I/O exception occurred while receiving file.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
        finally
        {
            // Close local socket and output file
            closed = close(tftp, output);
        }

        return closed;
    }

    private static boolean close(final TFTPClient tftp, final Closeable output) {
        boolean closed;
        tftp.close();
        try
        {
            if (output != null) {
                output.close();
            }
            closed = true;
        }
        catch (final IOException e)
        {
            closed = false;
            System.err.println("Error: error closing file.");
            System.err.println(e.getMessage());
        }
        return closed;
    }

    private static void open(final TFTPClient tftp) {
        try
        {
            tftp.open();
        }
        catch (final SocketException e)
        {
            throw new RuntimeException("Error: could not open local UDP socket.", e);
        }
    }

}


