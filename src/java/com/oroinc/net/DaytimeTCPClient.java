/***
 * $Id: DaytimeTCPClient.java,v 1.1 2002/04/03 01:04:27 brekke Exp $
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
 * The DaytimeTCPClient class is a TCP implementation of a client for the
 * Daytime protocol described in RFC 867.  To use the class, merely
 * establish a connection with
 * <a href="com.oroinc.net.SocketClient.html#connect"> connect </a>
 * and call <a href="#getTime"> getTime() </a> to retrieve the daytime
 * string, then
 * call <a href="com.oroinc.net.SocketClient.html#disconnect"> disconnect </a>
 * to close the connection properly.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see DaytimeUDPClient
 ***/

public final class DaytimeTCPClient extends SocketClient {
  /*** The default daytime port.  It is set to 13 according to RFC 867. ***/
  public static final int DEFAULT_PORT   = 13;

  // Received dates will likely be less than 64 characters.
  // This is a temporary buffer used while receiving data.
  private char[] __buffer = new char[64];

  /***
   * The default DaytimeTCPClient constructor.  It merely sets the default
   * port to <code> DEFAULT_PORT </code>.
   ***/
  public DaytimeTCPClient () {
    setDefaultPort(DEFAULT_PORT);
  }

  /***
   * Retrieves the time string from the server and returns it.  The
   * server will have closed the connection at this point, so you should
   * call
   * <a href="com.oroinc.net.SocketClient.html#disconnect"> disconnect </a>
   * after calling this method.  To retrieve another time, you must
   * initiate another connection with 
   * <a href="com.oroinc.net.SocketClient.html#connect"> connect </a>
   * before calling <code> getTime() </code> again.
   * <p>
   * @return The time string retrieved from the server.
   * @exception IOException  If an error occurs while fetching the time string.
   ***/
  public String getTime() throws IOException {
    int read;
    StringBuffer result = new StringBuffer(__buffer.length);
    BufferedReader reader;

    reader = new BufferedReader(new InputStreamReader(_input_));

    while(true) {
      read = reader.read(__buffer, 0, __buffer.length);
      if(read <= 0)
	break;
      result.append(__buffer, 0, read);
    }

    return result.toString();
  }

}

