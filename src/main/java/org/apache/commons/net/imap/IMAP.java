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
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.SocketClient;
import org.apache.commons.net.io.CRLFLineReader;


/**
 * The IMAP class provides the basic the functionality necessary to implement your
 * own IMAP client.
 */
public class IMAP extends SocketClient
{
    /** The default IMAP port (RFC 3501). */
    public static final int DEFAULT_PORT = 143;

    public enum IMAPState
    {
        /** A constant representing the state where the client is not yet connected to a server. */
        DISCONNECTED_STATE,
        /**  A constant representing the "not authenticated" state. */
        NOT_AUTH_STATE,
        /**  A constant representing the "authenticated" state. */
        AUTH_STATE,
        /**  A constant representing the "logout" state. */
        LOGOUT_STATE;
    }

    // RFC 3501, section 5.1.3. It should be "modified UTF-7".
    /**
     * The default control socket ecoding.
     */
    protected static final String __DEFAULT_ENCODING = "ISO-8859-1";

    private IMAPState __state;
    protected BufferedWriter __writer;

    protected BufferedReader _reader;
    private int _replyCode;
    private final List<String> _replyLines;

    private final char[] _initialID = { 'A', 'A', 'A', 'A' };

    /**
     * The default IMAPClient constructor.  Initializes the state
     * to <code>DISCONNECTED_STATE</code>.
     */
    public IMAP()
    {
        setDefaultPort(DEFAULT_PORT);
        __state = IMAPState.DISCONNECTED_STATE;
        _reader = null;
        __writer = null;
        _replyLines = new ArrayList<String>();
        createCommandSupport();
    }

    /**
     * Get the reply for a command that expects a tagged response.
     *
     * @throws IOException
     */
    private void __getReply() throws IOException
    {
        __getReply(true); // tagged response
    }

    /**
     * Get the reply for a command, reading the response until the
     * reply is found.
     *
     * @param wantTag {@code true} if the command expects a tagged response.
     * @throws IOException
     */
    private void __getReply(boolean wantTag) throws IOException
    {
        _replyLines.clear();
        String line = _reader.readLine();

        if (line == null) {
            throw new EOFException("Connection closed without indication.");
        }

        _replyLines.add(line);

        if (wantTag) {
            while(IMAPReply.isUntagged(line)) {
                int literalCount = IMAPReply.literalCount(line);
                while (literalCount >= 0) {
                    line=_reader.readLine();
                    if (line == null) {
                        throw new EOFException("Connection closed without indication.");
                    }
                    _replyLines.add(line);
                    literalCount -= (line.length() + 2); // Allow for CRLF
                }
                line = _reader.readLine();
                if (line == null) {
                    throw new EOFException("Connection closed without indication.");
                }
                _replyLines.add(line);
            }
            // check the response code on the last line
            _replyCode = IMAPReply.getReplyCode(line);
        } else {
            _replyCode = IMAPReply.getUntaggedReplyCode(line);
        }

        fireReplyReceived(_replyCode, getReplyString());
    }

    /**
     * Performs connection initialization and sets state to
     * {@link IMAPState#NOT_AUTH_STATE}.
     */
    @Override
    protected void _connectAction_() throws IOException
    {
        super._connectAction_();
        _reader =
          new CRLFLineReader(new InputStreamReader(_input_,
                                                   __DEFAULT_ENCODING));
        __writer =
          new BufferedWriter(new OutputStreamWriter(_output_,
                                                    __DEFAULT_ENCODING));
        int tmo = getSoTimeout();
        if (tmo <= 0) { // none set currently
            setSoTimeout(connectTimeout); // use connect timeout to ensure we don't block forever
        }
        __getReply(false); // untagged response
        if (tmo <= 0) {
            setSoTimeout(tmo); // restore the original value
        }
        setState(IMAPState.NOT_AUTH_STATE);
    }

    /**
     * Sets IMAP client state.  This must be one of the
     * <code>_STATE</code> constants.
     * <p>
     * @param state  The new state.
     */
    protected void setState(IMAP.IMAPState state)
    {
        __state = state;
    }


    /**
     * Returns the current IMAP client state.
     * <p>
     * @return The current IMAP client state.
     */
    public IMAP.IMAPState getState()
    {
        return __state;
    }

    /**
     * Disconnects the client from the server, and sets the state to
     * <code> DISCONNECTED_STATE </code>.  The reply text information
     * from the last issued command is voided to allow garbage collection
     * of the memory used to store that information.
     * <p>
     * @exception IOException  If there is an error in disconnecting.
     */
    @Override
    public void disconnect() throws IOException
    {
        super.disconnect();
        _reader = null;
        __writer = null;
        _replyLines.clear();
        setState(IMAPState.DISCONNECTED_STATE);
    }


    /**
     * Sends a command an arguments to the server and returns the reply code.
     * <p>
     * @param commandID The ID (tag) of the command.
     * @param command  The IMAP command to send.
     * @param args     The command arguments.
     * @return  The server reply code (either IMAPReply.OK, IMAPReply.NO or IMAPReply.BAD).
     */
    private int sendCommandWithID(String commandID, String command, String args) throws IOException
    {
        StringBuilder __commandBuffer = new StringBuilder();
        if (commandID != null)
        {
            __commandBuffer.append(commandID);
            __commandBuffer.append(' ');
        }
        __commandBuffer.append(command);

        if (args != null)
        {
            __commandBuffer.append(' ');
            __commandBuffer.append(args);
        }
        __commandBuffer.append(SocketClient.NETASCII_EOL);

        String message = __commandBuffer.toString();
        __writer.write(message);
        __writer.flush();

        fireCommandSent(command, message);

        __getReply();
        return _replyCode;
    }

    /**
     * Sends a command an arguments to the server and returns the reply code.
     * <p>
     * @param command  The IMAP command to send.
     * @param args     The command arguments.
     * @return  The server reply code (see IMAPReply).
     */
    public int sendCommand(String command, String args) throws IOException
    {
        return sendCommandWithID(generateCommandID(), command, args);
    }

    /**
     * Sends a command with no arguments to the server and returns the
     * reply code.
     * <p>
     * @param command  The IMAP command to send.
     * @return  The server reply code (see IMAPReply).
     */
    public int sendCommand(String command) throws IOException
    {
        return sendCommand(command, null);
    }

    /**
     * Sends a command and arguments to the server and returns the reply code.
     * <p>
     * @param command  The IMAP command to send
     *                  (one of the IMAPCommand constants).
     * @param args     The command arguments.
     * @return  The server reply code (see IMAPReply).
     */
    public int sendCommand(IMAPCommand command, String args) throws IOException
    {
        return sendCommand(command.getIMAPCommand(), args);
    }

    /**
     * Sends a command and arguments to the server and return whether successful.
     * <p>
     * @param command  The IMAP command to send
     *                  (one of the IMAPCommand constants).
     * @param args     The command arguments.
     * @return  {@code true} if the command was successful
     */
    public boolean doCommand(IMAPCommand command, String args) throws IOException
    {
        return IMAPReply.isSuccess(sendCommand(command, args));
    }

    /**
     * Sends a command with no arguments to the server and returns the
     * reply code.
     *
     * @param command  The IMAP command to send
     *                  (one of the IMAPCommand constants).
     * @return  The server reply code (see IMAPReply).
    **/
    public int sendCommand(IMAPCommand command) throws IOException
    {
        return sendCommand(command, null);
    }

    /**
     * Sends a command to the server and return whether successful.
     *
     * @param command  The IMAP command to send
     *                  (one of the IMAPCommand constants).
     * @return  {@code true} if the command was successful
     */
    public boolean doCommand(IMAPCommand command) throws IOException
    {
        return IMAPReply.isSuccess(sendCommand(command));
    }

    /**
     * Sends data to the server and returns the reply code.
     * <p>
     * @param command  The IMAP command to send.
     * @return  The server reply code (see IMAPReply).
     */
    public int sendData(String command) throws IOException
    {
        return sendCommandWithID(null, command, null);
    }

    /**
     * Returns an array of lines received as a reply to the last command
     * sent to the server.  The lines have end of lines truncated.
     * @return The last server response.
     */
    public String[] getReplyStrings()
    {
        return _replyLines.toArray(new String[_replyLines.size()]);
    }

    /**
     * Returns the reply to the last command sent to the server.
     * The value is a single string containing all the reply lines including
     * newlines.
     * <p>
     * @return The last server response.
     */
    public String getReplyString()
    {
        StringBuilder buffer = new StringBuilder(256);
        for (String s : _replyLines)
        {
            buffer.append(s);
            buffer.append(SocketClient.NETASCII_EOL);
        }

        return buffer.toString();
    }

    /**
     * Generates a new command ID (tag) for a command.
     * @return a new command ID (tag) for an IMAP command.
     */
    protected String generateCommandID()
    {
        String res = new String (_initialID);
        // "increase" the ID for the next call
        boolean carry = true; // want to increment initially
        for (int i = _initialID.length-1; carry && i>=0; i--)
        {
            if (_initialID[i] == 'Z')
            {
                _initialID[i] = 'A';
            }
            else
            {
                _initialID[i]++;
                carry = false; // did not wrap round
            }
        }
        return res;
    }
}
/* kate: indent-width 4; replace-tabs on; */
