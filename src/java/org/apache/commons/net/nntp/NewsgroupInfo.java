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
 * NewsgroupInfo stores information pertaining to a newsgroup returned by
 * the NNTP GROUP, LIST, and NEWGROUPS commands, implemented by
 * <a href="org.apache.commons.net.nntp.NNTPClient.html#selectNewsgroup">
 * selectNewsgroup </a>,
 * <a href="org.apache.commons.net.nntp.NNTPClient.html#listNewsgroups">
 * listNewsgroups </a>, and
 * <a href="org.apache.commons.net.nntp.NNTPClient.html#listNewNewsgroups">
 * listNewNewsgroups </a> respectively.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see NNTPClient
 ***/

public final class NewsgroupInfo
{
    /***
     * A constant indicating that the posting permission of a newsgroup is
     * unknown.  For example, the NNTP GROUP command does not return posting
     * information, so NewsgroupInfo instances obtained from that command
     * willhave an UNKNOWN_POSTING_PERMISSION.
     ***/
    public static final int UNKNOWN_POSTING_PERMISSION = 0;

    /*** A constant indicating that a newsgroup is moderated. ***/
    public static final int MODERATED_POSTING_PERMISSION = 1;

    /*** A constant indicating that a newsgroup is public and unmoderated. ***/
    public static final int PERMITTED_POSTING_PERMISSION = 2;

    /***
     * A constant indicating that a newsgroup is closed for general posting.
     ***/
    public static final int PROHIBITED_POSTING_PERMISSION = 3;

    private String __newsgroup;
    private int __estimatedArticleCount;
    private int __firstArticle, __lastArticle;
    private int __postingPermission;

    void _setNewsgroup(String newsgroup)
    {
        __newsgroup = newsgroup;
    }

    void _setArticleCount(int count)
    {
        __estimatedArticleCount = count;
    }

    void _setFirstArticle(int first)
    {
        __firstArticle = first;
    }

    void _setLastArticle(int last)
    {
        __lastArticle = last;
    }

    void _setPostingPermission(int permission)
    {
        __postingPermission = permission;
    }

    /***
     * Get the newsgroup name.
     * <p>
     * @return The name of the newsgroup.
     ***/
    public String getNewsgroup()
    {
        return __newsgroup;
    }

    /***
     * Get the estimated number of articles in the newsgroup.  The
     * accuracy of this value will depend on the server implementation.
     * <p>
     * @return The estimated number of articles in the newsgroup.
     ***/
    public int getArticleCount()
    {
        return __estimatedArticleCount;
    }

    /***
     * Get the number of the first article in the newsgroup.
     * <p>
     * @return The number of the first article in the newsgroup.
     ***/
    public int getFirstArticle()
    {
        return __firstArticle;
    }

    /***
     * Get the number of the last article in the newsgroup.
     * <p>
     * @return The number of the last article in the newsgroup.
     ***/
    public int getLastArticle()
    {
        return __lastArticle;
    }

    /***
     * Get the posting permission of the newsgroup.  This will be one of
     * the <code> POSTING_PERMISSION </code> constants.
     * <p>
     * @return The posting permission status of the newsgroup.
     ***/
    public int getPostingPermission()
    {
        return __postingPermission;
    }

    /*
    public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append(__newsgroup);
      buffer.append(' ');
      buffer.append(__lastArticle);
      buffer.append(' ');
      buffer.append(__firstArticle);
      buffer.append(' ');
      switch(__postingPermission) {
        case 1: buffer.append('m'); break;
        case 2: buffer.append('y'); break;
        case 3: buffer.append('n'); break;
      }
      return buffer.toString();
}
    */
}
