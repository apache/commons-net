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
 * JUnit test class for TerminalTypeOptionHandler
 ***/
public class WindowSizeOptionHandlerTest extends TelnetOptionHandlerTestAbstract
{
    /***
     * setUp for the test.
     ***/
    @Override
    protected void setUp()
    {
        opthand1 = new WindowSizeOptionHandler(80, 24);
        opthand2 = new WindowSizeOptionHandler(255, 255, true, true, true, true);
        opthand3 = new WindowSizeOptionHandler(0xFFFF, 0x00FF, false, false, false, false);
    }

    /***
     * test of the constructors.
     ***/
    @Override
    public void testConstructors()
    {
        assertEquals(TelnetOption.WINDOW_SIZE, opthand1.getOptionCode());
        super.testConstructors();
    }

    /***
     * test of client-driven subnegotiation.
     * Checks that no subnegotiation is made.
     ***/
    @Override
    public void testStartSubnegotiation()
    {
        assertNull(opthand1.startSubnegotiationRemote());
        assertNull(opthand2.startSubnegotiationRemote());
        assertNull(opthand3.startSubnegotiationRemote());
    }

    /***
     * test of client-driven subnegotiation.
     *
     ***/
    public void testStartSubnegotiationLocal()
    {
        int[] exp1 = {31, 0, 80, 0, 24};
        int[] start1 = opthand1.startSubnegotiationLocal();
        assertEquals(5, start1.length);
        equalInts(exp1, start1);

        int[] exp2 = {31, 0, 255, 255, 0, 255, 255};
        int[] start2 = opthand2.startSubnegotiationLocal();
        equalInts(exp2, start2);

        int[] exp3 = {31, 255, 255, 255, 255, 0, 255, 255};
        int[] start3 = opthand3.startSubnegotiationLocal();
        equalInts(exp3, start3);
    }



    /***
     * test of client-driven subnegotiation.
     * Checks that nothing is sent
     ***/
    @Override
    public void testAnswerSubnegotiation()
    {
        int subn[] =
        {
            TelnetOption.WINDOW_SIZE, 24, 80
        };

        int resp1[] = opthand1.answerSubnegotiation(subn, subn.length);
        int resp2[] = opthand2.answerSubnegotiation(subn, subn.length);
        int resp3[] = opthand3.answerSubnegotiation(subn, subn.length);

        assertNull(resp1);
        assertNull(resp2);
        assertNull(resp3);
    }

    /***
     * compares two arrays of int
     ***/
    private void equalInts(int a1[], int a2[])
    {
        assertEquals("Arrays should be the same length", a1.length, a2.length);
        for(int ii=0; ii<a1.length; ii++)
        {
            assertEquals("Array entry "+ii+" should match",a1[ii], a2[ii]);
        }
    }
}
