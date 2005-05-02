/*
 * Copyright 2001-2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.io;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;

/**
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
 * @author <a href="mailto:savarese@apache.org">Daniel F. Savarese</a>
 * @version $Id$
 */
public final class DotTerminatedMessageReader extends Reader
{
    private static final String LS;
    private static final char[] LS_CHARS;

    static
    {
        LS = System.getProperty("line.separator");
        LS_CHARS = LS.toCharArray();
    }

    private boolean atBeginning;
    private boolean eof;
    private int pos;
    private char[] internalBuffer;
    private PushbackReader internalReader;

    /**
     * Creates a DotTerminatedMessageReader that wraps an existing Reader
     * input source.
     * @param reader  The Reader input source containing the message.
     */
    public DotTerminatedMessageReader(Reader reader)
    {
        super(reader);
        internalBuffer = new char[LS_CHARS.length + 3];
        pos = internalBuffer.length;
        // Assumes input is at start of message
        atBeginning = true;
        eof = false;
        internalReader = new PushbackReader(reader);
    }

    /**
     * Reads and returns the next character in the message.  If the end of the
     * message has been reached, returns -1.  Note that a call to this method
     * may result in multiple reads from the underlying input stream to decode
     * the message properly (removing doubled dots and so on).  All of
     * this is transparent to the programmer and is only mentioned for
     * completeness.
     * @return The next character in the message. Returns -1 if the end of the
     *          message has been reached.
     * @exception IOException If an error occurs while reading the underlying
     *            stream.
     */
    public int read() throws IOException
    {
        int ch;

        synchronized (lock)
        {
            if (pos < internalBuffer.length)
            {
                return internalBuffer[pos++];
            }

            if (eof)
            {
                return -1;
            }

            if ((ch = internalReader.read()) == -1)
            {
                eof = true;
                return -1;
            }

            if (atBeginning)
            {
                atBeginning = false;
                if (ch == '.')
                {
                    ch = internalReader.read();

                    if (ch != '.')
                    {
                        // read newline
                        eof = true;
                        internalReader.read();
                        return -1;
                    }
                    else
                    {
                        return '.';
                    }
                }
            }

            if (ch == '\r')
            {
                ch = internalReader.read();

                if (ch == '\n')
                {
                    ch = internalReader.read();

                    if (ch == '.')
                    {
                        ch = internalReader.read();

                        if (ch != '.')
                        {
                            // read newline and indicate end of file
                            internalReader.read();
                            eof = true;
                        }
                        else
                        {
                            internalBuffer[--pos] = (char) ch;
                        }
                    }
                    else
                    {
                        internalReader.unread(ch);
                    }

                    pos -= LS_CHARS.length;
                    System.arraycopy(LS_CHARS, 0, internalBuffer, pos,
                                     LS_CHARS.length);
                    ch = internalBuffer[pos++];
                }
                else
                {
                    internalBuffer[--pos] = (char) ch;
                    return '\r';
                }
            }

            return ch;
        }
    }

    /**
     * Reads the next characters from the message into an array and
     * returns the number of characters read.  Returns -1 if the end of the
     * message has been reached.
     * @param buffer  The character array in which to store the characters.
     * @return The number of characters read. Returns -1 if the
     *          end of the message has been reached.
     * @exception IOException If an error occurs in reading the underlying
     *            stream.
     */
    public int read(char[] buffer) throws IOException
    {
        return read(buffer, 0, buffer.length);
    }

    /**
     * Reads the next characters from the message into an array and
     * returns the number of characters read.  Returns -1 if the end of the
     * message has been reached.  The characters are stored in the array
     * starting from the given offset and up to the length specified.
     * @param buffer  The character array in which to store the characters.
     * @param offset   The offset into the array at which to start storing
     *              characters.
     * @param length   The number of characters to read.
     * @return The number of characters read. Returns -1 if the
     *          end of the message has been reached.
     * @exception IOException If an error occurs in reading the underlying
     *            stream.
     */
    public int read(char[] buffer, int offset, int length) throws IOException
    {
        int ch, off;
        synchronized (lock)
        {
            if (length < 1)
            {
                return 0;
            }
            if ((ch = read()) == -1)
            {
                return -1;
            }
            off = offset;

            do
            {
                buffer[offset++] = (char) ch;
            }
            while (--length > 0 && (ch = read()) != -1);

            return (offset - off);
        }
    }

    /**
     * Determines if the message is ready to be read.
     * @return True if the message is ready to be read, false if not.
     * @exception IOException If an error occurs while checking the underlying
     *            stream.
     */
    public boolean ready() throws IOException
    {
        synchronized (lock)
        {
            return (pos < internalBuffer.length || internalReader.ready());
        }
    }

    /**
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
     * @exception IOException  If an error occurs while reading the
     *            underlying stream.
     */
    public void close() throws IOException
    {
        synchronized (lock)
        {
            if (internalReader == null)
            {
                return;
            }

            if (!eof)
            {
                while (read() != -1)
                {
                    ;
                }
            }
            eof = true;
            atBeginning = false;
            pos = internalBuffer.length;
            internalReader = null;
        }
    }
}
