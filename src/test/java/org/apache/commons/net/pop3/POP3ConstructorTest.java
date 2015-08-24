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

import junit.framework.TestCase;
import java.io.Reader;

/**
 * The POP3* tests all presume the existence of the following parameters:
 *   mailserver: localhost (running on the default port 110)
 *   account: username=test; password=password
 *   account: username=alwaysempty; password=password.
 *   mail: At least four emails in the test account and zero emails
 *         in the alwaysempty account
 *
 * If this won't work for you, you can change these parameters in the
 * TestSetupParameters class.
 *
 * The tests were originally run on a default installation of James.
 * Your mileage may vary based on the POP3 server you run the tests against.
 * Some servers are more standards-compliant than others.
 */
public class POP3ConstructorTest extends TestCase
{
    String user = POP3Constants.user;
    String emptyUser = POP3Constants.emptyuser;
    String password = POP3Constants.password;
    String mailhost = POP3Constants.mailhost;

    public POP3ConstructorTest(String name)
    {
        super(name);
    }

    /*
     * This test will ensure that the constants are not inadvertently changed.
     * If the constants are changed in org.apache.commons.net.pop3 for some
     * reason, this test will have to be updated.
     */
    public void testConstants()
    {
        //From POP3
        assertEquals(110, POP3.DEFAULT_PORT);
        assertEquals(-1, POP3.DISCONNECTED_STATE);
        assertEquals(0, POP3.AUTHORIZATION_STATE);
        assertEquals(1, POP3.TRANSACTION_STATE);
        assertEquals(2, POP3.UPDATE_STATE);

        //From POP3Command
        assertEquals(0, POP3Command.USER);
        assertEquals(1, POP3Command.PASS);
        assertEquals(2, POP3Command.QUIT);
        assertEquals(3, POP3Command.STAT);
        assertEquals(4, POP3Command.LIST);
        assertEquals(5, POP3Command.RETR);
        assertEquals(6, POP3Command.DELE);
        assertEquals(7, POP3Command.NOOP);
        assertEquals(8, POP3Command.RSET);
        assertEquals(9, POP3Command.APOP);
        assertEquals(10, POP3Command.TOP);
        assertEquals(11, POP3Command.UIDL);
    }

    public void testPOP3DefaultConstructor()
    {
        POP3 pop = new POP3();

        assertEquals(110, pop.getDefaultPort());
        assertEquals(POP3.DISCONNECTED_STATE, pop.getState());
        assertNull(pop._reader);
        assertNotNull(pop._replyLines);
    }

    public void testPOP3ClientStateTransition() throws Exception
    {
        POP3Client pop = new POP3Client();

        //Initial state
        assertEquals(110, pop.getDefaultPort());
        assertEquals(POP3.DISCONNECTED_STATE, pop.getState());
        assertNull(pop._reader);
        assertNotNull(pop._replyLines);

        //Now connect
        pop.connect(mailhost);
        assertEquals(POP3.AUTHORIZATION_STATE, pop.getState());

        //Now authenticate
        pop.login(user, password);
        assertEquals(POP3.TRANSACTION_STATE, pop.getState());

        //Now do a series of commands and make sure the state stays as it should
        pop.noop();
        assertEquals(POP3.TRANSACTION_STATE, pop.getState());
        pop.status();
        assertEquals(POP3.TRANSACTION_STATE, pop.getState());

        //Make sure we have at least one message to test
        POP3MessageInfo[] msg = pop.listMessages();

        if (msg.length > 0)
        {
            pop.deleteMessage(1);
            assertEquals(POP3.TRANSACTION_STATE, pop.getState());

            pop.reset();
            assertEquals(POP3.TRANSACTION_STATE, pop.getState());

            pop.listMessage(1);
            assertEquals(POP3.TRANSACTION_STATE, pop.getState());

            pop.listMessages();
            assertEquals(POP3.TRANSACTION_STATE, pop.getState());

            pop.listUniqueIdentifier(1);
            assertEquals(POP3.TRANSACTION_STATE, pop.getState());

            pop.listUniqueIdentifiers();
            assertEquals(POP3.TRANSACTION_STATE, pop.getState());

            Reader r = pop.retrieveMessage(1);
            assertEquals(POP3.TRANSACTION_STATE, pop.getState());

            //Add some sleep here to handle network latency
            while(!r.ready())
            {
                Thread.sleep(10);
            }
            r.close();
            r = null;

            r = pop.retrieveMessageTop(1, 10);
            assertEquals(POP3.TRANSACTION_STATE, pop.getState());

            //Add some sleep here to handle network latency
            while(!r.ready())
            {
                Thread.sleep(10);
            }
            r.close();
            r = null;

        }

        //Now logout
        pop.logout();
        assertEquals(POP3.UPDATE_STATE, pop.getState());
    }
}
