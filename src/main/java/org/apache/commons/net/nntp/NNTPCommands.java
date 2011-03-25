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
 * NNTPCommands stores a set of constants for NNTP command codes.
 * To interpret the meaning of the codes, familiarity with RFC 977 / RFC 2980 is assumed.
 * <p>
 */

public enum NNTPCommands
{
    ARTICLE(1),                               // Message Id or server article number
    AUTHINFO,                                 // TBA
    BODY(1),                                  // Message Id or server article number
    CHECK(1),                                 // Message Id +++
    DATE,                                     // NONE ++
    GROUP(1),                                 // newsgroup name
    HEAD(1),                                  // Message Id or server article number
    HELP,                                     // NONE
    IHAVE(1),                                 // Message Id
    LAST,                                     // NONE
    LIST,                                     // NONE
    LIST_ACTIVE("LIST ACTIVE", 1),            // Pattern ++
    LIST_ACTIVE_TIMES("LIST ACTIVE.TIMES"),   // NONE ++
    LIST_DISTRIBUTIONS("LIST DISTRIBUTIONS"), // NONE ++
    LIST_DISTRIB_PATS("LIST DISTRIB.PATS"),   // NONE ++
    LIST_NEWSGROUPS("LIST NEWSGROUPS", 1),   // Pattern ++
    LIST_OVERVIEW_FMT("LIST OVERVIEW.FMT"),   // NONE ++
    LIST_SUBSCRIPTIONS("LIST SUBSCRIPTIONS"), // NONE ++
    LISTGROUP(1),                             // Newsgroup name
    MODE_READER("MODE READER"),               // NONE ++
//  MODE_STREAM,                              // NONE
    NEWGROUPS(3, 4),                          // date, time, GMT?, distribution list
    NEWNEWS(3, 4),                            // date, time, GMT?, distribution list
    NEXT,                                     // NONE
    POST,                                     // NONE
    QUIT,                                     // NONE
//  SLAVE,                                    // NONE
    STAT(1),                                  // server article number
    TAKETHIS(1),                              // Message Id ++
//  XGTITLE deprecated - see LIST_NEWSGROUPS
    XHDR(1, 2),                               // Header name [message id | range of article numbers]
//  XINDEX, // Newsgroup name => prefer XOVER
    XOVER(1, 2),                              // article number or range of article numbers
    XPAT(3, 4),                               // Header name, pattern, message id or range of article numbers
    XPATH(1),                                 // Message id
//  XREPLIC,
    XROVER(1, 2),                             // article number or range of article numbers
//  XTHREAD // c.f. XINDEX => prefer XOVER
   ;

    private final String command;
    private final int minParamCount;
    private final int maxParamCount;

    NNTPCommands() {
        this(null, 0, 0);
    }

    NNTPCommands(String s) {
        this(s, 0, 0);
    }

    NNTPCommands(String s, int count) {
        this(s, count, count);
    }

    NNTPCommands(int min, int max) {
        this(null, min, max);
    }

    NNTPCommands(int count) {
        this(null, count, count);
    }

    NNTPCommands(String s, int min, int max) {
        this.command = s;
        this.minParamCount = min;
        this.maxParamCount = max;
    }

    /**
     * Get the command to be sent to the server.
     *
     * @return the command string, never {@code null}
     */
    public String getCommand() {
        return command != null ? command : name() ;
    }

    public boolean hasParam() {
        return minParamCount > 0;
    }
}

/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
