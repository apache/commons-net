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

package org.apache.commons.net.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "deprecation" })
public class Base64Test {

    private void checkDecoders(final String expected, final byte[] actual) {
        final byte[] decoded = Base64.decodeBase64(actual);
        assertEquals(expected, new String(decoded, StandardCharsets.UTF_8));
        assertEquals(expected, new String(java.util.Base64.getDecoder().decode(actual), StandardCharsets.UTF_8));
    }

    private void checkDecoders(final String expected, final String actual) {
        final byte[] decoded = Base64.decodeBase64(actual);
        assertEquals(expected, new String(decoded));
        assertEquals(expected, new String(decoded, StandardCharsets.UTF_8));
        assertEquals(expected, new String(java.util.Base64.getDecoder().decode(actual), StandardCharsets.UTF_8));
    }

    @Test
    public void testBase64() {
        final Base64 b64 = new Base64();
        assertFalse(b64.isUrlSafe());
    }

    @Test
    public void testBase64Boolean() {
        final Base64 b64 = new Base64(true);
        assertTrue(b64.isUrlSafe());
        assertArrayEquals(new byte[] { '\r', '\n' }, b64.getLineSeparator());
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
        final Base64 b64;
        b64 = new Base64(8, new byte[] {});
        assertFalse(b64.isUrlSafe());
        assertArrayEquals(new byte[] {}, b64.getLineSeparator());
    }

    @Test
    public void testBase64IntByteArrayBoolean() {
        Base64 b64;
        b64 = new Base64(8, new byte[] {}, false);
        assertFalse(b64.isUrlSafe());
        b64 = new Base64(8, new byte[] {}, true);
        assertTrue(b64.isUrlSafe());
    }

    @Test
    public void testDecodeBase64ByteArray() {
        checkDecoders("light w", new byte[] {'b', 'G', 'l', 'n', 'a', 'H', 'Q', 'g', 'd', 'w', '=', '='});
    }

    @Test
    public void testDecodeBase64String() {
        checkDecoders("light w", "bGlnaHQgdw==");
    }

    @Test
    public void testDecodeByteArray() {
        checkDecoders("foobar", new byte[] {'Z', 'm', '9', 'v', 'Y', 'm', 'F', 'y'});
    }

    @Test
    public void testDecodeByteArrayEmpty() {
        checkDecoders("", new byte[] {});

    }

    @Test
    public void testDecodeByteArrayNull() {
        assertNull(new Base64().decode((byte[]) null));
    }

    @Test
    @Disabled
    public void testDecodeInteger() {
        fail("Not yet implemented");
    }

    @Test
    public void testDecodeNullString() {
        final Base64 base64 = new Base64();
        assertThrows(NullPointerException.class, () -> base64.decode((String) null));
    }

    @Test
    @Disabled
    public void testDecodeObject() {
        fail("Not yet implemented");
    }

    @Test
    public void testDecodeString() {
        checkDecoders("Hello World!", "SGVsbG8gV29ybGQh");
    }

    @Test
    public void testEncodeBase64ByteArray() {
        final byte[] binaryData = null;
        assertArrayEquals(binaryData, Base64.encodeBase64(binaryData));
    }

    @Test
    @Disabled
    public void testEncodeBase64ByteArrayBoolean() {
        fail("Not yet implemented");
    }

    @Test
    @Disabled
    public void testEncodeBase64ByteArrayBooleanBoolean() {
        fail("Not yet implemented");
    }

    @Test
    public void testEncodeBase64ByteArrayBooleanBooleanInt() {
        final byte[] binaryData = { '1', '2', '3' };
        byte[] encoded;
        encoded = Base64.encodeBase64(binaryData, false, false);
        assertNotNull(encoded);
        assertEquals(4, encoded.length);
        assertThrows(IllegalArgumentException.class, () -> Base64.encodeBase64(binaryData, false, false, 3));
        encoded = Base64.encodeBase64(binaryData, false, false, 4); // NET-483
        assertNotNull(encoded);
        assertEquals(4, encoded.length);
        encoded = Base64.encodeBase64(binaryData, true, false);
        assertNotNull(encoded);
        assertEquals(6, encoded.length); // always adds trailer
        assertThrows(IllegalArgumentException.class, () -> Base64.encodeBase64(binaryData, true, false, 5));
        encoded = Base64.encodeBase64(binaryData, true, false, 6);
        assertNotNull(encoded);
        assertEquals(6, encoded.length);
    }

    @Test
    public void testEncodeBase64Chunked() {
        final byte[] bytesToEncode = {'f', 'o', 'o', 'b', 'a', 'r'};
        final byte[] encodedData = Base64.encodeBase64Chunked(bytesToEncode);
        assertEquals("Zm9vYmFy\r\n", new String(encodedData, StandardCharsets.UTF_8));
    }

    @Test
    public void testEncodeBase64StringByteArray() {
        final String stringToEncode = "Many hands make light work.";
        final String encodedData = Base64.encodeBase64String(stringToEncode.getBytes());
        assertEquals("TWFueSBoYW5kcyBtYWtlIGxpZ2h0IHdvcmsu\r\n", encodedData);
    }

    @Test
    public void testEncodeBase64StringByteArrayBoolean() {
        final byte[] bytesToEncode = "light work.".getBytes();
        final String chunkedResult = Base64.encodeBase64String(bytesToEncode, true);
        assertEquals("bGlnaHQgd29yay4=\r\n", chunkedResult);
        final String unchunkedResult = Base64.encodeBase64String(bytesToEncode, false);
        assertEquals("bGlnaHQgd29yay4=", unchunkedResult);
    }

    @Test
    public void testEncodeBase64StringUnChunked() {
        final byte[] bytesToEncode = "Many hands make light work.".getBytes();
        final String encodedData = Base64.encodeBase64StringUnChunked(bytesToEncode);
        assertEquals("TWFueSBoYW5kcyBtYWtlIGxpZ2h0IHdvcmsu", encodedData);
    }

    @Test
    @Disabled
    public void testEncodeBase64URLSafe() {
        fail("Not yet implemented");
    }

    @Test
    @Disabled
    public void testEncodeBase64URLSafeString() {
        fail("Not yet implemented");
    }

    @Test
    @Disabled
    public void testEncodeByteArray() {
        fail("Not yet implemented");
    }

    @Test
    @Disabled
    public void testEncodeInteger() {
        fail("Not yet implemented");
    }

    @Test
    @Disabled
    public void testEncodeObject() {
        fail("Not yet implemented");
    }

    @Test
    public void testEncodeToString() {
        final Base64 base64 = new Base64();
        final byte[] bytesToEncode = {'l', 'i', 'g', 'h', 't', ' ', 'w', 'o', 'r'};
        assertEquals("bGlnaHQgd29y\r\n", base64.encodeToString(bytesToEncode));
    }

    @Test
    public void testIsArrayByteBase64() {
        assertTrue(Base64.isArrayByteBase64(new byte[] { 'b', ' ' }));
        assertFalse(Base64.isArrayByteBase64(new byte[] { '?' }));
    }

    @Test
    public void testIsBase64() {
        assertTrue(Base64.isBase64((byte) 'b'));
        assertFalse(Base64.isBase64((byte) ' '));
    }

    @Test
    @Disabled
    public void testToIntegerBytes() {
        fail("Not yet implemented");
    }

}
