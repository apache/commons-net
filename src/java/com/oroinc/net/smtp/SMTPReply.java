/***
 * $Id: SMTPReply.java,v 1.1 2002/04/03 01:04:37 brekke Exp $
 *
 * NetComponents Internet Protocol Library
 * Copyright (C) 1997-2002  Daniel F. Savarese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library in the LICENSE file; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 ***/

package com.oroinc.net.smtp;

import java.net.*;
import java.io.*;

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

public final class SMTPReply {

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

  public static final int SYSTEM_STATUS                      = CODE_211;
  public static final int HELP_MESSAGE                       = CODE_214;
  public static final int SERVICE_READY                      = CODE_220;
  public static final int SERVICE_CLOSING_TRANSMISSION_CHANNEL = CODE_221;
  public static final int ACTION_OK                          = CODE_250;
  public static final int USER_NOT_LOCAL_WILL_FORWARD        = CODE_251;
  public static final int START_MAIL_INPUT                   = CODE_354;
  public static final int SERVICE_NOT_AVAILABLE              = CODE_421;
  public static final int ACTION_NOT_TAKEN                   = CODE_450;
  public static final int ACTION_ABORTED                     = CODE_451;
  public static final int INSUFFICIENT_STORAGE               = CODE_452;
  public static final int UNRECOGNIZED_COMMAND               = CODE_500;
  public static final int SYNTAX_ERROR_IN_ARGUMENTS          = CODE_501;
  public static final int COMMAND_NOT_IMPLEMENTED            = CODE_502;
  public static final int BAD_COMMAND_SEQUENCE               = CODE_503;
  public static final int COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER = CODE_504;
  public static final int MAILBOX_UNAVAILABLE                = CODE_550;
  public static final int USER_NOT_LOCAL                     = CODE_551;
  public static final int STORAGE_ALLOCATION_EXCEEDED        = CODE_552;
  public static final int MAILBOX_NAME_NOT_ALLOWED           = CODE_553;
  public static final int TRANSACTION_FAILED                 = CODE_554;

  // Cannot be instantiated
  private SMTPReply() {}

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
  public static boolean isPositivePreliminary(int reply) {
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
  public static boolean isPositiveCompletion(int reply) {
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
  public static boolean isPositiveIntermediate(int reply) {
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
  public static boolean isNegativeTransient(int reply) {
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
  public static boolean isNegativePermanent(int reply) {
    return (reply >= 500 && reply < 600);
  }

}
