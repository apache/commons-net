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
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
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
import java.io.InputStream;
import java.io.OutputStream;

/***
 * JUnit functional test for TelnetClient.
 * Connects to the weather forecast service
 * rainmaker.wunderground.com and asks for Los Angeles forecast.
 * <p>
 * @author Bruno D'Avanzo
 ***/
public class TelnetClientFunctionalTest extends TestCase
{
    protected TelnetClient tc1;

    /***
     * main for running the test.
     ***/
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(TelnetClientFunctionalTest.class);
    }

    /***
     * test setUp
     ***/
    protected void setUp()
    {
        tc1 = new TelnetClient();
    }

    /***
     * Do the functional test:
     * - connect to the weather service
     * - press return on the first menu
     * - send LAX on the second menu
     * - send X to exit
     ***/
    public void testFunctionalTest() throws Exception
    {
        boolean testresult = false;
        tc1.connect("rainmaker.wunderground.com", 3000);

        InputStream is = tc1.getInputStream();
        OutputStream os = tc1.getOutputStream();

        boolean cont = waitForString(is, "Return to continue:", 30000);
        if (cont)
        {
            os.write("\n".getBytes());
            os.flush();
            cont = waitForString(is, "city code--", 30000);
        }
        if (cont)
        {
            os.write("LAX\n".getBytes());
            os.flush();
            cont = waitForString(is, "Los Angeles", 30000);
        }
        if (cont)
        {
            cont = waitForString(is, "X to exit:", 30000);
        }
        if (cont)
        {
            os.write("X\n".getBytes());
            os.flush();
            tc1.disconnect();
            testresult = true;
        }

        assertTrue(testresult);
    }


    /***
     * Helper method. waits for a string with timeout
     ***/
    public boolean waitForString(InputStream is, String end, long timeout) throws Exception
    {
        byte buffer[] = new byte[32];
        long starttime = System.currentTimeMillis();

        String readbytes = new String();
        while((readbytes.indexOf(end) < 0) &&
              ((System.currentTimeMillis() - starttime) < timeout))
        {
            if(is.available() > 0)
            {
                int ret_read = is.read(buffer);
                readbytes = readbytes + new String(buffer, 0, ret_read);
            }
            else
            {
                Thread.sleep(500);
            }
        }

        if(readbytes.indexOf(end) >= 0)
        {
            return (true);
        }
        else
        {
            return (false);
        }
    }
}