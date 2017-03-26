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

import java.util.regex.Pattern;

/**
 * This class that performs some subnet calculations given IP address in CIDR-notation.
 * <p>For IPv4 address subnet, especially Classless Inter-Domain Routing (CIDR),
 * refer to <a href="https://tools.ietf.org/html/rfc4632">RFC4632</a>.</p>
 * <p>For IPv6 address subnet, refer to <a href="https://tools.ietf.org/html/rfc4291#section-2.3">
 * Section 2.3 of RFC 4291</a>.</p>
 * @since 2.0
 */
public class SubnetUtils {

    private static final String IPV4_ADDRESS = "(\\d{1,3}\\.){3}\\d{1,3}/\\d{1,2}";
    private static final String IPV6_ADDRESS = "([0-9a-f]{1,4}\\:){7}[0-9a-f]{1,4}/\\d{1,3}";

    private final SubnetInfo subnetInfo;

    /**
     * Constructor that creates subnet summary information based on
     * the provided IPv4 or IPv6 address in CIDR-notation,
     * e.g. "192.168.0.1/16" or "2001:db8:0:0:0:ff00:42:8329/46"
     *
     * NOTE: IPv6 address does NOT allow to omit consecutive sections of zeros in the current version.
     *
     * @param cidrNotation IPv4 or IPv6 address,
     * e.g. "192.168.0.1/16" or "2001:db8:0:0:0:ff00:42:8329/46"
     * @throws IllegalArgumentException if the parameter is invalid,
     * e.g. does not match either n.n.n.n/m where n = 1-3 decimal digits, m = 1-2 decimal digits in range 0-32; or
     * n:n:n:n:n:n:n:n/m n = 1-4 hexadecimal digits, m = 1-3 decimal digits in range 0-128.
     */
    public SubnetUtils(String cidrNotation) {
        subnetInfo = getByCIDRNortation(cidrNotation);
    }

    /**
     * Constructor that creates IPv4 subnet summary information,
     * given a dotted decimal address and mask.
     *
     * @param address an IP address, e.g. "192.168.0.1"
     * @param mask a dotted decimal netmask e.g. "255.255.0.0"
     * @throws IllegalArgumentException if the address or mask is invalid,
     * e.g. the address does not match n.n.n.n where n=1-3 decimal digits, or
     * the mask does not match n.n.n.n which n={0, 128, 192, 224, 240, 248, 252, 254, 255}
     * and after the 0-field, it is all zeros.
     */
    public SubnetUtils(String address, String mask) {
        subnetInfo = new IP4Subnet(address, mask);
    }

    /**
     * Returns <code>true</code> if the return value of {@link SubnetInfo#getAddressCount()}
     * includes the network and broadcast addresses.
     * @return true if the host count includes the network and broadcast addresses
     * @since 2.2
     */
    public boolean isInclusiveHostCount() {
        return subnetInfo.isInclusiveHostCount();
    }

    /**
     * Set to <code>true</code> if you want the return value of {@link SubnetInfo#getAddressCount()}
     * to include the network and broadcast addresses.
     * @param inclusiveHostCount true if network and broadcast addresses are to be included
     * @since 2.2
     */
    public void setInclusiveHostCount(boolean inclusiveHostCount) {
        subnetInfo.setInclusiveHostCount(inclusiveHostCount);
    }

    /**
     * Creates subnet summary information based on the provided IPv4 or IPv6 address in CIDR-notation,
     * e.g. "192.168.0.1/16" or "2001:db8:0:0:0:ff00:42:8329/46"
     *
     * NOTE: IPv6 address does NOT allow to omit consecutive sections of zeros in the current version.
     *
     * @param cidrNotation IPv4 or IPv6 address
     * @return a SubnetInfo object created from the IP address.
     * @since 3.7
     */
    public static SubnetInfo getByCIDRNortation(String cidrNotation) {
        if (Pattern.matches(IPV4_ADDRESS, cidrNotation)) {
            return new IP4Subnet(cidrNotation);
        } else if (Pattern.matches(IPV6_ADDRESS, cidrNotation)) {
            return new IP6Subnet(cidrNotation);
        } else {
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
     * @since 3.7
     */
    public static IP4Subnet getByMask(String address, String mask) {
        return new IP4Subnet(address, mask);
    }

    /**
     * Convenience container for subnet summary information.
     * @since 3.7 (reorganized)
     */
    public static class SubnetInfo {

        /*
         * Convenience function to check integer boundaries. Checks if a value x
         * is in the range [begin,end]. Returns x if it is in range, throws an
         * exception otherwise.
         */
        static int rangeCheck(int value, int begin, int end) {
            if (value < begin || value > end) {
                throw new IllegalArgumentException("Value [" + value + "] not in range [" + begin + "," + end + "]");
            }

            return value;
        }

        /*
         * Count the number of 1-bits in a 32-bit integer using a
         * divide-and-conquer strategy see Hacker's Delight section 5.1
         */
        static int pop(int x) {
            x = x - ((x >>> 1) & 0x55555555);
            x = (x & 0x33333333) + ((x >>> 2) & 0x33333333);
            x = (x + (x >>> 4)) & 0x0F0F0F0F;
            x = x + (x >>> 8);
            x = x + (x >>> 16);
            return x & 0x3F;
        }

        /*
         * Converts an integer array into a decimal format separated by symbol.
         */
        static String format(int[] arry, String symbol) {
            StringBuilder str = new StringBuilder();
            final int iMax = arry.length - 1;

            for (int i = 0; i <= iMax; i++) {
                str.append(arry[i]);

                if (i != iMax) {
                    str.append(symbol);
                }
            }

            return str.toString();
        }

        /**
         * Converts a dotted decimal format address to a packed integer format. (ONLY USE in IPv4)
         *
         * @param address a dotted decimal format address
         * @return a packed integer of a dotted decimal format address
         */
        public int asInteger(String address) { return 0; }

        /**
         * Returns <code>true</code> if the return value of {@link #getAddressCount()}
         * includes the network and broadcast addresses. (ONLY USE in IPv4)
         *
         * @return true if the host count includes the network and broadcast addresses
         */
        public boolean isInclusiveHostCount() { return false; }

        /**
         * Sets to <code>true</code> if you want the return value of {@link #getAddressCount()}
         * to include the network and broadcast addresses. (ONLY USE in IPv4)
         *
         * @param inclusiveHostCount true if network and broadcast addresses are to be included
         */
        public void setInclusiveHostCount(boolean inclusiveHostCount) {}

        /**
         * Returns true if the parameter <code>address</code> is in the
         * range of usable endpoint addresses for this subnet. This excludes the
         * network and broadcast addresses if the address is IPv4 address.
         *
         * @param address a dot-delimited IPv4 address, e.g. "192.168.0.1", or
         * a colon-hexadecimal IPv6 address, e.g. "2001:db8::ff00:42:8329"
         * @return true if in range, false otherwise
         */
        public boolean isInRange(String address) { return false; }

        /**
         * Returns true if the parameter <code>address</code> is in the
         * range of usable endpoint addresses for this subnet. This excludes the
         * network and broadcast addresses if the address is IPv4 address.
         *
         * @param address the address to check
         * @return true if it is in range
         */
        public boolean isInRange(int address) { return false; }

        /**
         * Returns true if the parameter <code>address</code> is in the
         * range of usable endpoint addresses for this subnet.
         *
         * @param address the address to check
         * @return true if it is in range
         */
        public boolean isInRange(int[] address) { return false; }

        /**
         * Returns the IP address.
         * IPv4 format: the dot-decimal format, e.g. "192.168.0.1"
         * IPv6 format: the colon-hexadecimal format, e.g. "2001:db8::ff00:42:8329"
         *
         * @return a string of the IP address
         */
        public String getAddress() { return null; }

        /**
         * Returns the CIDR suffixes, the count of consecutive 1 bits in the subnet mask.
         * The range in IPv4 is 0-32, and in IPv6 is 0-128, actually 64 or less.
         *
         * @return the CIDR suffixes of the address in an integer.
         */
        public int getCIDR() { return 0; }

        /**
         * Returns a netmask in the address. (ONLY USE IPv4)
         *
         * @return a string of netmask in a dot-decimal format.
         */
        public String getNetmask() { return null; }

        /**
         * Returns a network address in the address. (ONLY USE IPv4)
         *
         * @return a string of a network address in a dot-decimal format.
         */
        public String getNetworkAddress() { return null; }

        /**
         * Returns a broadcast address in the address. (ONLY USE IPv4)
         *
         * @return a string of a broadcast address in a dot-decimal format.
         */
        public String getBroadcastAddress() { return null; }

        /**
         * Returns a CIDR notation, in which the address is followed by slash and
         * the count of counting the 1-bit population in the subnet mask.
         * IPv4 CIDR notation: e.g. "192.168.0.1/24"
         * IPv6 CIDR notation: e.g. "2001:db8::ff00:42:8329/48"
         *
         * @return the CIDR notation of the address
         */
        public String getCIDRNotation() { return null; }

        /**
         * Returns a CIDR notation, in which the address is followed by slash and
         * the count of counting the 1-bit population in the subnet mask.
         * IPv4 CIDR notation: e.g. "192.168.0.1/24"
         * IPv6 CIDR notation: e.g. "2001:db8::ff00:42:8329/48"
         *
         * @return the CIDR notation of the address
         */
        public String getCidrSignature() {
            return getCIDRNotation();
        }

        /**
         * Returns the lowest address as a dotted decimal or
         * the colon-separated hexadecimal IP address.
         * Will be zero for CIDR/31 and CIDR/32 if the address is IPv4 address and
         * the inclusive flag is <code>false</code>.
         *
         * @return the IP address in dotted or colon 16-bit delimited format,
         * may be "0.0.0.0" or "::" if there is no valid address
         */
        public String getLowAddress() { return null; }

        /**
         * Returns the highest address as the dotted decimal or
         * the colon-separated hexadecimal IP address.
         * Will be zero for CIDR/31 and CIDR/32 if the address is IPv4 address and
         * the inclusive flag is <code>false</code>.
         *
         * @return the IP address in dotted or colon 16-bit delimited format,
         * may be "0.0.0.0" or "::" if there is no valid address
         */
        public String getHighAddress() { return null; }

        /**
         * Get the count of available addresses.
         * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
         * @return the count of addresses, may be zero.
         * @throws RuntimeException if the correct count is greater than {@code Integer.MAX_VALUE}
         * @deprecated (3.4) use {@link #getAddressCountLong()} instead
         */
        @Deprecated
        public int getAddressCount() {
            long countLong = getAddressCountLong();
            if (countLong > Integer.MAX_VALUE) {
                throw new RuntimeException("Count is larger than an integer: " + countLong);
            }
            // N.B. cannot be negative
            return (int)countLong;
        }

        /**
         * Returns the count of available addresses.
         * Will be zero for CIDR/31 and CIDR/32 if the address is IPv4 address and
         * the {@link IP4Subnet#inclusiveHostCount} flag is <code>false</code>.
         *
         * @return the count of addresses, may be zero
         */
        public long getAddressCountLong() { return 0; }

        /**
         * Returns the count of available addresses.
         * Will be zero for CIDR/31 and CIDR/32 if the address is IPv4 address and
         * the {@link IP4Subnet#inclusiveHostCount} flag is <code>false</code>
         *
         * @return the count of addresses in a string, may be zero
         */
        public String getAddressCountString() { return null; }


        /**
         * Returns a list of the available addresses.
         *
         * @return an array of the available addresses
         * @deprecated (3.7) overflow if the available addresses are greater than {@code Integer.MAX_VALUE}
         */
        @Deprecated
        public String[] getAllAddresses() { return new String[0]; }

    }

    /**
     * Return a {@link SubnetInfo} instance that contains subnet-specific statistics
     * @return new instance
     */
    public final SubnetInfo getInfo() { return subnetInfo; }

}
