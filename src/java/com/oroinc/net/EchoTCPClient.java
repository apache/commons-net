/***
 * $Id: EchoTCPClient.java,v 1.1 2002/04/03 01:04:27 brekke Exp $
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
 * The EchoTCPClient class is a TCP implementation of a client for the
 * Echo protocol described in RFC 862.  To use the class, merely
 * establish a connection with
 * <a href="com.oroinc.net.SocketClient.html#connect"> connect </a>
 * and call <a href="#getOutputStream"> getOutputStream() </a> to 
 * retrieve the echo output stream and
 * <a href="com.oroinc.net.DiscardTCPClient.html#getInputStream">
 * getInputStream() </a> to get the echo input stream.
 * Don't close either stream when you're done using them.  Rather, call
 * <a href="com.oroinc.net.SocketClient.html#disconnect"> disconnect </a>
 * to clean up properly.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see EchoUDPClient
 * @see DiscardTCPClient
 ***/

public final class EchoTCPClient extends DiscardTCPClient {
  /*** The default echo port.  It is set to 7 according to RFC 862. ***/
  public static final int DEFAULT_PORT = 7;

  /***
   * The default EchoTCPClient constructor.  It merely sets the default
   * port to <code> DEFAULT_PORT </code>.
   ***/
  public EchoTCPClient () {
    setDefaultPort(DEFAULT_PORT);
  }

  /***
   * Returns an InputStream from which you may read echoed data from
   * the server.  You should NOT close the InputStream when you're finished
   * reading from it.  Rather, you should call 
   * <a href="com.oroinc.net.SocketClient.html#disconnect"> disconnect </a>
   * to clean up properly.
   * <p>
   * @return An InputStream from which you can read echoed data from the
   *         server.
   ***/
  public InputStream getInputStream()   { return _input_; }

}
