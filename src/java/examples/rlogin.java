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

import java.io.IOException;
import org.apache.commons.net.bsd.RLoginClient;

/***
 * This is an example program demonstrating how to use the RLoginClient
 * class. This program connects to an rlogin daemon and begins to
 * interactively read input from stdin (this will be line buffered on most
 * systems, so don't expect character at a time interactivity), passing it
 * to the remote login process and writing the remote stdout and stderr
 * to local stdout.  If you don't have .rhosts or hosts.equiv files set up,
 * the rlogin daemon will prompt you for a password.
 * <p>
 * On Unix systems you will not be able to use the rshell capability
 * unless the process runs as root since only root can bind port addresses
 * lower than 1024.
 * <p>
 * JVM's using green threads will likely have problems if the rlogin daemon
 * requests a password.  This program is merely a demonstration and is
 * not suitable for use as an application, especially given that it relies
 * on line buffered input from System.in.  The best way to run this example
 * is probably from a Win95 dos box into a Unix host.
 * <p>
 * Example: java rlogin myhost localusername remoteusername vt100
 * <p>
 * Usage: rlogin <hostname> <localuser> <remoteuser> <terminal>
 * <p>
 ***/

// This class requires the IOUtil support class!
public final class rlogin
{

    public static final void main(String[] args)
    {
        String server, localuser, remoteuser, terminal;
        RLoginClient client;

        if (args.length != 4)
        {
            System.err.println(
                "Usage: rlogin <hostname> <localuser> <remoteuser> <terminal>");
            System.exit(1);
            return ; // so compiler can do proper flow control analysis
        }

        client = new RLoginClient();

        server = args[0];
        localuser = args[1];
        remoteuser = args[2];
        terminal = args[3];

        try
        {
            client.connect(server);
        }
        catch (IOException e)
        {
            System.err.println("Could not connect to server.");
            e.printStackTrace();
            System.exit(1);
        }

        try
        {
            client.rlogin(localuser, remoteuser, terminal);
        }
        catch (IOException e)
        {
            try
            {
                client.disconnect();
            }
            catch (IOException f)
            {}
            e.printStackTrace();
            System.err.println("rlogin authentication failed.");
            System.exit(1);
        }


        IOUtil.readWrite(client.getInputStream(), client.getOutputStream(),
                         System.in, System.out);

        try
        {
            client.disconnect();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }

}

