package org.apache.commons.net;

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

import java.io.InputStream;

/***
 * The CharGenTCPClient class is a TCP implementation of a client for the
 * character generator protocol described in RFC 864.  It can also be
 * used for Systat (RFC 866), Quote of the Day (RFC 865), and netstat
 * (port 15).  All of these protocols involve connecting to the appropriate
 * port, and reading data from an input stream.  The chargen protocol
 * actually sends data until the receiving end closes the connection.  All
 * of the others send only a fixed amount of data and then close the
 * connection.
 * <p>
 * To use the CharGenTCPClient class, just establish a
 * connection with
 * <a href="org.apache.commons.net.SocketClient.html#connect"> connect </a>
 * and call <a href="#getInputStream"> getInputStream() </a> to access
 * the data.  Don't close the input stream when you're done with it.  Rather,
 * call <a href="org.apache.commons.net.SocketClient.html#disconnect"> disconnect </a>
 * to clean up properly.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see CharGenUDPClient
 ***/

public final class CharGenTCPClient extends SocketClient
{
    /*** The systat port value of 11 according to RFC 866. ***/
    public static final int SYSTAT_PORT = 11;
    /*** The netstat port value of 19. ***/
    public static final int NETSTAT_PORT = 15;
    /*** The quote of the day port value of 17 according to RFC 865. ***/
    public static final int QUOTE_OF_DAY_PORT = 17;
    /*** The character generator port value of 19 according to RFC 864. ***/
    public static final int CHARGEN_PORT = 19;
    /*** The default chargen port.  It is set to 19 according to RFC 864. ***/
    public static final int DEFAULT_PORT = 19;

    /***
     * The default constructor for CharGenTCPClient.  It merely sets the
     * default port to <code> DEFAULT_PORT </code>.
     ***/
    public CharGenTCPClient ()
    {
        setDefaultPort(DEFAULT_PORT);
    }

    /***
     * Returns an InputStream from which the server generated data can be
     * read.  You should NOT close the InputStream when you're finished
     * reading from it.  Rather, you should call 
     * <a href="org.apache.commons.net.SocketClient.html#disconnect"> disconnect </a>
     * to clean up properly.
     * <p>
     * @return An InputStream from which the server generated data can be read.
     ***/
    public InputStream getInputStream()
    {
        return _input_;
    }
}




