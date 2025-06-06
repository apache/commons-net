/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.examples.nntp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.net.examples.PrintCommandListeners;
import org.apache.commons.net.io.Util;
import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NNTPReply;
import org.apache.commons.net.nntp.SimpleNNTPHeader;

/**
 * This is an example program using the NNTP package to post an article to the specified newsgroup(s). It prompts you for header information and a file name to
 * post.
 */

public final class PostMessage {

    public static void main(final String[] args) {
        final String from;
        final String subject;
        String newsgroup;
        final String fileName;
        final String server;
        final String organization;
        final String references;
        final BufferedReader stdin;
        Reader fileReader = null;
        final SimpleNNTPHeader header;
        final NNTPClient client;

        if (args.length < 1) {
            System.err.println("Usage: post newsserver");
            System.exit(1);
        }

        server = args[0];

        stdin = new BufferedReader(new InputStreamReader(System.in, Charset.defaultCharset()));

        try {
            System.out.print("From: ");
            System.out.flush();

            from = stdin.readLine();

            System.out.print("Subject: ");
            System.out.flush();

            subject = stdin.readLine();

            header = new SimpleNNTPHeader(from, subject);

            System.out.print("Newsgroup: ");
            System.out.flush();

            newsgroup = stdin.readLine();
            header.addNewsgroup(newsgroup);

            while (true) {
                System.out.print("Additional Newsgroup <Hit enter to end>: ");
                System.out.flush();

                newsgroup = stdin.readLine();
                if (newsgroup == null) {
                    break;
                }

                newsgroup = newsgroup.trim();

                if (newsgroup.isEmpty()) {
                    break;
                }

                header.addNewsgroup(newsgroup);
            }

            System.out.print("Organization: ");
            System.out.flush();

            organization = stdin.readLine();

            System.out.print("References: ");
            System.out.flush();

            references = stdin.readLine();

            if (organization != null && !organization.isEmpty()) {
                header.addHeaderField("Organization", organization);
            }

            if (references != null && !references.isEmpty()) {
                header.addHeaderField("References", references);
            }

            header.addHeaderField("X-Newsreader", "NetComponents");

            System.out.print("Filename: ");
            System.out.flush();

            fileName = stdin.readLine();

            try {
                fileReader = Files.newBufferedReader(Paths.get(fileName), Charset.defaultCharset());
            } catch (final FileNotFoundException e) {
                System.err.println("File not found. " + e.getMessage());
                System.exit(1);
            }

            client = new NNTPClient();
            client.addProtocolCommandListener(PrintCommandListeners.sysOutPrintCommandListener());

            client.connect(server);

            if (!NNTPReply.isPositiveCompletion(client.getReplyCode())) {
                client.disconnect();
                System.err.println("NNTP server refused connection.");
                System.exit(1);
            }

            if (client.isAllowedToPost()) {
                final Writer writer = client.postArticle();

                if (writer != null) {
                    writer.write(header.toString());
                    Util.copyReader(fileReader, writer);
                    writer.close();
                    client.completePendingCommand();
                }
            }

            fileReader.close();

            client.logout();

            client.disconnect();
        } catch (final IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
