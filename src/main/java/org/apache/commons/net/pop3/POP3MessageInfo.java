/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.pop3;

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
        this(0, null, 0);
    }

    /***
     * Creates a POP3MessageInfo instance with <code>number</code> set
     * to <code> num </code>, <code> size </code> set to <code> octets </code>,
     * and <code>identifier</code> set to null.
     ***/
    public POP3MessageInfo(int num, int octets)
    {
        this(num, null, octets);
    }

    /***
     * Creates a POP3MessageInfo instance with <code>number</code> set
     * to <code> num </code>, <code> size </code> undefined,
     * and <code>identifier</code> set to <code>uid</code>.
     ***/
    public POP3MessageInfo(int num, String uid)
    {
        this(num, uid, -1);
    }

    private POP3MessageInfo(int num, String uid, int size) {
        this.number = num;
        this.size = size;
        this.identifier = uid;
    }
}
