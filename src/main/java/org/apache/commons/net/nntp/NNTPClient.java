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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.commons.net.MalformedServerReplyException;
import org.apache.commons.net.io.DotTerminatedMessageReader;
import org.apache.commons.net.io.DotTerminatedMessageWriter;
import org.apache.commons.net.io.Util;

/***
 * NNTPClient encapsulates all the functionality necessary to post and
 * retrieve articles from an NNTP server.  As with all classes derived
 * from {@link org.apache.commons.net.SocketClient},
 * you must first connect to the server with
 * {@link org.apache.commons.net.SocketClient#connect  connect }
 * before doing anything, and finally
 * {@link org.apache.commons.net.nntp.NNTP#disconnect  disconnect() }
 * after you're completely finished interacting with the server.
 * Remember that the
 * {@link org.apache.commons.net.nntp.NNTP#isAllowedToPost isAllowedToPost()}
 *  method is defined in
 * {@link org.apache.commons.net.nntp.NNTP}.
 * <p>
 * You should keep in mind that the NNTP server may choose to prematurely
 * close a connection if the client has been idle for longer than a
 * given time period or if the server is being shutdown by the operator or
 * some other reason.  The NNTP class will detect a
 * premature NNTP server connection closing when it receives a
 * {@link org.apache.commons.net.nntp.NNTPReply#SERVICE_DISCONTINUED NNTPReply.SERVICE_DISCONTINUED }
 *  response to a command.
 * When that occurs, the NNTP class method encountering that reply will throw
 * an {@link org.apache.commons.net.nntp.NNTPConnectionClosedException}
 * .
 * <code>NNTPConectionClosedException</code>
 * is a subclass of <code> IOException </code> and therefore need not be
 * caught separately, but if you are going to catch it separately, its
 * catch block must appear before the more general <code> IOException </code>
 * catch block.  When you encounter an
 * {@link org.apache.commons.net.nntp.NNTPConnectionClosedException}
 * , you must disconnect the connection with
 * {@link org.apache.commons.net.nntp.NNTP#disconnect  disconnect() }
 *  to properly clean up the
 * system resources used by NNTP.  Before disconnecting, you may check the
 * last reply code and text with
 * {@link org.apache.commons.net.nntp.NNTP#getReplyCode  getReplyCode } and
 * {@link org.apache.commons.net.nntp.NNTP#getReplyString  getReplyString }.
 * <p>
 * Rather than list it separately for each method, we mention here that
 * every method communicating with the server and throwing an IOException
 * can also throw a
 * {@link org.apache.commons.net.MalformedServerReplyException}
 * , which is a subclass
 * of IOException.  A MalformedServerReplyException will be thrown when
 * the reply received from the server deviates enough from the protocol
 * specification that it cannot be interpreted in a useful manner despite
 * attempts to be as lenient as possible.
 * <p>
 * <p>
 * @author Rory Winston
 * @author Ted Wise
 * @see NNTP
 * @see NNTPConnectionClosedException
 * @see org.apache.commons.net.MalformedServerReplyException
 ***/

public class NNTPClient extends NNTP
{

    /**
     * Parse the reply and store the id and number in the pointer.
     *
     * @param reply the reply to parse "22n nnn <aaa>"
     * @param pointer the pointer to update
     *
     * @throws MalformedServerReplyException
     */
    private void __parseArticlePointer(String reply, ArticleInfo pointer)
    throws MalformedServerReplyException
    {
        String tokens[] = reply.split(" ");
        if (tokens.length >= 3) { // OK, we can parset the line
            int i = 1; // skip reply code
            try
            {
                // Get article number
                pointer.articleNumber = Long.parseLong(tokens[i++]);
                // Get article id
                pointer.articleId = tokens[i++];
                return; // done
            }
            catch (NumberFormatException e)
            {
                // drop through and raise exception
            }
        }
        throw new MalformedServerReplyException(
            "Could not parse article pointer.\nServer reply: " + reply);
    }

    /*
     * 211 n f l s group selected
     *     (n = estimated number of articles in group,
     *     f = first article number in the group,
     *     l = last article number in the group,
     *     s = name of the group.)
     */

    private static void __parseGroupReply(String reply, NewsgroupInfo info)
    throws MalformedServerReplyException
    {
        String tokens[] = reply.split(" ");
        if (tokens.length >= 5) {
            int i = 1;  // Skip numeric response value
            try
            {
                // Get estimated article count
                info._setArticleCount(Long.parseLong(tokens[i++]));
                // Get first article number
                info._setFirstArticle(Long.parseLong(tokens[i++]));
                // Get last article number
                info._setLastArticle(Long.parseLong(tokens[i++]));
                // Get newsgroup name
                info._setNewsgroup(tokens[i++]);

                info._setPostingPermission(NewsgroupInfo.UNKNOWN_POSTING_PERMISSION);
                return ;
            } catch (NumberFormatException e)
            {
               // drop through to report error
            }
        }

        throw new MalformedServerReplyException(
            "Could not parse newsgroup info.\nServer reply: " + reply);
    }


    // Format: group last first p
    static NewsgroupInfo __parseNewsgroupListEntry(String entry)
    {
        String tokens[] = entry.split(" ");
        if (tokens.length < 4) {
            return null;
        }
        NewsgroupInfo result = new NewsgroupInfo();

        int i = 0;

        result._setNewsgroup(tokens[i++]);

        try
        {
            long lastNum = Long.parseLong(tokens[i++]);
            long firstNum = Long.parseLong(tokens[i++]);
            result._setFirstArticle(firstNum);
            result._setLastArticle(lastNum);
            if ((firstNum == 0) && (lastNum == 0)) {
                result._setArticleCount(0);
            } else {
                result._setArticleCount(lastNum - firstNum + 1);
            }
        } catch (NumberFormatException e) {
            return null;
        }

        switch (tokens[i++].charAt(0))
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

    /**
     * Parse a response line from {@link #retrieveArticleInfo(long, long)}.
     *
     * @param line a response line
     * @return the parsed {@link Article}, if unparseable then isDummy()
     * will be true, and the subject will contain the raw info.
     * @since 3.0
     */
    static Article __parseArticleEntry(String line) {
        // Extract the article information
        // Mandatory format (from NNTP RFC 2980) is :
        // articleNumber\tSubject\tAuthor\tDate\tID\tReference(s)\tByte Count\tLine Count

        Article article = new Article();
        article.setSubject(line); // in case parsing fails
        String parts[] = line.split("\t");
        if (parts.length > 6) {
            int i = 0;
            try {
                article.setArticleNumber(Long.parseLong(parts[i++]));
                article.setSubject(parts[i++]);
                article.setFrom(parts[i++]);
                article.setDate(parts[i++]);
                article.setArticleId(parts[i++]);
                article.addReference(parts[i++]);
            } catch (NumberFormatException e) {
                // ignored, already handled
            }
        }
        return article;
    }

    private NewsgroupInfo[] __readNewsgroupListing() throws IOException
    {

        BufferedReader reader = new DotTerminatedMessageReader(_reader_);
        // Start of with a big vector because we may be reading a very large
        // amount of groups.
        Vector<NewsgroupInfo> list = new Vector<NewsgroupInfo>(2048);

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                NewsgroupInfo tmp = __parseNewsgroupListEntry(line);
                if (tmp != null) {
                    list.addElement(tmp);
                } else {
                    throw new MalformedServerReplyException(line);
                }
            }
        } finally {
            reader.close();
        }
        int size;
        if ((size = list.size()) < 1) {
            return new NewsgroupInfo[0];
        }

        NewsgroupInfo[] info = new NewsgroupInfo[size];
        list.copyInto(info);

        return info;
    }


    private BufferedReader __retrieve(int command,
                              String articleId, ArticleInfo pointer)
    throws IOException
    {
        if (articleId != null)
        {
            if (!NNTPReply.isPositiveCompletion(sendCommand(command, articleId))) {
                return null;
            }
        }
        else
        {
            if (!NNTPReply.isPositiveCompletion(sendCommand(command))) {
                return null;
            }
        }


        if (pointer != null) {
            __parseArticlePointer(getReplyString(), pointer);
        }

        return new DotTerminatedMessageReader(_reader_);
    }


    private BufferedReader __retrieve(int command,
                              long articleNumber, ArticleInfo pointer)
    throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(sendCommand(command,
                                            Long.toString(articleNumber)))) {
            return null;
        }

        if (pointer != null) {
            __parseArticlePointer(getReplyString(), pointer);
        }

        return new DotTerminatedMessageReader(_reader_);
    }



    /***
     * Retrieves an article from the NNTP server.  The article is referenced
     * by its unique article identifier (including the enclosing &lt and &gt).
     * The article number and identifier contained in the server reply
     * are returned through an ArticleInfo.  The <code> articleId </code>
     * field of the ArticleInfo cannot always be trusted because some
     * NNTP servers do not correctly follow the RFC 977 reply format.
     * <p>
     * A DotTerminatedMessageReader is returned from which the article can
     * be read.  If the article does not exist, null is returned.
     * <p>
     * You must not issue any commands to the NNTP server (i.e., call any
     * other methods) until you finish reading the message from the returned
     * BufferedReader instance.
     * The NNTP protocol uses the same stream for issuing commands as it does
     * for returning results.  Therefore the returned BufferedReader actually reads
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
    public BufferedReader retrieveArticle(String articleId, ArticleInfo pointer)
    throws IOException
    {
        return __retrieve(NNTPCommand.ARTICLE, articleId, pointer);

    }

    /**
     * Same as <code> retrieveArticle(articleId, (ArticleInfo) null) </code>
     * Note: the return can be cast to a {@link BufferedReader}
     */
    public Reader retrieveArticle(String articleId) throws IOException
    {
        return retrieveArticle(articleId, (ArticleInfo) null);
    }

    /**
     * Same as <code> retrieveArticle((String) null) </code>
     * Note: the return can be cast to a {@link BufferedReader}
     */
    public Reader retrieveArticle() throws IOException
    {
        return retrieveArticle((String) null);
    }


    /***
     * Retrieves an article from the currently selected newsgroup.  The
     * article is referenced by its article number.
     * The article number and identifier contained in the server reply
     * are returned through an ArticleInfo.  The <code> articleId </code>
     * field of the ArticleInfo cannot always be trusted because some
     * NNTP servers do not correctly follow the RFC 977 reply format.
     * <p>
     * A DotTerminatedMessageReader is returned from which the article can
     * be read.  If the article does not exist, null is returned.
     * <p>
     * You must not issue any commands to the NNTP server (i.e., call any
     * other methods) until you finish reading the message from the returned
     * BufferedReader instance.
     * The NNTP protocol uses the same stream for issuing commands as it does
     * for returning results.  Therefore the returned BufferedReader actually reads
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
    public BufferedReader retrieveArticle(long articleNumber, ArticleInfo pointer)
    throws IOException
    {
        return __retrieve(NNTPCommand.ARTICLE, articleNumber, pointer);
    }

    /*** Same as <code> retrieveArticle(articleNumber, null) </code> ***/
    public BufferedReader retrieveArticle(long articleNumber) throws IOException
    {
        return retrieveArticle(articleNumber, null);
    }



    /***
     * Retrieves an article header from the NNTP server.  The article is
     * referenced
     * by its unique article identifier (including the enclosing &lt and &gt).
     * The article number and identifier contained in the server reply
     * are returned through an ArticleInfo.  The <code> articleId </code>
     * field of the ArticleInfo cannot always be trusted because some
     * NNTP servers do not correctly follow the RFC 977 reply format.
     * <p>
     * A DotTerminatedMessageReader is returned from which the article can
     * be read.  If the article does not exist, null is returned.
     * <p>
     * You must not issue any commands to the NNTP server (i.e., call any
     * other methods) until you finish reading the message from the returned
     * BufferedReader instance.
     * The NNTP protocol uses the same stream for issuing commands as it does
     * for returning results.  Therefore the returned BufferedReader actually reads
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
    public BufferedReader retrieveArticleHeader(String articleId, ArticleInfo pointer)
    throws IOException
    {
        return __retrieve(NNTPCommand.HEAD, articleId, pointer);

    }

    /**
     * Same as <code> retrieveArticleHeader(articleId, (ArticleInfo) null) </code>
     *  Note: the return can be cast to a {@link BufferedReader}
     */
    public Reader retrieveArticleHeader(String articleId) throws IOException
    {
        return retrieveArticleHeader(articleId, (ArticleInfo) null);
    }

    /**
     * Same as <code> retrieveArticleHeader((String) null) </code>
     *  Note: the return can be cast to a {@link BufferedReader}
     */
    public Reader retrieveArticleHeader() throws IOException
    {
        return retrieveArticleHeader((String) null);
    }


    /***
     * Retrieves an article header from the currently selected newsgroup.  The
     * article is referenced by its article number.
     * The article number and identifier contained in the server reply
     * are returned through an ArticleInfo.  The <code> articleId </code>
     * field of the ArticleInfo cannot always be trusted because some
     * NNTP servers do not correctly follow the RFC 977 reply format.
     * <p>
     * A DotTerminatedMessageReader is returned from which the article can
     * be read.  If the article does not exist, null is returned.
     * <p>
     * You must not issue any commands to the NNTP server (i.e., call any
     * other methods) until you finish reading the message from the returned
     * BufferedReader instance.
     * The NNTP protocol uses the same stream for issuing commands as it does
     * for returning results.  Therefore the returned BufferedReader actually reads
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
    public BufferedReader retrieveArticleHeader(long articleNumber,
                                        ArticleInfo pointer)
    throws IOException
    {
        return __retrieve(NNTPCommand.HEAD, articleNumber, pointer);
    }


    /*** Same as <code> retrieveArticleHeader(articleNumber, null) </code> ***/
    public BufferedReader retrieveArticleHeader(long articleNumber) throws IOException
    {
        return retrieveArticleHeader(articleNumber, null);
    }



    /***
     * Retrieves an article body from the NNTP server.  The article is
     * referenced
     * by its unique article identifier (including the enclosing &lt and &gt).
     * The article number and identifier contained in the server reply
     * are returned through an ArticleInfo.  The <code> articleId </code>
     * field of the ArticleInfo cannot always be trusted because some
     * NNTP servers do not correctly follow the RFC 977 reply format.
     * <p>
     * A DotTerminatedMessageReader is returned from which the article can
     * be read.  If the article does not exist, null is returned.
     * <p>
     * You must not issue any commands to the NNTP server (i.e., call any
     * other methods) until you finish reading the message from the returned
     * BufferedReader instance.
     * The NNTP protocol uses the same stream for issuing commands as it does
     * for returning results.  Therefore the returned BufferedReader actually reads
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
    public BufferedReader retrieveArticleBody(String articleId, ArticleInfo pointer)
    throws IOException
    {
        return __retrieve(NNTPCommand.BODY, articleId, pointer);

    }

    /**
     * Same as <code> retrieveArticleBody(articleId, (ArticleInfo) null) </code>
     *  Note: the return can be cast to a {@link BufferedReader}
     */
    public Reader retrieveArticleBody(String articleId) throws IOException
    {
        return retrieveArticleBody(articleId, (ArticleInfo) null);
    }

    /**
     * Same as <code> retrieveArticleBody(null) </code>
     *  Note: the return can be cast to a {@link BufferedReader}
     */
    public Reader retrieveArticleBody() throws IOException
    {
        return retrieveArticleBody(null);
    }


    /***
     * Retrieves an article body from the currently selected newsgroup.  The
     * article is referenced by its article number.
     * The article number and identifier contained in the server reply
     * are returned through an ArticleInfo.  The <code> articleId </code>
     * field of the ArticleInfo cannot always be trusted because some
     * NNTP servers do not correctly follow the RFC 977 reply format.
     * <p>
     * A DotTerminatedMessageReader is returned from which the article can
     * be read.  If the article does not exist, null is returned.
     * <p>
     * You must not issue any commands to the NNTP server (i.e., call any
     * other methods) until you finish reading the message from the returned
     * BufferedReader instance.
     * The NNTP protocol uses the same stream for issuing commands as it does
     * for returning results.  Therefore the returned BufferedReader actually reads
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
    public BufferedReader retrieveArticleBody(long articleNumber,
                                      ArticleInfo pointer)
    throws IOException
    {
        return __retrieve(NNTPCommand.BODY, articleNumber, pointer);
    }


    /*** Same as <code> retrieveArticleBody(articleNumber, null) </code> ***/
    public BufferedReader retrieveArticleBody(long articleNumber) throws IOException
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
        if (!NNTPReply.isPositiveCompletion(group(newsgroup))) {
            return false;
        }

        if (info != null) {
            __parseGroupReply(getReplyString(), info);
        }

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
        if (!NNTPReply.isInformational(help())) {
            return null;
        }

        StringWriter help = new StringWriter();
        BufferedReader reader = new DotTerminatedMessageReader(_reader_);
        Util.copyReader(reader, help);
        reader.close();
        help.close();
        return help.toString();
    }

    /**
     * Send a "LIST OVERVIEW.FMT" command to the server.
     *
     * @return the contents of the Overview format, of {@code null} if the command failed
     * @throws IOException
     */
    public String[] listOverviewFmt() throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(sendCommand("LIST", "OVERVIEW.FMT"))){
            return null;
        }

        BufferedReader reader = new DotTerminatedMessageReader(_reader_);
        String line;
        ArrayList<String> list = new ArrayList<String>();
        while((line=reader.readLine()) != null) {
            list.add(line);
        }
        reader.close();
        return list.toArray(new String[list.size()]);
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
    public boolean selectArticle(String articleId, ArticleInfo pointer)
    throws IOException
    {
        if (articleId != null) {
            if (!NNTPReply.isPositiveCompletion(stat(articleId))) {
                return false;
            }
        } else {
            if (!NNTPReply.isPositiveCompletion(stat())) {
                return false;
            }
        }

        if (pointer != null) {
            __parseArticlePointer(getReplyString(), pointer);
        }

        return true;
    }

    /**** Same as <code> selectArticle(articleId, (ArticleInfo) null) </code> ***/
    public boolean selectArticle(String articleId) throws IOException
    {
        return selectArticle(articleId, (ArticleInfo) null);
    }

    /****
     * Same as <code> selectArticle((String) null, articleId) </code>.  Useful
     * for retrieving the current article number.
     ***/
    public boolean selectArticle(ArticleInfo pointer) throws IOException
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
    public boolean selectArticle(long articleNumber, ArticleInfo pointer)
    throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(stat(articleNumber))) {
            return false;
        }

        if (pointer != null) {
            __parseArticlePointer(getReplyString(), pointer);
        }

        return true;
    }


    /*** Same as <code> selectArticle(articleNumber, null) </code> ***/
    public boolean selectArticle(long articleNumber) throws IOException
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
    public boolean selectPreviousArticle(ArticleInfo pointer)
    throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(last())) {
            return false;
        }

        if (pointer != null) {
            __parseArticlePointer(getReplyString(), pointer);
        }

        return true;
    }

    /*** Same as <code> selectPreviousArticle((ArticleInfo) null) </code> ***/
    public boolean selectPreviousArticle() throws IOException
    {
        return selectPreviousArticle((ArticleInfo) null);
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
    public boolean selectNextArticle(ArticleInfo pointer) throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(next())) {
            return false;
        }

        if (pointer != null) {
            __parseArticlePointer(getReplyString(), pointer);
        }

        return true;
    }


    /*** Same as <code> selectNextArticle((ArticleInfo) null) </code> ***/
    public boolean selectNextArticle() throws IOException
    {
        return selectNextArticle((ArticleInfo) null);
    }


    /***
     * List all newsgroups served by the NNTP server.  If no newsgroups
     * are served, a zero length array will be returned.  If the command
     * fails, null will be returned.
     * The method uses the "LIST" command.
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
     * @see #iterateNewsgroupListing()
     * @see #iterateNewsgroups()
     ***/
    public NewsgroupInfo[] listNewsgroups() throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(list())) {
            return null;
        }

        return __readNewsgroupListing();
    }

    /**
     * List all newsgroups served by the NNTP server.  If no newsgroups
     * are served, no entries will be returned.
     * The method uses the "LIST" command.
     * <p>
     * @return An iterable of NewsgroupInfo instances containing the information
     *    for each newsgroup served by the NNTP server.   If no newsgroups
     *    are served, no entries will be returned.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @since 3.0
     */
    public Iterable<String> iterateNewsgroupListing() throws IOException {
        if (NNTPReply.isPositiveCompletion(list())) {
            return new ReplyIterator(_reader_);
        }
        throw new IOException("LIST command failed: "+getReplyString());
    }

    /**
     * List all newsgroups served by the NNTP server.  If no newsgroups
     * are served, no entries will be returned.
     * The method uses the "LIST" command.
     * <p>
     * @return An iterable of Strings containing the raw information
     *    for each newsgroup served by the NNTP server.   If no newsgroups
     *    are served, no entries will be returned.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @since 3.0
     */
    public Iterable<NewsgroupInfo> iterateNewsgroups() throws IOException {
        return new NewsgroupIterator(iterateNewsgroupListing());
    }

    /**
     * List the newsgroups that match a given pattern.
     * Uses the "LIST ACTIVE" command.
     * <p>
     * @param wildmat a pseudo-regex pattern (cf. RFC 2980)
     * @return An array of NewsgroupInfo instances containing the information
     *    for each newsgroup served by the NNTP server corresponding to the
     *    supplied pattern.   If no such newsgroups are served, a zero length
     *    array will be returned.  If the command fails, null will be returned.
     * @throws IOException
     * @see #iterateNewsgroupListing(String)
     * @see #iterateNewsgroups(String)
     */
    public NewsgroupInfo[] listNewsgroups(String wildmat) throws IOException
    {
        if(!NNTPReply.isPositiveCompletion(listActive(wildmat))) {
            return null;
        }
        return __readNewsgroupListing();
    }


    /**
     * List the newsgroups that match a given pattern.
     * Uses the "LIST ACTIVE" command.
     * <p>
     * @param wildmat a pseudo-regex pattern (cf. RFC 2980)
     * @return An iterable of Strings containing the raw information
     *    for each newsgroup served by the NNTP server corresponding to the
     *    supplied pattern.   If no such newsgroups are served, no entries
     *    will be returned.
     * @throws IOException
     * @since 3.0
     */
    public Iterable<String> iterateNewsgroupListing(String wildmat) throws IOException {
        if(NNTPReply.isPositiveCompletion(listActive(wildmat))) {
            return new ReplyIterator(_reader_);
        }
        throw new IOException("LIST ACTIVE "+wildmat+" command failed: "+getReplyString());
    }

    /**
     * List the newsgroups that match a given pattern.
     * Uses the "LIST ACTIVE" command.
     * <p>
     * @param wildmat a pseudo-regex pattern (cf. RFC 2980)
     * @return An iterable NewsgroupInfo instances containing the information
     *    for each newsgroup served by the NNTP server corresponding to the
     *    supplied pattern.   If no such newsgroups are served, no entries
     *    will be returned.
     * @throws IOException
     * @since 3.0
     */
    public Iterable<NewsgroupInfo> iterateNewsgroups(String wildmat) throws IOException {
        return new NewsgroupIterator(iterateNewsgroupListing(wildmat));
    }

    /***
     * List all new newsgroups added to the NNTP server since a particular
     * date subject to the conditions of the specified query.  If no new
     * newsgroups were added, a zero length array will be returned.  If the
     * command fails, null will be returned.
     * This uses the "NEWGROUPS" command.
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
     * @see #iterateNewNewsgroups(NewGroupsOrNewsQuery)
     * @see #iterateNewNewsgroupListing(NewGroupsOrNewsQuery)
     ***/
    public NewsgroupInfo[] listNewNewsgroups(NewGroupsOrNewsQuery query)
    throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(newgroups(
                                                query.getDate(), query.getTime(),
                                                query.isGMT(), query.getDistributions())))
        {
            return null;
        }

        return __readNewsgroupListing();
    }

    /**
     * List all new newsgroups added to the NNTP server since a particular
     * date subject to the conditions of the specified query.  If no new
     * newsgroups were added, no entries will be returned.
     * This uses the "NEWGROUPS" command.
     * <p>
     * @param query  The query restricting how to search for new newsgroups.
     * @return An iterable of Strings containing the raw information
     *    for each new newsgroup added to the NNTP server.   If no newsgroups
     *    were added, no entries will be returned.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @since 3.0
     */
    public Iterable<String> iterateNewNewsgroupListing(NewGroupsOrNewsQuery query) throws IOException {
        if (NNTPReply.isPositiveCompletion(newgroups(
                query.getDate(), query.getTime(),
                query.isGMT(), query.getDistributions()))) {
            return new ReplyIterator(_reader_);
        }
        throw new IOException("NEWGROUPS command failed: "+getReplyString());
    }

    /**
     * List all new newsgroups added to the NNTP server since a particular
     * date subject to the conditions of the specified query.  If no new
     * newsgroups were added, no entries will be returned.
     * This uses the "NEWGROUPS" command.
     * <p>
     * @param query  The query restricting how to search for new newsgroups.
     * @return An iterable of NewsgroupInfo instances containing the information
     *    for each new newsgroup added to the NNTP server.   If no newsgroups
     *    were added, no entries will be returned.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @since 3.0
     */
    public Iterable<NewsgroupInfo> iterateNewNewsgroups(NewGroupsOrNewsQuery query) throws IOException {
        return new NewsgroupIterator(iterateNewNewsgroupListing(query));
    }

    /***
     * List all new articles added to the NNTP server since a particular
     * date subject to the conditions of the specified query.  If no new
     * new news is found, a zero length array will be returned.  If the
     * command fails, null will be returned.  You must add at least one
     * newsgroup to the query, else the command will fail.  Each String
     * in the returned array is a unique message identifier including the
     * enclosing &lt and &gt.
     * This uses the "NEWNEWS" command.
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
     *
     * @see #iterateNewNews(NewGroupsOrNewsQuery)
     ***/
    public String[] listNewNews(NewGroupsOrNewsQuery query)
    throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(
                newnews(query.getNewsgroups(), query.getDate(), query.getTime(),
                        query.isGMT(), query.getDistributions()))) {
            return null;
        }

        Vector<String> list = new Vector<String>();
        BufferedReader reader = new DotTerminatedMessageReader(_reader_);

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                list.addElement(line);
            }
        } finally {
            reader.close();
        }

        int size = list.size();
        if (size < 1) {
            return new String[0];
        }

        String[] result = new String[size];
        list.copyInto(result);

        return result;
    }

    /**
     * List all new articles added to the NNTP server since a particular
     * date subject to the conditions of the specified query.  If no new
     * new news is found, no entries will be returned.
     * This uses the "NEWNEWS" command.
     * You must add at least one newsgroup to the query, else the command will fail.
     * Each String which is returned is a unique message identifier including the
     * enclosing &lt and &gt.
     * <p>
     * @param query  The query restricting how to search for new news.  You
     *    must add at least one newsgroup to the query.
     * @return An iterator of String instances containing the unique message
     *    identifiers for each new article added to the NNTP server.  If no
     *    new news is found, no strings will be returned.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @since 3.0
     */
    public Iterable<String> iterateNewNews(NewGroupsOrNewsQuery query) throws IOException {
        if (NNTPReply.isPositiveCompletion(newnews(
                query.getNewsgroups(), query.getDate(), query.getTime(),
                query.isGMT(), query.getDistributions()))) {
            return new ReplyIterator(_reader_);
        }
        throw new IOException("NEWNEWS command failed: "+getReplyString());
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
     * should check {@link NNTP#isAllowedToPost isAllowedToPost() }
     *  before trying to post.  However, a posting
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
     * {@link org.apache.commons.net.nntp.SimpleNNTPHeader}
     * class to construct the bare minimum acceptable header for most
     * news readers.  To construct more complicated headers you should
     * refer to RFC 822.  When the Java Mail API is finalized, you will be
     * able to use it to compose fully compliant Internet text messages.
     * The DotTerminatedMessageWriter takes care of doubling line-leading
     * dots and ending the message with a single dot upon closing, so all
     * you have to worry about is writing the header and the message.
     * <p>
     * Upon closing the returned Writer, you need to call
     * {@link #completePendingCommand  completePendingCommand() }
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
        if (!NNTPReply.isPositiveIntermediate(post())) {
            return null;
        }

        return new DotTerminatedMessageWriter(_writer_);
    }


    public Writer forwardArticle(String articleId) throws IOException
    {
        if (!NNTPReply.isPositiveIntermediate(ihave(articleId))) {
            return null;
        }

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
     * See {@link NNTP#xover}
     * for legal agument formats. Alternatively, read RFC 2980 :-)
     * <p>
     * @param articleRange
     * @return Returns a DotTerminatedMessageReader if successful, null
     *         otherwise
     * @exception IOException
     */
    private BufferedReader __retrieveArticleInfo(String articleRange)
        throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(xover(articleRange))) {
            return null;
        }

        return new DotTerminatedMessageReader(_reader_);
    }

    /**
     * Return article headers for a specified post.
     * <p>
     * @param articleNumber the article to retrieve headers for
     * @return a DotTerminatedReader if successful, null otherwise
     * @throws IOException
     */
    public BufferedReader retrieveArticleInfo(long articleNumber) throws IOException
    {
        return __retrieveArticleInfo(Long.toString(articleNumber));
    }

    /**
     * Return article headers for all articles between lowArticleNumber
     * and highArticleNumber, inclusively. Uses the XOVER command.
     * <p>
     * @param lowArticleNumber
     * @param highArticleNumber
     * @return a DotTerminatedReader if successful, null otherwise
     * @throws IOException
     */
    public BufferedReader retrieveArticleInfo(long lowArticleNumber,
            long highArticleNumber)
        throws IOException
    {
        return
            __retrieveArticleInfo(lowArticleNumber + "-" +
                                             highArticleNumber);
    }

    /**
     * Return article headers for all articles between lowArticleNumber
     * and highArticleNumber, inclusively, using the XOVER command.
     * <p>
     * @param lowArticleNumber
     * @param highArticleNumber
     * @return an Iterable of Articles
     * @throws IOException if the command failed
     * @since 3.0
     */
    public Iterable<Article> iterateArticleInfo(long lowArticleNumber, long highArticleNumber)
        throws IOException
    {
        BufferedReader info = retrieveArticleInfo(lowArticleNumber,highArticleNumber);
        if (info == null) {
            throw new IOException("XOVER command failed: "+getReplyString());
        }
        // N.B. info is already DotTerminated, so don't rewrap
        return new ArticleIterator(new ReplyIterator(info, false));
    }

    /***
     * Private implementation of XHDR functionality.
     *
     * See {@link NNTP#xhdr}
     * for legal agument formats. Alternatively, read RFC 1036.
     * <p>
     * @param header
     * @param articleRange
     * @return Returns a DotTerminatedMessageReader if successful, null
     *         otherwise
     * @exception IOException
     */
    private BufferedReader __retrieveHeader(String header, String articleRange)
        throws IOException
    {
        if (!NNTPReply.isPositiveCompletion(xhdr(header, articleRange))) {
            return null;
        }

        return new DotTerminatedMessageReader(_reader_);
    }

    /**
     * Return an article header for a specified post.
     * <p>
     * @param header the header to retrieve
     * @param articleNumber the article to retrieve the header for
     * @return a DotTerminatedReader if successful, null otherwise
     * @throws IOException
     */
    public BufferedReader retrieveHeader(String header, long articleNumber)
        throws IOException
    {
        return __retrieveHeader(header, Long.toString(articleNumber));
    }

    /**
     * Return an article header for all articles between lowArticleNumber
     * and highArticleNumber, inclusively.
     * <p>
     * @param header
     * @param lowArticleNumber
     * @param highArticleNumber
     * @return a DotTerminatedReader if successful, null otherwise
     * @throws IOException
     */
    public BufferedReader retrieveHeader(String header, long lowArticleNumber,
                                 long highArticleNumber)
        throws IOException
    {
        return
            __retrieveHeader(header,lowArticleNumber + "-" + highArticleNumber);
    }





    // DEPRECATED METHODS - for API compatibility only - DO NOT USE
    // ============================================================



    /**
     * @deprecated 3.0 use {@link #retrieveHeader(String, long, long)} instead
     */
    @Deprecated
    public Reader retrieveHeader(String s, int l, int h)
        throws IOException
    {
        return retrieveHeader(s, (long) l, (long) h);
    }

    /**
     * @deprecated 3.0 use {@link #retrieveArticleInfo(long, long)} instead
     */
    @Deprecated
    public Reader retrieveArticleInfo(int a, int b) throws IOException {
        return retrieveArticleInfo((long) a, (long) b);
    }

    /**
     * @deprecated 3.0 use {@link #retrieveHeader(String, long)} instead
     */
    @Deprecated
    public Reader retrieveHeader(String a, int b) throws IOException {
        return retrieveHeader(a, (long) b);
    }

    /**
     * @deprecated 3.0 use {@link #selectArticle(long, ArticleInfo)} instead
     */
    @Deprecated
    public boolean selectArticle(int a, ArticlePointer ap) throws IOException {
        ArticleInfo ai =  __ap2ai(ap);
        boolean b = selectArticle(a, ai);
        __ai2ap(ai, ap);
        return b;
    }

    /**
     * @deprecated 3.0 use {@link #retrieveArticleInfo(long)} instead
     */
    @Deprecated
    public Reader retrieveArticleInfo(int a) throws IOException {
        return retrieveArticleInfo((long) a);
    }

    /**
     * @deprecated 3.0 use {@link #selectArticle(long)} instead
     */
    @Deprecated
    public boolean selectArticle(int a) throws IOException {
        return selectArticle((long) a);
    }

    /**
     * @deprecated 3.0 use {@link #retrieveArticleHeader(long)} instead
     */
    @Deprecated
    public Reader retrieveArticleHeader(int a) throws IOException {
        return retrieveArticleHeader((long) a);
    }

    /**
     * @deprecated 3.0 use {@link #retrieveArticleHeader(long, ArticleInfo)} instead
     */
    @Deprecated
    public Reader retrieveArticleHeader(int a, ArticlePointer ap) throws IOException {
        ArticleInfo ai =  __ap2ai(ap);
        Reader rdr = retrieveArticleHeader(a, ai);
        __ai2ap(ai, ap);
        return rdr;
    }

    /**
     * @deprecated 3.0 use {@link #retrieveArticleBody(long)} instead
     */
    @Deprecated
    public Reader retrieveArticleBody(int a) throws IOException {
        return retrieveArticleBody((long) a);
    }

    /**
     * @deprecated 3.0 use {@link #retrieveArticle(long, ArticleInfo)} instead
     */
    @Deprecated
    public Reader retrieveArticle(int a, ArticlePointer ap) throws IOException {
        ArticleInfo ai =  __ap2ai(ap);
        Reader rdr = retrieveArticle(a, ai);
        __ai2ap(ai, ap);
        return rdr;
    }

    /**
     * @deprecated 3.0 use {@link #retrieveArticle(long)} instead
     */
    @Deprecated
    public Reader retrieveArticle(int a) throws IOException {
        return retrieveArticle((long) a);
    }

    /**
     * @deprecated 3.0 use {@link #retrieveArticleBody(long, ArticleInfo)} instead
     */
    @Deprecated
    public Reader retrieveArticleBody(int a, ArticlePointer ap) throws IOException {
        ArticleInfo ai =  __ap2ai(ap);
        Reader rdr = retrieveArticleBody(a, ai);
        __ai2ap(ai, ap);
        return rdr;
    }

    /**
     * @deprecated 3.0 use {@link #retrieveArticle(String, ArticleInfo)} instead
     */
    @Deprecated
    public Reader retrieveArticle(String a, ArticlePointer ap) throws IOException {
        ArticleInfo ai =  __ap2ai(ap);
        Reader rdr = retrieveArticle(a, ai);
        __ai2ap(ai, ap);
        return rdr;
    }

    /**
     * @deprecated 3.0 use {@link #retrieveArticleBody(String, ArticleInfo)} instead
     */
    @Deprecated
    public Reader retrieveArticleBody(String a, ArticlePointer ap) throws IOException {
        ArticleInfo ai =  __ap2ai(ap);
        Reader rdr = retrieveArticleBody(a, ai);
        __ai2ap(ai, ap);
        return rdr;
    }

    /**
     * @deprecated 3.0 use {@link #retrieveArticleHeader(String, ArticleInfo)} instead
     */
    @Deprecated
    public Reader retrieveArticleHeader(String a, ArticlePointer ap) throws IOException {
        ArticleInfo ai =  __ap2ai(ap);
        Reader rdr = retrieveArticleHeader(a, ai);
        __ai2ap(ai, ap);
        return rdr;
    }

    /**
     * @deprecated 3.0 use {@link #selectArticle(String, ArticleInfo)} instead
     */
    @Deprecated
    public boolean selectArticle(String a, ArticlePointer ap) throws IOException {
        ArticleInfo ai =  __ap2ai(ap);
        boolean b = selectArticle(a, ai);
        __ai2ap(ai, ap);
        return b;

    }

    /**
     * @deprecated 3.0 use {@link #selectArticle(ArticleInfo)} instead
     */
    @Deprecated
    public boolean selectArticle(ArticlePointer ap) throws IOException {
        ArticleInfo ai =  __ap2ai(ap);
        boolean b = selectArticle(ai);
        __ai2ap(ai, ap);
        return b;

    }

    /**
     * @deprecated 3.0 use {@link #selectNextArticle(ArticleInfo)} instead
     */
    @Deprecated
    public boolean selectNextArticle(ArticlePointer ap) throws IOException {
        ArticleInfo ai =  __ap2ai(ap);
        boolean b = selectNextArticle(ai);
        __ai2ap(ai, ap);
        return b;

    }

    /**
     * @deprecated 3.0 use {@link #selectPreviousArticle(ArticleInfo)} instead
     */
    @Deprecated
    public boolean selectPreviousArticle(ArticlePointer ap) throws IOException {
        ArticleInfo ai =  __ap2ai(ap);
        boolean b = selectPreviousArticle(ai);
        __ai2ap(ai, ap);
        return b;
    }

   // Helper methods

    private ArticleInfo __ap2ai(@SuppressWarnings("deprecation") ArticlePointer ap) {
        if (ap == null) {
            return null;
        }
        ArticleInfo ai = new ArticleInfo();
        return ai;
    }

    @SuppressWarnings("deprecation")
    private void __ai2ap(ArticleInfo ai, ArticlePointer ap){
        if (ap != null) { // ai cannot be null
            ap.articleId = ai.articleId;
            ap.articleNumber = (int) ai.articleNumber;
        }
    }
}


/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
