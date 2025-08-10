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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * JUnit test class for SuppressGAOptionHandler
 */
public class SuppressGAOptionHandlerTest extends AbstractTelnetOptionHandlerTest {

    /**
     * setUp for the test.
     */
    @BeforeEach
    protected void setUp() {
        opthand1 = new SuppressGAOptionHandler();
        opthand2 = new SuppressGAOptionHandler(true, true, true, true);
        opthand3 = new SuppressGAOptionHandler(false, false, false, false);
    }

    /**
     * test of server-driven subnegotiation. Checks that no subnegotiation is made.
     */
    @Test
    public void testAnswerSubnegotiation() {
        final int[] subn = { TelnetCommand.IAC, TelnetCommand.SB, TelnetOption.SUPPRESS_GO_AHEAD, 1, TelnetCommand.IAC, TelnetCommand.SE, };

        final int[] resp1 = opthand1.answerSubnegotiation(subn, subn.length);

        assertNull(resp1);
    }

    /**
     * test of the constructors.
     */
    @Override
    @Test
    public void testConstructors() {
        assertEquals(TelnetOption.SUPPRESS_GO_AHEAD, opthand1.getOptionCode());
        super.testConstructors();
    }

    /**
     * test of client-driven subnegotiation. Checks that no subnegotiation is made.
     */
    @Test
    public void testStartSubnegotiation() {

        final int[] resp1 = opthand1.startSubnegotiationLocal();
        final int[] resp2 = opthand1.startSubnegotiationRemote();

        assertNull(resp1);
        assertNull(resp2);
    }
}
