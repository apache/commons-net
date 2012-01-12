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

import java.io.IOException;

/**
 * The IMAPClient class provides the basic functionalities found in an
 * IMAP client.
 */
public class IMAPClient extends IMAP
{

    // --------- commands available in all states

    /**
     * Send a CAPABILITY command to the server.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs
     */
    public boolean capability() throws IOException
    {
        return doCommand (IMAPCommand.CAPABILITY);
    }

    /**
     * Send a NOOP command to the server.  This is useful for keeping
     * a connection alive since most IMAP servers will timeout after 10
     * minutes of inactivity.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean noop() throws IOException
    {
        return doCommand (IMAPCommand.NOOP);
    }

    /**
     * Send a LOGOUT command to the server.  To fully disconnect from the server
     * you must call disconnect().
     * A logout attempt is valid in any state.  If
     * the client is in the not authenticated or authenticated state, it enters the
     * logout on a successful logout.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean logout() throws IOException
    {
        return doCommand (IMAPCommand.LOGOUT);
    }

    // --------- commands available in the not-authenticated state
    // STARTTLS skipped - see IMAPSClient.
    // AUTHENTICATE skipped - see AuthenticatingIMAPClient.

    /**
     * Login to the IMAP server with the given username and password.  You
     * must first connect to the server with
     * {@link org.apache.commons.net.SocketClient#connect  connect }
     * before attempting to login.  A login attempt is only valid if
     * the client is in the NOT_AUTH_STATE.
     * After logging in, the client enters the AUTH_STATE.
     * <p>
     * @param username  The account name being logged in to.
     * @param password  The plain text password of the account.
     * @return True if the login attempt was successful, false if not.
     * @exception IOException If a network I/O error occurs in the process of
     *            logging in.
     */
    public boolean login(String username, String password) throws IOException
    {
        if (getState() != IMAP.IMAPState.NOT_AUTH_STATE)
        {
            return false;
        }

        if (!doCommand(IMAPCommand.LOGIN, username + " " + password))
        {
            return false;
        }

        setState(IMAP.IMAPState.AUTH_STATE);

        return true;
    }

    // --------- commands available in the authenticated state

    /**
     * Send a SELECT command to the server.
     * @param mailboxName The mailbox name to select.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean select(String mailboxName) throws IOException
    {
        return doCommand (IMAPCommand.SELECT, mailboxName);
    }

    /**
     * Send an EXAMINE command to the server.
     * @param mailboxName The mailbox name to examine.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean examine(String mailboxName) throws IOException
    {
        return doCommand (IMAPCommand.EXAMINE, mailboxName);
    }

    /**
     * Send a CREATE command to the server.
     * @param mailboxName The mailbox name to create.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean create(String mailboxName) throws IOException
    {
        return doCommand (IMAPCommand.CREATE, mailboxName);
    }

    /**
     * Send a DELETE command to the server.
     * @param mailboxName The mailbox name to delete.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean delete(String mailboxName) throws IOException
    {
        return doCommand (IMAPCommand.DELETE, mailboxName);
    }

    /**
     * Send a RENAME command to the server.
     * @param oldMailboxName The existing mailbox name to rename.
     * @param newMailboxName The new mailbox name.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean rename(String oldMailboxName, String newMailboxName) throws IOException
    {
        return doCommand (IMAPCommand.RENAME, oldMailboxName + " " + newMailboxName);
    }

    /**
     * Send a SUBSCRIBE command to the server.
     * @param mailboxName The mailbox name to subscribe to.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean subscribe(String mailboxName) throws IOException
    {
        return doCommand (IMAPCommand.SUBSCRIBE, mailboxName);
    }

    /**
     * Send a UNSUBSCRIBE command to the server.
     * @param mailboxName The mailbox name to unsubscribe from.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean unsubscribe(String mailboxName) throws IOException
    {
        return doCommand (IMAPCommand.UNSUBSCRIBE, mailboxName);
    }

    /**
     * Send a LIST command to the server.
     * @param refName The reference name.
     * @param mailboxName The mailbox name.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean list(String refName, String mailboxName) throws IOException
    {
        return doCommand (IMAPCommand.LIST, refName + " " + mailboxName);
    }

    /**
     * Send an LSUB command to the server.
     * @param refName The reference name.
     * @param mailboxName The mailbox name.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean lsub(String refName, String mailboxName) throws IOException
    {
        return doCommand (IMAPCommand.LSUB, refName + " " + mailboxName);
    }

    /**
     * Send a STATUS command to the server.
     * @param mailboxName The reference name.
     * @param itemNames The status data item names.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean status(String mailboxName, String[] itemNames) throws IOException
    {
        if (itemNames == null || itemNames.length < 1) {
            throw new IllegalArgumentException("STATUS command requires at least one data item name");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(mailboxName);

        sb.append(" (");
        for ( int i = 0; i < itemNames.length; i++ )
        {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(itemNames[i]);
        }
        sb.append(")");

        return doCommand (IMAPCommand.STATUS, sb.toString());
    }

    /**
     * Send an APPEND command to the server.
     * @param mailboxName The mailbox name.
     * @param flags The flag parenthesized list (optional).
     * @param datetime The date/time string (optional).
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean append(String mailboxName, String flags, String datetime) throws IOException
    {
        String args = mailboxName;
        if (flags != null) {
            args += " " + flags;
        }
        if (datetime != null) {
            if (datetime.charAt(0) == '{') {
                args += " " + datetime;
            } else {
                args += " {" + datetime + "}";
            }
        }
        return doCommand (IMAPCommand.APPEND, args);
    }

    /**
     * Send an APPEND command to the server.
     * @param mailboxName The mailbox name.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean append(String mailboxName) throws IOException
    {
        return append(mailboxName, null, null);
    }

    // --------- commands available in the selected state

    /**
     * Send a CHECK command to the server.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean check() throws IOException
    {
        return doCommand (IMAPCommand.CHECK);
    }

    /**
     * Send a CLOSE command to the server.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean close() throws IOException
    {
        return doCommand (IMAPCommand.CLOSE);
    }

    /**
     * Send an EXPUNGE command to the server.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean expunge() throws IOException
    {
        return doCommand (IMAPCommand.EXPUNGE);
    }

    /**
     * Send a SEARCH command to the server.
     * @param charset The charset (optional).
     * @param criteria The search criteria.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean search(String charset, String criteria) throws IOException
    {
        String args = "";
        if (charset != null) {
            args += "CHARSET " + charset;
        }
        args += criteria;
        return doCommand (IMAPCommand.SEARCH, args);
    }

    /**
     * Send a SEARCH command to the server.
     * @param criteria The search criteria.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean search(String criteria) throws IOException
    {
        return search(null, criteria);
    }

    /**
     * Send a FETCH command to the server.
     * @param sequenceSet The sequence set to fetch.
     * @param itemNames The item names for the FETCH command.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean fetch(String sequenceSet, String itemNames) throws IOException
    {
        return doCommand (IMAPCommand.FETCH, sequenceSet + " " + itemNames);
    }

    /**
     * Send a STORE command to the server.
     * @param sequenceSet The sequence set to store.
     * @param itemNames The item names for the STORE command.
     * @param itemValues The item values for the STORE command.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean store(String sequenceSet, String itemNames, String itemValues)
        throws IOException
    {
        return doCommand (IMAPCommand.STORE, sequenceSet + " " + itemNames + " " + itemValues);
    }

    /**
     * Send a COPY command to the server.
     * @param sequenceSet The sequence set to fetch.
     * @param mailboxName The mailbox name.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean copy(String sequenceSet, String mailboxName) throws IOException
    {
        return doCommand (IMAPCommand.COPY, sequenceSet + " " + mailboxName);
    }

    /**
     * Send a UID command to the server.
     * @param command The command for UID.
     * @param commandArgs The arguments for the command.
     * @return {@code true} if the command was successful,{@code false} if not.
     * @exception IOException If a network I/O error occurs.
     */
    public boolean uid(String command, String commandArgs) throws IOException
    {
        return doCommand (IMAPCommand.UID, command + " " + commandArgs);
    }

    /**
     * The status data items defined in RFC 3501.
     */
    public enum STATUS_DATA_ITEMS
    {
        /** The number of messages in the mailbox. */
        MESSAGES,
        /** The number of messages with the \Recent flag set. */
        RECENT,
        /** The next unique identifier value of the mailbox. */
        UIDNEXT,
        /** The unique identifier validity value of the mailbox. */
        UIDVALIDITY,
        /** The number of messages which do not have the \Seen flag set. */
        UNSEEN;
    }

    /**
     * The search criteria defined in RFC 3501.
     */
    public enum SEARCH_CRITERIA
    {
        /** All messages in the mailbox. */
        ALL,
        /** Messages with the \Answered flag set. */
        ANSWERED,
        /**
         * Messages that contain the specified string in the envelope
         * structure's BCC field.
         */
        BCC,
        /**
         * Messages whose internal date (disregarding time and timezone)
         * is earlier than the specified date.
         */
        BEFORE,
        /**
         * Messages that contain the specified string in the body of the
         * message.
         */
        BODY,
        /**
         * Messages that contain the specified string in the envelope
         * structure's CC field.
         */
        CC,
        /** Messages with the \Deleted flag set. */
        DELETED,
        /** Messages with the \Draft flag set. */
        DRAFT,
        /** Messages with the \Flagged flag set. */
        FLAGGED,
        /**
         * Messages that contain the specified string in the envelope
         * structure's FROM field.
         */
        FROM,
        /**
         * Messages that have a header with the specified field-name (as
         * defined in [RFC-2822]) and that contains the specified string
         * in the text of the header (what comes after the colon).  If the
         * string to search is zero-length, this matches all messages that
         * have a header line with the specified field-name regardless of
         * the contents.
         */
        HEADER,
        /** Messages with the specified keyword flag set. */
        KEYWORD,
        /**
         * Messages with an [RFC-2822] size larger than the specified
         * number of octets.
         */
        LARGER,
        /**
         * Messages that have the \Recent flag set but not the \Seen flag.
         * This is functionally equivalent to "(RECENT UNSEEN)".
         */
        NEW,
        /** Messages that do not match the specified search key. */
        NOT,
        /**
         * Messages that do not have the \Recent flag set.  This is
         * functionally equivalent to "NOT RECENT" (as opposed to "NOT
         * NEW").
         */
        OLD,
        /**
         * Messages whose internal date (disregarding time and timezone)
         * is within the specified date.
         */
        ON,
        /** Messages that match either search key. */
        OR,
        /** Messages that have the \Recent flag set. */
        RECENT,
        /** Messages that have the \Seen flag set. */
        SEEN,
        /**
         * Messages whose [RFC-2822] Date: header (disregarding time and
         * timezone) is earlier than the specified date.
         */
        SENTBEFORE,
        /**
         * Messages whose [RFC-2822] Date: header (disregarding time and
         * timezone) is within the specified date.
         */
        SENTON,
        /**
         * Messages whose [RFC-2822] Date: header (disregarding time and
         * timezone) is within or later than the specified date.
         */
        SENTSINCE,
        /**
         * Messages whose internal date (disregarding time and timezone)
         * is within or later than the specified date.
         */
        SINCE,
        /**
         * Messages with an [RFC-2822] size smaller than the specified
         * number of octets.
         */
        SMALLER,
        /**
         * Messages that contain the specified string in the envelope
         * structure's SUBJECT field.
         */
        SUBJECT,
        /**
         * Messages that contain the specified string in the header or
         * body of the message.
         */
        TEXT,
        /**
         * Messages that contain the specified string in the envelope
         * structure's TO field.
         */
        TO,
        /**
         * Messages with unique identifiers corresponding to the specified
         * unique identifier set.  Sequence set ranges are permitted.
         */
        UID,
        /** Messages that do not have the \Answered flag set. */
        UNANSWERED,
        /** Messages that do not have the \Deleted flag set. */
        UNDELETED,
        /** Messages that do not have the \Draft flag set. */
        UNDRAFT,
        /** Messages that do not have the \Flagged flag set. */
        UNFLAGGED,
        /** Messages that do not have the specified keyword flag set. */
        UNKEYWORD,
        /** Messages that do not have the \Seen flag set. */
        UNSEEN;
    }

    /**
     * The message data item names for the FETCH command defined in RFC 3501.
     */
    public enum FETCH_ITEM_NAMES
    {
        /** Macro equivalent to: (FLAGS INTERNALDATE RFC822.SIZE ENVELOPE). */
        ALL,
        /** Macro equivalent to: (FLAGS INTERNALDATE RFC822.SIZE). */
        FAST,
        /** Macro equivalent to: (FLAGS INTERNALDATE RFC822.SIZE ENVELOPE BODY). */
        FULL,
        /** Non-extensible form of BODYSTRUCTURE or the text of a particular body section. */
        BODY,
        /** The [MIME-IMB] body structure of the message. */
        BODYSTRUCTURE,
        /** The envelope structure of the message. */
        ENVELOPE,
        /** The flags that are set for this message. */
        FLAGS,
        /** The internal date of the message. */
        INTERNALDATE,
        /** A prefix for RFC-822 item names. */
        RFC822,
        /** The unique identifier for the message. */
        UID;
    }

}
/* kate: indent-width 4; replace-tabs on; */
