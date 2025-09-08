/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.nntp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.net.MalformedServerReplyException;
import org.apache.commons.net.ProtocolCommandSupport;
import org.apache.commons.net.SocketClient;
import org.apache.commons.net.io.CRLFLineReader;

/**
 * The NNTP class is not meant to be used by itself and is provided only so that you may easily implement your own NNTP client if you so desire. If you have no
 * need to perform your own implementation, you should use {@link org.apache.commons.net.nntp.NNTPClient}. The NNTP class is made public to provide access to
 * various NNTP constants and to make it easier for adventurous programmers (or those with special needs) to interact with the NNTP protocol and implement their
 * own clients. A set of methods with names corresponding to the NNTP command names are provided to facilitate this interaction.
 * <p>
 * You should keep in mind that the NNTP server may choose to prematurely close a connection if the client has been idle for longer than a given time period or
 * if the server is being shutdown by the operator or some other reason. The NNTP class will detect a premature NNTP server connection closing when it receives
 * a {@link org.apache.commons.net.nntp.NNTPReply#SERVICE_DISCONTINUED NNTPReply.SERVICE_DISCONTINUED} response to a command. When that occurs, the NNTP class
 * method encountering that reply will throw an {@link org.apache.commons.net.nntp.NNTPConnectionClosedException} . {@code NNTPConectionClosedException} is
 * a subclass of {@code IOException} and therefore need not be caught separately, but if you are going to catch it separately, its catch block must
 * appear before the more general {@code IOException} catch block. When you encounter an
 * {@link org.apache.commons.net.nntp.NNTPConnectionClosedException} , you must disconnect the connection with {@link #disconnect disconnect()} to properly
 * clean up the system resources used by NNTP. Before disconnecting, you may check the last reply code and text with {@link #getReplyCode getReplyCode} and
 * {@link #getReplyString getReplyString}.
 * </p>
 * <p>
 * Rather than list it separately for each method, we mention here that every method communicating with the server and throwing an IOException can also throw a
 * {@link org.apache.commons.net.MalformedServerReplyException} , which is a subclass of IOException. A MalformedServerReplyException will be thrown when the
 * reply received from the server deviates enough from the protocol specification that it cannot be interpreted in a useful manner despite attempts to be as
 * lenient as possible.
 * </p>
 *
 * @see NNTPClient
 * @see NNTPConnectionClosedException
 * @see org.apache.commons.net.MalformedServerReplyException
 */

public class NNTP extends SocketClient {
    /** The default NNTP port. Its value is 119 according to RFC 977. */
    public static final int DEFAULT_PORT = 119;

    // We have to ensure that the protocol communication is in ASCII,
    // but we use ISO-8859-1 just in case 8-bit characters cross
    // the wire.
    private static final Charset DEFAULT_ENCODING = StandardCharsets.ISO_8859_1;

    boolean _isAllowedToPost;
    private int replyCode;
    private String replyString;

    /**
     * Wraps {@link SocketClient#_input_} to communicate with server. Initialized by {@link #_connectAction_}. All server reads should be done through this
     * variable.
     */
    protected BufferedReader _reader_;

    /**
     * Wraps {@link SocketClient#_output_} to communicate with server. Initialized by {@link #_connectAction_}. All server reads should be done through this
     * variable.
     */
    protected BufferedWriter _writer_;

    /**
     * A ProtocolCommandSupport object used to manage the registering of ProtocolCommandListeners and te firing of ProtocolCommandEvents.
     */
    protected ProtocolCommandSupport _commandSupport_;

    /**
     * The default NNTP constructor. Sets the default port to {@code DEFAULT_PORT} and initializes internal data structures for saving NNTP reply
     * information.
     */
    public NNTP() {
        setDefaultPort(DEFAULT_PORT);
        replyString = null;
        _reader_ = null;
        _writer_ = null;
        _isAllowedToPost = false;
        _commandSupport_ = new ProtocolCommandSupport(this);
    }

    /**
     * Initiates control connections and gets initial reply, determining if the client is allowed to post to the server. Initializes {@link #_reader_} and
     * {@link #_writer_} to wrap {@link SocketClient#_input_} and {@link SocketClient#_output_}.
     */
    @Override
    protected void _connectAction_() throws IOException {
        super._connectAction_();
        _reader_ = new CRLFLineReader(new InputStreamReader(_input_, DEFAULT_ENCODING));
        _writer_ = new BufferedWriter(new OutputStreamWriter(_output_, DEFAULT_ENCODING));
        getReply();

        _isAllowedToPost = replyCode == NNTPReply.SERVER_READY_POSTING_ALLOWED;
    }

    /**
     * A convenience method to send the NNTP ARTICLE command to the server, receive the initial reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int article() throws IOException {
        return sendCommand(NNTPCommand.ARTICLE);
    }

    /**
     * @param a article number
     * @return number
     * @throws IOException on error
     * @deprecated - for API compatibility only - DO NOT USE
     */
    @Deprecated
    public int article(final int a) throws IOException {
        return article((long) a);
    }

    /**
     * A convenience method to send the NNTP ARTICLE command to the server, receive the initial reply, and return the reply code.
     *
     * @param articleNumber The number of the article to request from the currently selected newsgroup.
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int article(final long articleNumber) throws IOException {
        return sendCommand(NNTPCommand.ARTICLE, Long.toString(articleNumber));
    }

    /**
     * A convenience method to send the NNTP ARTICLE command to the server, receive the initial reply, and return the reply code.
     *
     * @param messageId The message identifier of the requested article, including the encapsulating &lt; and &gt; characters.
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int article(final String messageId) throws IOException {
        return sendCommand(NNTPCommand.ARTICLE, messageId);
    }

    /**
     * A convenience method to send the AUTHINFO PASS command to the server, receive the reply, and return the reply code. If this step is required, it should
     * immediately follow the AUTHINFO USER command (See RFC 2980)
     *
     * @param password a valid password.
     * @return The reply code received from the server. The server should return a 281 or 502 for this command.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int authinfoPass(final String password) throws IOException {
        final String passParameter = "PASS " + password;
        return sendCommand(NNTPCommand.AUTHINFO, passParameter);
    }

    /**
     * A convenience method to send the AUTHINFO USER command to the server, receive the reply, and return the reply code. (See RFC 2980)
     *
     * @param user A valid user name.
     * @return The reply code received from the server. The server should return a 381 or 281 for this command.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int authinfoUser(final String user) throws IOException {
        final String userParameter = "USER " + user;
        return sendCommand(NNTPCommand.AUTHINFO, userParameter);
    }

    /**
     * A convenience method to send the NNTP BODY command to the server, receive the initial reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int body() throws IOException {
        return sendCommand(NNTPCommand.BODY);
    }

    /**
     * @param a article number
     * @return number
     * @throws IOException on error
     * @deprecated - for API compatibility only - DO NOT USE
     */
    @Deprecated
    public int body(final int a) throws IOException {
        return body((long) a);
    }

    /**
     * A convenience method to send the NNTP BODY command to the server, receive the initial reply, and return the reply code.
     *
     * @param articleNumber The number of the article to request from the currently selected newsgroup.
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int body(final long articleNumber) throws IOException {
        return sendCommand(NNTPCommand.BODY, Long.toString(articleNumber));
    }

    /**
     * A convenience method to send the NNTP BODY command to the server, receive the initial reply, and return the reply code.
     *
     * @param messageId The message identifier of the requested article, including the encapsulating &lt; and &gt; characters.
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int body(final String messageId) throws IOException {
        return sendCommand(NNTPCommand.BODY, messageId);
    }

    /**
     * Closes the connection to the NNTP server and sets to null some internal data so that the memory may be reclaimed by the garbage collector. The reply text
     * and code information from the last command is voided so that the memory it used may be reclaimed.
     *
     * @throws IOException If an error occurs while disconnecting.
     */
    @Override
    public void disconnect() throws IOException {
        super.disconnect();
        _reader_ = null;
        _writer_ = null;
        replyString = null;
        _isAllowedToPost = false;
    }

    /**
     * Gets the command support.
     */
    @Override
    protected ProtocolCommandSupport getCommandSupport() {
        return _commandSupport_;
    }

    /**
     * Gets a reply from the NNTP server and returns the integer reply code. After calling this method, the actual reply text can be accessed from
     * {@link #getReplyString getReplyString}. Only use this method if you are implementing your own NNTP client or if you need to fetch a secondary response
     * from the NNTP server.
     *
     * @return The integer value of the reply code of the fetched NNTP reply. in response to the command.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while receiving the server reply.
     */
    public int getReply() throws IOException {
        replyString = _reader_.readLine();

        if (replyString == null) {
            throw new NNTPConnectionClosedException("Connection closed without indication.");
        }

        // In case we run into an anomaly we don't want fatal index exceptions
        // to be thrown.
        if (replyString.length() < 3) {
            throw new MalformedServerReplyException("Truncated server reply: " + replyString);
        }

        try {
            replyCode = Integer.parseInt(replyString.substring(0, 3));
        } catch (final NumberFormatException e) {
            throw new MalformedServerReplyException("Could not parse response code.\nServer Reply: " + replyString);
        }

        fireReplyReceived(replyCode, replyString + NETASCII_EOL);

        if (replyCode == NNTPReply.SERVICE_DISCONTINUED) {
            throw new NNTPConnectionClosedException("NNTP response 400 received.  Server closed connection.");
        }
        return replyCode;
    }

    /**
     * Gets the integer value of the reply code of the last NNTP reply. You will usually only use this method after you connect to the NNTP server to check
     * that the connection was successful since {@code connect} is of type void.
     *
     * @return The integer value of the reply code of the last NNTP reply.
     */
    public int getReplyCode() {
        return replyCode;
    }

    /**
     * Gets the entire text of the last NNTP server response exactly as it was received, not including the end of line marker.
     *
     * @return The entire text from the last NNTP response as a String.
     */
    public String getReplyString() {
        return replyString;
    }

    /**
     * A convenience method to send the NNTP GROUP command to the server, receive the reply, and return the reply code.
     *
     * @param newsgroup The name of the newsgroup to select.
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int group(final String newsgroup) throws IOException {
        return sendCommand(NNTPCommand.GROUP, newsgroup);
    }

    /**
     * A convenience method to send the NNTP HEAD command to the server, receive the initial reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int head() throws IOException {
        return sendCommand(NNTPCommand.HEAD);
    }

    /**
     * @param a article number
     * @return number
     * @throws IOException on error
     * @deprecated - for API compatibility only - DO NOT USE
     */
    @Deprecated
    public int head(final int a) throws IOException {
        return head((long) a);
    }

    /**
     * A convenience method to send the NNTP HEAD command to the server, receive the initial reply, and return the reply code.
     *
     * @param articleNumber The number of the article to request from the currently selected newsgroup.
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int head(final long articleNumber) throws IOException {
        return sendCommand(NNTPCommand.HEAD, Long.toString(articleNumber));
    }

    /**
     * A convenience method to send the NNTP HEAD command to the server, receive the initial reply, and return the reply code.
     *
     * @param messageId The message identifier of the requested article, including the encapsulating &lt; and &gt; characters.
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int head(final String messageId) throws IOException {
        return sendCommand(NNTPCommand.HEAD, messageId);
    }

    /**
     * A convenience method to send the NNTP HELP command to the server, receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int help() throws IOException {
        return sendCommand(NNTPCommand.HELP);
    }

    /**
     * A convenience method to send the NNTP IHAVE command to the server, receive the reply, and return the reply code.
     *
     * @param messageId The article identifier, including the encapsulating &lt; and &gt; characters.
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int ihave(final String messageId) throws IOException {
        return sendCommand(NNTPCommand.IHAVE, messageId);
    }

    /**
     * Indicates whether or not the client is allowed to post articles to the server it is currently connected to.
     *
     * @return True if the client can post articles to the server, false otherwise.
     */
    public boolean isAllowedToPost() {
        return _isAllowedToPost;
    }

    /**
     * A convenience method to send the NNTP LAST command to the server, receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int last() throws IOException {
        return sendCommand(NNTPCommand.LAST);
    }

    /**
     * A convenience method to send the NNTP LIST command to the server, receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int list() throws IOException {
        return sendCommand(NNTPCommand.LIST);
    }

    /**
     * A convenience wrapper for the extended LIST command that takes an argument, allowing us to selectively list multiple groups.
     *
     * @param wildmat A wildmat (pseudo-regex) pattern. See RFC 2980 for details.
     * @return the reply code received from the server.
     * @throws IOException if the command fails
     */
    public int listActive(final String wildmat) throws IOException {
        final StringBuilder command = new StringBuilder("ACTIVE ");
        command.append(wildmat);
        return sendCommand(NNTPCommand.LIST, command.toString());
    }

    private static void appendDateTimeDistributions(final StringBuilder buffer,
                                                    final String date,
                                                    final String time,
                                                    final boolean GMT,
                                                    final String distributions) {
        buffer.append(date);
        buffer.append(' ');
        buffer.append(time);

        if (GMT) {
            buffer.append(' ');
            buffer.append("GMT");
        }

        if (distributions != null) {
            buffer.append(" <");
            buffer.append(distributions);
            buffer.append('>');
        }
    }

    /**
     * A convenience method to send the "NEWGROUPS" command to the server, receive the reply, and return the reply code.
     *
     * @param date          The date after which to check for new groups. Date format is YYMMDD
     * @param time          The time after which to check for new groups. Time format is HHMMSS using a 24-hour clock.
     * @param GMT           True if the time is in GMT, false if local server time.
     * @param distributions Comma-separated distribution list to check for new groups. Set to null if no distributions.
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int newgroups(final String date, final String time, final boolean GMT, final String distributions) throws IOException {
        final StringBuilder buffer = new StringBuilder();
        appendDateTimeDistributions(buffer, date, time, GMT, distributions);
        return sendCommand(NNTPCommand.NEWGROUPS, buffer.toString());
    }

    /**
     * A convenience method to send the "NEWNEWS" command to the server, receive the reply, and return the reply code.
     *
     * @param newsgroups    A comma-separated list of newsgroups to check for new news.
     * @param date          The date after which to check for new news. Date format is YYMMDD
     * @param time          The time after which to check for new news. Time format is HHMMSS using a 24-hour clock.
     * @param GMT           True if the time is in GMT, false if local server time.
     * @param distributions Comma-separated distribution list to check for new news. Set to null if no distributions.
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int newnews(final String newsgroups, final String date, final String time, final boolean GMT, final String distributions) throws IOException {
        final StringBuilder buffer = new StringBuilder();

        buffer.append(newsgroups);
        buffer.append(' ');
        appendDateTimeDistributions(buffer, date, time, GMT, distributions);
        return sendCommand(NNTPCommand.NEWNEWS, buffer.toString());
    }

    /**
     * A convenience method to send the NNTP NEXT command to the server, receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int next() throws IOException {
        return sendCommand(NNTPCommand.NEXT);
    }

    /**
     * A convenience method to send the NNTP POST command to the server, receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int post() throws IOException {
        return sendCommand(NNTPCommand.POST);
    }

    /**
     * A convenience method to send the NNTP QUIT command to the server, receive the reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int quit() throws IOException {
        return sendCommand(NNTPCommand.QUIT);
    }

    /**
     * Sends an NNTP command with no arguments to the server, waits for a reply and returns the numerical response code. After invocation, for more detailed
     * information, the actual reply text can be accessed by calling {@link #getReplyString getReplyString}.
     *
     * @param command The NNTPCommand constant corresponding to the NNTP command to send.
     * @return The integer value of the NNTP reply code returned by the server in response to the command. in response to the command.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int sendCommand(final int command) throws IOException {
        return sendCommand(command, null);
    }

    /**
     * Sends an NNTP command to the server, waits for a reply and returns the numerical response code. After invocation, for more detailed information, the
     * actual reply text can be accessed by calling {@link #getReplyString getReplyString}.
     *
     * @param command The NNTPCommand constant corresponding to the NNTP command to send.
     * @param args    The arguments to the NNTP command. If this parameter is set to null, then the command is sent with no argument.
     * @return The integer value of the NNTP reply code returned by the server in response to the command. in response to the command.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int sendCommand(final int command, final String args) throws IOException {
        return sendCommand(NNTPCommand.getCommand(command), args);
    }

    /**
     * Sends an NNTP command with no arguments to the server, waits for a reply and returns the numerical response code. After invocation, for more detailed
     * information, the actual reply text can be accessed by calling {@link #getReplyString getReplyString}.
     *
     * @param command The text representation of the NNTP command to send.
     * @return The integer value of the NNTP reply code returned by the server in response to the command. in response to the command.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int sendCommand(final String command) throws IOException {
        return sendCommand(command, null);
    }

    /**
     * Sends an NNTP command to the server, waits for a reply and returns the numerical response code. After invocation, for more detailed information, the
     * actual reply text can be accessed by calling {@link #getReplyString getReplyString}.
     *
     * @param command The text representation of the NNTP command to send.
     * @param args    The arguments to the NNTP command. If this parameter is set to null, then the command is sent with no argument.
     * @return The integer value of the NNTP reply code returned by the server in response to the command.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int sendCommand(final String command, final String args) throws IOException {
        final StringBuilder builder = new StringBuilder(command);
        if (args != null) {
            builder.append(' ');
            builder.append(args);
        }
        builder.append(NETASCII_EOL);
        final String message;
        _writer_.write(message = builder.toString());
        _writer_.flush();
        fireCommandSent(command, message);
        return getReply();
    }

    /**
     * A convenience method to send the NNTP STAT command to the server, receive the initial reply, and return the reply code.
     *
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int stat() throws IOException {
        return sendCommand(NNTPCommand.STAT);
    }

    /**
     * @param a article number
     * @return number
     * @throws IOException on error
     * @deprecated - for API compatibility only - DO NOT USE
     */
    @Deprecated
    public int stat(final int a) throws IOException {
        return stat((long) a);
    }

    /**
     * A convenience method to send the NNTP STAT command to the server, receive the initial reply, and return the reply code.
     *
     * @param articleNumber The number of the article to request from the currently selected newsgroup.
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int stat(final long articleNumber) throws IOException {
        return sendCommand(NNTPCommand.STAT, Long.toString(articleNumber));
    }

    /**
     * A convenience method to send the NNTP STAT command to the server, receive the initial reply, and return the reply code.
     *
     * @param messageId The message identifier of the requested article, including the encapsulating &lt; and &gt; characters.
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int stat(final String messageId) throws IOException {
        return sendCommand(NNTPCommand.STAT, messageId);
    }

    /**
     * A convenience method to send the NNTP XHDR command to the server, receive the reply, and return the reply code.
     *
     * @param header           a String naming a header line (e.g., "subject"). See RFC-1036 for a list of valid header lines.
     * @param selectedArticles a String representation of the range of article headers required. This may be an article number, or a range of article numbers in
     *                         the form "XXXX-YYYY", where XXXX and YYYY are valid article numbers in the current group. It also may be of the form "XXX-",
     *                         meaning "return XXX and all following articles" In this revision, the last format is not possible (yet).
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int xhdr(final String header, final String selectedArticles) throws IOException {
        final StringBuilder command = new StringBuilder(header);
        command.append(" ");
        command.append(selectedArticles);
        return sendCommand(NNTPCommand.XHDR, command.toString());
    }

    /**
     * A convenience method to send the NNTP XOVER command to the server, receive the reply, and return the reply code.
     *
     * @param selectedArticles a String representation of the range of article headers required. This may be an article number, or a range of article numbers in
     *                         the form "XXXX-YYYY", where XXXX and YYYY are valid article numbers in the current group. It also may be of the form "XXX-",
     *                         meaning "return XXX and all following articles" In this revision, the last format is not possible (yet).
     * @return The reply code received from the server.
     * @throws NNTPConnectionClosedException If the NNTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send NNTP reply code 400. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending the command or receiving the server reply.
     */
    public int xover(final String selectedArticles) throws IOException {
        return sendCommand(NNTPCommand.XOVER, selectedArticles);
    }
}
