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

package org.apache.commons.net.examples.nntp;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.nntp.Article;
import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewsgroupInfo;


/**
 * Simple class showing some of the extended commands (AUTH, XOVER, LIST ACTIVE)
 */
public class ExtendedNNTPOps {


    private final NNTPClient client;

    public ExtendedNNTPOps() {
        client = new NNTPClient();
        client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));
    }


    private void demo(final String host, final String user, final String password) {
        try {
            client.connect(host);

            // AUTHINFO USER/AUTHINFO PASS
            if (user != null && password != null) {
                final boolean success = client.authenticate(user, password);
                if (success) {
                    System.out.println("Authentication succeeded");
                } else {
                    System.out.println("Authentication failed, error =" + client.getReplyString());
                }
            }

            // XOVER
            final NewsgroupInfo testGroup = new NewsgroupInfo();
            client.selectNewsgroup("alt.test", testGroup);
            final long lowArticleNumber = testGroup.getFirstArticleLong();
            final long  highArticleNumber = lowArticleNumber + 100;
            final Iterable<Article> articles = client.iterateArticleInfo(lowArticleNumber, highArticleNumber);

            for (final Article article : articles) {
                if (article.isDummy()) { // Subject will contain raw response
                    System.out.println("Could not parse: "+article.getSubject());
                } else {
                    System.out.println(article.getSubject());
                }
            }

            // LIST ACTIVE
            final NewsgroupInfo[] fanGroups = client.listNewsgroups("alt.fan.*");
            for (final NewsgroupInfo fanGroup : fanGroups)
            {
                System.out.println(fanGroup.getNewsgroup());
            }

        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(final String[] args) {
        final ExtendedNNTPOps ops;

        final int argc = args.length;
        if (argc < 1) {
            System.err.println("usage: ExtendedNNTPOps nntpserver [username password]");
            System.exit(1);
        }

        ops = new ExtendedNNTPOps();
        ops.demo(args[0], argc >=3 ? args[1] : null, argc >=3 ? args[2] : null);
    }

}
