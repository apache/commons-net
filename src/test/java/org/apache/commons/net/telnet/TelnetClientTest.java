/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Duration;

import junit.framework.TestCase;

/**
 * JUnit test class for TelnetClient.s Implements protocol compliance tests
 */
public class TelnetClientTest extends TestCase implements TelnetNotificationHandler {
    /**
     * Handy holder to hold both sides of the connection used in testing for clarity.
     */
    private final class TestConnection {
        private final TelnetTestSimpleServer server;
        private final TelnetClient client;
        private final int port;

        TestConnection(final TelnetTestSimpleServer server, final TelnetClient client, final int port) {
            this.server = server;
            this.client = client;
            this.port = port;
        }

        protected void close() {
            closeConnection(server, client, port);
        }
    }

    // four connections with different properties
    // to use in tests.
    private TestConnection STANDARD;
    private TestConnection OPTIONS;
    private TestConnection ANSI;
    private TestConnection NOREAD;
    private TestConnection SMALL_BUFFER;

    private final int NUM_CONNECTIONS = 5;

    protected int numdo;
    protected int numdont;
    protected int numwill;
    protected int numwont;

    protected int[] lastSubnegotiation;
    protected int lastSubnegotiationLength;

    void closeConnection(final TelnetTestSimpleServer server, final TelnetClient client, final int port) {
        if (server != null) {
            server.disconnect();
            server.stop();
        }
        try {
            if (client != null) {
                client.disconnect();
            }
        } catch (final IOException e) {
            System.err.println("failed to close client-server connection on port " + port);
            System.err.println("ERROR in closeConnection(), " + e.getMessage());
        }

    }

    /*
     * Helper method. compares two arrays of int
     */
    protected boolean equalBytes(final byte a1[], final byte a2[]) {
        if (a1.length != a2.length) {
            return false;
        }
        boolean result = true;
        for (int ii = 0; ii < a1.length; ii++) {

            if (a1[ii] != a2[ii]) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Callback method called when TelnetClient receives an option negotiation command. <p>
     *
     * @param negotiationCode   type of negotiation command received (RECEIVED_DO, RECEIVED_DONT, RECEIVED_WILL, RECEIVED_WONT) <p>
     * @param optionCode   code of the option negotiated <p>
     */
    @Override
    public void receivedNegotiation(final int negotiationCode, final int optionCode) {
        switch (negotiationCode) {
        case TelnetNotificationHandler.RECEIVED_DO:
            numdo++;
            break;
        case TelnetNotificationHandler.RECEIVED_DONT:
            numdont++;
            break;
        case TelnetNotificationHandler.RECEIVED_WILL:
            numwill++;
            break;
        case TelnetNotificationHandler.RECEIVED_WONT:
            numwont++;
            break;
        default:
            break;
        }
    }

    /*
     * open connections needed for the tests for the test.
     */
    @Override
    protected void setUp() throws Exception {
        final SimpleOptionHandler subnegotiationSizeHandler = new SimpleOptionHandler(99, false, false, true, false) {
            @Override
            public int[] answerSubnegotiation(final int[] suboptionData, final int suboptionLength) {
                lastSubnegotiation = suboptionData;
                lastSubnegotiationLength = suboptionLength;
                return null;
            }
        };

        int socket = 0;
        super.setUp();
        for (int port = 3333; socket < NUM_CONNECTIONS && port < 4000; port++) {
            TelnetTestSimpleServer server = null;
            TelnetClient client = null;
            try {
                server = new TelnetTestSimpleServer(port);
                switch (socket) {
                case 0:
                    client = new TelnetClient();
                    // redundant but makes code clearer.
                    client.setReaderThread(true);
                    client.addOptionHandler(subnegotiationSizeHandler);
                    client.connect("127.0.0.1", port);
                    STANDARD = new TestConnection(server, client, port);
                    break;
                case 1:
                    client = new TelnetClient();
                    final TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
                    final EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
                    final SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

                    client.addOptionHandler(ttopt);
                    client.addOptionHandler(echoopt);
                    client.addOptionHandler(gaopt);
                    client.connect("127.0.0.1", port);
                    OPTIONS = new TestConnection(server, client, port);
                    break;
                case 2:
                    client = new TelnetClient("ANSI");
                    client.connect("127.0.0.1", port);
                    ANSI = new TestConnection(server, client, port);
                    break;
                case 3:
                    client = new TelnetClient();
                    client.setReaderThread(false);
                    client.connect("127.0.0.1", port);
                    NOREAD = new TestConnection(server, client, port);
                    break;
                case 4:
                    client = new TelnetClient(8);
                    client.addOptionHandler(subnegotiationSizeHandler);
                    client.connect("127.0.0.1", port);
                    SMALL_BUFFER = new TestConnection(server, client, port);
                    break;
                }
                // only increment socket number on success
                socket++;
            } catch (final IOException e) {
                closeConnection(server, client, port);
            }
        }
        if (socket < NUM_CONNECTIONS) {
            System.err.println("Only created " + socket + " clients; wanted " + NUM_CONNECTIONS);
        }
        Thread.sleep(1000);
    }

    /*
     * @throws Exception
     */
    @Override
    protected void tearDown() throws Exception {
        NOREAD.close();
        ANSI.close();
        OPTIONS.close();
        STANDARD.close();
        SMALL_BUFFER.close();
        try {
            Thread.sleep(1000);
        } catch (final InterruptedException ie) {
            // do nothing
        }
        super.tearDown();
    }

    /*
     * test of AYT functionality
     */
    public void testAYT() throws Exception {
        boolean aytTrueOk = false;
        boolean aytFalseOk = false;

        final byte[] AYT = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.AYT };
        final byte[] response = { (byte) '[', (byte) 'Y', (byte) 'e', (byte) 's', (byte) ']' };
        final String[] inputs = new String[1];
        final String[] outputs = new String[1];
        inputs[0] = new String(AYT);
        outputs[0] = new String(response);

        final OutputStream os = ANSI.server.getOutputStream();
        final InputStream is = ANSI.server.getInputStream();
        final TelnetTestResponder tr = new TelnetTestResponder(is, os, inputs, outputs, 30000);
        assertNotNull(tr);
        final boolean res1 = ANSI.client.sendAYT(Duration.ofSeconds(2));

        if (res1) {
            aytTrueOk = true;
        }

        Thread.sleep(1000);
        is.skip(is.available());

        final boolean res2 = ANSI.client.sendAYT(Duration.ofSeconds(2));

        if (!res2) {
            aytFalseOk = true;
        }

        assertTrue(aytTrueOk);
        assertTrue(aytFalseOk);
    }

    /*
     * protocol compliance test in case of option handler removal
     */
    public void testDeleteOptionHandler() throws Exception {
        boolean removeOk = false;
        boolean removeInvalidOk1 = false;
        boolean removeInvalidOk2 = false;

        final byte[] buffread = new byte[6];
        final byte[] send = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.DO, (byte) TelnetOption.ECHO, (byte) TelnetCommand.IAC, (byte) TelnetCommand.DO,
                (byte) TelnetOption.SUPPRESS_GO_AHEAD, (byte) TelnetCommand.IAC, (byte) TelnetCommand.WILL, (byte) TelnetOption.SUPPRESS_GO_AHEAD };

        final byte[] expected = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.WONT, (byte) TelnetOption.SUPPRESS_GO_AHEAD, (byte) TelnetCommand.IAC,
                (byte) TelnetCommand.DONT, (byte) TelnetOption.SUPPRESS_GO_AHEAD };

        final InputStream is = OPTIONS.server.getInputStream();
        final OutputStream os = OPTIONS.server.getOutputStream();
        Thread.sleep(1000);
        is.skip(is.available());
        os.write(send);
        os.flush();
        Thread.sleep(1000);
        if (is.available() == 0) {
            OPTIONS.client.deleteOptionHandler(TelnetOption.SUPPRESS_GO_AHEAD);
            Thread.sleep(1000);
            if (is.available() == 6) {
                is.read(buffread);
                if (equalBytes(buffread, expected)) {
                    removeOk = true;
                }
            }
        }

        try {
            OPTIONS.client.deleteOptionHandler(TelnetOption.SUPPRESS_GO_AHEAD);
        } catch (final Exception e) {
            removeInvalidOk1 = true;
        }

        try {
            OPTIONS.client.deleteOptionHandler(550);
        } catch (final Exception e) {
            removeInvalidOk2 = true;
        }

        assertTrue(removeOk);
        assertTrue(removeInvalidOk1);
        assertTrue(removeInvalidOk2);
        assertTrue(OPTIONS.client.getLocalOptionState(TelnetOption.ECHO));
        assertFalse(OPTIONS.client.getLocalOptionState(TelnetOption.SUPPRESS_GO_AHEAD));
        assertFalse(OPTIONS.client.getLocalOptionState(TelnetOption.SUPPRESS_GO_AHEAD));
    }

    /*
     * tests the initial condition of the sessions
     */
    public void testInitial() throws Exception {
        boolean connect1Ok = false;
        boolean connect2Ok = false;
        boolean connect3Ok = false;
        boolean init2Ok = false;
        boolean addInvalidOk1 = false;
        boolean addInvalidOk2 = false;
        final byte[] buffread2 = new byte[9];
        final byte[] expected2 = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.WILL, (byte) TelnetOption.ECHO, (byte) TelnetCommand.IAC,
                (byte) TelnetCommand.WILL, (byte) TelnetOption.SUPPRESS_GO_AHEAD, (byte) TelnetCommand.IAC, (byte) TelnetCommand.DO,
                (byte) TelnetOption.SUPPRESS_GO_AHEAD, };

        final SimpleOptionHandler hand = new SimpleOptionHandler(550);
        try {
            STANDARD.client.addOptionHandler(hand);
        } catch (final Exception e) {
            addInvalidOk1 = true;
        }

        try {
            OPTIONS.client.addOptionHandler(hand);
        } catch (final Exception e) {
            addInvalidOk2 = true;
        }

        final InputStream is1 = STANDARD.server.getInputStream();
        Thread.sleep(1000);
        if (is1.available() == 0) {
            connect1Ok = true;
        }

        Thread.sleep(1000);
        final InputStream is2 = OPTIONS.server.getInputStream();
        if (is2.available() == 9) {
            is2.read(buffread2);
            connect2Ok = true;

            if (equalBytes(buffread2, expected2)) {
                init2Ok = true;
            }
        }

        final InputStream is3 = ANSI.server.getInputStream();
        Thread.sleep(1000);
        if (is3.available() == 0) {
            connect3Ok = true;
        }

        assertTrue(connect1Ok);
        assertTrue(connect2Ok);
        assertTrue(connect3Ok);
        assertFalse(STANDARD.client.getLocalOptionState(TelnetOption.ECHO));
        assertFalse(STANDARD.client.getRemoteOptionState(TelnetOption.ECHO));
        assertFalse(OPTIONS.client.getLocalOptionState(TelnetOption.ECHO));
        assertFalse(OPTIONS.client.getRemoteOptionState(TelnetOption.ECHO));
        assertFalse(ANSI.client.getLocalOptionState(TelnetOption.TERMINAL_TYPE));
        assertFalse(ANSI.client.getRemoteOptionState(TelnetOption.TERMINAL_TYPE));
        assertTrue(init2Ok);
        assertTrue(addInvalidOk1);
        assertTrue(addInvalidOk2);
    }

    /*
     * test of max subnegotiation length
     */
    public void testMaxSubnegotiationLength() throws Exception {
        final byte[] send = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.SB, (byte) 99, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6,
                (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) TelnetCommand.IAC,
                (byte) TelnetCommand.SE, };

        final OutputStream os1 = SMALL_BUFFER.server.getOutputStream();
        os1.write(send);
        os1.flush();
        Thread.sleep(500);

        // we sent 16 bytes, but the buffer size should just be 8
        assertEquals(8, lastSubnegotiationLength);
        assertEquals(8, lastSubnegotiation.length);
        assertEquals(99, lastSubnegotiation[0]);
        assertEquals(1, lastSubnegotiation[1]);
        assertEquals(2, lastSubnegotiation[2]);
        assertEquals(3, lastSubnegotiation[3]);
        assertEquals(4, lastSubnegotiation[4]);
        assertEquals(5, lastSubnegotiation[5]);
        assertEquals(6, lastSubnegotiation[6]);
        assertEquals(7, lastSubnegotiation[7]);

        final OutputStream os2 = STANDARD.server.getOutputStream();
        os2.write(send);
        os2.flush();
        Thread.sleep(500);

        // the standard subnegotiation buffer size is 512
        assertEquals(16, lastSubnegotiationLength);
        assertEquals(512, lastSubnegotiation.length);
        assertEquals(99, lastSubnegotiation[0]);
        assertEquals(1, lastSubnegotiation[1]);
        assertEquals(2, lastSubnegotiation[2]);
        assertEquals(3, lastSubnegotiation[3]);
        assertEquals(4, lastSubnegotiation[4]);
        assertEquals(5, lastSubnegotiation[5]);
        assertEquals(6, lastSubnegotiation[6]);
        assertEquals(7, lastSubnegotiation[7]);
    }

    /*
     * test of option negotiation notification
     */
    public void testNotification() throws Exception {
        final byte[] buffread1 = new byte[6];
        final byte[] send1 = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.DO, (byte) 15, (byte) TelnetCommand.IAC, (byte) TelnetCommand.WILL, (byte) 15, };

        final byte[] buffread2 = new byte[9];
        final byte[] send2 = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.DO, (byte) TelnetOption.TERMINAL_TYPE, (byte) TelnetCommand.IAC,
                (byte) TelnetCommand.DONT, (byte) TelnetOption.ECHO, (byte) TelnetCommand.IAC, (byte) TelnetCommand.DO, (byte) TelnetOption.SUPPRESS_GO_AHEAD,
                (byte) TelnetCommand.IAC, (byte) TelnetCommand.WONT, (byte) TelnetOption.SUPPRESS_GO_AHEAD };

        final byte[] buffread2b = new byte[11];

        numdo = 0;
        numdont = 0;
        numwill = 0;
        numwont = 0;
        OPTIONS.client.registerNotifHandler(this);

        final InputStream is1 = STANDARD.server.getInputStream();
        final OutputStream os1 = STANDARD.server.getOutputStream();
        is1.skip(is1.available());
        os1.write(send1);
        os1.flush();
        Thread.sleep(500);
        if (is1.available() > 0) {
            is1.read(buffread1);
        }

        final InputStream is2 = OPTIONS.server.getInputStream();
        final OutputStream os2 = OPTIONS.server.getOutputStream();
        Thread.sleep(500);
        is2.skip(is2.available());
        os2.write(send2);
        os2.flush();
        Thread.sleep(500);
        if (is2.available() > 0) {
            is2.read(buffread2);
            Thread.sleep(1000);
            if (is2.available() > 0) {
                is2.read(buffread2b);
            }
        }

        assertEquals(2, numdo);
        assertEquals(1, numdont);
        assertEquals(1, numwont);
        assertEquals(0, numwill);
    }

    /*
     * protocol compliance test for option negotiation
     */
    public void testOptionNegotiation() throws Exception {
        boolean negotiation1Ok = false;
        final byte[] buffread1 = new byte[6];
        final byte[] send1 = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.DO, (byte) 15, (byte) TelnetCommand.IAC, (byte) TelnetCommand.WILL, (byte) 15, };
        final byte[] expected1 = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.WONT, (byte) 15, (byte) TelnetCommand.IAC, (byte) TelnetCommand.DONT,
                (byte) 15, };

        boolean negotiation2Ok = false;
        final byte[] buffread2 = new byte[9];
        final byte[] send2 = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.DO, (byte) TelnetOption.TERMINAL_TYPE, (byte) TelnetCommand.IAC,
                (byte) TelnetCommand.DONT, (byte) TelnetOption.ECHO, (byte) TelnetCommand.IAC, (byte) TelnetCommand.DO, (byte) TelnetOption.SUPPRESS_GO_AHEAD,
                (byte) TelnetCommand.IAC, (byte) TelnetCommand.WONT, (byte) TelnetOption.SUPPRESS_GO_AHEAD };
        final byte[] expected2 = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.WILL, (byte) TelnetOption.TERMINAL_TYPE, (byte) TelnetCommand.IAC,
                (byte) TelnetCommand.WONT, (byte) TelnetOption.ECHO, (byte) TelnetCommand.IAC, (byte) TelnetCommand.DONT,
                (byte) TelnetOption.SUPPRESS_GO_AHEAD };

        final byte[] buffread2b = new byte[11];
        final byte[] send2b = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.SB, (byte) TelnetOption.TERMINAL_TYPE, (byte) 1, (byte) TelnetCommand.IAC,
                (byte) TelnetCommand.SE, };
        final byte[] expected2b = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.SB, (byte) TelnetOption.TERMINAL_TYPE, (byte) 0, (byte) 'V', (byte) 'T',
                (byte) '1', (byte) '0', (byte) '0', (byte) TelnetCommand.IAC, (byte) TelnetCommand.SE, };

        boolean negotiation3Ok = false;
        final byte[] buffread3 = new byte[6];
        final byte[] send3 = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.DO, (byte) TelnetOption.TERMINAL_TYPE, (byte) TelnetCommand.IAC,
                (byte) TelnetCommand.DO, (byte) TelnetOption.SUPPRESS_GO_AHEAD };
        final byte[] expected3 = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.WILL, (byte) TelnetOption.TERMINAL_TYPE, (byte) TelnetCommand.IAC,
                (byte) TelnetCommand.WONT, (byte) TelnetOption.SUPPRESS_GO_AHEAD };
        final byte[] buffread3b = new byte[10];
        final byte[] send3b = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.SB, (byte) TelnetOption.TERMINAL_TYPE, (byte) 1, (byte) TelnetCommand.IAC,
                (byte) TelnetCommand.SE, };
        final byte[] expected3b = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.SB, (byte) TelnetOption.TERMINAL_TYPE, (byte) 0, (byte) 'A', (byte) 'N',
                (byte) 'S', (byte) 'I', (byte) TelnetCommand.IAC, (byte) TelnetCommand.SE, };

        final InputStream is1 = STANDARD.server.getInputStream();
        final OutputStream os1 = STANDARD.server.getOutputStream();
        is1.skip(is1.available());
        os1.write(send1);
        os1.flush();
        Thread.sleep(1000);
        if (is1.available() == 6) {
            is1.read(buffread1);

            if (equalBytes(buffread1, expected1)) {
                negotiation1Ok = true;
            }
        }

        final InputStream is2 = OPTIONS.server.getInputStream();
        final OutputStream os2 = OPTIONS.server.getOutputStream();
        Thread.sleep(1000);
        is2.skip(is2.available());
        os2.write(send2);
        os2.flush();
        Thread.sleep(1000);
        if (is2.available() == 9) {
            is2.read(buffread2);

            if (equalBytes(buffread2, expected2)) {
                negotiation2Ok = true;
            }

            if (negotiation2Ok) {
                negotiation2Ok = false;
                os2.write(send2b);
                os2.flush();
                Thread.sleep(1000);
                if (is2.available() == 11) {
                    is2.read(buffread2b);

                    if (equalBytes(buffread2b, expected2b)) {
                        negotiation2Ok = true;
                    }
                }
            }
        }

        final InputStream is3 = ANSI.server.getInputStream();
        final OutputStream os3 = ANSI.server.getOutputStream();
        Thread.sleep(1000);
        is3.skip(is3.available());
        os3.write(send3);
        os3.flush();
        Thread.sleep(1000);
        if (is3.available() == 6) {
            is3.read(buffread3);

            if (equalBytes(buffread3, expected3)) {
                negotiation3Ok = true;
            }

            if (negotiation3Ok) {
                negotiation3Ok = false;
                os3.write(send3b);
                os3.flush();
                Thread.sleep(1000);
                if (is3.available() == 10) {
                    is3.read(buffread3b);
                    if (equalBytes(buffread3b, expected3b)) {
                        negotiation3Ok = true;
                    }
                }
            }
        }

        assertTrue(negotiation1Ok);
        assertTrue(negotiation2Ok);
        assertTrue(negotiation3Ok);
        assertFalse(STANDARD.client.getLocalOptionState(15));
        assertFalse(STANDARD.client.getRemoteOptionState(15));
        assertFalse(STANDARD.client.getLocalOptionState(TelnetOption.TERMINAL_TYPE));
        assertFalse(OPTIONS.client.getLocalOptionState(TelnetOption.ECHO));
        assertFalse(OPTIONS.client.getRemoteOptionState(TelnetOption.ECHO));
        assertTrue(OPTIONS.client.getLocalOptionState(TelnetOption.SUPPRESS_GO_AHEAD));
        assertFalse(OPTIONS.client.getRemoteOptionState(TelnetOption.SUPPRESS_GO_AHEAD));
        assertTrue(OPTIONS.client.getLocalOptionState(TelnetOption.TERMINAL_TYPE));
        assertTrue(ANSI.client.getLocalOptionState(TelnetOption.TERMINAL_TYPE));
        assertFalse(OPTIONS.client.getLocalOptionState(TelnetOption.ECHO));
    }

    /*
     * protocol compliance test for option renegotiation
     */
    public void testOptionRenegotiation() throws Exception {
        boolean negotiation1Ok = false;

        final byte[] buffread = new byte[6];
        final byte[] send = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.DO, (byte) TelnetOption.ECHO, (byte) TelnetCommand.IAC, (byte) TelnetCommand.DONT,
                (byte) TelnetOption.SUPPRESS_GO_AHEAD, (byte) TelnetCommand.IAC, (byte) TelnetCommand.WONT, (byte) TelnetOption.SUPPRESS_GO_AHEAD };
        final byte[] expected = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.WONT, (byte) TelnetOption.SUPPRESS_GO_AHEAD, (byte) TelnetCommand.IAC,
                (byte) TelnetCommand.DONT, (byte) TelnetOption.SUPPRESS_GO_AHEAD };

        final byte[] buffread2 = new byte[3];
        final byte[] send2 = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.DONT, (byte) TelnetOption.ECHO, };
        final byte[] expected2 = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.WONT, (byte) TelnetOption.ECHO, };

        final InputStream is = OPTIONS.server.getInputStream();
        final OutputStream os = OPTIONS.server.getOutputStream();
        Thread.sleep(1000);
        is.skip(is.available());
        os.write(send);
        os.flush();
        Thread.sleep(1000);
        if (is.available() == 6) {
            is.read(buffread);

            if (equalBytes(buffread, expected)) {
                negotiation1Ok = true;
            }

            if (negotiation1Ok) {
                negotiation1Ok = false;
                os.write(send2);
                os.flush();
                Thread.sleep(1000);
                if (is.available() == 3) {
                    is.read(buffread2);
                    if (equalBytes(buffread2, expected2)) {
                        negotiation1Ok = true;
                    }
                }
            }
        }

        assertTrue(negotiation1Ok);
        assertFalse(OPTIONS.client.getLocalOptionState(TelnetOption.ECHO));
    }

    /*
     * test of setReaderThread
     */
    public void testSetReaderThread() throws Exception {
        boolean negotiation1Ok = false;
        boolean negotiation2Ok = false;
        boolean readOk = false;
        final byte[] buffread1 = new byte[6];
        final byte[] send1 = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.DO, (byte) 15, (byte) TelnetCommand.IAC, (byte) TelnetCommand.WILL, (byte) 15, };
        final byte[] expected1 = { (byte) TelnetCommand.IAC, (byte) TelnetCommand.WONT, (byte) 15, (byte) TelnetCommand.IAC, (byte) TelnetCommand.DONT,
                (byte) 15, };

        final InputStream is1 = NOREAD.server.getInputStream();
        final OutputStream os1 = NOREAD.server.getOutputStream();
        is1.skip(is1.available());
        os1.write(send1);
        os1.flush();
        os1.write("A".getBytes());
        os1.flush();
        Thread.sleep(1000);
        final InputStream instr = NOREAD.client.getInputStream();
        final byte[] buff = new byte[4];

        final int retRead = instr.read(buff);
        if (retRead == 1 && buff[0] == 'A') {
            readOk = true;
        }

        // if (is1.available() == 6)
        // {
        int read = 0;
        int pos = 0;

        byte[] tmp = new byte[16];
        while (pos < 5) {
            read = is1.read(tmp);
            System.arraycopy(tmp, 0, buffread1, pos, read);
            pos += read;
        }

        if (equalBytes(buffread1, expected1)) {
            negotiation1Ok = true;
            // }
        }

        final InputStream is2 = STANDARD.server.getInputStream();
        final OutputStream os2 = STANDARD.server.getOutputStream();
        Thread.sleep(1000);
        is2.skip(is2.available());
        os2.write(send1);
        os2.flush();
        Thread.sleep(1000);

        tmp = new byte[16];
        while (pos < 5) {
            read = is2.read(tmp);
            System.arraycopy(tmp, 0, buffread1, pos, read);
            pos += read;
        }
        // if (is2.available() == 6)
        // {
        is2.read(buffread1);

        if (equalBytes(buffread1, expected1)) {
            negotiation2Ok = true;
            // }
        }

        assertFalse(NOREAD.client.getReaderThread());
        assertTrue(STANDARD.client.getReaderThread());
        assertTrue("Expected readOk to be true, got " + readOk, readOk);
        assertTrue("Expected negotiation1Ok to be true, got " + negotiation1Ok, negotiation1Ok);
        assertTrue("Expected negotiation2Ok to be true, got " + negotiation2Ok, negotiation2Ok);
    }

    /*
     * test of Spy functionality
     */
    public void testSpy() throws Exception {
        boolean test1spyOk = false;
        boolean test2spyOk = false;
        boolean stopspyOk = false;
        final byte[] expected1 = { (byte) 't', (byte) 'e', (byte) 's', (byte) 't', (byte) '1' };
        final byte[] expected2 = { (byte) 't', (byte) 'e', (byte) 's', (byte) 't', (byte) '2' };

        try (PipedOutputStream po = new PipedOutputStream(); final PipedInputStream pi = new PipedInputStream(po)) {

            final OutputStream os = STANDARD.server.getOutputStream();
            final OutputStream ostc = STANDARD.client.getOutputStream();

            STANDARD.client.registerSpyStream(po);

            os.write("test1".getBytes());
            os.flush();

            Thread.sleep(1000);
            final byte[] buffer = new byte[5];

            if (pi.available() == 5) {
                pi.read(buffer);
                if (equalBytes(buffer, expected1)) {
                    test1spyOk = true;
                }
            }

            ostc.write("test2".getBytes());
            ostc.flush();

            Thread.sleep(1000);

            if (pi.available() == 5) {
                pi.read(buffer);
                if (equalBytes(buffer, expected2)) {
                    test2spyOk = true;
                }
            }

            STANDARD.client.stopSpyStream();
            os.write("test1".getBytes());
            os.flush();
            ostc.write("test2".getBytes());
            ostc.flush();
            Thread.sleep(1000);
            if (pi.available() == 0) {
                stopspyOk = true;
            }

            assertTrue(test1spyOk);
            assertTrue(test2spyOk);
            assertTrue(stopspyOk);
        }
    }

}