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

public final class SMTPCommand
{


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

    public static final int HELLO = HELO;
    public static final int LOGIN = HELO;
    public static final int MAIL_FROM = MAIL;
    public static final int RECIPIENT = RCPT;
    public static final int SEND_MESSAGE_DATA = DATA;
    public static final int SEND_FROM = SEND;
    public static final int SEND_OR_MAIL_FROM = SOML;
    public static final int SEND_AND_MAIL_FROM = SAML;
    public static final int RESET = RSET;
    public static final int VERIFY = VRFY;
    public static final int EXPAND = EXPN;
    // public static final int HELP = HELP;
    // public static final int NOOP = NOOP;
    // public static final int TURN = TURN;
    // public static final int QUIT = QUIT;
    public static final int LOGOUT = QUIT;

    // Cannot be instantiated
    private SMTPCommand()
    {}

    static final String[] _commands = {
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
    public static final String getCommand(int command)
    {
        return _commands[command];
    }

}
