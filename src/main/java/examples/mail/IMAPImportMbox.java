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
import java.net.URI;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.net.imap.IMAPClient;
import org.apache.commons.net.imap.IMAPSClient;

/**
 * This is an example program demonstrating how to use the IMAP[S]Client class.
 * This program connects to a IMAP[S] server and imports messages into the folder from an mbox file.
 * <p>
 * Usage: IMAPImportMbox imap[s]://user:password@host[:port]/folder/path <mboxfile> [selectors]
 * <p>
 * An example selector might be:
 * <ul>
 * <li>1,2,3,7-10</li>
 * <li>-142986- : this is useful for retrieving messages by apmail number, which appears as From xyz-return-142986-apmail-...</li>
 * <ul>
 */
public final class IMAPImportMbox
{

    private static final String CRLF = "\r\n";

    public static void main(String[] args) throws IOException
    {
        if (args.length < 2)
        {
            System.err.println("Usage: IMAPImportMbox imap[s]://user:password@host[:port]/folder/path <mboxfile> [selectors]");
            System.err.println("\tWhere: a selector is a list of numbers/number ranges - 1,2,3-10 - or a list of strings to match in the initial From line");
            System.exit(1);
        }

        final URI uri      = URI.create(args[0]);
        final String file  = args[1];

        final File mbox = new File(file);
        if (!mbox.isFile() || !mbox.canRead()) {
            throw new IOException("Cannot read mailbox file: " + mbox);
        }

        final String userInfo = uri.getUserInfo();
        if (userInfo == null) {
            throw new IllegalArgumentException("Missing userInfo details");
        }

        String []userpass = userInfo.split(":");
        if (userpass.length != 2) {
            throw new IllegalArgumentException("Invalid userInfo details: '" + userpass + "'");            
        }

        String username = userpass[0];
        String password = userpass[1];

        String path = uri.getPath();
        if (path == null || path.length() < 1) {
            throw new IllegalArgumentException("Invalid folderPath: '" + path + "'");
        }
        String folder = path.substring(1); // skip the leading /

        List<String> contains = new ArrayList<String>(); // list of strings to find
        BitSet msgNums = new BitSet(); // list of message numbers

        for(int i = 2; i < args.length; i++) {
            String arg = args[i];
            if (arg.matches("\\d+(-\\d+)?(,\\d+(-\\d+)?)*")) { // number,m-n
                for(String entry : arg.split(",")) {
                    String []parts = entry.split("-");
                    if (parts.length == 2) { // m-n
                        int low = Integer.parseInt(parts[0]);
                        int high = Integer.parseInt(parts[1]);
                        for(int j=low; j <= high; j++) {
                            msgNums.set(j);
                        }
                    } else {
                        msgNums.set(Integer.parseInt(entry));
                    }
                }
            } else {
                contains.add(arg); // not a number/number range
            }
        }
//        System.out.println(msgNums.toString());
//        System.out.println(java.util.Arrays.toString(contains.toArray()));

        final IMAPClient imap;

        if ("imaps".equalsIgnoreCase(uri.getScheme())) {
            System.out.println("Using secure protocol");
            imap = new IMAPSClient(true); // implicit
//        } else if ("null".equals(uri.getScheme())) {
//            imap = new IMAPClient(){
//                @Override
//                public void connect(String host){ }
//                @Override
//                public boolean login(String user, String pass) {return true;}
//                @Override
//                public boolean logout() {return true;}
//                @Override
//                public void disconnect() {}
//                @Override
//                public void setSoTimeout(int t) {}
//                @Override
//                public boolean append(String mailboxName, String flags, String datetime, String message) {return true;}
//            };
        } else {
            imap = new IMAPClient();
        }

        String server = uri.getHost();
        int port = uri.getPort();
        if (port != -1) {
            imap.setDefaultPort(port);
        }

        System.out.println("Connecting to server " + server + " on " + imap.getDefaultPort());

        // We want to timeout if a response takes longer than 60 seconds
        imap.setDefaultTimeout(60000);

        try {
            imap.connect(server);
        } catch (IOException e) {
            throw new RuntimeException("Could not connect to server.", e);
        }

        int total = 0;
        int loaded = 0;
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
            boolean wanted = false; // Skip any leading rubbish
            while((line=br.readLine())!=null) {
                if (line.startsWith("From ")) { // start of message; i.e. end of previous (if any)
                    if (process(sb, imap, folder, total)) { // process previous message (if any)
                        loaded++;
                    }
                    sb.setLength(0);
                    total ++;
                    wanted = wanted(total, line, msgNums, contains);
                } else if (line.startsWith(">From ")) { // Unescape "From " in body text
                    line = line.substring(1);
                }
                // TODO process first Received: line to determine arrival date?
                if (wanted) {
                    sb.append(line);
                    sb.append(CRLF);
                }
            }
            br.close();
            if (wanted && process(sb, imap, folder, total)) { // last message (if any)
                loaded++;
            }
        } catch (IOException e) {
            System.out.println(imap.getReplyString());
            e.printStackTrace();
            System.exit(10);
            return;
        } finally {
            imap.logout();
            imap.disconnect();
        }
        System.out.println("Processed " + total + " messages, loaded " + loaded);
    }

    private static boolean process(final StringBuilder sb, final IMAPClient imap, final String folder
            ,final int msgNum) throws IOException {
        final int length = sb.length();
        boolean haveMessage = length > 2;
        if (haveMessage) {
            System.out.println("MsgNum: " + msgNum +" Length " + length);
            sb.setLength(length-2); // drop trailing CRLF
            String msg = sb.toString();
            if (!imap.append(folder, null, null, msg)) {
                throw new IOException("Failed to import message: " + msgNum + " " + imap.getReplyString());
            }
        }
        return haveMessage;
    }

    /**
     * Is the message wanted?
     * 
     * @param msgNum the message number
     * @param line the From line
     * @param msgNums the list of wanted message numbers
     * @param contains the list of strings to be contained
     * @return true if the message is wanted
     */
    private static boolean wanted(int msgNum, String line, BitSet msgNums, List<String> contains) {
        return (msgNums.isEmpty() && contains.isEmpty()) // no selectors 
             || msgNums.get(msgNum) // matches message number
             || listContains(contains, line); // contains string
    }

    /**
     * Is at least one entry in the list contained in the string?
     * @param contains the list of strings to look for
     * @param string the String to check against
     * @return true if at least one entry in the contains list is contained in the string
     */
    private static boolean listContains(List<String> contains, String string) {
        for(String entry : contains) {
            if (string.contains(entry)) {
                return true;
            }
        }
        return false;
    }

}
