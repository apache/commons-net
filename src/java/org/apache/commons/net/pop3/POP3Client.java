package org.apache.commons.net.pop3;

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

import java.io.IOException;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import org.apache.commons.net.io.DotTerminatedMessageReader;

/***
 * The POP3Client class implements the client side of the Internet POP3
 * Protocol defined in RFC 1939.  All commands are supported, including
 * the APOP command which requires MD5 encryption.  See RFC 1939 for
 * more details on the POP3 protocol.
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
 * @see POP3MessageInfo
 * @see org.apache.commons.net.io.DotTerminatedMessageReader
 * @see org.apache.commons.net.MalformedServerReplyException
 ***/

public class POP3Client extends POP3
{

    private static POP3MessageInfo __parseStatus(String line)
    {
        int num, size;
        StringTokenizer tokenizer;

        tokenizer = new StringTokenizer(line);

        if (!tokenizer.hasMoreElements())
            return null;

        num = size = 0;

        try
        {
            num = Integer.parseInt(tokenizer.nextToken());

            if (!tokenizer.hasMoreElements())
                return null;

            size = Integer.parseInt(tokenizer.nextToken());
        }
        catch (NumberFormatException e)
        {
            return null;
        }

        return new POP3MessageInfo(num, size);
    }

    private static POP3MessageInfo __parseUID(String line)
    {
        int num;
        StringTokenizer tokenizer;

        tokenizer = new StringTokenizer(line);

        if (!tokenizer.hasMoreElements())
            return null;

        num = 0;

        try
        {
            num = Integer.parseInt(tokenizer.nextToken());

            if (!tokenizer.hasMoreElements())
                return null;

            line = tokenizer.nextToken();
        }
        catch (NumberFormatException e)
        {
            return null;
        }

        return new POP3MessageInfo(num, line);
    }

    /***
     * Login to the POP3 server with the given username and password.  You
     * must first connect to the server with
     * <a href="org.apache.commons.net.SocketClient.html#connect"> connect </a>
     * before attempting to login.  A login attempt is only valid if
     * the client is in the
     * <a href="org.apache.commons.net.pop3.POP3.html#AUTHORIZATION_STATE">
     * AUTHORIZATION_STATE </a>.  After logging in, the client enters the
     * <a href="org.apache.commons.net.pop3.POP3.html#TRANSACTION_STATE">
     * TRANSACTION_STATE </a>.
     * <p>
     * @param username  The account name being logged in to.
     * @param password  The plain text password of the account.
     * @return True if the login attempt was successful, false if not.
     * @exception IOException If a network I/O error occurs in the process of
     *            logging in.
     ***/
    public boolean login(String username, String password) throws IOException
    {
        if (getState() != AUTHORIZATION_STATE)
            return false;

        if (sendCommand(POP3Command.USER, username) != POP3Reply.OK)
            return false;

        if (sendCommand(POP3Command.PASS, password) != POP3Reply.OK)
            return false;

        setState(TRANSACTION_STATE);

        return true;
    }


    /***
     * Login to the POP3 server with the given username and authentication
     * information.  Use this method when connecting to a server requiring
     * authentication using the APOP command.  Because the timestamp
     * produced in the greeting banner varies from server to server, it is
     * not possible to consistently extract the information.  Therefore,
     * after connecting to the server, you must call
     * <a href="org.apache.commons.net.pop3.POP3.html#getReplyString">
     * getReplyString </a> and parse out the timestamp information yourself.
     * <p>
     * You must first connect to the server with
     * <a href="org.apache.commons.net.SocketClient.html#connect"> connect </a>
     * before attempting to login.  A login attempt is only valid if
     * the client is in the
     * <a href="org.apache.commons.net.pop3.POP3.html#AUTHORIZATION_STATE">
     * AUTHORIZATION_STATE </a>.  After logging in, the client enters the
     * <a href="org.apache.commons.net.pop3.POP3.html#TRANSACTION_STATE">
     * TRANSACTION_STATE </a>.  After connecting, you must parse out the
     * server specific information to use as a timestamp, and pass that
     * information to this method.  The secret is a shared secret known
     * to you and the server.  See RFC 1939 for more details regarding
     * the APOP command.
     * <p>
     * @param username  The account name being logged in to.
     * @param timestamp  The timestamp string to combine with the secret.
     * @param secret  The shared secret which produces the MD5 digest when
     *        combined with the timestamp.
     * @return True if the login attempt was successful, false if not.
     * @exception IOException If a network I/O error occurs in the process of
     *            logging in.
     * @exception NoSuchAlgorithmException If the MD5 encryption algorithm 
     *      cannot be instantiated by the Java runtime system.
     ***/
    public boolean login(String username, String timestamp, String secret)
    throws IOException, NoSuchAlgorithmException
    {
        int i;
        byte[] digest;
        StringBuffer buffer, digestBuffer;
        MessageDigest md5;

        if (getState() != AUTHORIZATION_STATE)
            return false;

        md5 = MessageDigest.getInstance("MD5");
        timestamp += secret;
        digest = md5.digest(timestamp.getBytes());
        digestBuffer = new StringBuffer(128);

        for (i = 0; i < digest.length; i++)
            digestBuffer.append(Integer.toHexString(digest[i] & 0xff));

        buffer = new StringBuffer(256);
        buffer.append(username);
        buffer.append(' ');
        buffer.append(digestBuffer.toString());

        if (sendCommand(POP3Command.APOP, buffer.toString()) != POP3Reply.OK)
            return false;

        setState(TRANSACTION_STATE);

        return true;
    }


    /***
     * Logout of the POP3 server.  To fully disconnect from the server
     * you must call
     * <a href="org.apache.commons.net.pop3.POP3.html#disconnect"> disconnect </a>.
     * A logout attempt is valid in any state.  If
     * the client is in the
     * <a href="org.apache.commons.net.pop3.POP3.html#TRANSACTION_STATE">
     * TRANSACTION_STATE </a>, it enters the
     * <a href="org.apache.commons.net.pop3.POP3.html#UPDATE_STATE">
     * UPDATE_STATE </a> on a successful logout.
     * <p>
     * @return True if the logout attempt was successful, false if not.
     * @exception IOException If a network I/O error occurs in the process
     *           of logging out.
     ***/
    public boolean logout() throws IOException
    {
        if (getState() == TRANSACTION_STATE)
            setState(UPDATE_STATE);
        sendCommand(POP3Command.QUIT);
        return (_replyCode == POP3Reply.OK);
    }


    /***
     * Send a NOOP command to the POP3 server.  This is useful for keeping
     * a connection alive since most POP3 servers will timeout after 10
     * minutes of inactivity.  A noop attempt will only succeed if
     * the client is in the
     * <a href="org.apache.commons.net.pop3.POP3.html#TRANSACTION_STATE">
     * TRANSACTION_STATE </a>.
     * <p>
     * @return True if the noop attempt was successful, false if not.
     * @exception IOException If a network I/O error occurs in the process of
     *        sending the NOOP command.
     ***/
    public boolean noop() throws IOException
    {
        if (getState() == TRANSACTION_STATE)
            return (sendCommand(POP3Command.NOOP) == POP3Reply.OK);
        return false;
    }


    /***
     * Delete a message from the POP3 server.  The message is only marked
     * for deletion by the server.  If you decide to unmark the message, you
     * must issuse a <a href="#reset"> reset </a> command.  Messages marked
     * for deletion are only deleted by the server on
     * <a href="#logout"> logout </a>.
     * A delete attempt can only succeed if the client is in the
     * <a href="org.apache.commons.net.pop3.POP3.html#TRANSACTION_STATE">
     * TRANSACTION_STATE </a>.
     * <p>
     * @param messageId  The message number to delete.
     * @return True if the deletion attempt was successful, false if not.
     * @exception IOException If a network I/O error occurs in the process of
     *           sending the delete command.
     ***/
    public boolean deleteMessage(int messageId) throws IOException
    {
        if (getState() == TRANSACTION_STATE)
            return (sendCommand(POP3Command.DELE, Integer.toString(messageId))
                    == POP3Reply.OK);
        return false;
    }


    /***
     * Reset the POP3 session.  This is useful for undoing any message
     * deletions that may have been performed.  A reset attempt can only
     * succeed if the client is in the
     * <a href="org.apache.commons.net.pop3.POP3.html#TRANSACTION_STATE">
     * TRANSACTION_STATE </a>.
     * <p>
     * @return True if the reset attempt was successful, false if not.
     * @exception IOException If a network I/O error occurs in the process of
     *      sending the reset command.
     ***/
    public boolean reset() throws IOException
    {
        if (getState() == TRANSACTION_STATE)
            return (sendCommand(POP3Command.RSET) == POP3Reply.OK);
        return false;
    }

    /***
     * Get the mailbox status.  A status attempt can only
     * succeed if the client is in the
     * <a href="org.apache.commons.net.pop3.POP3.html#TRANSACTION_STATE">
     * TRANSACTION_STATE </a>.  Returns a POP3MessageInfo instance
     * containing the number of messages in the mailbox and the total
     * size of the messages in bytes.  Returns null if the status the 
     * attempt fails.
     * <p>
     * @return A POP3MessageInfo instance containing the number of
     *         messages in the mailbox and the total size of the messages
     *         in bytes.  Returns null if the status the attempt fails.
     * @exception IOException If a network I/O error occurs in the process of
     *       sending the status command.
     ***/
    public POP3MessageInfo status() throws IOException
    {
        if (getState() != TRANSACTION_STATE)
            return null;
        if (sendCommand(POP3Command.STAT) != POP3Reply.OK)
            return null;
        return __parseStatus(_lastReplyLine.substring(3));
    }


    /***
     * List an individual message.  A list attempt can only
     * succeed if the client is in the
     * <a href="org.apache.commons.net.pop3.POP3.html#TRANSACTION_STATE">
     * TRANSACTION_STATE </a>.  Returns a POP3MessageInfo instance
     * containing the number of the listed message and the
     * size of the message in bytes.  Returns null if the list
     * attempt fails (e.g., if the specified message number does
     * not exist).
     * <p>
     * @param messageId  The number of the message list.
     * @return A POP3MessageInfo instance containing the number of the
     *         listed message and the size of the message in bytes.  Returns
     *         null if the list attempt fails.
     * @exception IOException If a network I/O error occurs in the process of
     *         sending the list command.
     ***/
    public POP3MessageInfo listMessage(int messageId) throws IOException
    {
        if (getState() != TRANSACTION_STATE)
            return null;
        if (sendCommand(POP3Command.LIST, Integer.toString(messageId))
                != POP3Reply.OK)
            return null;
        return __parseStatus(_lastReplyLine.substring(3));
    }


    /***
     * List all messages.  A list attempt can only
     * succeed if the client is in the
     * <a href="org.apache.commons.net.pop3.POP3.html#TRANSACTION_STATE">
     * TRANSACTION_STATE </a>.  Returns an array of POP3MessageInfo instances,
     * each containing the number of a message and its size in bytes.
     * If there are no messages, this method returns a zero length array.
     * If the list attempt fails, it returns null.
     * <p>
     * @return An array of POP3MessageInfo instances representing all messages
     * in the order they appear in the mailbox,
     * each containing the number of a message and its size in bytes.
     * If there are no messages, this method returns a zero length array.
     * If the list attempt fails, it returns null.
     * @exception IOException If a network I/O error occurs in the process of
     *     sending the list command.
     ***/
    public POP3MessageInfo[] listMessages() throws IOException
    {
        POP3MessageInfo[] messages;
        Enumeration enum;
        int line;

        if (getState() != TRANSACTION_STATE)
            return null;
        if (sendCommand(POP3Command.LIST) != POP3Reply.OK)
            return null;
        getAdditionalReply();

        // This could be a zero length array if no messages present
        messages = new POP3MessageInfo[_replyLines.size() - 2];
        enum = _replyLines.elements();

        // Skip first line
        enum.nextElement();

        // Fetch lines.
        for (line = 0; line < messages.length; line++)
            messages[line] = __parseStatus((String)enum.nextElement());

        return messages;
    }

    /***
     * List the unique identifier for a message.  A list attempt can only
     * succeed if the client is in the
     * <a href="org.apache.commons.net.pop3.POP3.html#TRANSACTION_STATE">
     * TRANSACTION_STATE </a>.  Returns a POP3MessageInfo instance
     * containing the number of the listed message and the
     * unique identifier for that message.  Returns null if the list
     * attempt fails  (e.g., if the specified message number does
     * not exist).
     * <p>
     * @param messageId  The number of the message list.
     * @return A POP3MessageInfo instance containing the number of the
     *         listed message and the unique identifier for that message.
     *         Returns null if the list attempt fails.
     * @exception IOException If a network I/O error occurs in the process of
     *        sending the list unique identifier command.
     ***/
    public POP3MessageInfo listUniqueIdentifier(int messageId)
    throws IOException
    {
        if (getState() != TRANSACTION_STATE)
            return null;
        if (sendCommand(POP3Command.UIDL, Integer.toString(messageId))
                != POP3Reply.OK)
            return null;
        return __parseUID(_lastReplyLine.substring(3));
    }


    /***
     * List the unique identifiers for all messages.  A list attempt can only
     * succeed if the client is in the
     * <a href="org.apache.commons.net.pop3.POP3.html#TRANSACTION_STATE">
     * TRANSACTION_STATE </a>.  Returns an array of POP3MessageInfo instances,
     * each containing the number of a message and its unique identifier.
     * If there are no messages, this method returns a zero length array.
     * If the list attempt fails, it returns null.
     * <p>
     * @return An array of POP3MessageInfo instances representing all messages
     * in the order they appear in the mailbox,
     * each containing the number of a message and its unique identifier
     * If there are no messages, this method returns a zero length array.
     * If the list attempt fails, it returns null.
     * @exception IOException If a network I/O error occurs in the process of
     *     sending the list unique identifier command.
     ***/
    public POP3MessageInfo[] listUniqueIdentifiers() throws IOException
    {
        POP3MessageInfo[] messages;
        Enumeration enum;
        int line;

        if (getState() != TRANSACTION_STATE)
            return null;
        if (sendCommand(POP3Command.UIDL) != POP3Reply.OK)
            return null;
        getAdditionalReply();

        // This could be a zero length array if no messages present
        messages = new POP3MessageInfo[_replyLines.size() - 2];
        enum = _replyLines.elements();

        // Skip first line
        enum.nextElement();

        // Fetch lines.
        for (line = 0; line < messages.length; line++)
            messages[line] = __parseUID((String)enum.nextElement());

        return messages;
    }


    /***
     * Retrieve a message from the POP3 server.  A retrieve message attempt
     * can only succeed if the client is in the
     * <a href="org.apache.commons.net.pop3.POP3.html#TRANSACTION_STATE">
     * TRANSACTION_STATE </a>.  Returns a DotTerminatedMessageReader instance
     * from which the entire message can be read.
     * Returns null if the retrieval attempt fails  (e.g., if the specified
     * message number does not exist).
     * <p>
     * You must not issue any commands to the POP3 server (i.e., call any
     * other methods) until you finish reading the message from the
     * returned Reader instance.
     * The POP3 protocol uses the same stream for issuing commands as it does
     * for returning results.  Therefore the returned Reader actually reads
     * directly from the POP3 connection.  After the end of message has been
     * reached, new commands can be executed and their replies read.  If
     * you do not follow these requirements, your program will not work
     * properly.
     * <p>
     * @param messageId  The number of the message to fetch.
     * @return A DotTerminatedMessageReader instance
     * from which the entire message can be read.
     * Returns null if the retrieval attempt fails  (e.g., if the specified
     * message number does not exist).
     * @exception IOException If a network I/O error occurs in the process of
     *        sending the retrieve message command.
     ***/
    public Reader retrieveMessage(int messageId) throws IOException
    {
        if (getState() != TRANSACTION_STATE)
            return null;
        if (sendCommand(POP3Command.RETR, Integer.toString(messageId))
                != POP3Reply.OK)
            return null;

        return new DotTerminatedMessageReader(_reader);
    }


    /***
     * Retrieve only the specified top number of lines of a message from the 
     * POP3 server.  A retrieve top lines attempt
     * can only succeed if the client is in the
     * <a href="org.apache.commons.net.pop3.POP3.html#TRANSACTION_STATE">
     * TRANSACTION_STATE </a>.  Returns a DotTerminatedMessageReader instance
     * from which the specified top number of lines of the message can be
     * read.
     * Returns null if the retrieval attempt fails  (e.g., if the specified
     * message number does not exist).
     * <p>
     * You must not issue any commands to the POP3 server (i.e., call any
     * other methods) until you finish reading the message from the returned
     * Reader instance.
     * The POP3 protocol uses the same stream for issuing commands as it does
     * for returning results.  Therefore the returned Reader actually reads
     * directly from the POP3 connection.  After the end of message has been
     * reached, new commands can be executed and their replies read.  If
     * you do not follow these requirements, your program will not work
     * properly.
     * <p>
     * @param messageId  The number of the message to fetch.
     * @param numLines  The top number of lines to fetch. This must be >= 0.
     * @return  A DotTerminatedMessageReader instance
     * from which the specified top number of lines of the message can be
     * read.
     * Returns null if the retrieval attempt fails  (e.g., if the specified
     * message number does not exist).
     * @exception IOException If a network I/O error occurs in the process of
     *       sending the top command.
     ***/
    public Reader retrieveMessageTop(int messageId, int numLines)
    throws IOException
    {
        if (numLines < 0 || getState() != TRANSACTION_STATE)
            return null;
        if (sendCommand(POP3Command.TOP, Integer.toString(messageId) + " " +
                        Integer.toString(numLines)) != POP3Reply.OK)
            return null;

        return new DotTerminatedMessageReader(_reader);
    }


}

