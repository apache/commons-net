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
package examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import org.apache.commons.net.pop3.POP3Client;
import org.apache.commons.net.pop3.POP3MessageInfo;

/***
 * This is an example program demonstrating how to use the POP3Client class.
 * This program connects to a POP3 server and retrieves the message
 * headers of all the messages, printing the From: and Subject: header
 * entries for each message.
 * <p>
 * Usage: messages <pop3 server hostname> <username> <password>
 * <p>
 ***/
public final class messages
{

    public static final void printMessageInfo(BufferedReader reader, int id)
    throws IOException
    {
        String line, lower, from, subject;

        from = "";
        subject = "";

        while ((line = reader.readLine()) != null)
        {
            lower = line.toLowerCase();
            if (lower.startsWith("from: "))
                from = line.substring(6).trim();
            else if (lower.startsWith("subject: "))
                subject = line.substring(9).trim();
        }

        System.out.println(Integer.toString(id) + " From: " + from +
                           "  Subject: " + subject);
    }

    public static final void main(String[] args)
    {
        int message;
        String server, username, password;
        POP3Client pop3;
        Reader reader;
        POP3MessageInfo[] messages;

        if (args.length < 3)
        {
            System.err.println(
                "Usage: messages <pop3 server hostname> <username> <password>");
            System.exit(1);
        }

        server = args[0];
        username = args[1];
        password = args[2];

        pop3 = new POP3Client();
        // We want to timeout if a response takes longer than 60 seconds
        pop3.setDefaultTimeout(60000);

        try
        {
            pop3.connect(server);
        }
        catch (IOException e)
        {
            System.err.println("Could not connect to server.");
            e.printStackTrace();
            System.exit(1);
        }

        try
        {
            if (!pop3.login(username, password))
            {
                System.err.println("Could not login to server.  Check password.");
                pop3.disconnect();
                System.exit(1);
            }

            messages = pop3.listMessages();

            if (messages == null)
            {
                System.err.println("Could not retrieve message list.");
                pop3.disconnect();
                System.exit(1);
            }
            else if (messages.length == 0)
            {
                System.out.println("No messages");
                pop3.logout();
                pop3.disconnect();
                System.exit(1);
            }

            for (message = 0; message < messages.length; message++)
            {
                reader = pop3.retrieveMessageTop(messages[message].number, 0);

                if (reader == null)
                {
                    System.err.println("Could not retrieve message header.");
                    pop3.disconnect();
                    System.exit(1);
                }

                printMessageInfo(new BufferedReader(reader), messages[message].number);
            }

            pop3.logout();
            pop3.disconnect();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

