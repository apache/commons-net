/***
 * $Id: DefaultSocketFactory.java,v 1.1 2002/04/03 01:04:27 brekke Exp $
 *
 * NetComponents Internet Protocol Library
 * Copyright (C) 1997-2002  Daniel F. Savarese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library in the LICENSE file; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 ***/

package com.oroinc.net;

import java.io.*;
import java.net.*;

/***
 * DefaultSocketFactory implements the SocketFactory interface by
 * simply wrapping the java.net.Socket and java.net.ServerSocket
 * constructors.  It is the default SocketFactory used by
 * <a href="com.oroinc.net.SocketClient.html"> SocketClient </a>
 * implementations.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see SocketFactory
 * @see SocketClient
 * @see SocketClient#setSocketFactory
 ***/

public class DefaultSocketFactory implements SocketFactory {

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
       throws UnknownHostException, IOException
  {
    return new Socket(host, port);
  }

  /***
   * Creates a Socket connected to the given host and port.
   * <p>
   * @param address The address of the host to connect to.
   * @param port The port to connect to.
   * @return A Socket connected to the given host and port.
   * @exception IOException If an I/O error occurs while creating the Socket.
   ***/
  public Socket createSocket(InetAddress address, int port)
       throws IOException
  {
    return new Socket(address, port);
  }

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
  public Socket createSocket(String host, int port,
			   InetAddress localAddr, int localPort)
       throws UnknownHostException, IOException
  {
    return new Socket(host, port, localAddr, localPort);
  }

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
       throws IOException
  {
    return new Socket(address, port, localAddr, localPort);
  }

  /***
   * Creates a ServerSocket bound to a specified port.  A port
   * of 0 will create the ServerSocket on a system-determined free port.
   * <p>
   * @param port  The port on which to listen, or 0 to use any free port. 
   * @return A ServerSocket that will listen on a specified port.
   * @exception IOException If an I/O error occurs while creating
   *                        the ServerSocket.
   ***/
  public ServerSocket createServerSocket(int port) throws IOException {
    return new ServerSocket(port);
  }

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
       throws IOException
  {
    return new ServerSocket(port, backlog);
  }

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
       throws IOException
  {
    return new ServerSocket(port, backlog, bindAddr);
  }
}
