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

package org.apache.commons.net.time;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.net.SocketClient;

/***
 * The TimeTCPClient class is a TCP implementation of a client for the
 * Time protocol described in RFC 868.  To use the class, merely
 * establish a connection with
 * {@link org.apache.commons.net.SocketClient#connect  connect }
 * and call either {@link #getTime  getTime() } or
 * {@link #getDate  getDate() } to retrieve the time, then
 * call {@link org.apache.commons.net.SocketClient#disconnect  disconnect }
 * to close the connection properly.
 * <p>
 * <p>
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
     * {@link org.apache.commons.net.SocketClient#disconnect  disconnect }
     * after calling this method.  To retrieve another time, you must
     * initiate another connection with
     * {@link org.apache.commons.net.SocketClient#connect  connect }
     * before calling <code> getTime() </code> again.
     * <p>
     * @return The time value retrieved from the server.
     * @exception IOException  If an error occurs while fetching the time.
     ***/
    public long getTime() throws IOException
    {
        DataInputStream input;
        input = new DataInputStream(_input_);
        return (input.readInt() & 0xffffffffL);
    }

    /***
     * Retrieves the time from the server and returns a Java Date
     * containing the time converted to the local timezone.
     * <p>
     * The server will have closed the connection at this point, so you should
     * call
     * {@link org.apache.commons.net.SocketClient#disconnect  disconnect }
     * after calling this method.  To retrieve another time, you must
     * initiate another connection with
     * {@link org.apache.commons.net.SocketClient#connect  connect }
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

