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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/***
 * JUnit test class for TelnetClient.s
 * Implements protocol compliance tests
 * <p>
 * @author Bruno D'Avanzo
 ***/
public class TelnetClientTest extends TestCase implements TelnetNotificationHandler
{
    protected TelnetTestSimpleServer server1;
    protected TelnetTestSimpleServer server2;
    protected TelnetTestSimpleServer server3;
    protected TelnetClient tc1;
    protected TelnetClient tc2;
    protected TelnetClient tc3;
    protected int numdo = 0;
    protected int numdont = 0;
    protected int numwill = 0;
    protected int numwont = 0;

    /***
     * main for running the test.
     ***/
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(TelnetClientTest.class);
    }

    /***
     * open connections needed for the tests for the test.
     ***/
    protected void openConnections() throws Exception
    {
        server1 = new TelnetTestSimpleServer(3333);
        server2 = new TelnetTestSimpleServer(3334);
        server3 = new TelnetTestSimpleServer(3335);

        tc1 = new TelnetClient();
        tc2 = new TelnetClient();
        tc3 = new TelnetClient("ANSI");

        TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
        SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

        tc2.addOptionHandler(ttopt);
        tc2.addOptionHandler(echoopt);
        tc2.addOptionHandler(gaopt);

        tc1.connect("127.0.0.1", 3333);
        tc2.connect("127.0.0.1", 3334);
        tc3.connect("127.0.0.1", 3335);
        Thread.sleep(1000);
    }

    /***
     * tests the initial condition of the sessions
     ***/
    public void testInitial() throws Exception
    {
        boolean connect1_ok = false;
        boolean connect2_ok = false;
        boolean connect3_ok = false;
        boolean init2_ok = false;
        boolean add_invalid_ok1 = false;
        boolean add_invalid_ok2 = false;
        byte buffread2[] = new byte[9];
        byte expected2[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WILL, (byte)TelnetOption.ECHO,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WILL, (byte)TelnetOption.SUPPRESS_GO_AHEAD,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DO, (byte)TelnetOption.SUPPRESS_GO_AHEAD,
        };

        openConnections();

        SimpleOptionHandler hand = new SimpleOptionHandler(550);
        try
        {
            tc1.addOptionHandler(hand);
        }
        catch (Exception e)
        {
            add_invalid_ok1 = true;
        }

        try
        {
            tc2.addOptionHandler(hand);
        }
        catch (Exception e)
        {
            add_invalid_ok2 = true;
        }

        InputStream is1 = server1.getInputStream();
        Thread.sleep(1000);
        if(is1.available() == 0)
        {
            connect1_ok = true;
        }

        Thread.sleep(1000);
        InputStream is2 = server2.getInputStream();
        if(is2.available() == 9)
        {
            is2.read(buffread2);
            connect2_ok = true;

            if(equalBytes(buffread2, expected2))
                 init2_ok = true;
        }

        InputStream is3 = server3.getInputStream();
        Thread.sleep(1000);
        if(is3.available() == 0)
        {
            connect3_ok = true;
        }

        closeConnections();

        assertTrue(connect1_ok);
        assertTrue(connect2_ok);
        assertTrue(connect3_ok);
        assertTrue(!tc1.getLocalOptionState(TelnetOption.ECHO));
        assertTrue(!tc1.getRemoteOptionState(TelnetOption.ECHO));
        assertTrue(!tc2.getLocalOptionState(TelnetOption.ECHO));
        assertTrue(!tc2.getRemoteOptionState(TelnetOption.ECHO));
        assertTrue(!tc3.getLocalOptionState(TelnetOption.TERMINAL_TYPE));
        assertTrue(!tc3.getRemoteOptionState(TelnetOption.TERMINAL_TYPE));
        assertTrue(init2_ok);
        assertTrue(add_invalid_ok1);
        assertTrue(add_invalid_ok2);
    }

    /***
     * protocol compliance test for option negotiation
     ***/
    public void testOptionNegotiation() throws Exception
    {
        boolean negotiation1_ok = false;
        byte buffread1[] = new byte[6];
        byte send1[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DO, (byte)15,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WILL, (byte)15,
        };
        byte expected1[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WONT, (byte)15,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DONT, (byte)15,
        };

        boolean negotiation2_ok = false;
        byte buffread2[] = new byte[9];
        byte send2[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DO, (byte)TelnetOption.TERMINAL_TYPE,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DONT, (byte)TelnetOption.ECHO,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DO, (byte)TelnetOption.SUPPRESS_GO_AHEAD,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WONT, (byte)TelnetOption.SUPPRESS_GO_AHEAD
        };
        byte expected2[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WILL, (byte)TelnetOption.TERMINAL_TYPE,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WONT, (byte)TelnetOption.ECHO,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DONT, (byte)TelnetOption.SUPPRESS_GO_AHEAD
        };

        byte buffread2b[] = new byte[11];
        byte send2b[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.SB, (byte)TelnetOption.TERMINAL_TYPE,
            (byte)1, (byte)TelnetCommand.IAC, (byte)TelnetCommand.SE,
        };
        byte expected2b[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.SB, (byte)TelnetOption.TERMINAL_TYPE,
            (byte)0, 'V', 'T', '1', '0', '0', (byte)TelnetCommand.IAC, (byte)TelnetCommand.SE,
        };

        boolean negotiation3_ok = false;
        byte buffread3[] = new byte[6];
        byte send3[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DO, (byte)TelnetOption.TERMINAL_TYPE,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DO, (byte)TelnetOption.SUPPRESS_GO_AHEAD
        };
        byte expected3[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WILL, (byte)TelnetOption.TERMINAL_TYPE,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WONT, (byte)TelnetOption.SUPPRESS_GO_AHEAD
        };
        byte buffread3b[] = new byte[10];
        byte send3b[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.SB, (byte)TelnetOption.TERMINAL_TYPE,
            (byte)1, (byte)TelnetCommand.IAC, (byte)TelnetCommand.SE,
        };
        byte expected3b[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.SB, (byte)TelnetOption.TERMINAL_TYPE,
            (byte)0, 'A', 'N', 'S', 'I', (byte)TelnetCommand.IAC, (byte)TelnetCommand.SE,
        };

        openConnections();

        InputStream is1 = server1.getInputStream();
        OutputStream os1 = server1.getOutputStream();
        is1.skip(is1.available());
        os1.write(send1);
        os1.flush();
        Thread.sleep(1000);
        if(is1.available() == 6)
        {
            is1.read(buffread1);

            if(equalBytes(buffread1, expected1))
                negotiation1_ok = true;
        }

        InputStream is2 = server2.getInputStream();
        OutputStream os2 = server2.getOutputStream();
        Thread.sleep(1000);
        is2.skip(is2.available());
        os2.write(send2);
        os2.flush();
        Thread.sleep(1000);
        if(is2.available() == 9)
        {
            is2.read(buffread2);

            if(equalBytes(buffread2, expected2))
                negotiation2_ok = true;

            if(negotiation2_ok)
            {
                negotiation2_ok = false;
                os2.write(send2b);
                os2.flush();
                Thread.sleep(1000);
                if(is2.available() == 11)
                {
                    is2.read(buffread2b);

                    if(equalBytes(buffread2b, expected2b))
                        negotiation2_ok = true;
                }
            }
        }

        InputStream is3 = server3.getInputStream();
        OutputStream os3 = server3.getOutputStream();
        Thread.sleep(1000);
        is3.skip(is3.available());
        os3.write(send3);
        os3.flush();
        Thread.sleep(1000);
        if(is3.available() == 6)
        {
            is3.read(buffread3);

            if(equalBytes(buffread3, expected3))
                negotiation3_ok = true;

            if(negotiation3_ok)
            {
                negotiation3_ok = false;
                os3.write(send3b);
                os3.flush();
                Thread.sleep(1000);
                if(is3.available() == 10)
                {
                    is3.read(buffread3b);
                    if(equalBytes(buffread3b, expected3b))
                            negotiation3_ok = true;
                }
            }
        }

        closeConnections();

        assertTrue(negotiation1_ok);
        assertTrue(negotiation2_ok);
        assertTrue(negotiation3_ok);
        assertTrue(!tc1.getLocalOptionState(15));
        assertTrue(!tc1.getRemoteOptionState(15));
        assertTrue(!tc1.getLocalOptionState(TelnetOption.TERMINAL_TYPE));
        assertTrue(!tc2.getLocalOptionState(TelnetOption.ECHO));
        assertTrue(!tc2.getRemoteOptionState(TelnetOption.ECHO));
        assertTrue(tc2.getLocalOptionState(TelnetOption.SUPPRESS_GO_AHEAD));
        assertTrue(!tc2.getRemoteOptionState(TelnetOption.SUPPRESS_GO_AHEAD));
        assertTrue(tc2.getLocalOptionState(TelnetOption.TERMINAL_TYPE));
        assertTrue(tc3.getLocalOptionState(TelnetOption.TERMINAL_TYPE));
        assertTrue(!tc2.getLocalOptionState(TelnetOption.ECHO));
    }


    /***
     * protocol compliance test for option renegotiation
     ***/
    public void testOptionRenegotiation() throws Exception
    {
        boolean negotiation1_ok = false;

        byte buffread[] = new byte[6];
        byte send[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DO, (byte)TelnetOption.ECHO,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DONT, (byte)TelnetOption.SUPPRESS_GO_AHEAD,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WONT, (byte)TelnetOption.SUPPRESS_GO_AHEAD
        };
        byte expected[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WONT, (byte)TelnetOption.SUPPRESS_GO_AHEAD,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DONT, (byte)TelnetOption.SUPPRESS_GO_AHEAD
        };

        byte buffread2[] = new byte[3];
        byte send2[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DONT, (byte)TelnetOption.ECHO,
        };
        byte expected2[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WONT, (byte)TelnetOption.ECHO,
        };

        openConnections();

        InputStream is = server2.getInputStream();
        OutputStream os = server2.getOutputStream();
        Thread.sleep(1000);
        is.skip(is.available());
        os.write(send);
        os.flush();
        Thread.sleep(1000);
        if(is.available() == 6)
        {
            is.read(buffread);

            if(equalBytes(buffread, expected))
                negotiation1_ok = true;

            if(negotiation1_ok)
            {
                negotiation1_ok = false;
                os.write(send2);
                os.flush();
                Thread.sleep(1000);
                if(is.available() == 3)
                {
                    is.read(buffread2);
                    if(equalBytes(buffread2, expected2))
                            negotiation1_ok = true;
                }
            }
        }

        closeConnections();

        assertTrue(negotiation1_ok);
        assertTrue(!tc2.getLocalOptionState(TelnetOption.ECHO));
    }

    /***
     * test of option negotiation notification
     ***/
    public void testNotification() throws Exception
    {
        byte buffread1[] = new byte[6];
        byte send1[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DO, (byte)15,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WILL, (byte)15,
        };

        byte buffread2[] = new byte[9];
        byte send2[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DO, (byte)TelnetOption.TERMINAL_TYPE,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DONT, (byte)TelnetOption.ECHO,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DO, (byte)TelnetOption.SUPPRESS_GO_AHEAD,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WONT, (byte)TelnetOption.SUPPRESS_GO_AHEAD
        };

        byte buffread2b[] = new byte[11];
    
        openConnections();

        numdo = 0;
        numdont = 0;
        numwill = 0;
        numwont = 0;
        tc2.registerNotifHandler(this);

        InputStream is1 = server1.getInputStream();
        OutputStream os1 = server1.getOutputStream();
        is1.skip(is1.available());
        os1.write(send1);
        os1.flush();
        Thread.sleep(500);
        if(is1.available() > 0)
        {
            is1.read(buffread1);
        }

        InputStream is2 = server2.getInputStream();
        OutputStream os2 = server2.getOutputStream();
        Thread.sleep(500);
        is2.skip(is2.available());
        os2.write(send2);
        os2.flush();
        Thread.sleep(500);
        if(is2.available() > 0)
        {
            is2.read(buffread2);
                Thread.sleep(1000);
                if(is2.available() > 0)
                {
                    is2.read(buffread2b);
                }
        }


        closeConnections();

        assertTrue(numdo == 2);
        assertTrue(numdont == 1);
        assertTrue(numwont == 1);
        assertTrue(numwill == 0);
    }


    /***
     * protocol compliance test in case of option handler removal
     ***/
    public void testDeleteOptionHandler() throws Exception
    {
        boolean remove_ok = false;
        boolean remove_invalid_ok1 = false;
        boolean remove_invalid_ok2 = false;

        byte buffread[] = new byte[6];
        byte send[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DO, (byte)TelnetOption.ECHO,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DO, (byte)TelnetOption.SUPPRESS_GO_AHEAD,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WILL, (byte)TelnetOption.SUPPRESS_GO_AHEAD
        };

        byte expected[] =
        {
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WONT, (byte)TelnetOption.SUPPRESS_GO_AHEAD,
            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DONT, (byte)TelnetOption.SUPPRESS_GO_AHEAD
        };

        openConnections();

        InputStream is = server2.getInputStream();
        OutputStream os = server2.getOutputStream();
        Thread.sleep(1000);
        is.skip(is.available());
        os.write(send);
        os.flush();
        Thread.sleep(1000);
        if(is.available() == 0)
        {
            tc2.deleteOptionHandler(TelnetOption.SUPPRESS_GO_AHEAD);
            Thread.sleep(1000);
            if(is.available() == 6)
            {
                is.read(buffread);
                if(equalBytes(buffread, expected))
                    remove_ok = true;
            }
        }

        try
        {
            tc2.deleteOptionHandler(TelnetOption.SUPPRESS_GO_AHEAD);
        }
        catch (Exception e)
        {
            remove_invalid_ok1 = true;
        }

        try
        {
            tc2.deleteOptionHandler(550);
        }
        catch (Exception e)
        {
            remove_invalid_ok2 = true;
        }

        closeConnections();

        assertTrue(remove_ok);
        assertTrue(remove_invalid_ok1);
        assertTrue(remove_invalid_ok2);
        assertTrue(tc2.getLocalOptionState(TelnetOption.ECHO));
        assertTrue(!tc2.getLocalOptionState(TelnetOption.SUPPRESS_GO_AHEAD));
        assertTrue(!tc2.getLocalOptionState(TelnetOption.SUPPRESS_GO_AHEAD));
    }


    /***
     * test of AYT functionality
     ***/
    public void testAYT() throws Exception
    {
        boolean ayt_true_ok = false;
        boolean ayt_false_ok = false;


        byte AYT[] = { (byte)TelnetCommand.IAC, (byte)TelnetCommand.AYT };
        byte response[] = { '[', 'Y', 'e', 's', ']' };
        String inputs[] = new String[1];
        String outputs[] = new String[1];
        inputs[0] = new String (AYT);
        outputs[0] = new String (response);

        openConnections();

        OutputStream os = server3.getOutputStream();
        InputStream is = server3.getInputStream();
        TelnetTestResponder tr = new TelnetTestResponder(is, os, inputs, outputs, 30000);
        assertNotNull(tr);
        boolean res1 = tc3.sendAYT(2000);

        if(res1 == true)
            ayt_true_ok=true;

        Thread.sleep(1000);
        is.skip(is.available());

        boolean res2 = tc3.sendAYT(2000);

        if(res2 == false)
            ayt_false_ok=true;

        closeConnections();

        assertTrue(ayt_true_ok);
        assertTrue(ayt_false_ok);
    }

    /***
     * test of Spy functionality
     ***/
    public void testSpy() throws Exception
    {
        boolean test1spy_ok = false;
        boolean test2spy_ok = false;
        boolean stopspy_ok = false;
        byte expected1[] = { 't', 'e', 's', 't', '1' };
        byte expected2[] = { 't', 'e', 's', 't', '2' };

        openConnections();

        PipedOutputStream po = new PipedOutputStream();
        PipedInputStream pi = new PipedInputStream(po);

        OutputStream os = server1.getOutputStream();
        OutputStream ostc = tc1.getOutputStream();

        tc1.registerSpyStream(po);

        os.write("test1".getBytes());
        os.flush();

        Thread.sleep(1000);
        byte buffer[] = new byte[5];

        if(pi.available() == 5)
        {
            pi.read(buffer);
            if(equalBytes(buffer, expected1))
                test1spy_ok = true;
        }

        ostc.write("test2".getBytes());
        ostc.flush();

        Thread.sleep(1000);

        if(pi.available() == 5)
        {
            pi.read(buffer);
            if(equalBytes(buffer, expected2))
                test2spy_ok = true;
        }

        tc1.stopSpyStream();
        os.write("test1".getBytes());
        os.flush();
        ostc.write("test2".getBytes());
        ostc.flush();
        Thread.sleep(1000);
        if(pi.available() == 0)
        {
            stopspy_ok = true;
        }

        closeConnections();

        assertTrue(test1spy_ok);
        assertTrue(test2spy_ok);
        assertTrue(stopspy_ok);
    }

    /***
     * closes all the connections
     ***/
    protected void closeConnections()
    {
        try
        {
            server1.disconnect();
            server1.stop();
            tc1.disconnect();
            server2.disconnect();
            server2.stop();
            tc2.disconnect();
            server3.disconnect();
            server3.stop();
            tc3.disconnect();
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
        }
    }

    /***
     * Helper method. compares two arrays of int
     ***/
    protected boolean equalBytes(byte a1[], byte a2[])
    {
        if(a1.length != a2.length)
        {
            return(false);
        }
        else
        {
            boolean result = true;
            for(int ii=0; ii<a1.length; ii++)
            {
                if(a1[ii]!= a2[ii])
                    result = false;
            }
            return(result);
        }
    }

    /***
     * Callback method called when TelnetClient receives an option
     * negotiation command.
     * <p>
     * @param negotiation_code - type of negotiation command received
     * (RECEIVED_DO, RECEIVED_DONT, RECEIVED_WILL, RECEIVED_WONT)
     * <p>
     * @param option_code - code of the option negotiated
     * <p>
     ***/
    public void receivedNegotiation(int negotiation_code, int option_code)
    {
        if(negotiation_code == TelnetNotificationHandler.RECEIVED_DO)
        {
            numdo++;
        }
        else if(negotiation_code == TelnetNotificationHandler.RECEIVED_DONT)
        {
            numdont++;
        }
        else if(negotiation_code == TelnetNotificationHandler.RECEIVED_WILL)
        {
            numwill++;
        }
        else if(negotiation_code == TelnetNotificationHandler.RECEIVED_WONT)
        {
            numwont++;
        }
    }

}