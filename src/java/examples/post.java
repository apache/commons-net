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
import org.apache.commons.net.io.Util;
import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NNTPReply;
import org.apache.commons.net.nntp.SimpleNNTPHeader;

/***
 * This is an example program using the NNTP package to post an article
 * to the specified newsgroup(s).  It prompts you for header information and
 * a filename to post.
 * <p>
 ***/

public final class post
{

    public final static void main(String[] args)
    {
        String from, subject, newsgroup, filename, server, organization;
        String references;
        BufferedReader stdin;
        FileReader fileReader = null;
        SimpleNNTPHeader header;
        NNTPClient client;

        if (args.length < 1)
        {
            System.err.println("Usage: post newsserver");
            System.exit(1);
        }

        server = args[0];

        stdin = new BufferedReader(new InputStreamReader(System.in));

        try
        {
            System.out.print("From: ");
            System.out.flush();

            from = stdin.readLine();

            System.out.print("Subject: ");
            System.out.flush();

            subject = stdin.readLine();

            header = new SimpleNNTPHeader(from, subject);

            System.out.print("Newsgroup: ");
            System.out.flush();

            newsgroup = stdin.readLine();
            header.addNewsgroup(newsgroup);

            while (true)
            {
                System.out.print("Additional Newsgroup <Hit enter to end>: ");
                System.out.flush();

                // Of course you don't want to do this because readLine() may be null
                newsgroup = stdin.readLine().trim();

                if (newsgroup.length() == 0)
                    break;

                header.addNewsgroup(newsgroup);
            }

            System.out.print("Organization: ");
            System.out.flush();

            organization = stdin.readLine();

            System.out.print("References: ");
            System.out.flush();

            references = stdin.readLine();

            if (organization != null && organization.length() > 0)
                header.addHeaderField("Organization", organization);

            if (references != null && organization.length() > 0)
                header.addHeaderField("References", references);

            header.addHeaderField("X-Newsreader", "NetComponents");

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
                System.exit(1);
            }

            client = new NNTPClient();
            client.addProtocolCommandListener(new PrintCommandListener(
                                                  new PrintWriter(System.out)));

            client.connect(server);

            if (!NNTPReply.isPositiveCompletion(client.getReplyCode()))
            {
                client.disconnect();
                System.err.println("NNTP server refused connection.");
                System.exit(1);
            }

            if (client.isAllowedToPost())
            {
                Writer writer = client.postArticle();

                if (writer != null)
                {
                    writer.write(header.toString());
                    Util.copyReader(fileReader, writer);
                    writer.close();
                    client.completePendingCommand();
                }
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


