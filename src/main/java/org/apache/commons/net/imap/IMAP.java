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

package org.apache.commons.net.imap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.SocketClient;
import org.apache.commons.net.io.CRLFLineReader;
import org.apache.commons.net.util.NetConstants;

/**
 * The IMAP class provides the basic the functionality necessary to implement your own IMAP client.
 */
public class IMAP extends SocketClient {
    /**
     * Implement this interface and register it via {@link #setChunkListener(IMAPChunkListener)} in order to get access to multi-line partial command responses.
     * Useful when processing large FETCH responses.
     */
    public interface IMAPChunkListener {
        /**
         * Called when a multi-line partial response has been received.
         *
         * @param imap the instance, get the response by calling {@link #getReplyString()} or {@link #getReplyStrings()}
         * @return {@code true} if the reply buffer is to be cleared on return
         */
        boolean chunkReceived(IMAP imap);
    }

    public enum IMAPState {
        /** A constant representing the state where the client is not yet connected to a server. */
        DISCONNECTED_STATE,
        /** A constant representing the "not authenticated" state. */
        NOT_AUTH_STATE,
        /** A constant representing the "authenticated" state. */
        AUTH_STATE,
        /** A constant representing the "logout" state. */
        LOGOUT_STATE
    }

    /** The default IMAP port (RFC 3501). */
    public static final int DEFAULT_PORT = 143;

    // RFC 3501, section 5.1.3. It should be "modified UTF-7".
    /**
     * The default control socket encoding.
     */
    protected static final String __DEFAULT_ENCODING = StandardCharsets.ISO_8859_1.name();

    /**
     * <p>
     * Implementation of IMAPChunkListener that returns {@code true} but otherwise does nothing.
     * </p>
     * <p>
     * This is intended for use with a suitable ProtocolCommandListener. If the IMAP response contains multiple-line data, the protocol listener will be called
     * for each multi-line chunk. The accumulated reply data will be cleared after calling the listener. If the response is very long, this can significantly
     * reduce memory requirements. The listener will also start receiving response data earlier, as it does not have to wait for the entire response to be read.
     * </p>
     * <p>
     * The ProtocolCommandListener must be prepared to accept partial responses. This should not be a problem for listeners that just log the input.
     * </p>
     *
     * @see #setChunkListener(IMAPChunkListener)
     * @since 3.4
     */
    public static final IMAPChunkListener TRUE_CHUNK_LISTENER = imap -> true;

    /**
     * Quote an input string if necessary. If the string is enclosed in double-quotes it is assumed to be quoted already and is returned unchanged. If it is the
     * empty string, "" is returned. If it contains a space then it is enclosed in double quotes, escaping the characters backslash and double-quote.
     *
     * @param input the value to be quoted, may be null
     * @return the quoted value
     */
    static String quoteMailboxName(final String input) {
        if (input == null) { // Don't throw NPE here
            return null;
        }
        if (input.isEmpty()) {
            return "\"\""; // return the string ""
        }
        // Length check is necessary to ensure a lone double-quote is quoted
        if (input.length() > 1 && input.startsWith("\"") && input.endsWith("\"")) {
            return input; // Assume already quoted
        }
        if (input.contains(" ")) {
            // quoted strings must escape \ and "
            return "\"" + input.replaceAll("([\\\\\"])", "\\\\$1") + "\"";
        }
        return input;

    }

    private IMAPState state;
    protected BufferedWriter __writer;

    protected BufferedReader _reader;

    private int replyCode;
    private final List<String> replyLines;

    private volatile IMAPChunkListener chunkListener;

    private final char[] initialID = { 'A', 'A', 'A', 'A' };

    /**
     * The default IMAPClient constructor. Initializes the state to <code>DISCONNECTED_STATE</code>.
     */
    public IMAP() {
        setDefaultPort(DEFAULT_PORT);
        state = IMAPState.DISCONNECTED_STATE;
        _reader = null;
        __writer = null;
        replyLines = new ArrayList<>();
        createCommandSupport();
    }

    /**
     * Performs connection initialization and sets state to {@link IMAPState#NOT_AUTH_STATE}.
     */
    @Override
    protected void _connectAction_() throws IOException {
        super._connectAction_();
        _reader = new CRLFLineReader(new InputStreamReader(_input_, __DEFAULT_ENCODING));
        __writer = new BufferedWriter(new OutputStreamWriter(_output_, __DEFAULT_ENCODING));
        final int tmo = getSoTimeout();
        if (tmo <= 0) { // none set currently
            setSoTimeout(connectTimeout); // use connect timeout to ensure we don't block forever
        }
        getReply(false); // untagged response
        if (tmo <= 0) {
            setSoTimeout(tmo); // restore the original value
        }
        setState(IMAPState.NOT_AUTH_STATE);
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
        _reader = null;
        __writer = null;
        replyLines.clear();
        setState(IMAPState.DISCONNECTED_STATE);
    }

    /**
     * Sends a command to the server and return whether successful.
     *
     * @param command The IMAP command to send (one of the IMAPCommand constants).
     * @return {@code true} if the command was successful
     * @throws IOException on error
     */
    public boolean doCommand(final IMAPCommand command) throws IOException {
        return IMAPReply.isSuccess(sendCommand(command));
    }

    /**
     * Sends a command and arguments to the server and return whether successful.
     *
     * @param command The IMAP command to send (one of the IMAPCommand constants).
     * @param args    The command arguments.
     * @return {@code true} if the command was successful
     * @throws IOException on error
     */
    public boolean doCommand(final IMAPCommand command, final String args) throws IOException {
        return IMAPReply.isSuccess(sendCommand(command, args));
    }

    /**
     * Overrides {@link SocketClient#fireReplyReceived(int, String)} to avoid creating the reply string if there are no listeners to invoke.
     *
     * @param replyCode passed to the listeners
     * @param ignored   the string is only created if there are listeners defined.
     * @see #getReplyString()
     * @since 3.4
     */
    @Override
    protected void fireReplyReceived(final int replyCode, final String ignored) {
        if (getCommandSupport().getListenerCount() > 0) {
            getCommandSupport().fireReplyReceived(replyCode, getReplyString());
        }
    }

    /**
     * Generates a new command ID (tag) for a command.
     *
     * @return a new command ID (tag) for an IMAP command.
     */
    protected String generateCommandID() {
        final String res = new String(initialID);
        // "increase" the ID for the next call
        boolean carry = true; // want to increment initially
        for (int i = initialID.length - 1; carry && i >= 0; i--) {
            if (initialID[i] == 'Z') {
                initialID[i] = 'A';
            } else {
                initialID[i]++;
                carry = false; // did not wrap round
            }
        }
        return res;
    }

    /**
     * Get the reply for a command that expects a tagged response.
     *
     * @throws IOException
     */
    private void getReply() throws IOException {
        getReply(true); // tagged response
    }

    /**
     * Get the reply for a command, reading the response until the reply is found.
     *
     * @param wantTag {@code true} if the command expects a tagged response.
     * @throws IOException
     */
    private void getReply(final boolean wantTag) throws IOException {
        replyLines.clear();
        String line = _reader.readLine();

        if (line == null) {
            throw new EOFException("Connection closed without indication.");
        }

        replyLines.add(line);

        if (wantTag) {
            while (IMAPReply.isUntagged(line)) {
                int literalCount = IMAPReply.literalCount(line);
                final boolean isMultiLine = literalCount >= 0;
                while (literalCount >= 0) {
                    line = _reader.readLine();
                    if (line == null) {
                        throw new EOFException("Connection closed without indication.");
                    }
                    replyLines.add(line);
                    literalCount -= line.length() + 2; // Allow for CRLF
                }
                if (isMultiLine) {
                    final IMAPChunkListener il = chunkListener;
                    if (il != null) {
                        final boolean clear = il.chunkReceived(this);
                        if (clear) {
                            fireReplyReceived(IMAPReply.PARTIAL, getReplyString());
                            replyLines.clear();
                        }
                    }
                }
                line = _reader.readLine(); // get next chunk or final tag
                if (line == null) {
                    throw new EOFException("Connection closed without indication.");
                }
                replyLines.add(line);
            }
            // check the response code on the last line
            replyCode = IMAPReply.getReplyCode(line);
        } else {
            replyCode = IMAPReply.getUntaggedReplyCode(line);
        }

        fireReplyReceived(replyCode, getReplyString());
    }

    /**
     * Returns the reply to the last command sent to the server. The value is a single string containing all the reply lines including newlines.
     *
     * @return The last server response.
     */
    public String getReplyString() {
        final StringBuilder buffer = new StringBuilder(256);
        for (final String s : replyLines) {
            buffer.append(s);
            buffer.append(SocketClient.NETASCII_EOL);
        }

        return buffer.toString();
    }

    /**
     * Returns an array of lines received as a reply to the last command sent to the server. The lines have end of lines truncated.
     *
     * @return The last server response.
     */
    public String[] getReplyStrings() {
        return replyLines.toArray(NetConstants.EMPTY_STRING_ARRAY);
    }

    /**
     * Returns the current IMAP client state.
     *
     * @return The current IMAP client state.
     */
    public IMAP.IMAPState getState() {
        return state;
    }

    /**
     * Sends a command with no arguments to the server and returns the reply code.
     *
     * @param command The IMAP command to send (one of the IMAPCommand constants).
     * @return The server reply code (see IMAPReply).
     * @throws IOException on error
     **/
    public int sendCommand(final IMAPCommand command) throws IOException {
        return sendCommand(command, null);
    }

    /**
     * Sends a command and arguments to the server and returns the reply code.
     *
     * @param command The IMAP command to send (one of the IMAPCommand constants).
     * @param args    The command arguments.
     * @return The server reply code (see IMAPReply).
     * @throws IOException on error
     */
    public int sendCommand(final IMAPCommand command, final String args) throws IOException {
        return sendCommand(command.getIMAPCommand(), args);
    }

    /**
     * Sends a command with no arguments to the server and returns the reply code.
     *
     * @param command The IMAP command to send.
     * @return The server reply code (see IMAPReply).
     * @throws IOException on error
     */
    public int sendCommand(final String command) throws IOException {
        return sendCommand(command, null);
    }

    /**
     * Sends a command an arguments to the server and returns the reply code.
     *
     * @param command The IMAP command to send.
     * @param args    The command arguments.
     * @return The server reply code (see IMAPReply).
     * @throws IOException on error
     */
    public int sendCommand(final String command, final String args) throws IOException {
        return sendCommandWithID(generateCommandID(), command, args);
    }

    /**
     * Sends a command an arguments to the server and returns the reply code.
     *
     * @param commandID The ID (tag) of the command.
     * @param command   The IMAP command to send.
     * @param args      The command arguments.
     * @return The server reply code (either {@link IMAPReply#OK}, {@link IMAPReply#NO} or {@link IMAPReply#BAD}).
     */
    private int sendCommandWithID(final String commandID, final String command, final String args) throws IOException {
        final StringBuilder __commandBuffer = new StringBuilder();
        if (commandID != null) {
            __commandBuffer.append(commandID);
            __commandBuffer.append(' ');
        }
        __commandBuffer.append(command);

        if (args != null) {
            __commandBuffer.append(' ');
            __commandBuffer.append(args);
        }
        __commandBuffer.append(SocketClient.NETASCII_EOL);

        final String message = __commandBuffer.toString();
        __writer.write(message);
        __writer.flush();

        fireCommandSent(command, message);

        getReply();
        return replyCode;
    }

    /**
     * Sends data to the server and returns the reply code.
     *
     * @param command The IMAP command to send.
     * @return The server reply code (see IMAPReply).
     * @throws IOException on error
     */
    public int sendData(final String command) throws IOException {
        return sendCommandWithID(null, command, null);
    }

    /**
     * Sets the current chunk listener. If a listener is registered and the implementation returns true, then any registered
     * {@link org.apache.commons.net.PrintCommandListener PrintCommandListener} instances will be invoked with the partial response and a status of
     * {@link IMAPReply#PARTIAL} to indicate that the final reply code is not yet known.
     *
     * @param listener the class to use, or {@code null} to disable
     * @see #TRUE_CHUNK_LISTENER
     * @since 3.4
     */
    public void setChunkListener(final IMAPChunkListener listener) {
        chunkListener = listener;
    }

    /**
     * Sets IMAP client state. This must be one of the <code>_STATE</code> constants.
     *
     * @param state The new state.
     */
    protected void setState(final IMAP.IMAPState state) {
        this.state = state;
    }
}

