/*
 * Copyright 2001-2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/***
 * The SocketFactory interface provides a means for the programmer to
 * control the creation of sockets and provide his own Socket
 * implementations for use by all classes derived from
 * {@link org.apache.commons.net.SocketClient}.
 * This allows you to provide your own Socket implementations and
 * to perform security checks or browser capability requests before
 * creating a Socket.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see DefaultSocketFactory
 ***/

public interface SocketFactory
{

    /***
     * Creates a Socket connected to the given host and port.
     * <p>
     * @param host The hostname to connect to.
     * @param port The port to connect to.
     * @return A Socket connected to the given host and port.
     * @exception UnknownHostException  If the hostname cannot be resolved.
     * @exception IOException If an I/O error occurs while creating the Socket.
     ***/
    public Socket createSocket(String host, int port)
    throws UnknownHostException, IOException;


    /***
     * Creates a Socket connected to the given host and port.
     * <p>
     * @param address The address of the host to connect to.
     * @param port The port to connect to.
     * @return A Socket connected to the given host and port.
     * @exception IOException If an I/O error occurs while creating the Socket.
     ***/
    public Socket createSocket(InetAddress address, int port)
    throws IOException;


    /***
     * Creates a Socket connected to the given host and port and
     * originating from the specified local address and port.
     * <p>
     * @param host The hostname to connect to.
     * @param port The port to connect to.
     * @param localAddr  The local address to use.
     * @param localPort  The local port to use.
     * @return A Socket connected to the given host and port.
     * @exception UnknownHostException  If the hostname cannot be resolved.
     * @exception IOException If an I/O error occurs while creating the Socket.
     ***/
    public Socket createSocket(String host, int port, InetAddress localAddr,
                               int localPort)
    throws UnknownHostException, IOException;

    /***
     * Creates a Socket connected to the given host and port and
     * originating from the specified local address and port.
     * <p>
     * @param address The address of the host to connect to.
     * @param port The port to connect to.
     * @param localAddr  The local address to use.
     * @param localPort  The local port to use.
     * @return A Socket connected to the given host and port.
     * @exception IOException If an I/O error occurs while creating the Socket.
     ***/
    public Socket createSocket(InetAddress address, int port,
                               InetAddress localAddr, int localPort)
    throws IOException;

    /***
     * Creates a ServerSocket bound to a specified port.  A port
     * of 0 will create the ServerSocket on a system-determined free port.
     * <p>
     * @param port  The port on which to listen, or 0 to use any free port.
     * @return A ServerSocket that will listen on a specified port.
     * @exception IOException If an I/O error occurs while creating
     *                        the ServerSocket.
     ***/
    public ServerSocket createServerSocket(int port) throws IOException;

    /***
     * Creates a ServerSocket bound to a specified port with a given
     * maximum queue length for incoming connections.  A port of 0 will
     * create the ServerSocket on a system-determined free port.
     * <p>
     * @param port  The port on which to listen, or 0 to use any free port.
     * @param backlog  The maximum length of the queue for incoming connections.
     * @return A ServerSocket that will listen on a specified port.
     * @exception IOException If an I/O error occurs while creating
     *                        the ServerSocket.
     ***/
    public ServerSocket createServerSocket(int port, int backlog)
    throws IOException;

    /***
     * Creates a ServerSocket bound to a specified port on a given local
     * address with a given maximum queue length for incoming connections.
     * A port of 0 will
     * create the ServerSocket on a system-determined free port.
     * <p>
     * @param port  The port on which to listen, or 0 to use any free port.
     * @param backlog  The maximum length of the queue for incoming connections.
     * @param bindAddr  The local address to which the ServerSocket should bind.
     * @return A ServerSocket that will listen on a specified port.
     * @exception IOException If an I/O error occurs while creating
     *                        the ServerSocket.
     ***/
    public ServerSocket createServerSocket(int port, int backlog,
                                           InetAddress bindAddr)
    throws IOException;
}
