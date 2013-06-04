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
 *
 */

package org.apache.commons.net.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.nio.charset.Charset;
import org.junit.Assert;
import org.junit.Test;

public class ToNetASCIIInputStreamTest {

    private static final String ASCII = /*Charset.forName*/("ASCII");

    @Test
    public void testToNetASCIIInputStream1() throws Exception
    {
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

    @Test
    public void testToNetASCIIInputStream_single_bytes() throws Exception
    {
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

    private void byteTest(boolean byByte, String input, String expect) throws IOException {
        byte[] data = input.getBytes(ASCII);
        byte[] expected = expect.getBytes(ASCII);
        InputStream source = new ByteArrayInputStream(data);
        ToNetASCIIInputStream toNetASCII = new ToNetASCIIInputStream(source);
        byte[] output = new byte[data.length*2]; // cannot be longer than twice the input

        int length = byByte ?
                getSingleBytes(toNetASCII, output) :
                    getBuffer(toNetASCII, output);

        byte[] result = new byte[length];
        System.arraycopy(output, 0, result, 0, length);
        Assert.assertArrayEquals("Failed converting "+input,expected, result);
        toNetASCII.close();
    }

    private int getSingleBytes(ToNetASCIIInputStream toNetASCII, byte[] output)
            throws IOException {
        int b;
        int length=0;
        while((b=toNetASCII.read()) != -1) {
            output[length++]=(byte)b;
        }
        return length;
    }

    private int getBuffer(ToNetASCIIInputStream toNetASCII, byte[] output)
            throws IOException {
        int length=0;
        int remain=output.length;
        int chunk;
        int offset=0;
        while(remain > 0 && (chunk=toNetASCII.read(output,offset,remain)) != -1){
            length+=chunk;
            offset+=chunk;
            remain-=chunk;
        }
        return length;
    }

}
