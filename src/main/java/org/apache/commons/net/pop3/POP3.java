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

package org.apache.commons.net.pop3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.MalformedServerReplyException;
import org.apache.commons.net.ProtocolCommandSupport;
import org.apache.commons.net.SocketClient;
import org.apache.commons.net.io.CRLFLineReader;
import org.apache.commons.net.util.NetConstants;

/**
 * The POP3 class is not meant to be used by itself and is provided only so that you may easily implement your own POP3 client if you so desire. If you have no
 * need to perform your own implementation, you should use {@link org.apache.commons.net.pop3.POP3Client}.
 * <p>
 * Rather than list it separately for each method, we mention here that every method communicating with the server and throwing an IOException can also throw a
 * {@link org.apache.commons.net.MalformedServerReplyException} , which is a subclass of IOException. A MalformedServerReplyException will be thrown when the
 * reply received from the server deviates enough from the protocol specification that it cannot be interpreted in a useful manner despite attempts to be as
 * lenient as possible.
 *
 *
 * @see POP3Client
 * @see org.apache.commons.net.MalformedServerReplyException
 */

public class POP3 extends SocketClient {
    /** The default POP3 port. Set to 110 according to RFC 1288. */
    public static final int DEFAULT_PORT = 110;
    /**
     * A constant representing the state where the client is not yet connected to a POP3 server.
     */
    public static final int DISCONNECTED_STATE = -1;
    /** A constant representing the POP3 authorization state. */
    public static final int AUTHORIZATION_STATE = 0;
    /** A constant representing the POP3 transaction state. */
    public static final int TRANSACTION_STATE = 1;
    /** A constant representing the POP3 update state. */
    public static final int UPDATE_STATE = 2;

    static final String OK = "+OK";
    // The reply indicating intermediate response to a command.
    static final String OK_INT = "+ ";
    static final String ERROR = "-ERR";

    // We have to ensure that the protocol communication is in ASCII,
    // but we use ISO-8859-1 just in case 8-bit characters cross
    // the wire.
    static final Charset DEFAULT_ENCODING = StandardCharsets.ISO_8859_1;

    private int popState;
    BufferedWriter writer;

    BufferedReader reader;
    int replyCode;
    String lastReplyLine;
    List<String> replyLines;

    /**
     * A ProtocolCommandSupport object used to manage the registering of ProtocolCommandListeners and the firing of ProtocolCommandEvents.
     */
    protected ProtocolCommandSupport _commandSupport_;

    /**
     * The default POP3Client constructor. Initializes the state to <code>DISCONNECTED_STATE</code>.
     */
    public POP3() {
        setDefaultPort(DEFAULT_PORT);
        popState = DISCONNECTED_STATE;
        reader = null;
        writer = null;
        replyLines = new ArrayList<>();
        _commandSupport_ = new ProtocolCommandSupport(this);
    }

    /**
     * Performs connection initialization and sets state to <code> AUTHORIZATION_STATE </code>.
     */
    @Override
    protected void _connectAction_() throws IOException {
        super._connectAction_();
        reader = new CRLFLineReader(new InputStreamReader(_input_, DEFAULT_ENCODING));
        writer = new BufferedWriter(new OutputStreamWriter(_output_, DEFAULT_ENCODING));
        getReply();
        setState(AUTHORIZATION_STATE);
    }

    /**
     * Disconnects the client from the server, and sets the state to <code> DISCONNECTED_STATE </code>. The reply text information from the last issued command
     * is voided to allow garbage collection of the memory used to store that information.
     *
     * @throws IOException If there is an error in disconnecting.
     */
    @Override
    public void disconnect() throws IOException {
        super.disconnect();
        reader = null;
        writer = null;
        lastReplyLine = null;
        replyLines.clear();
        setState(DISCONNECTED_STATE);
    }

    /**
     * Retrieves the additional lines of a multi-line server reply.
     *
     * @throws IOException on error
     */
    public void getAdditionalReply() throws IOException {
        String line;

        line = reader.readLine();
        while (line != null) {
            replyLines.add(line);
            if (line.equals(".")) {
                break;
            }
            line = reader.readLine();
        }
    }

    /**
     * Provide command support to super-class
     */
    @Override
    protected ProtocolCommandSupport getCommandSupport() {
        return _commandSupport_;
    }

    private void getReply() throws IOException {
        final String line;

        replyLines.clear();
        line = reader.readLine();

        if (line == null) {
            throw new EOFException("Connection closed without indication.");
        }

        if (line.startsWith(OK)) {
            replyCode = POP3Reply.OK;
        } else if (line.startsWith(ERROR)) {
            replyCode = POP3Reply.ERROR;
        } else if (line.startsWith(OK_INT)) {
            replyCode = POP3Reply.OK_INT;
        } else {
            throw new MalformedServerReplyException("Received invalid POP3 protocol response from server." + line);
        }

        replyLines.add(line);
        lastReplyLine = line;

        fireReplyReceived(replyCode, getReplyString());
    }

    /**
     * Returns the reply to the last command sent to the server. The value is a single string containing all the reply lines including newlines. If the reply is
     * a single line, but its format ndicates it should be a multiline reply, then you must call {@link #getAdditionalReply getAdditionalReply() } to fetch the
     * rest of the reply, and then call <code>getReplyString</code> again. You only have to worry about this if you are implementing your own client using the
     * {@link #sendCommand sendCommand } methods.
     *
     * @return The last server response.
     */
    public String getReplyString() {
        final StringBuilder buffer = new StringBuilder(256);

        for (final String entry : replyLines) {
            buffer.append(entry);
            buffer.append(SocketClient.NETASCII_EOL);
        }

        return buffer.toString();
    }

    /**
     * Returns an array of lines received as a reply to the last command sent to the server. The lines have end of lines truncated. If the reply is a single
     * line, but its format ndicates it should be a multiline reply, then you must call {@link #getAdditionalReply getAdditionalReply() } to fetch the rest of
     * the reply, and then call <code>getReplyStrings</code> again. You only have to worry about this if you are implementing your own client using the
     * {@link #sendCommand sendCommand } methods.
     *
     * @return The last server response.
     */
    public String[] getReplyStrings() {
        return replyLines.toArray(NetConstants.EMPTY_STRING_ARRAY);
    }

    /**
     * Returns the current POP3 client state.
     *
     * @return The current POP3 client state.
     */
    public int getState() {
        return popState;
    }

    /**
     * Removes a ProtocolCommandListener.
     *
     * Delegates this incorrectly named method - removeProtocolCommandistener (note the missing "L")- to the correct method
     * {@link SocketClient#removeProtocolCommandListener}
     *
     * @param listener The ProtocolCommandListener to remove
     */
    public void removeProtocolCommandistener(final org.apache.commons.net.ProtocolCommandListener listener) {
        removeProtocolCommandListener(listener);
    }

    /**
     * Sends a command with no arguments to the server and returns the reply code.
     *
     * @param command The POP3 command to send (one of the POP3Command constants).
     * @return The server reply code (either {@code POP3Reply.OK}, {@code POP3Reply.ERROR} or {@code POP3Reply.OK_INT}).
     * @throws IOException on error
     */
    public int sendCommand(final int command) throws IOException {
        return sendCommand(POP3Command.commands[command], null);
    }

    /**
     * Sends a command an arguments to the server and returns the reply code.
     *
     * @param command The POP3 command to send (one of the POP3Command constants).
     * @param args    The command arguments.
     * @return The server reply code (either {@code POP3Reply.OK}, {@code POP3Reply.ERROR} or {@code POP3Reply.OK_INT}).
     * @throws IOException on error
     */
    public int sendCommand(final int command, final String args) throws IOException {
        return sendCommand(POP3Command.commands[command], args);
    }

    /**
     * Sends a command with no arguments to the server and returns the reply code.
     *
     * @param command The POP3 command to send.
     * @return The server reply code (either {@code POP3Reply.OK}, {@code POP3Reply.ERROR} or {@code POP3Reply.OK_INT}).
     * @throws IOException on error
     */
    public int sendCommand(final String command) throws IOException {
        return sendCommand(command, null);
    }

    /**
     * Sends a command an arguments to the server and returns the reply code.
     *
     * @param command The POP3 command to send.
     * @param args    The command arguments.
     * @return The server reply code (either {@code POP3Reply.OK}, {@code POP3Reply.ERROR} or {@code POP3Reply.OK_INT}).
     * @throws IOException on error
     */
    public int sendCommand(final String command, final String args) throws IOException {
        if (writer == null) {
            throw new IllegalStateException("Socket is not connected");
        }
        final StringBuilder __commandBuffer = new StringBuilder();
        __commandBuffer.append(command);

        if (args != null) {
            __commandBuffer.append(' ');
            __commandBuffer.append(args);
        }
        __commandBuffer.append(SocketClient.NETASCII_EOL);

        final String message = __commandBuffer.toString();
        writer.write(message);
        writer.flush();

        fireCommandSent(command, message);

        getReply();
        return replyCode;
    }

    /**
     * Set the internal POP3 state.
     *
     * @param state the new state. This must be one of the <code>_STATE</code> constants.
     */
    public void setState(final int state) {
        popState = state;
    }
}
