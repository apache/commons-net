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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.imap.IMAP.IMAPChunkListener;
import org.apache.commons.net.imap.IMAP;
import org.apache.commons.net.imap.IMAPClient;

/**
 * This is an example program demonstrating how to use the IMAP[S]Client class.
 * This program connects to a IMAP[S] server and exports selected messages from a folder into an mbox file.
 * <p>
 * Usage: IMAPExportMbox imap[s]://user:password@host[:port]/folder/path <mboxfile> [sequence-set] [item-names]
 * <p>
 * An example sequence-set might be:
 * <ul>
 * <li>11,2,3:10,20:*</li>
 * <li>1:* - this is the default</li>
 * </ul>
 * <p>
 * Some example item-names might be:
 * <ul>
 * <li>BODY.PEEK[HEADER.FIELDS (SUBJECT)]</li>
 * <li>ALL</li>
 * <li>ENVELOPE</li>
 * <li>(INTERNALDATE BODY.PEEK[]) - this is the default</li>
 * </ul>
 * <p>
 * For example:<br>
 * IMAPExportMbox imaps://username:password@imap.googlemail.com/messages_for_export exported.mbox 1:10,20<br>
 * IMAPExportMbox imaps://username:password@imap.googlemail.com/messages_for_export exported.mbox 3 ENVELOPE<br>
 * <p>
 * Note that the sequence-set and item names are passed unmodified to the FETCH command.
 * Also the listener that writes the mailbox only captures the multi-line responses.
 * It does not capture the output from ENVELOPE commands.
 */
public final class IMAPExportMbox
{

    private static final String CRLF = "\r\n";

    private static final Pattern PATFROM = Pattern.compile(">*From "); // unescaped From_
    // e.g. INTERNALDATE "27-Oct-2013 07:43:24 +0000"
    private static final Pattern PATID =
            Pattern.compile(".*INTERNALDATE \"(\\d\\d-\\w{3}-\\d{4} \\d\\d:\\d\\d:\\d\\d [+-]\\d+)\"");

    public static void main(String[] args) throws IOException
    {
        if (args.length < 2)
        {
            System.err.println("Usage: IMAPExportMbox imap[s]://user:password@host[:port]/folder/path <mboxfile> [sequence-set] [itemnames]");
            System.err.println("\ta sequence-set is a list of numbers/number ranges e.g. 1,2,3-10,20:* - default 1:*");
            System.err.println("\titemnames are the message data item name(s) e.g. BODY.PEEK[HEADER.FIELDS (SUBJECT)] or a macro e.g. ALL - default (INTERNALDATE BODY.PEEK[])");
            System.exit(1);
        }

        final URI uri      = URI.create(args[0]);
        final String file  = args[1];
        final String sequenceSet = args.length > 2 ? args[2] : "1:*";
        final String itemNames   = args.length > 3 ? args[3] : "(INTERNALDATE BODY.PEEK[])";

        final File mbox = "-".equals(file) ? null : new File(file);

        String path = uri.getPath();
        if (path == null || path.length() < 1) {
            throw new IllegalArgumentException("Invalid folderPath: '" + path + "'");
        }
        String folder = path.substring(1); // skip the leading /

        // suppress login details
        final PrintCommandListener listener = new PrintCommandListener(System.out, true);
        
        // Connect and login
        final IMAPClient imap = IMAPUtils.imapLogin(uri, 10000, listener);

        MboxListener chunkListener = new MboxListener(mbox); 
        try {

            imap.setSoTimeout(6000);

            if (!imap.select(folder)){
                throw new RuntimeException("Could not select folder: " + folder);
            }

            imap.removeProtocolCommandListener(listener); // no longer needed

            imap.setChunkListener(chunkListener);

            if (!imap.fetch(sequenceSet, itemNames)) {
                chunkListener.close();
                throw new IOException("FETCH " + sequenceSet + " " + itemNames+ " failed with " + imap.getReplyString());
            }

            // remains of response
            for(String line :imap.getReplyStrings()) {
                System.out.println(line);
            }
            chunkListener.close();
        } finally {
            imap.logout();
            imap.disconnect();
        }
        System.out.println("Processed " + chunkListener.total + " messages.");
    }

    private static boolean startsWith(String input, Pattern pat) {
        Matcher m = pat.matcher(input);
        return m.lookingAt();
    }

    private static class MboxListener implements IMAPChunkListener {

        private BufferedWriter bw;
        volatile int total = 0;
        private final File mbox;
        private final SimpleDateFormat DATE_FORMAT // for mbox From_ lines
            = new SimpleDateFormat("EEE MMM dd HH:mm:ss YYYY");

        // e.g. INTERNALDATE "27-Oct-2013 07:43:24 +0000"
        private final SimpleDateFormat IDPARSE // for parsing INTERNALDATE
        = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss Z");

        MboxListener(File mbox) {
          this.mbox=mbox;
          DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        public boolean chunkReceived(IMAP imap) {
            if (mbox == null) {
                return true;
            }
            if (bw == null) {
                try {
                    if (mbox.exists()) {
                        throw new IOException("mailbox file: " + mbox + " already exists!");
                    } else {
                        System.out.println("Creating: " + mbox);
                    }
                    bw = new BufferedWriter(new FileWriter(mbox));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            final String[] replyStrings = imap.getReplyStrings();
            Date received = new Date();
            final String firstLine = replyStrings[0];
            Matcher m = PATID.matcher(firstLine);
            if (m.lookingAt()) { // found a match
                String date = m.group(1);
                try {
                    received=IDPARSE.parse(date);
                } catch (ParseException e) {
                    System.err.println(e);
                }
            } else {
            }

            String replyTo = "MAILER-DAEMON"; // default
            for(int i=1; i< replyStrings.length - 1; i++) {
                final String line = replyStrings[i];
                String[] parts = line.split(" ", 2);
                if ("Return-Path:".equals(parts[0])) {
                    replyTo = parts[1];
                    replyTo = replyTo.substring(1,replyTo.length()-2); // drop <> wrapper
                    break;
                }
            }
            try {
            	total++;
                // Add initial mbox header line
                bw.append("From ");
                bw.append(replyTo);
                bw.append(' ');
                bw.append(DATE_FORMAT.format(received));
                bw.append(CRLF);
                // Debug
                bw.append("X-IMAP-Response: ").append(firstLine).append(CRLF);
                // Skip first and last lines
                for(int i=1; i< replyStrings.length - 1; i++) {
                    final String line = replyStrings[i];
                        if (startsWith(line, PATFROM)) {
                            bw.append('>'); // Escape a From_ line
                        }
                        bw.append(line);
                        bw.append(CRLF);
                }
                // The last line ends with the trailing closing ")" which needs to be stripped
                String lastLine = replyStrings[replyStrings.length-1];
                final int lastLength = lastLine.length();
                if (lastLength > 1) { // there's some content, we need to save it
                    bw.append(lastLine, 0, lastLength-1);
                    bw.append(CRLF);
                }
                bw.append(CRLF); // blank line between entries
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        public void close() throws IOException {
            if (bw != null) {
                bw.close();
            }   
        }
    }
}
