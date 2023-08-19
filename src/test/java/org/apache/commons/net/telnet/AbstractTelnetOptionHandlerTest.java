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
package org.apache.commons.net.telnet;

import junit.framework.TestCase;

/**
 * The TelnetOptionHandlerTest is the abstract class for testing TelnetOptionHandler. It can be used to derive the actual test classes for TelnetOptionHadler
 * derived classes, by adding creation of three new option handlers and testing of the specific subnegotiation behavior.
 */
public abstract class AbstractTelnetOptionHandlerTest extends TestCase {
    TelnetOptionHandler opthand1;
    TelnetOptionHandler opthand2;
    TelnetOptionHandler opthand3;

    /**
     * setUp for the test. The derived test class must implement this method by creating opthand1, opthand2, opthand3 like in the following: opthand1 = new
     * EchoOptionHandler(); opthand2 = new EchoOptionHandler(true, true, true, true); opthand3 = new EchoOptionHandler(false, false, false, false);
     */
    @Override
    protected abstract void setUp();

    /**
     * test of server-driven subnegotiation. Abstract test: the derived class should implement it.
     */
    public abstract void testAnswerSubnegotiation();
    // test subnegotiation

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
    public void testDo() {
        opthand2.setDo(true);
        opthand3.setDo(false);

        assertFalse(opthand1.getDo());
        assertTrue(opthand2.getDo());
        assertFalse(opthand3.getDo());
    }

    /**
     * test of client-driven subnegotiation. Abstract test: the derived class should implement it.
     */
    public abstract void testStartSubnegotiation();

    /**
     * test of setWill/getWill
     */
    public void testWill() {
        opthand2.setWill(true);
        opthand3.setWill(false);

        assertFalse(opthand1.getWill());
        assertTrue(opthand2.getWill());
        assertFalse(opthand3.getWill());
    }
}