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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/***
 * The DaytimeTCPClient class is a TCP implementation of a client for the
 * Daytime protocol described in RFC 867.  To use the class, merely
 * establish a connection with
 * <a href="org.apache.commons.net.SocketClient.html#connect"> connect </a>
 * and call <a href="#getTime"> getTime() </a> to retrieve the daytime
 * string, then
 * call <a href="org.apache.commons.net.SocketClient.html#disconnect"> disconnect </a>
 * to close the connection properly.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see DaytimeUDPClient
 ***/

public final class DaytimeTCPClient extends SocketClient
{
    /*** The default daytime port.  It is set to 13 according to RFC 867. ***/
    public static final int DEFAULT_PORT = 13;

    // Received dates will likely be less than 64 characters.
    // This is a temporary buffer used while receiving data.
    private char[] __buffer = new char[64];

    /***
     * The default DaytimeTCPClient constructor.  It merely sets the default
     * port to <code> DEFAULT_PORT </code>.
     ***/
    public DaytimeTCPClient ()
    {
        setDefaultPort(DEFAULT_PORT);
    }

    /***
     * Retrieves the time string from the server and returns it.  The
     * server will have closed the connection at this point, so you should
     * call
     * <a href="org.apache.commons.net.SocketClient.html#disconnect"> disconnect </a>
     * after calling this method.  To retrieve another time, you must
     * initiate another connection with 
     * <a href="org.apache.commons.net.SocketClient.html#connect"> connect </a>
     * before calling <code> getTime() </code> again.
     * <p>
     * @return The time string retrieved from the server.
     * @exception IOException  If an error occurs while fetching the time string.
     ***/
    public String getTime() throws IOException
    {
        int read;
        StringBuffer result = new StringBuffer(__buffer.length);
        BufferedReader reader;

        reader = new BufferedReader(new InputStreamReader(_input_));

        while (true)
        {
            read = reader.read(__buffer, 0, __buffer.length);
            if (read <= 0)
                break;
            result.append(__buffer, 0, read);
        }

        return result.toString();
    }

}

