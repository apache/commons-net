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

package org.apache.commons.net.examples.telnet;

import java.io.IOException;

import org.apache.commons.net.examples.util.IOUtil;
import org.apache.commons.net.telnet.TelnetClient;

/**
 * This is an example of a trivial use of the TelnetClient class.
 * It connects to the weather server at the University of Michigan,
 * um-weather.sprl.umich.edu port 3000, and allows the user to interact
 * with the server via standard input.  You could use this example to
 * connect to any telnet server, but it is obviously not general purpose
 * because it reads from standard input a line at a time, making it
 * inconvenient for use with a remote interactive shell.  The TelnetClient
 * class used by itself is mostly intended for automating access to telnet
 * resources rather than interactive use.
 */

// This class requires the IOUtil support class!
public final class WeatherTelnet
{

    public static void main(final String[] args)
    {
        final TelnetClient telnet;

        telnet = new TelnetClient();

        try
        {
            telnet.connect("rainmaker.wunderground.com", 3000);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        IOUtil.readWrite(telnet.getInputStream(), telnet.getOutputStream(),
                         System.in, System.out);

        try
        {
            telnet.disconnect();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }

}


