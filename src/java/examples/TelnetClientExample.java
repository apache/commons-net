package examples;

/* ====================================================================
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetOptionHandler;
import org.apache.commons.net.telnet.SimpleOptionHandler;
import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import java.util.StringTokenizer;


/***
 * This is a simple example of use of TelnetClient.
 * An external option handler (SimpleTelnetOptionHandler) is used.
 * Initial configuration requested by TelnetClient will be:
 * WILL ECHO, WILL SUPPRESS-GA, DO SUPPRESS-GA.
 * VT100 terminal type will be subnegotiated.
 * <p>
 * Also, use of the sendAYT(), getLocalOptionState(), getRemoteOptionState()
 * is demonstrated.
 * When connected, type AYT to send an AYT command to the server and see
 * the result.
 * Type OPT to see a report of the state of the first 25 options.
 * <p>
 * @author Bruno D'Avanzo
 ***/
public class TelnetClientExample implements Runnable
{
    static TelnetClient tc = null;

    /***
     * Main for the TelnetClientExample.
     ***/
    public static void main(String[] args) throws IOException
    {
        FileOutputStream fout = null;

        if(args.length < 1)
        {
            System.err.println("Usage: TelnetClientExample1 <remote-ip> [<remote-port>]");
            System.exit(1);
        }

        String remoteip = args[0];

        int remoteport;

        if (args.length > 1)
        {
            remoteport = (new Integer(args[1])).intValue();
        }
        else
        {
            remoteport = 23;
        }

        try
        {
            fout = new FileOutputStream ("spy.log", true);
        }
        catch (Exception e)
        {
            System.err.println(
                "Exception while opening the spy file: "
                + e.getMessage());
        }

        tc = new TelnetClient();

        TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
        EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
        SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

        try
        {
            tc.addOptionHandler(ttopt);
            tc.addOptionHandler(echoopt);
            tc.addOptionHandler(gaopt);
        }
        catch (InvalidTelnetOptionException e)
        {
            System.err.println("Error registering option handlers: " + e.getMessage());
        }

        while (true)
        {
            boolean end_loop = false;
            try
            {
                tc.connect(remoteip, remoteport);


                Thread reader = new Thread (new TelnetClientExample());
                System.out.println("TelnetClientExample");
                System.out.println("Type AYT to send an AYT telnet command");
                System.out.println("Type OPT to print a report of status of options (0-24)");
                System.out.println("Type REGISTER to register a new SimpleOptionHandler");
                System.out.println("Type UNREGISTER to unregister an OptionHandler");
                System.out.println("Type SPY to register the spy (connect to port 3333 to spy)");
                System.out.println("Type UNSPY to stop spying the connection");

                reader.start();
                OutputStream outstr = tc.getOutputStream();

                byte[] buff = new byte[1024];
                int ret_read = 0;

                do
                {
                    try
                    {
                        ret_read = System.in.read(buff);
                        if(ret_read > 0)
                        {
                            if((new String(buff, 0, ret_read)).startsWith("AYT"))
                            {
                                try
                                {
                                    System.out.println("Sending AYT");

                                    System.out.println("AYT response:" + tc.sendAYT(5000));
                                }
                                catch (Exception e)
                                {
                                    System.err.println("Exception waiting AYT response: " + e.getMessage());
                                }
                            }
                            else if((new String(buff, 0, ret_read)).startsWith("OPT"))
                            {
                                 System.out.println("Status of options:");
                                 for(int ii=0; ii<25; ii++)
                                    System.out.println("Local Option " + ii + ":" + tc.getLocalOptionState(ii) + " Remote Option " + ii + ":" + tc.getRemoteOptionState(ii));
                            }
                            else if((new String(buff, 0, ret_read)).startsWith("REGISTER"))
                            {
                                StringTokenizer st = new StringTokenizer(new String(buff));
                                try
                                {
                                    st.nextToken();
                                    int opcode = (new Integer(st.nextToken())).intValue();
                                    boolean initlocal = (new Boolean(st.nextToken())).booleanValue();
                                    boolean initremote = (new Boolean(st.nextToken())).booleanValue();
                                    boolean acceptlocal = (new Boolean(st.nextToken())).booleanValue();
                                    boolean acceptremote = (new Boolean(st.nextToken())).booleanValue();
                                    SimpleOptionHandler opthand = new SimpleOptionHandler(opcode, initlocal, initremote,
                                                                    acceptlocal, acceptremote);
                                    tc.addOptionHandler(opthand);
                                }
                                catch (Exception e)
                                {
                                    if(e instanceof InvalidTelnetOptionException)
                                    {
                                        System.err.println("Error registering option: " + e.getMessage());
                                    }
                                    else
                                    {
                                        System.err.println("Invalid REGISTER command.");
                                        System.err.println("Use REGISTER optcode initlocal initremote acceptlocal acceptremote");
                                        System.err.println("(optcode is an integer.)");
                                        System.err.println("(initlocal, initremote, acceptlocal, acceptremote are boolean)");
                                    }
                                }
                            }
                            else if((new String(buff, 0, ret_read)).startsWith("UNREGISTER"))
                            {
                                StringTokenizer st = new StringTokenizer(new String(buff));
                                try
                                {
                                    st.nextToken();
                                    int opcode = (new Integer(st.nextToken())).intValue();
                                    tc.deleteOptionHandler(opcode);
                                }
                                catch (Exception e)
                                {
                                    if(e instanceof InvalidTelnetOptionException)
                                    {
                                        System.err.println("Error unregistering option: " + e.getMessage());
                                    }
                                    else
                                    {
                                        System.err.println("Invalid UNREGISTER command.");
                                        System.err.println("Use UNREGISTER optcode");
                                        System.err.println("(optcode is an integer)");
                                    }
                                }
                            }
                            else if((new String(buff, 0, ret_read)).startsWith("SPY"))
                            {
                                try
                                {
                                    tc.registerSpyStream(fout);
                                }
                                catch (Exception e)
                                {
                                    System.err.println("Error registering the spy");
                                }
                            }
                            else if((new String(buff, 0, ret_read)).startsWith("UNSPY"))
                            {
                                tc.stopSpyStream();
                            }
                            else
                            {
                                try
                                {
                                        outstr.write(buff, 0 , ret_read);
                                        outstr.flush();
                                }
                                catch (Exception e)
                                {
                                        end_loop = true;
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        System.err.println("Exception while reading keyboard:" + e.getMessage());
                        end_loop = true;
                    }
                }
                while((ret_read > 0) && (end_loop == false));

                try
                {
                    tc.disconnect();
                }
                catch (Exception e)
                {
                          System.err.println("Exception while connecting:" + e.getMessage());
                }
            }
            catch (Exception e)
            {
                    System.err.println("Exception while connecting:" + e.getMessage());
                    System.exit(1);
            }
        }
    }


    /***
     * Reader thread.
     * Reads lines from the TelnetClient and echoes them
     * on the screen.
     ***/
    public void run()
    {
        InputStream instr = tc.getInputStream();

        try
        {
            byte[] buff = new byte[1024];
            int ret_read = 0;

            do
            {
                ret_read = instr.read(buff);
                if(ret_read > 0)
                {
                    System.out.print(new String(buff, 0, ret_read));
                }
            }
            while (ret_read >= 0);
        }
        catch (Exception e)
        {
            System.err.println("Exception while reading socket:" + e.getMessage());
        }

        try
        {
            tc.disconnect();
        }
        catch (Exception e)
        {
            System.err.println("Exception while closing telnet:" + e.getMessage());
        }
    }
}

