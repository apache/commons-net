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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/***
 * The SocketFactory interface provides a means for the programmer to
 * control the creation of sockets and provide his own Socket
 * implementations for use by all classes derived from
 * <a href="org.apache.commons.net.SocketClient.html"> SocketClient </a>.
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
