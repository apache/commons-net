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

package org.apache.commons.net.util;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

public class Base64Test {

    @Test
    public void testBase64() {
        Base64 b64 = new Base64();
        assertFalse(b64.isUrlSafe());
    }

    @Test
    public void testBase64Boolean() {
        Base64 b64 = new Base64(true);
        assertTrue(b64.isUrlSafe());
        assertTrue(Arrays.equals(new byte[]{'\r','\n'}, b64.getLineSeparator()));
    }

    @Test
    public void testBase64Int() {
        Base64 b64;
        b64 = new Base64(8);
        assertFalse(b64.isUrlSafe());
        assertEquals(8, b64.getLineLength());
        b64 = new Base64(11);
        assertEquals(8, b64.getLineLength());
    }

    @Test
    public void testBase64IntByteArray() {
        Base64 b64;
        b64 = new Base64(8, new byte[]{});
        assertFalse(b64.isUrlSafe());
        assertTrue(Arrays.equals(new byte[]{}, b64.getLineSeparator()));
    }

    @Test
    public void testBase64IntByteArrayBoolean() {
        Base64 b64;
        b64 = new Base64(8, new byte[]{}, false);
        assertFalse(b64.isUrlSafe());
        b64 = new Base64(8, new byte[]{}, true);
        assertTrue(b64.isUrlSafe());
    }

    @Test
    public void testIsBase64() {
        assertTrue(Base64.isBase64((byte)'b'));
        assertFalse(Base64.isBase64((byte)' '));
    }

    @Test
    public void testIsArrayByteBase64() {
        assertTrue(Base64.isArrayByteBase64(new byte[]{'b',' '}));
        assertFalse(Base64.isArrayByteBase64(new byte[]{'?'}));
    }

    @Test
    public void testEncodeBase64ByteArray() {
        byte[] binaryData=null;
        assertTrue(Arrays.equals(binaryData, Base64.encodeBase64(binaryData)));
    }

    @Test @Ignore
    public void testEncodeBase64StringByteArray() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testEncodeBase64StringUnChunked() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testEncodeBase64StringByteArrayBoolean() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testEncodeBase64URLSafe() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testEncodeBase64URLSafeString() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testEncodeBase64Chunked() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testDecodeObject() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testDecodeString() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testDecodeByteArray() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testEncodeBase64ByteArrayBoolean() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testEncodeBase64ByteArrayBooleanBoolean() {
        fail("Not yet implemented");
    }

    @Test
    public void testEncodeBase64ByteArrayBooleanBooleanInt() {
        byte[] binaryData = new byte[]{'1','2','3'};
        byte[] encoded;
        encoded = Base64.encodeBase64(binaryData, false, false);
        assertNotNull(encoded);
        assertEquals(4, encoded.length);
        try {
            Base64.encodeBase64(binaryData, false, false, 3);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
        encoded = Base64.encodeBase64(binaryData, false, false, 4); // NET-483
        assertNotNull(encoded);
        assertEquals(4, encoded.length);
        encoded = Base64.encodeBase64(binaryData, true, false);
        assertNotNull(encoded);
        assertEquals(6, encoded.length); // always adds trailer
        try {
            Base64.encodeBase64(binaryData, true, false, 5);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
        encoded = Base64.encodeBase64(binaryData, true, false, 6);
        assertNotNull(encoded);
        assertEquals(6, encoded.length);
    }

    @Test @Ignore
    public void testDecodeBase64String() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testDecodeBase64ByteArray() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testEncodeObject() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testEncodeToString() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testEncodeByteArray() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testDecodeInteger() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testEncodeInteger() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testToIntegerBytes() {
        fail("Not yet implemented");
    }

}
