package org.apache.commons.net.nntp;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

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
 * @author Daniel F. Savarese
 * @see NNTPClient
 ***/

public class SimpleNNTPHeader
{
    private String __subject, __from;
    private StringBuffer __newsgroups;
    private StringBuffer __headerFields;
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
        __newsgroups = new StringBuffer();
        __headerFields = new StringBuffer();
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
        if (__newsgroupCount++ > 0)
            __newsgroups.append(',');
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
    public String toString()
    {
        StringBuffer header = new StringBuffer();

        header.append("From: ");
        header.append(__from);
        header.append("\nNewsgroups: ");
        header.append(__newsgroups.toString());
        header.append("\nSubject: ");
        header.append(__subject);
        header.append('\n');
        if (__headerFields.length() > 0)
            header.append(__headerFields.toString());
        header.append('\n');

        return header.toString();
    }
}
