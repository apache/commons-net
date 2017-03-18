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

/**
 * Convenience container for IPv4 subnet summary information.
 * @see "https://tools.ietf.org/html/rfc4632"
 * @see "https://tools.ietf.org/html/rfc1519"
 * @since 3.7
 */
public final class IP4Subnet implements SubnetInfo {

    /* Mask to convert unsigned int to a long (i.e. keep 32 bits) */
    private static final long UNSIGNED_INT_MASK = 0x0FFFFFFFFL;
    private static final int NBITS = 32;

    private final int address;
    private final int cidr;
    private final int netmask;
    private final int network;
    private final int broadcast;

    /* Whether the broadcast/network address on IPv4 or the network address on IPv6 are included in host count */
    private boolean inclusiveHostCount = false;

    /*
     * Constructor that takes a CIDR-notation string, e.g. "192.168.0.1/16"
     *
     * @param cidrNotation A CIDR-notation string, e.g. "192.168.0.1/16"
     * @throws IllegalArgumentException
     *             if the parameter is invalid,
     *             i.e. does not match n.n.n.n/m where n=1-3 decimal digits, m is in range 0-32
     */
    IP4Subnet(String cidrNotation) {
        String[] addr = cidrNotation.split("/");

        this.address = toInteger(addr[0]);

        /* Create a network prefix, CIDR, from the number of bits specification /x  */
        this.cidr = SubnetUtils.rangeCheck(Integer.parseInt(addr[1]), 0, NBITS);

        /*
         * Create a binary netmask from the number of bits specification /x
         * An IPv4 netmask consists of 32 bits, a contiguous sequence
         * of the specified number of ones followed by all zeros.
         * So, it can be obtained by shifting an unsigned integer (32 bits) to the left by
         * the number of trailing zeros which is (32 - the # bits specification).
         * Note that there is no unsigned left shift operator, so we have to use
         * a long to ensure that the left-most bit is shifted out correctly.
         */
        this.netmask = (int) (UNSIGNED_INT_MASK << (NBITS - cidr));

        /* Calculate base network address */
        this.network = (address & netmask);

        /* Calculate broadcast address */
        this.broadcast = network | ~(netmask);
    }//IP4Subnet

    /**
     * Constructor that takes a dotted decimal address and a dotted decimal mask.
     * @param address An IP address, e.g. "192.168.0.1"
     * @param mask A dotted decimal netmask e.g. "255.255.0.0"
     * @throws IllegalArgumentException if the address or mask is invalid,
     * i.e. does not match n.n.n.n where n=1-3 decimal digits and the mask is not all zeros
     */
    IP4Subnet(String address, String mask) {
        this.address = toInteger(address);
        this.netmask = toInteger(mask);

        /*
         * Check the subnet mask
         *
         * An IPv4 subnet mask must consist of a set of contiguous 1-bits followed by a block of 0-bits.
         * If the mask follows the format, the numbers of subtracting one from the lowest one bit of the mask,
         * see Hacker's Delight section 2.1, equals to the bitwise complement of the mask.
         */
        if ((this.netmask & -this.netmask) - 1 != ~this.netmask) {
            throw new IllegalArgumentException("Could not parse [" + mask + "]");
        }

        this.cidr = SubnetUtils.pop(this.netmask);

        /* Calculate base network address */
        this.network = (this.address & this.netmask);

        /* Calculate broadcast address */
        this.broadcast = this.network | ~(this.netmask);
    }

    /**
     * Returns <code>true</code> if the return value of {@link IP4Subnet#getAddressCount()}
     * includes the network and broadcast addresses.
     * @return true if the host count includes the network and broadcast addresses
     */
    @Override
    public boolean isInclusiveHostCount() {
        return inclusiveHostCount;
    }

    /**
     * Set to <code>true</code> if you want the return value of {@link IP4Subnet#getAddressCount()}
     * to include the network and broadcast addresses.
     * @param inclusiveHostCount true if network and broadcast addresses are to be included
     */
    @Override
    public void setInclusiveHostCount(boolean inclusiveHostCount) {
        this.inclusiveHostCount = inclusiveHostCount;
    }

    // long versions of the values (as unsigned int) which are more suitable for range checking
    private long networkLong() {
        return network & UNSIGNED_INT_MASK;
    }

    private long broadcastLong() {
        return broadcast & UNSIGNED_INT_MASK;
    }

    /*
     * Creates the minimum address in the network to which the address belongs.
     *
     * inclusiveHostCount
     *  - true the network address
     *  - false the first address of the available as host addresses or 0 if no corresponding address.
     */
    private int low() {
        return inclusiveHostCount ? network : (broadcastLong() - networkLong()) > 1 ? network + 1 : 0;
    }

    /*
     * Creates the minimum address in the network to which the address belongs.
     *
     * inclusiveHostCount
     *  - true the network address
     *  - false the last address of the available as host addresses or 0 if no corresponding address.
     */
    private int high() {
        return inclusiveHostCount ? broadcast : (broadcastLong() - networkLong()) > 1 ? broadcast - 1 : 0;
    }

    /*
     * Converts a packed integer address into dotted decimal format
     */
    private String format(int val) {
        int ret[] = new int[4];
        for (int i = 3; i >= 0; i--) {
            ret[i] = (val >>> (8 * (3 - i))) & 0xff;
        }

        return SubnetUtils.format(ret, ".");
    }

    /*
     * Converts a packed integer address into dotted decimal format
     */
    static int toInteger(String address) {
        String[] addrArry = address.split("\\.");

        // Check the length of the array, must be 4
        if (addrArry.length != 4) {
            throw new IllegalArgumentException("Could not parse [" + address + "]");
        }

        /* Check range of each element and convert to integer */
        int addr = 0;
        for (int i = 0; i < 4; i++) {
            int n = SubnetUtils.rangeCheck(Integer.parseInt(addrArry[i]), 0, 255);
            addr |= (n & 0xff) << (8 * (3 - i));
        }

        return addr;
    }

    /**
     * Returns true if the parameter <code>address</code> is in the
     * range of usable endpoint addresses for this subnet. This excludes the
     * network and broadcast addresses.
     * @param address a dot-delimited IPv4 address, e.g. "192.168.0.1"
     * @return true if in range, false otherwise
     */
    @Override
    public boolean isInRange(String address) {
        return isInRange(toInteger(address));
    }

    /**
     * Returns true if the parameter <code>address</code> is in the
     * range of usable endpoint addresses for this subnet. This excludes the
     * network and broadcast addresses.
     * @param address an IPv4 address in binary
     * @return true if in range, false otherwise
     */
    @Override
    public boolean isInRange(int address) {
        long addLong = address & UNSIGNED_INT_MASK;
        long netLong = networkLong();
        long broadLong = broadcastLong();
        return (addLong > netLong) && (addLong < broadLong);
    }

    @Override
    public String getAddresss() {
        return format(address);
    }

    @Override
    public int getCIDR() {
        return cidr;
    }

    @Override
    public String getNetmask() {
        return format(netmask);
    }

    @Override
    public String getNetworkAddress() {
        return format(network);
    }

    @Override
    public String getBroadcastAddress() {
        return format(broadcast);
    }

    @Override
    public String getCIDRNotation() {
        return format(address) + "/" + cidr;
    }

    public String getCidrSignature() {
        return getCIDRNotation();
    }

    /**
     * Returns a CIDR notation, in which the address is followed by a slash character (/) and
     * the count of counting the 1-bit population in the subnet mask.
     * @return the CIDR notation of the address, e.g. "192.168.0.1/24"
     */
    @Override
    public String getLowAddress() {
        return format(low());
    }

    /**
     * Return the low address as a dotted IP address.
     * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
     * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
     */
    @Override
    public String getHighAddress() {
        return format(high());
    }

    /**
     * Return the high address as a dotted IP address.
     * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
     * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
     */
    @Override
    public String getAddressCount() {
        return Long.toString(getAddressCountLong());
    }

    /**
     * Returns the count of available addresses.
     * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
     * @return the count of addresses, may be zero.
     */
    @Override
    public long getAddressCountLong() {
        long b = broadcastLong();
        long n = networkLong();
        long count = (b - n) + (inclusiveHostCount ? 1 : -1);
        return count < 0 ? 0 : count;
    }

    /**
     * Returns subnet summary information of the address,
     * which includes an IP address by CIDR-Notation with the netmask,
     * network address, broadcast address, the first and last addresses of the network,
     * and the number of available addresses in the network which includes
     * the network and broadcast addresses if the inclusive flag is true.
     */
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append("CIDR-Notation:\t[").append(getCIDRNotation()).append("]")
        .append(" Netmask: [").append(getNetmask()).append("]\n")
        .append("Network:\t[").append(getNetworkAddress()).append("]\n")
        .append("Broadcast:\t[").append(getBroadcastAddress()).append("]\n")
        .append("First Address:\t[").append(getLowAddress()).append("]\n")
        .append("Last Address:\t[").append(getHighAddress()).append("]\n")
        .append("# Addresses:\t[").append(getAddressCountLong()).append("]\n");

        return buf.toString();
    }
}
