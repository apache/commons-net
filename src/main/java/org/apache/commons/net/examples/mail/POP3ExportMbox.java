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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.pop3.POP3Client;
import org.apache.commons.net.pop3.POP3MessageInfo;
import org.apache.commons.net.pop3.POP3SClient;

/**
 * This is an example program demonstrating how to use the POP3[S]Client class.
 * This program connects to a POP3[S] server and writes the messages
 * to an mbox file.
 * <p>
 * The code currently assumes that POP3Client decodes the POP3 data as iso-8859-1.
 * The POP3 standard only allows for ASCII so in theory iso-8859-1 should be OK.
 * However it appears that actual POP3 implementations may return 8bit data that is
 * outside the ASCII range; this may result in loss of data when the mailbox is created.
 * <p>
 * See main() method for usage details
 */
public final class POP3ExportMbox
{

    private static final Pattern PATFROM = Pattern.compile(">*From "); // unescaped From_

    public static void main(final String[] args)
    {
        int argIdx;
        String file = null;
        for(argIdx = 0; argIdx < args.length; argIdx++) {
            if (args[argIdx].equals("-F")) {
                file = args[++argIdx];
            } else {
                break;
            }
        }

        final int argCount = args.length - argIdx;
        if (argCount < 3)
        {
            System.err.println(
                "Usage: POP3Mail [-F file/directory] <server[:port]> <username> <password|-|*|VARNAME> [TLS [true=implicit]]");
            System.exit(1);
        }

        final String arg0[] = args[argIdx++].split(":");
        final String server=arg0[0];
        final String username = args[argIdx++];
        String password = args[argIdx++];
        // prompt for the password if necessary
        try {
            password = Utils.getPassword(username, password);
        } catch (final IOException e1) {
            System.err.println("Could not retrieve password: " + e1.getMessage());
            return;
        }

        final String proto = argCount > 3 ? args[argIdx++] : null;
        final boolean implicit = argCount > 4 ? Boolean.parseBoolean(args[argIdx++]) : false;

        final POP3Client pop3;

        if (proto != null) {
            System.out.println("Using secure protocol: "+proto);
            pop3 = new POP3SClient(proto, implicit);
        } else {
            pop3 = new POP3Client();
        }

        final int port;
        if (arg0.length == 2) {
            port = Integer.parseInt(arg0[1]);
        } else {
            port = pop3.getDefaultPort();
        }
        System.out.println("Connecting to server "+server+" on "+port);

        // We want to timeout if a response takes longer than 60 seconds
        pop3.setDefaultTimeout(60000);

        try
        {
            pop3.connect(server);
        }
        catch (final IOException e)
        {
            System.err.println("Could not connect to server.");
            e.printStackTrace();
            return;
        }

        try
        {
            if (!pop3.login(username, password))
            {
                System.err.println("Could not login to server.  Check password.");
                pop3.disconnect();
                return;
            }

            final POP3MessageInfo status = pop3.status();
            if (status == null) {
                System.err.println("Could not retrieve status.");
                pop3.logout();
                pop3.disconnect();
                return;
            }

            System.out.println("Status: " + status);
            final int count = status.number;
            if (file != null) {
                System.out.println("Getting messages: " + count);
                final File mbox = new File(file);
                if (mbox.isDirectory()) {
                    System.out.println("Writing dir: " + mbox);
                    // Currently POP3Client uses iso-8859-1
                    for (int i = 1; i <= count; i++) {
                        try (final OutputStreamWriter fw = new OutputStreamWriter(
                                new FileOutputStream(new File(mbox, i + ".eml")), StandardCharsets.ISO_8859_1)) {
                            writeFile(pop3, fw, i);
                        }
                    }
                } else {
                    System.out.println("Writing file: " + mbox);
                    // Currently POP3Client uses iso-8859-1
                    try (final OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(mbox),
                            StandardCharsets.ISO_8859_1)) {
                        for (int i = 1; i <= count; i++) {
                            writeMbox(pop3, fw, i);
                        }
                    }
                }
            }

            pop3.logout();
            pop3.disconnect();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            return;
        }
    }

    private static void writeFile(final POP3Client pop3, final OutputStreamWriter fw, final int i) throws IOException {
        try (final BufferedReader r = (BufferedReader) pop3.retrieveMessage(i)) {
            String line;
            while ((line = r.readLine()) != null) {
                fw.write(line);
                fw.write("\n");
            }
        }
    }

    private static void writeMbox(final POP3Client pop3, final OutputStreamWriter fw, final int i) throws IOException {
        final SimpleDateFormat DATE_FORMAT // for mbox From_ lines
                = new SimpleDateFormat("EEE MMM dd HH:mm:ss YYYY");
        final String replyTo = "MAILER-DAEMON"; // default
        final Date received = new Date();
        try (final BufferedReader r = (BufferedReader) pop3.retrieveMessage(i)) {
            fw.append("From ");
            fw.append(replyTo);
            fw.append(' ');
            fw.append(DATE_FORMAT.format(received));
            fw.append("\n");
            String line;
            while ((line = r.readLine()) != null) {
                if (startsWith(line, PATFROM)) {
                    fw.write(">");
                }
                fw.write(line);
                fw.write("\n");
            }
            fw.write("\n");
        }
    }

    private static boolean startsWith(final String input, final Pattern pat) {
        final Matcher m = pat.matcher(input);
        return m.lookingAt();
    }
}

