package org.apache.commons.net.io;

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
 *    nor may "Apache" appear in their name, without
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

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

public final class Util
{
    /***
     * The default buffer size used by <a href="#copyStream"> copyStream </a>
     * and <a href="#copyReader"> copyReader </a>. It's value is 1024.
     ***/
    public static final int DEFAULT_COPY_BUFFER_SIZE = 1024;

    // Cannot be instantiated
    private Util()
    { }


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
        total = 0;

        try
        {
            while ((bytes = source.read(buffer)) != -1)
            {
                // Technically, some read(byte[]) methods may return 0 and we cannot
                // accept that as an indication of EOF.

                if (bytes == 0)
                {
                    bytes = source.read();
                    if (bytes < 0)
                        break;
                    dest.write(bytes);
                    dest.flush();
                    ++total;
                    if (listener != null)
                        listener.bytesTransferred(total, 1, streamSize);
                    continue;
                }

                dest.write(buffer, 0, bytes);
                dest.flush();
                total += bytes;
                if (listener != null)
                    listener.bytesTransferred(total, bytes, streamSize);
            }
        }
        catch (IOException e)
        {
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
        total = 0;

        try
        {
            while ((chars = source.read(buffer)) != -1)
            {
                // Technically, some read(char[]) methods may return 0 and we cannot
                // accept that as an indication of EOF.
                if (chars == 0)
                {
                    chars = source.read();
                    if (chars < 0)
                        break;
                    dest.write(chars);
                    dest.flush();
                    ++total;
                    if (listener != null)
                        listener.bytesTransferred(total, chars, streamSize);
                    continue;
                }

                dest.write(buffer, 0, chars);
                dest.flush();
                total += chars;
                if (listener != null)
                    listener.bytesTransferred(total, chars, streamSize);
            }
        }
        catch (IOException e)
        {
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
