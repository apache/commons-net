/***
 * $Id: SimpleNNTPHeader.java,v 1.1 2002/04/03 01:04:35 brekke Exp $
 *
 * NetComponents Internet Protocol Library
 * Copyright (C) 1997-2002  Daniel F. Savarese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library in the LICENSE file; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 ***/

package com.oroinc.net.nntp;

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

public class SimpleNNTPHeader {
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
  public SimpleNNTPHeader(String from, String subject) {
    __from       = from;
    __subject    = subject;
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
  public void addNewsgroup(String newsgroup) {
    if(__newsgroupCount++ > 0)
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
  public void addHeaderField(String headerField, String value) {
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
  public String getFromAddress() { return __from; }

  /***
   * Returns the subject used in the <code> Subject: </code> header field.
   * <p>
   * @return The subject.
   ***/
  public String getSubject()     { return __subject; }

  /***
   * Returns the contents of the <code> Newsgroups: </code> header field.
   * <p>
   * @return The comma-separated list of newsgroups to which the article
   *         is being posted.
   ***/
  public String getNewsgroups()  { return __newsgroups.toString(); }

  /***
   * Converts the SimpleNNTPHeader to a properly formatted header in
   * the form of a String, including the blank line used to separate
   * the header from the article body.
   * <p>
   * @return The article header in the form of a String.
   ***/
  public String toString() {
    StringBuffer header = new StringBuffer();

    header.append("From: ");
    header.append(__from);
    header.append("\nNewsgroups: ");
    header.append(__newsgroups.toString());
    header.append("\nSubject: ");
    header.append(__subject);
    header.append('\n');
    if(__headerFields.length() > 0)
      header.append(__headerFields.toString());
    header.append('\n');

    return header.toString();
  }
}
