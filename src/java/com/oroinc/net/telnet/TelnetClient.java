/***
 * $Id: TelnetClient.java,v 1.1 2002/04/03 01:04:28 brekke Exp $
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

package com.oroinc.net.telnet;

import java.net.*;
import java.io.*;

import com.oroinc.io.*;

/***
 * The TelnetClient class implements the simple network virtual
 * terminal (NVT) for the Telnet protocol according to RFC 854.  It
 * does not implement any of the extra Telnet options because it
 * is meant to be used within a Java program providing automated
 * access to Telnet accessible resources.  A telnet client implementing
 * extra options and meant for use with a terminal emulator can be
 * found in <b>NetComponents Pro <sup><font size=-1>TM</font></sup></b>
 * <p>
 * The class can be used by first connecting to a server using the
 * SocketClient
 * <a href="com.oroinc.net.SocketClient.html#connect">connect</a>
 * method.  Then an InputStream and OutputStream for sending and
 * receiving data over the Telnet connection can be obtained by
 * using the <a href="#getInputStream"> getInputStream() </a> and
 * <a href="#getOutputStream"> getOutputStream() </a> methods.
 * When you finish using the streams, you must call
 * <a href="#disconnect"> disconnect </a> rather than simply
 * closing the streams.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public class TelnetClient extends Telnet {
  private InputStream  __input;
  private OutputStream __output;

  /***
   * Default TelnetClient constructor.
   ***/
  public TelnetClient() {
    __input = null;
    __output = null;
  }

  void _flushOutputStream() throws IOException { _output_.flush(); }
  void _closeOutputStream() throws IOException { _output_.close(); }

  /***
   * Handles special connection requirements.
   * <p>
   * @exception IOException  If an error occurs during connection setup.
   ***/
  protected void _connectAction_() throws IOException {
    super._connectAction_();
    InputStream input;
    TelnetInputStream tmp;

    if(FromNetASCIIInputStream.isConversionRequired())
      input = new FromNetASCIIInputStream(_input_);
    else
      input = _input_;


    tmp = new TelnetInputStream(input, this);
    tmp._start();
    // __input CANNOT refer to the TelnetInputStream.  We run into
    // blocking problems when some classes use TelnetInputStream, so
    // we wrap it with a BufferedInputStream which we know is safe.
    // This blocking behavior requires further investigation, but right
    // now it looks like classes like InputStreamReader are not implemented
    // in a safe manner.
    __input = new BufferedInputStream(tmp);
    __output = new ToNetASCIIOutputStream(new TelnetOutputStream(this));
  }

  /***
   * Disconnects the telnet session, closing the input and output streams
   * as well as the socket.  If you have references to the
   * input and output streams of the telnet connection, you should not
   * close them yourself, but rather call disconnect to properly close
   * the connection.
   ***/
  public void disconnect() throws IOException {
    __input.close();
    __output.close();
    super.disconnect();
  }

  /***
   * Returns the telnet connection output stream.  You should not close the
   * stream when you finish with it.  Rather, you should call
   * <a href="#disconnect"> disconnect </a>.
   * <p>
   * @return The telnet connection output stream.
   ***/
  public OutputStream getOutputStream() { return __output; }

  /***
   * Returns the telnet connection input stream.  You should not close the
   * stream when you finish with it.  Rather, you should call
   * <a href="#disconnect"> disconnect </a>.
   * <p>
   * @return The telnet connection input stream.
   ***/
  public InputStream  getInputStream()  { return __input; }
}
