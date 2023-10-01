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

package org.apache.commons.net.smtp;

/**
 * SMTPCommand stores a set of constants for SMTP command codes. To interpret the meaning of the codes, familiarity with RFC 821 is assumed. The mnemonic
 * constant names are transcriptions from the code descriptions of RFC 821. For those who think in terms of the actual SMTP commands, a set of constants such as
 * {@link #HELO HELO } are provided where the constant name is the same as the SMTP command.
 */
public final class SMTPCommand {

    /** SMTP command {@value}. */
    public static final int HELO = 0;

    /** SMTP command {@value}. */
    public static final int MAIL = 1;

    /** SMTP command {@value}. */
    public static final int RCPT = 2;

    /** SMTP command {@value}. */
    public static final int DATA = 3;

    /** SMTP command {@value}. */
    public static final int SEND = 4;

    /** SMTP command {@value}. */
    public static final int SOML = 5;

    /** SMTP command {@value}. */
    public static final int SAML = 6;

    /** SMTP command {@value}. */
    public static final int RSET = 7;

    /** SMTP command {@value}. */
    public static final int VRFY = 8;

    /** SMTP command {@value}. */
    public static final int EXPN = 9;

    /** SMTP command {@value}. */
    public static final int HELP = 10;

    /** SMTP command {@value}. */
    public static final int NOOP = 11;

    /** SMTP command {@value}. */
    public static final int TURN = 12;

    /** SMTP command {@value}. */
    public static final int QUIT = 13;

    /**
     * The authorization command
     *
     * @since 3.0
     */
    public static final int AUTH = 14;

    /**
     * The extended hello command
     *
     * @since 3.0
     */
    public static final int EHLO = 15;

    private static final int NEXT = EHLO + 1; // update as necessary when adding new entries

    /** Alias for {@value}. */
    public static final int HELLO = HELO;

    /** Alias for {@value}. */
    public static final int LOGIN = HELO;

    /** Alias for {@value}. */
    public static final int MAIL_FROM = MAIL;

    /** Alias for {@value}. */
    public static final int RECIPIENT = RCPT;

    /** Alias for {@value}. */
    public static final int SEND_MESSAGE_DATA = DATA;

    /** Alias for {@value}. */
    public static final int SEND_FROM = SEND;

    /** Alias for {@value}. */
    public static final int SEND_OR_MAIL_FROM = SOML;

    /** Alias for {@value}. */
    public static final int SEND_AND_MAIL_FROM = SAML;

    /** Alias for {@value}. */
    public static final int RESET = RSET;

    /** Alias for {@value}. */
    public static final int VERIFY = VRFY;

    /** Alias for {@value}. */
    public static final int EXPAND = EXPN;

    /** Alias for {@value}. */
    public static final int LOGOUT = QUIT;

    private static final String[] commands = { "HELO", "MAIL FROM:", "RCPT TO:", "DATA", "SEND FROM:", "SOML FROM:", "SAML FROM:", "RSET", "VRFY", "EXPN",
            "HELP", "NOOP", "TURN", "QUIT", "AUTH", "EHLO" };

    static {
        if (commands.length != NEXT) {
            throw new IllegalStateException("Error in array definition");
        }
    }

    /**
     * Gets the SMTP protocol command string corresponding to a specified command code.
     *
     * @param command The command code.
     * @return The SMTP protocol command string corresponding to a specified command code.
     */
    public static String getCommand(final int command) {
        return commands[command];
    }

    /** Cannot be instantiated. */
    private SMTPCommand() {
    }

}
