/***
 * $Id: ToNetASCIIInputStream.java,v 1.1 2002/04/03 01:04:41 brekke Exp $
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
 * This class wraps an input stream, replacing all singly occurring
 * &lt;LF&gt; (linefeed) characters with &lt;CR&gt;&lt;LF&gt; (carriage return
 * followed by linefeed), which is the NETASCII standard for representing
 * a newline.
 * You would use this class to implement ASCII file transfers requiring
 * conversion to NETASCII.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class ToNetASCIIInputStream extends FilterInputStream {
  private static final int __NOTHING_SPECIAL = 0;
  private static final int __LAST_WAS_CR     = 1;
  private static final int __LAST_WAS_NL     = 2;
  private int __status;

  /***
   * Creates a ToNetASCIIInputStream instance that wraps an existing
   * InputStream.
   * <p>
   * @param input  The InputStream to .
   ***/
  public ToNetASCIIInputStream(InputStream input) {
    super(input);
    __status = __NOTHING_SPECIAL;
  }


  /***
   * Reads and returns the next byte in the stream.  If the end of the
   * message has been reached, returns -1.
   * <p>
   * @return The next character in the stream. Returns -1 if the end of the
   *          stream has been reached.
   * @exception IOException If an error occurs while reading the underlying
   *            stream.
   ***/
  public int read() throws IOException {
    int ch;

    if(__status == __LAST_WAS_NL) {
      __status = __NOTHING_SPECIAL;
      return '\n';
    }

    ch = in.read();

    switch(ch) {
    case '\r':
      __status = __LAST_WAS_CR;
      return '\r';
    case '\n':
      if(__status != __LAST_WAS_CR) {
	__status = __LAST_WAS_NL;
	return '\r';
      }
      // else fall through
    default:
      __status = __NOTHING_SPECIAL;
      return ch;
    }
    // statement not reached
    //return ch;
  }


  /***
   * Reads the next number of bytes from the stream into an array and
   * returns the number of bytes read.  Returns -1 if the end of the
   * stream has been reached.
   * <p>
   * @param buffer  The byte array in which to store the data.
   * @return The number of bytes read. Returns -1 if the
   *          end of the message has been reached.
   * @exception IOException If an error occurs in reading the underlying
   *            stream.
   ***/
  public int read(byte buffer[]) throws IOException {
    return read(buffer, 0, buffer.length);
  }                 

 
  /***
   * Reads the next number of bytes from the stream into an array and returns
   * the number of bytes read.  Returns -1 if the end of the
   * message has been reached.  The characters are stored in the array
   * starting from the given offset and up to the length specified.
   * <p>
   * @param buffer The byte array in which to store the data.
   * @param offset  The offset into the array at which to start storing data.
   * @param length   The number of bytes to read.
   * @return The number of bytes read. Returns -1 if the
   *          end of the stream has been reached.
   * @exception IOException If an error occurs while reading the underlying
   *            stream.
   ***/
  public int read(byte buffer[], int offset, int length) throws IOException {
    int ch, off;

    if(length < 1)
      return 0;

    ch = available();

    if(length > ch)
      length = ch;

    // If nothing is available, block to read only one character
    if(length < 1)
      length = 1;

    if((ch = read()) == -1)
      return -1;

    off = offset;

    do {
      buffer[offset++] = (byte)ch;
    } while(--length > 0 && (ch = read()) != -1);

    return (offset - off);
  }

  /*** Returns false.  Mark is not supported. ***/
  public boolean markSupported() { return false; }

  public int available() throws IOException {
    int result;

    result = in.available();

    if(__status == __LAST_WAS_NL)
      return (result + 1);

    return result;
  }
}
