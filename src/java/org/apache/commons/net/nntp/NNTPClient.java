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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.commons.net.io.DotTerminatedMessageReader;
import org.apache.commons.net.io.DotTerminatedMessageWriter;
import org.apache.commons.net.io.Util;
import org.apache.commons.net.MalformedServerReplyException;

/***
 * NNTPClient encapsulates all the functionality necessary to post and
 * retrieve articles from an NNTP server.  As with all classes derived
 * from <a href="org.apache.commons.net.SocketClient.html"> SocketClient </a>,
 * you must first connect to the server with 
 * <a href="org.apache.commons.net.SocketClient.html#connect"> connect </a>
 * before doing anything, and finally
 * <a href="org.apache.commons.net.nntp.NNTP.html#disconnect"> disconnect() </a>
 * after you're completely finished interacting with the server.
 * Remember that the 
 * <a href="org.apache.commons.net.nntp.NNTP.html#isAllowedToPost">
 * isAllowedToPost()</a> method is defined in
 * <a href="org.apache.commons.net.nntp.NNTP.html"> NNTP </a>.
 * <p>
 * You should keep in mind that the NNTP server may choose to prematurely
 * close a connection if the client has been idle for longer than a
 * given time period or if the server is being shutdown by the operator or
 * some other reason.  The NNTP class will detect a
 * premature NNTP server connection closing when it receives a
 * <a href="org.apache.commons.net.nntp.NNTPReply.html#SERVICE_DISCONTINUED">
 * NNTPReply.SERVICE_DISCONTINUED </a> response to a command.
 * When that occurs, the NNTP class method encountering that reply will throw
 * an <a href="org.apache.commons.net.nntp.NNTPConnectionClosedException.html">
 * NNTPConnectionClosedException </a>.
 * <code>NNTPConectionClosedException</code>
 * is a subclass of <code> IOException </code> and therefore need not be
 * caught separately, but if you are going to catch it separately, its
 * catch block must appear before the more general <code> IOException </code>
 * catch block.  When you encounter an
 * <a href="org.apache.commons.net.nntp.NNTPConnectionClosedException.html">
 * NNTPConnectionClosedException </a>, you must disconnect the connection with
 * <a href="org.apache.commons.net.nntp.NNTP.html#disconnect"> disconnect() </a>
 *  to properly clean up the
 * system resources used by NNTP.  Before disconnecting, you may check the
 * last reply code and text with
 * <a href="org.apache.commons.net.nntp.NNTP.html#getReplyCode"> getReplyCode </a> and
 * <a href="org.apache.commons.net.nntp.NNTP.html#getReplyString"> getReplyString </a>.
 * <p>
 * Rather than list it separately for each method, we mention here that
 * every method communicating with the server and throwing an IOException
 * can also throw a 
 * <a href="org.apache.commons.net.MalformedServerReplyException.html">
 * MalformedServerReplyException </a>, which is a subclass
 * of IOException.  A MalformedServerReplyException will be thrown when
 * the reply received from the server deviates enough from the protocol 
 * specification that it cannot be interpreted in a useful manner despite
 * attempts to be as lenient as possible.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see NNTP
 * @see NNTPConnectionClosedException
 * @see org.apache.commons.net.MalformedServerReplyException
 ***/

public class NNTPClient extends NNTP
{

    private void __parseArticlePointer(String reply, ArticlePointer pointer)
    throws MalformedServerReplyException
    {
        StringTokenizer tokenizer;

        // Do loop is a kluge to simulate goto
        do
        {
            tokenizer = new StringTokenizer(reply);

            if (tokenizer.countTokens() < 3)
                break;

            // Skip numeric response value
            tokenizer.nextToken();
            // Get article number
            try
            {
                pointer.articleNumber = Integer.parseInt(tokenizer.nextToken());
            }
            catch (NumberFormatException e)
            {
                break;
            }

            // Get article id
            pointer.articleId = tokenizer.nextToken();
            return ;
        }
        while (false);

        throw new MalformedServerReplyException(
            "Could not parse article pointer.\nServer reply: " + reply);
    }


    private void __parseGroupReply(String reply, NewsgroupInfo info)
    throws MalformedServerReplyException
    {
        String count, first, last;
        StringTokenizer tokenizer;

        // Do loop is a kluge to simulate goto
        do
        {
            tokenizer = new StringTokenizer(reply);

            if (tokenizer.countTokens() < 5)
                break;

            // Skip numeric response value
            tokenizer.nextToken();
            // Get estimated article count
            count = tokenizer.nextToken();
            // Get first article number
            first = tokenizer.nextToken();
            // Get last article number
            last = tokenizer.nextToken();
            // Get newsgroup name
            info._setNewsgroup(tokenizer.nextToken());

            try
            {
                info._setArticleCount(Integer.parseInt(count));
                info._setFirstArticle(Integer.parseInt(first));
                info._setLastArticle(Integer.parseInt(last));
            }
            catch (NumberFormatException e)
            {
                break;
            }

            info._setPostingPermission(NewsgroupInfo.UNKNOWN_POSTING_PERMISSION);
            return ;
        }
        while (false);

        throw new MalformedServerReplyException(
            "Could not parse newsgroup info.\nServer reply: " + reply);
    }


    private NewsgroupInfo __parseNewsgroupListEntry(String entry)
    {
        NewsgroupInfo result;
        StringTokenizer tokenizer;
        int lastNum, firstNum;
        String last, first, permission;

        result = new NewsgroupInfo();
        tokenizer = new StringTokenizer(entry);

        if (tokenizer.countTokens() < 4)
            return null;

        result._setNewsgroup(tokenizer.nextToken());
        last = tokenizer.nextToken();
        first = tokenizer.nextToken();
        permission = tokenizer.nextToken();

        try
        {
            lastNum = Integer.parseInt(last);
            firstNum = Integer.parseInt(first);
            result._setFirstArticle(firstNum);
            result._setLastArticle(lastNum);
            result._setArticleCount(lastNum - firstNum + 1);
        }
        catch (NumberFormatException e)
        {
            return null;
        }

        switch (permission.charAt(0))
        {
        case 'y':
        case 'Y':
            result._setPostingPermission(
                NewsgroupInfo.PERMITTED_POSTING_PERMISSION);
            break;
        case 'n':
        case 'N':
            result._setPostingPermission(
                NewsgroupInfo.PROHIBITED_POSTING_PERMISSION);
            break;
        case 'm':
        case 'M':
            result._setPostingPermission(
                NewsgroupInfo.MODERATED_POSTING_PERMISSION);
            break;
        default:
            result._setPostingPermission(
                NewsgroupInfo.UNKNOWN_POSTING_PERMISSION);
            break;
        }

        return result;
    }

    private NewsgroupInfo[] __readNewsgroupListing() throws IOException
    {
        int size;
        String line;
        Vector list;
        BufferedReader reader;
        NewsgroupInfo tmp, info[];

        reader = new BufferedReader(new DotTerminatedMessageReader(_reader_));
        // Start of with a big vector because we may be reading a very large
        // amount of groups.
        list = new Vector(2048);

        while ((line = reader.readLine()) != null)
        {
            tmp = __parseNewsgroupListEntry(line);
            if (tmp != null)
                list.addElement(tmp);
            else
                throw new MalformedServerReplyException(line);
        }

        if ((size = list.size()) < 1)
            return new NewsgroupInfo[0];

        info = new NewsgroupInfo[size];
        list.copyInto(info);

        return info;
    }


    private Reader __retrieve(int command,
                              String articleId, ArticlePointer pointer)
    throws IOException
    {
        Reader reader;

        if (articleId != null)
        {
            if (!NNTPReply.isPositiveCompletion(sendCommand(command, articleId)))
                return null;
        }
        else
        {
            if (!NNTPReply.isPositiveCompletion(sendCommand(command)))
                return null;
        }


        if (pointer != null)
            __parseArticlePointer(getReplyString(), pointer);

        reader = new DotTerminatedMessageReader(_reader_);
        return reader;
    }


    private Reader __retrieve(int command,
                              int articleNumber, ArticlePointer pointer)
    throws IOException
    {
        Reader reader;

        if (!NNTPReply.isPositiveCompletion(sendCommand(command,
                                            Integer.toString(articleNumber))))
            return null;

        if (pointer != null)
            __parseArticlePointer(getReplyString(), pointer);

        reader = new DotTerminatedMessageReader(_reader_);
        return reader;
    }



    /***
     * Retrieves an article from the NNTP server.  The article is referenced
     * by its unique article identifier (including the enclosing &lt and &gt).
     * The article number and identifier contained in the server reply
     * are returned through an ArticlePointer.  The <code> articleId </code>
     * field of the ArticlePointer cannot always be trusted because some
     * NNTP servers do not correctly follow the RFC 977 reply format.
     * <p>
     * A DotTerminatedMessageReader is returned from which the article can
     * be read.  If the article does not exist, null is returned.
     * <p>
     * You must not issue any commands to the NNTP server (i.e., call any
     * other methods) until you finish reading the message from the returned
     * Reader instance.
     * The NNTP protocol uses the same stream for issuing commands as it does
     * for returning results.  Therefore the returned Reader actually reads
     * directly from the NNTP connection.  After the end of message has been
     * reached, new commands can be executed and their replies read.  If
     * you do not follow these requirements, your program will not work
     * properly.
     * <p>
     * @param articleId  The unique article identifier of the article to 
     *     retrieve.  If this parameter is null, the currently selected
     *     article is retrieved.
     * @param pointer    A parameter through which to return the article's
     *   number and unique id.  The articleId field cannot always be trusted
     *   because of server deviations from RFC 977 reply formats.  You may
     *   set this parameter to null if you do not desire to retrieve the
     *   returned article information.
     * @return A DotTerminatedMessageReader instance from which the article
     *         be read.  null if the article does not exist.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public Reader retrieveArticle(String articleId, ArticlePointer pointer)
    throws IOException
    {
        return __retrieve(NNTPCommand.ARTICLE, articleId, pointer);

    }

    /*** Same as <code> retrieveArticle(articleId, null) </code> ***/
    public Reader retrieveArticle(String articleId) throws IOException
    {
        return retrieveArticle(articleId, null);
    }

    /*** Same as <code> retrieveArticle(null) </code> ***/
    public Reader retrieveArticle() throws IOException
    {
        return retrieveArticle(null);
    }


    /***
     * Retrieves an article from the currently selected newsgroup.  The
     * article is referenced by its article number.
     * The article number and identifier contained in the server reply
     * are returned through an ArticlePointer.  The <code> articleId </code>
     * field of the ArticlePointer cannot always be trusted because some
     * NNTP servers do not correctly follow the RFC 977 reply format.
     * <p>
     * A DotTerminatedMessageReader is returned from which the article can
     * be read.  If the article does not exist, null is returned.
     * <p>
     * You must not issue any commands to the NNTP server (i.e., call any
     * other methods) until you finish reading the message from the returned
     * Reader instance.
     * The NNTP protocol uses the same stream for issuing commands as it does
     * for returning results.  Therefore the returned Reader actually reads
     * directly from the NNTP connection.  After the end of message has been
     * reached, new commands can be executed and their replies read.  If
     * you do not follow these requirements, your program will not work
     * properly.
     * <p>
     * @param articleNumber  The number of the the article to 
     *     retrieve.
     * @param pointer    A parameter through which to return the article's
     *   number and unique id.  The articleId field cannot always be trusted
     *   because of server deviations from RFC 977 reply formats.  You may
     *   set this parameter to null if you do not desire to retrieve the
     *   returned article information.
     * @return A DotTerminatedMessageReader instance from which the article
     *         be read.  null if the article does not exist.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public Reader retrieveArticle(int articleNumber, ArticlePointer pointer)
    throws IOException
    {
        return __retrieve(NNTPCommand.ARTICLE, articleNumber, pointer);
    }

    /*** Same as <code> retrieveArticle(articleNumber, null) </code> ***/
    public Reader retrieveArticle(int articleNumber) throws IOException
    {
        return retrieveArticle(articleNumber, null);
    }



    /***
     * Retrieves an article header from the NNTP server.  The article is
     * referenced
     * by its unique article identifier (including the enclosing &lt and &gt).
     * The article number and identifier contained in the server reply
     * are returned through an ArticlePointer.  The <code> articleId </code>
     * field of the ArticlePointer cannot always be trusted because some
     * NNTP servers do not correctly follow the RFC 977 reply format.
     * <p>
     * A DotTerminatedMessageReader is returned from which the article can
     * be read.  If the article does not exist, null is returned.
     * <p>
     * You must not issue any commands to the NNTP server (i.e., call any
     * other methods) until you finish reading the message from the returned
     * Reader instance.
     * The NNTP protocol uses the same stream for issuing commands as it does
     * for returning results.  Therefore the returned Reader actually reads
     * directly from the NNTP connection.  After the end of message has been
     * reached, new commands can be executed and their replies read.  If
     * you do not follow these requirements, your program will not work
     * properly.
     * <p>
     * @param articleId  The unique article identifier of the article whose
     *    header is being retrieved.  If this parameter is null, the
     *    header of the currently selected article is retrieved.
     * @param pointer    A parameter through which to return the article's
     *   number and unique id.  The articleId field cannot always be trusted
     *   because of server deviations from RFC 977 reply formats.  You may
     *   set this parameter to null if you do not desire to retrieve the
     *   returned article information.
     * @return A DotTerminatedMessageReader instance from which the article
     *         header can be read.  null if the article does not exist.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public Reader retrieveArticleHeader(String articleId, ArticlePointer pointer)
    throws IOException
    {
        return __retrieve(NNTPCommand.HEAD, articleId, pointer);

    }

    /*** Same as <code> retrieveArticleHeader(articleId, null) </code> ***/
    public Reader retrieveArticleHeader(String articleId) throws IOException
    {
        return retrieveArticleHeader(articleId, null);
    }

    /*** Same as <code> retrieveArticleHeader(null) </code> ***/
    public Reader retrieveArticleHeader() throws IOException
    {
        return retrieveArticleHeader(null);
    }


    /***
     * Retrieves an article header from the currently selected newsgroup.  The
     * article is referenced by its article number.
     * The article number and identifier contained in the server reply
     * are returned through an ArticlePointer.  The <code> articleId </code>
     * field of the ArticlePointer cannot always be trusted because some
     * NNTP servers do not correctly follow the RFC 977 reply format.
     * <p>
     * A DotTerminatedMessageReader is returned from which the article can
     * be read.  If the article does not exist, null is returned.
     * <p>
     * You must not issue any commands to the NNTP server (i.e., call any
     * other methods) until you finish reading the message from the returned
     * Reader instance.
     * The NNTP protocol uses the same stream for issuing commands as it does
     * for returning results.  Therefore the returned Reader actually reads
     * directly from the NNTP connection.  After the end of message has been
     * reached, new commands can be executed and their replies read.  If
     * you do not follow these requirements, your program will not work
     * properly.
     * <p>
     * @param articleNumber  The number of the the article whose header is
     *     being retrieved.
     * @param pointer    A parameter through which to return the article's
     *   number and unique id.  The articleId field cannot always be trusted
     *   because of server deviations from RFC 977 reply formats.  You may
     *   set this parameter to null if you do not desire to retrieve the
     *   returned article information.
     * @return A DotTerminatedMessageReader instance from which the article
     *         header can be read.  null if the article does not exist.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public Reader retrieveArticleHeader(int articleNumber,
                                        ArticlePointer pointer)
    throws IOException
    {
        return __retrieve(NNTPCommand.HEAD, articleNumber, pointer);
    }


    /*** Same as <code> retrieveArticleHeader(articleNumber, null) </code> ***/
    public Reader retrieveArticleHeader(int articleNumber) throws IOException
    {
        return retrieveArticleHeader(articleNumber, null);
    }



    /***
     * Retrieves an article body from the NNTP server.  The article is
     * referenced
     * by its unique article identifier (including the enclosing &lt and &gt).
     * The article number and identifier contained in the server reply
     * are returned through an ArticlePointer.  The <code> articleId </code>
     * field of the ArticlePointer cannot always be trusted because some
     * NNTP servers do not correctly follow the RFC 977 reply format.
     * <p>
     * A DotTerminatedMessageReader is returned from which the article can
     * be read.  If the article does not exist, null is returned.
     * <p>
     * You must not issue any commands to the NNTP server (i.e., call any
     * other methods) until you finish reading the message from the returned
     * Reader instance.
     * The NNTP protocol uses the same stream for issuing commands as it does
     * for returning results.  Therefore the returned Reader actually reads
     * directly from the NNTP connection.  After the end of message has been
     * reached, new commands can be executed and their replies read.  If
     * you do not follow these requirements, your program will not work
     * properly.
     * <p>
     * @param articleId  The unique article identifier of the article whose
     *    body is being retrieved.  If this parameter is null, the
     *    body of the currently selected article is retrieved.
     * @param pointer    A parameter through which to return the article's
     *   number and unique id.  The articleId field cannot always be trusted
     *   because of server deviations from RFC 977 reply formats.  You may
     *   set this parameter to null if you do not desire to retrieve the
     *   returned article information.
     * @return A DotTerminatedMessageReader instance from which the article
     *         body can be read.  null if the article does not exist.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public Reader retrieveArticleBody(String articleId, ArticlePointer pointer)
    throws IOException
    {
        return __retrieve(NNTPCommand.BODY, articleId, pointer);

    }

    /*** Same as <code> retrieveArticleBody(articleId, null) </code> ***/
    public Reader retrieveArticleBody(String articleId) throws IOException
    {
        return retrieveArticleBody(articleId, null);
    }

    /*** Same as <code> retrieveArticleBody(null) </code> ***/
    public Reader retrieveArticleBody() throws IOException
    {
        return retrieveArticleBody(null);
    }


    /***
     * Retrieves an article body from the currently selected newsgroup.  The
     * article is referenced by its article number.
     * The article number and identifier contained in the server reply
     * are returned through an ArticlePointer.  The <code> articleId </code>
     * field of the ArticlePointer cannot always be trusted because some
     * NNTP servers do not correctly follow the RFC 977 reply format.
     * <p>
     * A DotTerminatedMessageReader is returned from which the article can
     * be read.  If the article does not exist, null is returned.
     * <p>
     * You must not issue any commands to the NNTP server (i.e., call any
     * other methods) until you finish reading the message from the returned
     * Reader instance.
     * The NNTP protocol uses the same stream for issuing commands as it does
     * for returning results.  Therefore the returned Reader actually reads
     * directly from the NNTP connection.  After the end of message has been
     * reached, new commands can be executed and their replies read.  If
     * you do not follow these requirements, your program will not work
     * properly.
     * <p>
     * @param articleNumber  The number of the the article whose body is
     *     being retrieved.
     * @param pointer    A parameter through which to return the article's
     *   number and unique id.  The articleId field cannot always be trusted
     *   because of server deviations from RFC 977 reply formats.  You may
     *   set this parameter to null if you do not desire to retrieve the
     *   returned article information.
     * @return A DotTerminatedMessageReader instance from which the article
     *         body can be read.  null if the article does not exist.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public Reader retrieveArticleBody(int articleNumber,
                                      ArticlePointer pointer)
    throws IOException
    {
        return __retrieve(NNTPCommand.BODY, articleNumber, pointer);
    }


    /*** Same as <code> retrieveArticleBody(articleNumber, null) </code> ***/
    public Reader retrieveArticleBody(int articleNumber) throws IOException
    {
        return retrieveArticleBody(articleNumber, null);
    }


    /***
     * Select the specified newsgroup to be the target of for future article
     * retrieval and posting operations.  Also return the newsgroup
     * information contained in the server reply through the info parameter.
     * <p>
     * @param newsgroup  The newsgroup to select.
     * @param info  A parameter through which the newsgroup information of
     *      the selected newsgroup contained in the server reply is returned.
     *      Set this to null if you do not desire this information.
     * @return True if the newsgroup exists and was selected, false otherwise.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean selectNewsgroup(String newsgroup, NewsgroupInfo info)
    throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(group(newsgroup)))
            return false;

        if (info != null)
            __parseGroupReply(getReplyString(), info);

        return true;
    }

    /*** Same as <code> selectNewsgroup(newsgroup, null) </code> ***/
    public boolean selectNewsgroup(String newsgroup) throws IOException
    {
        return selectNewsgroup(newsgroup, null);
    }

    /***
     * List the command help from the server.
     * <p>
     * @return The sever help information.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public String listHelp() throws IOException
    {
        StringWriter help;
        Reader reader;

        if (!NNTPReply.isInformational(help()))
            return null;

        help = new StringWriter();
        reader = new DotTerminatedMessageReader(_reader_);
        Util.copyReader(reader, help);
        reader.close();
        help.close();
        return help.toString();
    }


    /***
     * Select an article by its unique identifier (including enclosing
     * &lt and &gt) and return its article number and id through the
     * pointer parameter.  This is achieved through the STAT command.
     * According to RFC 977, this will NOT set the current article pointer
     * on the server.  To do that, you must reference the article by its
     * number.
     * <p>
     * @param articleId  The unique article identifier of the article that
     *    is being selectedd.  If this parameter is null, the
     *    body of the current article is selected
     * @param pointer    A parameter through which to return the article's
     *   number and unique id.  The articleId field cannot always be trusted
     *   because of server deviations from RFC 977 reply formats.  You may
     *   set this parameter to null if you do not desire to retrieve the
     *   returned article information.
     * @return True if successful, false if not.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean selectArticle(String articleId, ArticlePointer pointer)
    throws IOException
    {
        if (articleId != null)
        {
            if (!NNTPReply.isPositiveCompletion(stat(articleId)))
                return false;
        }
        else
        {
            if (!NNTPReply.isPositiveCompletion(stat()))
                return false;
        }

        if (pointer != null)
            __parseArticlePointer(getReplyString(), pointer);

        return true;
    }

    /**** Same as <code> selectArticle(articleId, null) </code> ***/
    public boolean selectArticle(String articleId) throws IOException
    {
        return selectArticle(articleId, null);
    }

    /****
     * Same as <code> selectArticle(null, articleId) </code>.  Useful
     * for retrieving the current article number.
     ***/
    public boolean selectArticle(ArticlePointer pointer) throws IOException
    {
        return selectArticle(null, pointer);
    }


    /***
     * Select an article in the currently selected newsgroup by its number.
     * and return its article number and id through the
     * pointer parameter.  This is achieved through the STAT command.
     * According to RFC 977, this WILL set the current article pointer
     * on the server.  Use this command to select an article before retrieving
     * it, or to obtain an article's unique identifier given its number.
     * <p>
     * @param articleNumber The number of the article to select from the 
     *       currently selected newsgroup.
     * @param pointer    A parameter through which to return the article's
     *   number and unique id.  Although the articleId field cannot always
     *   be trusted because of server deviations from RFC 977 reply formats,
     *   we haven't found a server that misformats this information in response
     *   to this particular command.  You may set this parameter to null if
     *   you do not desire to retrieve the returned article information.
     * @return True if successful, false if not.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean selectArticle(int articleNumber, ArticlePointer pointer)
    throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(stat(articleNumber)))
            return false;

        if (pointer != null)
            __parseArticlePointer(getReplyString(), pointer);

        return true;
    }


    /*** Same as <code> selectArticle(articleNumber, null) </code> ***/
    public boolean selectArticle(int articleNumber) throws IOException
    {
        return selectArticle(articleNumber, null);
    }


    /***
     * Select the article preceeding the currently selected article in the
     * currently selected newsgroup and return its number and unique id
     * through the pointer parameter.  Because of deviating server
     * implementations, the articleId information cannot be trusted.  To
     * obtain the article identifier, issue a
     * <code> selectArticle(pointer.articleNumber, pointer) </code> immediately
     * afterward.
     * <p>
     * @param pointer    A parameter through which to return the article's
     *   number and unique id.  The articleId field cannot always be trusted
     *   because of server deviations from RFC 977 reply formats.  You may
     *   set this parameter to null if you do not desire to retrieve the
     *   returned article information.
     * @return True if successful, false if not (e.g., there is no previous
     *     article).
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean selectPreviousArticle(ArticlePointer pointer)
    throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(last()))
            return false;

        if (pointer != null)
            __parseArticlePointer(getReplyString(), pointer);

        return true;
    }

    /*** Same as <code> selectPreviousArticle(null) </code> ***/
    public boolean selectPreviousArticle() throws IOException
    {
        return selectPreviousArticle(null);
    }


    /***
     * Select the article following the currently selected article in the
     * currently selected newsgroup and return its number and unique id
     * through the pointer parameter.  Because of deviating server
     * implementations, the articleId information cannot be trusted.  To
     * obtain the article identifier, issue a
     * <code> selectArticle(pointer.articleNumber, pointer) </code> immediately
     * afterward.
     * <p>
     * @param pointer    A parameter through which to return the article's
     *   number and unique id.  The articleId field cannot always be trusted
     *   because of server deviations from RFC 977 reply formats.  You may
     *   set this parameter to null if you do not desire to retrieve the
     *   returned article information.
     * @return True if successful, false if not (e.g., there is no following
     *         article).
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean selectNextArticle(ArticlePointer pointer) throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(next()))
            return false;

        if (pointer != null)
            __parseArticlePointer(getReplyString(), pointer);

        return true;
    }


    /*** Same as <code> selectNextArticle(null) </code> ***/
    public boolean selectNextArticle() throws IOException
    {
        return selectNextArticle(null);
    }


    /***
     * List all newsgroups served by the NNTP server.  If no newsgroups
     * are served, a zero length array will be returned.  If the command
     * fails, null will be returned.
     * <p>
     * @return An array of NewsgroupInfo instances containing the information
     *    for each newsgroup served by the NNTP server.   If no newsgroups
     *    are served, a zero length array will be returned.  If the command
     *    fails, null will be returned.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public NewsgroupInfo[] listNewsgroups() throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(list()))
            return null;

        return __readNewsgroupListing();
    }
    
    /**
     * An overloaded listNewsgroups() command that allows us to
     * specify with a pattern what groups we want to list. Wraps the
     * LIST ACTIVE command.
     * <p>
     * @param wildmat a pseudo-regex pattern (cf. RFC 2980)
     * @return
     * @throws IOException
     */
    public NewsgroupInfo[] listNewsgroups(String wildmat) throws IOException 
    {
        if(!NNTPReply.isPositiveCompletion(listActive(wildmat)))
            return null;
        return __readNewsgroupListing();
    }


    /***
     * List all new newsgroups added to the NNTP server since a particular
     * date subject to the conditions of the specified query.  If no new
     * newsgroups were added, a zero length array will be returned.  If the 
     * command fails, null will be returned.
     * <p>
     * @param query  The query restricting how to search for new newsgroups.
     * @return An array of NewsgroupInfo instances containing the information
     *    for each new newsgroup added to the NNTP server.   If no newsgroups
     *    were added, a zero length array will be returned.  If the command
     *    fails, null will be returned.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public NewsgroupInfo[] listNewNewsgroups(NewGroupsOrNewsQuery query)
    throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(newgroups(
                                                query.getDate(), query.getTime(),
                                                query.isGMT(), query.getDistributions())))
            return null;

        return __readNewsgroupListing();
    }


    /***
     * List all new articles added to the NNTP server since a particular
     * date subject to the conditions of the specified query.  If no new
     * new news is found, a zero length array will be returned.  If the 
     * command fails, null will be returned.  You must add at least one
     * newsgroup to the query, else the command will fail.  Each String
     * in the returned array is a unique message identifier including the
     * enclosing &lt and &gt.
     * <p>
     * @param query  The query restricting how to search for new news.  You
     *    must add at least one newsgroup to the query.
     * @return An array of String instances containing the unique message
     *    identifiers for each new article added to the NNTP server.  If no
     *    new news is found, a zero length array will be returned.  If the
     *    command fails, null will be returned.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public String[] listNewNews(NewGroupsOrNewsQuery query)
    throws IOException
    {
        int size;
        String line;
        Vector list;
        String[] result;
        BufferedReader reader;

        if (!NNTPReply.isPositiveCompletion(newnews(
                                                query.getNewsgroups(), query.getDate(), query.getTime(),
                                                query.isGMT(), query.getDistributions())))
            return null;

        list = new Vector();
        reader = new BufferedReader(new DotTerminatedMessageReader(_reader_));

        while ((line = reader.readLine()) != null)
            list.addElement(line);

        size = list.size();

        if (size < 1)
            return new String[0];

        result = new String[size];
        list.copyInto(result);

        return result;
    }

    /***
     * There are a few NNTPClient methods that do not complete the
     * entire sequence of NNTP commands to complete a transaction.  These
     * commands require some action by the programmer after the reception
     * of a positive preliminary command.  After the programmer's code
     * completes its actions, it must call this method to receive
     * the completion reply from the server and verify the success of the
     * entire transaction.
     * <p>
     * For example
     * <pre>
     * writer = client.postArticle();
     * if(writer == null) // failure
     *   return false;
     * header = new SimpleNNTPHeader("foobar@foo.com", "Just testing"); 
     * header.addNewsgroup("alt.test");
     * writer.write(header.toString());
     * writer.write("This is just a test");
     * writer.close();
     * if(!client.completePendingCommand()) // failure
     *   return false;
     * </pre>
     * <p>
     * @return True if successfully completed, false if not.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean completePendingCommand() throws IOException
    {
        return NNTPReply.isPositiveCompletion(getReply());
    }

    /***
     * Post an article to the NNTP server.  This method returns a
     * DotTerminatedMessageWriter instance to which the article can be
     * written.  Null is returned if the posting attempt fails.  You
     * should check <a href="org.apache.commons.nntp.NNTP.html#isAllowedToPost">
     * isAllowedToPost() </a> before trying to post.  However, a posting
     * attempt can fail due to malformed headers.
     * <p>
     * You must not issue any commands to the NNTP server (i.e., call any
     * (other methods) until you finish writing to the returned Writer
     * instance and close it.  The NNTP protocol uses the same stream for
     * issuing commands as it does for returning results.  Therefore the
     * returned Writer actually writes directly to the NNTP connection.
     * After you close the writer, you can execute new commands.  If you
     * do not follow these requirements your program will not work properly.
     * <p>
     * Different NNTP servers will require different header formats, but
     * you can use the provided
     * <a href="org.apache.commons.net.nntp.SimpleNNTPHeader.html"> SimpleNNTPHeader </a>
     * class to construct the bare minimum acceptable header for most
     * news readers.  To construct more complicated headers you should
     * refer to RFC 822.  When the Java Mail API is finalized, you will be
     * able to use it to compose fully compliant Internet text messages.
     * The DotTerminatedMessageWriter takes care of doubling line-leading
     * dots and ending the message with a single dot upon closing, so all
     * you have to worry about is writing the header and the message.
     * <p>
     * Upon closing the returned Writer, you need to call
     * <a href="#completePendingCommand"> completePendingCommand() </a>
     * to finalize the posting and verify its success or failure from
     * the server reply.
     * <p>
     * @return A DotTerminatedMessageWriter to which the article (including
     *      header) can be written.  Returns null if the command fails.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/

    public Writer postArticle() throws IOException
    {
        if (!NNTPReply.isPositiveIntermediate(post()))
            return null;

        return new DotTerminatedMessageWriter(_writer_);
    }


    public Writer forwardArticle(String articleId) throws IOException
    {
        if (!NNTPReply.isPositiveIntermediate(ihave(articleId)))
            return null;

        return new DotTerminatedMessageWriter(_writer_);
    }


    /***
     * Logs out of the news server gracefully by sending the QUIT command.
     * However, you must still disconnect from the server before you can open
     * a new connection.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean logout() throws IOException
    {
        return NNTPReply.isPositiveCompletion(quit());
    }
    

    /**
     * Log into a news server by sending the AUTHINFO USER/AUTHINFO
     * PASS command sequence. This is usually sent in response to a
     * 480 reply code from the NNTP server.
     * <p>
     * @param username a valid username
     * @param password the corresponding password
     * @return True for successful login, false for a failure
     * @throws IOException
     */
    public boolean authenticate(String username, String password)
	throws IOException 
    {
        int replyCode = authinfoUser(username);
		
        if (replyCode == NNTPReply.MORE_AUTH_INFO_REQUIRED) 
            {
                replyCode = authinfoPass(password);
			
                if (replyCode == NNTPReply.AUTHENTICATION_ACCEPTED) 
                    {
                        _isAllowedToPost = true;
                        return true;
                    }
            }
        return false;
    }
			
    /***
     * Private implementation of XOVER functionality.  
     * 
     * See <a href="org.apache.commons.nntp.NNTP.html#xover">
     * for legal agument formats. Alternatively, read RFC 2980 :-) 
     * <p>
     * @param articleRange
     * @return Returns a DotTerminatedMessageReader if successful, null
     *         otherwise
     * @exception IOException
     */
    private Reader __retrieveArticleInfo(String articleRange)
        throws IOException 
    {
        if (!NNTPReply.isPositiveCompletion(xover(articleRange)))
            return null;

        return new DotTerminatedMessageReader(_reader_);
    }
			
    /**
     * Return article headers for a specified post.
     * <p>
     * @param articleNumber the article to retrieve headers for
     * @return a DotTerminatedReader if successful, null otherwise
     * @throws IOException
     */
    public Reader retrieveArticleInfo(int articleNumber) throws IOException 
    {
        return __retrieveArticleInfo(new Integer(articleNumber).toString());
    }
			
    /**
     * Return article headers for all articles between lowArticleNumber
     * and highArticleNumber, inclusively.
     * <p>
     * @param lowArticleNumber 
     * @param highArticleNumber 
     * @return a DotTerminatedReader if successful, null otherwise
     * @throws IOException
     */
    public Reader retrieveArticleInfo(int lowArticleNumber,
                                      int highArticleNumber)
        throws IOException 
    {
        return
            __retrieveArticleInfo(new String(lowArticleNumber + "-" + 
                                             highArticleNumber));
    }
}


/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
