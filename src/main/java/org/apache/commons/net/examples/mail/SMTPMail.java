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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.io.Util;
import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.apache.commons.net.smtp.SimpleSMTPHeader;

/**
 * This is an example program using the SMTP package to send a message
 * to the specified recipients.  It prompts you for header information and
 * a file name containing the message.
 */

public final class SMTPMail
{

    public static void main(final String[] args)
    {
        final String sender;
        final String recipient;
        final String subject;
        final String fileName;
        final String server;
        String cc;
        final List<String> ccList = new ArrayList<>();
        final BufferedReader stdin;
        FileReader fileReader = null;
        final Writer writer;
        final SimpleSMTPHeader header;
        final SMTPClient client;

        if (args.length < 1)
        {
            System.err.println("Usage: SMTPMail <smtpserver>");
            System.exit(1);
        }

        server = args[0];

        stdin = new BufferedReader(new InputStreamReader(System.in));

        try
        {
            System.out.print("From: ");
            System.out.flush();

            sender = stdin.readLine();

            System.out.print("To: ");
            System.out.flush();

            recipient = stdin.readLine();

            System.out.print("Subject: ");
            System.out.flush();

            subject = stdin.readLine();

            header = new SimpleSMTPHeader(sender, recipient, subject);


            while (true)
            {
                System.out.print("CC <enter one address per line, hit enter to end>: ");
                System.out.flush();

                cc = stdin.readLine();

                if (cc== null || cc.isEmpty()) {
                    break;
                }

                header.addCC(cc.trim());
                ccList.add(cc.trim());
            }

            System.out.print("Filename: ");
            System.out.flush();

            fileName = stdin.readLine();

            try
            {
                fileReader = new FileReader(fileName);
            }
            catch (final FileNotFoundException e)
            {
                System.err.println("File not found. " + e.getMessage());
            }

            client = new SMTPClient();
            client.addProtocolCommandListener(new PrintCommandListener(
                                                  new PrintWriter(System.out), true));

            client.connect(server);

            if (!SMTPReply.isPositiveCompletion(client.getReplyCode()))
            {
                client.disconnect();
                System.err.println("SMTP server refused connection.");
                System.exit(1);
            }

            client.login();

            client.setSender(sender);
            client.addRecipient(recipient);



            for (final String recpt : ccList) {
                client.addRecipient(recpt);
            }

            writer = client.sendMessageData();

            if (writer != null)
            {
                writer.write(header.toString());
                Util.copyReader(fileReader, writer);
                writer.close();
                client.completePendingCommand();
            }

            if (fileReader != null ) {
                fileReader.close();
            }

            client.logout();

            client.disconnect();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}


