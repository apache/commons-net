/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Objects;

/**
 * Provides Base64 encoding and decoding as defined by RFC 2045.
 *
 * <p>
 * This class implements section <cite>6.8. Base64 Content-Transfer-Encoding</cite> from RFC 2045 <cite>Multipurpose Internet Mail Extensions (MIME) Part One:
 * Format of Internet Message Bodies</cite> by Freed and Borenstein.
 * </p>
 * <p>
 * The class can be parameterized in the following manner with various constructors:
 * <ul>
 * <li>URL-safe mode: Default off.</li>
 * <li>Line length: Default 76. Line length that aren't multiples of 4 will still essentially end up being multiples of 4 in the encoded data.
 * <li>Line separator: Default is CRLF ("\r\n")</li>
 * </ul>
 * <p>
 * Since this class operates directly on byte streams, and not character streams, it is hard-coded to only encode/decode character encodings which are
 * compatible with the lower 127 ASCII chart (ISO-8859-1, Windows-1252, UTF-8, etc).
 * </p>
 *
 * @deprecated Use {@link java.util.Base64}.
 * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>
 * @since 2.2
 */
@Deprecated
public class Base64 {

    /**
     * Chunk size per RFC 2045 section 6.8.
     *
     * <p>
     * The {@value} character limit does not count the trailing CRLF, but counts all other characters, including any equal signs.
     * </p>
     *
     * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 section 6.8</a>
     */
    static final int CHUNK_SIZE = 76;

    /**
     * Chunk separator per RFC 2045 section 2.1.
     *
     * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 section 2.1</a>
     */
    static final byte[] CHUNK_SEPARATOR = { '\r', '\n' };

    /**
     * Byte used to pad output.
     */
    private static final byte PAD = '=';

    /**
     * This array is a lookup table that translates Unicode characters drawn from the "Base64 Alphabet" (as specified in Table 1 of RFC 2045) into their 6-bit
     * positive integer equivalents. Characters that are not in the Base64 alphabet but fall within the bounds of the array are translated to -1.
     *
     * Note: '+' and '-' both decode to 62. '/' and '_' both decode to 63. This means decoder seamlessly handles both URL_SAFE and STANDARD base64. (The
     * encoder, on the other hand, needs to know ahead of time what to emit).
     *
     * Thanks to "commons" project in ws.apache.org for <a href="https://svn.apache.org/repos/asf/webservices/commons/trunk/modules/util/">this code</a>
     */
    private static final byte[] DECODE_TABLE = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32,
            33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51 };

    // The static final fields above are used for the original static byte[] methods on Base64.
    // The private member fields below are used with the new streaming approach, which requires
    // some state be preserved between calls of encode() and decode().

    /**
     * Tests a given byte array to see if it contains any valid character within the Base64 alphabet.
     *
     * @param arrayOctet byte array to test
     * @return {@code true} if any byte is a valid character in the Base64 alphabet; {@code false} otherwise
     */
    private static boolean containsBase64Byte(final byte[] arrayOctet) {
        for (final byte element : arrayOctet) {
            if (isBase64(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Decodes Base64 data into octets.
     *
     * @param base64 Byte array containing Base64 data
     * @return Array containing decoded data.
     */
    public static byte[] decodeBase64(final byte[] base64) {
        return isEmpty(base64) ? base64 : getDecoder().decode(base64);
    }

    /**
     * Decodes a Base64 String into octets.
     *
     * @param base64 String containing Base64 data
     * @return Array containing decoded data.
     * @since 1.4
     */
    public static byte[] decodeBase64(final String base64) {
        return getDecoder().decode(base64);
    }

    /**
     * Decodes a byte64-encoded integer according to crypto standards such as W3C's XML-Signature
     *
     * @param source a byte array containing base64 character data
     * @return A BigInteger
     * @since 1.4
     */
    public static BigInteger decodeInteger(final byte[] source) {
        return new BigInteger(1, decodeBase64(source));
    }

    private static byte[] encode(final byte[] binaryData, final int lineLength, final byte[] lineSeparator, final boolean urlSafe) {
        if (isEmpty(binaryData)) {
            return binaryData;
        }
        return lineLength > 0 ? encodeBase64Chunked(binaryData, lineLength, lineSeparator)
                : urlSafe ? encodeBase64URLSafe(binaryData) : encodeBase64(binaryData);
    }

    /**
     * Encodes binary data using the base64 algorithm but does not chunk the output.
     *
     * @param source binary data to encode
     * @return byte[] containing Base64 characters in their UTF-8 representation.
     */
    public static byte[] encodeBase64(final byte[] source) {
        return isEmpty(source) ? source : getEncoder().encode(source);
    }

    /**
     * Encodes binary data using the base64 algorithm, optionally chunking the output into 76 character blocks.
     *
     * @param binaryData Array containing binary data to encode.
     * @param chunked  if {@code true} this encoder will chunk the base64 output into 76 character blocks
     * @return Base64-encoded data.
     * @throws IllegalArgumentException Thrown when the input array needs an output array bigger than {@link Integer#MAX_VALUE}
     */
    public static byte[] encodeBase64(final byte[] binaryData, final boolean chunked) {
        return chunked ? encodeBase64Chunked(binaryData) : encodeBase64(binaryData, false, false);
    }

    /**
     * Encodes binary data using the base64 algorithm, optionally chunking the output into 76 character blocks.
     *
     * @param binaryData Array containing binary data to encode.
     * @param chunked  if {@code true} this encoder will chunk the base64 output into 76 character blocks
     * @param urlSafe    if {@code true} this encoder will emit - and _ instead of the usual + and / characters.
     * @return Base64-encoded data.
     * @throws IllegalArgumentException Thrown when the input array needs an output array bigger than {@link Integer#MAX_VALUE}
     * @since 1.4
     */
    public static byte[] encodeBase64(final byte[] binaryData, final boolean chunked, final boolean urlSafe) {
        return encodeBase64(binaryData, chunked, urlSafe, Integer.MAX_VALUE);
    }

    /**
     * Encodes binary data using the base64 algorithm, optionally chunking the output into 76 character blocks.
     *
     * @param binaryData    Array containing binary data to encode.
     * @param chunked     if {@code true} this encoder will chunk the base64 output into 76 character blocks
     * @param urlSafe       if {@code true} this encoder will emit - and _ instead of the usual + and / characters.
     * @param maxResultSize The maximum result size to accept.
     * @return Base64-encoded data.
     * @throws IllegalArgumentException Thrown when the input array needs an output array bigger than maxResultSize
     * @since 1.4
     */
    public static byte[] encodeBase64(final byte[] binaryData, final boolean chunked, final boolean urlSafe, final int maxResultSize) {
        if (isEmpty(binaryData)) {
            return binaryData;
        }
        final long len = getEncodeLength(binaryData, chunked ? CHUNK_SIZE : 0, chunked ? CHUNK_SEPARATOR : NetConstants.EMPTY_BTYE_ARRAY);
        if (len > maxResultSize) {
            throw new IllegalArgumentException(
                    "Input array too big, the output array would be bigger (" + len + ") than the specified maxium size of " + maxResultSize);
        }
        return chunked ? encodeBase64Chunked(binaryData) : urlSafe ? encodeBase64URLSafe(binaryData) : encodeBase64(binaryData);
    }

    /**
     * Encodes binary data using the base64 algorithm and chunks the encoded output into 76 character blocks separated by CR-LF.
     * <p>
     * The return value ends in a CR-LF.
     * </p>
     *
     * @param binaryData binary data to encode
     * @return Base64 characters chunked in 76 character blocks
     * @throws ArithmeticException if the {@code binaryData} would overflows a byte[].
     */
    public static byte[] encodeBase64Chunked(final byte[] binaryData) {
        return encodeBase64Chunked(binaryData, CHUNK_SIZE, CHUNK_SEPARATOR);
    }

    private static byte[] encodeBase64Chunked(final byte[] binaryData, final int lineLength, final byte[] lineSeparator) {
        final long encodeLength = getEncodeLength(binaryData, lineLength, lineSeparator);
        final byte[] dst = new byte[Math.toIntExact(encodeLength)];
        getMimeEncoder(lineLength, lineSeparator).encode(binaryData, dst);
        // Copy chunk separator at the end
        System.arraycopy(lineSeparator, 0, dst, dst.length - lineSeparator.length, lineSeparator.length);
        return dst;
    }

    /**
     * Encodes binary data using the base64 algorithm into 76 character blocks separated by CR-LF.
     * <p>
     * The return value ends in a CR-LF.
     * </p>
     * <p>
     * For a non-chunking version, see {@link #encodeBase64StringUnChunked(byte[])}.
     * </p>
     *
     * @param binaryData binary data to encode
     * @return String containing Base64 characters.
     * @since 1.4
     */
    public static String encodeBase64String(final byte[] binaryData) {
        return getMimeEncoder().encodeToString(binaryData) + "\r\n";
    }

    /**
     * Encodes binary data using the base64 algorithm.
     *
     * @param binaryData  binary data to encode
     * @param chunked whether to split the output into chunks
     * @return String containing Base64 characters.
     * @since 3.2
     */
    public static String encodeBase64String(final byte[] binaryData, final boolean chunked) {
        return newStringUtf8(encodeBase64(binaryData, chunked));
    }

    /**
     * Encodes binary data using the base64 algorithm, without using chunking.
     * <p>
     * For a chunking version, see {@link #encodeBase64String(byte[])}.
     * </p>
     *
     * @param binaryData binary data to encode
     * @return String containing Base64 characters.
     * @since 3.2
     */
    public static String encodeBase64StringUnChunked(final byte[] binaryData) {
        return getEncoder().encodeToString(binaryData);
    }

    /**
     * Encodes binary data using a URL-safe variation of the base64 algorithm but does not chunk the output. The url-safe variation emits - and _ instead of +
     * and / characters.
     *
     * @param binaryData binary data to encode
     * @return byte[] containing Base64 characters in their UTF-8 representation.
     * @since 1.4
     */
    public static byte[] encodeBase64URLSafe(final byte[] binaryData) {
        return getUrlEncoder().withoutPadding().encode(binaryData);
    }

    /**
     * Encodes binary data using a URL-safe variation of the base64 algorithm but does not chunk the output. The url-safe variation emits - and _ instead of +
     * and / characters.
     *
     * @param binaryData binary data to encode
     * @return String containing Base64 characters
     * @since 1.4
     */
    public static String encodeBase64URLSafeString(final byte[] binaryData) {
        return getUrlEncoder().withoutPadding().encodeToString(binaryData);
    }

    /**
     * Encodes to a byte64-encoded integer according to crypto standards such as W3C's XML-Signature
     *
     * @param bigInt a BigInteger
     * @return A byte array containing base64 character data
     * @throws NullPointerException if null is passed in
     * @since 1.4
     */
    public static byte[] encodeInteger(final BigInteger bigInt) {
        return encodeBase64(toIntegerBytes(bigInt), false);
    }

    private static Decoder getDecoder() {
        return java.util.Base64.getDecoder();
    }

    /**
     * Pre-calculates the amount of space needed to base64-encode the supplied array.
     *
     * @param array          byte[] array which will later be encoded
     * @param lineSize      line-length of the output (<= 0 means no chunking) between each chunkSeparator (e.g. CRLF).
     * @param linkSeparator the sequence of bytes used to separate chunks of output (e.g. CRLF).
     *
     * @return amount of space needed to encode the supplied array. Returns a long since a max-len array will require Integer.MAX_VALUE + 33%.
     */
    static long getEncodeLength(final byte[] array, int lineSize, final byte[] linkSeparator) {
        // base64 always encodes to multiples of 4.
        lineSize = lineSize / 4 * 4;
        long len = array.length * 4 / 3;
        final long mod = len % 4;
        if (mod != 0) {
            len += 4 - mod;
        }
        if (lineSize > 0) {
            final boolean lenChunksPerfectly = len % lineSize == 0;
            len += len / lineSize * linkSeparator.length;
            if (!lenChunksPerfectly) {
                len += linkSeparator.length;
            }
        }
        return len;
    }

    private static Encoder getEncoder() {
        return java.util.Base64.getEncoder();
    }

    private static Encoder getMimeEncoder() {
        return java.util.Base64.getMimeEncoder();
    }

    private static Encoder getMimeEncoder(final int lineLength, final byte[] lineSeparator) {
        return java.util.Base64.getMimeEncoder(lineLength, lineSeparator);
    }

    private static Encoder getUrlEncoder() {
        return java.util.Base64.getUrlEncoder();
    }

    /**
     * Tests a given byte array to see if it contains only valid characters within the Base64 alphabet. Currently, the method treats whitespace as valid.
     *
     * @param arrayOctet byte array to test
     * @return {@code true} if all bytes are valid characters in the Base64 alphabet or if the byte array is empty; false, otherwise
     */
    public static boolean isArrayByteBase64(final byte[] arrayOctet) {
        for (final byte element : arrayOctet) {
            if (!isBase64(element) && !isWhiteSpace(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether or not the <code>octet</code> is in the base 64 alphabet.
     *
     * @param octet The value to test
     * @return {@code true} if the value is defined in the base 64 alphabet, {@code false} otherwise.
     * @since 1.4
     */
    public static boolean isBase64(final byte octet) {
        return octet == PAD || octet >= 0 && octet < DECODE_TABLE.length && DECODE_TABLE[octet] != -1;
    }

    private static boolean isEmpty(final byte[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if a byte value is whitespace or not.
     *
     * @param byteToCheck the byte to check
     * @return true if byte is whitespace, false otherwise
     */
    private static boolean isWhiteSpace(final byte byteToCheck) {
        switch (byteToCheck) {
        case ' ':
        case '\n':
        case '\r':
        case '\t':
            return true;
        default:
            return false;
        }
    }

    private static String newStringUtf8(final byte[] encode) {
        return new String(encode, StandardCharsets.UTF_8);
    }

    /**
     * Returns a byte-array representation of a <code>BigInteger</code> without sign bit.
     *
     * @param bigInt <code>BigInteger</code> to be converted
     * @return a byte array representation of the BigInteger parameter
     */
    private static byte[] toIntegerBytes(final BigInteger bigInt) {
        Objects.requireNonNull(bigInt, "bigInt");
        int bitlen = bigInt.bitLength();
        // round bitlen
        bitlen = bitlen + 7 >> 3 << 3;
        final byte[] bigBytes = bigInt.toByteArray();
        if (bigInt.bitLength() % 8 != 0 && bigInt.bitLength() / 8 + 1 == bitlen / 8) {
            return bigBytes;
        }
        // set up params for copying everything but sign bit
        int startSrc = 0;
        int len = bigBytes.length;

        // if bigInt is exactly byte-aligned, just skip signbit in copy
        if (bigInt.bitLength() % 8 == 0) {
            startSrc = 1;
            len--;
        }
        final int startDst = bitlen / 8 - len; // to pad w/ nulls as per spec
        final byte[] resizedBytes = new byte[bitlen / 8];
        System.arraycopy(bigBytes, startSrc, resizedBytes, startDst, len);
        return resizedBytes;
    }

    /**
     * Line length for encoding. Not used when decoding. A value of zero or less implies no chunking of the base64 encoded data.
     */
    private final int lineLength;

    /**
     * Line separator for encoding. Not used when decoding. Only used if lineLength > 0.
     */
    private final byte[] lineSeparator;

    /**
     * Whether encoding is URL and filename safe, or not.
     */
    private final boolean urlSafe;

    /**
     * Creates a Base64 codec used for decoding (all modes) and encoding in URL-unsafe mode.
     * <p>
     * When encoding the line length is 76, the line separator is CRLF, and the encoding table is STANDARD_ENCODE_TABLE.
     * </p>
     *
     * <p>
     * When decoding all variants are supported.
     * </p>
     */
    public Base64() {
        this(false);
    }

    /**
     * Creates a Base64 codec used for decoding (all modes) and encoding in the given URL-safe mode.
     * <p>
     * When encoding the line length is 76, the line separator is CRLF, and the encoding table is STANDARD_ENCODE_TABLE.
     * </p>
     *
     * <p>
     * When decoding all variants are supported.
     * </p>
     *
     * @param urlSafe if {@code true}, URL-safe encoding is used. In most cases this should be set to {@code false}.
     * @since 1.4
     */
    public Base64(final boolean urlSafe) {
        this(CHUNK_SIZE, CHUNK_SEPARATOR, urlSafe);
    }

    /**
     * Creates a Base64 codec used for decoding (all modes) and encoding in URL-unsafe mode.
     * <p>
     * When encoding the line length is given in the constructor, the line separator is CRLF, and the encoding table is STANDARD_ENCODE_TABLE.
     * </p>
     * <p>
     * Line lengths that aren't multiples of 4 will still essentially end up being multiples of 4 in the encoded data.
     * </p>
     * <p>
     * When decoding all variants are supported.
     * </p>
     *
     * @param lineLength Each line of encoded data will be at most of the given length (rounded down to the nearest multiple of 4).
     *                   If {@code lineLength <= 0}, then the output will not be divided into lines (chunks). Ignored when decoding.
     * @since 1.4
     */
    public Base64(final int lineLength) {
        this(lineLength, CHUNK_SEPARATOR);
    }

    /**
     * Creates a Base64 codec used for decoding (all modes) and encoding in URL-unsafe mode.
     * <p>
     * When encoding the line length and line separator are given in the constructor, and the encoding table is STANDARD_ENCODE_TABLE.
     * </p>
     * <p>
     * Line lengths that aren't multiples of 4 will still essentially end up being multiples of 4 in the encoded data.
     * </p>
     * <p>
     * When decoding all variants are supported.
     * </p>
     *
     * @param lineLength    Each line of encoded data will be at most of the given length (rounded down to the nearest multiple of 4).
     *                      If {@code lineLength <= 0}, then the output will not be divided into lines (chunks). Ignored when decoding.
     * @param lineSeparator Each line of encoded data will end with this sequence of bytes. Not used for decoding.
     * @throws IllegalArgumentException Thrown when the provided lineSeparator included some base64 characters.
     * @since 1.4
     */
    public Base64(final int lineLength, final byte[] lineSeparator) {
        this(lineLength, lineSeparator, false);
    }

    /**
     * Creates a Base64 codec used for decoding (all modes) and encoding in URL-unsafe mode.
     * <p>
     * When encoding the line length and line separator are given in the constructor, and the encoding table is STANDARD_ENCODE_TABLE.
     * </p>
     * <p>
     * Line lengths that aren't multiples of 4 will still essentially end up being multiples of 4 in the encoded data.
     * </p>
     * <p>
     * When decoding all variants are supported.
     * </p>
     *
     * @param lineLength    Each line of encoded data will be at most of the given length (rounded down to the nearest multiple of 4).
     *                      If {@code lineLength <= 0}, then the output will not be divided into lines (chunks). Ignored when decoding.
     * @param lineSeparator Each line of encoded data will end with this sequence of bytes. Not used for decoding.
     * @param urlSafe       Instead of emitting '+' and '/' we emit '-' and '_' respectively. urlSafe is only applied to encode operations. Decoding seamlessly
     *                      handles both modes.
     * @throws IllegalArgumentException The provided lineSeparator included some base64 characters. That's not going to work!
     * @since 1.4
     */
    public Base64(int lineLength, byte[] lineSeparator, final boolean urlSafe) {
        if (lineSeparator == null || urlSafe) {
            lineLength = 0; // disable chunk-separating
            lineSeparator = NetConstants.EMPTY_BTYE_ARRAY; // this just gets ignored
        }
        this.lineLength = lineLength > 0 ? lineLength / 4 * 4 : 0;
        this.lineSeparator = new byte[lineSeparator.length];
        System.arraycopy(lineSeparator, 0, this.lineSeparator, 0, lineSeparator.length);
        if (containsBase64Byte(lineSeparator)) {
            final String sep = newStringUtf8(lineSeparator);
            throw new IllegalArgumentException("lineSeperator must not contain base64 characters: [" + sep + "]");
        }
        this.urlSafe = urlSafe;
    }

    /**
     * Decodes a byte array containing characters in the Base64 alphabet.
     *
     * @param source A byte array containing Base64 character data
     * @return a byte array containing binary data; will return {@code null} if provided byte array is {@code null}.
     */
    public byte[] decode(final byte[] source) {
        return isEmpty(source) ? source : getDecoder().decode(source);
    }

    /**
     * Decodes a String containing characters in the Base64 alphabet.
     *
     * @param source A String containing Base64 character data, must not be {@code null}
     * @return a byte array containing binary data
     * @since 1.4
     */
    public byte[] decode(final String source) {
        return getDecoder().decode(source);
    }

    /**
     * Encodes a byte[] containing binary data, into a byte[] containing characters in the Base64 alphabet.
     *
     * @param source a byte array containing binary data
     * @return A byte array containing only Base64 character data
     */
    public byte[] encode(final byte[] source) {
        return encode(source, lineLength, lineSeparator, isUrlSafe());
    }

    /**
     * Encodes a byte[] containing binary data, into a String containing characters in the Base64 alphabet.
     *
     * @param source a byte array containing binary data
     * @return A String containing only Base64 character data
     * @since 1.4
     */
    public String encodeToString(final byte[] source) {
        return newStringUtf8(encode(source));
    }

    int getLineLength() {
        return lineLength;
    }

    byte[] getLineSeparator() {
        return lineSeparator.clone();
    }

    /**
     * Tests whether our current encoding mode. True if we're URL-SAFE, false otherwise.
     *
     * @return true if we're in URL-SAFE mode, false otherwise.
     * @since 1.4
     */
    public boolean isUrlSafe() {
        return urlSafe;
    }
}
