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

/**
 * POP3MessageInfo is used to return information about messages stored on a POP3 server. Its fields are used to mean slightly different things depending on the
 * information being returned.
 * <p>
 * In response to a status command, {@code number} contains the number of messages in the mailbox, {@code size} contains the size of the mailbox
 * in bytes, and {@code identifier} is null.
 * </p>
 * <p>
 * In response to a message listings, {@code number} contains the message number, {@code size} contains the size of the message in bytes, and
 * {@code identifier} is null.
 * </p>
 * <p>
 * In response to unique identifier listings, {@code number} contains the message number, {@code size} is undefined, and {@code identifier}
 * contains the message's unique identifier.
 * </p>
 */
public final class POP3MessageInfo {

    /** Number. */
    public int number;

    /** Size. */
    public int size;

    /** Identifier. */
    public String identifier;

    /**
     * Constructs a new instance with {@code number} and {@code size} set to 0, and {@code identifier} set to null.
     */
    public POP3MessageInfo() {
        this(0, null, 0);
    }

    /**
     * Constructs a new instance with {@code number} set to {@code num}, {@code size} set to {@code octets}, and
     * {@code identifier} set to null.
     *
     * @param num    the number
     * @param octets the size
     */
    public POP3MessageInfo(final int num, final int octets) {
        this(num, null, octets);
    }

    /**
     * Constructs a new instance with {@code number} set to {@code num}, {@code size} undefined, and {@code identifier} set to
     * {@code uid}.
     *
     * @param num the number
     * @param uid the UID
     */
    public POP3MessageInfo(final int num, final String uid) {
        this(num, uid, -1);
    }

    /**
     * Constructs a new instance.
     *
     * @param num    the number.
     * @param uid    the UID.
     * @param octets the size.
     */
    private POP3MessageInfo(final int num, final String uid, final int size) {
        this.number = num;
        this.size = size;
        this.identifier = uid;
    }

    /**
     * @since 3.6
     */
    @Override
    public String toString() {
        return "Number: " + number + ". Size: " + size + ". Id: " + identifier;
    }
}
