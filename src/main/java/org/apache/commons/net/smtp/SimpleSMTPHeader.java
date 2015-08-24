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

/***
 * This class is used to construct a bare minimum
 * acceptable header for an email message.  To construct more
 * complicated headers you should refer to RFC 5322.  When the
 * Java Mail API is finalized, you will be
 * able to use it to compose fully compliant Internet text messages.
 * <p>
 * The main purpose of the class is to faciliatate the mail sending
 * process, by relieving the programmer from having to explicitly format
 * a simple message header.  For example:
 * <pre>
 * writer = client.sendMessageData();
 * if(writer == null) // failure
 *   return false;
 * header =
 *    new SimpleSMTPHeader("foobar@foo.com", "foo@bar.com" "Just testing");
 * header.addCC("bar@foo.com");
 * header.addHeaderField("Organization", "Foobar, Inc.");
 * writer.write(header.toString());
 * writer.write("This is just a test");
 * writer.close();
 * if(!client.completePendingCommand()) // failure
 *   return false;
 * </pre>
 *
 * @see SMTPClient
 ***/

public class SimpleSMTPHeader
{
    private final String __subject;
    private final String __from;
    private final String __to;
    private final StringBuffer __headerFields;
    private boolean hasHeaderDate;
    private StringBuffer __cc;

    /***
     * Creates a new SimpleSMTPHeader instance initialized with the given
     * from, to, and subject header field values.
     * <p>
     * @param from  The value of the <code>From:</code> header field.  This
     *              should be the sender's email address.
     *              Must not be null.
     * @param to    The value of the <code>To:</code> header field.  This
     *              should be the recipient's email address.
     *              May be null
     * @param subject  The value of the <code>Subject:</code> header field.
     *              This should be the subject of the message.
     *              May be null
     ***/
    public SimpleSMTPHeader(String from, String to, String subject)
    {
        if (from == null) {
            throw new IllegalArgumentException("From cannot be null");
        }
        __to = to;
        __from = from;
        __subject = subject;
        __headerFields = new StringBuffer();
        __cc = null;
    }

    /***
     * Adds an arbitrary header field with the given value to the article
     * header.  These headers will be written before the From, To, Subject, and
     * Cc fields when the SimpleSMTPHeader is convertered to a string.
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
        if (!hasHeaderDate && "Date".equals(headerField)) {
            hasHeaderDate = true;
        }
        __headerFields.append(headerField);
        __headerFields.append(": ");
        __headerFields.append(value);
        __headerFields.append('\n');
    }


    /***
     * Add an email address to the CC (carbon copy or courtesy copy) list.
     * <p>
     * @param address The email address to add to the CC list.
     ***/
    public void addCC(String address)
    {
        if (__cc == null) {
            __cc = new StringBuffer();
        } else {
            __cc.append(", ");
        }

        __cc.append(address);
    }


    /***
     * Converts the SimpleSMTPHeader to a properly formatted header in
     * the form of a String, including the blank line used to separate
     * the header from the article body.  The header fields CC and Subject
     * are only included when they are non-null.
     * <p>
     * @return The message header in the form of a String.
     ***/
    @Override
    public String toString()
    {
        StringBuilder header = new StringBuilder();

        final String pattern = "EEE, dd MMM yyyy HH:mm:ss Z"; // Fri, 21 Nov 1997 09:55:06 -0600
        final SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);

        if (!hasHeaderDate) {
            addHeaderField("Date", format.format(new Date()));
        }
        if (__headerFields.length() > 0) {
            header.append(__headerFields.toString());
        }

        header.append("From: ").append(__from).append("\n");

        if (__to != null) {
            header.append("To: ").append(__to).append("\n");
        }

        if (__cc != null)
        {
            header.append("Cc: ").append(__cc.toString()).append("\n");
        }

        if (__subject != null)
        {
            header.append("Subject: ").append(__subject).append("\n");
        }

        header.append('\n'); // end of headers; body follows

        return header.toString();
    }
}



