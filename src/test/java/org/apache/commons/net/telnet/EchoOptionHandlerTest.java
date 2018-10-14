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

/***
 * JUnit test class for EchoOptionHandler
 ***/
public class EchoOptionHandlerTest extends TelnetOptionHandlerTestAbstract
{

    /***
     * setUp for the test.
     ***/
    @Override
    protected void setUp()
    {
        opthand1 = new EchoOptionHandler();
        opthand2 = new EchoOptionHandler(true, true, true, true);
        opthand3 = new EchoOptionHandler(false, false, false, false);
    }

    /***
     * test of the constructors.
     ***/
    @Override
    public void testConstructors()
    {
        assertEquals(opthand1.getOptionCode(), TelnetOption.ECHO);
        super.testConstructors();
    }

    /***
     * test of client-driven subnegotiation.
     * Checks that no subnegotiation is made.
     ***/
    @Override
    public void testStartSubnegotiation()
    {
        int resp1[] = opthand1.startSubnegotiationLocal();
        int resp2[] = opthand1.startSubnegotiationRemote();

        assertNull(resp1);
        assertNull(resp2);
    }

    /***
     * test of server-driven subnegotiation.
     * Checks that no subnegotiation is made.
     ***/
    @Override
    public void testAnswerSubnegotiation()
    {
        int subn[] =
        {
            TelnetCommand.IAC, TelnetCommand.SB, TelnetOption.ECHO,
            1, TelnetCommand.IAC, TelnetCommand.SE,
        };

        int resp1[] = opthand1.answerSubnegotiation(subn, subn.length);

        assertNull(resp1);
    }
}
