package org.apache.commons.net.telnet;

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
 * The TelnetOption class cannot be instantiated and only serves as a
 * storehouse for telnet option constants.
 * <p>
 * Details regarding Telnet option specification can be found in RFC 855.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see org.apache.commons.net.telnet.Telnet
 * @see org.apache.commons.net.telnet.TelnetClient
 ***/

public class TelnetOption
{
    /*** The maximum value an option code can have.  This value is 255. ***/
    public static final int MAX_OPTION_VALUE = 255;

    public static int BINARY = 0;

    public static int ECHO = 1;

    public static int PREPARE_TO_RECONNECT = 2;

    public static int SUPPRESS_GO_AHEAD = 3;

    public static int APPROXIMATE_MESSAGE_SIZE = 4;

    public static int STATUS = 5;

    public static int TIMING_MARK = 6;

    public static int REMOTE_CONTROLLED_TRANSMISSION = 7;

    public static int NEGOTIATE_OUTPUT_LINE_WIDTH = 8;

    public static int NEGOTIATE_OUTPUT_PAGE_SIZE = 9;

    public static int NEGOTIATE_CARRIAGE_RETURN = 10;

    public static int NEGOTIATE_HORIZONTAL_TAB_STOP = 11;

    public static int NEGOTIATE_HORIZONTAL_TAB = 12;

    public static int NEGOTIATE_FORMFEED = 13;

    public static int NEGOTIATE_VERTICAL_TAB_STOP = 14;

    public static int NEGOTIATE_VERTICAL_TAB = 15;

    public static int NEGOTIATE_LINEFEED = 16;

    public static int EXTENDED_ASCII = 17;

    public static int FORCE_LOGOUT = 18;

    public static int BYTE_MACRO = 19;

    public static int DATA_ENTRY_TERMINAL = 20;

    public static int SUPDUP = 21;

    public static int SUPDUP_OUTPUT = 22;

    public static int SEND_LOCATION = 23;

    public static int TERMINAL_TYPE = 24;

    public static int END_OF_RECORD = 25;

    public static int TACACS_USER_IDENTIFICATION = 26;

    public static int OUTPUT_MARKING = 27;

    public static int TERMINAL_LOCATION_NUMBER = 28;

    public static int REGIME_3270 = 29;

    public static int X3_PAD = 30;

    public static int WINDOW_SIZE = 31;

    public static int TERMINAL_SPEED = 32;

    public static int REMOTE_FLOW_CONTROL = 33;

    public static int LINEMODE = 34;

    public static int X_DISPLAY_LOCATION = 35;

    public static int OLD_ENVIRONMENT_VARIABLES = 36;

    public static int AUTHENTICATION = 37;

    public static int ENCRYPTION = 38;

    public static int NEW_ENVIRONMENT_VARIABLES = 39;

    public static int EXTENDED_OPTIONS_LIST = 255;

    private static int __FIRST_OPTION = BINARY;
    private static int __LAST_OPTION = EXTENDED_OPTIONS_LIST;

    private static final String __optionString[] = {
                "BINARY", "ECHO", "RCP", "SUPPRESS GO AHEAD", "NAME", "STATUS",
                "TIMING MARK", "RCTE", "NAOL", "NAOP", "NAOCRD", "NAOHTS", "NAOHTD",
                "NAOFFD", "NAOVTS", "NAOVTD", "NAOLFD", "EXTEND ASCII", "LOGOUT",
                "BYTE MACRO", "DATA ENTRY TERMINAL", "SUPDUP", "SUPDUP OUTPUT",
                "SEND LOCATION", "TERMINAL TYPE", "END OF RECORD", "TACACS UID",
                "OUTPUT MARKING", "TTYLOC", "3270 REGIME", "X.3 PAD", "NAWS", "TSPEED",
                "LFLOW", "LINEMODE", "XDISPLOC", "OLD-ENVIRON", "AUTHENTICATION",
                "ENCRYPT", "NEW-ENVIRON", "TN3270E", "XAUTH", "CHARSET", "RSP",
                "Com Port Control", "Suppress Local Echo", "Start TLS",
                "KERMIT", "SEND-URL", "FORWARD_X", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "TELOPT PRAGMA LOGON", "TELOPT SSPI LOGON",
                "TELOPT PRAGMA HEARTBEAT", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", "", "",
                "Extended-Options-List"
            };


    /***
     * Returns the string representation of the telnet protocol option
     * corresponding to the given option code.
     * <p>
     * @param The option code of the telnet protocol option
     * @return The string representation of the telnet protocol option.
     ***/
    public static final String getOption(int code)
    {
        if(__optionString[code].length() == 0)
        {
            return "UNASSIGNED";
        }
        else
        {
            return __optionString[code];
        }
    }


    /***
     * Determines if a given option code is valid.  Returns true if valid,
     * false if not.
     * <p>
     * @param code  The option code to test.
     * @return True if the option code is valid, false if not.
     **/
    public static final boolean isValidOption(int code)
    {
        return (code <= __LAST_OPTION);
    }

    // Cannot be instantiated
    private TelnetOption()
    { }
}
