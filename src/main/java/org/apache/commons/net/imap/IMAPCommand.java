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

/**
 * IMAPCommand stores IMAP command codes.
 */
public enum IMAPCommand {
    // These enums must either use the same name as the IMAP command
    // or must provide the correct string as the parameter.

    // Commands valid in any state:
    /**
     * Valid in any state.
     */
    CAPABILITY(0),

    /**
     * Valid in any state.
     */
    NOOP(0),

    /**
     * Valid in any state.
     */
    LOGOUT(0),

    // Commands valid in Not Authenticated state
    /**
     * Valid in Not Authenticated state
     */
    STARTTLS(0),

    /**
     * Valid in Not Authenticated state
     */
    AUTHENTICATE(1),

    /**
     * Valid in Not Authenticated state
     */
    LOGIN(2),

    XOAUTH(1),

    // commands valid in authenticated state
    /**
     * Valid in authenticated state.
     */
    SELECT(1),

    /**
     * Valid in authenticated state.
     */
    EXAMINE(1),

    /**
     * Valid in authenticated state.
     */
    CREATE(1),

    /**
     * Valid in authenticated state.
     */
    DELETE(1),

    /**
     * Valid in authenticated state.
     */
    RENAME(2),
    /**
     * Valid in authenticated state.
     */
    SUBSCRIBE(1),
    /**
     * Valid in authenticated state.
     */
    UNSUBSCRIBE(1),
    /**
     * Valid in authenticated state.
     */
    LIST(2),
    /**
     * Valid in authenticated state.
     */
    LSUB(2),
    /**
     * Valid in authenticated state.
     */
    STATUS(2), // P2 = list in ()

    /**
     * Valid in authenticated state.
     */
    APPEND(2, 4), // mbox [(flags)] [date-time] literal

    // commands valid in selected state (substate of authenticated)
    /**
     * Valid in selected state (substate of authenticated).
     */
    CHECK(0),

    /**
     * Valid in selected state (substate of authenticated).
     */
    CLOSE(0),

    /**
     * Valid in selected state (substate of authenticated).
     */
    EXPUNGE(0),

    /**
     * Valid in selected state (substate of authenticated).
     */
    SEARCH(1, Integer.MAX_VALUE),

    /**
     * Valid in selected state (substate of authenticated).
     */
    FETCH(2),

    /**
     * Valid in selected state (substate of authenticated).
     */
    STORE(3),

    /**
     * Valid in selected state (substate of authenticated).
     */
    COPY(2),

    /**
     * Valid in selected state (substate of authenticated).
     */
    UID(2, Integer.MAX_VALUE);

    /**
     * Get the IMAP protocol string command corresponding to a command code.
     *
     * @param command the {@link IMAPCommand} whose command string is required. Must not be null.
     * @return The IMAP protocol string command corresponding to a command code.
     */
    public static final String getCommand(final IMAPCommand command) {
        return command.getIMAPCommand();
    }

    private final String imapCommand;

    @SuppressWarnings("unused") // not yet used
    private final int minParamCount;

    @SuppressWarnings("unused") // not yet used
    private final int maxParamCount;

    IMAPCommand() {
        this(null);
    }

    IMAPCommand(final int paramCount) {
        this(null, paramCount, paramCount);
    }

    IMAPCommand(final int minCount, final int maxCount) {
        this(null, minCount, maxCount);
    }

    IMAPCommand(final String name) {
        this(name, 0);
    }

    IMAPCommand(final String name, final int paramCount) {
        this(name, paramCount, paramCount);
    }

    IMAPCommand(final String name, final int minCount, final int maxCount) {
        this.imapCommand = name;
        this.minParamCount = minCount;
        this.maxParamCount = maxCount;
    }

    /**
     * Gets the IMAP protocol string command for this command
     *
     * @return The IMAP protocol string command corresponding to this command
     */
    public String getIMAPCommand() {
        return imapCommand != null ? imapCommand : name();
    }

}


