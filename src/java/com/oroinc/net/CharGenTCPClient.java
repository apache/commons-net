/***
 * $Id: CharGenTCPClient.java,v 1.1 2002/04/03 01:04:25 brekke Exp $
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
 * The CharGenTCPClient class is a TCP implementation of a client for the
 * character generator protocol described in RFC 864.  It can also be
 * used for Systat (RFC 866), Quote of the Day (RFC 865), and netstat
 * (port 15).  All of these protocols involve connecting to the appropriate
 * port, and reading data from an input stream.  The chargen protocol
 * actually sends data until the receiving end closes the connection.  All
 * of the others send only a fixed amount of data and then close the
 * connection.
 * <p>
 * To use the CharGenTCPClient class, just establish a
 * connection with
 * <a href="com.oroinc.net.SocketClient.html#connect"> connect </a>
 * and call <a href="#getInputStream"> getInputStream() </a> to access
 * the data.  Don't close the input stream when you're done with it.  Rather,
 * call <a href="com.oroinc.net.SocketClient.html#disconnect"> disconnect </a>
 * to clean up properly.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see CharGenUDPClient
 ***/

public final class CharGenTCPClient extends SocketClient {
  /*** The systat port value of 11 according to RFC 866. ***/
  public static final int SYSTAT_PORT       = 11;
  /*** The netstat port value of 19. ***/
  public static final int NETSTAT_PORT      = 15;
  /*** The quote of the day port value of 17 according to RFC 865. ***/
  public static final int QUOTE_OF_DAY_PORT = 17;
  /*** The character generator port value of 19 according to RFC 864. ***/
  public static final int CHARGEN_PORT      = 19;
  /*** The default chargen port.  It is set to 19 according to RFC 864. ***/
  public static final int DEFAULT_PORT      = 19;

  /***
   * The default constructor for CharGenTCPClient.  It merely sets the
   * default port to <code> DEFAULT_PORT </code>.
   ***/
  public CharGenTCPClient () {
    setDefaultPort(DEFAULT_PORT);
  }

  /***
   * Returns an InputStream from which the server generated data can be
   * read.  You should NOT close the InputStream when you're finished
   * reading from it.  Rather, you should call 
   * <a href="com.oroinc.net.SocketClient.html#disconnect"> disconnect </a>
   * to clean up properly.
   * <p>
   * @return An InputStream from which the server generated data can be read.
   ***/
  public InputStream getInputStream()   { return _input_; }
}




