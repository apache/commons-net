/***
 * $Id: Util.java,v 1.1 2002/04/03 01:04:41 brekke Exp $
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
 * The Util class cannot be instantiated and stores short static convenience
 * methods that are often quite useful.
 * <p>
 * <p>
 * @see CopyStreamException
 * @see CopyStreamListener
 * @see CopyStreamAdapter
 * @author Daniel F. Savarese
 ***/

public final class Util {
  /***
   * The default buffer size used by <a href="#copyStream"> copyStream </a>
   * and <a href="#copyReader"> copyReader </a>. It's value is 1024.
   ***/
  public static final int DEFAULT_COPY_BUFFER_SIZE = 1024;

  // Cannot be instantiated
  private Util() { }


  /***
   * Copies the contents of an InputStream to an OutputStream using a
   * copy buffer of a given size and notifies the provided
   * CopyStreamListener of the progress of the copy operation by calling
   * its bytesTransferred(long, int) method after each write to the 
   * destination.  If you wish to notify more than one listener you should
   * use a CopyStreamAdapter as the listener and register the additional
   * listeners with the CopyStreamAdapter.
   * <p>
   * The contents of the InputStream are
   * read until the end of the stream is reached, but neither the
   * source nor the destination are closed.  You must do this yourself
   * outside of the method call.  The number of bytes read/written is
   * returned.
   * <p>
   * @param source  The source InputStream.
   * @param dest    The destination OutputStream.
   * @param bufferSize  The number of bytes to buffer during the copy.
   * @param streamSize  The number of bytes in the stream being copied.
   *          Should be set to CopyStreamEvent.UNKNOWN_STREAM_SIZE if unknown.
   * @param listener  The CopyStreamListener to notify of progress.  If
   *      this parameter is null, notification is not attempted.
   * @exception CopyStreamException  If an error occurs while reading from the
   *            source or writing to the destination.  The CopyStreamException
   *            will contain the number of bytes confirmed to have been
   *            transferred before an
   *            IOException occurred, and it will also contain the IOException
   *            that caused the error.  These values can be retrieved with
   *            the CopyStreamException getTotalBytesTransferred() and
   *            getIOException() methods.
   ***/
  public static final long copyStream(InputStream source, OutputStream dest,
				      int bufferSize, long streamSize,
				      CopyStreamListener listener)
       throws CopyStreamException
  {
    int bytes;
    long total;
    byte[] buffer;

    buffer = new byte[bufferSize];
    total  = 0;

    try {
      while((bytes = source.read(buffer)) != -1) {
	// Technically, some read(byte[]) methods may return 0 and we cannot
	// accept that as an indication of EOF.

	if(bytes == 0) {
	  bytes = source.read();
	  if(bytes < 0)
	    break;
	  dest.write(bytes);
	  dest.flush();
	  ++total;
	  if(listener != null)
	    listener.bytesTransferred(total, 1, streamSize);
	  continue;
	}

	dest.write(buffer, 0, bytes);
	dest.flush();
	total+=bytes;
	if(listener != null)
	  listener.bytesTransferred(total, bytes, streamSize);
      }
    } catch(IOException e) {
      throw new CopyStreamException("IOException caught while copying.",
				    total, e);
    } 

    return total;
  }


  /***
   * Copies the contents of an InputStream to an OutputStream using a
   * copy buffer of a given size.  The contents of the InputStream are
   * read until the end of the stream is reached, but neither the
   * source nor the destination are closed.  You must do this yourself
   * outside of the method call.  The number of bytes read/written is
   * returned.
   * <p>
   * @param source  The source InputStream.
   * @param dest    The destination OutputStream.
   * @return  The number of bytes read/written in the copy operation.
   * @exception CopyStreamException  If an error occurs while reading from the
   *            source or writing to the destination.  The CopyStreamException
   *            will contain the number of bytes confirmed to have been
   *            transferred before an
   *            IOException occurred, and it will also contain the IOException
   *            that caused the error.  These values can be retrieved with
   *            the CopyStreamException getTotalBytesTransferred() and
   *            getIOException() methods.
   ***/
  public static final long copyStream(InputStream source, OutputStream dest,
				     int bufferSize)
       throws CopyStreamException
  {
    return copyStream(source, dest, bufferSize,
		      CopyStreamEvent.UNKNOWN_STREAM_SIZE, null);
  }


  /***
   * Same as <code> copyStream(source, dest, DEFAULT_COPY_BUFFER_SIZE); </code>
   ***/
  public static final long copyStream(InputStream source, OutputStream dest)
       throws CopyStreamException
  {
    return copyStream(source, dest, DEFAULT_COPY_BUFFER_SIZE);
  }


  /***
   * Copies the contents of a Reader to a Writer using a
   * copy buffer of a given size and notifies the provided
   * CopyStreamListener of the progress of the copy operation by calling
   * its bytesTransferred(long, int) method after each write to the 
   * destination.  If you wish to notify more than one listener you should
   * use a CopyStreamAdapter as the listener and register the additional
   * listeners with the CopyStreamAdapter.
   * <p> 
   * The contents of the Reader are
   * read until its end is reached, but neither the source nor the
   * destination are closed.  You must do this yourself outside of the
   * method call.  The number of characters read/written is returned.
   * <p>
   * @param source  The source Reader.
   * @param dest    The destination writer.
   * @param bufferSize  The number of characters to buffer during the copy.
   * @param streamSize  The number of characters in the stream being copied.
   *          Should be set to CopyStreamEvent.UNKNOWN_STREAM_SIZE if unknown.
   * @param listener  The CopyStreamListener to notify of progress.  If
   *      this parameter is null, notification is not attempted.
   * @return  The number of characters read/written in the copy operation.
   * @exception CopyStreamException  If an error occurs while reading from the
   *            source or writing to the destination.  The CopyStreamException
   *            will contain the number of bytes confirmed to have been
   *            transferred before an
   *            IOException occurred, and it will also contain the IOException
   *            that caused the error.  These values can be retrieved with
   *            the CopyStreamException getTotalBytesTransferred() and
   *            getIOException() methods.
   ***/
  public static final long copyReader(Reader source, Writer dest, 
				      int bufferSize, long streamSize,
				      CopyStreamListener listener)
       throws CopyStreamException
  {
    int chars;
    long total;
    char[] buffer;

    buffer = new char[bufferSize];
    total  = 0;

    try {
      while((chars = source.read(buffer)) != -1) {
	// Technically, some read(char[]) methods may return 0 and we cannot
	// accept that as an indication of EOF.
	if(chars == 0) {
	  chars = source.read();
	  if(chars < 0)
	    break;
	  dest.write(chars);
	  dest.flush();
	  ++total;
	  if(listener != null)
	    listener.bytesTransferred(total, chars, streamSize);
	  continue;
	}

	dest.write(buffer, 0, chars);
	dest.flush();
	total+=chars;
	if(listener != null)
	  listener.bytesTransferred(total, chars, streamSize);
      }
    } catch(IOException e) {
      throw new CopyStreamException("IOException caught while copying.",
				    total, e);
    } 

    return total;
  }


  /***
   * Copies the contents of a Reader to a Writer using a
   * copy buffer of a given size.  The contents of the Reader are
   * read until its end is reached, but neither the source nor the
   * destination are closed.  You must do this yourself outside of the
   * method call.  The number of characters read/written is returned.
   * <p>
   * @param source  The source Reader.
   * @param dest    The destination writer.
   * @param bufferSize  The number of characters to buffer during the copy.
   * @return  The number of characters read/written in the copy operation.
   * @exception CopyStreamException  If an error occurs while reading from the
   *            source or writing to the destination.  The CopyStreamException
   *            will contain the number of bytes confirmed to have been
   *            transferred before an
   *            IOException occurred, and it will also contain the IOException
   *            that caused the error.  These values can be retrieved with
   *            the CopyStreamException getTotalBytesTransferred() and
   *            getIOException() methods.
   ***/
  public static final long copyReader(Reader source, Writer dest, 
				      int bufferSize)
       throws CopyStreamException
  {
    return copyReader(source, dest, bufferSize,
		      CopyStreamEvent.UNKNOWN_STREAM_SIZE, null);
  }


  /***
   * Same as <code> copyReader(source, dest, DEFAULT_COPY_BUFFER_SIZE); </code>
   ***/
  public static final long copyReader(Reader source, Writer dest)
       throws CopyStreamException
  {
    return copyReader(source, dest, DEFAULT_COPY_BUFFER_SIZE);
  }

}
