/***
 * $Id: EchoUDPClient.java,v 1.1 2002/04/03 01:04:28 brekke Exp $
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
 * The EchoUDPClient class is a UDP implementation of a client for the
 * Echo protocol described in RFC 862.  To use the class,
 * just open a local UDP port
 * with <a href="com.oroinc.net.DatagramSocketClient.html#open"> open </a>
 * and call <a href="#send"> send </a> to send datagrams to the server,
 * then call <a href="#receive"> receive </a> to receive echoes.
 * After you're done echoing data, call
 * <a href="com.oroinc.net.DatagramSocketClient.html#close"> close() </a>
 * to clean up properly.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see EchoTCPClient
 * @see DiscardUDPClient
 ***/

public final class EchoUDPClient extends DiscardUDPClient {
  /*** The default echo port.  It is set to 7 according to RFC 862. ***/
  public static final int DEFAULT_PORT = 7;

  private DatagramPacket __receivePacket = new DatagramPacket(new byte[0], 0);

  /***
   * Sends the specified data to the specified server at the default echo
   * port.
   * <p>
   * @param data  The echo data to send.
   * @param length  The length of the data to send.  Should be less than
   *    or equal to the length of the data byte array.
   * @param host  The address of the server.
   * @exception IOException If an error occurs during the datagram send
   *     operation.
   ***/
  public void send(byte[] data, int length, InetAddress host)
       throws IOException
  {
    send(data, length, host, DEFAULT_PORT);
  }


  /*** Same as <code> send(data, data.length, host) </code> ***/
  public void send(byte[] data, InetAddress host) throws IOException {
    send(data, data.length, host, DEFAULT_PORT);
  }


  /***
   * Receives echoed data and returns its length.  The data may be divided
   * up among multiple datagrams, requiring multiple calls to receive.
   * Also, the UDP packets will not necessarily arrive in the same order
   * they were sent.
   * <p>
   * @return  Length of actual data received.
   * @exception IOException If an error occurs while receiving the data.
   ***/
  public int receive(byte[] data, int length) throws IOException {
    __receivePacket.setData(data);
    __receivePacket.setLength(length);
    _socket_.receive(__receivePacket);
    return __receivePacket.getLength();
  }

  /*** Same as <code> receive(data, data.length)</code> ***/
  public int receive(byte[] data) throws IOException {
    return receive(data, data.length);
  }

}

