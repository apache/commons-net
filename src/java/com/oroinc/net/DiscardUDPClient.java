/***
 * $Id: DiscardUDPClient.java,v 1.1 2002/04/03 01:04:27 brekke Exp $
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
 * The DiscardUDPClient class is a UDP implementation of a client for the
 * Discard protocol described in RFC 863.  To use the class,
 * just open a local UDP port
 * with <a href="com.oroinc.net.DatagramSocketClient.html#open"> open </a>
 * and call <a href="#send"> send </a> to send datagrams to the server
 * After you're done sending discard data, call
 * <a href="com.oroinc.net.DatagramSocketClient.html#close"> close() </a>
 * to clean up properly.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see DiscardTCPClient
 ***/

public class DiscardUDPClient extends DatagramSocketClient {
  /*** The default discard port.  It is set to 9 according to RFC 863. ***/
  public static final int DEFAULT_PORT = 9;

  DatagramPacket _sendPacket;

  public DiscardUDPClient() {
    _sendPacket = new DatagramPacket(new byte[0], 0);
  }


  /***
   * Sends the specified data to the specified server at the specified port.
   * <p>
   * @param data  The discard data to send.
   * @param length  The length of the data to send.  Should be less than
   *    or equal to the length of the data byte array.
   * @param host  The address of the server.
   * @param port  The service port.
   * @exception IOException If an error occurs during the datagram send
   *            operation.
   ***/
  public void send(byte[] data, int length, InetAddress host, int port)
       throws IOException
  {
    _sendPacket.setData(data);
    _sendPacket.setLength(length);
    _sendPacket.setAddress(host);
    _sendPacket.setPort(port);
    _socket_.send(_sendPacket);
  }


  /***
   * Same as
   * <code>send(data, length, host. DiscardUDPClient.DEFAULT_PORT)</code>.
   ***/
  public void send(byte[] data, int length, InetAddress host)
       throws IOException
  {
    send(data, length, host, DEFAULT_PORT);
  }


  /***
   * Same as
   * <code>send(data, data.length, host. DiscardUDPClient.DEFAULT_PORT)</code>.
   ***/
  public void send(byte[] data, InetAddress host) throws IOException {
    send(data, data.length, host, DEFAULT_PORT);
  }

}

