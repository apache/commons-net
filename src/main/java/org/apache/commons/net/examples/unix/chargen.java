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
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.commons.net.chargen.CharGenTCPClient;
import org.apache.commons.net.chargen.CharGenUDPClient;

/**
 * This is an example program demonstrating how to use the CharGenTCPClient
 * and CharGenUDPClient classes.  This program connects to the default
 * chargen service port of a specified server, then reads 100 lines from
 * of generated output, writing each line to standard output, and then
 * closes the connection.  The UDP invocation of the program sends 50
 * datagrams, printing the reply to each.
 * The default is to use the TCP port.  Use the -udp flag to use the UDP
 * port.
 * <p>
 * Usage: chargen [-udp] <hostname>
 */
public final class chargen
{

    public static void chargenTCP(final String host) throws IOException
    {
        int lines = 100;
        String line;
        final CharGenTCPClient client = new CharGenTCPClient();

        // We want to timeout if a response takes longer than 60 seconds
        client.setDefaultTimeout(60000);
        client.connect(host);
        try (final BufferedReader chargenInput = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

            // We assume the chargen service outputs lines, but it really doesn't
            // have to, so this code might actually not work if no newlines are
            // present.
            while (lines-- > 0) {
                if ((line = chargenInput.readLine()) == null) {
                    break;
                }
                System.out.println(line);
            }
        }
        client.disconnect();
    }

    public static void chargenUDP(final String host) throws IOException
    {
        int packets = 50;
        byte[] data;
        final InetAddress address;
        final CharGenUDPClient client;

        address = InetAddress.getByName(host);
        client = new CharGenUDPClient();

        client.open();
        // If we don't receive a return packet within 5 seconds, assume
        // the packet is lost.
        client.setSoTimeout(5000);

        while (packets-- > 0)
        {
            client.send(address);

            try
            {
                data = client.receive();
            }
            // Here we catch both SocketException and InterruptedIOException,
            // because even though the JDK 1.1 docs claim that
            // InterruptedIOException is thrown on a timeout, it seems
            // SocketException is also thrown.
            catch (final SocketException e)
            {
                // We timed out and assume the packet is lost.
                System.err.println("SocketException: Timed out and dropped packet");
                continue;
            }
            catch (final InterruptedIOException e)
            {
                // We timed out and assume the packet is lost.
                System.err.println(
                    "InterruptedIOException: Timed out and dropped packet");
                continue;
            }
            System.out.write(data);
            System.out.flush();
        }

        client.close();
    }


    public static void main(final String[] args)
    {

        if (args.length == 1)
        {
            try
            {
                chargenTCP(args[0]);
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
                chargenUDP(args[1]);
            }
            catch (final IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }
        }
        else
        {
            System.err.println("Usage: chargen [-udp] <hostname>");
            System.exit(1);
        }

    }

}

