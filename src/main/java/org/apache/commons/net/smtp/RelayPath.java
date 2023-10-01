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

package org.apache.commons.net.smtp;

import java.util.Enumeration;
import java.util.Vector;

/**
 * A class used to represent forward and reverse relay paths. The SMTP MAIL command requires a reverse relay path while the SMTP RCPT command requires a forward
 * relay path. See RFC 821 for more details. In general, you will not have to deal with relay paths.
 *
 * @see SMTPClient
 */
public final class RelayPath {
    private final Vector<String> path;
    private final String emailAddress;

    /**
     * Create a relay path with the specified email address as the ultimate destination.
     *
     * @param emailAddress The destination email address.
     */
    public RelayPath(final String emailAddress) {
        this.path = new Vector<>();
        this.emailAddress = emailAddress;
    }

    /**
     * Add a mail relay host to the relay path. Hosts are added left to right. For example, the following will create the path
     * <code><b> &lt; @bar.com,@foo.com:foobar@foo.com &gt; </b></code>
     *
     * <pre>
     * path = new RelayPath("foobar@foo.com");
     * path.addRelay("bar.com");
     * path.addRelay("foo.com");
     * </pre>
     *
     * @param hostname The host to add to the relay path.
     */
    public void addRelay(final String hostname) {
        path.addElement(hostname);
    }

    /**
     * Return the properly formatted string representation of the relay path.
     *
     * @return The properly formatted string representation of the relay path.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        final Enumeration<String> hosts;

        buffer.append('<');

        hosts = path.elements();

        if (hosts.hasMoreElements()) {
            buffer.append('@');
            buffer.append(hosts.nextElement());

            while (hosts.hasMoreElements()) {
                buffer.append(",@");
                buffer.append(hosts.nextElement());
            }
            buffer.append(':');
        }

        buffer.append(emailAddress);
        buffer.append('>');

        return buffer.toString();
    }

}
