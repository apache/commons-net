package org.apache.commons.net.nntp;

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
 * NNTPCommand stores a set of constants for NNTP command codes.  To interpret
 * the meaning of the codes, familiarity with RFC 977 is assumed.
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class NNTPCommand
{

    public static final int ARTICLE   = 0;
    public static final int BODY      = 1;
    public static final int GROUP     = 2;
    public static final int HEAD      = 3;
    public static final int HELP      = 4;
    public static final int IHAVE     = 5;
    public static final int LAST      = 6;
    public static final int LIST      = 7;
    public static final int NEWGROUPS = 8;
    public static final int NEWNEWS   = 9;
    public static final int NEXT      = 10;
    public static final int POST      = 11;
    public static final int QUIT      = 12;
    public static final int SLAVE     = 13;
    public static final int STAT      = 14;
    public static final int AUTHINFO  = 15;
    public static final int XOVER     = 16;

    // Cannot be instantiated
    private NNTPCommand()
    {}

    static final String[] _commands = {
        "ARTICLE", "BODY", "GROUP", "HEAD", "HELP", "IHAVE", "LAST", "LIST",
        "NEWGROUPS", "NEWNEWS", "NEXT", "POST", "QUIT", "SLAVE", "STAT",
        "AUTHINFO", "XOVER"
    };


    /***
     * Retrieve the NNTP protocol command string corresponding to a specified
     * command code.
     * <p>
     * @param The command code.
     * @return The NNTP protcol command string corresponding to a specified
     *         command code.
     ***/
    public static final String getCommand(int command)
    {
        return _commands[command];
    }

}

/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
