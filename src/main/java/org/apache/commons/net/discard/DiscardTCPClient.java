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

package org.apache.commons.net.discard;

import java.io.OutputStream;

import org.apache.commons.net.SocketClient;

/***
 * The DiscardTCPClient class is a TCP implementation of a client for the
 * Discard protocol described in RFC 863.  To use the class, merely
 * establish a connection with
 * {@link org.apache.commons.net.SocketClient#connect  connect }
 * and call {@link #getOutputStream  getOutputStream() } to
 * retrieve the discard output stream.  Don't close the output stream
 * when you're done writing to it.  Rather, call
 * {@link org.apache.commons.net.SocketClient#disconnect  disconnect }
 * to clean up properly.
 * <p>
 * <p>
 * @see DiscardUDPClient
 ***/

public class DiscardTCPClient extends SocketClient
{
    /*** The default discard port.  It is set to 9 according to RFC 863. ***/
    public static final int DEFAULT_PORT = 9;

    /***
     * The default DiscardTCPClient constructor.  It merely sets the default
     * port to <code> DEFAULT_PORT </code>.
     ***/
    public DiscardTCPClient ()
    {
        setDefaultPort(DEFAULT_PORT);
    }

    /***
     * Returns an OutputStream through which you may write data to the server.
     * You should NOT close the OutputStream when you're finished
     * reading from it.  Rather, you should call
     * {@link org.apache.commons.net.SocketClient#disconnect  disconnect }
     * to clean up properly.
     * <p>
     * @return An OutputStream through which you can write data to the server.
     ***/
    public OutputStream getOutputStream()
    {
        return _output_;
    }
}
