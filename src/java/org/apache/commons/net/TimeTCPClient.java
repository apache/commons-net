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

import java.io.IOException;
import java.util.Date;
import java.io.DataInputStream;

/***
 * The TimeTCPClient class is a TCP implementation of a client for the
 * Time protocol described in RFC 868.  To use the class, merely
 * establish a connection with
 * <a href="org.apache.commons.net.SocketClient.html#connect"> connect </a>
 * and call either <a href="#getTime"> getTime() </a> or  
 * <a href="#getDate"> getDate() </a> to retrieve the time, then
 * call <a href="org.apache.commons.net.SocketClient.html#disconnect"> disconnect </a>
 * to close the connection properly.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see TimeUDPClient
 ***/

public final class TimeTCPClient extends SocketClient
{
    /*** The default time port.  It is set to 37 according to RFC 868. ***/
    public static final int DEFAULT_PORT = 37;

    /***
     * The number of seconds between 00:00 1 January 1900 and
     * 00:00 1 January 1970.  This value can be useful for converting
     * time values to other formats.
     ***/
    public static final long SECONDS_1900_TO_1970 = 2208988800L;

    /***
     * The default TimeTCPClient constructor.  It merely sets the default
     * port to <code> DEFAULT_PORT </code>.
     ***/
    public TimeTCPClient ()
    {
        setDefaultPort(DEFAULT_PORT);
    }

    /***
     * Retrieves the time from the server and returns it.  The time
     * is the number of seconds since 00:00 (midnight) 1 January 1900 GMT,
     * as specified by RFC 868.  This method reads the raw 32-bit big-endian
     * unsigned integer from the server, converts it to a Java long, and
     * returns the value.  
     * <p>
     * The server will have closed the connection at this point, so you should
     * call
     * <a href="org.apache.commons.net.SocketClient.html#disconnect"> disconnect </a>
     * after calling this method.  To retrieve another time, you must
     * initiate another connection with 
     * <a href="org.apache.commons.net.SocketClient.html#connect"> connect </a>
     * before calling <code> getTime() </code> again.
     * <p>
     * @return The time value retrieved from the server.
     * @exception IOException  If an error occurs while fetching the time.
     ***/
    public long getTime() throws IOException
    {
        DataInputStream input;
        input = new DataInputStream(_input_);
        return (long)(input.readInt() & 0xffffffffL);
    }

    /***
     * Retrieves the time from the server and returns a Java Date
     * containing the time converted to the local timezone.
     * <p>
     * The server will have closed the connection at this point, so you should
     * call
     * <a href="org.apache.commons.net.SocketClient.html#disconnect"> disconnect </a>
     * after calling this method.  To retrieve another time, you must
     * initiate another connection with 
     * <a href="org.apache.commons.net.SocketClient.html#connect"> connect </a>
     * before calling <code> getDate() </code> again.
     * <p>
     * @return A Date value containing the time retrieved from the server
     *     converted to the local timezone.
     * @exception IOException  If an error occurs while fetching the time.
     ***/
    public Date getDate() throws IOException
    {
        return new Date((getTime() - SECONDS_1900_TO_1970)*1000L);
    }

}

