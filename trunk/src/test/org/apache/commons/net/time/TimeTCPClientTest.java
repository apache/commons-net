package org.apache.commons.net.time;

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

import java.net.InetAddress;
import java.util.Calendar;
import java.io.IOException;
import java.util.TimeZone;

import junit.framework.TestCase;
import org.apache.commons.net.TimeTCPClient;

public class TimeTCPClientTest extends TestCase
{
    private TimeTestSimpleServer server1;

    private int _port = 3333; // default test port

    /***
     * main for running the test.
     ***/
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TimeTCPClientTest.class);
    }

    /***
     * open connections needed for the tests for the test.
     ***/
    protected void openConnections() throws Exception
    {
	try {
            server1 = new TimeTestSimpleServer(_port);
            server1.connect();
	} catch (IOException ioe)
	{
	    // try again on another port
	    _port = 4000;
            server1 = new TimeTestSimpleServer(_port);
            server1.connect();
	}
        server1.start();
    }

    /***
     *  tests the constant basetime used by TimeClient against tha
     *  computed from Calendar class.
     */
    public void testInitial() {
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        Calendar calendar = Calendar.getInstance(utcZone);
        calendar.set(1900, Calendar.JANUARY, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long baseTime = calendar.getTime().getTime() / 1000L;

        assertTrue(baseTime == -TimeTCPClient.SECONDS_1900_TO_1970);
    }

    /***
     * tests the times retrieved via the Time protocol implementation.
     ***/
    public void testCompareTimes() throws Exception
    {
        openConnections();

        long time, time2;
        long clientTime, clientTime2;
        try
        {
            TimeTCPClient client = new TimeTCPClient();
            try
            {
                // We want to timeout if a response takes longer than 60 seconds
                client.setDefaultTimeout(60000);
                client.connect(InetAddress.getLocalHost(), _port);
                clientTime = client.getDate().getTime();
                time = System.currentTimeMillis();
            } finally
            {
                client.disconnect();
            }

            try
            {
                // We want to timeout if a response takes longer than 60 seconds
                client.setDefaultTimeout(60000);
                client.connect(InetAddress.getLocalHost(), _port);
                clientTime2 = (client.getTime() - TimeTCPClient.SECONDS_1900_TO_1970)*1000L;
                time2 = System.currentTimeMillis();
            } finally
            {
                client.disconnect();
            }

        } finally
        {
            closeConnections();
        }

      // current time shouldn't differ from time reported via network by 5 seconds
      assertTrue(Math.abs(time - clientTime) < 5000);
      assertTrue(Math.abs(time2 - clientTime2) < 5000);
    }

    /***
     * closes all the connections
     ***/
    protected void closeConnections()
    {
        try
        {
            server1.stop();
            Thread.sleep(1000);
        } catch (Exception e)
        {
        }
    }
}

