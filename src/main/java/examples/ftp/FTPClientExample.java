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
import java.util.Arrays;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

/***
 * This is an example program demonstrating how to use the FTPClient class.
 * This program connects to an FTP server and retrieves the specified
 * file.  If the -s flag is used, it stores the local file at the FTP server.
 * Just so you can see what's happening, all reply strings are printed.
 * If the -b flag is used, a binary transfer is assumed (default is ASCII).
 * <p>
 * Usage: ftp [-s] [-b] [-l] [-#] [-k nnn] <hostname> <username> <password> <remote file> <local file>
 * <p>
 ***/
public final class FTPClientExample
{

    public static final String USAGE =
        "Usage: ftp [-s] [-b] [-l|-f] [-a] [-e] [-k secs [-w msec]] [-#] <hostname> <username> <password> <remote file> <local file>\n" +
        "\nDefault behavior is to download a file and use ASCII transfer mode.\n" +
        "\t-s store file on server (upload)\n" +
        "\t-l list files (local file is ignored)\n" +
        "\t-f issue FEAT command (local file is ignored)\n" +
        "\t-# add hash display during transfers\n" +
        "\t-k secs use keep-alive timer (setControlKeepAliveTimeout)\n" +
        "\t-w msec wait time for keep-alive reply (setControlKeepAliveReplyTimeout)\n" +
        "\t-a use local active mode (default is local passive)\n" +
        "\t-e use EPSV with IPv4 (default false)\n" +
        "\t-b use binary transfer mode\n";

    public static final void main(String[] args)
    {
        int base = 0;
        boolean storeFile = false, binaryTransfer = false, error = false, listFiles = false;
        boolean localActive = false;
        boolean useEpsvWithIPv4 = false;
        boolean feat = false;
        String server, username, password, remote, local;
        final FTPClient ftp = new FTPClient();

        for (base = 0; base < args.length; base++)
        {
            if (args[base].equals("-s")) {
                storeFile = true;
            }
            else if (args[base].equals("-a")) {
                localActive = true;
            }
            else if (args[base].equals("-b")) {
                binaryTransfer = true;
            }
            else if (args[base].equals("-e")) {
                useEpsvWithIPv4 = true;
            }
            else if (args[base].equals("-f")) {
                feat = true;
            }
            else if (args[base].equals("-l")) {
                listFiles = true;
            }
            else if (args[base].equals("-#")) {
                ftp.setCopyStreamListener(createListener());
            }
            else if (args[base].equals("-k")) {
                ftp.setControlKeepAliveTimeout(Long.parseLong(args[++base]));
            }
            else if (args[base].equals("-w")) {
                ftp.setControlKeepAliveReplyTimeout(Integer.parseInt(args[++base]));
            }
            else {
                break;
            }
        }

        if ((args.length - base) != 5) // server, user, pass, remote, local
        {
            System.err.println(USAGE);
            System.exit(1);
        }

        server = args[base++];
        int port = 21;
        String parts[] = server.split(":");
        if (parts.length == 2){
            server=parts[0];
            port=Integer.parseInt(parts[1]);
        }
        username = args[base++];
        password = args[base++];
        remote = args[base++];
        local = args[base];

        ftp.addProtocolCommandListener(new PrintCommandListener(
                                           new PrintWriter(System.out)));

        try
        {
            int reply;
            ftp.connect(server, port);
            System.out.println("Connected to " + server + ".");

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftp.disconnect();
                System.err.println("FTP server refused connection.");
                System.exit(1);
            }
        }
        catch (IOException e)
        {
            if (ftp.isConnected())
            {
                try
                {
                    ftp.disconnect();
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
            if (!ftp.login(username, password))
            {
                ftp.logout();
                error = true;
                break __main;
            }

            System.out.println("Remote system is " + ftp.getSystemType());

            if (binaryTransfer)
                ftp.setFileType(FTP.BINARY_FILE_TYPE);

            // Use passive mode as default because most of us are
            // behind firewalls these days.
            if (localActive) {
                ftp.enterLocalActiveMode();
            } else {
                ftp.enterLocalPassiveMode();
            }

            ftp.setUseEPSVwithIPv4(useEpsvWithIPv4);

            if (storeFile)
            {
                InputStream input;

                input = new FileInputStream(local);

                ftp.storeFile(remote, input);

                input.close();
            }
            else if (listFiles)
            {
                for (FTPFile f : ftp.listFiles(remote)) {
                    System.out.println(f);
                }

            }
            else if (feat)
            {
                if (ftp.features()) {
                    System.out.println(Arrays.toString(ftp.getReplyStrings()));
                }
            }
            else
            {
                OutputStream output;

                output = new FileOutputStream(local);

                ftp.retrieveFile(remote, output);

                output.close();
            }

            ftp.noop(); // check that control connection is working OK

            ftp.logout();
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
            if (ftp.isConnected())
            {
                try
                {
                    ftp.disconnect();
                }
                catch (IOException f)
                {
                    // do nothing
                }
            }
        }

        System.exit(error ? 1 : 0);
    } // end main

    private static CopyStreamListener createListener(){
        return new CopyStreamListener(){
            private long megsTotal = 0;
            public void bytesTransferred(CopyStreamEvent event) {
                bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
            }

            public void bytesTransferred(long totalBytesTransferred,
                    int bytesTransferred, long streamSize) {
                long megs = totalBytesTransferred / 1000000;
                for (long l = megsTotal; l < megs; l++) {
                    System.err.print("#");
                }
                megsTotal = megs;
            }
        };
    }
}

