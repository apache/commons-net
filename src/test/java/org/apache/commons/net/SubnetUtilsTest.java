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

package org.apache.commons.net;

import org.apache.commons.net.util.subnet.SubnetInfo;
import org.apache.commons.net.util.subnet.SubnetUtils;

import junit.framework.TestCase;

@SuppressWarnings("deprecation") // deliberate use of deprecated methods
public class SubnetUtilsTest extends TestCase {

    // TODO Lower address test
    public void testAddresses() {
        /*
         * SubnetUtils utils = new SubnetUtils("192.168.0.1/29"); SubnetInfo
         * info = utils.getInfo();
         */
        SubnetInfo info = SubnetUtils.getByCIDRNortation("192.168.0.1/29");
        assertTrue(info.isInRange("192.168.0.1"));
        // We don't count the broadcast address as usable
        assertFalse(info.isInRange("192.168.0.7"));
        assertFalse(info.isInRange("192.168.0.8"));
        assertFalse(info.isInRange("10.10.2.1"));
        assertFalse(info.isInRange("192.168.1.1"));
        assertFalse(info.isInRange("192.168.0.255"));

        SubnetInfo ip6 = SubnetUtils.getByCIDRNortation("2001:db8:ac10:fc01:0:0:0:1/57");
        assertTrue(ip6.isInRange("2001:db8:ac10:fc01:0:0:0:1"));
        assertTrue(ip6.isInRange("2001:db8:ac10:fc00:0:0:0:0"));
        assertTrue(ip6.isInRange("2001:db8:ac10:fc7f:ffff:ffff:ffff:ffff"));
        // Different network
        assertFalse(ip6.isInRange("2001:db8:ac10:fc80:0:0:0:0"));
        assertFalse(ip6.isInRange("2001:db8:ac20:fc01:0:0:0:1"));
    }

    /**
     * Test using the inclusiveHostCount flag, which includes the network and
     * broadcast addresses in host counts
     */
    public void testCidrAddresses() {
        /*
         * SubnetUtils utils = new SubnetUtils("192.168.0.1/8");
         * utils.setInclusiveHostCount(true); SubnetInfo info = utils.getInfo();
         */
        SubnetInfo info = SubnetUtils.getByCIDRNortation("192.168.0.1/8");
        assertEquals("255.0.0.0", info.getNetmask());
        assertEquals(16777214, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/8");
        info.setInclusiveHostCount(true);
        assertEquals("255.0.0.0", info.getNetmask());
        assertEquals(16777216, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/1");
        info.setInclusiveHostCount(true);
        assertEquals("128.0.0.0", info.getNetmask());
        assertEquals(2147483648L, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/9");
        info.setInclusiveHostCount(true);
        assertEquals("255.128.0.0", info.getNetmask());
        assertEquals(8388608, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/10");
        info.setInclusiveHostCount(true);
        assertEquals("255.192.0.0", info.getNetmask());
        assertEquals(4194304, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/11");
        info.setInclusiveHostCount(true);
        assertEquals("255.224.0.0", info.getNetmask());
        assertEquals(2097152, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/12");
        info.setInclusiveHostCount(true);
        assertEquals("255.240.0.0", info.getNetmask());
        assertEquals(1048576, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/13");
        info.setInclusiveHostCount(true);
        assertEquals("255.248.0.0", info.getNetmask());
        assertEquals(524288, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/14");
        info.setInclusiveHostCount(true);
        assertEquals("255.252.0.0", info.getNetmask());
        assertEquals(262144, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/15");
        info.setInclusiveHostCount(true);
        assertEquals("255.254.0.0", info.getNetmask());
        assertEquals(131072, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/16");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.0.0", info.getNetmask());
        assertEquals(65536, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/17");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.128.0", info.getNetmask());
        assertEquals(32768, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/18");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.192.0", info.getNetmask());
        assertEquals(16384, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/19");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.224.0", info.getNetmask());
        assertEquals(8192, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/20");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.240.0", info.getNetmask());
        assertEquals(4096, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/21");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.248.0", info.getNetmask());
        assertEquals(2048, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/22");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.252.0", info.getNetmask());
        assertEquals(1024, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/23");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.254.0", info.getNetmask());
        assertEquals(512, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/24");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.255.0", info.getNetmask());
        assertEquals(256, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/25");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.255.128", info.getNetmask());
        assertEquals(128, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/26");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.255.192", info.getNetmask());
        assertEquals(64, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/27");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.255.224", info.getNetmask());
        assertEquals(32, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/28");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.255.240", info.getNetmask());
        assertEquals(16, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/29");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.255.248", info.getNetmask());
        assertEquals(8, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/30");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.255.252", info.getNetmask());
        assertEquals(4, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/31");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.255.254", info.getNetmask());
        assertEquals(2, info.getAddressCountLong());

        info = SubnetUtils.getByCIDRNortation("192.168.0.1/32");
        info.setInclusiveHostCount(true);
        assertEquals("255.255.255.255", info.getNetmask());
        assertEquals(1, info.getAddressCountLong());

        SubnetUtils.getByCIDRNortation("192.168.0.1/1");
    }

    public void testInvalidMasks() {
        try {
            SubnetUtils.getByCIDRNortation("192.168.0.1/33");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Ignored
        }
    }

    public void testNET428_31() throws Exception {
        final SubnetInfo subnetInfo = SubnetUtils.getByCIDRNortation("1.2.3.4/31");
        assertEquals(0, subnetInfo.getAddressCountLong());
        /*
         * String[] address = subnetInfo.getAllAddresses();
         * assertNotNull(address); assertEquals(0, address.length);
         */
    }

    public void testNET428_32() throws Exception {
        final SubnetInfo subnetInfo = SubnetUtils.getByCIDRNortation("1.2.3.4/32");
        assertEquals(0, subnetInfo.getAddressCountLong());
        /*
         * String[] address = subnetInfo.getAllAddresses();
         * assertNotNull(address); assertEquals(0, address.length);
         */
    }

    public void testParseSimpleNetmask() {
        final String address = "192.168.0.1";
        final String masks[] = new String[] { "255.0.0.0", "255.255.0.0", "255.255.255.0", "255.255.255.248" };
        final String bcastAddresses[] = new String[] { "192.255.255.255", "192.168.255.255", "192.168.0.255",
                "192.168.0.7" };
        final String lowAddresses[] = new String[] { "192.0.0.1", "192.168.0.1", "192.168.0.1", "192.168.0.1" };
        final String highAddresses[] = new String[] { "192.255.255.254", "192.168.255.254", "192.168.0.254",
                "192.168.0.6" };
        final String networkAddresses[] = new String[] { "192.0.0.0", "192.168.0.0", "192.168.0.0", "192.168.0.0" };
        final String cidrSignatures[] = new String[] { "192.168.0.1/8", "192.168.0.1/16", "192.168.0.1/24",
                "192.168.0.1/29" };
        final long usableAddresses[] = new long[] { 16777214, 65534, 254, 6 };

        for (int i = 0; i < masks.length; ++i) {
            SubnetInfo info = SubnetUtils.getByMask(address, masks[i]);
            assertEquals(bcastAddresses[i], info.getBroadcastAddress());
            assertEquals(cidrSignatures[i], info.getCidrSignature());
            assertEquals(lowAddresses[i], info.getLowAddress());
            assertEquals(highAddresses[i], info.getHighAddress());
            assertEquals(networkAddresses[i], info.getNetworkAddress());
            assertEquals(usableAddresses[i], info.getAddressCountLong());
        }
    }

    public void testParseSimpleNetmaskExclusive() {
        String address = "192.168.15.7";
        String masks[] = new String[] { "255.255.255.252", "255.255.255.254", "255.255.255.255" };
        String bcast[] = new String[] { "192.168.15.7", "192.168.15.7", "192.168.15.7" };
        String netwk[] = new String[] { "192.168.15.4", "192.168.15.6", "192.168.15.7" };
        String lowAd[] = new String[] { "192.168.15.5", "0.0.0.0", "0.0.0.0" };
        String highA[] = new String[] { "192.168.15.6", "0.0.0.0", "0.0.0.0" };
        String cidrS[] = new String[] { "192.168.15.7/30", "192.168.15.7/31", "192.168.15.7/32" };
        long usableAd[] = new long[] { 2, 0, 0 };
        // low and high addresses don't exist

        for (int i = 0; i < masks.length; ++i) {
            SubnetInfo info = SubnetUtils.getByMask(address, masks[i]);
            info.setInclusiveHostCount(false);
            assertEquals("ci " + masks[i], cidrS[i], info.getCidrSignature());
            assertEquals("bc " + masks[i], bcast[i], info.getBroadcastAddress());
            assertEquals("nw " + masks[i], netwk[i], info.getNetworkAddress());
            assertEquals("ac " + masks[i], usableAd[i], info.getAddressCountLong());
            assertEquals("lo " + masks[i], lowAd[i], info.getLowAddress());
            assertEquals("hi " + masks[i], highA[i], info.getHighAddress());
        }
    }

    public void testParseSimpleNetmaskInclusive() {
        String address = "192.168.15.7";
        String masks[] = new String[] { "255.255.255.252", "255.255.255.254", "255.255.255.255" };
        String bcast[] = new String[] { "192.168.15.7", "192.168.15.7", "192.168.15.7" };
        String netwk[] = new String[] { "192.168.15.4", "192.168.15.6", "192.168.15.7" };
        String lowAd[] = new String[] { "192.168.15.4", "192.168.15.6", "192.168.15.7" };
        String highA[] = new String[] { "192.168.15.7", "192.168.15.7", "192.168.15.7" };
        String cidrS[] = new String[] { "192.168.15.7/30", "192.168.15.7/31", "192.168.15.7/32" };
        long usableAd[] = new long[] { 4, 2, 1 };

        for (int i = 0; i < masks.length; ++i) {
            SubnetInfo info = SubnetUtils.getByMask(address, masks[i]);
            info.setInclusiveHostCount(true);
            assertEquals("ci " + masks[i], cidrS[i], info.getCidrSignature());
            assertEquals("bc " + masks[i], bcast[i], info.getBroadcastAddress());
            assertEquals("ac " + masks[i], usableAd[i], info.getAddressCountLong());
            assertEquals("nw " + masks[i], netwk[i], info.getNetworkAddress());
            assertEquals("lo " + masks[i], lowAd[i], info.getLowAddress());
            assertEquals("hi " + masks[i], highA[i], info.getHighAddress());
        }
    }

    public void testZeroAddressAndCidr() {
        SubnetUtils.getByCIDRNortation("0.0.0.0/0");
    }

    public void testNET521() {
        SubnetUtils utils;
        SubnetInfo info;

        info = SubnetUtils.getByCIDRNortation("0.0.0.0/0");
        info.setInclusiveHostCount(true);
        assertEquals("0.0.0.0", info.getNetmask());
        assertEquals(4294967296L, info.getAddressCountLong());
        try {
            info.getAddressCountLong();
            fail("Expected RuntimeException");
        } catch (RuntimeException expected) {
            // ignored
        }
        info = SubnetUtils.getByCIDRNortation("128.0.0.0/1");
        info.setInclusiveHostCount(true);
        assertEquals("128.0.0.0", info.getNetmask());
        assertEquals(2147483648L, info.getAddressCountLong());
        try {
            info.getAddressCountLong();
            fail("Expected RuntimeException");
        } catch (RuntimeException expected) {
            // ignored
        }
        // if we exclude the broadcast and network addresses, the count is less
        // than Integer.MAX_VALUE
        info.setInclusiveHostCount(false);
        assertEquals(2147483646, info.getAddressCountLong());
    }

    public void testNET624() {
        SubnetUtils.getByCIDRNortation("0.0.0.0/0");
        SubnetUtils.getByMask("0.0.0.0", "0.0.0.0");
        SubnetUtils.getByMask("0.0.0.0", "128.0.0.0");
        try {
            SubnetUtils.getByMask("0.0.0.0", "64.0.0.0");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Ignored
        }
        try {
            SubnetUtils.getByMask("0.0.0.0", "0.0.0.1");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // Ignored
        }
    }

    public void testNET520() {
        SubnetInfo info = SubnetUtils.getByCIDRNortation("0.0.0.0/0");
        info.setInclusiveHostCount(true);
        assertEquals("0.0.0.0", info.getNetworkAddress());
        assertEquals("255.255.255.255", info.getBroadcastAddress());
        assertTrue(info.isInRange("127.0.0.0"));
        info.setInclusiveHostCount(false);
        assertTrue(info.isInRange("127.0.0.0"));
    }
}
