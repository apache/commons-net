package org.apache.commons.net.telnet;

/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import junit.framework.TestCase;

/***
 * The TelnetOptionHandlerTest is the abstract class for
 * testing TelnetOptionHandler. It can be used to derive
 * the actual test classes for TelnetOptionHadler derived
 * classes, by adding creation of three new option handlers
 * and testing of the specific subnegotiation behaviour.
 * <p>
 * @author Bruno D'Avanzo
 ***/
public abstract class TelnetOptionHandlerTestAbstract extends TestCase
{
    TelnetOptionHandler opthand1;
    TelnetOptionHandler opthand2;
    TelnetOptionHandler opthand3;

    /***
     * setUp for the test. The derived test class must implement
     * this method by creating opthand1, opthand2, opthand3
     * like in the following:
     *     opthand1 = new EchoOptionHandler();
     *     opthand2 = new EchoOptionHandler(true, true, true, true);
     *     opthand3 = new EchoOptionHandler(false, false, false, false);
     ***/
    protected abstract void setUp();

    /***
     * test of the constructors. The derived class may add
     * test of the option code.
     ***/
    public void testConstructors()
    {
        // add test of the option code
        assertTrue(!opthand1.getInitLocal());
        assertTrue(!opthand1.getInitRemote());
        assertTrue(!opthand1.getAcceptLocal());
        assertTrue(!opthand1.getAcceptRemote());

        assertTrue(opthand2.getInitLocal());
        assertTrue(opthand2.getInitRemote());
        assertTrue(opthand2.getAcceptLocal());
        assertTrue(opthand2.getAcceptRemote());

        assertTrue(!opthand3.getInitLocal());
        assertTrue(!opthand3.getInitRemote());
        assertTrue(!opthand3.getAcceptLocal());
        assertTrue(!opthand3.getAcceptRemote());
    }

    /***
     * test of setWill/getWill
     ***/
    public void testWill()
    {
        opthand2.setWill(true);
        opthand3.setWill(false);

        assertTrue(!opthand1.getWill());
        assertTrue(opthand2.getWill());
        assertTrue(!opthand3.getWill());
    }

    /***
     * test of setDo/getDo
     ***/
    public void testDo()
    {
        opthand2.setDo(true);
        opthand3.setDo(false);

        assertTrue(!opthand1.getDo());
        assertTrue(opthand2.getDo());
        assertTrue(!opthand3.getDo());
    }

    /***
     * test of client-driven subnegotiation. Abstract test:
     * the derived class should implement it.
     ***/
    public abstract void testStartSubnegotiation();

    /***
     * test of server-driven subnegotiation. Abstract test:
     * the derived class should implement it.
     ***/
    public abstract void testAnswerSubnegotiation();
        // test subnegotiation
}