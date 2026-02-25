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
package org.apache.commons.net.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.net.util.SubnetUtils6.SubnetInfo;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link SubnetUtils6}.
 */
class SubnetUtils6Test {

    private static final BigInteger TWO = BigInteger.valueOf(2);

    @Test
    void testBasicCidr64() {
        final SubnetUtils6 utils = new SubnetUtils6("2001:db8::1/64");
        final SubnetInfo info = utils.getInfo();

        assertEquals(64, info.getPrefixLength());
        assertEquals("2001:db8:0:0:0:0:0:1", info.getAddress());
        assertEquals("2001:db8:0:0:0:0:0:0", info.getNetworkAddress());
        assertEquals("2001:db8:0:0:ffff:ffff:ffff:ffff", info.getHighAddress());
        // 2^64 addresses
        assertEquals(TWO.pow(64), info.getAddressCount());
    }

    @Test
    void testBasicCidr128() {
        final SubnetUtils6 utils = new SubnetUtils6("2001:db8::1/128");
        final SubnetInfo info = utils.getInfo();

        assertEquals(128, info.getPrefixLength());
        assertEquals("2001:db8:0:0:0:0:0:1", info.getNetworkAddress());
        assertEquals("2001:db8:0:0:0:0:0:1", info.getHighAddress());
        assertEquals(BigInteger.ONE, info.getAddressCount());
    }

    @Test
    void testCidr0() {
        final SubnetUtils6 utils = new SubnetUtils6("::/0");
        final SubnetInfo info = utils.getInfo();

        assertEquals(0, info.getPrefixLength());
        assertEquals("0:0:0:0:0:0:0:0", info.getNetworkAddress());
        // 2^128 addresses
        assertEquals(TWO.pow(128), info.getAddressCount());
    }

    @Test
    void testCidr48() {
        final SubnetUtils6 utils = new SubnetUtils6("2001:db8:abcd::/48");
        final SubnetInfo info = utils.getInfo();

        assertEquals(48, info.getPrefixLength());
        assertEquals("2001:db8:abcd:0:0:0:0:0", info.getNetworkAddress());
        assertEquals("2001:db8:abcd:ffff:ffff:ffff:ffff:ffff", info.getHighAddress());
    }

    @Test
    void testCompressedAddress() {
        final SubnetUtils6 utils = new SubnetUtils6("fe80::1/10");
        final SubnetInfo info = utils.getInfo();

        assertEquals(10, info.getPrefixLength());
        assertTrue(info.isInRange("fe80::1"));
        assertTrue(info.isInRange("fe80::ffff"));
        assertTrue(info.isInRange("febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(info.isInRange("fec0::1")); // Outside /10 range
    }

    @Test
    void testConstructorWithSeparateArgs() {
        final SubnetUtils6 utils = new SubnetUtils6("2001:db8::1", 64);
        final SubnetInfo info = utils.getInfo();

        assertEquals(64, info.getPrefixLength());
        assertEquals("2001:db8:0:0:0:0:0:0", info.getNetworkAddress());
    }

    @Test
    void testFullAddress() {
        final SubnetUtils6 utils = new SubnetUtils6("2001:0db8:0000:0000:0000:0000:0000:0001/64");
        final SubnetInfo info = utils.getInfo();

        assertEquals(64, info.getPrefixLength());
        assertEquals("2001:db8:0:0:0:0:0:0", info.getNetworkAddress());
    }

    @Test
    void testGetCidrSignature() {
        final SubnetUtils6 utils = new SubnetUtils6("2001:db8::1/64");
        final SubnetInfo info = utils.getInfo();

        assertEquals("2001:db8:0:0:0:0:0:1/64", info.getCidrSignature());
    }

    @Test
    void testInvalidCidr() {
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils6(null));
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils6("2001:db8::1"));
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils6("2001:db8::1/"));
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils6("2001:db8::1/129"));
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils6("2001:db8::1/-1"));
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils6("2001:db8::1/abc"));
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils6("not-an-address/64"));
    }

    @Test
    void testInvalidTwoArgConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils6("2001:db8::1", 129));
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils6("2001:db8::1", -1));
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils6("not-an-address", 64));
    }

    @Test
    void testInvalidIPv4Address() {
        // IPv4 addresses should be rejected
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils6("192.168.1.1/24"));
    }

    @Test
    void testIsInRange() {
        final SubnetUtils6 utils = new SubnetUtils6("2001:db8::/32");
        final SubnetInfo info = utils.getInfo();

        // Addresses in range
        assertTrue(info.isInRange("2001:db8::1"));
        assertTrue(info.isInRange("2001:db8::"));
        assertTrue(info.isInRange("2001:db8:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertTrue(info.isInRange("2001:db8:1234:5678:9abc:def0:1234:5678"));

        // Addresses out of range
        assertFalse(info.isInRange("2001:db9::1"));
        assertFalse(info.isInRange("2001:db7::1"));
        assertFalse(info.isInRange("2002:db8::1"));
        assertFalse(info.isInRange("::1"));
    }

    @Test
    void testIsInRangeWithInvalidString() {
        final SubnetUtils6 utils = new SubnetUtils6("2001:db8::/32");
        final SubnetInfo info = utils.getInfo();

        assertThrows(IllegalArgumentException.class, () -> info.isInRange("not-an-address"));
        assertThrows(IllegalArgumentException.class, () -> info.isInRange("192.168.1.1"));
    }

    @Test
    void testIsInRangeWithBigInteger() throws UnknownHostException {
        final SubnetUtils6 utils = new SubnetUtils6("2001:db8::/32");
        final SubnetInfo info = utils.getInfo();

        // Test with null
        assertFalse(info.isInRange((BigInteger) null));

        final BigInteger inRange = new BigInteger(1, InetAddress.getByName("2001:db8::1").getAddress());
        assertTrue(info.isInRange(inRange));
        final BigInteger outOfRange = new BigInteger(1, InetAddress.getByName("2001:db9::1").getAddress());
        assertFalse(info.isInRange(outOfRange));
    }

    @Test
    void testIsInRangeWithByteArray() throws UnknownHostException {
        final SubnetUtils6 utils = new SubnetUtils6("2001:db8::/32");
        final SubnetInfo info = utils.getInfo();

        // Test with null
        assertFalse(info.isInRange((byte[]) null));

        // Test with wrong length
        assertFalse(info.isInRange(new byte[4]));
        assertFalse(info.isInRange(new byte[15]));
        assertFalse(info.isInRange(new byte[17]));

        assertTrue(info.isInRange(InetAddress.getByName("2001:db8::1").getAddress()));
        assertFalse(info.isInRange(InetAddress.getByName("2001:db9::1").getAddress()));
    }

    @Test
    void testIsInRangeWithInet6Address() throws UnknownHostException {
        final SubnetUtils6 utils = new SubnetUtils6("2001:db8::/32");
        final SubnetInfo info = utils.getInfo();

        // Test with actual Inet6Address
        final Inet6Address addr = (Inet6Address) InetAddress.getByName("2001:db8::1");
        assertTrue(info.isInRange(addr));

        final Inet6Address addrOutside = (Inet6Address) InetAddress.getByName("2001:db9::1");
        assertFalse(info.isInRange(addrOutside));

        // Test with null
        assertFalse(info.isInRange((Inet6Address) null));
    }

    @Test
    void testLinkLocalAddress() {
        final SubnetUtils6 utils = new SubnetUtils6("fe80::/10");
        final SubnetInfo info = utils.getInfo();

        assertTrue(info.isInRange("fe80::1"));
        assertTrue(info.isInRange("fe80::1:2:3:4"));
        assertFalse(info.isInRange("::1")); // Loopback is not link-local
    }

    @Test
    void testLoopbackAddress() {
        final SubnetUtils6 utils = new SubnetUtils6("::1/128");
        final SubnetInfo info = utils.getInfo();

        assertEquals(128, info.getPrefixLength());
        assertTrue(info.isInRange("::1"));
        assertFalse(info.isInRange("::2"));
    }

    @Test
    void testToString() {
        final SubnetUtils6 utils = new SubnetUtils6("2001:db8::1/64");
        final SubnetInfo info = utils.getInfo();
        final String str = utils.toString();

        assertNotNull(str);
        assertTrue(str.contains("CIDR Signature"));
        assertTrue(str.contains("Network"));
        assertTrue(str.contains("First address"));
        assertTrue(str.contains("Last address"));
        assertTrue(str.contains("Address Count"));

        // note: The CIDR signature from toString can be fed back into the constructor
        final String cidr = info.getCidrSignature();
        final SubnetUtils6 roundTrip = new SubnetUtils6(cidr);
        final SubnetInfo roundTripInfo = roundTrip.getInfo();
        assertEquals(info.getPrefixLength(), roundTripInfo.getPrefixLength());
        assertEquals(info.getNetworkAddress(), roundTripInfo.getNetworkAddress());
        assertEquals(info.getHighAddress(), roundTripInfo.getHighAddress());
        assertEquals(info.getAddressCount(), roundTripInfo.getAddressCount());
        assertEquals(info.getCidrSignature(), roundTripInfo.getCidrSignature());
    }

    @Test
    void testUniqueLocalAddress() {
        // ULA range is fc00::/7
        final SubnetUtils6 utils = new SubnetUtils6("fd00::/8");
        final SubnetInfo info = utils.getInfo();

        assertTrue(info.isInRange("fd00::1"));
        assertTrue(info.isInRange("fdff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertFalse(info.isInRange("fc00::1")); // fc00::/8 is different from fd00::/8
    }

    @Test
    void testHighBitAddress() {
        final SubnetUtils6 utils = new SubnetUtils6("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff/128");
        final SubnetInfo info = utils.getInfo();

        assertEquals("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff", info.getAddress());
        assertEquals("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff", info.getNetworkAddress());
        assertEquals("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff", info.getHighAddress());
        assertEquals(BigInteger.ONE, info.getAddressCount());
    }

    @Test
    void testGetLowAddress() {
        final SubnetUtils6 utils = new SubnetUtils6("2001:db8::100/120");
        final SubnetInfo info = utils.getInfo();

        // getLowAddress returns the network address (same as getNetworkAddress)
        assertEquals(info.getNetworkAddress(), info.getLowAddress());
        assertEquals("2001:db8:0:0:0:0:0:100", info.getLowAddress());
    }

    // All examples below are from https://datatracker.ietf.org/doc/html/rfc5952 to verify properly
    /**
     * RFC 5952 Section 1: all representations of the same address must parse identically.
     */
    @Test
    void testRfc5952Section1EquivalentRepresentations() {
        assertEquivalentSubnets(
                "2001:db8:0:0:1:0:0:1/128",
                "2001:0db8:0:0:1:0:0:1/128",
                "2001:db8::1:0:0:1/128",
                "2001:db8::0:1:0:0:1/128",
                "2001:0db8::1:0:0:1/128",
                "2001:db8:0:0:1::1/128",
                "2001:db8:0000:0:1::1/128",
                "2001:DB8:0:0:1::1/128"
        );
    }

    /**
     * RFC 5952 Section 2.1: leading zeros in each 16-bit group must not affect parsing.
     */
    @Test
    void testRfc5952Section21LeadingZeros() {
        assertEquivalentSubnets(
                "2001:db8:aaaa:bbbb:cccc:dddd:eeee:0001/128",
                "2001:db8:aaaa:bbbb:cccc:dddd:eeee:001/128",
                "2001:db8:aaaa:bbbb:cccc:dddd:eeee:01/128",
                "2001:db8:aaaa:bbbb:cccc:dddd:eeee:1/128"
        );
    }

    /**
     * RFC 5952 Section 2.2:  various :: compression positions must resolve to the same address.
     */
    @Test
    void testRfc5952Section22ZeroCompression() {
        assertEquivalentSubnets(
                "2001:db8:0:0:0:0:0:1/128",
                "2001:db8:0:0:0::1/128",
                "2001:db8:0:0::1/128",
                "2001:db8:0::1/128",
                "2001:db8::1/128"
        );
    }

    /**
     * RFC 5952 Section 2.3:  uppercase, lowercase, and mixed-case hex digits must parse identically.
     */
    @Test
    void testRfc5952Section23CaseInsensitivity() {
        assertEquivalentSubnets(
                "2001:db8:aaaa:bbbb:cccc:dddd:eeee:aaaa/128",
                "2001:db8:aaaa:bbbb:cccc:dddd:eeee:AAAA/128",
                "2001:db8:aaaa:bbbb:cccc:dddd:eeee:AaAa/128",
                "2001:DB8:AAAA:BBBB:CCCC:DDDD:EEEE:AAAA/128"
        );
    }

    /**
     * RFC 5952 Section 4.1:  canonical form suppresses leading zeros.
     * Verifies that {@code 2001:0db8::0001} and {@code 2001:db8::1} produce the same output.
     */
    @Test
    void testRfc5952Section41CanonicalLeadingZeros() {
        final SubnetInfo a = new SubnetUtils6("2001:0db8::0001/128").getInfo();
        final SubnetInfo b = new SubnetUtils6("2001:db8::1/128").getInfo();
        assertEquals(a.getAddress(), b.getAddress());
    }

    /**
     * RFC 5952 Section 4.2.1: :: must compress the longest possible run.
     * Both forms represent the same address.
     */
    @Test
    void testRfc5952Section421MaximumShortening() {
        final SubnetInfo a = new SubnetUtils6("2001:db8::0:1/128").getInfo();
        final SubnetInfo b = new SubnetUtils6("2001:db8::1/128").getInfo();
        assertEquals(a.getAddress(), b.getAddress());
    }

    /**
     * RFC 5952 Section 4.2.3:  when two zero runs of equal length exist, the first must be compressed.
     * Both input forms must parse to the same address.
     */
    @Test
    void testRfc5952Section423FirstLongestRunCompressed() {
        assertEquivalentSubnets(
                "2001:db8:0:0:1:0:0:1/128",
                "2001:db8::1:0:0:1/128",
                "2001:db8:0:0:1::1/128"
        );
    }

    private static void assertEquivalentSubnets(final String... cidrs) {
        final SubnetInfo reference = new SubnetUtils6(cidrs[0]).getInfo();
        for (int i = 1; i < cidrs.length; i++) {
            final SubnetInfo other = new SubnetUtils6(cidrs[i]).getInfo();
            assertEquals(reference.getNetworkAddress(), other.getNetworkAddress(),
                cidrs[0] + " vs " + cidrs[i] + " network");
            assertEquals(reference.getHighAddress(), other.getHighAddress(),
                cidrs[0] + " vs " + cidrs[i] + " high");
            assertEquals(reference.getAddress(), other.getAddress(),
                cidrs[0] + " vs " + cidrs[i] + " address");
        }
    }

    @Test
    void testRfc5952Section5SpecialAddresses() {
        final SubnetInfo loopback = new SubnetUtils6("::1/128").getInfo();
        assertEquals("0:0:0:0:0:0:0:1", loopback.getAddress());

        final SubnetInfo unspecified = new SubnetUtils6("::/128").getInfo();
        assertEquals("0:0:0:0:0:0:0:0", unspecified.getAddress());
    }
}
