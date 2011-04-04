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

package org.apache.commons.net.chargen;

import java.io.InputStream;

import org.apache.commons.net.SocketClient;

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
 * {@link org.apache.commons.net.SocketClient#connect  connect }
 * and call {@link #getInputStream  getInputStream() } to access
 * the data.  Don't close the input stream when you're done with it.  Rather,
 * call {@link org.apache.commons.net.SocketClient#disconnect  disconnect }
 * to clean up properly.
 * <p>
 * <p>
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
     * {@link org.apache.commons.net.SocketClient#disconnect  disconnect }
     * to clean up properly.
     * <p>
     * @return An InputStream from which the server generated data can be read.
     ***/
    public InputStream getInputStream()
    {
        return _input_;
    }
}




