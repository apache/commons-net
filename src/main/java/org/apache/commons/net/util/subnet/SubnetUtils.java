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
package org.apache.commons.net.util.subnet;

import java.util.regex.Pattern;

/**
 * This class that performs some subnet calculations given IP address in CIDR-notation.
 * <p>For IPv4 address subnet, especially Classless Inter-Domain Routing (CIDR),
 * refer to <a href="https://tools.ietf.org/html/rfc4632">RFC4632</a>.</p>
 * <p>For IPv6 address subnet, refer to <a href="https://tools.ietf.org/html/rfc4291#section-2.3">
 * Section 2.3 of RFC 4291</a>.</p>
 *
 * @since 3.7 (moved from the util package and reorganized)
 */
public class SubnetUtils
{

    private static final String IPV4_ADDRESS = "(\\d{1,3}\\.){3}\\d{1,3}/\\d{1,2}";
    private static final String IPV6_ADDRESS = "([0-9a-f]{1,4}\\:){7}[0-9a-f]{1,4}/\\d{1,3}";

    /**
     * Creates subnet summary information based on the provided IPv4 or IPv6 address in CIDR-notation,
     * e.g. "192.168.0.1/16" or "2001:db8:0:0:0:ff00:42:8329/46"
     *
     * NOTE: IPv6 address does NOT allow to omit consecutive sections of zeros in the current version.
     *
     * @param cidrNotation IPv4 or IPv6 address
     * @return a SubnetInfo object created from the IP address.
     */
    public static SubnetInfo getByCIDRNortation(String cidrNotation)
    {
        if (Pattern.matches(IPV4_ADDRESS, cidrNotation))
        {
            return new IP4Subnet(cidrNotation);
        }
        else if (Pattern.matches(IPV6_ADDRESS, cidrNotation))
        {
            return new IP6Subnet(cidrNotation);
        }
        else
        {
            throw new IllegalArgumentException("Could not parse [" + cidrNotation + "]");
        }
    }

    /**
     * Creates IPv4 subnet summary information, given a dotted decimal address and mask.
     *
     * @param address an IP address, e.g. "192.168.0.1"
     * @param mask a dotted decimal netmask e.g. "255.255.0.0"
     * @throws IllegalArgumentException if the address or mask is invalid,
     * e.g. the address does not match n.n.n.n where n=1-3 decimal digits, or
     * the mask does not match n.n.n.n which n={0, 128, 192, 224, 240, 248, 252, 254, 255}
     * and after the 0-field, it is all zeros.
     * @return an IP4Subnet object generated based on <code>address</code> and <code>mask</code>.
     */
    public static IP4Subnet getByMask(String address, String mask)
    {
        return new IP4Subnet(address, mask);
    }

}
