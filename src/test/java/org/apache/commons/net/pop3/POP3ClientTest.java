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

import java.io.IOException;
import java.net.InetAddress;

import junit.framework.TestCase;

/**
 *
 * The POP3* tests all presume the existence of the following parameters: mailserver: localhost (running on the default port 110) account: username=test;
 * password=password account: username=alwaysempty; password=password. mail: At least four emails in the test account and zero emails in the alwaysempty account
 *
 * If this won't work for you, you can change these parameters in the TestSetupParameters class.
 *
 * The tests were originally run on a default installation of James. Your mileage may vary based on the POP3 server you run the tests against. Some servers are
 * more standards-compliant than others.
 */
public class POP3ClientTest extends TestCase {
    POP3Client p;

    String user = POP3Constants.user;
    String emptyUser = POP3Constants.emptyuser;
    String password = POP3Constants.password;
    String mailhost = POP3Constants.mailhost;

    public POP3ClientTest(final String name) {
        super(name);
    }

    private void connect() throws Exception {
        p.connect(InetAddress.getByName(mailhost));
        assertTrue(p.isConnected());
        assertEquals(POP3.AUTHORIZATION_STATE, p.getState());
    }

    private void login() throws Exception {
        assertTrue(p.login(user, password));
        assertEquals(POP3.TRANSACTION_STATE, p.getState());
    }

    private void reset() throws IOException {
        // Case where this is the first time reset is called
        if (p == null) {
            // Do nothing
        } else if (p.isConnected()) {
            p.disconnect();
        }
        p = null;
        p = new POP3Client();
    }

    public void testInvalidLoginWithBadName() throws Exception {
        reset();
        connect();

        // Try with an invalid user that doesn't exist
        assertFalse(p.login("badusername", password));
    }

    public void testInvalidLoginWithBadPassword() throws Exception {
        reset();
        connect();

        // Try with a bad password
        assertFalse(p.login(user, "badpassword"));
    }

    /*
     * Test to try to run the login method from the disconnected, transaction and update states
     */
    public void testLoginFromWrongState() throws Exception {
        reset();

        // Not currently connected, not in authorization state
        // Try to login with good name/password
        assertFalse(p.login(user, password));

        // Now connect and set the state to 'transaction' and try again
        connect();
        p.setState(POP3.TRANSACTION_STATE);
        assertFalse(p.login(user, password));
        p.disconnect();

        // Now connect and set the state to 'update' and try again
        connect();
        p.setState(POP3.UPDATE_STATE);
        assertFalse(p.login(user, password));
        p.disconnect();
    }

    public void testLogoutFromAllStates() throws Exception {
        // From 'transaction' state
        reset();
        connect();
        login();
        assertTrue(p.logout());
        assertEquals(POP3.UPDATE_STATE, p.getState());

        // From 'update' state
        reset();
        connect();
        login();
        p.setState(POP3.UPDATE_STATE);
        assertTrue(p.logout());
    }

    /*
     * Simple test to logon to a valid server using a valid user name and password.
     */
    public void testValidLoginWithNameAndPassword() throws Exception {
        reset();
        connect();

        // Try with a valid user
        login();
    }
}
