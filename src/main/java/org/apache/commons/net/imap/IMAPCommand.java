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
public enum IMAPCommand
{
    // These enums must either use the same name as the IMAP command
    // or must provide the correct string as the parameter.

    // Commands valid in any state:

    CAPABILITY(0),
    NOOP(0),
    LOGOUT(0),

    // Commands valid in Not Authenticated state
    STARTTLS(0),
    AUTHENTICATE(1),
    LOGIN(2),

    XOAUTH(1),

    // commands valid in authenticated state
    SELECT(1),
    EXAMINE(1),
    CREATE(1),
    DELETE(1),
    RENAME(2),
    SUBSCRIBE(1),
    UNSUBSCRIBE(1),
    LIST(2),
    LSUB(2),
    STATUS(2), // P2 = list in ()
    APPEND(2,4), // mbox [(flags)] [date-time] literal

    // commands valid in selected state (substate of authenticated)
    CHECK(0),
    CLOSE(0),
    EXPUNGE(0),
    SEARCH(1, Integer.MAX_VALUE),
    FETCH(2),
    STORE(3),
    COPY(2),
    UID(2, Integer.MAX_VALUE),
    ;

    private final String imapCommand;

    @SuppressWarnings("unused") // not yet used
    private final int minParamCount;
    @SuppressWarnings("unused") // not yet used
    private final int maxParamCount;

    IMAPCommand(){
        this(null);
    }

    IMAPCommand(String name){
        this(name, 0);
    }

    IMAPCommand(int paramCount){
        this(null, paramCount, paramCount);
   }

    IMAPCommand(int minCount, int maxCount){
        this(null, minCount, maxCount);
   }

    IMAPCommand(String name, int paramCount){
        this(name, paramCount, paramCount);
    }

    IMAPCommand(String name, int minCount, int maxCount){
        this.imapCommand = name;
        this.minParamCount = minCount;
        this.maxParamCount = maxCount;
    }

    /**
     * Get the IMAP protocol string command corresponding to a command code.
     *
     * @param command the IMAPCommand whose command string is required.
     * @return The IMAP protocol string command corresponding to a command code.
     */
    public static final String getCommand(IMAPCommand command) {
        return command.getIMAPCommand();
    }

    /**
     * Get the IMAP protocol string command for this command
     *
     * @return The IMAP protocol string command corresponding to this command
     */
    public String getIMAPCommand() {
        return imapCommand != null ? imapCommand : name();
    }

}

/* kate: indent-width 4; replace-tabs on; */
