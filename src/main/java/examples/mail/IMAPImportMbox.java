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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.net.imap.IMAPClient;
import org.apache.commons.net.imap.IMAPSClient;

/**
 * This is an example program demonstrating how to use the IMAP[S]Client class.
 * This program connects to a IMAP[S] server and imports messages into the folder from an mbox file.
 * <p>
 * Usage: IMAPMail <imap[s] server hostname> <folder> <username> <password> <mboxfile> [secure protocol, e.g. TLS]
 * <p>
 */
public final class IMAPImportMbox
{

    private static final String CRLF = "\r\n";

    public static void main(String[] args) throws IOException
    {
        if (args.length < 5)
        {
            System.err.println(
                "Usage: IMAPImportMbox <imap server hostname> <folder> <username> <password> <mboxfile> [TLS]");
            System.exit(1);
        }

        final String server   = args[0];
        final String folder   = args[1];
        final String username = args[2];
        final String password = args[3];
        final String file     = args[4];
        final String proto = (args.length > 5) ? args[5] : null;

        final File mbox = new File(file);
        if (!mbox.isFile() || !mbox.canRead()) {
            throw new IOException("Cannot read mailbox file: " + mbox);
        }

        IMAPClient imap;

        if (proto != null) {
            System.out.println("Using secure protocol: " + proto);
            imap = new IMAPSClient(proto, true); // implicit
        } else {
            imap = new IMAPClient();
        }
        System.out.println("Connecting to server " + server + " on " + imap.getDefaultPort());

        // We want to timeout if a response takes longer than 60 seconds
        imap.setDefaultTimeout(60000);

        try {
            imap.connect(server);
        } catch (IOException e) {
            throw new RuntimeException("Could not connect to server.", e);
        }

        try {
            if (!imap.login(username, password)) {
                System.err.println("Could not login to server. Check password.");
                imap.disconnect();
                System.exit(3);
            }

            imap.setSoTimeout(6000);

            final BufferedReader br = new BufferedReader(new FileReader(file)); // TODO charset?

            String line;
            StringBuilder sb = new StringBuilder();
            while((line=br.readLine())!=null) {
                if (line.startsWith("From ")) { // start of message; i.e. end of previous (if any)
                    process(sb, imap, folder);
                    sb.setLength(0);
                } else if (line.startsWith(">From ")) { // Unescape "From " in body text
                    line = line.substring(1);
                }
                // TODO process first Received: line to determine arrival date?
                sb.append(line);
                sb.append(CRLF);
            }
            br.close();
            process(sb, imap, folder); // end of last message (if any)
        } catch (IOException e) {
            System.out.println(imap.getReplyString());
            e.printStackTrace();
            System.exit(10);
            return;
        } finally {
            imap.logout();
            imap.disconnect();
        }
    }

    private static void process(final StringBuilder sb, final IMAPClient imap, final String folder) throws IOException {
        final int length = sb.length();
        if (length > 2) {
            System.out.println("Length " + length);
            sb.setLength(length-2); // drop trailing CRLF
            String msg = sb.toString();
            if (!imap.append(folder, null, null, msg)) {
                throw new IOException("Failed to import message: " + imap.getReplyString());
            }
        }
    }
}
