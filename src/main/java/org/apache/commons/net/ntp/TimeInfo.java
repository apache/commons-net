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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class to network time packet messages (NTP, etc.) that computes related timing info and stats.
 */
public class TimeInfo {

    private final NtpV3Packet message;
    private List<String> comments;
    private Long delayMillis;
    private Long offsetMillis;

    /**
     * time at which time message packet was received by local machine
     */
    private final long returnTimeMillis;

    /**
     * flag indicating that the TimeInfo details was processed and delay/offset were computed
     */
    private boolean detailsComputed;

    /**
     * Create TimeInfo object with raw packet message and destination time received.
     *
     * @param message          NTP message packet
     * @param returnTimeMillis destination receive time
     * @throws IllegalArgumentException if message is null
     */
    public TimeInfo(final NtpV3Packet message, final long returnTimeMillis) {
        this(message, returnTimeMillis, null, true);
    }

    /**
     * Create TimeInfo object with raw packet message and destination time received. Auto-computes details if computeDetails flag set otherwise this is delayed
     * until computeDetails() is called. Delayed computation is for fast initialization when sub-millisecond timing is needed.
     *
     * @param msgPacket        NTP message packet
     * @param returnTimeMillis destination receive time
     * @param doComputeDetails flag to pre-compute delay/offset values
     * @throws IllegalArgumentException if message is null
     */
    public TimeInfo(final NtpV3Packet msgPacket, final long returnTimeMillis, final boolean doComputeDetails) {
        this(msgPacket, returnTimeMillis, null, doComputeDetails);
    }

    /**
     * Create TimeInfo object with raw packet message and destination time received.
     *
     * @param message          NTP message packet
     * @param returnTimeMillis destination receive time
     * @param comments         List of errors/warnings identified during processing
     * @throws IllegalArgumentException if message is null
     */
    public TimeInfo(final NtpV3Packet message, final long returnTimeMillis, final List<String> comments) {
        this(message, returnTimeMillis, comments, true);
    }

    /**
     * Create TimeInfo object with raw packet message and destination time received. Auto-computes details if computeDetails flag set otherwise this is delayed
     * until computeDetails() is called. Delayed computation is for fast initialization when sub-millisecond timing is needed.
     *
     * @param message          NTP message packet
     * @param returnTimeMillis destination receive time
     * @param comments         list of comments used to store errors/warnings with message
     * @param doComputeDetails flag to pre-compute delay/offset values
     * @throws IllegalArgumentException if message is null
     */
    public TimeInfo(final NtpV3Packet message, final long returnTimeMillis, final List<String> comments, final boolean doComputeDetails) {
        if (message == null) {
            throw new IllegalArgumentException("message cannot be null");
        }
        this.returnTimeMillis = returnTimeMillis;
        this.message = message;
        this.comments = comments;
        if (doComputeDetails) {
            computeDetails();
        }
    }

    /**
     * Add comment (error/warning) to list of comments associated with processing of NTP parameters. If comment list not create then one will be created.
     *
     * @param comment the comment
     */
    public void addComment(final String comment) {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        comments.add(comment);
    }

    /**
     * Compute and validate details of the NTP message packet. Computed fields include the offset and delay.
     */
    public void computeDetails() {
        if (detailsComputed) {
            return; // details already computed - do nothing
        }
        detailsComputed = true;
        if (comments == null) {
            comments = new ArrayList<>();
        }

        final TimeStamp origNtpTime = message.getOriginateTimeStamp();
        final long origTimeMillis = origNtpTime.getTime();

        // Receive Time is time request received by server (t2)
        final TimeStamp rcvNtpTime = message.getReceiveTimeStamp();
        final long rcvTimeMillis = rcvNtpTime.getTime();

        // Transmit time is time reply sent by server (t3)
        final TimeStamp xmitNtpTime = message.getTransmitTimeStamp();
        final long xmitTimeMillis = xmitNtpTime.getTime();

        /*
         * Round-trip network delay and local clock offset (or time drift) is calculated according to this standard NTP equation:
         *
         * LocalClockOffset = ((ReceiveTimestamp - OriginateTimestamp) + (TransmitTimestamp - DestinationTimestamp)) / 2
         *
         * equations from RFC-1305 (NTPv3) roundtrip delay = (t4 - t1) - (t3 - t2) local clock offset = ((t2 - t1) + (t3 - t4)) / 2
         *
         * It takes into account network delays and assumes that they are symmetrical.
         *
         * Note the typo in SNTP RFCs 1769/2030 which state that the delay is (T4 - T1) - (T2 - T3) with the "T2" and "T3" switched.
         */
        if (origNtpTime.ntpValue() == 0) {
            // without originate time cannot determine when packet went out
            // might be via a broadcast NTP packet...
            if (xmitNtpTime.ntpValue() != 0) {
                offsetMillis = Long.valueOf(xmitTimeMillis - returnTimeMillis);
                comments.add("Error: zero orig time -- cannot compute delay");
            } else {
                comments.add("Error: zero orig time -- cannot compute delay/offset");
            }
        } else if (rcvNtpTime.ntpValue() == 0 || xmitNtpTime.ntpValue() == 0) {
            comments.add("Warning: zero rcvNtpTime or xmitNtpTime");
            // assert destTime >= origTime since network delay cannot be negative
            if (origTimeMillis > returnTimeMillis) {
                comments.add("Error: OrigTime > DestRcvTime");
            } else {
                // without receive or xmit time cannot figure out processing time
                // so delay is simply the network travel time
                delayMillis = Long.valueOf(returnTimeMillis - origTimeMillis);
            }
            // TODO: is offset still valid if rcvNtpTime=0 || xmitNtpTime=0 ???
            // Could always hash origNtpTime (sendTime) but if host doesn't set it
            // then it's an malformed ntp host anyway and we don't care?
            // If server is in broadcast mode then we never send out a query in first place...
            if (rcvNtpTime.ntpValue() != 0) {
                // xmitTime is 0 just use rcv time
                offsetMillis = Long.valueOf(rcvTimeMillis - origTimeMillis);
            } else if (xmitNtpTime.ntpValue() != 0) {
                // rcvTime is 0 just use xmitTime time
                offsetMillis = Long.valueOf(xmitTimeMillis - returnTimeMillis);
            }
        } else {
            long delayValueMillis = returnTimeMillis - origTimeMillis;
            // assert xmitTime >= rcvTime: difference typically < 1ms
            if (xmitTimeMillis < rcvTimeMillis) {
                // server cannot send out a packet before receiving it...
                comments.add("Error: xmitTime < rcvTime"); // time-travel not allowed
            } else {
                // subtract processing time from round-trip network delay
                final long deltaMillis = xmitTimeMillis - rcvTimeMillis;
                // in normal cases the processing delta is less than
                // the total roundtrip network travel time.
                if (deltaMillis <= delayValueMillis) {
                    delayValueMillis -= deltaMillis; // delay = (t4 - t1) - (t3 - t2)
                } else // if delta - delayValue == 1 ms then it's a round-off error
                // e.g. delay=3ms, processing=4ms
                if (deltaMillis - delayValueMillis == 1) {
                    // delayValue == 0 -> local clock saw no tick change but destination clock did
                    if (delayValueMillis != 0) {
                        comments.add("Info: processing time > total network time by 1 ms -> assume zero delay");
                        delayValueMillis = 0;
                    }
                } else {
                    comments.add("Warning: processing time > total network time");
                }
            }
            delayMillis = Long.valueOf(delayValueMillis);
            if (origTimeMillis > returnTimeMillis) {
                comments.add("Error: OrigTime > DestRcvTime");
            }

            offsetMillis = Long.valueOf((rcvTimeMillis - origTimeMillis + xmitTimeMillis - returnTimeMillis) / 2);
        }
    }

    /**
     * Compares this object against the specified object. The result is {@code true} if and only if the argument is not <code>null</code> and is a
     * <code>TimeStamp</code> object that contains the same values as this object.
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
        final TimeInfo other = (TimeInfo) obj;
        return returnTimeMillis == other.returnTimeMillis && message.equals(other.message);
    }

    /**
     * Get host address from message datagram if available
     *
     * @return host address of available otherwise null
     * @since 3.4
     */
    public InetAddress getAddress() {
        final DatagramPacket pkt = message.getDatagramPacket();
        return pkt == null ? null : pkt.getAddress();
    }

    /**
     * Return list of comments (if any) during processing of NTP packet.
     *
     * @return List or null if not yet computed
     */
    public List<String> getComments() {
        return comments;
    }

    /**
     * Get round-trip network delay. If null then could not compute the delay.
     *
     * @return Long or null if delay not available.
     */
    public Long getDelay() {
        return delayMillis;
    }

    /**
     * Returns NTP message packet.
     *
     * @return NTP message packet.
     */
    public NtpV3Packet getMessage() {
        return message;
    }

    /**
     * Get clock offset needed to adjust local clock to match remote clock. If null then could not compute the offset.
     *
     * @return Long or null if offset not available.
     */
    public Long getOffset() {
        return offsetMillis;
    }

    /**
     * Returns time at which time message packet was received by local machine.
     *
     * @return packet return time.
     */
    public long getReturnTime() {
        return returnTimeMillis;
    }

    /**
     * Computes a hash code for this object. The result is the exclusive OR of the return time and the message hash code.
     *
     * @return a hash code value for this object.
     * @since 3.4
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = (int) returnTimeMillis;
        result = prime * result + message.hashCode();
        return result;
    }

}
