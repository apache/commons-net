/***
 * $Id: DotTerminatedMessageReader.java,v 1.1 2002/04/03 01:04:41 brekke Exp $
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
import java.util.*;

/***
 * DotTerminatedMessageReader is a class used to read messages from a
 * server that are terminated by a single dot followed by a 
 * &lt;CR&gt;&lt;LF&gt;
 * sequence and with double dots appearing at the begining of lines which
 * do not signal end of message yet start with a dot.  Various Internet
 * protocols such as NNTP and POP3 produce messages of this type.
 * <p>
 * This class handles stripping of the duplicate period at the beginning
 * of lines starting with a period, converts NETASCII newlines to the
 * local line separator format, truncates the end of message indicator,
 * and ensures you cannot read past the end of the message.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class DotTerminatedMessageReader extends Reader {
  private static final String __lineSeparator;
  private static final char[] __lineSeparatorChars;

  static {
    __lineSeparator        = System.getProperty("line.separator");
    __lineSeparatorChars   = __lineSeparator.toCharArray();
  }

  private boolean __atBeginning, __eof;
  private int __pos;
  private char[] __buffer;
  private PushbackReader __in;

  /***
   * Creates a DotTerminatedMessageReader that wraps an existing Reader
   * input source.
   * <p>
   * @param reader  The Reader input source containing the message.
   ***/
  public DotTerminatedMessageReader(Reader reader) {
    super(reader);
    __buffer = new char[__lineSeparatorChars.length + 3];
    __pos    = __buffer.length;
    // Assumes input is at start of message
    __atBeginning = true;
    __eof         = false;
    __in = new PushbackReader(reader);
  }


  /***
   * Reads and returns the next character in the message.  If the end of the
   * message has been reached, returns -1.  Note that a call to this method
   * may result in multiple reads from the underlying input stream to decode
   * the message properly (removing doubled dots and so on).  All of
   * this is transparent to the programmer and is only mentioned for
   * completeness.
   * <p>
   * @return The next character in the message. Returns -1 if the end of the
   *          message has been reached.
   * @exception IOException If an error occurs while reading the underlying
   *            stream.
   ***/
  public int read() throws IOException {
    int ch;

    synchronized(lock) {
      if(__pos < __buffer.length)
	return __buffer[__pos++];

      if(__eof)
	return -1;

      if((ch = __in.read()) == -1) {
	__eof = true;
	return -1;
      }

      if(__atBeginning) {
	__atBeginning = false;
	if(ch == '.') {
	  ch = __in.read();

	  if(ch != '.') {
	    // read newline
	    __eof = true;
	    __in.read();
	    return -1;
	  } else
	    return '.';
	}
      }

      if(ch == '\r') {
	ch = __in.read();

	if(ch == '\n') {
	  ch = __in.read();

	  if(ch == '.') {
	    ch = __in.read();

	    if(ch != '.') {
	      // read newline and indicate end of file
	      __in.read();
	      __eof = true;
	    } else
	      __buffer[--__pos] = (char)ch;
	  } else
	    __in.unread(ch);

	  __pos-=__lineSeparatorChars.length;
	  System.arraycopy(__lineSeparatorChars, 0, __buffer, __pos,
			   __lineSeparatorChars.length);
	  ch = __buffer[__pos++];
	} else {
	  __buffer[--__pos] = (char)ch;
	  return '\r';
	}
      }

      return ch;
    }
  }


  /***
   * Reads the next characters from the message into an array and
   * returns the number of characters read.  Returns -1 if the end of the
   * message has been reached.
   * <p>
   * @param buffer  The character array in which to store the characters.
   * @return The number of characters read. Returns -1 if the
   *          end of the message has been reached.
   * @exception IOException If an error occurs in reading the underlying
   *            stream.
   ***/
  public int read(char[] buffer) throws IOException {
    return read(buffer, 0, buffer.length);
  }                 


  /***
   * Reads the next characters from the message into an array and
   * returns the number of characters read.  Returns -1 if the end of the
   * message has been reached.  The characters are stored in the array
   * starting from the given offset and up to the length specified.
   * <p>
   * @param bufffer  The character array in which to store the characters.
   * @param offset   The offset into the array at which to start storing
   *              characters.
   * @param length   The number of characters to read.
   * @return The number of characters read. Returns -1 if the
   *          end of the message has been reached.
   * @exception IOException If an error occurs in reading the underlying
   *            stream.
   ***/
  public int read(char[] buffer, int offset, int length) throws IOException {
    int ch, off;
    synchronized(lock) {
      if(length < 1)
	return 0;

      if((ch = read()) == -1)
	return -1;

      off = offset;

      do {
	buffer[offset++] = (char)ch;
      } while(--length > 0 && (ch = read()) != -1);

      return (offset - off);
    }
  }


  /***
   * Determines if the message is ready to be read.
   * <p>
   * @return True if the message is ready to be read, false if not.
   * @exception IOException If an error occurs while checking the underlying
   *            stream.
   ***/
  public boolean ready() throws IOException {
    synchronized(lock) {
      return (__pos < __buffer.length || __in.ready());
    }
  }


  /***
   * Closes the message for reading.  This doesn't actually close the
   * underlying stream.  The underlying stream may still be used for 
   * communicating with the server and therefore is not closed.
   * <p>
   * If the end of the message has not yet been reached, this method
   * will read the remainder of the message until it reaches the end,
   * so that the underlying stream may continue to be used properly
   * for communicating with the server.  If you do not fully read
   * a message, you MUST close it, otherwise your program will likely
   * hang or behave improperly.
   * <p>
   * @exception IOException  If an error occurs while reading the
   *            underlying stream.
   ***/
  public void close() throws IOException {
    synchronized(lock) {
      if(__in == null)
	return;

      if(!__eof)
	while(read() != -1);

      __eof         = true;
      __atBeginning = false;
      __pos         = __buffer.length;
      __in          = null;
    }
  }

}

