package examples;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.commons.net.EchoTCPClient;
import org.apache.commons.net.EchoUDPClient;

/***
 * This is an example program demonstrating how to use the EchoTCPClient
 * and EchoUDPClient classes.  This program connects to the default echo
 * service port of a specified server, then reads lines from standard
 * input, writing them to the echo server, and then printing the echo.
 * The default is to use the TCP port.  Use the -udp flag to use the UDP
 * port.
 * <p>
 * Usage: echo [-udp] <hostname>
 * <p>
 ***/
public final class echo
{

    public static final void echoTCP(String host) throws IOException
    {
        EchoTCPClient client = new EchoTCPClient();
        BufferedReader input, echoInput;
        PrintWriter echoOutput;
        String line;

        // We want to timeout if a response takes longer than 60 seconds
        client.setDefaultTimeout(60000);
        client.connect(host);
        System.out.println("Connected to " + host + ".");
        input = new BufferedReader(new InputStreamReader(System.in));
        echoOutput =
            new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
        echoInput =
            new BufferedReader(new InputStreamReader(client.getInputStream()));

        while ((line = input.readLine()) != null)
        {
            echoOutput.println(line);
            System.out.println(echoInput.readLine());
        }

        client.disconnect();
    }

    public static final void echoUDP(String host) throws IOException
    {
        int length, count;
        byte[] data;
        String line;
        BufferedReader input;
        InetAddress address;
        EchoUDPClient client;

        input = new BufferedReader(new InputStreamReader(System.in));
        address = InetAddress.getByName(host);
        client = new EchoUDPClient();

        client.open();
        // If we don't receive an echo within 5 seconds, assume the packet is lost.
        client.setSoTimeout(5000);
        System.out.println("Ready to echo to " + host + ".");

        // Remember, there are no guarantees about the ordering of returned
        // UDP packets, so there is a chance the output may be jumbled.
        while ((line = input.readLine()) != null)
        {
            data = line.getBytes();
            client.send(data, address);
            count = 0;
            do
            {
                try
                {
                    length = client.receive(data);
                }
                // Here we catch both SocketException and InterruptedIOException,
                // because even though the JDK 1.1 docs claim that
                // InterruptedIOException is thrown on a timeout, it seems
                // SocketException is also thrown.
                catch (SocketException e)
                {
                    // We timed out and assume the packet is lost.
                    System.err.println(
                        "SocketException: Timed out and dropped packet");
                    break;
                }
                catch (InterruptedIOException e)
                {
                    // We timed out and assume the packet is lost.
                    System.err.println(
                        "InterruptedIOException: Timed out and dropped packet");
                    break;
                }
                System.out.print(new String(data, 0, length));
                count += length;
            }
            while (count < data.length);

            System.out.println();
        }

        client.close();
    }


    public static final void main(String[] args)
    {

        if (args.length == 1)
        {
            try
            {
                echoTCP(args[0]);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }
        }
        else if (args.length == 2 && args[0].equals("-udp"))
        {
            try
            {
                echoUDP(args[1]);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }
        }
        else
        {
            System.err.println("Usage: echo [-udp] <hostname>");
            System.exit(1);
        }

    }

}

