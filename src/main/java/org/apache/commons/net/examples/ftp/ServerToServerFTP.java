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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * This is an example program demonstrating how to use the FTPClient class.
 * This program arranges a server to server file transfer that transfers
 * a file from host1 to host2.  Keep in mind, this program might only work
 * if host2 is the same as the host you run it on (for security reasons,
 * some ftp servers only allow PORT commands to be issued with a host
 * argument equal to the client host).
 * <p>
 * Usage: ftp <host1> <user1> <pass1> <file1> <host2> <user2> <pass2> <file2>
 */
public final class ServerToServerFTP
{

    public static void main(final String[] args)
    {
        String server1;
        final String username1;
        final String password1;
        final String file1;
        String server2;
        final String username2;
        final String password2;
        final String file2;
        String [] parts;
        int port1=0, port2=0;
        final FTPClient ftp1;
        final FTPClient ftp2;
        final ProtocolCommandListener listener;

        if (args.length < 8)
        {
            System.err.println(
                "Usage: ftp <host1> <user1> <pass1> <file1> <host2> <user2> <pass2> <file2>"
            );
            System.exit(1);
        }

        server1 = args[0];
        parts = server1.split(":");
        if (parts.length == 2) {
            server1=parts[0];
            port1 = Integer.parseInt(parts[1]);
        }
        username1 = args[1];
        password1 = args[2];
        file1 = args[3];
        server2 = args[4];
        parts = server2.split(":");
        if (parts.length == 2) {
            server2=parts[0];
            port2 = Integer.parseInt(parts[1]);
        }
        username2 = args[5];
        password2 = args[6];
        file2 = args[7];

        listener = new PrintCommandListener(new PrintWriter(System.out), true);
        ftp1 = new FTPClient();
        ftp1.addProtocolCommandListener(listener);
        ftp2 = new FTPClient();
        ftp2.addProtocolCommandListener(listener);

        try
        {
            final int reply;
            if (port1 > 0) {
                ftp1.connect(server1, port1);
            } else {
                ftp1.connect(server1);
            }
            System.out.println("Connected to " + server1 + ".");

            reply = ftp1.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftp1.disconnect();
                System.err.println("FTP server1 refused connection.");
                System.exit(1);
            }
        }
        catch (final IOException e)
        {
            if (ftp1.isConnected())
            {
                try
                {
                    ftp1.disconnect();
                }
                catch (final IOException f)
                {
                    // do nothing
                }
            }
            System.err.println("Could not connect to server1.");
            e.printStackTrace();
            System.exit(1);
        }

        try
        {
            final int reply;
            if (port2 > 0) {
                ftp2.connect(server2, port2);
            } else {
                ftp2.connect(server2);
            }
            System.out.println("Connected to " + server2 + ".");

            reply = ftp2.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftp2.disconnect();
                System.err.println("FTP server2 refused connection.");
                System.exit(1);
            }
        }
        catch (final IOException e)
        {
            if (ftp2.isConnected())
            {
                try
                {
                    ftp2.disconnect();
                }
                catch (final IOException f)
                {
                    // do nothing
                }
            }
            System.err.println("Could not connect to server2.");
            e.printStackTrace();
            System.exit(1);
        }

__main:
        try
        {
            if (!ftp1.login(username1, password1))
            {
                System.err.println("Could not login to " + server1);
                break __main;
            }

            if (!ftp2.login(username2, password2))
            {
                System.err.println("Could not login to " + server2);
                break __main;
            }

            // Let's just assume success for now.
            ftp2.enterRemotePassiveMode();

            ftp1.enterRemoteActiveMode(InetAddress.getByName(ftp2.getPassiveHost()),
                                       ftp2.getPassivePort());

            // Although you would think the store command should be sent to server2
            // first, in reality, ftp servers like wu-ftpd start accepting data
            // connections right after entering passive mode.  Additionally, they
            // don't even send the positive preliminary reply until after the
            // transfer is completed (in the case of passive mode transfers).
            // Therefore, calling store first would hang waiting for a preliminary
            // reply.
            if (ftp1.remoteRetrieve(file1) && ftp2.remoteStoreUnique(file2))
            {
                //      if(ftp1.remoteRetrieve(file1) && ftp2.remoteStore(file2)) {
                // We have to fetch the positive completion reply.
                ftp1.completePendingCommand();
                ftp2.completePendingCommand();
            }
            else
            {
                System.err.println(
                    "Couldn't initiate transfer. Check that file names are valid.");
                break __main;
            }

        }
        catch (final IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        finally
        {
            try
            {
                if (ftp1.isConnected())
                {
                    ftp1.logout();
                    ftp1.disconnect();
                }
            }
            catch (final IOException e)
            {
                // do nothing
            }

            try
            {
                if (ftp2.isConnected())
                {
                    ftp2.logout();
                    ftp2.disconnect();
                }
            }
            catch (final IOException e)
            {
                // do nothing
            }
        }
    }
}
