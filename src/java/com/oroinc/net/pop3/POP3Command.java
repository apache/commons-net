/***
 * $Id: POP3Command.java,v 1.1 2002/04/03 01:04:33 brekke Exp $
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

package com.oroinc.net.pop3;

import java.io.*;
import java.net.*;
import java.util.*;

/***
 * POP3Command stores POP3 command code constants.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class POP3Command {
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
  public static final int TOP  = 10;
  /*** List unique message identifier(s). ***/
  public static final int UIDL = 11;

  static final String[] _commands = {
    "USER", "PASS", "QUIT", "STAT", "LIST", "RETR", "DELE", "NOOP", "RSET",
    "APOP", "TOP", "UIDL"
  };

  // Cannot be instantiated.
  private POP3Command() {}

  /***
   * Get the POP3 protocol string command corresponding to a command code.
   * <p>
   * @return The POP3 protocol string command corresponding to a command code.
   ***/
  public static final String getCommand(int command) {
    return _commands[command];
  }
}
