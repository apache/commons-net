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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

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

public final class ToNetASCIIInputStream extends FilterInputStream
{
    private static final int __NOTHING_SPECIAL = 0;
    private static final int __LAST_WAS_CR = 1;
    private static final int __LAST_WAS_NL = 2;
    private int __status;

    /***
     * Creates a ToNetASCIIInputStream instance that wraps an existing
     * InputStream.
     * <p>
     * @param input  The InputStream to .
     ***/
    public ToNetASCIIInputStream(InputStream input)
    {
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
    public int read() throws IOException
    {
        int ch;

        if (__status == __LAST_WAS_NL)
        {
            __status = __NOTHING_SPECIAL;
            return '\n';
        }

        ch = in.read();

        switch (ch)
        {
        case '\r':
            __status = __LAST_WAS_CR;
            return '\r';
        case '\n':
            if (__status != __LAST_WAS_CR)
            {
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

        if (length > ch)
            length = ch;

        // If nothing is available, block to read only one character
        if (length < 1)
            length = 1;

        if ((ch = read()) == -1)
            return -1;

        off = offset;

        do
        {
            buffer[offset++] = (byte)ch;
        }
        while (--length > 0 && (ch = read()) != -1);

        return (offset - off);
    }

    /*** Returns false.  Mark is not supported. ***/
    public boolean markSupported()
    {
        return false;
    }

    public int available() throws IOException
    {
        int result;

        result = in.available();

        if (__status == __LAST_WAS_NL)
            return (result + 1);

        return result;
    }
}
