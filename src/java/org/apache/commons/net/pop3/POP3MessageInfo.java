package org.apache.commons.net.pop3;

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

/***
 * POP3MessageInfo is used to return information about messages stored on
 * a POP3 server.  Its fields are used to mean slightly different things
 * depending on the information being returned.
 * <p>
 * In response to a status command, <code> number </code>
 * contains the number of messages in the mailbox, <code> size </code> 
 * contains the size of the mailbox in bytes, and <code> identifier </code>
 * is null.
 * <p>
 * In response to a message listings, <code> number </code>
 * contains the message number, <code> size </code> contains the
 * size of the message in bytes, and <code> identifier </code> is null.
 * <p>
 * In response to unique identifier listings, <code> number </code> contains
 * the message number, <code> size </code> is undefined, and
 * <code> identifier </code> contains the message's unique identifier.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class POP3MessageInfo
{
    public int number;
    public int size;
    public String identifier;

    /***
     * Creates a POP3MessageInfo instance with <code>number</code> and
     * <code> size </code> set to 0, and <code>identifier</code> set to
     * null.
     ***/
    public POP3MessageInfo()
    {
        number = size = 0;
        identifier = null;
    }

    /***
     * Creates a POP3MessageInfo instance with <code>number</code> set
     * to <code> num </code>, <code> size </code> set to <code> octets </code>,
     * and <code>identifier</code> set to null.
     ***/
    public POP3MessageInfo(int num, int octets)
    {
        number = num;
        size = octets;
        identifier = null;
    }

    /***
     * Creates a POP3MessageInfo instance with <code>number</code> set
     * to <code> num </code>, <code> size </code> undefined,
     * and <code>identifier</code> set to <code>uid</code>.
     ***/
    public POP3MessageInfo(int num, String uid)
    {
        number = num;
        size = -1;
        identifier = uid;
    }
}
