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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.apache.commons.net.MalformedServerReplyException;
import org.apache.commons.net.ProtocolCommandSupport;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.SocketClient;

/***
 * The NNTP class is not meant to be used by itself and is provided
 * only so that you may easily implement your own NNTP client if 
 * you so desire.  If you have no need to perform your own implementation,
 * you should use <a href="org.apache.commons.net.nntp.NNTPClient.html">NNTPClient</a>.
 * The NNTP class is made public to provide access to various NNTP constants
 * and to make it easier for adventurous programmers (or those with special
 * needs) to interact with the NNTP protocol and implement their own clients.
 * A set of methods with names corresponding to the NNTP command names are
 * provided to facilitate this interaction.
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
 * <a href="#disconnect"> disconnect() </a> to properly clean up the
 * system resources used by NNTP.  Before disconnecting, you may check the
 * last reply code and text with
 * <a href="#getReplyCode"> getReplyCode </a> and
 * <a href="#getReplyString"> getReplyString </a>.
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
 * @see NNTPClient
 * @see NNTPConnectionClosedException
 * @see org.apache.commons.net.MalformedServerReplyException
 ***/

public class NNTP extends SocketClient
{
    /*** The default NNTP port.  Its value is 119 according to RFC 977. ***/
    public static final int DEFAULT_PORT = 119;

    // We have to ensure that the protocol communication is in ASCII
    // but we use ISO-8859-1 just in case 8-bit characters cross
    // the wire.
    private static final String __DEFAULT_ENCODING = "ISO-8859-1";

    private StringBuffer __commandBuffer;

    boolean _isAllowedToPost;
    int _replyCode;
    String _replyString;

    /**
     * Wraps {@link SocketClient#_input_}
     * to communicate with server.  Initialized by {@link #_connectAction_}.
     * All server reads should be done through this variable.
     */
    protected BufferedReader _reader_;

    /**
     * Wraps {@link SocketClient#_output_}
     * to communicate with server.  Initialized by {@link #_connectAction_}.
     * All server reads should be done through this variable.
     */
    protected BufferedWriter _writer_;

    /***
     * A ProtocolCommandSupport object used to manage the registering of
     * ProtocolCommandListeners and te firing of ProtocolCommandEvents.
     ***/
    protected ProtocolCommandSupport _commandSupport_;

    /***
     * The default NNTP constructor.  Sets the default port to
     * <code>DEFAULT_PORT</code> and initializes internal data structures
     * for saving NNTP reply information.
     ***/
    public NNTP()
    {
        setDefaultPort(DEFAULT_PORT);
        __commandBuffer = new StringBuffer();
        _replyString = null;
        _reader_ = null;
        _writer_ = null;
        _isAllowedToPost = false;
        _commandSupport_ = new ProtocolCommandSupport(this);
    }

    private void __getReply() throws IOException
    {
        _replyString = _reader_.readLine();

        if (_replyString == null)
            throw new NNTPConnectionClosedException(
                "Connection closed without indication.");

        // In case we run into an anomaly we don't want fatal index exceptions
        // to be thrown.
        if (_replyString.length() < 3)
            throw new MalformedServerReplyException(
                "Truncated server reply: " + _replyString);
        try
        {
            _replyCode = Integer.parseInt(_replyString.substring(0, 3));
        }
        catch (NumberFormatException e)
        {
            throw new MalformedServerReplyException(
                "Could not parse response code.\nServer Reply: " + _replyString);
        }

        if (_commandSupport_.getListenerCount() > 0)
            _commandSupport_.fireReplyReceived(_replyCode, _replyString +
                                               SocketClient.NETASCII_EOL);

        if (_replyCode == NNTPReply.SERVICE_DISCONTINUED)
            throw new NNTPConnectionClosedException(
                "NNTP response 400 received.  Server closed connection.");
    }

    /***
     * Initiates control connections and gets initial reply, determining
     * if the client is allowed to post to the server.  Initializes
     * {@link #_reader_} and {@link #_writer_} to wrap
     * {@link SocketClient#_input_} and {@link SocketClient#_output_}.
     ***/
    protected void _connectAction_() throws IOException
    {
        super._connectAction_();
        _reader_ =
            new BufferedReader(new InputStreamReader(_input_,
                                                     __DEFAULT_ENCODING));
        _writer_ =
            new BufferedWriter(new OutputStreamWriter(_output_,
                                                      __DEFAULT_ENCODING));
        __getReply();

        _isAllowedToPost = (_replyCode == NNTPReply.SERVER_READY_POSTING_ALLOWED);
    }

    /***
     * Adds a ProtocolCommandListener.  Delegates this task to
     * <a href="#_commandSupport_"> _commandSupport_ </a>.
     * <p>
     * @param listener  The ProtocolCommandListener to add.
     ***/
    public void addProtocolCommandListener(ProtocolCommandListener listener)
    {
        _commandSupport_.addProtocolCommandListener(listener);
    }

    /***
     * Removes a ProtocolCommandListener.  Delegates this task to
     * <a href="#_commandSupport_"> _commandSupport_ </a>.
     * <p>
     * @param listener  The ProtocolCommandListener to remove.
     ***/
    public void removeProtocolCommandListener(ProtocolCommandListener listener)
    {
        _commandSupport_.removeProtocolCommandListener(listener);
    }

    /***
     * Closes the connection to the NNTP server and sets to null
     * some internal data so that the memory may be reclaimed by the
     * garbage collector.  The reply text and code information from the
     * last command is voided so that the memory it used may be reclaimed.
     * <p>
     * @exception IOException If an error occurs while disconnecting.
     ***/
    public void disconnect() throws IOException
    {
        super.disconnect();
        _reader_ = null;
        _writer_ = null;
        _replyString = null;
        _isAllowedToPost = false;
    }


    /***
     * Indicates whether or not the client is allowed to post articles to
     * the server it is currently connected to.
     * <p>
     * @return True if the client can post articles to the server, false
     *         otherwise.
     ***/
    public boolean isAllowedToPost()
    {
        return _isAllowedToPost;
    }


    /***
     * Sends an NNTP command to the server, waits for a reply and returns the
     * numerical response code.  After invocation, for more detailed
     * information, the actual reply text can be accessed by calling
     * <a href="#getReplyString"> getReplyString </a>.
     * <p>
     * @param command  The text representation of the  NNTP command to send.
     * @param args The arguments to the NNTP command.  If this parameter is
     *             set to null, then the command is sent with no argument.
     * @return The integer value of the NNTP reply code returned by the server
     *         in response to the command.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int sendCommand(String command, String args) throws IOException
    {
        String message;

        __commandBuffer.setLength(0);
        __commandBuffer.append(command);

        if (args != null)
        {
            __commandBuffer.append(' ');
            __commandBuffer.append(args);
        }
        __commandBuffer.append(SocketClient.NETASCII_EOL);

        _writer_.write(message = __commandBuffer.toString());
        _writer_.flush();

        if (_commandSupport_.getListenerCount() > 0)
            _commandSupport_.fireCommandSent(command, message);

        __getReply();
        return _replyCode;
    }


    /***
     * Sends an NNTP command to the server, waits for a reply and returns the
     * numerical response code.  After invocation, for more detailed
     * information, the actual reply text can be accessed by calling
     * <a href="#getReplyString"> getReplyString </a>.
     * <p>
     * @param command  The NNTPCommand constant corresponding to the NNTP command
     *                 to send.
     * @param args The arguments to the NNTP command.  If this parameter is
     *             set to null, then the command is sent with no argument.
     * @return The integer value of the NNTP reply code returned by the server
     *         in response to the command.
     *         in response to the command.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int sendCommand(int command, String args) throws IOException
    {
        return sendCommand(NNTPCommand._commands[command], args);
    }


    /***
     * Sends an NNTP command with no arguments to the server, waits for a
     * reply and returns the numerical response code.  After invocation, for
     * more detailed information, the actual reply text can be accessed by
     * calling <a href="#getReplyString"> getReplyString </a>.
     * <p>
     * @param command  The text representation of the  NNTP command to send.
     * @return The integer value of the NNTP reply code returned by the server
     *         in response to the command.
     *         in response to the command.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int sendCommand(String command) throws IOException
    {
        return sendCommand(command, null);
    }


    /***
     * Sends an NNTP command with no arguments to the server, waits for a
     * reply and returns the numerical response code.  After invocation, for
     * more detailed information, the actual reply text can be accessed by
     * calling <a href="#getReplyString"> getReplyString </a>.
     * <p>
     * @param command  The NNTPCommand constant corresponding to the NNTP command
     *                 to send.
     * @return The integer value of the NNTP reply code returned by the server
     *         in response to the command.
     *         in response to the command.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int sendCommand(int command) throws IOException
    {
        return sendCommand(command, null);
    }


    /***
     * Returns the integer value of the reply code of the last NNTP reply.
     * You will usually only use this method after you connect to the
     * NNTP server to check that the connection was successful since
     * <code> connect </code> is of type void.
     * <p>
     * @return The integer value of the reply code of the last NNTP reply.
     ***/
    public int getReplyCode()
    {
        return _replyCode;
    }

    /***
     * Fetches a reply from the NNTP server and returns the integer reply
     * code.  After calling this method, the actual reply text can be accessed
     * from <a href="#getReplyString"> getReplyString </a>.  Only use this
     * method if you are implementing your own NNTP client or if you need to
     * fetch a secondary response from the NNTP server.
     * <p>
     * @return The integer value of the reply code of the fetched NNTP reply.
     *         in response to the command.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while 
     *      receiving the server reply.
     ***/
    public int getReply() throws IOException
    {
        __getReply();
        return _replyCode;
    }


    /***
     * Returns the entire text of the last NNTP server response exactly
     * as it was received, not including the end of line marker.
     * <p>
     * @return The entire text from the last NNTP response as a String.
     ***/
    public String getReplyString()
    {
        return _replyString;
    }


    /***
     * A convenience method to send the NNTP ARTICLE command to the server,
     * receive the initial reply, and return the reply code.
     * <p>
     * @param messageId  The message identifier of the requested article,
     *                   including the encapsulating &lt and &gt characters.
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int article(String messageId) throws IOException
    {
        return sendCommand(NNTPCommand.ARTICLE, messageId);
    }

    /***
     * A convenience method to send the NNTP ARTICLE command to the server,
     * receive the initial reply, and return the reply code.
     * <p>
     * @param articleNumber The number of the article to request from the
     *                      currently selected newsgroup.
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int article(int articleNumber) throws IOException
    {
        return sendCommand(NNTPCommand.ARTICLE, Integer.toString(articleNumber));
    }

    /***
     * A convenience method to send the NNTP ARTICLE command to the server,
     * receive the initial reply, and return the reply code.
     * <p>
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int article() throws IOException
    {
        return sendCommand(NNTPCommand.ARTICLE);
    }



    /***
     * A convenience method to send the NNTP BODY command to the server,
     * receive the initial reply, and return the reply code.
     * <p>
     * @param messageId  The message identifier of the requested article,
     *                   including the encapsulating &lt and &gt characters.
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int body(String messageId) throws IOException
    {
        return sendCommand(NNTPCommand.BODY, messageId);
    }

    /***
     * A convenience method to send the NNTP BODY command to the server,
     * receive the initial reply, and return the reply code.
     * <p>
     * @param articleNumber The number of the article to request from the
     *                      currently selected newsgroup.
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int body(int articleNumber) throws IOException
    {
        return sendCommand(NNTPCommand.BODY, Integer.toString(articleNumber));
    }

    /***
     * A convenience method to send the NNTP BODY command to the server,
     * receive the initial reply, and return the reply code.
     * <p>
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int body() throws IOException
    {
        return sendCommand(NNTPCommand.BODY);
    }



    /***
     * A convenience method to send the NNTP HEAD command to the server,
     * receive the initial reply, and return the reply code.
     * <p>
     * @param messageId  The message identifier of the requested article,
     *                   including the encapsulating &lt and &gt characters.
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int head(String messageId) throws IOException
    {
        return sendCommand(NNTPCommand.HEAD, messageId);
    }

    /***
     * A convenience method to send the NNTP HEAD command to the server,
     * receive the initial reply, and return the reply code.
     * <p>
     * @param articleNumber The number of the article to request from the
     *                      currently selected newsgroup.
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int head(int articleNumber) throws IOException
    {
        return sendCommand(NNTPCommand.HEAD, Integer.toString(articleNumber));
    }

    /***
     * A convenience method to send the NNTP HEAD command to the server,
     * receive the initial reply, and return the reply code.
     * <p>
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int head() throws IOException
    {
        return sendCommand(NNTPCommand.HEAD);
    }



    /***
     * A convenience method to send the NNTP STAT command to the server,
     * receive the initial reply, and return the reply code.
     * <p>
     * @param messageId  The message identifier of the requested article,
     *                   including the encapsulating &lt and &gt characters.
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int stat(String messageId) throws IOException
    {
        return sendCommand(NNTPCommand.STAT, messageId);
    }

    /***
     * A convenience method to send the NNTP STAT command to the server,
     * receive the initial reply, and return the reply code.
     * <p>
     * @param articleNumber The number of the article to request from the
     *                      currently selected newsgroup.
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int stat(int articleNumber) throws IOException
    {
        return sendCommand(NNTPCommand.STAT, Integer.toString(articleNumber));
    }

    /***
     * A convenience method to send the NNTP STAT command to the server,
     * receive the initial reply, and return the reply code.
     * <p>
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int stat() throws IOException
    {
        return sendCommand(NNTPCommand.STAT);
    }


    /***
     * A convenience method to send the NNTP GROUP command to the server,
     * receive the reply, and return the reply code.
     * <p>
     * @param newsgroup  The name of the newsgroup to select.
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int group(String newsgroup) throws IOException
    {
        return sendCommand(NNTPCommand.GROUP, newsgroup);
    }


    /***
     * A convenience method to send the NNTP HELP command to the server,
     * receive the reply, and return the reply code.
     * <p>
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int help() throws IOException
    {
        return sendCommand(NNTPCommand.HELP);
    }


    /***
     * A convenience method to send the NNTP IHAVE command to the server,
     * receive the reply, and return the reply code.
     * <p>
     * @param messageId  The article identifier,
     *                   including the encapsulating &lt and &gt characters.
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int ihave(String messageId) throws IOException
    {
        return sendCommand(NNTPCommand.IHAVE, messageId);
    }


    /***
     * A convenience method to send the NNTP LAST command to the server,
     * receive the reply, and return the reply code.
     * <p>
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int last() throws IOException
    {
        return sendCommand(NNTPCommand.LAST);
    }



    /***
     * A convenience method to send the NNTP LIST command to the server,
     * receive the reply, and return the reply code.
     * <p>
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int list() throws IOException
    {
        return sendCommand(NNTPCommand.LIST);
    }



    /***
     * A convenience method to send the NNTP NEXT command to the server,
     * receive the reply, and return the reply code.
     * <p>
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int next() throws IOException
    {
        return sendCommand(NNTPCommand.NEXT);
    }


    /***
     * A convenience method to send the NNTP NEWGROUPS command to the server,
     * receive the reply, and return the reply code.
     * <p>
     * @param date The date after which to check for new groups.
     *             Date format is YYMMDD
     * @param time The time after which to check for new groups.
     *             Time format is HHMMSS using a 24-hour clock.
     * @param GMT  True if the time is in GMT, false if local server time.
     * @param distributions  Comma-separated distribution list to check for
     *            new groups. Set to null if no distributions.
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int newgroups(String date, String time, boolean GMT,
                         String distributions) throws IOException
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append(date);
        buffer.append(' ');
        buffer.append(time);

        if (GMT)
        {
            buffer.append(' ');
            buffer.append("GMT");
        }

        if (distributions != null)
        {
            buffer.append(" <");
            buffer.append(distributions);
            buffer.append('>');
        }

        return sendCommand(NNTPCommand.NEWGROUPS, buffer.toString());
    }


    /***
     * A convenience method to send the NNTP NEWGROUPS command to the server,
     * receive the reply, and return the reply code.
     * <p>
     * @param newsgroups A comma-separated list of newsgroups to check for new
     *             news.
     * @param date The date after which to check for new news.
     *             Date format is YYMMDD
     * @param time The time after which to check for new news.
     *             Time format is HHMMSS using a 24-hour clock.
     * @param GMT  True if the time is in GMT, false if local server time.
     * @param distributions  Comma-separated distribution list to check for
     *            new news. Set to null if no distributions.
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int newnews(String newsgroups, String date, String time, boolean GMT,
                       String distributions) throws IOException
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append(newsgroups);
        buffer.append(' ');
        buffer.append(date);
        buffer.append(' ');
        buffer.append(time);

        if (GMT)
        {
            buffer.append(' ');
            buffer.append("GMT");
        }

        if (distributions != null)
        {
            buffer.append(" <");
            buffer.append(distributions);
            buffer.append('>');
        }

        return sendCommand(NNTPCommand.NEWNEWS, buffer.toString());
    }



    /***
     * A convenience method to send the NNTP POST command to the server,
     * receive the reply, and return the reply code.
     * <p>
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int post() throws IOException
    {
        return sendCommand(NNTPCommand.POST);
    }



    /***
     * A convenience method to send the NNTP QUIT command to the server,
     * receive the reply, and return the reply code.
     * <p>
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int quit() throws IOException
    {
        return sendCommand(NNTPCommand.QUIT);
    }
     
    /***
     * A convenience method to send the AUTHINFO USER command to the server, 
     *  receive the reply, and return the reply code. (See RFC 2980)
     * <p>
     * @param username A valid username.
     * @return The reply code received from the server. The server should
     *          return a 381 or 281 for this command. 
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int authinfoUser(String username) throws IOException {
        String userParameter = "USER " + username;
        return sendCommand(NNTPCommand.AUTHINFO, userParameter);
    }

    /***
     * A convenience method to send the AUTHINFO PASS command to the server, 
     * receive the reply, and return the reply code.  If this step is
     * required, it should immediately follow the AUTHINFO USER command
     * (See RFC 2980)
     * <p>
     * @param password a valid password.
     * @return The reply code received from the server. The server should
     *         return a	281 or 502 for this command. 
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int authinfoPass(String password) throws IOException {
        String passParameter = "PASS " + password;
        return sendCommand(NNTPCommand.AUTHINFO, passParameter);
    }
		
    /***
     * A convenience method to send the NNTP XOVER command to the server,
     * receive the reply, and return the reply code.
     * <p>
     * @param selectedArticles a String representation of the range of
     * article headers required. This may be an article number, or a
     * range of article numbers in the form "XXXX-YYYY", where XXXX
     * and YYYY are valid article numbers in the current group.  It
     * also may be of the form "XXX-", meaning "return XXX and all
     * following articles" In this revision, the last format is not
     * possible (yet).
     * @return The reply code received from the server.
     * @exception NNTPConnectionClosedException
     *      If the NNTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send NNTP reply code 400.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int xover(String selectedArticles) throws IOException {
        return sendCommand(NNTPCommand.XOVER, selectedArticles);
    }
		
    /**
     * A convenience wrapper for the extended LIST command that takes
     * an argument, allowing us to selectively list multiple groups.
     * <p>
     * @param wildmat A wildmat (pseudo-regex) pattern. See RFC 2980 for
     *                details.
     * @return the reply code received from the server.
     * @throws IOException
     */
    public int listActive(String wildmat) throws IOException {
        StringBuffer command = new StringBuffer("ACTIVE ");
        command.append(wildmat);
        return sendCommand(NNTPCommand.LIST, command.toString());
    }	
}

/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
