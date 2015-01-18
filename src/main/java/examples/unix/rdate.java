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

package examples.unix;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.net.time.TimeTCPClient;
import org.apache.commons.net.time.TimeUDPClient;

/***
 * This is an example program demonstrating how to use the TimeTCPClient
 * and TimeUDPClient classes.  It's very similar to the simple Unix rdate
 * command.  This program connects to the default time service port of a
 * specified server, retrieves the time, and prints it to standard output.
 * The default is to use the TCP port.  Use the -udp flag to use the UDP
 * port.  You can test this program by using the NIST time server at
 * 132.163.135.130 (warning: the IP address may change).
 * <p>
 * Usage: rdate [-udp] <hostname>
 ***/
public final class rdate
{

    public static final void timeTCP(String host) throws IOException
    {
        TimeTCPClient client = new TimeTCPClient();

        // We want to timeout if a response takes longer than 60 seconds
        client.setDefaultTimeout(60000);
        client.connect(host);
        System.out.println(client.getDate().toString());
        client.disconnect();
    }

    public static final void timeUDP(String host) throws IOException
    {
        TimeUDPClient client = new TimeUDPClient();

        // We want to timeout if a response takes longer than 60 seconds
        client.setDefaultTimeout(60000);
        client.open();
        System.out.println(client.getDate(InetAddress.getByName(host)).toString());
        client.close();
    }


    public static void main(String[] args)
    {

        if (args.length == 1)
        {
            try
            {
                timeTCP(args[0]);
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
                timeUDP(args[1]);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }
        }
        else
        {
            System.err.println("Usage: rdate [-udp] <hostname>");
            System.exit(1);
        }

    }

}

