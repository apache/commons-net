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
package org.apache.commons.net.examples.cidr;

import java.nio.charset.Charset;
import java.util.Scanner;

import org.apache.commons.net.util.SubnetUtils6;
import org.apache.commons.net.util.SubnetUtils6.SubnetInfo;

/**
 * Example class that shows how to use the {@link SubnetUtils6} class.
 */
public class SubnetUtils6Example {

    public static void main(final String[] args) {
        final String subnet = "2001:db8:85a3::8a2e:370:7334/64";
        final SubnetUtils6 utils = new SubnetUtils6(subnet);
        final SubnetInfo info = utils.getInfo();

        System.out.printf("Subnet Information for %s:%n", subnet);
        System.out.println("--------------------------------------");
        System.out.printf("IP Address:\t\t\t%s%n", info.getAddress());
        System.out.printf("Prefix Length:\t\t\t%d%n", info.getPrefixLength());
        System.out.printf("CIDR Representation:\t\t%s%n%n", info.getCidrSignature());

        System.out.printf("Network Address:\t\t%s%n", info.getNetworkAddress());
        System.out.printf("Low Address:\t\t\t%s%n", info.getLowAddress());
        System.out.printf("High Address:\t\t\t%s%n", info.getHighAddress());

        System.out.printf("Total addresses in subnet:\t%s%n%n", info.getAddressCount());

        final String prompt = "Enter an IPv6 address (e.g., 2001:db8:85a3::1):";
        System.out.println(prompt);
        try (Scanner scanner = new Scanner(System.in, Charset.defaultCharset().name())) {
            while (scanner.hasNextLine()) {
                final String address = scanner.nextLine();
                System.out.println("The IP address [" + address + "] is " + (info.isInRange(address) ? "" : "not ") + "within the subnet [" + subnet + "]");
                System.out.println(prompt);
            }
        }
    }

}
