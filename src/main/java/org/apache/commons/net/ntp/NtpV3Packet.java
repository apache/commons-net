package org.apache.commons.net.ntp;
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


import java.net.DatagramPacket;

/**
 * Interface for a NtpV3Packet with get/set methods corresponding to the fields
 * in the NTP Data Message Header described in RFC 1305.
 *
 * @version $Revision$
 */
public interface NtpV3Packet
{

    /**
     * Standard NTP UDP port
     */
    public static final int NTP_PORT = 123;

    public static final int LI_NO_WARNING = 0;
    public static final int LI_LAST_MINUTE_HAS_61_SECONDS = 1;
    public static final int LI_LAST_MINUTE_HAS_59_SECONDS = 2;
    public static final int LI_ALARM_CONDITION = 3;

    /* mode options */
    public static final int MODE_RESERVED = 0;
    public static final int MODE_SYMMETRIC_ACTIVE = 1;
    public static final int MODE_SYMMETRIC_PASSIVE = 2;
    public static final int MODE_CLIENT = 3;
    public static final int MODE_SERVER = 4;
    public static final int MODE_BROADCAST = 5;
    public static final int MODE_CONTROL_MESSAGE = 6;
    public static final int MODE_PRIVATE = 7;

    public static final int NTP_MINPOLL = 4;  // 16 seconds
    public static final int NTP_MAXPOLL = 14; // 16284 seconds

    public static final int NTP_MINCLOCK = 1;
    public static final int NTP_MAXCLOCK = 10;

    public static final int VERSION_3 = 3;
    public static final int VERSION_4 = 4;

    /* possible getType values such that other time-related protocols can
     * have its information represented as NTP packets
     */
    public static final String TYPE_NTP = "NTP";         // RFC-1305/2030
    public static final String TYPE_ICMP = "ICMP";       // RFC-792
    public static final String TYPE_TIME = "TIME";       // RFC-868
    public static final String TYPE_DAYTIME = "DAYTIME"; // RFC-867

    /**
     * @return a datagram packet with the NTP parts already filled in
     */
    public DatagramPacket getDatagramPacket();

    /**
     * Set the contents of this object from the datagram packet
     * @param dp the packet
     */
    public void setDatagramPacket(DatagramPacket dp);

    /**
     * @return leap indicator as defined in RFC-1305
     */
    public int getLeapIndicator();

    /**
     * Set leap indicator.
     * @param li - leap indicator code
     */
    public void setLeapIndicator(int li);

    /**
     * @return mode as defined in RFC-1305
     */
    public int getMode();

    /**
     * @return mode as human readable string; e.g. 3=Client
     */
    public String getModeName();

    /**
     * Set mode as defined in RFC-1305
     * @param mode the mode to set
     */
    public void setMode(int mode);

    /**
     * @return poll interval as defined in RFC-1305.
     * Field range between NTP_MINPOLL and NTP_MAXPOLL.
     */
    public int getPoll();

    /**
     * Set poll interval as defined in RFC-1305.
     * Field range between NTP_MINPOLL and NTP_MAXPOLL.
     * @param poll the interval to set
     */
    public void setPoll(int poll);

    /**
     * @return precision as defined in RFC-1305
     */
    public int getPrecision();

    /**
     * Set precision as defined in RFC-1305
     * @param precision Precision
     * @since 3.4
     */
    void setPrecision(int precision);

    /**
     * @return root delay as defined in RFC-1305
     */
    public int getRootDelay();

    /**
     * Set root delay as defined in RFC-1305
     * @param delay the delay to set
     * @since 3.4
    */
    void setRootDelay(int delay);

    /**
     * @return root delay in milliseconds
     */
    public double getRootDelayInMillisDouble();

    /**
     * @return root dispersion as defined in RFC-1305
     */
    public int getRootDispersion();

    /**
     *
     * @param dispersion the value to set
     * @since 3.4
     */
    void setRootDispersion(int dispersion);

    /**
     * @return root dispersion in milliseconds
     */
    public long getRootDispersionInMillis();

    /**
     * @return root dispersion in milliseconds
     */
    public double getRootDispersionInMillisDouble();

    /**
     * @return version as defined in RFC-1305
     */
    public int getVersion();

    /**
     * Set version as defined in RFC-1305
     * @param version the version to set
     */
    public void setVersion(int version);

    /**
     * @return stratum as defined in RFC-1305
     */
    public int getStratum();

    /**
     * Set stratum as defined in RFC-1305
     * @param stratum the stratum to set
     */
    public void setStratum(int stratum);

    /**
     * @return the reference id string
     */
    public String getReferenceIdString();

    /**
     * @return the reference id (32-bit code) as defined in RFC-1305
     */
    public int getReferenceId();

    /**
     * Set reference clock identifier field.
     * @param refId the clock id field to set
     */
    public void setReferenceId(int refId);

    /**
     * @return the transmit timestamp as defined in RFC-1305
     */
    public TimeStamp getTransmitTimeStamp();

    /**
     * @return the reference time as defined in RFC-1305
     */
    public TimeStamp getReferenceTimeStamp();

    /**
     * @return the originate time as defined in RFC-1305
     */
    public TimeStamp getOriginateTimeStamp();

    /**
     * @return the receive time as defined in RFC-1305
     */
    public TimeStamp getReceiveTimeStamp();

    /**
     * Set the transmit timestamp given NTP TimeStamp object.
     * @param ts - timestamp
     */
    public void setTransmitTime(TimeStamp ts);

    /**
     * Set the reference timestamp given NTP TimeStamp object.
     * @param ts - timestamp
     */
    public void setReferenceTime(TimeStamp ts);

    /**
     * Set originate timestamp given NTP TimeStamp object.
     * @param ts - timestamp
     */
    public void setOriginateTimeStamp(TimeStamp ts);

    /**
     * Set receive timestamp given NTP TimeStamp object.
     * @param ts - timestamp
     */
    public void setReceiveTimeStamp(TimeStamp ts);

    /**
     * Return type of time packet. The values (e.g. NTP, TIME, ICMP, ...)
     * correspond to the protocol used to obtain the timing information.
     *
     * @return packet type string identifier
     */
    public String getType();

}
