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

package org.apache.commons.net.nntp;

/**
 * This class is used to construct the bare minimum acceptable header for most newsreaders. To construct more complicated headers you should refer to RFC 822.
 * When the Java Mail API is finalized, you will be able to use it to compose fully compliant Internet text messages.
 * <p>
 * The main purpose of the class is to faciliatate the article posting process, by relieving the programmer from having to explicitly format an article header.
 * For example:
 * </p>
 *
 * <pre>
 * writer = client.postArticle();
 * if (writer == null) // failure
 *     return false;
 * header = new SimpleNNTPHeader("foobar@foo.com", "Just testing");
 * header.addNewsgroup("alt.test");
 * header.addHeaderField("Organization", "Foobar, Inc.");
 * writer.write(header.toString());
 * writer.write("This is just a test");
 * writer.close();
 * if (!client.completePendingCommand()) // failure
 *     return false;
 * </pre>
 *
 * @see NNTPClient
 */

public class SimpleNNTPHeader {
    private final String subject, from;
    private final StringBuilder newsgroups;
    private final StringBuilder headerFields;
    private int newsgroupCount;

    /**
     * Creates a new SimpleNNTPHeader instance initialized with the given from and subject header field values.
     *
     * @param from    The value of the <code>From:</code> header field. This should be the article poster's email address.
     * @param subject The value of the <code>Subject:</code> header field. This should be the subject of the article.
     */
    public SimpleNNTPHeader(final String from, final String subject) {
        this.from = from;
        this.subject = subject;
        this.newsgroups = new StringBuilder();
        this.headerFields = new StringBuilder();
        this.newsgroupCount = 0;
    }

    /**
     * Adds an arbitrary header field with the given value to the article header.
     * These headers will be written after the {@code From}, Newsgroups, and Subject fields
     * when the SimpleNNTPHeader is converted to a string. An example use would be:
     *
     * <pre>
     * header.addHeaderField("Organization", "Foobar, Inc.");
     * </pre>
     *
     * @param headerField The header field to add, not including the colon.
     * @param value       The value of the added header field.
     */
    public void addHeaderField(final String headerField, final String value) {
        headerFields.append(headerField);
        headerFields.append(": ");
        headerFields.append(value);
        headerFields.append('\n');
    }

    /**
     * Adds a newsgroup to the article <code>Newsgroups:</code> field.
     *
     * @param newsgroup The newsgroup to add to the article's newsgroup distribution list.
     */
    public void addNewsgroup(final String newsgroup) {
        if (newsgroupCount++ > 0) {
            newsgroups.append(',');
        }
        newsgroups.append(newsgroup);
    }

    /**
     * Returns the address used in the <code> From: </code> header field.
     *
     * @return The from address.
     */
    public String getFromAddress() {
        return from;
    }

    /**
     * Returns the contents of the <code> Newsgroups: </code> header field.
     *
     * @return The comma-separated list of newsgroups to which the article is being posted.
     */
    public String getNewsgroups() {
        return newsgroups.toString();
    }

    /**
     * Returns the subject used in the <code> Subject: </code> header field.
     *
     * @return The subject.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Converts the SimpleNNTPHeader to a properly formatted header in the form of a String, including the blank line used to separate the header from the
     * article body.
     *
     * @return The article header in the form of a String.
     */
    @Override
    public String toString() {
        final StringBuilder header = new StringBuilder();

        header.append("From: ");
        header.append(from);
        header.append("\nNewsgroups: ");
        header.append(newsgroups.toString());
        header.append("\nSubject: ");
        header.append(subject);
        header.append('\n');
        if (headerFields.length() > 0) {
            header.append(headerFields.toString());
        }
        header.append('\n');

        return header.toString();
    }
}
