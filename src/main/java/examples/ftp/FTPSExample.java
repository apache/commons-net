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

package examples.ftp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

/***
 * This is an example program demonstrating how to use the FTPSClient class.
 * This program connects to an FTP server and retrieves the specified
 * file.  If the -s flag is used, it stores the local file at the FTP server.
 * Just so you can see what's happening, all reply strings are printed.
 * If the -b flag is used, a binary transfer is assumed (default is ASCII).
 * <p>
 * Usage: ftp [-s] [-b] <hostname> <username> <password> <remote file> <local file>
 * <p>
 ***/
public final class FTPSExample
{

    public static final String USAGE =
        "Usage: ftp [-s] [-b] [-l] <hostname> <username> <password> <remote file> <local file>\n" +
        "\nDefault behavior is to download a file and use ASCII transfer mode.\n" +
        "\t-l directory listing (local file is ignored)\n" +
        "\t-s store file on server (upload)\n" +
        "\t-b use binary transfer mode\n";

    public static final void main(String[] args) throws NoSuchAlgorithmException
    {
        int base = 0;
        boolean storeFile = false, binaryTransfer = false, error = false, listing = false;
        String server, username, password, remote, local;
        String protocol = "SSL";    // SSL/TLS
        FTPSClient ftps;

        for (base = 0; base < args.length; base++)
        {
            if (args[base].equals("-s"))
                storeFile = true;
            else if (args[base].equals("-b"))
                binaryTransfer = true;
            else if (args[base].equals("-l"))
                listing = true;
            else
                break;
        }

        if ((args.length - base) != 5)
        {
            System.err.println(USAGE);
            System.exit(1);
        }

        server = args[base++];
        String parts[] = server.split(":");
        int port = 0;
        if (parts.length == 2) {
            server = parts[0];
            port = Integer.parseInt(parts[1]);
        }
        username = args[base++];
        password = args[base++];
        remote = args[base++];
        local = args[base];

        ftps = new FTPSClient(protocol);

        ftps.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        try
        {
            int reply;

            if (port > 0) {
                ftps.connect(server, port);
            } else {
                ftps.connect(server);
            }
            System.out.println("Connected to " + server + ".");

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftps.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftps.disconnect();
                System.err.println("FTP server refused connection.");
                System.exit(1);
            }
        }
        catch (IOException e)
        {
            if (ftps.isConnected())
            {
                try
                {
                    ftps.disconnect();
                }
                catch (IOException f)
                {
                    // do nothing
                }
            }
            System.err.println("Could not connect to server.");
            e.printStackTrace();
            System.exit(1);
        }

__main:
        try
        {
            ftps.setBufferSize(1000);

            if (!ftps.login(username, password))
            {
                ftps.logout();
                error = true;
                break __main;
            }


            System.out.println("Remote system is " + ftps.getSystemType());

            if (binaryTransfer) ftps.setFileType(FTP.BINARY_FILE_TYPE);

            // Use passive mode as default because most of us are
            // behind firewalls these days.
            ftps.enterLocalPassiveMode();

            if (storeFile)
            {
                InputStream input;

                input = new FileInputStream(local);

                ftps.storeFile(remote, input);

                input.close();
            }
            else if (listing)
            {
                for (FTPFile f : ftps.listFiles(remote)) {
                    System.out.println(f);
                }
            }
            else
            {
                OutputStream output;

                output = new FileOutputStream(local);

                ftps.retrieveFile(remote, output);

                output.close();
            }

            ftps.logout();
        }
        catch (FTPConnectionClosedException e)
        {
            error = true;
            System.err.println("Server closed connection.");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            error = true;
            e.printStackTrace();
        }
        finally
        {
            if (ftps.isConnected())
            {
                try
                {
                    ftps.disconnect();
                }
                catch (IOException f)
                {
                    // do nothing
                }
            }
        }

        System.exit(error ? 1 : 0);
    } // end main

}
