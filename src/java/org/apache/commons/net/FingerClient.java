package org.apache.commons.net;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;

/***
 * The FingerClient class implements the client side of the Internet Finger
 * Protocol defined in RFC 1288.  To finger a host you create a
 * FingerClient instance, connect to the host, query the host, and finally
 * disconnect from the host.  If the finger service you want to query is on
 * a non-standard port, connect to the host at that port.
 * Here's a sample use:
 * <pre>
 *    FingerClient finger;
 * 
 *    finger = new FingerClient();
 *
 *    try {
 *      finger.connect("foo.bar.com");
 *      System.out.println(finger.query("foobar", false));
 *      finger.disconnect();
 *    } catch(IOException e) {
 *      System.err.println("Error I/O exception: " + e.getMessage());
 *      return;
 *    }
 * </pre>
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public class FingerClient extends SocketClient
{
    /***
     * The default FINGER port.  Set to 79 according to RFC 1288.
     ***/
    public static final int DEFAULT_PORT = 79;

    private static final String __LONG_FLAG = "/W ";

    private transient StringBuffer __query = new StringBuffer(64);
    private transient char[] __buffer = new char[1024];

    /***
     * The default FingerClient constructor.  Initializes the
     * default port to <code> DEFAULT_PORT </code>.
     ***/
    public FingerClient()
    {
        setDefaultPort(DEFAULT_PORT);
    }


    /***
     * Fingers a user at the connected host and returns the output
     * as a String.  You must first connect to a finger server before
     * calling this method, and you should disconnect afterward.
     * <p>
     * @param longOutput Set to true if long output is requested, false if not.
     * @param username  The name of the user to finger.
     * @return The result of the finger query.
     * @exception IOException If an I/O error occurs while reading the socket.
     ***/
    public String query(boolean longOutput, String username) throws IOException
    {
        int read;
        StringBuffer result = new StringBuffer(__buffer.length);
        BufferedReader input;

        input =
            new BufferedReader(new InputStreamReader(getInputStream(longOutput,
                               username)));

        while (true)
        {
            read = input.read(__buffer, 0, __buffer.length);
            if (read <= 0)
                break;
            result.append(__buffer, 0, read);
        }

        input.close();

        return result.toString();
    }


    /***
     * Fingers the connected host and returns the output
     * as a String.  You must first connect to a finger server before
     * calling this method, and you should disconnect afterward.
     * This is equivalent to calling <code> query(longOutput, "") </code>.
     * <p>
     * @param longOutput Set to true if long output is requested, false if not.
     * @return The result of the finger query.
     * @exception IOException If an I/O error occurs while reading the socket.
     ***/
    public String query(boolean longOutput) throws IOException
    {
        return query(longOutput, "");
    }


    /***
     * Fingers a user and returns the input stream from the network connection
     * of the finger query.  You must first connect to a finger server before
     * calling this method, and you should disconnect after finishing reading
     * the stream.
     * <p>
     * @param longOutput Set to true if long output is requested, false if not.
     * @param username  The name of the user to finger.
     * @return The InputStream of the network connection of the finger query.
     *         Can be read to obtain finger results.
     * @exception IOException If an I/O error during the operation.
     ***/
    public InputStream getInputStream(boolean longOutput, String username)
    throws IOException
    {
        DataOutputStream output;

        __query.setLength(0);
        if (longOutput)
            __query.append(__LONG_FLAG);
        __query.append(username);
        __query.append(SocketClient.NETASCII_EOL);

        output = 
          new DataOutputStream(new BufferedOutputStream(_output_, 1024));
        output.writeBytes(__query.toString());
        output.flush();

        return _input_;
    }


    /***
     * Fingers the connected host and returns the input stream from
     * the network connection of the finger query.  This is equivalent to
     * calling getInputStream(longOutput, "").  You must first connect to a
     * finger server before calling this method, and you should disconnect
     * after finishing reading the stream.
     * <p>
     * @param longOutput Set to true if long output is requested, false if not.
     * @return The InputStream of the network connection of the finger query.
     *         Can be read to obtain finger results.
     * @exception IOException If an I/O error during the operation.
     ***/
    public InputStream getInputStream(boolean longOutput) throws IOException
    {
        return getInputStream(longOutput, "");
    }

}
