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
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.commons.net.CharGenTCPClient;
import org.apache.commons.net.CharGenUDPClient;

/***
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
 * <p>
 ***/
public final class chargen
{

    public static final void chargenTCP(String host) throws IOException
    {
        int lines = 100;
        String line;
        CharGenTCPClient client = new CharGenTCPClient();
        BufferedReader chargenInput;

        // We want to timeout if a response takes longer than 60 seconds
        client.setDefaultTimeout(60000);
        client.connect(host);
        chargenInput =
            new BufferedReader(new InputStreamReader(client.getInputStream()));

        // We assume the chargen service outputs lines, but it really doesn't
        // have to, so this code might actually not work if no newlines are
        // present.
        while (lines-- > 0)
        {
            if ((line = chargenInput.readLine()) == null)
                break;
            System.out.println(line);
        }

        client.disconnect();
    }

    public static final void chargenUDP(String host) throws IOException
    {
        int packets = 50;
        byte[] data;
        InetAddress address;
        CharGenUDPClient client;

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
            catch (SocketException e)
            {
                // We timed out and assume the packet is lost.
                System.err.println("SocketException: Timed out and dropped packet");
                continue;
            }
            catch (InterruptedIOException e)
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


    public static final void main(String[] args)
    {

        if (args.length == 1)
        {
            try
            {
                chargenTCP(args[0]);
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
                chargenUDP(args[1]);
            }
            catch (IOException e)
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

