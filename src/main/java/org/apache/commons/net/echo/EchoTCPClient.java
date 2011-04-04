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

package org.apache.commons.net.echo;

import java.io.InputStream;

import org.apache.commons.net.discard.DiscardTCPClient;

/***
 * The EchoTCPClient class is a TCP implementation of a client for the
 * Echo protocol described in RFC 862.  To use the class, merely
 * establish a connection with
 * {@link org.apache.commons.net.SocketClient#connect  connect }
 * and call {@link DiscardTCPClient#getOutputStream  getOutputStream() } to
 * retrieve the echo output stream and
 * {@link #getInputStream getInputStream() }
 *  to get the echo input stream.
 * Don't close either stream when you're done using them.  Rather, call
 * {@link org.apache.commons.net.SocketClient#disconnect  disconnect }
 * to clean up properly.
 * <p>
 * <p>
 * @see EchoUDPClient
 * @see DiscardTCPClient
 ***/

public final class EchoTCPClient extends DiscardTCPClient
{
    /*** The default echo port.  It is set to 7 according to RFC 862. ***/
    public static final int DEFAULT_PORT = 7;

    /***
     * The default EchoTCPClient constructor.  It merely sets the default
     * port to <code> DEFAULT_PORT </code>.
     ***/
    public EchoTCPClient ()
    {
        setDefaultPort(DEFAULT_PORT);
    }

    /***
     * Returns an InputStream from which you may read echoed data from
     * the server.  You should NOT close the InputStream when you're finished
     * reading from it.  Rather, you should call
     * {@link org.apache.commons.net.SocketClient#disconnect  disconnect }
     * to clean up properly.
     * <p>
     * @return An InputStream from which you can read echoed data from the
     *         server.
     ***/
    public InputStream getInputStream()
    {
        return _input_;
    }

}
