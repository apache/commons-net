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
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.commons.net.FingerClient;

/***
 * This is an example of how you would implement the finger command
 * in Java using NetComponents.  The Java version is much shorter.
 * But keep in mind that the Unix finger command reads all sorts of
 * local files to output local finger information.  This program only
 * queries the finger daemon.
 * <p>
 * The -l flag is used to request long output from the server.
 * <p>
 ***/
public final class finger
{

    public static final void main(String[] args)
    {
        boolean longOutput = false;
        int arg = 0, index;
        String handle, host;
        FingerClient finger;
        InetAddress address = null;

        // Get flags.  If an invalid flag is present, exit with usage message.
        while (arg < args.length && args[arg].startsWith("-"))
        {
            if (args[arg].equals("-l"))
                longOutput = true;
            else
            {
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
            catch (UnknownHostException e)
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
            catch (IOException e)
            {
                System.err.println("Error I/O exception: " + e.getMessage());
                System.exit(1);
            }

            return ;
        }

        // Finger each argument
        while (arg < args.length)
        {

            index = args[arg].lastIndexOf("@");

            if (index == -1)
            {
                handle = args[arg];
                try
                {
                    address = InetAddress.getLocalHost();
                }
                catch (UnknownHostException e)
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
                }
                catch (UnknownHostException e)
                {
                    System.err.println("Error unknown host: " + e.getMessage());
                    System.exit(1);
                }
            }

            System.out.println("[" + address.getHostName() + "]");

            try
            {
                finger.connect(address);
                System.out.print(finger.query(longOutput, handle));
                finger.disconnect();
            }
            catch (IOException e)
            {
                System.err.println("Error I/O exception: " + e.getMessage());
                System.exit(1);
            }

            ++arg;
            if (arg != args.length)
                System.out.print("\n");
        }
    }
}

