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

/***
 * JUnit test class for TelnetOption
 ***/
public class TelnetOptionTest extends TestCase
{
    /***
     * test of the isValidOption method.
     ***/
    public void testisValidOption()
    {
        assertTrue(TelnetOption.isValidOption(0));
        assertTrue(TelnetOption.isValidOption(91));
        assertTrue(TelnetOption.isValidOption(255));
        assertTrue(!TelnetOption.isValidOption(256));
    }

    /***
     * test of the getOption method.
     ***/
    public void testGetOption()
    {
        assertEquals(TelnetOption.getOption(0), "BINARY");
        assertEquals(TelnetOption.getOption(91), "UNASSIGNED");
        assertEquals(TelnetOption.getOption(255), "Extended-Options-List");
    }
}