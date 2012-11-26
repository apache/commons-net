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

package examples.mail;

import java.io.IOException;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.imap.IMAPClient;
import org.apache.commons.net.imap.IMAPSClient;

/**
 * This is an example program demonstrating how to use the IMAP[S]Client class.
 * This program connects to a IMAP[S] server, lists its capabilities and shows
 * the status of the inbox.
 * <p>
 * Usage: IMAPMail <imap[s] server hostname> <username> <password> [secure protocol, e.g. TLS]
 * <p>
 */
public final class IMAPMail
{

    public static void main(String[] args)
    {
        if (args.length < 3)
        {
            System.err.println(
                "Usage: IMAPMail <imap server hostname> <username> <password> [TLS]");
            System.exit(1);
        }

        String server = args[0];
        String username = args[1];
        String password = args[2];

        String proto = (args.length > 3) ? args[3] : null;

        IMAPClient imap;

        if (proto != null) {
            System.out.println("Using secure protocol: " + proto);
            imap = new IMAPSClient(proto, true); // implicit
            // enable the next line to only check if the server certificate has expired (does not check chain):
//            ((IMAPSClient) imap).setTrustManager(TrustManagerUtils.getValidateServerCertificateTrustManager());
            // OR enable the next line if the server uses a self-signed certificate (no checks)
//            ((IMAPSClient) imap).setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
        } else {
            imap = new IMAPClient();
        }
        System.out.println("Connecting to server " + server + " on " + imap.getDefaultPort());

        // We want to timeout if a response takes longer than 60 seconds
        imap.setDefaultTimeout(60000);

        // suppress login details
        imap.addProtocolCommandListener(new PrintCommandListener(System.out, true));

        try
        {
            imap.connect(server);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not connect to server.", e);
        }

        try
        {
            if (!imap.login(username, password))
            {
                System.err.println("Could not login to server. Check password.");
                imap.disconnect();
                System.exit(3);
            }

            imap.setSoTimeout(6000);

            imap.capability();

            imap.select("inbox");

            imap.examine("inbox");

            imap.status("inbox", new String[]{"MESSAGES"});

            imap.logout();
            imap.disconnect();
        }
        catch (IOException e)
        {
            System.out.println(imap.getReplyString());
            e.printStackTrace();
            System.exit(10);
            return;
        }
    }
}

/* kate: indent-width 4; replace-tabs on; */
