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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.imap.IMAP;
import org.apache.commons.net.imap.IMAP.IMAPChunkListener;
import org.apache.commons.net.imap.IMAPClient;
import org.apache.commons.net.imap.IMAPReply;

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
 * <li>BODY.PEEK[HEADER]</li>
 * <li>'BODY.PEEK[HEADER.FIELDS (SUBJECT)]'</li>
 * <li>ALL - macro equivalent to '(FLAGS INTERNALDATE RFC822.SIZE ENVELOPE)'</li>
 * <li>FAST - macro equivalent to '(FLAGS INTERNALDATE RFC822.SIZE)'</li>
 * <li>FULL - macro equivalent to '(FLAGS INTERNALDATE RFC822.SIZE ENVELOPE BODY)'</li>
 * <li>ENVELOPE X-GM-LABELS</li>
 * <li>'(INTERNALDATE BODY.PEEK[])' - this is the default</li>
 * </ul>
 * <p>
 * Macro names cannot be combined with anything else; they must be used alone.<br>
 * Note that using BODY will set the \Seen flag. This is why the default uses BODY.PEEK[].<br>
 * The item name X-GM-LABELS is a Google Mail extension; it shows the labels for a message.<br>
 * For example:<br>
 * IMAPExportMbox imaps://username:password@imap.googlemail.com/messages_for_export exported.mbox 1:10,20<br>
 * IMAPExportMbox imaps://username:password@imap.googlemail.com/messages_for_export exported.mbox 3 ENVELOPE X-GM-LABELS<br>
 * <p>
 * The sequence-set is passed unmodified to the FETCH command.<br>
 * The item names are wrapped in parentheses if more than one is provided.
 * Otherwise, the parameter is assumed to be wrapped if necessary.<br>
 * Parameters with spaces must be quoted otherwise the OS shell will normally treat them as separate parameters.<br>
 * Also the listener that writes the mailbox only captures the multi-line responses (e.g. ones that include BODY references).
 * It does not capture the output from FETCH commands using item names such as ENVELOPE or FLAGS that return a single line response.
 */
public final class IMAPExportMbox
{

    private static final String CRLF = "\r\n";
    private static final String LF = "\n";
    private static final String EOL_DEFAULT = System.getProperty("line.separator");

    private static final Pattern PATFROM = Pattern.compile(">*From "); // unescaped From_
    // e.g. * nnn (INTERNALDATE "27-Oct-2013 07:43:24 +0000"  BODY[] {nn} ...)
    private static final Pattern PATID = // INTERNALDATE
            Pattern.compile(".*INTERNALDATE \"(\\d\\d-\\w{3}-\\d{4} \\d\\d:\\d\\d:\\d\\d [+-]\\d+)\"");
    private static final int PATID_DATE_GROUP = 1;

    private static final Pattern PATSEQ = Pattern.compile("\\* (\\d+) "); // Sequence number
    private static final int PATSEQ_SEQUENCE_GROUP = 1;

    // e.g. * 382 EXISTS
    private static final Pattern PATEXISTS = Pattern.compile("\\* (\\d+) EXISTS"); // Response from SELECT

    // AAAC NO [TEMPFAIL] FETCH Temporary failure on server [CODE: WBL]
    private static final Pattern PATTEMPFAIL = Pattern.compile("[A-Z]{4} NO \\[TEMPFAIL\\] FETCH .*");

    private static final int CONNECT_TIMEOUT = 10; // Seconds
    private static final int READ_TIMEOUT = 10;

    public static void main(final String[] args) throws IOException, URISyntaxException
    {
        int connect_timeout = CONNECT_TIMEOUT;
        int read_timeout = READ_TIMEOUT;

        int argIdx = 0;
        String eol = EOL_DEFAULT;
        boolean printHash = false;
        boolean printMarker = false;
        int retryWaitSecs = 0;

        for(argIdx = 0; argIdx < args.length; argIdx++) {
            if (args[argIdx].equals("-c")) {
                connect_timeout = Integer.parseInt(args[++argIdx]);
            } else if (args[argIdx].equals("-r")) {
                read_timeout = Integer.parseInt(args[++argIdx]);
            } else if (args[argIdx].equals("-R")) {
                retryWaitSecs = Integer.parseInt(args[++argIdx]);
            } else if (args[argIdx].equals("-LF")) {
                eol = LF;
            } else if (args[argIdx].equals("-CRLF")) {
                eol = CRLF;
            } else if (args[argIdx].equals("-.")) {
                printHash = true;
            } else if (args[argIdx].equals("-X")) {
                printMarker = true;
            } else {
                break;
            }
        }

        final int argCount = args.length - argIdx;

        if (argCount < 2)
        {
            System.err.println("Usage: IMAPExportMbox [-LF|-CRLF] [-c n] [-r n] [-R n] [-.] [-X]" +
                               " imap[s]://user:password@host[:port]/folder/path [+|-]<mboxfile> [sequence-set] [itemnames]");
            System.err.println("\t-LF | -CRLF set end-of-line to LF or CRLF (default is the line.separator system property)");
            System.err.println("\t-c connect timeout in seconds (default 10)");
            System.err.println("\t-r read timeout in seconds (default 10)");
            System.err.println("\t-R temporary failure retry wait in seconds (default 0; i.e. disabled)");
            System.err.println("\t-. print a . for each complete message received");
            System.err.println("\t-X print the X-IMAP line for each complete message received");
            System.err.println("\tthe mboxfile is where the messages are stored; use '-' to write to standard output.");
            System.err.println("\tPrefix file name with '+' to append to the file. Prefix with '-' to allow overwrite.");
            System.err.println("\ta sequence-set is a list of numbers/number ranges e.g. 1,2,3-10,20:* - default 1:*");
            System.err.println("\titemnames are the message data item name(s) e.g. BODY.PEEK[HEADER.FIELDS (SUBJECT)]" +
                               " or a macro e.g. ALL - default (INTERNALDATE BODY.PEEK[])");
            System.exit(1);
        }

        final String uriString = args[argIdx++];
        URI uri;
        try {
            uri = URI.create(uriString);
        } catch(final IllegalArgumentException e) { // cannot parse the path as is; let's pull it apart and try again
            final Matcher m = Pattern.compile("(imaps?://[^/]+)(/.*)").matcher(uriString);
            if (m.matches()) {
                uri = URI.create(m.group(1)); // Just the scheme and auth parts
                uri = new URI(uri.getScheme(), uri.getAuthority(), m.group(2), null, null);
            } else {
                throw e;
            }
        }
        final String file  = args[argIdx++];
        String sequenceSet = argCount > 2 ? args[argIdx++] : "1:*";
        final String itemNames;
        // Handle 0, 1 or multiple item names
        if (argCount > 3) {
            if (argCount > 4) {
                final StringBuilder sb = new StringBuilder();
                sb.append("(");
                for(int i=4; i <= argCount; i++) {
                    if (i>4) {
                        sb.append(" ");
                    }
                    sb.append(args[argIdx++]);
                }
                sb.append(")");
                itemNames = sb.toString();
            } else {
                itemNames = args[argIdx++];
            }
        } else {
            itemNames = "(INTERNALDATE BODY.PEEK[])";
        }

        final boolean checkSequence = sequenceSet.matches("\\d+:(\\d+|\\*)"); // are we expecting a sequence?
        final MboxListener mboxListener;
        if (file.equals("-")) {
            mboxListener = null;
        } else if (file.startsWith("+")) {
            final File mbox = new File(file.substring(1));
            System.out.println("Appending to file " + mbox);
            mboxListener = new MboxListener(
                new BufferedWriter(new FileWriter(mbox, true)), eol, printHash, printMarker, checkSequence);
        } else if (file.startsWith("-")) {
            final File mbox = new File(file.substring(1));
            System.out.println("Writing to file " + mbox);
            mboxListener = new MboxListener(
                new BufferedWriter(new FileWriter(mbox, false)), eol, printHash, printMarker, checkSequence);
        } else {
            final File mboxFile = new File(file);
            if (mboxFile.exists() && mboxFile.length() > 0) {
                throw new IOException("mailbox file: " + mboxFile + " already exists and is non-empty!");
            }
            System.out.println("Creating file " + mboxFile);
            mboxListener = new MboxListener(new BufferedWriter(new FileWriter(mboxFile)), eol, printHash, printMarker,
                    checkSequence);
        }

        final String path = uri.getPath();
        if (path == null || path.length() < 1) {
            throw new IllegalArgumentException("Invalid folderPath: '" + path + "'");
        }
        final String folder = path.substring(1); // skip the leading /

        // suppress login details
        final PrintCommandListener listener = new PrintCommandListener(System.out, true) {
            @Override
            public void protocolReplyReceived(final ProtocolCommandEvent event) {
                if (event.getReplyCode() != IMAPReply.PARTIAL){ // This is dealt with by the chunk listener
                    super.protocolReplyReceived(event);
                }
            }
        };

        // Connect and login
        final IMAPClient imap = IMAPUtils.imapLogin(uri, connect_timeout * 1000, listener);

        String maxIndexInFolder = null;

        try {

            imap.setSoTimeout(read_timeout * 1000);

            if (!imap.select(folder)){
                throw new IOException("Could not select folder: " + folder);
            }

            for(final String line : imap.getReplyStrings()) {
                maxIndexInFolder = matches(line, PATEXISTS, 1);
                if (maxIndexInFolder != null) {
                    break;
                }
            }

            if (mboxListener != null) {
                imap.setChunkListener(mboxListener);
            } // else the command listener displays the full output without processing


            while (true) {
                final boolean ok = imap.fetch(sequenceSet, itemNames);
                // If the fetch failed, can we retry?
                if (!ok && retryWaitSecs > 0 && mboxListener != null && checkSequence) {
                    final String replyString = imap.getReplyString(); //includes EOL
                    if (startsWith(replyString, PATTEMPFAIL)) {
                        System.err.println("Temporary error detected, will retry in " + retryWaitSecs + "seconds");
                        sequenceSet = mboxListener.lastSeq+1+":*";
                        try {
                            Thread.sleep(retryWaitSecs * 1000);
                        } catch (final InterruptedException e) {
                            // ignored
                        }
                    } else {
                        throw new IOException("FETCH " + sequenceSet + " " + itemNames+ " failed with " + replyString);
                    }
                } else {
                    break;
                }
            }

        } catch (final IOException ioe) {
            final String count = mboxListener == null ? "?" : mboxListener.total.toString();
            System.err.println(
                    "FETCH " + sequenceSet + " " + itemNames + " failed after processing " + count + " complete messages ");
            if (mboxListener != null) {
                System.err.println("Last complete response seen: "+mboxListener.lastFetched);
            }
            throw ioe;
        } finally {

            if (printHash) {
                System.err.println();
            }

            if (mboxListener != null) {
                mboxListener.close();
                final Iterator<String> missingIds = mboxListener.missingIds.iterator();
                if (missingIds.hasNext()) {
                    final StringBuilder sb = new StringBuilder();
                    for(;;) {
                        sb.append(missingIds.next());
                        if (!missingIds.hasNext()) {
                            break;
                        }
                        sb.append(",");
                    }
                    System.err.println("*** Missing ids: " + sb.toString());
                }
            }
            imap.logout();
            imap.disconnect();
        }
        if (mboxListener != null) {
            System.out.println("Processed " + mboxListener.total + " messages.");
        }
        if (maxIndexInFolder != null) {
            System.out.println("Folder contained " + maxIndexInFolder + " messages.");
        }
    }

    private static boolean startsWith(final String input, final Pattern pat) {
        final Matcher m = pat.matcher(input);
        return m.lookingAt();
    }

    private static String matches(final String input, final Pattern pat, final int index) {
        final Matcher m = pat.matcher(input);
        if (m.lookingAt()) {
            return m.group(index);
        }
        return null;
    }

    private static class MboxListener implements IMAPChunkListener {

        private final BufferedWriter bufferedWriter;
        volatile AtomicInteger total = new AtomicInteger();
        volatile String lastFetched;
        volatile List<String> missingIds = new ArrayList<>();
        volatile long lastSeq = -1;
        private final String lineSeparator;
        private final SimpleDateFormat DATE_FORMAT // for mbox From_ lines
            = new SimpleDateFormat("EEE MMM dd HH:mm:ss YYYY");

        // e.g. INTERNALDATE "27-Oct-2013 07:43:24 +0000"
        // for parsing INTERNALDATE
        private final SimpleDateFormat IDPARSE = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss Z");
        private final boolean printHash;
        private final boolean printMarker;
        private final boolean checkSequence;

        MboxListener(final BufferedWriter bufferedWriter, final String lineSeparator, final boolean printHash,
            final boolean printMarker, final boolean checkSequence) {
            this.lineSeparator = lineSeparator;
            this.printHash = printHash;
            this.printMarker = printMarker;
            DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
            this.bufferedWriter = bufferedWriter;
            this.checkSequence = checkSequence;
        }

        @Override
        public boolean chunkReceived(final IMAP imap) {
            final String[] replyStrings = imap.getReplyStrings();
            Date received = new Date();
            final String firstLine = replyStrings[0];
            Matcher m = PATID.matcher(firstLine);
            if (m.lookingAt()) { // found a match
                final String date = m.group(PATID_DATE_GROUP);
                try {
                    received=IDPARSE.parse(date);
                } catch (final ParseException e) {
                    System.err.println(e);
                }
            } else {
                System.err.println("No timestamp found in: " + firstLine + "  - using current time");
            }
            String replyTo = "MAILER-DAEMON"; // default
            for(int i=1; i< replyStrings.length - 1; i++) {
                final String line = replyStrings[i];
                if (line.startsWith("Return-Path: ")) {
                    final String[] parts = line.split(" ", 2);
                    if (!parts[1].equals("<>")) {// Don't replace default with blank
                        replyTo = parts[1];
                        if (replyTo.startsWith("<")) {
                            if (replyTo.endsWith(">")) {
                                replyTo = replyTo.substring(1,replyTo.length()-1); // drop <> wrapper
                            } else {
                                System.err.println("Unexpected Return-path: '" + line+ "' in " + firstLine);
                            }
                        }
                    }
                    break;
                }
            }
            try {
                // Add initial mbox header line
                bufferedWriter.append("From ");
                bufferedWriter.append(replyTo);
                bufferedWriter.append(' ');
                bufferedWriter.append(DATE_FORMAT.format(received));
                bufferedWriter.append(lineSeparator);
                // Debug
                bufferedWriter.append("X-IMAP-Response: ").append(firstLine).append(lineSeparator);
                if (printMarker) {
                    System.err.println("[" + total + "] " + firstLine);
                }
                // Skip first and last lines
                for(int i=1; i< replyStrings.length - 1; i++) {
                    final String line = replyStrings[i];
                        if (startsWith(line, PATFROM)) {
                            bufferedWriter.append('>'); // Escape a From_ line
                        }
                        bufferedWriter.append(line);
                        bufferedWriter.append(lineSeparator);
                }
                // The last line ends with the trailing closing ")" which needs to be stripped
                final String lastLine = replyStrings[replyStrings.length-1];
                final int lastLength = lastLine.length();
                if (lastLength > 1) { // there's some content, we need to save it
                    bufferedWriter.append(lastLine, 0, lastLength-1);
                    bufferedWriter.append(lineSeparator);
                }
                bufferedWriter.append(lineSeparator); // blank line between entries
            } catch (final IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e); // chunkReceived cannot throw a checked Exception
            }
            lastFetched = firstLine;
            total.incrementAndGet();
            if (checkSequence) {
                m = PATSEQ.matcher(firstLine);
                if (m.lookingAt()) { // found a match
                    final long msgSeq = Long.parseLong(m.group(PATSEQ_SEQUENCE_GROUP)); // Cannot fail to parse
                    if (lastSeq != -1) {
                        final long missing = msgSeq - lastSeq - 1;
                        if (missing != 0) {
                            for(long j = lastSeq + 1; j < msgSeq; j++) {
                                missingIds.add(String.valueOf(j));
                            }
                            System.err.println(
                                "*** Sequence error: current=" + msgSeq + " previous=" + lastSeq + " Missing=" + missing);
                        }
                    }
                    lastSeq = msgSeq;
                }
            }
            if (printHash) {
                System.err.print(".");
            }
            return true;
        }

        public void close() throws IOException {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }
    }
}
