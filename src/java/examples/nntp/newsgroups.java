/*
 * Copyright 2001-2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package examples.nntp;

import java.io.IOException;
import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewsgroupInfo;

/***
 * This is a trivial example using the NNTP package to approximate the
 * Unix newsgroups command.  It merely connects to the specified news
 * server and issues fetches the list of newsgroups stored by the server.
 * On servers that store a lot of newsgroups, this command can take a very
 * long time (listing upwards of 30,000 groups).
 * <p>
 ***/

public final class newsgroups
{

    public final static void main(String[] args)
    {
        NNTPClient client;
        NewsgroupInfo[] list;

        if (args.length < 1)
        {
            System.err.println("Usage: newsgroups newsserver");
            System.exit(1);
        }

        client = new NNTPClient();

        try
        {
            client.connect(args[0]);

            list = client.listNewsgroups();

            if (list != null)
            {
                for (int i = 0; i < list.length; i++)
                    System.out.println(list[i].getNewsgroup());
            }
            else
            {
                System.err.println("LIST command failed.");
                System.err.println("Server reply: " + client.getReplyString());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (client.isConnected())
                    client.disconnect();
            }
            catch (IOException e)
            {
                System.err.println("Error disconnecting from server.");
                e.printStackTrace();
                System.exit(1);
            }
        }

    }

}


