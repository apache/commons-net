package org.apache.commons.net.smtp;

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

import java.util.Enumeration;
import java.util.Vector;

/***
 * A class used to represent forward and reverse relay paths.  The
 * SMTP MAIL command requires a reverse relay path while the SMTP RCPT
 * command requires a forward relay path.  See RFC 821 for more details.
 * In general, you will not have to deal with relay paths.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see SMTPClient
 ***/

public final class RelayPath
{
    Vector _path;
    String _emailAddress;

    /***
     * Create a relay path with the specified email address as the ultimate
     * destination.
     * <p>
     * @param emailAddress The destination email address.
     ***/
    public RelayPath(String emailAddress)
    {
        _path = new Vector();
        _emailAddress = emailAddress;
    }

    /***
     * Add a mail relay host to the relay path.  Hosts are added left to
     * right.  For example, the following will create the path
     * <code><b> &lt @bar.com,@foo.com:foobar@foo.com &gt </b></code>
     * <pre>
     * path = new RelayPath("foobar@foo.com");
     * path.addRelay("bar.com");
     * path.addRelay("foo.com");
     * </pre>
     * <p>
     * @param hostname The host to add to the relay path.
     ***/
    public void addRelay(String hostname)
    {
        _path.addElement(hostname);
    }

    /***
     * Return the properly formatted string representation of the relay path.
     * <p>
     * @return The properly formatted string representation of the relay path.
     ***/
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        Enumeration hosts;

        buffer.append('<');

        hosts = _path.elements();

        if (hosts.hasMoreElements())
        {
            buffer.append('@');
            buffer.append((String)hosts.nextElement());

            while (hosts.hasMoreElements())
            {
                buffer.append(",@");
                buffer.append((String)hosts.nextElement());
            }
            buffer.append(':');
        }

        buffer.append(_emailAddress);
        buffer.append('>');

        return buffer.toString();
    }

}
