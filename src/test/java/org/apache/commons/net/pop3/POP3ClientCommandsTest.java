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
import java.io.Reader;
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
public class POP3ClientCommandsTest extends TestCase {
    POP3Client pop3Client;

    String user = POP3Constants.user;
    String emptyUser = POP3Constants.emptyuser;
    String password = POP3Constants.password;
    String mailhost = POP3Constants.mailhost;

    public POP3ClientCommandsTest(final String name) {
        super(name);
    }

    private void connect() throws Exception {
        pop3Client.connect(InetAddress.getByName(mailhost));
        assertTrue(pop3Client.isConnected());
        assertEquals(POP3.AUTHORIZATION_STATE, pop3Client.getState());
    }

    private void login() throws Exception {
        assertTrue(pop3Client.login(user, password));
        assertEquals(POP3.TRANSACTION_STATE, pop3Client.getState());
    }

    private void reset() throws IOException {
        // Case where this is the first time reset is called
        if (pop3Client == null) {
            // Do nothing
        } else if (pop3Client.isConnected()) {
            pop3Client.disconnect();
        }
        pop3Client = null;
        pop3Client = new POP3Client();
    }

    public void testDelete() throws Exception {
        reset();
        connect();
        login();
        // Get the original number of messages
        POP3MessageInfo[] msg = pop3Client.listMessages();
        final int numMessages = msg.length;
        int numDeleted = 0;

        // Now delete some and logout
        for (int i = 0; i < numMessages - 3; i++) {
            pop3Client.deleteMessage(i + 1);
            numDeleted++;
        }
        // Check to see that they are marked as deleted
        assertEquals(numMessages, numDeleted + 3);

        // Logout and come back in
        pop3Client.logout();
        reset();
        connect();
        login();

        // Get the new number of messages, because of
        // reset, new number should match old number
        msg = pop3Client.listMessages();
        assertEquals(numMessages - numDeleted, msg.length);
    }

    public void testDeleteWithReset() throws Exception {
        reset();
        connect();
        login();
        // Get the original number of messages
        POP3MessageInfo[] msg = pop3Client.listMessages();
        final int numMessages = msg.length;
        int numDeleted = 0;

        // Now delete some and logout
        for (int i = 0; i < numMessages - 1; i++) {
            pop3Client.deleteMessage(i + 1);
            numDeleted++;
        }
        // Check to see that they are marked as deleted
        assertEquals(numMessages, numDeleted + 1);

        // Now reset to unmark the messages as deleted
        pop3Client.reset();

        // Logout and come back in
        pop3Client.logout();
        reset();
        connect();
        login();

        // Get the new number of messages, because of
        // reset, new number should match old number
        msg = pop3Client.listMessages();
        assertEquals(numMessages, msg.length);
    }

    public void testListMessageOnEmptyMailbox() throws Exception {
        reset();
        connect();
        assertTrue(pop3Client.login(emptyUser, password));

        // The first message is always at index 1
        final POP3MessageInfo msg = pop3Client.listMessage(1);
        assertNull(msg);
    }

    public void testListMessageOnFullMailbox() throws Exception {
        reset();
        connect();
        login();

        // The first message is always at index 1
        POP3MessageInfo msg = pop3Client.listMessage(1);
        assertNotNull(msg);
        assertEquals(1, msg.number);
        assertTrue(msg.size > 0);
        assertNull(msg.identifier);

        // Now retrieve a message from index 0
        msg = pop3Client.listMessage(0);
        assertNull(msg);

        // Now retrieve a msg that is not there
        msg = pop3Client.listMessage(100000);
        assertNull(msg);

        // Now retrieve a msg with a negative index
        msg = pop3Client.listMessage(-2);
        assertNull(msg);

        // Now try to get a valid message from the update state
        pop3Client.setState(POP3.UPDATE_STATE);
        msg = pop3Client.listMessage(1);
        assertNull(msg);
    }

    public void testListMessagesOnEmptyMailbox() throws Exception {
        reset();
        connect();
        assertTrue(pop3Client.login(emptyUser, password));

        POP3MessageInfo[] msg = pop3Client.listMessages();
        assertEquals(0, msg.length);

        // Now test from the update state
        pop3Client.setState(POP3.UPDATE_STATE);
        msg = pop3Client.listMessages();
        assertNull(msg);
    }

    public void testListMessagesOnFullMailbox() throws Exception {
        reset();
        connect();
        login();

        POP3MessageInfo[] msg = pop3Client.listMessages();
        assertTrue(msg.length > 0);

        for (int i = 0; i < msg.length; i++) {
            assertNotNull(msg[i]);
            assertEquals(i + 1, msg[i].number);
            assertTrue(msg[i].size > 0);
            assertNull(msg[i].identifier);
        }

        // Now test from the update state
        pop3Client.setState(POP3.UPDATE_STATE);
        msg = pop3Client.listMessages();
        assertNull(msg);
    }

    public void testListUniqueIdentifierOnEmptyMailbox() throws Exception {
        reset();
        connect();
        assertTrue(pop3Client.login(emptyUser, password));

        // The first message is always at index 1
        final POP3MessageInfo msg = pop3Client.listUniqueIdentifier(1);
        assertNull(msg);
    }

    public void testListUniqueIDOnFullMailbox() throws Exception {
        reset();
        connect();
        login();

        // The first message is always at index 1
        POP3MessageInfo msg = pop3Client.listUniqueIdentifier(1);
        assertNotNull(msg);
        assertEquals(1, msg.number);
        assertNotNull(msg.identifier);

        // Now retrieve a message from index 0
        msg = pop3Client.listUniqueIdentifier(0);
        assertNull(msg);

        // Now retrieve a msg that is not there
        msg = pop3Client.listUniqueIdentifier(100000);
        assertNull(msg);

        // Now retrieve a msg with a negative index
        msg = pop3Client.listUniqueIdentifier(-2);
        assertNull(msg);

        // Now try to get a valid message from the update state
        pop3Client.setState(POP3.UPDATE_STATE);
        msg = pop3Client.listUniqueIdentifier(1);
        assertNull(msg);
    }

    public void testListUniqueIDsOnEmptyMailbox() throws Exception {
        reset();
        connect();
        assertTrue(pop3Client.login(emptyUser, password));

        POP3MessageInfo[] msg = pop3Client.listUniqueIdentifiers();
        assertEquals(0, msg.length);

        // Now test from the update state
        pop3Client.setState(POP3.UPDATE_STATE);
        msg = pop3Client.listUniqueIdentifiers();
        assertNull(msg);
    }

    public void testListUniqueIDsOnFullMailbox() throws Exception {
        reset();
        connect();
        login();

        POP3MessageInfo[] msg = pop3Client.listUniqueIdentifiers();
        assertTrue(msg.length > 0);

        for (int i = 0; i < msg.length; i++) {
            assertNotNull(msg[i]);
            assertEquals(i + 1, msg[i].number);
            assertNotNull(msg[i].identifier);
        }

        // Now test from the update state
        pop3Client.setState(POP3.UPDATE_STATE);
        msg = pop3Client.listUniqueIdentifiers();
        assertNull(msg);
    }

    public void testNoopCommand() throws Exception {
        reset();
        connect();

        // Should fail before authorization
        assertFalse(pop3Client.noop());

        // Should pass in transaction state
        login();
        assertTrue(pop3Client.noop());

        // Should fail in update state
        pop3Client.setState(POP3.UPDATE_STATE);
        assertFalse(pop3Client.noop());
    }

    public void testResetAndDeleteShouldFails() throws Exception {
        reset();
        connect();
        login();

        pop3Client.setState(POP3.UPDATE_STATE);
        assertFalse(pop3Client.reset());

        assertFalse(pop3Client.deleteMessage(1));
    }

    public void testRetrieveMessageOnEmptyMailbox() throws Exception {
        reset();
        connect();
        assertTrue(pop3Client.login(emptyUser, password));
        assertNull(pop3Client.retrieveMessage(1));
    }

    public void testRetrieveMessageOnFullMailbox() throws Exception {
        reset();
        connect();
        login();
        int reportedSize = 0;
        int actualSize = 0;

        final POP3MessageInfo[] msg = pop3Client.listMessages();
        assertTrue(msg.length > 0);

        for (int i = msg.length; i > 0; i--) {
            reportedSize = msg[i - 1].size;
            final Reader r = pop3Client.retrieveMessage(i);
            assertNotNull(r);

            int delaycount = 0;
            if (!r.ready()) {
                // Give the reader time to get the message
                // from the server
                Thread.sleep(500);
                delaycount++;
                // but don't wait too long
                if (delaycount == 4) {
                    break;
                }
            }
            while (r.ready()) {
                r.read();
                actualSize++;
            }
            // Due to variations in line termination
            // on different platforms, the actual
            // size may vary slightly. On Win2KPro, the
            // actual size is 2 bytes larger than the reported
            // size.
            assertTrue(actualSize >= reportedSize);
        }
    }

    public void testRetrieveMessageShouldFails() throws Exception {
        reset();
        connect();
        login();

        // Try to get message 0
        assertNull(pop3Client.retrieveMessage(0));

        // Try to get a negative message
        assertNull(pop3Client.retrieveMessage(-2));

        // Try to get a message that is not there
        assertNull(pop3Client.retrieveMessage(100000));

        // Change states and try to get a valid message
        pop3Client.setState(POP3.UPDATE_STATE);
        assertNull(pop3Client.retrieveMessage(1));
    }

    public void testRetrieveMessageTopOnEmptyMailbox() throws Exception {
        reset();
        connect();
        assertTrue(pop3Client.login(emptyUser, password));
        assertNull(pop3Client.retrieveMessageTop(1, 10));
    }

    public void testRetrieveMessageTopOnFullMailbox() throws Exception {
        reset();
        connect();
        login();
        final int numLines = 10;

        final POP3MessageInfo[] msg = pop3Client.listMessages();
        assertTrue(msg.length > 0);

        for (int i = 0; i < msg.length; i++) {
            try (Reader reader = pop3Client.retrieveMessageTop(i + 1, numLines)) {
                assertNotNull(reader);
            }
        }
    }

    public void testRetrieveMessageTopShouldFails() throws Exception {
        reset();
        connect();
        login();

        // Try to get message 0
        assertNull(pop3Client.retrieveMessageTop(0, 10));

        // Try to get a negative message
        assertNull(pop3Client.retrieveMessageTop(-2, 10));

        // Try to get a message that is not there
        assertNull(pop3Client.retrieveMessageTop(100000, 10));

        // Change states and try to get a valid message
        pop3Client.setState(POP3.UPDATE_STATE);
        assertNull(pop3Client.retrieveMessageTop(1, 10));
    }

    public void testRetrieveOverSizedMessageTopOnFullMailbox() throws Exception {
        reset();
        connect();
        login();
        int actualSize = 0;

        final POP3MessageInfo msg = pop3Client.listMessage(1);
        final int reportedSize = msg.size;

        // Now try to retrieve more lines than exist in the message
        final Reader r = pop3Client.retrieveMessageTop(1, 100000);
        assertNotNull(r);

        int delaycount = 0;
        while (!r.ready()) {
            // Give the reader time to get the message
            // from the server
            Thread.sleep(500);
            delaycount++;
            // but don't wait too long
            if (delaycount == 4) {
                break;
            }
        }
        while (r.ready()) {
            r.read();
            actualSize++;
        }
        // Due to variations in line termination
        // on different platforms, the actual
        // size may vary slightly. On Win2KPro, the
        // actual size is 2 bytes larger than the reported
        // size.
        assertTrue(actualSize >= reportedSize);
    }

    public void testStatus() throws Exception {
        reset();
        connect();

        // Should fail in authorization state
        assertNull(pop3Client.status());

        // Should pass on a mailbox with mail in it
        login();
        final POP3MessageInfo msg = pop3Client.status();
        assertTrue(msg.number > 0);
        assertTrue(msg.size > 0);
        assertNull(msg.identifier);
        pop3Client.logout();

        // Should also pass on a mailbox with no mail in it
        reset();
        connect();
        assertTrue(pop3Client.login(emptyUser, password));
        final POP3MessageInfo msg2 = pop3Client.status();
        assertEquals(0, msg2.number);
        assertEquals(0, msg2.size);
        assertNull(msg2.identifier);
        pop3Client.logout();

        // Should fail in the 'update' state
        reset();
        connect();
        login();
        pop3Client.setState(POP3.UPDATE_STATE);
        assertNull(pop3Client.status());
    }
}
