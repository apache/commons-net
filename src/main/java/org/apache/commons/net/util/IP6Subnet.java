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

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convenience container for IPv6 subnet summary information.
 * @see <a href="https://tools.ietf.org/html/rfc4291#section-2.3">https://tools.ietf.org/html/rfc4291#section-2.3</a>
 * @since 3.7
 */
public final class IP6Subnet extends SubnetUtils.SubnetInfo
{

    private static final int NBITS = 128;
    private static final String ZERO_FIELDS = "(0\\:){2,}";

    private final int[] ip6Address;
    private final int cidr;

    /**
     * Constructor that takes an IPv6 address in CIDR-notation, e.g. "2001:db8:0:0:0:ff00:42:8329/48".
     *
     * @param  cidrNotation an IPv6 address in CIDR-notation
     */
    public IP6Subnet(String cidrNotation)
    {
        String[] tmp = cidrNotation.split("/");

        ip6Address = toArray(tmp[0]);
        cidr = rangeCheck(Integer.parseInt(tmp[1]), 0, NBITS);
    }

    /*
     * Creates the minimum address in the network
     * to which the address belongs, it has all-zero in the host fields.
     */
    private int[] low()
    {
        int[] addr = new int[8];

        // Copy of the network prefix in the address
        int index = cidr / 16;
        for (int i = 0; i <= index; i++)
        {
            addr[i] = ip6Address[i];
        }

        // Set the out of the network prefix bits.
        addr[index] &= 0xffff << (16 - (cidr % 16));

        return addr;
    }

    /*
     * Creates the maximum address in the network
     * to which the address belongs, it has all-ones in the host fields.
     */
    private int[] high()
    {
        int[] highAddr = new int[8];

        // Copy of the network prefix in the address
        int index = cidr / 16;
        for (int i = 0; i <= index; i++)
        {
            highAddr[i] = ip6Address[i];
        }

        // Set the out of the network prefix bits
        highAddr[index] |= 0xffff >> (cidr % 16);

        // Fill the following fields with 1-bits
        for (int i = index + 1; i < 8; i++)
        {
            highAddr[i] = 0xffff;
        }

        return highAddr;
    }

    private static int[] toArray(String address)
    {
        int[] ret = new int[8];
        String[] addrArry = address.split(":");

        for (int i = 0; i < addrArry.length; i++)
        {
            ret[i] = Integer.parseInt(addrArry[i], 16);
        }

        return ret;
    }

    /*
     * Converts a packed integer address into the colon-separated hexadecimal format.
     */
    private String format(int[] val)
    {
        String address = format(val, ":");

        /* The longest run of consecutive 0 fields MUST be shortened based on RFC 5952. */
        String regex = "";
        int maxLength = 0;

        // Find the longest zero fields
        Matcher match = Pattern.compile(ZERO_FIELDS).matcher(address);
        while (match.find())
        {
            String reg = match.group();
            int len = reg.length();

            if (maxLength < len)
            {
                regex = reg;
                maxLength = len;
            }
        }

        // Remove all leading zeroes
        return address.replace(regex, ":");
    }

    /**
     * Returns true if the parameter <code>address</code> is
     * in the range of usable endpoint addresses for this subnet.
     *
     * @param address a colon-delimited address, e.g. "2001:db8:0:0:0:ff00:42:8329"
     * @return true if in range, false otherwise
     */
    @Override
    public boolean isInRange(String address)
    {
        return isInRange(toArray(address));
    }

    /**
     * Returns true if the parameter <code>address</code> is
     * in the range of usable endpoint addresses for this subnet.
     *
     * @param address an IPv6 address in binary
     * @return true if in range, false otherwise
     */
    @Override
    public boolean isInRange(int[] address)
    {
        int prefixSize = cidr / 16;
        int[] lowAddress = low();
        int[] highAddress = high();

        // Have the same network prefix groups
        for (int i = 0; i < prefixSize; i++)
        {
            // Whether all 16 bits are the same values.
            if (address[i] != ip6Address[i])
            {
                return false;
            }
        }

        //The host identifier is in range between the lowest and the hightest addresses
        int addr = address[prefixSize];
        int lowAddr = lowAddress[prefixSize];
        int highAddr = highAddress[prefixSize];

        return (addr >= lowAddr) && (addr <= highAddr);
    }

    /**
     * Returns the <code>address</code>, that is the colon 16-bit delimited hexadecimal format
     * for IPv6 addresses, e.g. "2001:db8::ff00:42:8329".
     *
     * @return a string of the IP address
     */
    @Override
    public String getAddress()
    {
        return format(ip6Address);
    }

    /**
     * Returns the CIDR suffixes, the count of consecutive 1-bit in the subnet mask.
     * The range of IPv6 address is between 0 and 128, but it is actually less than 64.
     *
     * @return the CIDR suffixes of the address in an integer.
     */
    @Override
    public int getCIDR()
    {
        return cidr;
    }

    /**
     * Returns the IPv6-CIDR notation, in which the address is followed by a slash and
     * the count of counting the 1-bit population in the subnet mask.
     *
     * @return the CIDR notation of the address, e.g. "2001:db8::ff00:42:8329/48"
     */
    @Override
    public String getCIDRNotation()
    {
        return format(ip6Address) + "/" + cidr;
    }

    /**
     * Returns the lowest address as a colon-separated IPv6 address.
     *
     * @return the IP address in a colon 16-bit delimited hexadecimal format,
     * may be "::" if there is no valid address
     */
    @Override
    public String getLowAddress()
    {
        return format(low());
    }

    /**
     * Returns the highest address as a colon-separated IPv6 address.
     *
     * @return the IP address in a colon 16-bit delimited hexadecimal format,
     * may be "::" if there is no valid address
     */
    @Override
    public String getHighAddress()
    {
        return format(high());
    }

    /**
     * Returns the count of available addresses.
     *
     * @return the count of addresses in a string, may be zero
     */
    @Override
    public String getAddressCountString()
    {
        return new BigInteger("2").pow(128 - cidr).toString();
    }

    /**
     * Returns subnet summary information of the address,
     * which includes an IP address by CIDR-Notation, the first and
     * the last addresses of the network, and the number of available addresses
     * in the network which includes all-zero and all-ones in the host fields,
     * known as network or broadcast addresses.
     */
    @Override
    public String toString()
    {
        final StringBuilder buf = new StringBuilder();
        buf.append("CIDR-Notation:\t[").append(getCIDRNotation()).append("]")
        .append("First Address:\t[").append(getLowAddress()).append("]\n")
        .append("Last Address:\t[").append(getHighAddress()).append("]\n")
        .append("# Addresses:\t[").append(getAddressCountString()).append("]\n");

        return buf.toString();
    }

}
