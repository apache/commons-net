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

import java.io.IOException;
import java.io.InputStream;

/***
 * The WhoisClient class implements the client side of the Internet Whois
 * Protocol defined in RFC 954.   To query a host you create a
 * WhoisClient instance, connect to the host, query the host, and finally
 * disconnect from the host.  If the whois service you want to query is on
 * a non-standard port, connect to the host at that port.
 * Here's a sample use:
 * <pre>
 *    WhoisClient whois;
 *
 *    whois = new WhoisClient();
 *
 *    try {
 *      whois.connect(WhoisClient.DEFAULT_HOST);
 *      System.out.println(whois.query("foobar"));
 *      whois.disconnect();
 *    } catch(IOException e) {
 *      System.err.println("Error I/O exception: " + e.getMessage());
 *      return;
 *    }
 * </pre>
 *
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class WhoisClient extends FingerClient
{
    /***
     * The default whois host to query.  It is set to whois.internic.net.
     ***/
    public static final String DEFAULT_HOST = "whois.internic.net";

    /***
     * The default whois port.  It is set to 43 according to RFC 954.
     ***/
    public static final int DEFAULT_PORT = 43;


    /***
     * The default whois constructor.    Initializes the
     * default port to <code> DEFAULT_PORT </code>.
     ***/
    public WhoisClient()
    {
        setDefaultPort(DEFAULT_PORT);
    }

    /***
     * Queries the connected whois server for information regarding
     * the given handle.  It is up to the programmer to be familiar with the
     * handle syntax of the whois server.  You must first connect to a whois
     * server before calling this method, and you should disconnect afterward.
     * <p>
     * @param handle  The handle to lookup.
     * @return The result of the whois query.
     * @exception IOException  If an I/O error occurs during the operation.
     ***/
    public String query(String handle) throws IOException
    {
        return query(false, handle);
    }


    /***
     * Queries the connected whois server for information regarding
     * the given handle and returns the InputStream of the network connection.
     * It is up to the programmer to be familiar with the handle syntax
     * of the whois server.  You must first connect to a finger server before
     * calling this method, and you should disconnect after finishing reading
     * the stream.
     * <p>
     * @param handle  The handle to lookup.
     * @return The InputStream of the network connection of the whois query.
     *         Can be read to obtain whois results.
     * @exception IOException  If an I/O error occurs during the operation.
     ***/
    public InputStream getInputStream(String handle) throws IOException
    {
        return getInputStream(false, handle);
    }

}

