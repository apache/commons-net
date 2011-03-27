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
    CAPABILITY,
    NOOP,
    LOGOUT,
    STARTTLS,
    AUTHENTICATE,
    LOGIN,
    SELECT,
    EXAMINE,
    CREATE,
    DELETE,
    RENAME,
    SUBSCRIBE,
    UNSUBSCRIBE,
    LIST,
    LSUB,
    STATUS,
    APPEND,
    CHECK,
    CLOSE,
    EXPUNGE,
    SEARCH,
    FETCH,
    STORE,
    COPY,
    UID,
    ;
    
    private final String imapCommand;

    IMAPCommand(){
        imapCommand = name();
    }
    
    IMAPCommand(String name){
        imapCommand = name;
    }
    
    /**
     * Get the IMAP protocol string command corresponding to a command code.
     *
     * @param command the IMAPCommand whose command string is required.
     * @return The IMAP protocol string command corresponding to a command code.
     */
    public static final String getCommand(IMAPCommand command) {
        return command.imapCommand;
    }

}

/* kate: indent-width 4; replace-tabs on; */
