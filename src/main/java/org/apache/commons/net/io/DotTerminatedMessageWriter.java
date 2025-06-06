/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.io;

import java.io.IOException;
import java.io.Writer;

/**
 * DotTerminatedMessageWriter is a class used to write messages to a server that are terminated by a single dot followed by a &lt;CR&gt;&lt;LF&gt; sequence and
 * with double dots appearing at the beginning of lines which do not signal end of message yet start with a dot. Various Internet protocols such as
 * NNTP and POP3 produce messages of this type.
 * <p>
 * This class handles the doubling of line-starting periods, converts single linefeeds to NETASCII newlines, and on closing will send the final message
 * terminator dot and NETASCII newline sequence.
 * </p>
 */
public final class DotTerminatedMessageWriter extends Writer {
    private static final int NOTHING_SPECIAL_STATE = 0;
    private static final int LAST_WAS_CR_STATE = 1;
    private static final int LAST_WAS_NL_STATE = 2;

    private int state;
    private Writer output;

    /**
     * Creates a DotTerminatedMessageWriter that wraps an existing Writer output destination.
     *
     * @param output The Writer output destination to write the message.
     */
    public DotTerminatedMessageWriter(final Writer output) {
        super(output);
        this.output = output;
        this.state = NOTHING_SPECIAL_STATE;
    }

    /**
     * Flushes the underlying output, writing all buffered output, but doesn't actually close the underlying stream. The underlying stream may still be used for
     * communicating with the server and therefore is not closed.
     *
     * @throws IOException If an error occurs while writing to the underlying output or closing the Writer.
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (output == null) {
                return;
            }

            if (state == LAST_WAS_CR_STATE) {
                output.write('\n');
            } else if (state != LAST_WAS_NL_STATE) {
                output.write("\r\n");
            }

            output.write(".\r\n");

            output.flush();
            output = null;
        }
    }

    /**
     * Flushes the underlying output, writing all buffered output.
     *
     * @throws IOException If an error occurs while writing to the underlying output.
     */
    @Override
    public void flush() throws IOException {
        synchronized (lock) {
            output.flush();
        }
    }

    /**
     * Writes a character array to the output.
     *
     * @param buffer The character array to write.
     * @throws IOException If an error occurs while writing to the underlying output.
     */
    @Override
    public void write(final char[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    /**
     * Writes a number of characters from a character array to the output starting from a given offset.
     *
     * @param buffer The character array to write.
     * @param offset The offset into the array at which to start copying data.
     * @param length The number of characters to write.
     * @throws IOException If an error occurs while writing to the underlying output.
     */
    @Override
    public void write(final char[] buffer, int offset, int length) throws IOException {
        synchronized (lock) {
            while (length-- > 0) {
                write(buffer[offset++]);
            }
        }
    }

    /**
     * Writes a character to the output. Note that a call to this method may result in multiple writes to the underling Writer in order to convert naked
     * linefeeds to NETASCII line separators and to double line-leading periods. This is transparent to the programmer and is only mentioned for completeness.
     *
     * @param ch The character to write.
     * @throws IOException If an error occurs while writing to the underlying output.
     */
    @Override
    public void write(final int ch) throws IOException {
        synchronized (lock) {
            switch (ch) {
            case '\r':
                state = LAST_WAS_CR_STATE;
                output.write('\r');
                return;
            case '\n':
                if (state != LAST_WAS_CR_STATE) {
                    output.write('\r');
                }
                output.write('\n');
                state = LAST_WAS_NL_STATE;
                return;
            case '.':
                // Double the dot at the beginning of a line
                if (state == LAST_WAS_NL_STATE) {
                    output.write('.');
                }
                // falls through$
            default:
                state = NOTHING_SPECIAL_STATE;
                output.write(ch);
            }
        }
    }

    /**
     * Writes a String to the output.
     *
     * @param string The String to write.
     * @throws IOException If an error occurs while writing to the underlying output.
     */
    @Override
    public void write(final String string) throws IOException {
        write(string.toCharArray());
    }

    /**
     * Writes part of a String to the output starting from a given offset.
     *
     * @param string The String to write.
     * @param offset The offset into the String at which to start copying data.
     * @param length The number of characters to write.
     * @throws IOException If an error occurs while writing to the underlying output.
     */
    @Override
    public void write(final String string, final int offset, final int length) throws IOException {
        write(string.toCharArray(), offset, length);
    }

}
