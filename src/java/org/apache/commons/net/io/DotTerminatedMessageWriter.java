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
import java.io.Writer;

/***
 * DotTerminatedMessageWriter is a class used to write messages to a
 * server that are terminated by a single dot followed by a
 * &lt;CR&gt;&lt;LF&gt;
 * sequence and with double dots appearing at the begining of lines which
 * do not signal end of message yet start with a dot.  Various Internet
 * protocols such as NNTP and POP3 produce messages of this type.
 * <p>
 * This class handles the doubling of line-starting periods,
 * converts single linefeeds to NETASCII newlines, and on closing
 * will send the final message terminator dot and NETASCII newline
 * sequence.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class DotTerminatedMessageWriter extends Writer
{
    private static final int __NOTHING_SPECIAL_STATE = 0;
    private static final int __LAST_WAS_CR_STATE = 1;
    private static final int __LAST_WAS_NL_STATE = 2;

    private int __state;
    private Writer __output;


    /***
     * Creates a DotTerminatedMessageWriter that wraps an existing Writer
     * output destination.
     * <p>
     * @param output  The Writer output destination to write the message.
     ***/
    public DotTerminatedMessageWriter(Writer output)
    {
        super(output);
        __output = output;
        __state = __NOTHING_SPECIAL_STATE;
    }


    /***
     * Writes a character to the output.  Note that a call to this method
     * may result in multiple writes to the underling Writer in order to
     * convert naked linefeeds to NETASCII line separators and to double
     * line-leading periods.  This is transparent to the programmer and
     * is only mentioned for completeness.
     * <p>
     * @param ch  The character to write.
     * @exception IOException  If an error occurs while writing to the
     *            underlying output.
     ***/
    public void write(int ch) throws IOException
    {
        synchronized (lock)
        {
            switch (ch)
            {
            case '\r':
                __state = __LAST_WAS_CR_STATE;
                __output.write('\r');
                return ;
            case '\n':
                if (__state != __LAST_WAS_CR_STATE)
                    __output.write('\r');
                __output.write('\n');
                __state = __LAST_WAS_NL_STATE;
                return ;
            case '.':
                // Double the dot at the beginning of a line
                if (__state == __LAST_WAS_NL_STATE)
                    __output.write('.');
                // Fall through
            default:
                __state = __NOTHING_SPECIAL_STATE;
                __output.write(ch);
                return ;
            }
        }
    }


    /***
     * Writes a number of characters from a character array to the output
     * starting from a given offset.
     * <p>
     * @param buffer  The character array to write.
     * @param offset  The offset into the array at which to start copying data.
     * @param length  The number of characters to write.
     * @exception IOException If an error occurs while writing to the underlying
     *            output.
     ***/
    public void write(char[] buffer, int offset, int length) throws IOException
    {
        synchronized (lock)
        {
            while (length-- > 0)
                write(buffer[offset++]);
        }
    }


    /***
     * Writes a character array to the output.
     * <p>
     * @param buffer  The character array to write.
     * @exception IOException If an error occurs while writing to the underlying
     *            output.
     ***/
    public void write(char[] buffer) throws IOException
    {
        write(buffer, 0, buffer.length);
    }


    /***
     * Writes a String to the output.
     * <p>
     * @param string  The String to write.
     * @exception IOException If an error occurs while writing to the underlying
     *            output.
     ***/
    public void write(String string) throws IOException
    {
        write(string.toCharArray());
    }


    /***
     * Writes part of a String to the output starting from a given offset.
     * <p>
     * @param string  The String to write.
     * @param offset  The offset into the String at which to start copying data.
     * @param length  The number of characters to write.
     * @exception IOException If an error occurs while writing to the underlying
     *            output.
     ***/
    public void write(String string, int offset, int length) throws IOException
    {
        write(string.toCharArray(), offset, length);
    }


    /***
     * Flushes the underlying output, writing all buffered output.
     * <p>
     * @exception IOException If an error occurs while writing to the underlying
     *            output.
     ***/
    public void flush() throws IOException
    {
        synchronized (lock)
        {
            __output.flush();
        }
    }


    /***
     * Flushes the underlying output, writing all buffered output, but doesn't
     * actually close the underlying stream.  The underlying stream may still
     * be used for communicating with the server and therefore is not closed.
     * <p>
     * @exception IOException If an error occurs while writing to the underlying
     *            output or closing the Writer.
     ***/
    public void close() throws IOException
    {
        synchronized (lock)
        {
            if (__output == null)
                return ;

            if (__state == __LAST_WAS_CR_STATE)
                __output.write('\n');
            else if (__state != __LAST_WAS_NL_STATE)
                __output.write("\r\n");

            __output.write(".\r\n");

            __output.flush();
            __output = null;
        }
    }

}
