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
import java.net.UnknownHostException;

import org.apache.commons.net.finger.FingerClient;

/**
 * This is an example of how you would implement the finger command
 * in Java using NetComponents.  The Java version is much shorter.
 * But keep in mind that the Unix finger command reads all sorts of
 * local files to output local finger information.  This program only
 * queries the finger daemon.
 * <p>
 * The -l flag is used to request long output from the server.
 */
public final class finger
{

    public static void main(final String[] args)
    {
        boolean longOutput = false;
        int arg = 0, index;
        String handle, host;
        final FingerClient finger;
        InetAddress address = null;

        // Get flags.  If an invalid flag is present, exit with usage message.
        while (arg < args.length && args[arg].startsWith("-"))
        {
            if (args[arg].equals("-l")) {
                longOutput = true;
            } else {
                System.err.println("usage: finger [-l] [[[handle][@<server>]] ...]");
                System.exit(1);
            }
            ++arg;
        }


        finger = new FingerClient();
        // We want to timeout if a response takes longer than 60 seconds
        finger.setDefaultTimeout(60000);

        if (arg >= args.length)
        {
            // Finger local host

            try
            {
                address = InetAddress.getLocalHost();
            }
            catch (final UnknownHostException e)
            {
                System.err.println("Error unknown host: " + e.getMessage());
                System.exit(1);
            }

            try
            {
                finger.connect(address);
                System.out.print(finger.query(longOutput));
                finger.disconnect();
            }
            catch (final IOException e)
            {
                System.err.println("Error I/O exception: " + e.getMessage());
                System.exit(1);
            }

            return ;
        }

        // Finger each argument
        while (arg < args.length)
        {

            index = args[arg].lastIndexOf('@');

            if (index == -1)
            {
                handle = args[arg];
                try
                {
                    address = InetAddress.getLocalHost();
                }
                catch (final UnknownHostException e)
                {
                    System.err.println("Error unknown host: " + e.getMessage());
                    System.exit(1);
                }
            }
            else
            {
                handle = args[arg].substring(0, index);
                host = args[arg].substring(index + 1);

                try
                {
                    address = InetAddress.getByName(host);
                    System.out.println("[" + address.getHostName() + "]");
                }
                catch (final UnknownHostException e)
                {
                    System.err.println("Error unknown host: " + e.getMessage());
                    System.exit(1);
                }
            }

            try
            {
                finger.connect(address);
                System.out.print(finger.query(longOutput, handle));
                finger.disconnect();
            }
            catch (final IOException e)
            {
                System.err.println("Error I/O exception: " + e.getMessage());
                System.exit(1);
            }

            ++arg;
            if (arg != args.length) {
                System.out.print("\n");
            }
        }
    }
}

