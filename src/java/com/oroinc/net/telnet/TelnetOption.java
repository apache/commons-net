/***
 * $Id: TelnetOption.java,v 1.1 2002/04/03 01:04:28 brekke Exp $
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
 * The TelnetOption class cannot be instantiated and only serves as a
 * storehouse for telnet option constants.
 * <p>
 * Details regarding Telnet option specification can be found in RFC 855.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see Telnet
 * @see TelnetClient
 ***/

public class TelnetOption {
  /*** The maximum value an option code can have.  This value is 255. ***/
  public static final int MAX_OPTION_VALUE = 255;

  public static int BINARY = 0;

  public static int ECHO   = 1;

  public static int PREPARE_TO_RECONNECT     = 2;

  public static int SUPPRESS_GO_AHEAD        = 3;

  public static int APPROXIMATE_MESSAGE_SIZE = 4;

  public static int STATUS      = 5;

  public static int TIMING_MARK = 6;

  public static int REMOTE_CONTROLLED_TRANSMISSION = 7;

  public static int NEGOTIATE_OUTPUT_LINE_WIDTH    = 8;

  public static int NEGOTIATE_OUTPUT_PAGE_SIZE     = 9;

  public static int NEGOTIATE_CARRIAGE_RETURN      = 10;

  public static int NEGOTIATE_HORIZONTAL_TAB_STOP  = 11;

  public static int NEGOTIATE_HORIZONTAL_TAB       = 12;

  public static int NEGOTIATE_FORMFEED             = 13;

  public static int NEGOTIATE_VERTICAL_TAB_STOP    = 14;

  public static int NEGOTIATE_VERTICAL_TAB     = 15;

  public static int NEGOTIATE_LINEFEED         = 16;

  public static int EXTENDED_ASCII             = 17;

  public static int FORCE_LOGOUT               = 18;

  public static int BYTE_MACRO                 = 19;

  public static int DATA_ENTRY_TERMINAL        = 20;

  public static int SUPDUP                     = 21;

  public static int SUPDUP_OUTPUT              = 22;

  public static int SEND_LOCATION              = 23;

  public static int TERMINAL_TYPE              = 24;

  public static int END_OF_RECORD              = 25;

  public static int TACACS_USER_IDENTIFICATION = 26;

  public static int OUTPUT_MARKING             = 27;

  public static int TERMINAL_LOCATION_NUMBER   = 28;

  public static int REGIME_3270                = 29;

  public static int X3_PAD                     = 30;

  public static int WINDOW_SIZE                = 31;

  public static int TERMINAL_SPEED             = 32;

  public static int REMOTE_FLOW_CONTROL        = 33;

  public static int LINEMODE                   = 34;

  public static int X_DISPLAY_LOCATION         = 35;

  public static int OLD_ENVIRONMENT_VARIABLES  = 36;

  public static int AUTHENTICATION             = 37;

  public static int ENCRYPTION                 = 38;

  public static int NEW_ENVIRONMENT_VARIABLES  = 39;

  public static int EXTENDED_OPTIONS_LIST      = 255;

  private static int __FIRST_OPTION = BINARY;
  private static int __LAST_OPTION  = NEW_ENVIRONMENT_VARIABLES;

  private static final String __optionString[] = {
    "BINARY", "ECHO", "RCP", "SUPPRESS GO AHEAD", "NAME", "STATUS",
    "TIMING MARK", "RCTE","NAOL", "NAOP", "NAOCRD", "NAOHTS", "NAOHTD",
    "NAOFFD", "NAOVTS", "NAOVTD", "NAOLFD", "EXTEND ASCII", "LOGOUT",
    "BYTE MACRO", "DATA ENTRY TERMINAL", "SUPDUP", "SUPDUP OUTPUT",
    "SEND LOCATION", "TERMINAL TYPE", "END OF RECORD", "TACACS UID",
    "OUTPUT MARKING", "TTYLOC", "3270 REGIME", "X.3 PAD", "NAWS", "TSPEED",
    "LFLOW", "LINEMODE", "XDISPLOC", "OLD-ENVIRON", "AUTHENTICATION",
    "ENCRYPT", "NEW-ENVIRON"
  };


  /***
   * Returns the string representation of the telnet protocol option
   * corresponding to the given option code.
   * <p>
   * @param The option code of the telnet protocol option
   * @return The string representation of the telnet protocol option.
   ***/
  public static final String getOption(int code) {
    return __optionString[code];
  }


  /***
   * Determines if a given option code is valid.  Returns true if valid,
   * false if not.
   * <p>
   * @param code  The option code to test.
   * @return True if the option code is valid, false if not.
   **/
  public static final boolean isValidOption(int code) {
    return (code <= __LAST_OPTION);
  }

  // Cannot be instantiated
  private TelnetOption() { }
}
