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

/**
 * Extended Interface for a NtpV3Packet with get/set methods corresponding to the fields
 * in the NTP Data Message Header described in RFC 1305.
 * Adds {@link #setPrecision(int)}, {@link #setRootDelay(int)} and {@link #setRootDispersion(int)}
 * @version $Revision: 1652863 $
 * @since 3.4
 */
public interface NtpV3Packetbis extends NtpV3Packet
{

    /**
     * Set precision as defined in RFC-1305
     * @param precision Precision
     * @since 3.4
     */
    void setPrecision(int precision);

    /**
     * Set root delay as defined in RFC-1305
     * @param delay the delay to set
     * @since 3.4
    */
    void setRootDelay(int delay);

    /**
     *
     * @param dispersion the value to set
     * @since 3.4
     */
    void setRootDispersion(int dispersion);

}
