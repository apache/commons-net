/***
 * $Id: DefaultDatagramSocketFactory.java,v 1.1 2002/04/03 01:04:27 brekke Exp $
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

import java.net.*;

/***
 * DefaultDatagramSocketFactory implements the DatagramSocketFactory 
 * interface by simply wrapping the java.net.DatagramSocket
 * constructors.  It is the default DatagramSocketFactory used by
 * <a href="com.oroinc.net.DatagramSocketClient.html"> 
 * DatagramSocketClient </a> implementations.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see DatagramSocketFactory
 * @see DatagramSocketClient
 * @see DatagramSocketClient#setDatagramSocketFactory
 ***/

public class DefaultDatagramSocketFactory implements DatagramSocketFactory {

  /***
   * Creates a DatagramSocket on the local host at the first available port.
   * <p>
   * @exception SocketException If the socket could not be created.
   ***/
  public DatagramSocket createDatagramSocket() throws SocketException {
    return new DatagramSocket();
  }

  /***
   * Creates a DatagramSocket on the local host at a specified port.
   * <p>
   * @param port The port to use for the socket.
   * @exception SocketException If the socket could not be created.
   ***/
  public DatagramSocket createDatagramSocket(int port) throws SocketException {
    return new DatagramSocket(port);
  }

  /***
   * Creates a DatagramSocket at the specified address on the local host
   * at a specified port.
   * <p>
   * @param port The port to use for the socket.
   * @param laddr  The local address to use.
   * @exception SocketException If the socket could not be created.
   ***/
  public DatagramSocket createDatagramSocket(int port, InetAddress laddr)
       throws SocketException
  {
    return new DatagramSocket(port, laddr);
  }
}
