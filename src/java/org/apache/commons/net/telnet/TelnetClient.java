package org.apache.commons.net.telnet;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.net.*;
import java.io.*;

import org.apache.commons.io.*;

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
 * <a href="org.apache.commons.net.SocketClient.html#connect">connect</a>
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
