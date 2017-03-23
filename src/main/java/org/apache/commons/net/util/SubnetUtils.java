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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.util.subnet.IP4Subnet;
import org.apache.commons.net.util.subnet.IP6Subnet;
import org.apache.commons.net.util.subnet.SubnetInfo;

/**
 * This class that performs some subnet calculations given IP address in CIDR-notation.
 * <p>For IPv4 address subnet, especially Classless Inter-Domain Routing (CIDR),
 * refer to <a href="https://tools.ietf.org/html/rfc4632">RFC4632</a>.</p>
 * <p>For IPv6 address subnet, refer to <a href="https://tools.ietf.org/html/rfc4291#section-2.3">
 * Section 2.3 of RFC 4291</a>.</p>
 * @since 2.0
 */
public class SubnetUtils {

    private static final String IP_ADDRESS = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";
    private static final String SLASH_FORMAT = IP_ADDRESS + "/(\\d{1,2})"; // 0 -> 32
    private static final Pattern addressPattern = Pattern.compile(IP_ADDRESS);
    private static final Pattern cidrPattern = Pattern.compile(SLASH_FORMAT);
    private static final int NBITS = 32;

    private final int netmask;
    private final int address;
    private final int network;
    private final int broadcast;

    /** Whether the broadcast/network address are included in host count */
    private boolean inclusiveHostCount = false;
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
     * @since 2.2
     * @return true if the host count includes the network and broadcast addresses
     */
    public boolean isInclusiveHostCount() {
        return inclusiveHostCount;
    }

    /**
     * Set to <code>true</code> if you want the return value of {@link SubnetInfo#getAddressCount()}
     * to include the network and broadcast addresses.
     * @param inclusiveHostCount true if network and broadcast addresses are to be included
     * @since 2.2
     */
    public void setInclusiveHostCount(boolean inclusiveHostCount) {
        this.inclusiveHostCount = inclusiveHostCount;
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
     *
     */
    public final class SubnetInfo {
        /* Mask to convert unsigned int to a long (i.e. keep 32 bits) */
        private static final long UNSIGNED_INT_MASK = 0x0FFFFFFFFL;

        private SubnetInfo() {}

        // long versions of the values (as unsigned int) which are more suitable for range checking
        private long networkLong()  { return network &  UNSIGNED_INT_MASK; }
        private long broadcastLong(){ return broadcast &  UNSIGNED_INT_MASK; }

        private int low() {
            return (isInclusiveHostCount() ? network :
                broadcastLong() - networkLong() > 1 ? network + 1 : 0);
        }

        private int high() {
            return (isInclusiveHostCount() ? broadcast :
                broadcastLong() - networkLong() > 1 ? broadcast -1  : 0);
        }

        /**
         * Returns true if the parameter <code>address</code> is in the
         * range of usable endpoint addresses for this subnet. This excludes the
         * network and broadcast addresses.
         * @param address A dot-delimited IPv4 address, e.g. "192.168.0.1"
         * @return True if in range, false otherwise
         */
        public boolean isInRange(String address) {
            return isInRange(toInteger(address));
        }

        /**
         *
         * @param address the address to check
         * @return true if it is in range
         * @since 3.4 (made public)
         */
        public boolean isInRange(int address) {
            long addLong = address & UNSIGNED_INT_MASK;
            long lowLong = low() & UNSIGNED_INT_MASK;
            long highLong = high() & UNSIGNED_INT_MASK;
            return addLong >= lowLong && addLong <= highLong;
        }

        public String getBroadcastAddress() {
            return format(toArray(broadcast));
        }

        public String getNetworkAddress() {
            return format(toArray(network));
        }

        public String getNetmask() {
            return format(toArray(netmask));
        }

        public String getAddress() {
            return format(toArray(address));
        }

        /**
         * Return the low address as a dotted IP address.
         * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
         *
         * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
         */
        public String getLowAddress() {
            return format(toArray(low()));
        }

        /**
         * Return the high address as a dotted IP address.
         * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
         *
         * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
         */
        public String getHighAddress() {
            return format(toArray(high()));
        }

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
         * Get the count of available addresses.
         * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
         * @return the count of addresses, may be zero.
         * @since 3.4
         */
        public long getAddressCountLong() {
            long b = broadcastLong();
            long n = networkLong();
            long count = b - n + (isInclusiveHostCount() ? 1 : -1);
            return count < 0 ? 0 : count;
        }

        public int asInteger(String address) {
            return toInteger(address);
        }

        public String getCidrSignature() {
            return format(toArray(address)) + "/" + pop(netmask);
        }

        public String[] getAllAddresses() {
            int ct = getAddressCount();
            String[] addresses = new String[ct];
            if (ct == 0) {
                return addresses;
            }
            for (int add = low(), j=0; add <= high(); ++add, ++j) {
                addresses[j] = format(toArray(add));
            }
            return addresses;
        }

        /**
         * {@inheritDoc}
         * @since 2.2
         */
        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder();
            buf.append("CIDR Signature:\t[").append(getCidrSignature()).append("]")
                .append(" Netmask: [").append(getNetmask()).append("]\n")
                .append("Network:\t[").append(getNetworkAddress()).append("]\n")
                .append("Broadcast:\t[").append(getBroadcastAddress()).append("]\n")
                 .append("First Address:\t[").append(getLowAddress()).append("]\n")
                 .append("Last Address:\t[").append(getHighAddress()).append("]\n")
                 .append("# Addresses:\t[").append(getAddressCount()).append("]\n");
            return buf.toString();
        }
    }

    /**
     * Return a {@link SubnetInfo} instance that contains subnet-specific statistics
     * @return new instance
     */
    public final SubnetInfo getInfo() { return new SubnetInfo(); }

    /*
     * Convert a dotted decimal format address to a packed integer format
     */
    private static int toInteger(String address) {
        Matcher matcher = addressPattern.matcher(address);
        if (matcher.matches()) {
            return matchAddress(matcher);
        } else {
            throw new IllegalArgumentException("Could not parse [" + address + "]");
        }
    }

    /*
     * Convenience method to extract the components of a dotted decimal address and
     * pack into an integer using a regex match
     */
    private static int matchAddress(Matcher matcher) {
        int addr = 0;
        for (int i = 1; i <= 4; ++i) {
            int n = (rangeCheck(Integer.parseInt(matcher.group(i)), 0, 255));
            addr |= ((n & 0xff) << 8*(4-i));
        }
        return addr;
    }

    /*
     * Convert a packed integer address into a 4-element array
     */
    private int[] toArray(int val) {
        int ret[] = new int[4];
        for (int j = 3; j >= 0; --j) {
            ret[j] |= ((val >>> 8*(3-j)) & (0xff));
        }
        return ret;
    }

    /*
     * Convert a 4-element array into dotted decimal format
     */
    private String format(int[] octets) {
        StringBuilder str = new StringBuilder();
        for (int i =0; i < octets.length; ++i){
            str.append(octets[i]);
            if (i != octets.length - 1) {
                str.append(".");
            }
        }
        return str.toString();
    }

    /*
     * Convenience function to check integer boundaries.
     * Checks if a value x is in the range [begin,end].
     * Returns x if it is in range, throws an exception otherwise.
     */
    private static int rangeCheck(int value, int begin, int end) {
        if (value >= begin && value <= end) { // (begin,end]
            return value;
        }

        throw new IllegalArgumentException("Value [" + value + "] not in range ["+begin+","+end+"]");
    }

    /*
     * Count the number of 1-bits in a 32-bit integer using a divide-and-conquer strategy
     * see Hacker's Delight section 5.1
     */
    int pop(int x) {
        x = x - ((x >>> 1) & 0x55555555);
        x = (x & 0x33333333) + ((x >>> 2) & 0x33333333);
        x = (x + (x >>> 4)) & 0x0F0F0F0F;
        x = x + (x >>> 8);
        x = x + (x >>> 16);
        return x & 0x0000003F;
    }

}
