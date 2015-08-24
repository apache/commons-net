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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.net.io.CRLFLineReader;
import org.apache.commons.net.util.SSLContextUtils;
import org.apache.commons.net.util.SSLSocketUtils;

/**
 * SMTP over SSL processing. Copied from FTPSClient.java and modified to suit SMTP.
 * If implicit mode is selected (NOT the default), SSL/TLS negotiation starts right
 * after the connection has been established. In explicit mode (the default), SSL/TLS
 * negotiation starts when the user calls execTLS() and the server accepts the command.
 * Implicit usage:
 *               SMTPSClient c = new SMTPSClient(true);
 *               c.connect("127.0.0.1", 465);
 * Explicit usage:
 *               SMTPSClient c = new SMTPSClient();
 *               c.connect("127.0.0.1", 25);
 *               if (c.execTLS()) { /rest of the commands here/ }
 *
 * Warning: the hostname is not verified against the certificate by default, use
 * {@link #setHostnameVerifier(HostnameVerifier)} or {@link #setEndpointCheckingEnabled(boolean)}
 * (on Java 1.7+) to enable verification.
 * @since 3.0
 */
public class SMTPSClient extends SMTPClient
{
    /** Default secure socket protocol name, like TLS */
    private static final String DEFAULT_PROTOCOL = "TLS";

    /** The security mode. True - Implicit Mode / False - Explicit Mode. */
    private final boolean isImplicit;
    /** The secure socket protocol to be used, like SSL/TLS. */
    private final String protocol;
    /** The context object. */
    private SSLContext context = null;
    /** The cipher suites. SSLSockets have a default set of these anyway,
        so no initialization required. */
    private String[] suites = null;
    /** The protocol versions. */
    private String[] protocols = null;

    /** The {@link TrustManager} implementation, default null (i.e. use system managers). */
    private TrustManager trustManager = null;

    /** The {@link KeyManager}, default null (i.e. use system managers). */
    private KeyManager keyManager = null; // seems not to be required

    /** The {@link HostnameVerifier} to use post-TLS, default null (i.e. no verification). */
    private HostnameVerifier hostnameVerifier = null;

    /** Use Java 1.7+ HTTPS Endpoint Identification Algorithim. */
    private boolean tlsEndpointChecking;

    /**
     * Constructor for SMTPSClient, using {@link #DEFAULT_PROTOCOL} i.e. TLS
     * Sets security mode to explicit (isImplicit = false).
     */
    public SMTPSClient()
    {
        this(DEFAULT_PROTOCOL, false);
    }

    /**
     * Constructor for SMTPSClient, using {@link #DEFAULT_PROTOCOL} i.e. TLS
     * @param implicit The security mode, {@code true} for implicit, {@code false} for explicit
     */
    public SMTPSClient(boolean implicit)
    {
        this(DEFAULT_PROTOCOL, implicit);
    }

    /**
     * Constructor for SMTPSClient, using explicit security mode.
     * @param proto the protocol.
     */
    public SMTPSClient(String proto)
    {
        this(proto, false);
    }

    /**
     * Constructor for SMTPSClient.
     * @param proto the protocol.
     * @param implicit The security mode, {@code true} for implicit, {@code false} for explicit
     */
    public SMTPSClient(String proto, boolean implicit)
    {
        protocol = proto;
        isImplicit = implicit;
    }

    /**
     * Constructor for SMTPSClient.
     * @param proto the protocol.
     * @param implicit The security mode, {@code true} for implicit, {@code false} for explicit
     * @param encoding the encoding
     * @since 3.3
     */
    public SMTPSClient(String proto, boolean implicit, String encoding)
    {
        super(encoding);
        protocol = proto;
        isImplicit = implicit;
    }

    /**
     * Constructor for SMTPSClient, using {@link #DEFAULT_PROTOCOL} i.e. TLS
     * @param implicit The security mode, {@code true} for implicit, {@code false} for explicit
     * @param ctx A pre-configured SSL Context.
     */
    public SMTPSClient(boolean implicit, SSLContext ctx)
    {
        isImplicit = implicit;
        context = ctx;
        protocol = DEFAULT_PROTOCOL;
    }

    /**
     * Constructor for SMTPSClient.
     * @param context A pre-configured SSL Context.
     * @see #SMTPSClient(boolean, SSLContext)
     */
    public SMTPSClient(SSLContext context)
    {
        this(false, context);
    }

    /**
     * Because there are so many connect() methods,
     * the _connectAction_() method is provided as a means of performing
     * some action immediately after establishing a connection,
     * rather than reimplementing all of the connect() methods.
     * @throws IOException If it is thrown by _connectAction_().
     * @see org.apache.commons.net.SocketClient#_connectAction_()
     */
    @Override
    protected void _connectAction_() throws IOException
    {
        // Implicit mode.
        if (isImplicit) {
            performSSLNegotiation();
        }
        super._connectAction_();
        // Explicit mode - don't do anything. The user calls execTLS()
    }

    /**
     * Performs a lazy init of the SSL context.
     * @throws IOException When could not initialize the SSL context.
     */
    private void initSSLContext() throws IOException
    {
        if (context == null)
        {
            context = SSLContextUtils.createSSLContext(protocol, getKeyManager(), getTrustManager());
        }
    }

    /**
     * SSL/TLS negotiation. Acquires an SSL socket of a
     * connection and carries out handshake processing.
     * @throws IOException If server negotiation fails.
     */
    private void performSSLNegotiation() throws IOException
    {
        initSSLContext();

        SSLSocketFactory ssf = context.getSocketFactory();
        String host = (_hostname_ != null) ? _hostname_ : getRemoteAddress().getHostAddress();
        int port = getRemotePort();
        SSLSocket socket =
            (SSLSocket) ssf.createSocket(_socket_, host, port, true);
        socket.setEnableSessionCreation(true);
        socket.setUseClientMode(true);

        if (tlsEndpointChecking) {
            SSLSocketUtils.enableEndpointNameVerification(socket);
        }
        if (protocols != null) {
            socket.setEnabledProtocols(protocols);
        }
        if (suites != null) {
            socket.setEnabledCipherSuites(suites);
        }
        socket.startHandshake();

        // TODO the following setup appears to duplicate that in the super class methods
        _socket_ = socket;
        _input_ = socket.getInputStream();
        _output_ = socket.getOutputStream();
        _reader = new CRLFLineReader(
                        new InputStreamReader(_input_, encoding));
        _writer = new BufferedWriter(
                        new OutputStreamWriter(_output_, encoding));

        if (hostnameVerifier != null && !hostnameVerifier.verify(host, socket.getSession())) {
            throw new SSLHandshakeException("Hostname doesn't match certificate");
        }
    }

    /**
     * Get the {@link KeyManager} instance.
     * @return The current {@link KeyManager} instance.
     */
    public KeyManager getKeyManager()
    {
        return keyManager;
    }

    /**
     * Set a {@link KeyManager} to use.
     * @param newKeyManager The KeyManager implementation to set.
     * @see org.apache.commons.net.util.KeyManagerUtils
     */
    public void setKeyManager(KeyManager newKeyManager)
    {
        keyManager = newKeyManager;
    }

    /**
     * Controls which particular cipher suites are enabled for use on this
     * connection. Called before server negotiation.
     * @param cipherSuites The cipher suites.
     */
    public void setEnabledCipherSuites(String[] cipherSuites)
    {
        suites = new String[cipherSuites.length];
        System.arraycopy(cipherSuites, 0, suites, 0, cipherSuites.length);
    }

    /**
     * Returns the names of the cipher suites which could be enabled
     * for use on this connection.
     * When the underlying {@link java.net.Socket Socket} is not an {@link SSLSocket} instance, returns null.
     * @return An array of cipher suite names, or <code>null</code>.
     */
    public String[] getEnabledCipherSuites()
    {
        if (_socket_ instanceof SSLSocket)
        {
            return ((SSLSocket)_socket_).getEnabledCipherSuites();
        }
        return null;
    }

    /**
     * Controls which particular protocol versions are enabled for use on this
     * connection. I perform setting before a server negotiation.
     * @param protocolVersions The protocol versions.
     */
    public void setEnabledProtocols(String[] protocolVersions)
    {
        protocols = new String[protocolVersions.length];
        System.arraycopy(protocolVersions, 0, protocols, 0, protocolVersions.length);
    }

    /**
     * Returns the names of the protocol versions which are currently
     * enabled for use on this connection.
     * When the underlying {@link java.net.Socket Socket} is not an {@link SSLSocket} instance, returns null.
     * @return An array of protocols, or <code>null</code>.
     */
    public String[] getEnabledProtocols()
    {
        if (_socket_ instanceof SSLSocket)
        {
            return ((SSLSocket)_socket_).getEnabledProtocols();
        }
        return null;
    }

    /**
     * The TLS command execution.
     * @throws IOException If an I/O error occurs while sending
     * the command or performing the negotiation.
     * @return TRUE if the command and negotiation succeeded.
     */
    public boolean execTLS() throws IOException
    {
        if (!SMTPReply.isPositiveCompletion(sendCommand("STARTTLS")))
        {
            return false;
            //throw new SSLException(getReplyString());
        }
        performSSLNegotiation();
        return true;
    }

    /**
     * Get the currently configured {@link TrustManager}.
     * @return A TrustManager instance.
     */
    public TrustManager getTrustManager()
    {
        return trustManager;
    }

    /**
     * Override the default {@link TrustManager} to use.
     * @param newTrustManager The TrustManager implementation to set.
     * @see org.apache.commons.net.util.TrustManagerUtils
     */
    public void setTrustManager(TrustManager newTrustManager)
    {
        trustManager = newTrustManager;
    }

    /**
     * Get the currently configured {@link HostnameVerifier}.
     * @return A HostnameVerifier instance.
     * @since 3.4
     */
    public HostnameVerifier getHostnameVerifier()
    {
        return hostnameVerifier;
    }

    /**
     * Override the default {@link HostnameVerifier} to use.
     * @param newHostnameVerifier The HostnameVerifier implementation to set or <code>null</code> to disable.
     * @since 3.4
     */
    public void setHostnameVerifier(HostnameVerifier newHostnameVerifier)
    {
        hostnameVerifier = newHostnameVerifier;
    }

    /**
     * Return whether or not endpoint identification using the HTTPS algorithm
     * on Java 1.7+ is enabled. The default behaviour is for this to be disabled.
     *
     * @return True if enabled, false if not.
     * @since 3.4
     */
    public boolean isEndpointCheckingEnabled()
    {
        return tlsEndpointChecking;
    }

    /**
     * Automatic endpoint identification checking using the HTTPS algorithm
     * is supported on Java 1.7+. The default behaviour is for this to be disabled.
     *
     * @param enable Enable automatic endpoint identification checking using the HTTPS algorithm on Java 1.7+.
     * @since 3.4
     */
    public void setEndpointCheckingEnabled(boolean enable)
    {
        tlsEndpointChecking = enable;
    }
}

/* kate: indent-width 4; replace-tabs on; */
