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
package org.apache.commons.net.ftp;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import junit.framework.TestCase;

/**
 * Test the socket connect timeout functionality
 */
public class TestConnectTimeout extends TestCase {

    public void testConnectTimeout() throws SocketException, IOException {
        FTPClient client = new FTPClient();
        client.setConnectTimeout(1000);

        try {
            // Connect to a valid host on a bogus port
            // TODO use a local server if possible
            client.connect("www.apache.org", 1234);
            fail("Expecting an Exception");
        }
        catch (ConnectException se) {
            assertTrue(true);
        }
        catch (SocketTimeoutException se) {
            assertTrue(true);
        }
        catch (UnknownHostException ue) {
            // Not much we can do about this, we may be firewalled
            assertTrue(true);
        }

    }
}
