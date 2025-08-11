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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The TelnetOptionHandlerTest is the abstract class for testing TelnetOptionHandler. It can be used to derive the actual test classes for TelnetOptionHadler
 * derived classes, by adding creation of three new option handlers and testing of the specific subnegotiation behavior.
 */
public abstract class AbstractTelnetOptionHandlerTest {
    TelnetOptionHandler opthand1;
    TelnetOptionHandler opthand2;
    TelnetOptionHandler opthand3;

    /**
     * test of the constructors. The derived class may add test of the option code.
     */
    public void testConstructors() {
        // add test of the option code
        assertFalse(opthand1.getInitLocal());
        assertFalse(opthand1.getInitRemote());
        assertFalse(opthand1.getAcceptLocal());
        assertFalse(opthand1.getAcceptRemote());

        assertTrue(opthand2.getInitLocal());
        assertTrue(opthand2.getInitRemote());
        assertTrue(opthand2.getAcceptLocal());
        assertTrue(opthand2.getAcceptRemote());

        assertFalse(opthand3.getInitLocal());
        assertFalse(opthand3.getInitRemote());
        assertFalse(opthand3.getAcceptLocal());
        assertFalse(opthand3.getAcceptRemote());
    }

    /**
     * test of setDo/getDo
     */
    @Test
    public void testDo() {
        opthand2.setDo(true);
        opthand3.setDo(false);

        assertFalse(opthand1.getDo());
        assertTrue(opthand2.getDo());
        assertFalse(opthand3.getDo());
    }

    /**
     * test of setWill/getWill
     */
    @Test
    public void testWill() {
        opthand2.setWill(true);
        opthand3.setWill(false);

        assertFalse(opthand1.getWill());
        assertTrue(opthand2.getWill());
        assertFalse(opthand3.getWill());
    }
}