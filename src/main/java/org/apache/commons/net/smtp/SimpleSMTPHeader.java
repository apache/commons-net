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

package org.apache.commons.net.smtp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class is used to construct a bare minimum acceptable header for an email message. To construct more complicated headers you should refer to RFC 5322.
 * When the Java Mail API is finalized, you will be able to use it to compose fully compliant Internet text messages.
 * <p>
 * The main purpose of the class is to facilitate the mail sending process, by relieving the programmer from having to explicitly format a simple message
 * header. For example:
 * </p>
 *
 * <pre>
 * writer = client.sendMessageData();
 * if (writer == null) // failure
 *   return false;
 * header =
 *    new SimpleSMTPHeader("foobar@foo.com", "foo@bar.com" "Just testing");
 * header.addCC("bar@foo.com");
 * header.addHeaderField("Organization", "Foobar, Inc.");
 * writer.write(header.toString());
 * writer.write("This is just a test");
 * writer.close();
 * if (!client.completePendingCommand()) // failure
 *   return false;
 * </pre>
 *
 * @see SMTPClient
 */

public class SimpleSMTPHeader {
    private final String subject;
    private final String from;
    private final String to;
    private final StringBuffer headerFields;
    private boolean hasHeaderDate;
    private StringBuffer cc;

    /**
     * Creates a new SimpleSMTPHeader instance initialized with the given from, to, and subject header field values.
     *
     * @param from    The value of the <code>From:</code> header field. This should be the sender's email address. Must not be null.
     * @param to      The value of the <code>To:</code> header field. This should be the recipient's email address. May be null
     * @param subject The value of the <code>Subject:</code> header field. This should be the subject of the message. May be null
     */
    public SimpleSMTPHeader(final String from, final String to, final String subject) {
        if (from == null) {
            throw new IllegalArgumentException("From cannot be null");
        }
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.headerFields = new StringBuffer();
        this.cc = null;
    }

    /**
     * Add an email address to the CC (carbon copy or courtesy copy) list.
     *
     * @param address The email address to add to the CC list.
     */
    public void addCC(final String address) {
        if (cc == null) {
            cc = new StringBuffer();
        } else {
            cc.append(", ");
        }

        cc.append(address);
    }

    /**
     * Adds an arbitrary header field with the given value to the article header. These headers will be written before the
     * {@code From}, {@code To}, {@code Subject}, and {@code Cc} fields when the SimpleSMTPHeader is converted to a string.
     * An example use would be:
     *
     * <pre>
     * header.addHeaderField("Organization", "Foobar, Inc.");
     * </pre>
     *
     * @param headerField The header field to add, not including the colon.
     * @param value       The value of the added header field.
     */
    public void addHeaderField(final String headerField, final String value) {
        if (!hasHeaderDate && "Date".equals(headerField)) {
            hasHeaderDate = true;
        }
        headerFields.append(headerField);
        headerFields.append(": ");
        headerFields.append(value);
        headerFields.append('\n');
    }

    /**
     * Converts the SimpleSMTPHeader to a properly formatted header in the form of a String, including the blank line used to separate the header from the
     * article body. The header fields CC and Subject are only included when they are non-null.
     *
     * @return The message header in the form of a String.
     */
    @Override
    public String toString() {
        final StringBuilder header = new StringBuilder();

        final String pattern = "EEE, dd MMM yyyy HH:mm:ss Z"; // Fri, 21 Nov 1997 09:55:06 -0600
        final SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);

        if (!hasHeaderDate) {
            addHeaderField("Date", format.format(new Date()));
        }
        if (headerFields.length() > 0) {
            header.append(headerFields.toString());
        }

        header.append("From: ").append(from).append("\n");

        if (to != null) {
            header.append("To: ").append(to).append("\n");
        }

        if (cc != null) {
            header.append("Cc: ").append(cc.toString()).append("\n");
        }

        if (subject != null) {
            header.append("Subject: ").append(subject).append("\n");
        }

        header.append('\n'); // end of headers; body follows

        return header.toString();
    }
}
