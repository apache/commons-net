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

/**
 * A class that performs some subnet calculations given a network address and a subnet mask.
 * @see "http://www.faqs.org/rfcs/rfc1519.html"
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


    /**
     * Constructor that takes a CIDR-notation string, e.g. "192.168.0.1/16"
     * @param cidrNotation A CIDR-notation string, e.g. "192.168.0.1/16"
     * @throws IllegalArgumentException if the parameter is invalid,
     * i.e. does not match n.n.n.n/m where n=1-3 decimal digits, m = 1-2 decimal digits in range 0-32
     */
    public SubnetUtils(String cidrNotation) {
      Matcher matcher = cidrPattern.matcher(cidrNotation);

      if (matcher.matches()) {
          this.address = matchAddress(matcher);

          /* Create a binary netmask from the number of bits specification /x */

          int trailingZeroes = NBITS - rangeCheck(Integer.parseInt(matcher.group(5)), 0, NBITS);
          /*
           * An IPv4 netmask consists of 32 bits, a contiguous sequence 
           * of the specified number of ones followed by all zeros.
           * So, it can be obtained by shifting an unsigned integer (32 bits) to the left by
           * the number of trailing zeros which is (32 - the # bits specification).
           * Note that there is no unsigned left shift operator, so we have to use
           * a long to ensure that the left-most bit is shifted out correctly.
           */
          this.netmask = (int) (0x0FFFFFFFFL << trailingZeroes );

          /* Calculate base network address */
          this.network = (address & netmask);

          /* Calculate broadcast address */
          this.broadcast = network | ~(netmask);
      } else {
          throw new IllegalArgumentException("Could not parse [" + cidrNotation + "]");
      }
    }

    /**
     * Constructor that takes a dotted decimal address and a dotted decimal mask.
     * @param address An IP address, e.g. "192.168.0.1"
     * @param mask A dotted decimal netmask e.g. "255.255.0.0"
     * @throws IllegalArgumentException if the address or mask is invalid,
     * i.e. does not match n.n.n.n where n=1-3 decimal digits and the mask is not all zeros
     */
    public SubnetUtils(String address, String mask) {
        this.address = toInteger(address);
        this.netmask = toInteger(mask);

        if ((this.netmask & -this.netmask) - 1 != ~this.netmask) {
            throw new IllegalArgumentException("Could not parse [" + mask + "]");
        }

        /* Calculate base network address */
        this.network = (this.address & this.netmask);

        /* Calculate broadcast address */
        this.broadcast = this.network | ~(this.netmask);
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
         * Returns the count of available addresses.
         * Will be zero for CIDR/31 and CIDR/32 if the address is IPv4 address and
         * the inclusive flag is <code>false</code>.
         *
         * @return the count of addresses, may be zero
         */
        public long getAddressCountLong() { return 0; }

        /**
         * Returns the count of available addresses.
         * Will be zero for CIDR/31 and CIDR/32 if the address is IPv4 address and
         * the inclusive flag is <code>false</code>.
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
        public String[] getAllAddresses() { return new String[0]; }

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
