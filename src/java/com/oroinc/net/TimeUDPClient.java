/***
 * $Id: TimeUDPClient.java,v 1.1 2002/04/03 01:04:27 brekke Exp $
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

import java.util.*;

/***
 * The TimeUDPClient class is a UDP implementation of a client for the
 * Time protocol described in RFC 868.  To use the class, merely
 * open a local datagram socket with
 * <a href="com.oroinc.net.DatagramSocketClient.html#open"> open </a>
 * and call <a href="#getTime"> getTime </a> or
 * <a href="#getTime"> getDate </a> to retrieve the time. Then call
 * <a href="com.oroinc.net.DatagramSocketClient.html#close"> close </a>
 * to close the connection properly.  Unlike
 * <a href="com.oroinc.net.TimeTCPClient.html"> TimeTCPClient </a>,
 * successive calls to <a href="#getTime"> getTime </a> or
 * <a href="#getDate"> getDate </a> are permitted
 * without re-establishing a connection.  That is because UDP is a
 * connectionless protocol and the Time protocol is stateless.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see TimeTCPClient
 ***/

public final class TimeUDPClient extends DatagramSocketClient {
  /*** The default time port.  It is set to 37 according to RFC 868. ***/
  public static final int DEFAULT_PORT          = 37;

  /***
   * The number of seconds between 00:00 1 January 1900 and
   * 00:00 1 January 1970.  This value can be useful for converting
   * time values to other formats.
   ***/
  public static final long SECONDS_1900_TO_1970 = 2208988800L;

  private byte[] __dummyData = new byte[1];
  private byte[] __timeData  = new byte[4];

  /***
   * Retrieves the time from the specified server and port and
   * returns it. The time is the number of seconds since 
   * 00:00 (midnight) 1 January 1900 GMT, as specified by RFC 868.
   * This method reads the raw 32-bit big-endian
   * unsigned integer from the server, converts it to a Java long, and
   * returns the value.  
   * <p>
   * @param host The address of the server.
   * @param port The port of the service.
   * @return The time value retrieved from the server.
   * @exception IOException If an error occurs while retrieving the time.
   ***/
  public long getTime(InetAddress host, int port) throws IOException {
    long time;
    DatagramPacket sendPacket, receivePacket;

    sendPacket =
      new DatagramPacket(__dummyData, __dummyData.length, host, port);
    receivePacket = new DatagramPacket(__timeData, __timeData.length);

    _socket_.send(sendPacket);
    _socket_.receive(receivePacket);

    time = 0L;
    time |= (((__timeData[0] & 0xff) << 24) & 0xffffffffL);
    time |= (((__timeData[1] & 0xff) << 16) & 0xffffffffL);
    time |= (((__timeData[2] & 0xff) << 8) & 0xffffffffL);
    time |= ((__timeData[3] & 0xff) & 0xffffffffL);

    return time;
  }

  /*** Same as <code> getTime(host, DEFAULT_PORT); </code> ***/
  public long getTime(InetAddress host) throws IOException {
    return getTime(host, DEFAULT_PORT);
  }


  /***
   * Retrieves the time from the server and returns a Java Date
   * containing the time converted to the local timezone.
   * <p>
   * @param host The address of the server.
   * @param port The port of the service.
   * @return A Date value containing the time retrieved from the server
   *     converted to the local timezone.
   * @exception IOException  If an error occurs while fetching the time.
   ***/
  public Date getDate(InetAddress host, int port) throws IOException {
    return new Date((getTime(host, port) - SECONDS_1900_TO_1970)*1000L);
  }


  /*** Same as <code> getTime(host, DEFAULT_PORT); </code> ***/
  public Date getDate(InetAddress host) throws IOException {
    return new Date((getTime(host, DEFAULT_PORT) -
		     SECONDS_1900_TO_1970)*1000L);
  }

}

