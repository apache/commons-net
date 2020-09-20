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
package org.apache.commons.net.examples.cidr;

import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

/**
 * Example class that shows how to use the {@link SubnetUtils} class.
 *
 */
public class SubnetUtilsExample {

    public static void main(final String[] args) {
        final String subnet = "192.168.0.3/31";
        final SubnetUtils utils = new SubnetUtils(subnet);
        final SubnetInfo info = utils.getInfo();

        System.out.printf("Subnet Information for %s:%n", subnet);
        System.out.println("--------------------------------------");
        System.out.printf("IP Address:\t\t\t%s\t[%s]%n", info.getAddress(),
                Integer.toBinaryString(info.asInteger(info.getAddress())));
        System.out.printf("Netmask:\t\t\t%s\t[%s]%n", info.getNetmask(),
                Integer.toBinaryString(info.asInteger(info.getNetmask())));
        System.out.printf("CIDR Representation:\t\t%s%n%n", info.getCidrSignature());

        System.out.printf("Supplied IP Address:\t\t%s%n%n", info.getAddress());

        System.out.printf("Network Address:\t\t%s\t[%s]%n", info.getNetworkAddress(),
                Integer.toBinaryString(info.asInteger(info.getNetworkAddress())));
        System.out.printf("Broadcast Address:\t\t%s\t[%s]%n", info.getBroadcastAddress(),
                Integer.toBinaryString(info.asInteger(info.getBroadcastAddress())));
        System.out.printf("Low Address:\t\t\t%s\t[%s]%n", info.getLowAddress(),
                Integer.toBinaryString(info.asInteger(info.getLowAddress())));
        System.out.printf("High Address:\t\t\t%s\t[%s]%n", info.getHighAddress(),
                Integer.toBinaryString(info.asInteger(info.getHighAddress())));

        System.out.printf("Total usable addresses: \t%d%n", Long.valueOf(info.getAddressCountLong()));
        System.out.printf("Address List: %s%n%n", Arrays.toString(info.getAllAddresses()));

        final String prompt = "Enter an IP address (e.g. 192.168.0.10):";
        System.out.println(prompt);
        try (final Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                final String address = scanner.nextLine();
                System.out.println("The IP address [" + address + "] is " + (info.isInRange(address) ? "" : "not ")
                        + "within the subnet [" + subnet + "]");
                System.out.println(prompt);
            }
        }
    }

}
