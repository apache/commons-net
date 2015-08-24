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
 * JUnit test class for InvalidTelnetOptionException
 ***/
public class InvalidTelnetOptionExceptionTest extends TestCase
{
    private InvalidTelnetOptionException exc1;
    private String msg1;
    private int code1;

    /***
     * setUp for the test.
     ***/
    @Override
    protected void setUp()
    {
        msg1 = "MSG";
        code1 = 13;
        exc1 = new InvalidTelnetOptionException(msg1, code1);
    }

    /***
     * test of the constructors.
     ***/
    public void testConstructors()
    {
        assertTrue(exc1.getMessage().indexOf(msg1) >= 0);
        assertTrue(exc1.getMessage().indexOf("" +code1) >= 0);
    }
}