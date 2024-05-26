/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.io;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

//import java.nio.charset.Charset;

public class ToNetASCIIInputStreamTest {

    private void byteTest(final boolean byByte, final String input, final String expect) throws IOException {
        final byte[] data = input.getBytes(StandardCharsets.US_ASCII);
        final byte[] expected = expect.getBytes(StandardCharsets.US_ASCII);
        final InputStream source = new ByteArrayInputStream(data);
        try (final ToNetASCIIInputStream toNetASCII = new ToNetASCIIInputStream(source)) {
            final byte[] output = new byte[data.length * 2]; // cannot be longer than twice the input

            final int length = byByte ? getSingleBytes(toNetASCII, output) : getBuffer(toNetASCII, output);

            final byte[] result = new byte[length];
            System.arraycopy(output, 0, result, 0, length);
            assertArrayEquals(expected, result, "Failed converting " + input);
        }
    }

    private int getBuffer(final ToNetASCIIInputStream toNetASCII, final byte[] output) throws IOException {
        int length = 0;
        int remain = output.length;
        int chunk;
        int offset = 0;
        while (remain > 0 && (chunk = toNetASCII.read(output, offset, remain)) != -1) {
            length += chunk;
            offset += chunk;
            remain -= chunk;
        }
        return length;
    }

    private int getSingleBytes(final ToNetASCIIInputStream toNetASCII, final byte[] output) throws IOException {
        int b;
        int length = 0;
        while ((b = toNetASCII.read()) != -1) {
            output[length++] = (byte) b;
        }
        return length;
    }

    @Test
    public void testToNetASCIIInputStream_single_bytes() throws Exception {
        byteTest(true, "", "");
        byteTest(true, "\r", "\r");
        byteTest(true, "\n", "\r\n");
        byteTest(true, "a", "a");
        byteTest(true, "a\nb", "a\r\nb");
        byteTest(true, "a\r\nb", "a\r\nb");
        byteTest(true, "Hello\nWorld\n", "Hello\r\nWorld\r\n");
        byteTest(true, "Hello\nWorld\r\n", "Hello\r\nWorld\r\n");
        byteTest(true, "Hello\nWorld\n\r", "Hello\r\nWorld\r\n\r");
    }

    @Test
    public void testToNetASCIIInputStream1() throws Exception {
        byteTest(false, "", "");
        byteTest(false, "\r", "\r");
        byteTest(false, "a", "a");
        byteTest(false, "a\nb", "a\r\nb");
        byteTest(false, "a\r\nb", "a\r\nb");
        byteTest(false, "\n", "\r\n");
        byteTest(false, "Hello\nWorld\n", "Hello\r\nWorld\r\n");
        byteTest(false, "Hello\nWorld\r\n", "Hello\r\nWorld\r\n");
        byteTest(false, "Hello\nWorld\n\r", "Hello\r\nWorld\r\n\r");
    }

}
