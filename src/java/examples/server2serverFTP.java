package examples;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/***
 * This is an example program demonstrating how to use the FTPClient class.
 * This program arranges a server to server file transfer that transfers
 * a file from host1 to host2.  Keep in mind, this program might only work
 * if host2 is the same as the host you run it on (for security reasons, 
 * some ftp servers only allow PORT commands to be issued with a host
 * argument equal to the client host).
 * <p>
 * Usage: ftp <host1> <user1> <pass1> <file1> <host2> <user2> <pass2> <file2>
 * <p>
 ***/
public final class server2serverFTP
{

    public static final void main(String[] args)
    {
        String server1, username1, password1, file1;
        String server2, username2, password2, file2;
        FTPClient ftp1, ftp2;
        ProtocolCommandListener listener;

        if (args.length < 8)
        {
            System.err.println(
                "Usage: ftp <host1> <user1> <pass1> <file1> <host2> <user2> <pass2> <file2>"
            );
            System.exit(1);
        }

        server1 = args[0];
        username1 = args[1];
        password1 = args[2];
        file1 = args[3];
        server2 = args[4];
        username2 = args[5];
        password2 = args[6];
        file2 = args[7];

        listener = new PrintCommandListener(new PrintWriter(System.out));
        ftp1 = new FTPClient();
        ftp1.addProtocolCommandListener(listener);
        ftp2 = new FTPClient();
        ftp2.addProtocolCommandListener(listener);

        try
        {
            int reply;
            ftp1.connect(server1);
            System.out.println("Connected to " + server1 + ".");

            reply = ftp1.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftp1.disconnect();
                System.err.println("FTP server1 refused connection.");
                System.exit(1);
            }
        }
        catch (IOException e)
        {
            if (ftp1.isConnected())
            {
                try
                {
                    ftp1.disconnect();
                }
                catch (IOException f)
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
            int reply;
            ftp2.connect(server2);
            System.out.println("Connected to " + server2 + ".");

            reply = ftp2.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftp2.disconnect();
                System.err.println("FTP server2 refused connection.");
                System.exit(1);
            }
        }
        catch (IOException e)
        {
            if (ftp2.isConnected())
            {
                try
                {
                    ftp2.disconnect();
                }
                catch (IOException f)
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
                    "Couldn't initiate transfer.  Check that filenames are valid.");
                break __main;
            }

        }
        catch (IOException e)
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
            catch (IOException e)
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
            catch (IOException e)
            {
                // do nothing
            }
        }
    }
}
