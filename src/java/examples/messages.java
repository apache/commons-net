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

