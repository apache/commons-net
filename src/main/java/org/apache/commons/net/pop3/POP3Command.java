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

/***
 * POP3Command stores POP3 command code constants.
 * <p>
 * <p>
 ***/

public final class POP3Command
{
    /*** Send user name. ***/
    public static final int USER = 0;
    /*** Send password. ***/
    public static final int PASS = 1;
    /*** Quit session. ***/
    public static final int QUIT = 2;
    /*** Get status. ***/
    public static final int STAT = 3;
    /*** List message(s). ***/
    public static final int LIST = 4;
    /*** Retrieve message(s). ***/
    public static final int RETR = 5;
    /*** Delete message(s). ***/
    public static final int DELE = 6;
    /*** No operation.  Used as a session keepalive. ***/
    public static final int NOOP = 7;
    /*** Reset session. ***/
    public static final int RSET = 8;
    /*** Authorization. ***/
    public static final int APOP = 9;
    /*** Retrieve top number lines from message. ***/
    public static final int TOP = 10;
    /*** List unique message identifier(s). ***/
    public static final int UIDL = 11;
    /**
     * The capabilities command.
     * @since 3.0
     */
    public static final int CAPA = 12;
    /**
     * Authentication
     * @since 3.0
     */
    public static final int AUTH = 13;

    private static final int _NEXT_ = AUTH + 1; // update as necessary when adding new entries

    static final String[] _commands = {
                                          "USER", "PASS", "QUIT", "STAT", "LIST", "RETR", "DELE", "NOOP", "RSET",
                                          "APOP", "TOP", "UIDL", "CAPA", "AUTH",
                                      };

    static {
        if (_commands.length != _NEXT_) {
            throw new RuntimeException("Error in array definition");
        }
    }

    // Cannot be instantiated.
    private POP3Command()
    {}

    /***
     * Get the POP3 protocol string command corresponding to a command code.
     * <p>
     * @return The POP3 protocol string command corresponding to a command code.
     ***/
    public static final String getCommand(int command)
    {
        return _commands[command];
    }
}
