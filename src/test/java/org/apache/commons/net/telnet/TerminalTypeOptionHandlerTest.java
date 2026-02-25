/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.telnet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TerminalTypeOptionHandlerTest extends AbstractTelnetOptionHandlerTest {
    /*
     * compares two arrays of int
     */
    protected boolean equalInts(final int a1[], final int a2[]) {
        if (a1.length != a2.length) {
            return false;
        }
        boolean result = true;
        for (int ii = 0; ii < a1.length; ii++) {
            if (a1[ii] != a2[ii]) {
                result = false;
                break;
            }
        }
        return result;
    }

    @BeforeEach
    protected void setUp() {
        opthand1 = new TerminalTypeOptionHandler("VT100");
        opthand2 = new TerminalTypeOptionHandler("ANSI", true, true, true, true);
        opthand3 = new TerminalTypeOptionHandler("ANSI", false, false, false, false);
    }

    /*
     * test of client-driven subnegotiation. Checks that the terminal type is sent
     */
    @Test
    void testAnswerSubnegotiation() {
        final int[] subn = { TelnetOption.TERMINAL_TYPE, 1 };

        final int[] expected1 = { TelnetOption.TERMINAL_TYPE, 0, 'V', 'T', '1', '0', '0' };

        final int[] expected2 = { TelnetOption.TERMINAL_TYPE, 0, 'A', 'N', 'S', 'I' };

        final int[] resp1 = opthand1.answerSubnegotiation(subn, subn.length);
        final int[] resp2 = opthand2.answerSubnegotiation(subn, subn.length);

        assertTrue(equalInts(resp1, expected1));
        assertTrue(equalInts(resp2, expected2));
    }

    @Override
    @Test
    void testConstructors() {
        assertEquals(TelnetOption.TERMINAL_TYPE, opthand1.getOptionCode());
        super.testConstructors();
    }

    /*
     * test of client-driven subnegotiation. Checks that no subnegotiation is made.
     */
    @Test
    void testStartSubnegotiation() {

        final int[] resp1 = opthand1.startSubnegotiationLocal();
        final int[] resp2 = opthand1.startSubnegotiationRemote();

        assertNull(resp1);
        assertNull(resp2);
    }
}
