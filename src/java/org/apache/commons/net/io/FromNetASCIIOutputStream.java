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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/***
 * This class wraps an output stream, replacing all occurrences
 * of &lt;CR&gt;&lt;LF&gt; (carriage return followed by a linefeed),
 * which is the NETASCII standard for representing a newline, with the
 * local line separator representation.  You would use this class to 
 * implement ASCII file transfers requiring conversion from NETASCII.
 * <p>
 * Because of the translation process, a call to <code>flush()</code> will
 * not flush the last byte written if that byte was a carriage
 * return.  A call to <a href="#close"> close() </a>, however, will
 * flush the carriage return.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class FromNetASCIIOutputStream extends FilterOutputStream
{
    private boolean __lastWasCR;

    /***
     * Creates a FromNetASCIIOutputStream instance that wraps an existing
     * OutputStream.
     * <p>
     * @param output  The OutputStream to wrap.
     ***/
    public FromNetASCIIOutputStream(OutputStream output)
    {
        super(output);
        __lastWasCR = false;
    }


    private void __write(int ch) throws IOException
    {
        switch (ch)
        {
        case '\r':
            __lastWasCR = true;
            // Don't write anything.  We need to see if next one is linefeed
            break;
        case '\n':
            if (__lastWasCR)
            {
                out.write(FromNetASCIIInputStream._lineSeparatorBytes);
                __lastWasCR = false;
                break;
            }
            __lastWasCR = false;
            out.write('\n');
            break;
        default:
            if (__lastWasCR)
            {
                out.write('\r');
                __lastWasCR = false;
            }
            out.write(ch);
            break;
        }
    }


    /***
     * Writes a byte to the stream.    Note that a call to this method
     * might not actually write a byte to the underlying stream until a
     * subsequent character is written, from which it can be determined if
     * a NETASCII line separator was encountered.
     * This is transparent to the programmer and is only mentioned for
     * completeness.
     * <p>
     * @param ch The byte to write.
     * @exception IOException If an error occurs while writing to the underlying
     *            stream.
     ***/
    public synchronized void write(int ch)
    throws IOException
    {
        if (FromNetASCIIInputStream._noConversionRequired)
        {
            out.write(ch);
            return ;
        }

        __write(ch);
    }


    /***
     * Writes a byte array to the stream.
     * <p>
     * @param buffer  The byte array to write.
     * @exception IOException If an error occurs while writing to the underlying
     *            stream.
     ***/
    public synchronized void write(byte buffer[])
    throws IOException
    {
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
        if (FromNetASCIIInputStream._noConversionRequired)
        {
            // FilterOutputStream method is very slow.
            //super.write(buffer, offset, length);
            out.write(buffer, offset, length);
            return ;
        }

        while (length-- > 0)
            __write(buffer[offset++]);
    }


    /***
     * Closes the stream, writing all pending data.
     * <p>
     * @exception IOException  If an error occurs while closing the stream.
     ***/
    public synchronized void close()
    throws IOException
    {
        if (FromNetASCIIInputStream._noConversionRequired)
        {
            super.close();
            return ;
        }

        if (__lastWasCR)
            out.write('\r');
        super.close();
    }
}
