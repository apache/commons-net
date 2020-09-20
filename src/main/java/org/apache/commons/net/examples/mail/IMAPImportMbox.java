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

package org.apache.commons.net.examples.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.imap.IMAPClient;

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
 * </ul>
 * <p>
 * For example:<br>
 * IMAPImportMbox imaps://user:pass@imap.googlemail.com/imported_messages 201401.mbox 1-10,20 -142986-
 */
public final class IMAPImportMbox
{

    private static final String CRLF = "\r\n";
    private static final Pattern PATFROM = Pattern.compile(">+From "); // escaped From

    public static void main(final String[] args) throws IOException
    {
        if (args.length < 2)
        {
            System.err.println("Usage: IMAPImportMbox imap[s]://user:password@host[:port]/folder/path <mboxfile> [selectors]");
            System.err.println("\tWhere: a selector is a list of numbers/number ranges - 1,2,3-10" +
                               " - or a list of strings to match in the initial From line");
            System.exit(1);
        }

        final URI uri      = URI.create(args[0]);
        final String file  = args[1];

        final File mbox = new File(file);
        if (!mbox.isFile() || !mbox.canRead()) {
            throw new IOException("Cannot read mailbox file: " + mbox);
        }

        final String path = uri.getPath();
        if (path == null || path.length() < 1) {
            throw new IllegalArgumentException("Invalid folderPath: '" + path + "'");
        }
        final String folder = path.substring(1); // skip the leading /

        final List<String> contains = new ArrayList<>(); // list of strings to find
        final BitSet msgNums = new BitSet(); // list of message numbers

        for(int i = 2; i < args.length; i++) {
            final String arg = args[i];
            if (arg.matches("\\d+(-\\d+)?(,\\d+(-\\d+)?)*")) { // number,m-n
                for(final String entry : arg.split(",")) {
                    final String []parts = entry.split("-");
                    if (parts.length == 2) { // m-n
                        final int low = Integer.parseInt(parts[0]);
                        final int high = Integer.parseInt(parts[1]);
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

        // Connect and login
        final IMAPClient imap = IMAPUtils.imapLogin(uri, 10000, null);

        int total = 0;
        int loaded = 0;
        try {
            imap.setSoTimeout(6000);

            final BufferedReader br = new BufferedReader(new FileReader(file)); // TODO charset?

            String line;
            final StringBuilder sb = new StringBuilder();
            boolean wanted = false; // Skip any leading rubbish
            while((line=br.readLine())!=null) {
                if (line.startsWith("From ")) { // start of message; i.e. end of previous (if any)
                    if (process(sb, imap, folder, total)) { // process previous message (if any)
                        loaded++;
                    }
                    sb.setLength(0);
                    total ++;
                    wanted = wanted(total, line, msgNums, contains);
                } else if (startsWith(line, PATFROM)) { // Unescape ">+From " in body text
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
        } catch (final IOException e) {
            System.out.println("Error processing msg: " + total + " " + imap.getReplyString());
            e.printStackTrace();
            System.exit(10);
            return;
        } finally {
            imap.logout();
            imap.disconnect();
        }
        System.out.println("Processed " + total + " messages, loaded " + loaded);
    }

    private static boolean startsWith(final String input, final Pattern pat) {
        final Matcher m = pat.matcher(input);
        return m.lookingAt();
    }

    private static String getDate(final String msg) {
                                              // From SENDER Fri Sep 13 17:04:01 2019
        final Pattern FROM_RE = Pattern.compile("From \\S+ +\\S+ (\\S+)  ?(\\S+) (\\S+) (\\S+)");
        //                                                 [Fri]   Sep      13     HMS   2019
        // output date: 13-Sep-2019 17:04:01 +0000
        String date = null;
        final Matcher m = FROM_RE.matcher(msg);
        if (m.lookingAt()) {
            date = m.group(2)+"-"+m.group(1)+"-"+m.group(4)+" "+m.group(3)+" +0000";
        }
        return date;
    }

    private static boolean process(final StringBuilder sb, final IMAPClient imap, final String folder
            ,final int msgNum) throws IOException {
        final int length = sb.length();
        final boolean haveMessage = length > 2;
        if (haveMessage) {
            System.out.println("MsgNum: " + msgNum +" Length " + length);
            sb.setLength(length-2); // drop trailing CRLF (mbox format has trailing blank line)
            final String msg = sb.toString();
            if (!imap.append(folder, null, getDate(msg), msg)) {
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
    private static boolean wanted(final int msgNum, final String line, final BitSet msgNums, final List<String> contains) {
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
    private static boolean listContains(final List<String> contains, final String string) {
        for(final String entry : contains) {
            if (string.contains(entry)) {
                return true;
            }
        }
        return false;
    }

}
