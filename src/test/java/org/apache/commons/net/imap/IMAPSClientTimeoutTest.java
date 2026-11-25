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

package org.apache.commons.net.imap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.junit.jupiter.api.Test;

/** Checks configured read timeouts during implicit and explicit IMAPS TLS handshakes. */
class IMAPSClientTimeoutTest {

    private static final int READ_TIMEOUT = 150;

    private static final long AWAIT_MILLIS = 3000;

    private static final ThreadFactory DAEMONS = new ThreadFactory() {

        public Thread newThread(final Runnable runnable) {
            final Thread thread = new Thread(runnable, "imaps-timeout-peer");
            thread.setDaemon(true);
            return thread;
        }
    };

    @Test
    void implicitConnectUsesTheDefaultReadTimeoutForTheHandshake() throws Exception {
        final IMAPSClient client = new IMAPSClient(true);
        client.setConnectTimeout(300);
        client.setDefaultTimeout(READ_TIMEOUT);
        final Loopback loopback = new Loopback(false);
        try {
            final Future<IOException> call = loopback.execute(new ClientAction() {

                public void run() throws IOException {
                    client.connect(loopback.address(), loopback.port());
                }
            });
            assertReadTimedOut(awaitFailure(call, AWAIT_MILLIS));
            loopback.assertTlsStageReached();
        } finally {
            try {
                loopback.close();
            } finally {
                client.disconnect();
            }
        }
    }

    @Test
    void explicitStartTlsKeepsTheReadTimeoutSetAfterConnect() throws Exception {
        final IMAPSClient client = new IMAPSClient(false);
        final Loopback loopback = new Loopback(true);
        try {
            final Future<IOException> call = loopback.execute(new ClientAction() {

                public void run() throws IOException {
                    client.connect(loopback.address(), loopback.port());
                    assertEquals(0, client.getSoTimeout(), "explicit mode must connect without a read timeout");
                    client.setSoTimeout(READ_TIMEOUT);
                    assertEquals(READ_TIMEOUT, client.getSoTimeout(), "setSoTimeout must apply the read timeout to the socket");
                    client.execTLS();
                }
            });
            assertReadTimedOut(awaitFailure(call, AWAIT_MILLIS));
            loopback.assertTlsStageReached();
            try {
                assertEquals(READ_TIMEOUT, client.getSoTimeout(), "the TLS command must not reset the caller's read timeout");
            } catch (final SocketException socketClosedByFailedHandshake) {
                // The failed handshake closed the socket, so the stored value can no longer be read back.
            }
        } finally {
            try {
                loopback.close();
            } finally {
                client.disconnect();
            }
        }
    }

    @Test
    void implicitConnectWithoutReadTimeoutKeepsWaiting() throws Exception {
        final IMAPSClient client = new IMAPSClient(true);
        client.setConnectTimeout(150);
        client.setDefaultTimeout(0);
        final Loopback loopback = new Loopback(false);
        try {
            final Future<IOException> call = loopback.execute(new ClientAction() {

                public void run() throws IOException {
                    client.connect(loopback.address(), loopback.port());
                }
            });
            try {
                final IOException premature = call.get(400, TimeUnit.MILLISECONDS);
                fail("the handshake read must keep waiting when no read timeout is configured, but the call ended with: "
                        + premature);
            } catch (final TimeoutException expected) {
                // No read timeout was configured, so the handshake read is still pending.
            }
            loopback.assertTlsStageReached();
        } finally {
            try {
                loopback.close();
            } finally {
                client.disconnect();
            }
        }
    }

    @Test
    void layeredSslSocketReportsTheUnderlyingReadTimeout() throws Exception {
        final ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        Socket raw = null;
        Socket accepted = null;
        try {
            raw = new Socket();
            raw.connect(new InetSocketAddress(server.getInetAddress(), server.getLocalPort()), 1000);
            accepted = server.accept();
            raw.setSoTimeout(READ_TIMEOUT);
            final SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            final SSLSocket layered = (SSLSocket) factory.createSocket(raw, server.getInetAddress().getHostAddress(),
                    server.getLocalPort(), true);
            assertEquals(READ_TIMEOUT, layered.getSoTimeout(),
                    "a layered SSLSocket must report the read timeout of the socket it wraps");
        } finally {
            closeQuietly(raw);
            closeQuietly(accepted);
            closeQuietly(server);
        }
    }

    private static void assertReadTimedOut(final IOException failure) {
        Throwable cause = failure;
        while (cause != null) {
            if (cause instanceof SocketTimeoutException) {
                return;
            }
            cause = cause.getCause();
        }
        fail("expected the pending TLS handshake read to time out, but the call failed with: " + failure);
    }

    private static IOException awaitFailure(final Future<IOException> call, final long timeoutMillis) throws Exception {
        final IOException failure;
        try {
            failure = call.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (final TimeoutException stillBlocked) {
            return fail("the client call was still blocked after " + timeoutMillis
                    + " ms; the TLS handshake read did not observe the configured read timeout");
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof AssertionError) {
                throw (AssertionError) cause;
            }
            throw new AssertionError("the client call failed with an unexpected exception", cause);
        }
        if (failure == null) {
            return fail("the client call completed without an I/O failure; expected a socket read timeout");
        }
        return failure;
    }

    private static void closeQuietly(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final IOException ignored) {
                // Cleanup only.
            }
        }
    }

    private static void hold(final Socket socket, final CountDownLatch tlsStage) {
        try {
            final InputStream in = socket.getInputStream();
            if (in.read() >= 0) {
                // Real TLS bytes have arrived; the handshake is unambiguously in progress.
                tlsStage.countDown();
            }
            while (in.read() >= 0) {
                // Keep reading, but never reply to the TLS handshake.
            }
        } catch (final IOException connectionClosed) {
            // The client or the test closed the connection; stop holding it open.
        }
    }

    private interface ClientAction {

        void run() throws IOException;
    }

    private static final class Loopback implements Closeable {

        private final ServerSocket server;
        private final ExecutorService executor;
        private final AtomicReference<Socket> accepted = new AtomicReference<Socket>();
        private final CountDownLatch tlsStage = new CountDownLatch(1);
        private final Future<Void> peer;

        Loopback(final boolean explicit) throws IOException {
            server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
            executor = Executors.newFixedThreadPool(2, DAEMONS);
            peer = executor.submit(new Callable<Void>() {

                public Void call() throws Exception {
                    serve(explicit);
                    return null;
                }
            });
        }

        InetAddress address() {
            return server.getInetAddress();
        }

        int port() {
            return server.getLocalPort();
        }

        Future<IOException> execute(final ClientAction action) {
            return executor.submit(new Callable<IOException>() {

                public IOException call() throws Exception {
                    try {
                        action.run();
                        return null;
                    } catch (final IOException e) {
                        return e;
                    }
                }
            });
        }

        /** Asserts that the peer reached the TLS stage, and surfaces any peer side assertion failure. */
        void assertTlsStageReached() throws Exception {
            final boolean reached = tlsStage.await(2, TimeUnit.SECONDS);
            if (peer.isDone()) {
                try {
                    peer.get();
                } catch (final ExecutionException e) {
                    throw new AssertionError("the loopback peer did not answer as expected", e.getCause());
                }
            }
            assertTrue(reached, "the client never reached the TLS handshake stage");
        }

        public void close() {
            closeQuietly(accepted.get());
            closeQuietly(server);
            executor.shutdownNow();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void serve(final boolean explicit) throws Exception {
            final Socket socket = server.accept();
            accepted.set(socket);
            if (explicit) {
                final OutputStream out = socket.getOutputStream();
                out.write("* OK loopback IMAPS peer ready\r\n".getBytes(StandardCharsets.US_ASCII));
                out.flush();
                final BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
                final String command = in.readLine();
                final int space = command == null ? -1 : command.indexOf(' ');
                if (space < 0) {
                    throw new AssertionError("expected a tagged STARTTLS command, but received: " + command);
                }
                final String tag = command.substring(0, space);
                if (!"STARTTLS".equalsIgnoreCase(command.substring(space + 1).trim())) {
                    throw new AssertionError("expected STARTTLS to be requested, but received: " + command);
                }
                out.write((tag + " OK begin TLS negotiation\r\n").getBytes(StandardCharsets.US_ASCII));
                out.flush();
            }
            hold(socket, tlsStage);
        }
    }
}
