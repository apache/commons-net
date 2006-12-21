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
 * of RFC 977.  For those who think in terms of the actual reply code values,
 * a set of CODE_NUM constants are provided where NUM is the numerical value
 * of the code.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class NNTPReply
{

    public static final int CODE_100 = 100;
    public static final int CODE_199 = 199;
    public static final int CODE_200 = 200;
    public static final int CODE_201 = 201;
    public static final int CODE_202 = 202;
    public static final int CODE_205 = 205;
    public static final int CODE_211 = 211;
    public static final int CODE_215 = 215;
    public static final int CODE_220 = 220;
    public static final int CODE_221 = 221;
    public static final int CODE_222 = 222;
    public static final int CODE_223 = 223;
    public static final int CODE_230 = 230;
    public static final int CODE_231 = 231;
    public static final int CODE_235 = 235;
    public static final int CODE_240 = 240;
    public static final int CODE_281 = 281;
    public static final int CODE_335 = 335;
    public static final int CODE_340 = 340;
    public static final int CODE_381 = 381;
    public static final int CODE_400 = 400;
    public static final int CODE_408 = 408;
    public static final int CODE_411 = 411;
    public static final int CODE_412 = 412;
    public static final int CODE_420 = 420;
    public static final int CODE_421 = 421;
    public static final int CODE_422 = 422;
    public static final int CODE_423 = 423;
    public static final int CODE_430 = 430;
    public static final int CODE_435 = 435;
    public static final int CODE_436 = 436;
    public static final int CODE_437 = 437;
    public static final int CODE_440 = 440;
    public static final int CODE_441 = 441;
    public static final int CODE_482 = 482;
    public static final int CODE_500 = 500;
    public static final int CODE_501 = 501;
    public static final int CODE_502 = 502;
    public static final int CODE_503 = 503;

    public static final int HELP_TEXT_FOLLOWS                = CODE_100;
    public static final int DEBUG_OUTPUT                     = CODE_199;
    public static final int SERVER_READY_POSTING_ALLOWED     = CODE_200;
    public static final int SERVER_READY_POSTING_NOT_ALLOWED = CODE_201;
    public static final int SLAVE_STATUS_NOTED               = CODE_202;
    public static final int CLOSING_CONNECTION               = CODE_205;
    public static final int GROUP_SELECTED                   = CODE_211;
    public static final int ARTICLE_RETRIEVED_HEAD_AND_BODY_FOLLOW = CODE_220;
    public static final int ARTICLE_RETRIEVED_HEAD_FOLLOWS = CODE_221;
    public static final int ARTICLE_RETRIEVED_BODY_FOLLOWS = CODE_222;
    public static final int
      ARTICLE_RETRIEVED_REQUEST_TEXT_SEPARATELY = CODE_223;
    public static final int ARTICLE_LIST_BY_MESSAGE_ID_FOLLOWS = CODE_230;
    public static final int NEW_NEWSGROUP_LIST_FOLLOWS         = CODE_231;
    public static final int ARTICLE_TRANSFERRED_OK             = CODE_235;
    public static final int ARTICLE_POSTED_OK                  = CODE_240;
    public static final int AUTHENTICATION_ACCEPTED            = CODE_281;
    public static final int SEND_ARTICLE_TO_TRANSFER           = CODE_335;
    public static final int SEND_ARTICLE_TO_POST               = CODE_340;
    public static final int MORE_AUTH_INFO_REQUIRED            = CODE_381;
    public static final int SERVICE_DISCONTINUED               = CODE_400;
    public static final int NO_SUCH_NEWSGROUP                  = CODE_411;
    public static final int AUTHENTICATION_REQUIRED            = CODE_408;
    public static final int NO_NEWSGROUP_SELECTED              = CODE_412;
    public static final int NO_CURRENT_ARTICLE_SELECTED        = CODE_420;
    public static final int NO_NEXT_ARTICLE                    = CODE_421;
    public static final int NO_PREVIOUS_ARTICLE                = CODE_422;
    public static final int NO_SUCH_ARTICLE_NUMBER             = CODE_423;
    public static final int NO_SUCH_ARTICLE_FOUND              = CODE_430;
    public static final int ARTICLE_NOT_WANTED                 = CODE_435;
    public static final int TRANSFER_FAILED                    = CODE_436;
    public static final int ARTICLE_REJECTED                   = CODE_437;
    public static final int POSTING_NOT_ALLOWED                = CODE_440;
    public static final int POSTING_FAILED                     = CODE_441;
    public static final int AUTHENTICATION_REJECTED            = CODE_482;
    public static final int COMMAND_NOT_RECOGNIZED             = CODE_500;
    public static final int COMMAND_SYNTAX_ERROR               = CODE_501;
    public static final int PERMISSION_DENIED                  = CODE_502;
    public static final int PROGRAM_FAULT                      = CODE_503;

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
