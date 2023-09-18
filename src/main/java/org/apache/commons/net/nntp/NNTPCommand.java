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

package org.apache.commons.net.nntp;

/**
 * NNTPCommand stores a set of constants for NNTP command codes. To interpret the meaning of the codes, familiarity with RFC 977 is assumed.
 */
public final class NNTPCommand {

    /** NNTP command code {@value}. */
    public static final int ARTICLE = 0;

    /** NNTP command code {@value}. */
    public static final int BODY = 1;

    /** NNTP command code {@value}. */
    public static final int GROUP = 2;

    /** NNTP command code {@value}. */
    public static final int HEAD = 3;

    /** NNTP command code {@value}. */
    public static final int HELP = 4;

    /** NNTP command code {@value}. */
    public static final int IHAVE = 5;

    /** NNTP command code {@value}. */
    public static final int LAST = 6;

    /** NNTP command code {@value}. */
    public static final int LIST = 7;

    /** NNTP command code {@value}. */
    public static final int NEWGROUPS = 8;

    /** NNTP command code {@value}. */
    public static final int NEWNEWS = 9;

    /** NNTP command code {@value}. */
    public static final int NEXT = 10;

    /** NNTP command code {@value}. */
    public static final int POST = 11;

    /** NNTP command code {@value}. */
    public static final int QUIT = 12;

    /** NNTP command code {@value}. */
    public static final int SLAVE = 13;

    /** NNTP command code {@value}. */
    public static final int STAT = 14;

    /** NNTP command code {@value}. */
    public static final int AUTHINFO = 15;

    /** NNTP command code {@value}. */
    public static final int XOVER = 16;

    /** NNTP command code {@value}. */
    public static final int XHDR = 17;

    private static final String[] commands = { "ARTICLE", "BODY", "GROUP", "HEAD", "HELP", "IHAVE", "LAST", "LIST", "NEWGROUPS", "NEWNEWS", "NEXT", "POST",
            "QUIT", "SLAVE", "STAT", "AUTHINFO", "XOVER", "XHDR" };

    /**
     * Gets the NNTP protocol command string corresponding to a specified command code.
     *
     * @param command The command code.
     * @return The NNTP protcol command string corresponding to a specified command code.
     */
    public static String getCommand(final int command) {
        return commands[command];
    }

    /** Cannot be instantiated. */
    private NNTPCommand() {
    }

}
