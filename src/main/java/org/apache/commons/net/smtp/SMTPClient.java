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

package org.apache.commons.net.smtp;

import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;

import org.apache.commons.net.io.DotTerminatedMessageWriter;

/**
 * SMTPClient encapsulates all the functionality necessary to send files through an SMTP server. This class takes care of all low level details of interacting
 * with an SMTP server and provides a convenient higher level interface. As with all classes derived from {@link org.apache.commons.net.SocketClient}, you must
 * first connect to the server with {@link org.apache.commons.net.SocketClient#connect connect} before doing anything, and finally
 * {@link org.apache.commons.net.SocketClient#disconnect disconnect} after you're completely finished interacting with the server. Then you need to check the
 * SMTP reply code to see if the connection was successful. For example:
 *
 * <pre>
 *    try {
 *      int reply;
 *      client.connect("mail.foobar.com");
 *      System.out.print(client.getReplyString());
 *
 *      // After connection attempt, you should check the reply code to verify
 *      // success.
 *      reply = client.getReplyCode();
 *
 *      if (!SMTPReply.isPositiveCompletion(reply)) {
 *        client.disconnect();
 *        System.err.println("SMTP server refused connection.");
 *        System.exit(1);
 *      }
 *
 *      // Do useful stuff here.
 *      ...
 *    } catch (IOException e) {
 *      if (client.isConnected()) {
 *        try {
 *          client.disconnect();
 *        } catch (IOException f) {
 *          // do nothing
 *        }
 *      }
 *      System.err.println("Could not connect to server.");
 *      e.printStackTrace();
 *      System.exit(1);
 *    }
 * </pre>
 * <p>
 * Immediately after connecting is the only real time you need to check the reply code (because connect is of type void). The convention for all the SMTP
 * command methods in SMTPClient is such that they either return a boolean value or some other value. The boolean methods return true on a successful completion
 * reply from the SMTP server and false on a reply resulting in an error condition or failure. The methods returning a value other than boolean return a value
 * containing the higher level data produced by the SMTP command, or null if a reply resulted in an error condition or failure. If you want to access the exact
 * SMTP reply code causing a success or failure, you must call {@link org.apache.commons.net.smtp.SMTP#getReplyCode getReplyCode} after a success or failure.
 * </p>
 * <p>
 * You should keep in mind that the SMTP server may choose to prematurely close a connection for various reasons. The SMTPClient class will detect a premature
 * SMTP server connection closing when it receives a {@link org.apache.commons.net.smtp.SMTPReply#SERVICE_NOT_AVAILABLE SMTPReply.SERVICE_NOT_AVAILABLE}
 * response to a command. When that occurs, the method encountering that reply will throw an {@link org.apache.commons.net.smtp.SMTPConnectionClosedException}.
 * {@code SMTPConnectionClosedException} is a subclass of {@code IOException} and therefore need not be caught separately, but if you are going to
 * catch it separately, its catch block must appear before the more general {@code IOException} catch block. When you encounter an
 * {@link org.apache.commons.net.smtp.SMTPConnectionClosedException} , you must disconnect the connection with {@link #disconnect disconnect()} to properly
 * clean up the system resources used by SMTPClient. Before disconnecting, you may check the last reply code and text with
 * {@link org.apache.commons.net.smtp.SMTP#getReplyCode getReplyCode}, {@link org.apache.commons.net.smtp.SMTP#getReplyString getReplyString}, and
 * {@link org.apache.commons.net.smtp.SMTP#getReplyStrings getReplyStrings}.
 * </p>
 * <p>
 * Rather than list it separately for each method, we mention here that every method communicating with the server and throwing an IOException can also throw a
 * {@link org.apache.commons.net.MalformedServerReplyException} , which is a subclass of IOException. A MalformedServerReplyException will be thrown when the
 * reply received from the server deviates enough from the protocol specification that it cannot be interpreted in a useful manner despite attempts to be as
 * lenient as possible.
 * </p>
 *
 * @see SMTP
 * @see SimpleSMTPHeader
 * @see RelayPath
 * @see SMTPConnectionClosedException
 * @see org.apache.commons.net.MalformedServerReplyException
 */
public class SMTPClient extends SMTP {

    /**
     * Default SMTPClient constructor. Creates a new SMTPClient instance.
     */
    public SMTPClient() {
    }

    /**
     * Overloaded constructor that takes an encoding specification
     *
     * @param encoding The encoding to use
     * @since 2.0
     */
    public SMTPClient(final String encoding) {
        super(encoding);
    }

    /**
     * Adds a recipient for a message using the SMTP RCPT command, specifying a forward relay path. The sender must be set first before any recipients may be
     * specified, otherwise the mail server will reject your commands.
     *
     * @param path The forward relay path pointing to the recipient.
     * @return True if successfully completed, false if not.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean addRecipient(final RelayPath path) throws IOException {
        return SMTPReply.isPositiveCompletion(rcpt(path.toString()));
    }

    /**
     * Adds a recipient for a message using the SMTP RCPT command, the recipient's email address. The sender must be set first before any recipients may be
     * specified, otherwise the mail server will reject your commands.
     *
     * @param address The recipient's email address.
     * @return True if successfully completed, false if not.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean addRecipient(final String address) throws IOException {
        return SMTPReply.isPositiveCompletion(rcpt("<" + address + ">"));
    }

    /**
     * At least one SMTPClient method ({@link #sendMessageData sendMessageData}) does not complete the entire sequence of SMTP commands to complete a
     * transaction. These types of commands require some action by the programmer after the reception of a positive intermediate command. After the programmer's
     * code completes its actions, it must call this method to receive the completion reply from the server and verify the success of the entire transaction.
     * <p>
     * For example,
     * </p>
     * <pre>
     * writer = client.sendMessageData();
     * if (writer == null) // failure
     *     return false;
     * header = new SimpleSMTPHeader("foobar@foo.com", "foo@foobar.com", "Re: Foo");
     * writer.write(header.toString());
     * writer.write("This is just a test");
     * writer.close();
     * if (!client.completePendingCommand()) // failure
     *     return false;
     * </pre>
     *
     * @return True if successfully completed, false if not.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean completePendingCommand() throws IOException {
        return SMTPReply.isPositiveCompletion(getReply());
    }

    /**
     * Fetches the system help information from the server and returns the full string.
     *
     * @return The system help string obtained from the server. null if the information could not be obtained.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public String listHelp() throws IOException {
        if (SMTPReply.isPositiveCompletion(help())) {
            return getReplyString();
        }
        return null;
    }

    /**
     * Fetches the help information for a given command from the server and returns the full string.
     *
     * @param command The command on which to ask for help.
     * @return The command help string obtained from the server. null if the information could not be obtained.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public String listHelp(final String command) throws IOException {
        if (SMTPReply.isPositiveCompletion(help(command))) {
            return getReplyString();
        }
        return null;
    }

    /**
     * Login to the SMTP server by sending the {@code HELO} command with the client hostname as an argument.
     * Before performing any mail commands, you must first log in.
     *
     * @return True if successfully completed, false if not.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean login() throws IOException {
        final InetAddress host = getLocalAddress();
        final String name = host.getHostName();
        if (name == null) {
            return false;
        }
        return SMTPReply.isPositiveCompletion(helo(name));
    }

    /**
     * Login to the SMTP server by sending the {@code HELO} command with the given hostname as an argument.
     * Before performing any mail commands, you must first log in.
     *
     * @param hostname The hostname with which to greet the SMTP server.
     * @return True if successfully completed, false if not.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean login(final String hostname) throws IOException {
        return SMTPReply.isPositiveCompletion(helo(hostname));
    }

    /**
     * Logout of the SMTP server by sending the QUIT command.
     *
     * @return True if successfully completed, false if not.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean logout() throws IOException {
        return SMTPReply.isPositiveCompletion(quit());
    }

    /**
     * Aborts the current mail transaction, resetting all server stored sender, recipient, and mail data, cleaning all buffers and tables.
     *
     * @return True if successfully completed, false if not.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean reset() throws IOException {
        return SMTPReply.isPositiveCompletion(rset());
    }

    /**
     * Sends the SMTP DATA command in preparation to send an email message. This method returns a DotTerminatedMessageWriter instance to which the message can
     * be written. Null is returned if the DATA command fails.
     * <p>
     * You must not issue any commands to the SMTP server (i.e., call any (other methods) until you finish writing to the returned Writer instance and close it.
     * The SMTP protocol uses the same stream for issuing commands as it does for returning results. Therefore, the returned Writer actually writes directly to
     * the SMTP connection. After you close the writer, you can execute new commands. If you do not follow these requirements your program will not work
     * properly.
     * </p>
     * <p>
     * You can use the provided {@link org.apache.commons.net.smtp.SimpleSMTPHeader} class to construct a bare minimum header. To construct more complicated
     * headers you should refer to RFC 5322. When the Java Mail API is finalized, you will be able to use it to compose fully compliant Internet text messages.
     * The DotTerminatedMessageWriter takes care of doubling line-leading dots and ending the message with a single dot upon closing, so all you have to worry
     * about is writing the header and the message.
     * </p>
     * <p>
     * Upon closing the returned Writer, you need to call {@link #completePendingCommand completePendingCommand()} to finalize the transaction and verify its
     * success or failure from the server reply.
     * </p>
     *
     * @return A DotTerminatedMessageWriter to which the message (including header) can be written. Returns null if the command fails.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     * @see #sendShortMessageData(String)
     */
    public Writer sendMessageData() throws IOException {
        if (!SMTPReply.isPositiveIntermediate(data())) {
            return null;
        }
        return new DotTerminatedMessageWriter(writer);
    }

    /**
     * Sends a NOOP command to the SMTP server. This is useful for preventing server timeouts.
     *
     * @return True if successfully completed, false if not.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean sendNoOp() throws IOException {
        return SMTPReply.isPositiveCompletion(noop());
    }

    /**
     * Sends a short messages. This method fetches the Writer returned by {@link #sendMessageData sendMessageData()} and writes the
     * specified String to it. After writing the message, this method calls {@link #completePendingCommand completePendingCommand()} to finalize the
     * transaction and returns its success or failure.
     *
     * @param message The short email message to send. This must include the headers and the body, but not the trailing "."
     * @return True if successfully completed, false if not.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean sendShortMessageData(final String message) throws IOException {
        try (Writer writer = sendMessageData()) {
            if (writer == null) {
                return false;
            }
            writer.write(message);
        }
        return completePendingCommand();
    }

    /**
     * Sends a short email without having to explicitly set the sender and recipient(s). This method sets the sender and recipient
     * using {@link #setSender setSender} and {@link #addRecipient addRecipient}, and then sends the message using {@link #sendShortMessageData
     * sendShortMessageData }.
     *
     * @param sender    The email address of the sender.
     * @param recipient The email address of the recipient.
     * @param message   The short email message to send. This must include the headers and the body, but not the trailing "."
     * @return True if successfully completed, false if not.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean sendSimpleMessage(final String sender, final String recipient, final String message) throws IOException {
        if (!setSender(sender)) {
            return false;
        }
        if (!addRecipient(recipient)) {
            return false;
        }
        return sendShortMessageData(message);
    }

    /**
     * Sends a short email without having to explicitly set the sender and recipient(s). This method sets the sender and recipients
     * using {@link #setSender(String) setSender} and {@link #addRecipient(String) addRecipient}, and then sends the message using
     * {@link #sendShortMessageData(String) sendShortMessageData}.
     * <p>
     * Note that the method ignores failures when calling {@link #addRecipient(String) addRecipient} so long as at least one call succeeds. If no recipients can
     * be successfully added then the method will fail (and does not attempt to send the message)
     * </p>
     *
     * @param sender     The email address of the sender.
     * @param recipients An array of recipient email addresses.
     * @param message    The short email message to send. This must include the headers and the body, but not the trailing "."
     * @return True if successfully completed, false if not.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean sendSimpleMessage(final String sender, final String[] recipients, final String message) throws IOException {
        if (!setSender(sender)) {
            return false;
        }
        boolean oneSuccess = false;
        for (final String recipient : recipients) {
            if (addRecipient(recipient)) {
                oneSuccess = true;
            }
        }
        if (!oneSuccess) {
            return false;
        }
        return sendShortMessageData(message);
    }

    /**
     * Sets the sender of a message using the SMTP MAIL command, specifying a reverse relay path. The sender must be set first before any recipients may be
     * specified, otherwise the mail server will reject your commands.
     *
     * @param path The reverse relay path pointing back to the sender.
     * @return True if successfully completed, false if not.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean setSender(final RelayPath path) throws IOException {
        return SMTPReply.isPositiveCompletion(mail(path.toString()));
    }

    /**
     * Sets the sender of a message using the SMTP MAIL command, specifying the sender's email address. The sender must be set first before any recipients may
     * be specified, otherwise the mail server will reject your commands.
     *
     * @param address The sender's email address.
     * @return True if successfully completed, false if not.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean setSender(final String address) throws IOException {
        return SMTPReply.isPositiveCompletion(mail("<" + address + ">"));
    }

    /**
     * Verifies that a user or email address is valid, i.e., that mail can be delivered to that mailbox on the server.
     *
     * @param user The user name or email address to validate.
     * @return True if the user name is valid, false if not.
     * @throws SMTPConnectionClosedException If the SMTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                       causing the server to send SMTP reply code 421. This exception may be caught either as an IOException or
     *                                       independently as itself.
     * @throws IOException                   If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean verify(final String user) throws IOException {
        final int result = vrfy(user);
        return result == SMTPReply.ACTION_OK || result == SMTPReply.USER_NOT_LOCAL_WILL_FORWARD;
    }

}
