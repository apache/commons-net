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
import java.io.PushbackInputStream;

/***
 * This class wraps an input stream, replacing all occurrences
 * of &lt;CR&gt;&lt;LF&gt; (carriage return followed by a linefeed),
 * which is the NETASCII standard for representing a newline, with the
 * local line separator representation.  You would use this class to 
 * implement ASCII file transfers requiring conversion from NETASCII.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class FromNetASCIIInputStream extends PushbackInputStream
{
    static final boolean _noConversionRequired;
    static final String _lineSeparator;
    static final byte[] _lineSeparatorBytes;

    static {
        _lineSeparator = System.getProperty("line.separator");
        _noConversionRequired = _lineSeparator.equals("\r\n");
        _lineSeparatorBytes = _lineSeparator.getBytes();
    }

    private int __length = 0;

    /***
     * Returns true if the NetASCII line separator differs from the system
     * line separator, false if they are the same.  This method is useful
     * to determine whether or not you need to instantiate a
     * FromNetASCIIInputStream object.
     * <p>
     * @return True if the NETASCII line separator differs from the local
     *   system line separator, false if they are the same.
     ***/
    public static final boolean isConversionRequired()
    {
        return !_noConversionRequired;
    }

    /***
     * Creates a FromNetASCIIInputStream instance that wraps an existing
     * InputStream.
     ***/
    public FromNetASCIIInputStream(InputStream input)
    {
        super(input, _lineSeparatorBytes.length + 1);
    }


    private int __read() throws IOException
    {
        int ch;

        ch = super.read();

        if (ch == '\r')
        {
            ch = super.read();
            if (ch == '\n')
            {
                unread(_lineSeparatorBytes);
                ch = super.read();
                // This is a kluge for read(byte[], ...) to read the right amount
                --__length;
            }
            else
            {
                if (ch != -1)
                    unread(ch);
                return '\r';
            }
        }

        return ch;
    }


    /***
     * Reads and returns the next byte in the stream.  If the end of the
     * message has been reached, returns -1.  Note that a call to this method
     * may result in multiple reads from the underlying input stream in order
     * to convert NETASCII line separators to the local line separator format.
     * This is transparent to the programmer and is only mentioned for
     * completeness.
     * <p>
     * @return The next character in the stream. Returns -1 if the end of the
     *          stream has been reached.
     * @exception IOException If an error occurs while reading the underlying
     *            stream.
     ***/
    public int read() throws IOException
    {
        if (_noConversionRequired)
            return super.read();

        return __read();
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
    public int read(byte buffer[]) throws IOException
    {
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
    public int read(byte buffer[], int offset, int length) throws IOException
    {
        int ch, off;

        if (length < 1)
            return 0;

        ch = available();

        __length = (length > ch ? ch : length);

        // If nothing is available, block to read only one character
        if (__length < 1)
            __length = 1;

        if (_noConversionRequired)
            return super.read(buffer, offset, __length);

        if ((ch = __read()) == -1)
            return -1;

        off = offset;

        do
        {
            buffer[offset++] = (byte)ch;
        }
        while (--__length > 0 && (ch = __read()) != -1);


        return (offset - off);
    }


    // PushbackInputStream in JDK 1.1.3 returns the wrong thing
    /***
     * Returns the number of bytes that can be read without blocking EXCEPT
     * when newline conversions have to be made somewhere within the
     * available block of bytes.  In other words, you really should not
     * rely on the value returned by this method if you are trying to avoid 
     * blocking.
     ***/
    public int available() throws IOException
    {
        return (buf.length - pos) + in.available();
    }

}
