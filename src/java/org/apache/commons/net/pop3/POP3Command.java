package org.apache.commons.net.pop3;

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
 * POP3Command stores POP3 command code constants.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class POP3Command
{
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
    public static final int TOP = 10;
    /*** List unique message identifier(s). ***/
    public static final int UIDL = 11;

    static final String[] _commands = {
                                          "USER", "PASS", "QUIT", "STAT", "LIST", "RETR", "DELE", "NOOP", "RSET",
                                          "APOP", "TOP", "UIDL"
                                      };

    // Cannot be instantiated.
    private POP3Command()
    {}

    /***
     * Get the POP3 protocol string command corresponding to a command code.
     * <p>
     * @return The POP3 protocol string command corresponding to a command code.
     ***/
    public static final String getCommand(int command)
    {
        return _commands[command];
    }
}
