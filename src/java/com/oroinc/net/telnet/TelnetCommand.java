/***
 * $Id: TelnetCommand.java,v 1.1 2002/04/03 01:04:28 brekke Exp $
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

package com.oroinc.net.telnet;

/***
 * The TelnetCommand class cannot be instantiated and only serves as a
 * storehouse for telnet command constants.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see Telnet
 * @see TelnetClient
 ***/

public final class TelnetCommand {
  /*** The maximum value a command code can have.  This value is 255. ***/
  public static final int MAX_COMMAND_VALUE = 255;

  /*** Interpret As Command code.  Value is 255 according to RFC 854. ***/
  public static final int IAC   = 255;

  /*** Don't use option code.  Value is 254 according to RFC 854. ***/
  public static final int DONT  = 254;

  /*** Request to use option code.  Value is 253 according to RFC 854. ***/
  public static final int DO    = 253;

  /*** Refuse to use option code.  Value is 252 according to RFC 854. ***/
  public static final int WONT  = 252;

  /*** Agree to use option code.  Value is 251 according to RFC 854. ***/
  public static final int WILL  = 251;

  /*** Start subnegotiation code.  Value is 250 according to RFC 854. ***/
  public static final int SB    = 250;

  /*** Go Ahead code.  Value is 249 according to RFC 854. ***/
  public static final int GA    = 249;

  /*** Erase Line code.  Value is 248 according to RFC 854. ***/
  public static final int EL    = 248;

  /*** Erase Character code.  Value is 247 according to RFC 854. ***/
  public static final int EC    = 247;

  /*** Are You There code.  Value is 246 according to RFC 854. ***/
  public static final int AYT   = 246;

  /*** Abort Output code.  Value is 245 according to RFC 854. ***/
  public static final int AO    = 245;

  /*** Interrupt Process code.  Value is 244 according to RFC 854. ***/
  public static final int IP    = 244;

  /*** Break code.  Value is 243 according to RFC 854. ***/
  public static final int BREAK = 243;

  /*** Data mark code.  Value is 242 according to RFC 854. ***/
  public static final int DM    = 242;

  /*** No Operation code.  Value is 241 according to RFC 854. ***/
  public static final int NOP   = 241;

  /*** End subnegotiation code.  Value is 240 according to RFC 854. ***/
  public static final int SE    = 240;

  /*** End of record code.  Value is 239. ***/
  public static final int EOR   = 239;

  /*** Abort code.  Value is 238. ***/
  public static final int ABORT = 238;

  /*** Suspend process code.  Value is 237. ***/
  public static final int SUSP  = 237;

  /*** End of file code.  Value is 236. ***/
  public static final int EOF   = 236;

  /*** Synchronize code.  Value is 242. ***/
  public static final int SYNCH = 242;

  /*** String representations of commands. ***/
  private static final String __commandString[] = {
    "IAC", "DONT", "DO", "WONT", "WILL", "SB", "GA", "EL", "EC", "AYT", 
    "AO", "IP", "BRK", "DMARK", "NOP", "SE", "EOR", "ABORT", "SUSP", "EOF"
  };

  private static final int __FIRST_COMMAND = IAC;
  private static final int __LAST_COMMAND  = EOF;

  /***
   * Returns the string representation of the telnet protocol command
   * corresponding to the given command code.
   * <p>
   * @param The command code of the telnet protocol command.
   * @return The string representation of the telnet protocol command.
   ***/
  public static final String getCommand(int code) {
    return __commandString[__FIRST_COMMAND - code];
  }

  /***
   * Determines if a given command code is valid.  Returns true if valid,
   * false if not.
   * <p>
   * @param code  The command code to test.
   * @return True if the command code is valid, false if not.
   **/
  public static final boolean isValidCommand(int code) {
    return (code <= __FIRST_COMMAND && code >= __LAST_COMMAND);
  }

  // Cannot be instantiated
  private TelnetCommand() { }
}
