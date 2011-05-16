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

/***
 * NNTPReply stores a set of constants for NNTP reply codes.  To interpret
 * the meaning of the codes, familiarity with RFC 977 is assumed.
 * The mnemonic constant names are transcriptions from the code descriptions
 * of RFC 977.
 ***/

public final class NNTPReply
{

    public static final int HELP_TEXT_FOLLOWS                = 100;
    public static final int DEBUG_OUTPUT                     = 199;
    public static final int SERVER_READY_POSTING_ALLOWED     = 200;
    public static final int SERVER_READY_POSTING_NOT_ALLOWED = 201;
    public static final int SLAVE_STATUS_NOTED               = 202;
    public static final int CLOSING_CONNECTION               = 205;
    public static final int GROUP_SELECTED                   = 211;
    public static final int ARTICLE_RETRIEVED_HEAD_AND_BODY_FOLLOW = 220;
    public static final int ARTICLE_RETRIEVED_HEAD_FOLLOWS = 221;
    public static final int ARTICLE_RETRIEVED_BODY_FOLLOWS = 222;
    public static final int ARTICLE_RETRIEVED_REQUEST_TEXT_SEPARATELY = 223;
    public static final int ARTICLE_LIST_BY_MESSAGE_ID_FOLLOWS = 230;
    public static final int NEW_NEWSGROUP_LIST_FOLLOWS         = 231;
    public static final int ARTICLE_TRANSFERRED_OK             = 235;
    public static final int ARTICLE_POSTED_OK                  = 240;
    public static final int AUTHENTICATION_ACCEPTED            = 281;
    public static final int SEND_ARTICLE_TO_TRANSFER           = 335;
    public static final int SEND_ARTICLE_TO_POST               = 340;
    public static final int MORE_AUTH_INFO_REQUIRED            = 381;
    public static final int SERVICE_DISCONTINUED               = 400;
    public static final int NO_SUCH_NEWSGROUP                  = 411;
    public static final int NO_NEWSGROUP_SELECTED              = 412;
    public static final int NO_CURRENT_ARTICLE_SELECTED        = 420;
    public static final int NO_NEXT_ARTICLE                    = 421;
    public static final int NO_PREVIOUS_ARTICLE                = 422;
    public static final int NO_SUCH_ARTICLE_NUMBER             = 423;
    public static final int NO_SUCH_ARTICLE_FOUND              = 430;
    public static final int ARTICLE_NOT_WANTED                 = 435;
    public static final int TRANSFER_FAILED                    = 436;
    public static final int ARTICLE_REJECTED                   = 437;
    public static final int POSTING_NOT_ALLOWED                = 440;
    public static final int POSTING_FAILED                     = 441;
    /** @since 2.2 - corrected value to 480 */
    public static final int AUTHENTICATION_REQUIRED            = 480;
    public static final int AUTHENTICATION_REJECTED            = 482;
    public static final int COMMAND_NOT_RECOGNIZED             = 500;
    public static final int COMMAND_SYNTAX_ERROR               = 501;
    public static final int PERMISSION_DENIED                  = 502;
    public static final int PROGRAM_FAULT                      = 503;

    // Cannot be instantiated

    private NNTPReply()
    {}

    /***
     * Determine if a reply code is an informational response.  All
     * codes beginning with a 1 are positive informational responses.
     * Informational responses are used to provide human readable
     * information such as help text.
     * <p>
     * @param reply  The reply code to test.
     * @return True if a reply code is an informational response, false
     *         if not.
     ***/
    public static boolean isInformational(int reply)
    {
        return (reply >= 100 && reply < 200);
    }

    /***
     * Determine if a reply code is a positive completion response.  All
     * codes beginning with a 2 are positive completion responses.
     * The NNTP server will send a positive completion response on the final
     * successful completion of a command.
     * <p>
     * @param reply  The reply code to test.
     * @return True if a reply code is a postive completion response, false
     *         if not.
     ***/
    public static boolean isPositiveCompletion(int reply)
    {
        return (reply >= 200 && reply < 300);
    }

    /***
     * Determine if a reply code is a positive intermediate response.  All
     * codes beginning with a 3 are positive intermediate responses.
     * The NNTP server will send a positive intermediate response on the
     * successful completion of one part of a multi-part command or
     * sequence of commands.  For example, after a successful POST command,
     * a positive intermediate response will be sent to indicate that the
     * server is ready to receive the article to be posted.
     * <p>
     * @param reply  The reply code to test.
     * @return True if a reply code is a postive intermediate response, false
     *         if not.
     ***/
    public static boolean isPositiveIntermediate(int reply)
    {
        return (reply >= 300 && reply < 400);
    }

    /***
     * Determine if a reply code is a negative transient response.  All
     * codes beginning with a 4 are negative transient responses.
     * The NNTP server will send a negative transient response on the
     * failure of a correctly formatted command that could not be performed
     * for some reason.  For example, retrieving an article that does not
     * exist will result in a negative transient response.
     * <p>
     * @param reply  The reply code to test.
     * @return True if a reply code is a negative transient response, false
     *         if not.
     ***/
    public static boolean isNegativeTransient(int reply)
    {
        return (reply >= 400 && reply < 500);
    }

    /***
     * Determine if a reply code is a negative permanent response.  All
     * codes beginning with a 5 are negative permanent responses.
     * The NNTP server will send a negative permanent response when
     * it does not implement a command, a command is incorrectly formatted,
     * or a serious program error occurs.
     * <p>
     * @param reply  The reply code to test.
     * @return True if a reply code is a negative permanent response, false
     *         if not.
     ***/
    public static boolean isNegativePermanent(int reply)
    {
        return (reply >= 500 && reply < 600);
    }

}

/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
