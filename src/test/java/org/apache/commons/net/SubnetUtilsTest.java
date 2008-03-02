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

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

import junit.framework.TestCase;

public class SubnetUtilsTest extends TestCase {
	
	public void testParseSimpleNetmask() {
		final String address = "192.168.0.1";
		final String masks[] = new String[] { "255.0.0.0", "255.255.0.0", "255.255.255.0", "255.255.255.248"};
		final String bcastAddresses[] = new String[] { "192.255.255.255", "192.168.255.255", "192.168.0.255", "192.168.0.7"};
		final String lowAddresses[] = new String[] { "192.0.0.1", "192.168.0.1", "192.168.0.1", "192.168.0.1" };
		final String highAddresses[] = new String[] { "192.255.255.254", "192.168.255.254", "192.168.0.254", "192.168.0.6" };
		final String networkAddresses[] = new String[] { "192.0.0.0", "192.168.0.0", "192.168.0.0", "192.168.0.0" };
		final String cidrSignatures[] = new String[] { "192.168.0.1/8", "192.168.0.1/16", "192.168.0.1/24", "192.168.0.1/29" };
		final int usableAddresses[] = new int[] { 16777214, 65534, 254, 6 };
		
		for (int i = 0; i < masks.length; ++i) {
			SubnetUtils utils = new SubnetUtils(address, masks[i]);
			SubnetInfo info = utils.getInfo();
			assertEquals(bcastAddresses[i], info.getBroadcastAddress());
			assertEquals(cidrSignatures[i], info.getCidrSignature());
			assertEquals(lowAddresses[i], info.getLowAddress());
			assertEquals(highAddresses[i], info.getHighAddress());
			assertEquals(networkAddresses[i], info.getNetworkAddress());
			assertEquals(usableAddresses[i], info.getAddressCount());
		}
	}
	
	public void testAddresses() {
		SubnetUtils utils = new SubnetUtils("192.168.0.1/29");
		SubnetInfo info = utils.getInfo();
		assertTrue(info.isInRange("192.168.0.1"));
		// We dont count the broadcast address as usable
		assertFalse(info.isInRange("192.168.0.7"));
		assertFalse(info.isInRange("192.168.0.8"));
		assertFalse(info.isInRange("10.10.2.1"));
		assertFalse(info.isInRange("192.168.1.1"));
		assertFalse(info.isInRange("192.168.0.255"));
	}
}
