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
import java.net.UnknownHostException;

import org.apache.commons.net.whois.WhoisClient;

/***
 * This is an example of how you would implement the Linux fwhois command
 * in Java using NetComponents.  The Java version is much shorter.
 ***/
public final class fwhois
{

    public static void main(String[] args)
    {
        int index;
        String handle, host;
        InetAddress address = null;
        WhoisClient whois;

        if (args.length != 1)
        {
            System.err.println("usage: fwhois handle[@<server>]");
            System.exit(1);
        }

        index = args[0].lastIndexOf("@");

        whois = new WhoisClient();
        // We want to timeout if a response takes longer than 60 seconds
        whois.setDefaultTimeout(60000);

        if (index == -1)
        {
            handle = args[0];
            host = WhoisClient.DEFAULT_HOST;
        }
        else
        {
            handle = args[0].substring(0, index);
            host = args[0].substring(index + 1);
        }

        try
        {
            address = InetAddress.getByName(host);
            System.out.println("[" + address.getHostName() + "]");
        }
        catch (UnknownHostException e)
        {
            System.err.println("Error unknown host: " + e.getMessage());
            System.exit(1);
        }

        try
        {
            whois.connect(address);
            System.out.print(whois.query(handle));
            whois.disconnect();
        }
        catch (IOException e)
        {
            System.err.println("Error I/O exception: " + e.getMessage());
            System.exit(1);
        }
    }

}
