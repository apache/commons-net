/***
 * $Id: DiscardTCPClient.java,v 1.1 2002/04/03 01:04:26 brekke Exp $
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
 * The DiscardTCPClient class is a TCP implementation of a client for the
 * Discard protocol described in RFC 863.  To use the class, merely
 * establish a connection with
 * <a href="com.oroinc.net.SocketClient.html#connect"> connect </a>
 * and call <a href="#getOutputStream"> getOutputStream() </a> to 
 * retrieve the discard output stream.  Don't close the output stream
 * when you're done writing to it.  Rather, call
 * <a href="com.oroinc.net.SocketClient.html#disconnect"> disconnect </a>
 * to clean up properly.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see DiscardUDPClient
 ***/

public class DiscardTCPClient extends SocketClient {
  /*** The default discard port.  It is set to 9 according to RFC 863. ***/
  public static final int DEFAULT_PORT = 9;

  /***
   * The default DiscardTCPClient constructor.  It merely sets the default
   * port to <code> DEFAULT_PORT </code>.
   ***/
  public DiscardTCPClient () {
    setDefaultPort(DEFAULT_PORT);
  }

  /***
   * Returns an OutputStream through which you may write data to the server.
   * You should NOT close the OutputStream when you're finished
   * reading from it.  Rather, you should call 
   * <a href="com.oroinc.net.SocketClient.html#disconnect"> disconnect </a>
   * to clean up properly.
   * <p>
   * @return An OutputStream through which you can write data to the server.
   ***/
  public OutputStream getOutputStream() { return _output_; }
}
