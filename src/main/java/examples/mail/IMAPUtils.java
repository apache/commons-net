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

package examples.mail;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Locale;

import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.imap.IMAPClient;
import org.apache.commons.net.imap.IMAPSClient;

/**
 * Utility class for shared IMAP utilities
 */

class IMAPUtils {

    /**
     * Parse the URI and use the details to connect to the IMAP(S) server and login.
     *
     * @param uri the URI to use, e.g. imaps://user:pass@imap.mail.yahoo.com/folder
     * or imaps://user:pass@imap.googlemail.com/folder
     * @param defaultTimeout initial timeout (in milliseconds)
     * @param listener for tracing protocol IO (may be null)
     * @return the IMAP client - connected and logged in
     * @throws IOException if any problems occur
     */
    static IMAPClient imapLogin(URI uri, int defaultTimeout, ProtocolCommandListener listener) throws IOException {
        final String userInfo = uri.getUserInfo();
        if (userInfo == null) {
            throw new IllegalArgumentException("Missing userInfo details");
        }

        String []userpass = userInfo.split(":");
        if (userpass.length != 2) {
            throw new IllegalArgumentException("Invalid userInfo details: '" + userInfo + "'");
        }

        String username = userpass[0];
        String password = userpass[1];
        /*
         * If the initial password is:
         * '*' - replace it with a line read from the system console
         * '-' - replace it with next line from STDIN
         * 'ABCD' - if the input is all upper case, use the field as an environment variable name
         *
         * Note: there are no guarantees that the password cannot be snooped.
         *
         * Even using the console may be subject to memory snooping,
         * however it should be safer than the other methods.
         *
         * STDIN may require creating a temporary file which could be read by others
         * Environment variables may be visible by using PS
         */
        if ("-".equals(password)) { // stdin
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            password = in.readLine();
        } else if ("*".equals(password)) { // console
            Console con = System.console(); // Java 1.6
            if (con != null) {
                char[] pwd = con.readPassword("Password for " + username + ": ");
                password = new String(pwd);
            } else {
                throw new IOException("Cannot access Console");
            }
        } else if (password.equals(password.toUpperCase(Locale.ROOT))) { // environment variable name
            final String tmp = System.getenv(password);
            if (tmp != null) { // don't overwrite if variable does not exist (just in case password is all uppers)
                password=tmp;
            }
        }

        final IMAPClient imap;

        final String scheme = uri.getScheme();
        if ("imaps".equalsIgnoreCase(scheme)) {
            System.out.println("Using secure protocol");
            imap = new IMAPSClient(true); // implicit
        } else if ("imap".equalsIgnoreCase(scheme)) {
            imap = new IMAPClient();
        } else {
            throw new IllegalArgumentException("Invalid protocol: " + scheme);
        }
        final int port = uri.getPort();
        if (port != -1) {
            imap.setDefaultPort(port);
        }

        imap.setDefaultTimeout(defaultTimeout);

        if (listener != null) {
            imap.addProtocolCommandListener(listener);
        }

        final String server = uri.getHost();
        System.out.println("Connecting to server " + server + " on " + imap.getDefaultPort());

        try {
            imap.connect(server);
            System.out.println("Successfully connected");
        } catch (IOException e) {
            throw new RuntimeException("Could not connect to server.", e);
        }

        if (!imap.login(username, password)) {
            imap.disconnect();
            throw new RuntimeException("Could not login to server. Check login details.");
        }

        return imap;
    }
}
