/***
 * $Id: SMTPCommand.java,v 1.1 2002/04/03 01:04:36 brekke Exp $
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
 * SMTPCommand stores a set of constants for SMTP command codes.  To interpret
 * the meaning of the codes, familiarity with RFC 821 is assumed.
 * The mnemonic constant names are transcriptions from the code descriptions
 * of RFC 821.  For those who think in terms of the actual SMTP commands,
 * a set of constants such as <a href="#HELO"> HELO </a> are provided
 * where the constant name is the same as the SMTP command.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class SMTPCommand {


  public static final int HELO = 0;
  public static final int MAIL = 1;
  public static final int RCPT = 2;
  public static final int DATA = 3;
  public static final int SEND = 4;
  public static final int SOML = 5;
  public static final int SAML = 6;
  public static final int RSET = 7;
  public static final int VRFY = 8;
  public static final int EXPN = 9;
  public static final int HELP = 10;
  public static final int NOOP = 11;
  public static final int TURN = 12;
  public static final int QUIT = 13;

  public static final int HELLO              = HELO;
  public static final int LOGIN              = HELO;
  public static final int MAIL_FROM          = MAIL;
  public static final int RECIPIENT          = RCPT;
  public static final int SEND_MESSAGE_DATA  = DATA;
  public static final int SEND_FROM          = SEND;
  public static final int SEND_OR_MAIL_FROM  = SOML;
  public static final int SEND_AND_MAIL_FROM = SAML;
  public static final int RESET              = RSET;
  public static final int VERIFY             = VRFY;
  public static final int EXPAND             = EXPN;
  // public static final int HELP = HELP;
  // public static final int NOOP = NOOP;
  // public static final int TURN = TURN;
  // public static final int QUIT = QUIT;
  public static final int LOGOUT             = QUIT;

  // Cannot be instantiated
  private SMTPCommand() {}

  static final String[] _commands =  {
    "HELO", "MAIL FROM:", "RCPT TO:", "DATA", "SEND FROM:", "SOML FROM:",
    "SAML FROM:", "RSET", "VRFY", "EXPN", "HELP", "NOOP", "TURN", "QUIT"
  };


  /***
   * Retrieve the SMTP protocol command string corresponding to a specified
   * command code.
   * <p>
   * @param The command code.
   * @return The SMTP protcol command string corresponding to a specified
   *         command code.
   ***/
  public static final String getCommand(int command) {
    return _commands[command];
  }

}
