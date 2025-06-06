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

import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.net.examples.PrintCommandListeners;
import org.apache.commons.net.nntp.Article;
import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewsgroupInfo;
import org.apache.commons.net.nntp.Threader;

/**
 * Sample program demonstrating the use of article iteration and threading.
 */
public class MessageThreading {
    public static void main(final String[] args) throws SocketException, IOException {

        if (args.length != 2 && args.length != 4) {
            System.out.println("Usage: MessageThreading <hostname> <groupname> [<user> <password>]");
            return;
        }

        final String hostname = args[0];
        final String newsgroup = args[1];

        final NNTPClient client = new NNTPClient();
        client.addProtocolCommandListener(PrintCommandListeners.sysOutPrintCommandListener());
        client.connect(hostname);

        if (args.length == 4) { // Optional auth
            final String user = args[2];
            final String password = args[3];
            if (!client.authenticate(user, password)) {
                System.out.println("Authentication failed for user " + user + "!");
                System.exit(1);
            }
        }

        final String[] fmt = client.listOverviewFmt();
        if (fmt != null) {
            System.out.println("LIST OVERVIEW.FMT:");
            for (final String s : fmt) {
                System.out.println(s);
            }
        } else {
            System.out.println("Failed to get OVERVIEW.FMT");
        }
        final NewsgroupInfo group = new NewsgroupInfo();
        client.selectNewsgroup(newsgroup, group);

        final long lowArticleNumber = group.getFirstArticleLong();
        final long highArticleNumber = lowArticleNumber + 5000;

        System.out.println("Retrieving articles between [" + lowArticleNumber + "] and [" + highArticleNumber + "]");
        final Iterable<Article> articles = client.iterateArticleInfo(lowArticleNumber, highArticleNumber);

        System.out.println("Building message thread tree...");
        final Threader threader = new Threader();
        final Article root = (Article) threader.thread(articles);

        Article.printThread(root, 0);
    }

    public MessageThreading() {
    }

}
