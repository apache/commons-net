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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests https://issues.apache.org/jira/browse/NET-728
 */
public class SubnetUtilsNet728Test {

    private static final String CIDR_SUFFIX_30 = "30";
    private static final String CIDR_SUFFIX_32 = "32";
    private static final String cidr1 = "192.168.0.151";
    private static final String cidr2 = "192.168.0.50";

    private static final SubnetUtils snu1s30;
    private static final SubnetUtils snu1s32;
    private static final SubnetUtils snu2s32;

    static {
        snu1s32 = new SubnetUtils(StringUtils.joinWith("/", cidr1, CIDR_SUFFIX_32));
        snu1s32.setInclusiveHostCount(true);
        snu1s30 = new SubnetUtils(StringUtils.joinWith("/", cidr1, CIDR_SUFFIX_30));
        snu1s30.setInclusiveHostCount(true);
        snu2s32 = new SubnetUtils(StringUtils.joinWith("/", cidr2, CIDR_SUFFIX_32));
        snu2s32.setInclusiveHostCount(true);
    }

    @Test
    void test() {
        final SubnetUtils s = new SubnetUtils("192.168.1.1/32");
        s.setInclusiveHostCount(true);
        final SubnetUtils ss = new SubnetUtils("10.65.0.151/32");
        ss.setInclusiveHostCount(true);
        assertTrue(ss.getInfo().isInRange("10.65.0.151"));
        assertTrue(s.getInfo().isInRange("192.168.1.1"));
    }

    @Test
    void testCidr1InRange2() {
        assertTrue(snu1s30.getInfo().isInRange(cidr1), snu1s30::toString);
    }

    @Test
    void testCidr1NotInRange1() {
        assertTrue(snu1s32.getInfo().isInRange(cidr1), snu1s32::toString);
    }

    @Test
    void testCidr2InRange3() {
        assertTrue(snu2s32.getInfo().isInRange(cidr2), snu2s32::toString);
    }

    @Test
    void testCidr2NotInRange3() {
        assertTrue(snu2s32.getInfo().isInRange(cidr2), snu2s32::toString);
    }

}
