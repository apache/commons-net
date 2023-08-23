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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation") // deliberate use of deprecated methods
public class SubnetUtilsTest {

    @Test
    public void testAddresses() {
        final SubnetUtils utils = new SubnetUtils("192.168.0.1/29");
        final SubnetInfo info = utils.getInfo();
        assertTrue(info.isInRange("192.168.0.1"));
        assertTrue(info.isInRange("192.168.0.2"));
        assertTrue(info.isInRange("192.168.0.3"));
        assertTrue(info.isInRange("192.168.0.4"));
        assertTrue(info.isInRange("192.168.0.5"));
        assertTrue(info.isInRange("192.168.0.6"));
        // We don't count the broadcast address as usable
        assertFalse(info.isInRange("192.168.0.7"));
        assertFalse(info.isInRange("192.168.0.8"));
        assertFalse(info.isInRange("10.10.2.1"));
        assertFalse(info.isInRange("192.168.1.1"));
        assertFalse(info.isInRange("192.168.0.255"));
        //
        assertEquals(-1062731775, info.asInteger("192.168.0.1"));
        assertThrows(IllegalArgumentException.class, () -> info.asInteger("bad"));
        //
        assertArrayEquals(new String[] { "192.168.0.1", "192.168.0.2", "192.168.0.3", "192.168.0.4", "192.168.0.5", "192.168.0.6" }, info.getAllAddresses());
    }

    @Test
    public void testAddressIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils("bad"));
    }

    /**
     * Test using the inclusiveHostCount flag, which includes the network and broadcast addresses in host counts
     */
    @Test
    public void testCidrAddresses() {
        SubnetUtils utils = new SubnetUtils("192.168.0.1/8");
        utils.setInclusiveHostCount(true);
        SubnetInfo info = utils.getInfo();
        assertEquals("255.0.0.0", info.getNetmask());
        assertEquals(16777216, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/0");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("0.0.0.0", info.getNetmask());
        assertEquals(4294967296L, info.getAddressCountLong());

        utils = new SubnetUtils("192.168.0.1/1");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("128.0.0.0", info.getNetmask());
        assertEquals(2147483648L, info.getAddressCountLong());

        utils = new SubnetUtils("192.168.0.1/9");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.128.0.0", info.getNetmask());
        assertEquals(8388608, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/10");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.192.0.0", info.getNetmask());
        assertEquals(4194304, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/11");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.224.0.0", info.getNetmask());
        assertEquals(2097152, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/12");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.240.0.0", info.getNetmask());
        assertEquals(1048576, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/13");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.248.0.0", info.getNetmask());
        assertEquals(524288, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/14");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.252.0.0", info.getNetmask());
        assertEquals(262144, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/15");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.254.0.0", info.getNetmask());
        assertEquals(131072, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/16");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.0.0", info.getNetmask());
        assertEquals(65536, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/17");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.128.0", info.getNetmask());
        assertEquals(32768, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/18");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.192.0", info.getNetmask());
        assertEquals(16384, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/19");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.224.0", info.getNetmask());
        assertEquals(8192, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/20");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.240.0", info.getNetmask());
        assertEquals(4096, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/21");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.248.0", info.getNetmask());
        assertEquals(2048, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/22");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.252.0", info.getNetmask());
        assertEquals(1024, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/23");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.254.0", info.getNetmask());
        assertEquals(512, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/24");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.255.0", info.getNetmask());
        assertEquals(256, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/25");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.255.128", info.getNetmask());
        assertEquals(128, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/26");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.255.192", info.getNetmask());
        assertEquals(64, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/27");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.255.224", info.getNetmask());
        assertEquals(32, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/28");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.255.240", info.getNetmask());
        assertEquals(16, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/29");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.255.248", info.getNetmask());
        assertEquals(8, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/30");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.255.252", info.getNetmask());
        assertEquals(4, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/31");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.255.254", info.getNetmask());
        assertEquals(2, info.getAddressCount());

        utils = new SubnetUtils("192.168.0.1/32");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("255.255.255.255", info.getNetmask());
        assertEquals(1, info.getAddressCount());

    }

    @Test
    public void testInvalidMasks() {
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils("192.168.0.1/33"));
    }

    @Test
    public void testNET428_31() {
        final SubnetUtils subnetUtils = new SubnetUtils("1.2.3.4/31");
        assertEquals(0, subnetUtils.getInfo().getAddressCount());
        final String[] address = subnetUtils.getInfo().getAllAddresses();
        assertNotNull(address);
        assertEquals(0, address.length);
    }

    @Test
    public void testNET428_32() {
        final SubnetUtils subnetUtils = new SubnetUtils("1.2.3.4/32");
        assertEquals(0, subnetUtils.getInfo().getAddressCount());
        final String[] address = subnetUtils.getInfo().getAllAddresses();
        assertNotNull(address);
        assertEquals(0, address.length);
    }

    @Test
    public void testNET520() {
        final SubnetUtils utils = new SubnetUtils("0.0.0.0/0");
        utils.setInclusiveHostCount(true);
        final SubnetInfo info = utils.getInfo();
        assertEquals("0.0.0.0", info.getNetworkAddress());
        assertEquals("255.255.255.255", info.getBroadcastAddress());
        assertTrue(info.isInRange("127.0.0.0"));
        utils.setInclusiveHostCount(false);
        assertTrue(info.isInRange("127.0.0.0"));
    }

    @Test
    public void testNET521() {
        SubnetUtils utils;
        SubnetInfo info;

        utils = new SubnetUtils("0.0.0.0/0");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("0.0.0.0", info.getNetmask());
        assertEquals(4294967296L, info.getAddressCountLong());
        assertThrows(RuntimeException.class, info::getAddressCount);
        utils = new SubnetUtils("128.0.0.0/1");
        utils.setInclusiveHostCount(true);
        info = utils.getInfo();
        assertEquals("128.0.0.0", info.getNetmask());
        assertEquals(2147483648L, info.getAddressCountLong());
        assertThrows(RuntimeException.class, info::getAddressCount);
        // if we exclude the broadcast and network addresses, the count is less than Integer.MAX_VALUE
        utils.setInclusiveHostCount(false);
        info = utils.getInfo();
        assertEquals(2147483646, info.getAddressCount());
    }

    @Test
    public void testNET624() {
        assertDoesNotThrow(() -> new SubnetUtils("0.0.0.0/0"));
        assertDoesNotThrow(() -> new SubnetUtils("0.0.0.0", "0.0.0.0"));
        assertDoesNotThrow(() -> new SubnetUtils("0.0.0.0", "128.0.0.0"));
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils("0.0.0.0", "64.0.0.0"));
        assertThrows(IllegalArgumentException.class, () -> new SubnetUtils("0.0.0.0", "0.0.0.1"));
    }

    @Test
    public void testNET641() {
        assertFalse(new SubnetUtils("192.168.1.0/00").getInfo().isInRange("0.0.0.0"));
        assertFalse(new SubnetUtils("192.168.1.0/30").getInfo().isInRange("0.0.0.0"));
        assertFalse(new SubnetUtils("192.168.1.0/31").getInfo().isInRange("0.0.0.0"));
        assertFalse(new SubnetUtils("192.168.1.0/32").getInfo().isInRange("0.0.0.0"));
    }

    @Test
    public void testNET675() {
        final SubnetUtils utils = new SubnetUtils("192.168.0.15/32");
        utils.setInclusiveHostCount(true);
        final SubnetInfo info = utils.getInfo();
        assertTrue(info.isInRange("192.168.0.15"));
    }

    @Test
    public void testNET679() {
        final SubnetUtils utils = new SubnetUtils("10.213.160.0/16");
        utils.setInclusiveHostCount(true);
        final SubnetInfo info = utils.getInfo();
        assertTrue(info.isInRange("10.213.0.0"));
        assertTrue(info.isInRange("10.213.255.255"));
    }

    @Test
    public void testNext() {
        final SubnetUtils utils = new SubnetUtils("192.168.0.1/29");
        assertEquals("192.168.0.2", utils.getNext().getInfo().getAddress());
    }

    @Test
    public void testParseSimpleNetmask() {
        final String address = "192.168.0.1";
        final String[] masks = { "255.0.0.0", "255.255.0.0", "255.255.255.0", "255.255.255.248" };
        final String[] bcastAddresses = { "192.255.255.255", "192.168.255.255", "192.168.0.255", "192.168.0.7" };
        final String[] lowAddresses = { "192.0.0.1", "192.168.0.1", "192.168.0.1", "192.168.0.1" };
        final String[] highAddresses = { "192.255.255.254", "192.168.255.254", "192.168.0.254", "192.168.0.6" };
        final String[] nextAddresses = { "192.168.0.2", "192.168.0.2", "192.168.0.2", "192.168.0.2" };
        final String[] previousAddresses = { "192.168.0.0", "192.168.0.0", "192.168.0.0", "192.168.0.0" };
        final String[] networkAddresses = { "192.0.0.0", "192.168.0.0", "192.168.0.0", "192.168.0.0" };
        final String[] cidrSignatures = { "192.168.0.1/8", "192.168.0.1/16", "192.168.0.1/24", "192.168.0.1/29" };
        final int[] usableAddresses = { 16777214, 65534, 254, 6 };

        for (int i = 0; i < masks.length; ++i) {
            final SubnetUtils utils = new SubnetUtils(address, masks[i]);
            final SubnetInfo info = utils.getInfo();
            assertEquals(address, info.getAddress());
            assertEquals(bcastAddresses[i], info.getBroadcastAddress());
            assertEquals(cidrSignatures[i], info.getCidrSignature());
            assertEquals(lowAddresses[i], info.getLowAddress());
            assertEquals(highAddresses[i], info.getHighAddress());
            assertEquals(nextAddresses[i], info.getNextAddress());
            assertEquals(previousAddresses[i], info.getPreviousAddress());
            assertEquals(networkAddresses[i], info.getNetworkAddress());
            assertEquals(usableAddresses[i], info.getAddressCount());
        }
    }

    @Test
    public void testParseSimpleNetmaskExclusive() {
        final String address = "192.168.15.7";
        final String[] masks = { "255.255.255.252", "255.255.255.254", "255.255.255.255" };
        final String[] bcast = { "192.168.15.7", "192.168.15.7", "192.168.15.7" };
        final String[] netwk = { "192.168.15.4", "192.168.15.6", "192.168.15.7" };
        final String[] lowAd = { "192.168.15.5", "0.0.0.0", "0.0.0.0" };
        final String[] highA = { "192.168.15.6", "0.0.0.0", "0.0.0.0" };
        final String[] cidrS = { "192.168.15.7/30", "192.168.15.7/31", "192.168.15.7/32" };
        final int[] usableAd = { 2, 0, 0 };
        // low and high addresses don't exist

        for (int i = 0; i < masks.length; ++i) {
            final SubnetUtils utils = new SubnetUtils(address, masks[i]);
            utils.setInclusiveHostCount(false);
            final SubnetInfo info = utils.getInfo();
            assertEquals(cidrS[i], info.getCidrSignature(), "ci " + masks[i]);
            assertEquals(bcast[i], info.getBroadcastAddress(), "bc " + masks[i]);
            assertEquals(netwk[i], info.getNetworkAddress(), "nw " + masks[i]);
            assertEquals(usableAd[i], info.getAddressCount(), "ac " + masks[i]);
            assertEquals(lowAd[i], info.getLowAddress(), "lo " + masks[i]);
            assertEquals(highA[i], info.getHighAddress(), "hi " + masks[i]);
        }
    }

    @Test
    public void testParseSimpleNetmaskInclusive() {
        final String address = "192.168.15.7";
        final String[] masks = { "255.255.255.252", "255.255.255.254", "255.255.255.255" };
        final String[] bcast = { "192.168.15.7", "192.168.15.7", "192.168.15.7" };
        final String[] netwk = { "192.168.15.4", "192.168.15.6", "192.168.15.7" };
        final String[] lowAd = { "192.168.15.4", "192.168.15.6", "192.168.15.7" };
        final String[] highA = { "192.168.15.7", "192.168.15.7", "192.168.15.7" };
        final String[] cidrS = { "192.168.15.7/30", "192.168.15.7/31", "192.168.15.7/32" };
        final int[] usableAd = { 4, 2, 1 };

        for (int i = 0; i < masks.length; ++i) {
            final SubnetUtils utils = new SubnetUtils(address, masks[i]);
            utils.setInclusiveHostCount(true);
            final SubnetInfo info = utils.getInfo();
            assertEquals(cidrS[i], info.getCidrSignature(), "ci " + masks[i]);
            assertEquals(bcast[i], info.getBroadcastAddress(), "bc " + masks[i]);
            assertEquals(usableAd[i], info.getAddressCount(), "ac " + masks[i]);
            assertEquals(netwk[i], info.getNetworkAddress(), "nw " + masks[i]);
            assertEquals(lowAd[i], info.getLowAddress(), "lo " + masks[i]);
            assertEquals(highA[i], info.getHighAddress(), "hi " + masks[i]);
        }
    }

    @Test
    public void testPrevious() {
        final SubnetUtils utils = new SubnetUtils("192.168.0.1/29");
        assertEquals("192.168.0.0", utils.getPrevious().getInfo().getAddress());
    }

    @Test
    public void testToString() {
        final SubnetUtils utils = new SubnetUtils("192.168.0.1/29");
        assertDoesNotThrow(() -> utils.toString());
        final SubnetInfo info = utils.getInfo();
        assertDoesNotThrow(() -> info.toString());
    }

    @Test
    public void testZeroAddressAndCidr() {
        final SubnetUtils snu = new SubnetUtils("0.0.0.0/0");
        assertNotNull(snu);
    }
}
