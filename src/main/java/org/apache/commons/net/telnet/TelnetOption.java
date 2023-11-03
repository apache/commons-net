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

package org.apache.commons.net.telnet;

/**
 * The TelnetOption class cannot be instantiated and only serves as a storehouse for telnet option constants.
 * <p>
 * Details regarding Telnet option specification can be found in RFC 855.
 * </p>
 *
 * @see org.apache.commons.net.telnet.Telnet
 * @see org.apache.commons.net.telnet.TelnetClient
 */
public class TelnetOption {
    /** The maximum value an option code can have. This value is 255. */
    public static final int MAX_OPTION_VALUE = 255;

    /**
     * {@value}
     */
    public static final int BINARY = 0;

    /**
     * {@value}
     */
    public static final int ECHO = 1;

    /**
     * {@value}
     */
    public static final int PREPARE_TO_RECONNECT = 2;

    /**
     * {@value}
     */
    public static final int SUPPRESS_GO_AHEAD = 3;

    /**
     * {@value}
     */
    public static final int APPROXIMATE_MESSAGE_SIZE = 4;

    /**
     * {@value}
     */
    public static final int STATUS = 5;

    /**
     * {@value}
     */
    public static final int TIMING_MARK = 6;

    /**
     * {@value}
     */
    public static final int REMOTE_CONTROLLED_TRANSMISSION = 7;

    /**
     * {@value}
     */
    public static final int NEGOTIATE_OUTPUT_LINE_WIDTH = 8;

    /**
     * {@value}
     */
    public static final int NEGOTIATE_OUTPUT_PAGE_SIZE = 9;

    /**
     * {@value}
     */
    public static final int NEGOTIATE_CARRIAGE_RETURN = 10;

    /**
     * {@value}
     */
    public static final int NEGOTIATE_HORIZONTAL_TAB_STOP = 11;

    /**
     * {@value}
     */
    public static final int NEGOTIATE_HORIZONTAL_TAB = 12;

    /**
     * {@value}
     */
    public static final int NEGOTIATE_FORMFEED = 13;

    /**
     * {@value}
     */
    public static final int NEGOTIATE_VERTICAL_TAB_STOP = 14;

    /**
     * {@value}
     */
    public static final int NEGOTIATE_VERTICAL_TAB = 15;

    /**
     * {@value}
     */
    public static final int NEGOTIATE_LINEFEED = 16;

    /**
     * {@value}
     */
    public static final int EXTENDED_ASCII = 17;

    /**
     * {@value}
     */
    public static final int FORCE_LOGOUT = 18;

    /**
     * {@value}
     */
    public static final int BYTE_MACRO = 19;

    /**
     * {@value}
     */
    public static final int DATA_ENTRY_TERMINAL = 20;

    /**
     * {@value}
     */
    public static final int SUPDUP = 21;

    /**
     * {@value}
     */
    public static final int SUPDUP_OUTPUT = 22;

    /**
     * {@value}
     */
    public static final int SEND_LOCATION = 23;

    /**
     * {@value}
     */
    public static final int TERMINAL_TYPE = 24;

    /**
     * {@value}
     */
    public static final int END_OF_RECORD = 25;

    /**
     * {@value}
     */
    public static final int TACACS_USER_IDENTIFICATION = 26;

    /**
     * {@value}
     */
    public static final int OUTPUT_MARKING = 27;

    /**
     * {@value}
     */
    public static final int TERMINAL_LOCATION_NUMBER = 28;

    /**
     * {@value}
     */
    public static final int REGIME_3270 = 29;

    /**
     * {@value}
     */
    public static final int X3_PAD = 30;

    /**
     * {@value}
     */
    public static final int WINDOW_SIZE = 31;

    /**
     * {@value}
     */
    public static final int TERMINAL_SPEED = 32;

    /**
     * {@value}
     */
    public static final int REMOTE_FLOW_CONTROL = 33;

    /**
     * {@value}
     */
    public static final int LINEMODE = 34;

    /**
     * {@value}
     */
    public static final int X_DISPLAY_LOCATION = 35;

    /**
     * {@value}
     */
    public static final int OLD_ENVIRONMENT_VARIABLES = 36;

    /**
     * {@value}
     */
    public static final int AUTHENTICATION = 37;

    /**
     * {@value}
     */
    public static final int ENCRYPTION = 38;

    /**
     * {@value}
     */
    public static final int NEW_ENVIRONMENT_VARIABLES = 39;

    /**
     * {@value}
     */
    public static final int EXTENDED_OPTIONS_LIST = 255;

    /**
     * {@value}
     */
    @SuppressWarnings("unused")
    private static final int FIRST_OPTION = BINARY;

    /**
     * {@value}
     */
    private static final int LAST_OPTION = EXTENDED_OPTIONS_LIST;

    /**
     * {@value}
     */
    private static final String[] optionString = { "BINARY", "ECHO", "RCP", "SUPPRESS GO AHEAD", "NAME", "STATUS", "TIMING MARK", "RCTE", "NAOL", "NAOP",
            "NAOCRD", "NAOHTS", "NAOHTD", "NAOFFD", "NAOVTS", "NAOVTD", "NAOLFD", "EXTEND ASCII", "LOGOUT", "BYTE MACRO", "DATA ENTRY TERMINAL", "SUPDUP",
            "SUPDUP OUTPUT", "SEND LOCATION", "TERMINAL TYPE", "END OF RECORD", "TACACS UID", "OUTPUT MARKING", "TTYLOC", "3270 REGIME", "X.3 PAD", "NAWS",
            "TSPEED", "LFLOW", "LINEMODE", "XDISPLOC", "OLD-ENVIRON", "AUTHENTICATION", "ENCRYPT", "NEW-ENVIRON", "TN3270E", "XAUTH", "CHARSET", "RSP",
            "Com Port Control", "Suppress Local Echo", "Start TLS", "KERMIT", "SEND-URL", "FORWARD_X", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
            "TELOPT PRAGMA LOGON", "TELOPT SSPI LOGON", "TELOPT PRAGMA HEARTBEAT", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
            "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "Extended-Options-List" };

    /**
     * Returns the string representation of the telnet protocol option corresponding to the given option code.
     *
     * @param code The option code of the telnet protocol option
     * @return The string representation of the telnet protocol option.
     */
    public static final String getOption(final int code) {
        if (optionString[code].isEmpty()) {
            return "UNASSIGNED";
        }
        return optionString[code];
    }

    /**
     * Determines if a given option code is valid. Returns true if valid, false if not.
     *
     * @param code The option code to test.
     * @return True if the option code is valid, false if not.
     **/
    public static final boolean isValidOption(final int code) {
        return code <= LAST_OPTION;
    }

    /** Cannot be instantiated. */
    private TelnetOption() {
    }
}
