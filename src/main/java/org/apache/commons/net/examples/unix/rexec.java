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

import org.apache.commons.net.bsd.RExecClient;
import org.apache.commons.net.examples.util.IOUtil;

/**
 * This is an example program demonstrating how to use the RExecClient class.
 * This program connects to an rexec server and requests that the
 * given command be executed on the server.  It then reads input from stdin
 * (this will be line buffered on most systems, so don't expect character
 * at a time interactivity), passing it to the remote process and writes
 * the process stdout and stderr to local stdout.
 * <p>
 * Example: java rexec myhost myusername mypassword "ps -aux"
 * <p>
 * Usage: rexec <hostname> <username> <password> <command>
 */

// This class requires the IOUtil support class!
public final class rexec
{

    public static void main(final String[] args)
    {
        final String server;
        final String username;
        final String password;
        final String command;
        final RExecClient client;

        if (args.length != 4)
        {
            System.err.println(
                "Usage: rexec <hostname> <username> <password> <command>");
            System.exit(1);
            return ; // so compiler can do proper flow control analysis
        }

        client = new RExecClient();

        server = args[0];
        username = args[1];
        password = args[2];
        command = args[3];

        try
        {
            client.connect(server);
        }
        catch (final IOException e)
        {
            System.err.println("Could not connect to server.");
            e.printStackTrace();
            System.exit(1);
        }

        try
        {
            client.rexec(username, password, command);
        }
        catch (final IOException e)
        {
            try
            {
                client.disconnect();
            }
            catch (final IOException f)
            {/* ignored */}
            e.printStackTrace();
            System.err.println("Could not execute command.");
            System.exit(1);
        }


        IOUtil.readWrite(client.getInputStream(), client.getOutputStream(),
                         System.in, System.out);

        try
        {
            client.disconnect();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }

}

