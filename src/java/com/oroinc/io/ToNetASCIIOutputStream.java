/***
 * $Id: ToNetASCIIOutputStream.java,v 1.1 2002/04/03 01:04:41 brekke Exp $
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

/***
 * This class wraps an output stream, replacing all singly occurring
 * &lt;LF&gt; (linefeed) characters with &lt;CR&gt;&lt;LF&gt; (carriage return
 * followed by linefeed), which is the NETASCII standard for representing
 * a newline.
 * You would use this class to implement ASCII file transfers requiring
 * conversion to NETASCII.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class ToNetASCIIOutputStream extends FilterOutputStream {
  private boolean __lastWasCR;

  /***
   * Creates a ToNetASCIIOutputStream instance that wraps an existing
   * OutputStream.
   * <p>
   * @param output  The OutputStream to wrap.
   ***/
  public ToNetASCIIOutputStream(OutputStream output) {
    super(output);
    __lastWasCR = false;
  }


  /***
   * Writes a byte to the stream.    Note that a call to this method
   * may result in multiple writes to the underlying input stream in order
   * to convert naked newlines to NETASCII line separators.
   * This is transparent to the programmer and is only mentioned for
   * completeness.
   * <p>
   * @param ch The byte to write.
   * @exception IOException If an error occurs while writing to the underlying
   *            stream.
   ***/
  public synchronized void write(int ch) throws IOException {
    switch(ch) {
    case '\r':
      __lastWasCR = true;
      out.write('\r');
      return;
    case '\n':
      if(!__lastWasCR)
	out.write('\r');
      // Fall through
    default:
      __lastWasCR = false;
      out.write(ch);
      return;
    }
  }


  /***
   * Writes a byte array to the stream.
   * <p>
   * @param buffer  The byte array to write.
   * @exception IOException If an error occurs while writing to the underlying
   *            stream.
   ***/
  public synchronized void write(byte buffer[]) throws IOException {
    write(buffer, 0, buffer.length);
  }                 


  /***
   * Writes a number of bytes from a byte array to the stream starting from
   * a given offset.
   * <p>
   * @param buffer  The byte array to write.
   * @param offset  The offset into the array at which to start copying data.
   * @param length  The number of bytes to write.
   * @exception IOException If an error occurs while writing to the underlying
   *            stream.
   ***/
  public synchronized void write(byte buffer[], int offset, int length)
       throws IOException
  {
    while(length-- > 0)
      write(buffer[offset++]);
  }

}
