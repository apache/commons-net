/***
 * $Id: SocketInputStream.java,v 1.1 2002/04/03 01:04:41 brekke Exp $
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

package com.oroinc.io;

import java.io.*;
import java.net.*;

/***
 * This class wraps an input stream, storing a reference to its originating
 * socket.  When the stream is closed, it will also close the socket
 * immediately afterward.  This class is useful for situations where you
 * are dealing with a stream originating from a socket, but do not have
 * a reference to the socket, and want to make sure it closes when the
 * stream closes.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see SocketOutputStream
 ***/

public class SocketInputStream extends FilterInputStream {
  private Socket __socket;

  /***
   * Creates a SocketInputStream instance wrapping an input stream and
   * storing a reference to a socket that should be closed on closing
   * the stream.
   * <p>
   * @param socket  The socket to close on closing the stream.
   * @param stream  The input stream to wrap.
   ***/
  public SocketInputStream(Socket socket, InputStream stream) {
    super(stream);
    __socket = socket;
  }

  /***
   * Closes the stream and immediately afterward closes the referenced
   * socket.
   * <p>
   * @exception IOException  If there is an error in closing the stream
   *                         or socket.
   ***/
  public void close() throws IOException {
    super.close();
    __socket.close();
  }
}
