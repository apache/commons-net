/***
 * $Id: SocketOutputStream.java,v 1.1 2002/04/03 01:04:40 brekke Exp $
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
 * This class wraps an output stream, storing a reference to its originating
 * socket.  When the stream is closed, it will also close the socket
 * immediately afterward.  This class is useful for situations where you
 * are dealing with a stream originating from a socket, but do not have
 * a reference to the socket, and want to make sure it closes when the
 * stream closes.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see SocketInputStream
 ***/

public class SocketOutputStream extends FilterOutputStream {
  private Socket __socket;

  /***
   * Creates a SocketOutputStream instance wrapping an output stream and
   * storing a reference to a socket that should be closed on closing
   * the stream.
   * <p>
   * @param socket  The socket to close on closing the stream.
   * @param stream  The input stream to wrap.
   ***/
  public SocketOutputStream(Socket socket, OutputStream stream) {
    super(stream);
    __socket = socket;
  }


  /***
   * Writes a number of bytes from a byte array to the stream starting from
   * a given offset.  This method bypasses the equivalent method in
   * FilterOutputStream because the FilterOutputStream implementation is
   * very inefficient.
   * <p>
   * @param buffer  The byte array to write.
   * @param offset  The offset into the array at which to start copying data.
   * @param length  The number of bytes to write.
   * @exception IOException If an error occurs while writing to the underlying
   *            stream.
   ***/
  public void write(byte buffer[], int offset, int length) throws IOException {
    out.write(buffer, offset, length);
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
