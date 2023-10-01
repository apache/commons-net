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

package org.apache.commons.net.ntp;

import java.net.DatagramPacket;

/**
 * Implementation of NtpV3Packet with methods converting Java objects to/from the Network Time Protocol (NTP) data message header format described in RFC-1305.
 */
public class NtpV3Impl implements NtpV3Packet {

    private static final int MODE_INDEX = 0;
    private static final int MODE_SHIFT = 0;

    private static final int VERSION_INDEX = 0;
    private static final int VERSION_SHIFT = 3;

    private static final int LI_INDEX = 0;
    private static final int LI_SHIFT = 6;

    private static final int STRATUM_INDEX = 1;
    private static final int POLL_INDEX = 2;
    private static final int PRECISION_INDEX = 3;

    private static final int ROOT_DELAY_INDEX = 4;
    private static final int ROOT_DISPERSION_INDEX = 8;
    private static final int REFERENCE_ID_INDEX = 12;

    private static final int REFERENCE_TIMESTAMP_INDEX = 16;
    private static final int ORIGINATE_TIMESTAMP_INDEX = 24;
    private static final int RECEIVE_TIMESTAMP_INDEX = 32;
    private static final int TRANSMIT_TIMESTAMP_INDEX = 40;

//    private static final int KEY_IDENTIFIER_INDEX = 48;
//    private static final int MESSAGE_DIGEST = 54; /* len 16 bytes */

    /**
     * Convert byte to unsigned integer. Java only has signed types, so we have to do more work to get unsigned ops.
     *
     * @param b input byte
     * @return unsigned int value of byte
     */
    protected static final int ui(final byte b) {
        final int i = b & 0xFF;
        return i;
    }

    /**
     * Convert byte to unsigned long. Java only has signed types, so we have to do more work to get unsigned ops
     *
     * @param b input byte
     * @return unsigned long value of byte
     */
    protected static final long ul(final byte b) {
        final long i = b & 0xFF;
        return i;
    }

    private final byte[] buf = new byte[48];

    private volatile DatagramPacket dp;

    /** Creates a new instance of NtpV3Impl */
    public NtpV3Impl() {
    }

    /**
     * Compares this object against the specified object. The result is {@code true} if and only if the argument is not <code>null</code> and is a
     * <code>NtpV3Impl</code> object that contains the same values as this object.
     *
     * @param obj the object to compare with.
     * @return {@code true} if the objects are the same; {@code false} otherwise.
     * @since 3.4
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final NtpV3Impl other = (NtpV3Impl) obj;
        return java.util.Arrays.equals(buf, other.buf);
    }

    /**
     * Returns the datagram packet with the NTP details already filled in.
     *
     * @return a datagram packet.
     */
    @Override
    public synchronized DatagramPacket getDatagramPacket() {
        if (dp == null) {
            dp = new DatagramPacket(buf, buf.length);
            dp.setPort(NTP_PORT);
        }
        return dp;
    }

    /**
     * @return 4 bytes as 32-bit int
     */
    private int getInt(final int index) {
        final int i = ui(buf[index]) << 24 | ui(buf[index + 1]) << 16 | ui(buf[index + 2]) << 8 | ui(buf[index + 3]);

        return i;
    }

    /**
     * Returns leap indicator as defined in RFC-1305 which is a two-bit code: 0=no warning 1=last minute has 61 seconds 2=last minute has 59 seconds 3=alarm
     * condition (clock not synchronized)
     *
     * @return leap indicator as defined in RFC-1305.
     */
    @Override
    public int getLeapIndicator() {
        return ui(buf[LI_INDEX]) >> LI_SHIFT & 0x3;
    }

    /**
     * Get Long value represented by bits starting at specified index.
     *
     * @return 8 bytes as 64-bit long
     */
    private long getLong(final int index) {
        final long i = ul(buf[index]) << 56 | ul(buf[index + 1]) << 48 | ul(buf[index + 2]) << 40 | ul(buf[index + 3]) << 32 | ul(buf[index + 4]) << 24
                | ul(buf[index + 5]) << 16 | ul(buf[index + 6]) << 8 | ul(buf[index + 7]);
        return i;
    }

    /**
     * Returns mode as defined in RFC-1305 which is a 3-bit integer whose value is indicated by the MODE_xxx parameters.
     *
     * @return mode as defined in RFC-1305.
     */
    @Override
    public int getMode() {
        return ui(buf[MODE_INDEX]) >> MODE_SHIFT & 0x7;
    }

    /**
     * Return human-readable name of message mode type as described in RFC 1305.
     *
     * @return mode name as string.
     */
    @Override
    public String getModeName() {
        return NtpUtils.getModeName(getMode());
    }

    /**
     * Returns the {@code originate} time as defined in RFC-1305.
     *
     * @return the {@code originate} time. Never returns null.
     */
    @Override
    public TimeStamp getOriginateTimeStamp() {
        return getTimestamp(ORIGINATE_TIMESTAMP_INDEX);
    }

    /**
     * Returns poll interval as defined in RFC-1305, which is an eight-bit signed integer indicating the maximum interval between successive messages, in
     * seconds to the nearest power of two (e.g. value of six indicates an interval of 64 seconds). The values that can appear in this field range from
     * NTP_MINPOLL to NTP_MAXPOLL inclusive.
     *
     * @return poll interval as defined in RFC-1305.
     */
    @Override
    public int getPoll() {
        return buf[POLL_INDEX];
    }

    /**
     * Returns precision as defined in RFC-1305 encoded as an 8-bit signed integer (seconds to the nearest power of two). Values normally range from -6 to -20.
     *
     * @return precision as defined in RFC-1305.
     */
    @Override
    public int getPrecision() {
        return buf[PRECISION_INDEX];
    }

    /**
     * Returns {@code receive} timestamp as defined in RFC-1305.
     *
     * @return the {@code receive} time. Never returns null.
     */
    @Override
    public TimeStamp getReceiveTimeStamp() {
        return getTimestamp(RECEIVE_TIMESTAMP_INDEX);
    }

    /**
     * Returns the reference id as defined in RFC-1305, which is a 32-bit integer whose value is dependent on several criteria.
     *
     * @return the reference id as defined in RFC-1305.
     */
    @Override
    public int getReferenceId() {
        return getInt(REFERENCE_ID_INDEX);
    }

    /**
     * Returns the reference id string. String cannot be null but value is dependent on the version of the NTP spec supported and stratum level. Value can be an
     * empty string, clock type string, IP address, or a hex string.
     *
     * @return the reference id string.
     */
    @Override
    public String getReferenceIdString() {
        final int version = getVersion();
        final int stratum = getStratum();
        if (version == VERSION_3 || version == VERSION_4) {
            if (stratum == 0 || stratum == 1) {
                return idAsString(); // 4-character ASCII string (e.g. GPS, USNO)
            }
            // in NTPv4 servers this is latest transmit timestamp of ref source
            if (version == VERSION_4) {
                return idAsHex();
            }
        }

        // Stratum 2 and higher this is a four-octet IPv4 address
        // of the primary reference host.
        if (stratum >= 2) {
            return idAsIPAddress();
        }
        return idAsHex();
    }

    /**
     * Returns the reference time as defined in RFC-1305.
     *
     * @return the reference time as <code>TimeStamp</code> object. Never returns null.
     */
    @Override
    public TimeStamp getReferenceTimeStamp() {
        return getTimestamp(REFERENCE_TIMESTAMP_INDEX);
    }

    /**
     * Return root delay as defined in RFC-1305, which is the total roundtrip delay to the primary reference source, in seconds. Values can take positive and
     * negative values, depending on clock precision and skew.
     *
     * @return root delay as defined in RFC-1305.
     */
    @Override
    public int getRootDelay() {
        return getInt(ROOT_DELAY_INDEX);
    }

    /**
     * Return root delay as defined in RFC-1305 in milliseconds, which is the total roundtrip delay to the primary reference source, in seconds. Values can take
     * positive and negative values, depending on clock precision and skew.
     *
     * @return root delay in milliseconds
     */
    @Override
    public double getRootDelayInMillisDouble() {
        final double l = getRootDelay();
        return l / 65.536;
    }

    /**
     * Returns root dispersion as defined in RFC-1305.
     *
     * @return root dispersion.
     */
    @Override
    public int getRootDispersion() {
        return getInt(ROOT_DISPERSION_INDEX);
    }

    /**
     * Returns root dispersion (as defined in RFC-1305) in milliseconds.
     *
     * @return root dispersion in milliseconds
     */
    @Override
    public long getRootDispersionInMillis() {
        final long l = getRootDispersion();
        return l * 1000 / 65536L;
    }

    /**
     * Returns root dispersion (as defined in RFC-1305) in milliseconds as double precision value.
     *
     * @return root dispersion in milliseconds
     */
    @Override
    public double getRootDispersionInMillisDouble() {
        final double l = getRootDispersion();
        return l / 65.536;
    }

    /**
     * Returns Stratum as defined in RFC-1305, which indicates the stratum level of the local clock, with values defined as follows: 0=unspecified, 1=primary
     * ref clock, and all others a secondary reference (via NTP).
     *
     * @return Stratum level as defined in RFC-1305.
     */
    @Override
    public int getStratum() {
        return ui(buf[STRATUM_INDEX]);
    }

    /**
     * Get NTP Timestamp at specified starting index.
     *
     * @param index index into data array
     * @return TimeStamp object for 64 bits starting at index
     */
    private TimeStamp getTimestamp(final int index) {
        return new TimeStamp(getLong(index));
    }

    /**
     * Returns the {@code transmit} timestamp as defined in RFC-1305.
     *
     * @return the {@code transmit} timestamp as defined in RFC-1305. Never returns a null object.
     */
    @Override
    public TimeStamp getTransmitTimeStamp() {
        return getTimestamp(TRANSMIT_TIMESTAMP_INDEX);
    }

    /**
     * Return type of time packet. The values (e.g. NTP, TIME, ICMP, ...) correspond to the protocol used to obtain the timing information.
     *
     * @return packet type string identifier which in this case is "NTP".
     */
    @Override
    public String getType() {
        return "NTP";
    }

    /**
     * Returns NTP version number as defined in RFC-1305.
     *
     * @return NTP version number.
     */
    @Override
    public int getVersion() {
        return ui(buf[VERSION_INDEX]) >> VERSION_SHIFT & 0x7;
    }

    /**
     * Computes a hash code for this object. The result is the exclusive OR of the values of this object stored as a byte array.
     *
     * @return a hash code value for this object.
     * @since 3.4
     */
    @Override
    public int hashCode() {
        return java.util.Arrays.hashCode(buf);
    }

    private String idAsHex() {
        return Integer.toHexString(getReferenceId());
    }

    /**
     * Returns Reference id as dotted IP address.
     *
     * @return refId as IP address string.
     */
    private String idAsIPAddress() {
        return ui(buf[REFERENCE_ID_INDEX]) + "." + ui(buf[REFERENCE_ID_INDEX + 1]) + "." + ui(buf[REFERENCE_ID_INDEX + 2]) + "."
                + ui(buf[REFERENCE_ID_INDEX + 3]);
    }

    private String idAsString() {
        final StringBuilder id = new StringBuilder();
        for (int i = 0; i <= 3; i++) {
            final char c = (char) buf[REFERENCE_ID_INDEX + i];
            if (c == 0) { // 0-terminated string
                break;
            }
            id.append(c);
        }
        return id.toString();
    }

    /**
     * Set the contents of this object from source datagram packet.
     *
     * @param srcDp source DatagramPacket to copy contents from, never null.
     * @throws IllegalArgumentException if srcDp is null or byte length is less than minimum length of 48 bytes
     */
    @Override
    public void setDatagramPacket(final DatagramPacket srcDp) {
        if (srcDp == null || srcDp.getLength() < buf.length) {
            throw new IllegalArgumentException();
        }
        final byte[] incomingBuf = srcDp.getData();
        int len = srcDp.getLength();
        if (len > buf.length) {
            len = buf.length;
        }
        System.arraycopy(incomingBuf, 0, buf, 0, len);
        final DatagramPacket dp = getDatagramPacket();
        dp.setAddress(srcDp.getAddress());
        final int port = srcDp.getPort();
        dp.setPort(port > 0 ? port : NTP_PORT);
        dp.setData(buf);
    }

    /**
     * Set integer value at index position.
     *
     * @param idx   index position
     * @param value 32-bit int value
     */
    private void setInt(final int idx, int value) {
        for (int i = 3; i >= 0; i--) {
            buf[idx + i] = (byte) (value & 0xff);
            value >>>= 8; // shift right one-byte
        }
    }

    /**
     * Set leap indicator as defined in RFC-1305.
     *
     * @param li leap indicator.
     */
    @Override
    public void setLeapIndicator(final int li) {
        buf[LI_INDEX] = (byte) (buf[LI_INDEX] & 0x3F | (li & 0x3) << LI_SHIFT);
    }

    /**
     * Set mode as defined in RFC-1305.
     *
     * @param mode the mode to set
     */
    @Override
    public void setMode(final int mode) {
        buf[MODE_INDEX] = (byte) (buf[MODE_INDEX] & 0xF8 | mode & 0x7);
    }

    /**
     * Set originate timestamp given NTP TimeStamp object. If <code>ts</code> is null then zero time is used.
     *
     * @param ts NTP timestamp
     */
    @Override
    public void setOriginateTimeStamp(final TimeStamp ts) {
        setTimestamp(ORIGINATE_TIMESTAMP_INDEX, ts);
    }

    /**
     * Set poll interval as defined in RFC-1305.
     *
     * @param poll poll interval.
     */
    @Override
    public void setPoll(final int poll) {
        buf[POLL_INDEX] = (byte) (poll & 0xFF);
    }

    /**
     * Set precision as defined in RFC-1305.
     *
     * @param precision the precision to set
     * @since 3.4
     */
    @Override
    public void setPrecision(final int precision) {
        buf[PRECISION_INDEX] = (byte) (precision & 0xFF);
    }

    /**
     * Set receive timestamp given NTP TimeStamp object. If <code>ts</code> is null then zero time is used.
     *
     * @param ts timestamp
     */
    @Override
    public void setReceiveTimeStamp(final TimeStamp ts) {
        setTimestamp(RECEIVE_TIMESTAMP_INDEX, ts);
    }

    /**
     * Set reference clock identifier field with 32-bit unsigned integer value. See RFC-1305 for description.
     *
     * @param refId reference clock identifier.
     */
    @Override
    public void setReferenceId(final int refId) {
        setInt(REFERENCE_ID_INDEX, refId);
    }

    /**
     * Set Reference time with NTP timestamp. If <code>ts</code> is null then zero time is used.
     *
     * @param ts NTP timestamp
     */
    @Override
    public void setReferenceTime(final TimeStamp ts) {
        setTimestamp(REFERENCE_TIMESTAMP_INDEX, ts);
    }

    /**
     * Set root delay as defined in RFC-1305.
     *
     * @param delay root delay
     * @since 3.4
     */
    @Override
    public void setRootDelay(final int delay) {
        setInt(ROOT_DELAY_INDEX, delay);
    }

    /**
     * Set root dispersion as defined in RFC-1305.
     *
     * @param dispersion root dispersion
     * @since 3.4
     */
    @Override
    public void setRootDispersion(final int dispersion) {
        setInt(ROOT_DISPERSION_INDEX, dispersion);
    }

    /**
     * Set stratum level as defined in RFC-1305.
     *
     * @param stratum stratum level.
     */
    @Override
    public void setStratum(final int stratum) {
        buf[STRATUM_INDEX] = (byte) (stratum & 0xFF);
    }

    /**
     * Sets the NTP timestamp at the given array index.
     *
     * @param index index into the byte array.
     * @param t     TimeStamp.
     */
    private void setTimestamp(final int index, final TimeStamp t) {
        long ntpTime = t == null ? 0 : t.ntpValue();
        // copy 64-bits from Long value into 8 x 8-bit bytes of array
        // one byte at a time shifting 8-bits for each position.
        for (int i = 7; i >= 0; i--) {
            buf[index + i] = (byte) (ntpTime & 0xFF);
            ntpTime >>>= 8; // shift to next byte
        }
        // buf[index] |= 0x80; // only set if 1900 baseline....
    }

    /**
     * Set transmit time with NTP timestamp. If <code>ts</code> is null then zero time is used.
     *
     * @param ts NTP timestamp
     */
    @Override
    public void setTransmitTime(final TimeStamp ts) {
        setTimestamp(TRANSMIT_TIMESTAMP_INDEX, ts);
    }

    /**
     * Set NTP version as defined in RFC-1305.
     *
     * @param version NTP version.
     */
    @Override
    public void setVersion(final int version) {
        buf[VERSION_INDEX] = (byte) (buf[VERSION_INDEX] & 0xC7 | (version & 0x7) << VERSION_SHIFT);
    }

    /**
     * Returns details of NTP packet as a string.
     *
     * @return details of NTP packet as a string.
     */
    @Override
    public String toString() {
        return "[" + "version:" + getVersion() + ", mode:" + getMode() + ", poll:" + getPoll() + ", precision:" + getPrecision() + ", delay:" + getRootDelay()
                + ", dispersion(ms):" + getRootDispersionInMillisDouble() + ", id:" + getReferenceIdString() + ", xmitTime:"
                + getTransmitTimeStamp().toDateString() + " ]";
    }

}
