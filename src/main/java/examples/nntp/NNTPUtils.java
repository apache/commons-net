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
package examples.nntp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.net.nntp.Article;
import org.apache.commons.net.nntp.NNTPClient;

/**
 *
 * Some convenience methods for NNTP example classes.
 *
 * @author Rory Winston <rwinston@checkfree.com>
 */
public class NNTPUtils {

    /**
     * Given an {@link NNTPClient} instance, and an integer range of messages, return
     * an array of {@link Article} instances.
     * @param client
     * @param lowArticleNumber
     * @param highArticleNumber
     * @return Article[] An array of Article
     * @throws IOException
     */
    public  static List<Article> getArticleInfo(NNTPClient client, int lowArticleNumber, int highArticleNumber)
    throws IOException {
        Reader reader = null;
        List<Article> articles = new ArrayList<Article>();
        reader = client.retrieveArticleInfo(
                    lowArticleNumber,
                    highArticleNumber);

        if (reader != null) {
            BufferedReader bufReader = new BufferedReader(reader);

            // Extract the article information
            // Mandatory format (from NNTP RFC 2980) is :
            // articleNumber\tSubject\tAuthor\tDate\tID\tReference(s)\tByte Count\tLine Count

            String msg;
            while ((msg=bufReader.readLine()) != null) {
                System.out.println("Message:" + msg);
                String parts[] = msg.split("\t");
                if (parts.length > 6) {
                    int i = 0;
                    Article article = new Article();
                    article.setArticleNumber(Integer.parseInt(parts[i++]));
                    article.setSubject(parts[i++]);
                    article.setFrom(parts[i++]);
                    article.setDate(parts[i++]);
                    article.setArticleId(parts[i++]);
                    article.addReference(parts[i++]);
                    articles.add(article);
                }
            }
        }
        return articles;
    }
}
