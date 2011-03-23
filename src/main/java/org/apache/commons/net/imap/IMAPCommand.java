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
 * IMAPCommand stores IMAP command code constants.
 */
public class IMAPCommand
{
    /** The  command. */
    public static final int CAPABILITY = 0;
    /** The  command. */
    public static final int NOOP = 1;
    /** The  command. */
    public static final int LOGOUT = 2;
    /** The  command. */
    public static final int STARTTLS = 3;
    /** The  command. */
    public static final int AUTHENTICATE = 4;
    /** The  command. */
    public static final int LOGIN = 5;
    /** The  command. */
    public static final int SELECT = 6;
    /** The  command. */
    public static final int EXAMINE = 7;
    /** The  command. */
    public static final int CREATE = 8;
    /** The  command. */
    public static final int DELETE = 9;
    /** The  command. */
    public static final int RENAME = 10;
    /** The  command. */
    public static final int SUBSCRIBE = 11;
    /** The  command. */
    public static final int UNSUBSCRIBE = 12;
    /** The  command. */
    public static final int LIST = 13;
    /** The  command. */
    public static final int LSUB = 14;
    /** The  command. */
    public static final int STATUS = 15;
    /** The  command. */
    public static final int APPEND = 16;
    /** The  command. */
    public static final int CHECK = 17;
    /** The  command. */
    public static final int CLOSE = 18;
    /** The  command. */
    public static final int EXPUNGE = 19;
    /** The  command. */
    public static final int SEARCH = 20;
    /** The  command. */
    public static final int FETCH = 21;
    /** The  command. */
    public static final int STORE = 22;
    /** The  command. */
    public static final int COPY = 23;
    /** The  command. */
    public static final int UID = 24;



    static final String[] _commands = {
                                          "CAPABILITY",
                                          "NOOP",
                                          "LOGOUT",
                                          "STARTTLS",
                                          "AUTHENTICATE",
                                          "LOGIN",
                                          "SELECT",
                                          "EXAMINE",
                                          "CREATE",
                                          "DELETE",
                                          "RENAME",
                                          "SUBSCRIBE",
                                          "UNSUBSCRIBE",
                                          "LIST",
                                          "LSUB",
                                          "STATUS",
                                          "APPEND",
                                          "CHECK",
                                          "CLOSE",
                                          "EXPUNGE",
                                          "SEARCH",
                                          "FETCH",
                                          "STORE",
                                          "COPY",
                                          "UID",
                                      };

    // Cannot be instantiated.
    private IMAPCommand()
    {}

    /**
     * Get the IMAP protocol string command corresponding to a command code.
     * <p>
     * @return The IMAP protocol string command corresponding to a command code.
     */
    public static final String getCommand(int command)
    {
        return _commands[command];
    }

}

/* kate: indent-width 4; replace-tabs on; */
