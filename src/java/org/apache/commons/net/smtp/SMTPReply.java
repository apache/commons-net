package org.apache.commons.net.smtp;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/***
 * SMTPReply stores a set of constants for SMTP reply codes.  To interpret
 * the meaning of the codes, familiarity with RFC 821 is assumed.
 * The mnemonic constant names are transcriptions from the code descriptions
 * of RFC 821.  For those who think in terms of the actual reply code values,
 * a set of CODE_NUM constants are provided where NUM is the numerical value
 * of the code.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class SMTPReply
{

    public static final int CODE_211 = 211;
    public static final int CODE_214 = 214;
    public static final int CODE_215 = 215;
    public static final int CODE_220 = 220;
    public static final int CODE_221 = 221;
    public static final int CODE_250 = 250;
    public static final int CODE_251 = 251;
    public static final int CODE_354 = 354;
    public static final int CODE_421 = 421;
    public static final int CODE_450 = 450;
    public static final int CODE_451 = 451;
    public static final int CODE_452 = 452;
    public static final int CODE_500 = 500;
    public static final int CODE_501 = 501;
    public static final int CODE_502 = 502;
    public static final int CODE_503 = 503;
    public static final int CODE_504 = 504;
    public static final int CODE_550 = 550;
    public static final int CODE_551 = 551;
    public static final int CODE_552 = 552;
    public static final int CODE_553 = 553;
    public static final int CODE_554 = 554;

    public static final int SYSTEM_STATUS = CODE_211;
    public static final int HELP_MESSAGE = CODE_214;
    public static final int SERVICE_READY = CODE_220;
    public static final int SERVICE_CLOSING_TRANSMISSION_CHANNEL = CODE_221;
    public static final int ACTION_OK = CODE_250;
    public static final int USER_NOT_LOCAL_WILL_FORWARD = CODE_251;
    public static final int START_MAIL_INPUT = CODE_354;
    public static final int SERVICE_NOT_AVAILABLE = CODE_421;
    public static final int ACTION_NOT_TAKEN = CODE_450;
    public static final int ACTION_ABORTED = CODE_451;
    public static final int INSUFFICIENT_STORAGE = CODE_452;
    public static final int UNRECOGNIZED_COMMAND = CODE_500;
    public static final int SYNTAX_ERROR_IN_ARGUMENTS = CODE_501;
    public static final int COMMAND_NOT_IMPLEMENTED = CODE_502;
    public static final int BAD_COMMAND_SEQUENCE = CODE_503;
    public static final int COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER = CODE_504;
    public static final int MAILBOX_UNAVAILABLE = CODE_550;
    public static final int USER_NOT_LOCAL = CODE_551;
    public static final int STORAGE_ALLOCATION_EXCEEDED = CODE_552;
    public static final int MAILBOX_NAME_NOT_ALLOWED = CODE_553;
    public static final int TRANSACTION_FAILED = CODE_554;

    // Cannot be instantiated
    private SMTPReply()
    {}

    /***
     * Determine if a reply code is a positive preliminary response.  All
     * codes beginning with a 1 are positive preliminary responses.
     * Postitive preliminary responses are used to indicate tentative success.
     * No further commands can be issued to the SMTP server after a positive
     * preliminary response until a follow up response is received from the
     * server.
     * <p>
     * <b> Note: </b> <em> No SMTP commands defined in RFC 822 provide this
     * type of reply. </em>
     * <p>
     * @param reply  The reply code to test.
     * @return True if a reply code is a postive preliminary response, false
     *         if not.
     ***/
    public static boolean isPositivePreliminary(int reply)
    {
        return (reply >= 100 && reply < 200);
    }

    /***
     * Determine if a reply code is a positive completion response.  All
     * codes beginning with a 2 are positive completion responses.
     * The SMTP server will send a positive completion response on the final
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
     * The SMTP server will send a positive intermediate response on the
     * successful completion of one part of a multi-part sequence of
     * commands.  For example, after a successful DATA command, a positive
     * intermediate response will be sent to indicate that the server is
     * ready to receive the message data.
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
     * The SMTP server will send a negative transient response on the
     * failure of a command that can be reattempted with success.
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
     * The SMTP server will send a negative permanent response on the
     * failure of a command that cannot be reattempted with success.
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
