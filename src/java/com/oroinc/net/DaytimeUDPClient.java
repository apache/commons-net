/***
 * $Id: DaytimeUDPClient.java,v 1.1 2002/04/03 01:04:28 brekke Exp $
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
 * The DaytimeUDPClient class is a UDP implementation of a client for the
 * Daytime protocol described in RFC 867.  To use the class, merely
 * open a local datagram socket with
 * <a href="com.oroinc.net.DatagramSocketClient.html#open"> open </a>
 * and call <a href="#getTime"> getTime </a> to retrieve the daytime
 * string, then
 * call <a href="com.oroinc.net.DatagramSocketClient.html#close"> close </a>
 * to close the connection properly.  Unlike
 * <a href="com.oroinc.net.DaytimeTCPClient.html"> DaytimeTCPClient </a>,
 * successive calls to <a href="#getTime"> getTime </a> are permitted
 * without re-establishing a connection.  That is because UDP is a
 * connectionless protocol and the Daytime protocol is stateless.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see DaytimeTCPClient
 ***/

public final class DaytimeUDPClient extends DatagramSocketClient {
  /*** The default daytime port.  It is set to 13 according to RFC 867. ***/
  public static final int DEFAULT_PORT   = 13;

  private byte[] __dummyData = new byte[1];
  // Received dates should be less than 256 bytes
  private byte[] __timeData  = new byte[256];

  /***
   * Retrieves the time string from the specified server and port and
   * returns it.
   * <p>
   * @param host The address of the server.
   * @param port The port of the service.
   * @return The time string.
   * @exception IOException If an error occurs while retrieving the time.
   ***/
  public String getTime(InetAddress host, int port) throws IOException {
    DatagramPacket sendPacket, receivePacket;

    sendPacket =
      new DatagramPacket(__dummyData, __dummyData.length, host, port);
    receivePacket = new DatagramPacket(__timeData, __timeData.length);

    _socket_.send(sendPacket);
    _socket_.receive(receivePacket);

    return new String(receivePacket.getData(), 0, receivePacket.getLength());
  }

  /*** Same as <code>getTime(host, DaytimeUDPClient.DEFAULT_PORT);</code> ***/
  public String getTime(InetAddress host) throws IOException {
    return getTime(host, DEFAULT_PORT);
  }

}

