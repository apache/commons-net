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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.apache.commons.lang3.ArrayFill;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

@SuppressWarnings({ "deprecation" })
public class Base64Test {

    private static String toString(final byte[] encodedData) {
        return encodedData != null ? new String(encodedData, StandardCharsets.UTF_8) : null;
    }

    private void checkDecoders(final String expected, final byte[] actual) {
        final byte[] decoded = Base64.decodeBase64(actual);
        assertEquals(expected, toString(decoded));
        assertEquals(expected, toString(actual != null ? getJreDecoder().decode(actual) : null));
        assertEquals(expected, toString(new Base64().decode(actual)));
    }

    private void checkDecoders(final String expected, final String actual) {
        final byte[] decoded = Base64.decodeBase64(actual);
        assertEquals(expected, new String(decoded));
        assertEquals(expected, toString(decoded));
        assertEquals(expected, toString(actual != null ? getJreDecoder().decode(actual) : null));
        assertEquals(expected, toString(new Base64().decode(actual)));
    }

    private Decoder getJreDecoder() {
        return java.util.Base64.getDecoder();
    }

    private Encoder getJreEncoder() {
        return java.util.Base64.getEncoder();
    }

    private Encoder getJreMimeEncoder() {
        return java.util.Base64.getMimeEncoder();
    }

    private Encoder getJreMimeEncoder(final int lineLength, final byte[] lineSeparator) {
        return java.util.Base64.getMimeEncoder(lineLength, lineSeparator);
    }

    private Encoder getJreUrlEncoder() {
        return java.util.Base64.getUrlEncoder();
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
        assertArrayEquals(new byte[] {}, b64.getLineSeparator());
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
        final String stringToEncode = "<<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>>";
        final byte[] encodedData = new Base64(Base64.CHUNK_SIZE, Base64.CHUNK_SEPARATOR).encode(stringToEncode.getBytes());
        assertEquals("PDw/Pz8+Pjw8Pz8/Pj48PD8/Pz4+PDw/Pz8+Pjw8Pz8/Pj48PD8/Pz4+PDw/Pz8+Pjw8Pz8/Pj48\r\nPD8/Pz4+PDw/Pz8+Pjw8Pz8/Pj4=\r\n", toString(encodedData));
        assertEquals(getJreMimeEncoder().encodeToString(stringToEncode.getBytes()) + "\r\n", toString(encodedData));
        assertEquals("PDw/Pz8+Pjw8Pz8/Pj48PD8/Pz4+PDw/Pz8+Pjw8Pz8/Pj48PD8/Pz4+PDw/Pz8+Pjw8Pz8/Pj48~PD8/Pz4+PDw/Pz8+Pjw8Pz8/Pj4=~",
                toString(new Base64(Base64.CHUNK_SIZE, "~".getBytes()).encode(stringToEncode.getBytes())));
        assertEquals(getJreMimeEncoder(Base64.CHUNK_SIZE, "~".getBytes()).encodeToString(stringToEncode.getBytes()) + "~",
                toString(new Base64(Base64.CHUNK_SIZE, "~".getBytes()).encode(stringToEncode.getBytes())));
        assertEquals(getJreMimeEncoder(Base64.CHUNK_SIZE - 2, "~~".getBytes()).encodeToString(stringToEncode.getBytes()) + "~~",
                toString(new Base64(Base64.CHUNK_SIZE - 2, "~~".getBytes()).encode(stringToEncode.getBytes())));
        assertEquals(getJreMimeEncoder(Base64.CHUNK_SIZE + 2, "~~~".getBytes()).encodeToString(stringToEncode.getBytes()) + "~~~",
                toString(new Base64(Base64.CHUNK_SIZE + 2, "~~~".getBytes()).encode(stringToEncode.getBytes())));
    }

    @Test
    public void testBase64IntByteArrayBoolean() {
        Base64 b64;
        b64 = new Base64(8, new byte[] {}, false);
        assertFalse(b64.isUrlSafe());
        b64 = new Base64(8, new byte[] {}, true);
        assertTrue(b64.isUrlSafe());
        assertThrows(IllegalArgumentException.class, () -> new Base64(8, new byte[] { 'A' }, false));
    }

    @Test
    public void testDecodeBase64ByteArray() {
        checkDecoders("light w", new byte[] { 'b', 'G', 'l', 'n', 'a', 'H', 'Q', 'g', 'd', 'w', '=', '=' });
    }

    @Test
    public void testDecodeBase64String() {
        checkDecoders("light w", "bGlnaHQgdw==");
    }

    @Test
    public void testDecodeByteArray() {
        checkDecoders("foobar", new byte[] { 'Z', 'm', '9', 'v', 'Y', 'm', 'F', 'y' });
    }

    @Test
    public void testDecodeByteArrayEmpty() {
        checkDecoders("", new byte[] {});
        checkDecoders(null, (byte[]) null);

    }

    @Test
    public void testDecodeByteArrayNull() {
        assertNull(new Base64().decode((byte[]) null));
    }

    @Test
    public void testDecodeInteger() {
        testDecodeInteger(BigInteger.ONE);
        testDecodeInteger(BigInteger.TEN);
        testDecodeInteger(BigInteger.ZERO);
    }

    private void testDecodeInteger(final BigInteger bi) {
        assertEquals(bi, Base64.decodeInteger(getJreEncoder().encode(bi.toByteArray())));
    }

    @Test
    public void testDecodeNullString() {
        final Base64 base64 = new Base64();
        assertThrows(NullPointerException.class, () -> base64.decode((String) null));
    }

    @Test
    public void testDecodeString() {
        checkDecoders("Hello World!", "SGVsbG8gV29ybGQh");
    }

    @Test
    public void testEncodeBase64ByteArrayBoolean() {
        final byte[] binaryData = { '1', '2', '3' };
        final byte[] urlUnsafeData = "<<???>>".getBytes();
        final byte[] urlUnsafeDataChunky = "<<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>>".getBytes();
        byte[] encoded;
        // Boolean parameter: "isChunked".
        //
        // isChunked false
        encoded = Base64.encodeBase64(binaryData, false);
        assertNotNull(encoded);
        assertEquals(4, encoded.length);
        assertEquals(Base64.getEncodeLength(binaryData, Base64.CHUNK_SIZE, Base64.CHUNK_SEPARATOR) - 2, encoded.length);
        assertEquals(getJreEncoder().encodeToString(binaryData), toString(encoded));
        assertEquals("MTIz", toString(encoded));
        // URL unsafe
        // <<???>>
        encoded = Base64.encodeBase64(urlUnsafeData, false);
        assertEquals("PDw/Pz8+Pg==", toString(encoded));
        encoded = Base64.encodeBase64(urlUnsafeDataChunky, false);
        assertEquals(getJreEncoder().encodeToString(urlUnsafeDataChunky), toString(encoded));
        //
        // isChunked false
        encoded = Base64.encodeBase64(binaryData, false);
        assertNotNull(encoded);
        assertEquals(4, encoded.length);
        assertEquals(Base64.getEncodeLength(binaryData, Base64.CHUNK_SIZE, Base64.CHUNK_SEPARATOR) - 2, encoded.length);
        assertEquals("MTIz", toString(encoded));
        //
        // isChunked true
        encoded = Base64.encodeBase64(binaryData, true);
        assertNotNull(encoded);
        assertEquals(6, encoded.length); // always adds trailer
        assertEquals(Base64.getEncodeLength(binaryData, Base64.CHUNK_SIZE, Base64.CHUNK_SEPARATOR), encoded.length);
        assertEquals("MTIz\r\n", toString(encoded));
        // URL unsafe
        // <<???>>
        encoded = Base64.encodeBase64(urlUnsafeData, true);
        assertEquals("PDw/Pz8+Pg==\r\n", toString(encoded));
        encoded = Base64.encodeBase64(urlUnsafeDataChunky, true);
        assertEquals(getJreMimeEncoder().encodeToString(urlUnsafeDataChunky) + "\r\n", toString(encoded));
        //
        // isChunked true
        encoded = Base64.encodeBase64(binaryData, true);
        assertNotNull(encoded);
        assertEquals(6, encoded.length);
        assertEquals(Base64.getEncodeLength(binaryData, Base64.CHUNK_SIZE, Base64.CHUNK_SEPARATOR), encoded.length);
        assertEquals("MTIz\r\n", toString(encoded));
    }

    @Test
    public void testEncodeBase64ByteArrayBooleanBoolean() {
        final byte[] binaryData = { '1', '2', '3' };
        byte[] encoded;
        encoded = Base64.encodeBase64(binaryData, false, false);
        assertNotNull(encoded);
        assertEquals(4, encoded.length);
        encoded = Base64.encodeBase64(binaryData, false, false);
        assertNotNull(encoded);
        assertEquals(4, encoded.length);
        encoded = Base64.encodeBase64(binaryData, true, false);
        assertNotNull(encoded);
        assertEquals(6, encoded.length); // always adds trailer
        encoded = Base64.encodeBase64(binaryData, true, false);
        assertNotNull(encoded);
        assertEquals(6, encoded.length);
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
    public void testEncodeBase64ByteArrayEdges() {
        final byte[] binaryData = null;
        assertArrayEquals(binaryData, Base64.encodeBase64(binaryData));
        final byte[] binaryData2 = {};
        assertArrayEquals(binaryData2, Base64.encodeBase64(binaryData2));
    }

    @Test
    public void testEncodeBase64Chunked() {
        byte[] bytesToEncode = { 'f', 'o', 'o', 'b', 'a', 'r' };
        byte[] encodedData = Base64.encodeBase64Chunked(bytesToEncode);
        assertEquals("Zm9vYmFy\r\n", toString(encodedData));
        // URL unsafe data
        // <<???>>
        bytesToEncode = "<<???>>".getBytes();
        encodedData = Base64.encodeBase64Chunked(bytesToEncode);
        assertEquals("PDw/Pz8+Pg==\r\n", toString(encodedData));
        // > 76
        final byte[] chunkMe = ArrayFill.fill(new byte[Base64.CHUNK_SIZE * 2], (byte) 'A');
        final byte[] chunked = Base64.encodeBase64Chunked(chunkMe);
        assertEquals('\r', chunked[chunked.length - 2]);
        assertEquals('\n', chunked[chunked.length - 1]);
        assertArrayEquals(ArrayUtils.addAll(getJreMimeEncoder().encode(chunkMe), Base64.CHUNK_SEPARATOR), chunked);
    }

    @Test
    public void testEncodeBase64StringByteArray() {
        String stringToEncode = "Many hands make light work.";
        String encodedData = Base64.encodeBase64String(stringToEncode.getBytes());
        assertEquals("TWFueSBoYW5kcyBtYWtlIGxpZ2h0IHdvcmsu\r\n", encodedData);
        // URL unsafe data
        // <<???>>
        stringToEncode = "<<???>>";
        encodedData = Base64.encodeBase64String(stringToEncode.getBytes());
        assertEquals("PDw/Pz8+Pg==\r\n", encodedData);
        // > 76
        final byte[] chunkMe = ArrayFill.fill(new byte[Base64.CHUNK_SIZE * 2], (byte) 'A');
        final String chunked = Base64.encodeBase64String(chunkMe);
        assertEquals('\r', chunked.charAt(chunked.length() - 2));
        assertEquals('\n', chunked.charAt(chunked.length() - 1));
        assertEquals(getJreMimeEncoder().encodeToString(chunkMe) + "\r\n", chunked);
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
        byte[] bytesToEncode = "Many hands make light work.".getBytes();
        String encodedData = Base64.encodeBase64StringUnChunked(bytesToEncode);
        assertEquals("TWFueSBoYW5kcyBtYWtlIGxpZ2h0IHdvcmsu", encodedData);
        // URL unsafe data
        // <<???>>
        bytesToEncode = "<<???>>".getBytes();
        encodedData = Base64.encodeBase64StringUnChunked(bytesToEncode);
        assertEquals("PDw/Pz8+Pg==", encodedData);
        // > 76
        final byte[] chunkMe = ArrayFill.fill(new byte[Base64.CHUNK_SIZE * 2], (byte) 'A');
        final String chunked = Base64.encodeBase64StringUnChunked(chunkMe);
        assertEquals(getJreEncoder().encodeToString(chunkMe), chunked);
    }

    @Test
    public void testEncodeBase64URLSafe() {
        byte[] bytesToEncode = "Many hands make light work.".getBytes();
        byte[] encodedData = Base64.encodeBase64URLSafe(bytesToEncode);
        assertEquals("TWFueSBoYW5kcyBtYWtlIGxpZ2h0IHdvcmsu", toString(encodedData));
        // URL unsafe data
        // <<???>>
        bytesToEncode = "<<???>>".getBytes();
        encodedData = Base64.encodeBase64URLSafe(bytesToEncode);
        assertEquals("PDw_Pz8-Pg", toString(encodedData));
        // > 76 line length
        bytesToEncode = "<<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>>".getBytes();
        encodedData = Base64.encodeBase64URLSafe(bytesToEncode);
        assertEquals(getJreUrlEncoder().withoutPadding().encodeToString(bytesToEncode), toString(encodedData));
        final String encodedUrlSafe = "PDw_Pz8-Pjw8Pz8_Pj48PD8_Pz4-PDw_Pz8-Pjw8Pz8_Pj48PD8_Pz4-PDw_Pz8-Pjw8Pz8_Pj48PD8_Pz4-PDw_Pz8-Pjw8Pz8_Pj4";
        assertEquals(encodedUrlSafe, toString(encodedData));
        // instance
        assertEquals(encodedUrlSafe, toString(new Base64(0, null, true).encode(bytesToEncode)));
        assertEquals(encodedUrlSafe, toString(new Base64(1, null, true).encode(bytesToEncode)));
        assertEquals(encodedUrlSafe, toString(new Base64(999, null, true).encode(bytesToEncode)));
        assertEquals(encodedUrlSafe, toString(new Base64(0, new byte[0], true).encode(bytesToEncode)));
        assertEquals(encodedUrlSafe, toString(new Base64(0, new byte[10], true).encode(bytesToEncode)));
        assertEquals(encodedUrlSafe, toString(new Base64(0, Base64.CHUNK_SEPARATOR, true).encode(bytesToEncode)));
        assertEquals(encodedUrlSafe, toString(new Base64(Base64.CHUNK_SIZE, Base64.CHUNK_SEPARATOR, true).encode(bytesToEncode)));
        assertEquals(encodedUrlSafe, toString(new Base64(999, Base64.CHUNK_SEPARATOR, true).encode(bytesToEncode)));
        assertEquals(encodedUrlSafe, toString(new Base64(true).encode(bytesToEncode)));
    }

    @Test
    public void testEncodeBase64URLSafeString() {
        byte[] bytesToEncode = "Many hands make light work.".getBytes();
        String encodedData = Base64.encodeBase64URLSafeString(bytesToEncode);
        assertEquals("TWFueSBoYW5kcyBtYWtlIGxpZ2h0IHdvcmsu", encodedData);
        // URL unsafe data
        // <<???>>
        bytesToEncode = "<<???>>".getBytes();
        encodedData = Base64.encodeBase64URLSafeString(bytesToEncode);
        assertEquals("PDw_Pz8-Pg", encodedData);
        // > 76 line length
        bytesToEncode = "<<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>><<???>>".getBytes();
        encodedData = Base64.encodeBase64URLSafeString(bytesToEncode);
        assertEquals(getJreUrlEncoder().withoutPadding().encodeToString(bytesToEncode), encodedData);
        assertEquals("PDw_Pz8-Pjw8Pz8_Pj48PD8_Pz4-PDw_Pz8-Pjw8Pz8_Pj48PD8_Pz4-PDw_Pz8-Pjw8Pz8_Pj48PD8_Pz4-PDw_Pz8-Pjw8Pz8_Pj4", encodedData);
    }

    @Test
    public void testEncodeByteArray() {
        final Base64 base64 = new Base64();
        final byte[] bytesToEncode = { 'l', 'i', 'g', 'h', 't', ' ', 'w', 'o', 'r' };
        assertEquals("bGlnaHQgd29y\r\n", new String(base64.encode(bytesToEncode), StandardCharsets.UTF_8));
    }

    @Test
    public void testEncodeByteArrayEmpty() {
        assertNull(new Base64().encode((byte[]) null));
        final byte[] empty = {};
        assertSame(empty, new Base64().encode(empty));
    }

    @Test
    public void testEncodeByteArrayNull() {
        assertNull(new Base64().encode((byte[]) null));
    }

    @Test
    public void testEncodeInteger() {
        testEncodeInteger(BigInteger.ONE);
        testEncodeInteger(BigInteger.TEN);
        testEncodeInteger(BigInteger.ZERO);
    }

    private void testEncodeInteger(final BigInteger bi) {
        final byte[] decodedBytes = getJreDecoder().decode(Base64.encodeInteger(bi));
        final BigInteger decoded = decodedBytes.length == 0 ? BigInteger.ZERO : new BigInteger(decodedBytes);
        assertEquals(bi, decoded);
    }

    @Test
    public void testEncodeToString() {
        final Base64 base64 = new Base64();
        final byte[] bytesToEncode = { 'l', 'i', 'g', 'h', 't', ' ', 'w', 'o', 'r' };
        assertEquals("bGlnaHQgd29y\r\n", base64.encodeToString(bytesToEncode));
    }

    @Test
    public void testIsArrayByteBase64() {
        assertTrue(Base64.isArrayByteBase64(new byte[] { 'b', ' ' }));
        assertFalse(Base64.isArrayByteBase64(new byte[] { '?' }));
    }

    @Test
    public void testIsBase64() {
        assertTrue(Base64.isBase64((byte) '='));
        assertTrue(Base64.isBase64((byte) 'b'));
        assertFalse(Base64.isBase64((byte) ' '));
        assertFalse(Base64.isBase64((byte) -1));
    }

}
