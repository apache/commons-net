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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.commons.net.io.Util;
import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.apache.commons.net.smtp.SimpleSMTPHeader;

/***
 * This is an example program using the SMTP package to send a message
 * to the specified recipients.  It prompts you for header information and
 * a filename containing the message.
 * <p>
 ***/

public final class mail
{

    public final static void main(String[] args)
    {
        String sender, recipient, subject, filename, server, cc;
        Vector ccList = new Vector();
        BufferedReader stdin;
        FileReader fileReader = null;
        Writer writer;
        SimpleSMTPHeader header;
        SMTPClient client;
        Enumeration enum;

        if (args.length < 1)
        {
            System.err.println("Usage: mail smtpserver");
            System.exit(1);
        }

        server = args[0];

        stdin = new BufferedReader(new InputStreamReader(System.in));

        try
        {
            System.out.print("From: ");
            System.out.flush();

            sender = stdin.readLine();

            System.out.print("To: ");
            System.out.flush();

            recipient = stdin.readLine();

            System.out.print("Subject: ");
            System.out.flush();

            subject = stdin.readLine();

            header = new SimpleSMTPHeader(sender, recipient, subject);


            while (true)
            {
                System.out.print("CC <enter one address per line, hit enter to end>: ");
                System.out.flush();

                // Of course you don't want to do this because readLine() may be null
                cc = stdin.readLine().trim();

                if (cc.length() == 0)
                    break;

                header.addCC(cc);
                ccList.addElement(cc);
            }

            System.out.print("Filename: ");
            System.out.flush();

            filename = stdin.readLine();

            try
            {
                fileReader = new FileReader(filename);
            }
            catch (FileNotFoundException e)
            {
                System.err.println("File not found. " + e.getMessage());
            }

            client = new SMTPClient();
            client.addProtocolCommandListener(new PrintCommandListener(
                                                  new PrintWriter(System.out)));

            client.connect(server);

            if (!SMTPReply.isPositiveCompletion(client.getReplyCode()))
            {
                client.disconnect();
                System.err.println("SMTP server refused connection.");
                System.exit(1);
            }

            client.login();

            client.setSender(sender);
            client.addRecipient(recipient);

            enum = ccList.elements();

            while (enum.hasMoreElements())
                client.addRecipient((String)enum.nextElement());

            writer = client.sendMessageData();

            if (writer != null)
            {
                writer.write(header.toString());
                Util.copyReader(fileReader, writer);
                writer.close();
                client.completePendingCommand();
            }

            fileReader.close();

            client.logout();

            client.disconnect();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }
}


