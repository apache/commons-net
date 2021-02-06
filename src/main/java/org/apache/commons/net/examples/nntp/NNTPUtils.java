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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.nntp.Article;
import org.apache.commons.net.nntp.NNTPClient;

/**
 * Some convenience methods for NNTP example classes.
 */
public class NNTPUtils {

    /**
     * Given an {@link NNTPClient} instance, and an integer range of messages, return
     * an array of {@link Article} instances.
     * @param client the client to use
     * @param lowArticleNumber low number
     * @param highArticleNumber high number
     * @return Article[] An array of Article
     * @throws IOException on error
     */
    public  static List<Article> getArticleInfo(final NNTPClient client, final long lowArticleNumber, final long highArticleNumber)
    throws IOException {
        final List<Article> articles = new ArrayList<>();
        final Iterable<Article> arts = client.iterateArticleInfo(lowArticleNumber, highArticleNumber);
        for(final Article article : arts){
            articles.add(article);
        }
        return articles;
    }
}
