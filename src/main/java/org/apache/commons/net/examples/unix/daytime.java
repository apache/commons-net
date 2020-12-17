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

import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.net.daytime.DaytimeTCPClient;
import org.apache.commons.net.daytime.DaytimeUDPClient;

/**
 * This is an example program demonstrating how to use the DaytimeTCP
 * and DaytimeUDP classes.
 * This program connects to the default daytime service port of a
 * specified server, retrieves the daytime, and prints it to standard output.
 * The default is to use the TCP port.  Use the -udp flag to use the UDP
 * port.
 * <p>
 * Usage: daytime [-udp] <hostname>
 */
public final class daytime
{

    public static void daytimeTCP(final String host) throws IOException
    {
        final DaytimeTCPClient client = new DaytimeTCPClient();

        // We want to timeout if a response takes longer than 60 seconds
        client.setDefaultTimeout(60000);
        client.connect(host);
        System.out.println(client.getTime().trim());
        client.disconnect();
    }

    public static void daytimeUDP(final String host) throws IOException
    {
        final DaytimeUDPClient client = new DaytimeUDPClient();

        // We want to timeout if a response takes longer than 60 seconds
        client.setDefaultTimeout(60000);
        client.open();
        System.out.println(client.getTime(
                                          InetAddress.getByName(host)).trim());
        client.close();
    }


    public static void main(final String[] args)
    {

        if (args.length == 1)
        {
            try
            {
                daytimeTCP(args[0]);
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
                daytimeUDP(args[1]);
            }
            catch (final IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }
        }
        else
        {
            System.err.println("Usage: daytime [-udp] <hostname>");
            System.exit(1);
        }

    }

}

