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

package org.apache.commons.net.examples.unix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.commons.net.echo.EchoTCPClient;
import org.apache.commons.net.echo.EchoUDPClient;

/**
 * This is an example program demonstrating how to use the EchoTCPClient
 * and EchoUDPClient classes.  This program connects to the default echo
 * service port of a specified server, then reads lines from standard
 * input, writing them to the echo server, and then printing the echo.
 * The default is to use the TCP port.  Use the -udp flag to use the UDP
 * port.
 * <p>
 * Usage: echo [-udp] <hostname>
 */
public final class echo
{

    public static void echoTCP(final String host) throws IOException
    {
        final EchoTCPClient client = new EchoTCPClient();
        final BufferedReader input;
        final BufferedReader echoInput;
        final PrintWriter echoOutput;
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
        echoOutput.close();
        echoInput.close();
        echoInput.close();
        client.disconnect();
    }

    public static void echoUDP(final String host) throws IOException
    {
        int length, count;
        byte[] data;
        String line;
        final BufferedReader input;
        final InetAddress address;
        final EchoUDPClient client;

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
                catch (final SocketException e)
                {
                    // We timed out and assume the packet is lost.
                    System.err.println(
                        "SocketException: Timed out and dropped packet");
                    break;
                }
                catch (final InterruptedIOException e)
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


    public static void main(final String[] args)
    {

        if (args.length == 1)
        {
            try
            {
                echoTCP(args[0]);
            }
            catch (final IOException e)
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
            catch (final IOException e)
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

