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

/***
 * This class is used to construct the bare minimum
 * acceptable header for most news readers.  To construct more
 * complicated headers you should refer to RFC 822.  When the
 * Java Mail API is finalized, you will be
 * able to use it to compose fully compliant Internet text messages.
 * <p>
 * The main purpose of the class is to faciliatate the article posting
 * process, by relieving the programmer from having to explicitly format
 * an article header.  For example:
 * <pre>
 * writer = client.postArticle();
 * if(writer == null) // failure
 *   return false;
 * header = new SimpleNNTPHeader("foobar@foo.com", "Just testing");
 * header.addNewsgroup("alt.test");
 * header.addHeaderField("Organization", "Foobar, Inc.");
 * writer.write(header.toString());
 * writer.write("This is just a test");
 * writer.close();
 * if(!client.completePendingCommand()) // failure
 *   return false;
 * </pre>
 * <p>
 * <p>
 * @see NNTPClient
 ***/

public class SimpleNNTPHeader
{
    private final String __subject, __from;
    private final StringBuilder __newsgroups;
    private final StringBuilder __headerFields;
    private int __newsgroupCount;

    /***
     * Creates a new SimpleNNTPHeader instance initialized with the given
     * from and subject header field values.
     * <p>
     * @param from  The value of the <code>From:</code> header field.  This
     *              should be the article poster's email address.
     * @param subject  The value of the <code>Subject:</code> header field.
     *              This should be the subject of the article.
     ***/
    public SimpleNNTPHeader(String from, String subject)
    {
        __from = from;
        __subject = subject;
        __newsgroups = new StringBuilder();
        __headerFields = new StringBuilder();
        __newsgroupCount = 0;
    }

    /***
     * Adds a newsgroup to the article <code>Newsgroups:</code> field.
     * <p>
     * @param newsgroup  The newsgroup to add to the article's newsgroup
     *                   distribution list.
     ***/
    public void addNewsgroup(String newsgroup)
    {
        if (__newsgroupCount++ > 0) {
            __newsgroups.append(',');
        }
        __newsgroups.append(newsgroup);
    }

    /***
     * Adds an arbitrary header field with the given value to the article
     * header.  These headers will be written after the From, Newsgroups,
     * and Subject fields when the SimpleNNTPHeader is convertered to a string.
     * An example use would be:
     * <pre>
     * header.addHeaderField("Organization", "Foobar, Inc.");
     * </pre>
     * <p>
     * @param headerField  The header field to add, not including the colon.
     * @param value  The value of the added header field.
     ***/
    public void addHeaderField(String headerField, String value)
    {
        __headerFields.append(headerField);
        __headerFields.append(": ");
        __headerFields.append(value);
        __headerFields.append('\n');
    }


    /***
     * Returns the address used in the <code> From: </code> header field.
     * <p>
     * @return The from address.
     ***/
    public String getFromAddress()
    {
        return __from;
    }

    /***
     * Returns the subject used in the <code> Subject: </code> header field.
     * <p>
     * @return The subject.
     ***/
    public String getSubject()
    {
        return __subject;
    }

    /***
     * Returns the contents of the <code> Newsgroups: </code> header field.
     * <p>
     * @return The comma-separated list of newsgroups to which the article
     *         is being posted.
     ***/
    public String getNewsgroups()
    {
        return __newsgroups.toString();
    }

    /***
     * Converts the SimpleNNTPHeader to a properly formatted header in
     * the form of a String, including the blank line used to separate
     * the header from the article body.
     * <p>
     * @return The article header in the form of a String.
     ***/
    @Override
    public String toString()
    {
        StringBuilder header = new StringBuilder();

        header.append("From: ");
        header.append(__from);
        header.append("\nNewsgroups: ");
        header.append(__newsgroups.toString());
        header.append("\nSubject: ");
        header.append(__subject);
        header.append('\n');
        if (__headerFields.length() > 0) {
            header.append(__headerFields.toString());
        }
        header.append('\n');

        return header.toString();
    }
}
