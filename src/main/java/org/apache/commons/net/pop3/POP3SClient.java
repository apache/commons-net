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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.net.io.CRLFLineReader;
import org.apache.commons.net.util.SSLContextUtils;

/**
 * POP3 over SSL processing. Copied from FTPSClient.java and modified to suit POP3.
 * If implicit mode is selected (NOT the default), SSL/TLS negotiation starts right
 * after the connection has been established. In explicit mode (the default), SSL/TLS
 * negotiation starts when the user calls execTLS() and the server accepts the command.
 * Implicit usage:
 *               POP3SClient c = new POP3SClient(true);
 *               c.connect("127.0.0.1", 995);
 * Explicit usage:
 *               POP3SClient c = new POP3SClient();
 *               c.connect("127.0.0.1", 110);
 *               if (c.execTLS()) { /rest of the commands here/ }
 * @since 3.0
 */
public class POP3SClient extends POP3Client
{
    // from http://www.iana.org/assignments/port-numbers

    //    pop3s           995/tcp    pop3 protocol over TLS/SSL (was spop3)
    //    pop3s           995/udp    pop3 protocol over TLS/SSL (was spop3)

    private static final int DEFAULT_POP3S_PORT = 995;

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
    private String[] protocols = //null;
        null;//{"SSLv2", "SSLv3", "TLSv1", "TLSv1.1", "SSLv2Hello"};

    /** The FTPS {@link TrustManager} implementation, default null. */
    private TrustManager trustManager = null;

    /** The {@link KeyManager}, default null. */
    private KeyManager keyManager = null;

    /**
     * Constructor for POP3SClient, using {@link #DEFAULT_PROTOCOL} i.e. TLS
     * Sets security mode to explicit.
     */
    public POP3SClient()
    {
        this(DEFAULT_PROTOCOL, false);
    }

    /**
     * Constructor for POP3SClient, using {@link #DEFAULT_PROTOCOL} i.e. TLS
     * @param implicit The security mode, {@code true} for implicit, {@code false} for explicit
     */
    public POP3SClient(boolean implicit)
    {
        this(DEFAULT_PROTOCOL, implicit);
    }

    /**
     * Constructor for POP3SClient.
     * Sets security mode to explicit.
     * @param proto the protocol.
     */
    public POP3SClient(String proto)
    {
        this(proto, false);
    }

    /**
     * Constructor for POP3SClient.
     * @param proto the protocol.
     * @param implicit The security mode, {@code true} for implicit, {@code false} for explicit
     */
    public POP3SClient(String proto, boolean implicit)
    {
        this(proto, implicit, null);
    }

    /**
     * Constructor for POP3SClient.
     * Sets the default port to {@link #DEFAULT_POP3S_PORT} - 995 - if using implicit mode
     * @param proto the protocol.
     * @param implicit The security mode, {@code true} for implicit, {@code false} for explicit
     * @param ctx the context to be used
     */
    public POP3SClient(String proto, boolean implicit, SSLContext ctx)
    {
        super();
        protocol = proto;
        isImplicit = implicit;
        context = ctx;
        if (isImplicit) {
            setDefaultPort(DEFAULT_POP3S_PORT);
        }
    }

    /**
     * Constructor for POP3SClient, using {@link #DEFAULT_PROTOCOL} i.e. TLS
     * @param implicit The security mode, {@code true} for implicit, {@code false} for explicit
     * @param ctx A pre-configured SSL Context.
     */
    public POP3SClient(boolean implicit, SSLContext ctx)
    {
        this(DEFAULT_PROTOCOL, implicit, ctx);
    }

    /**
     * Constructor for POP3SClient, using {@link #DEFAULT_PROTOCOL} - TLS - and isImplicit = false
     * @param context A pre-configured SSL Context.
     * @see #POP3SClient(boolean, SSLContext)
     */
    public POP3SClient(SSLContext context)
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
        String ip = getRemoteAddress().getHostAddress();
        int port = getRemotePort();
        SSLSocket socket =
            (SSLSocket) ssf.createSocket(_socket_, ip, port, true);
        socket.setEnableSessionCreation(true);
        socket.setUseClientMode(true);

        if (protocols != null) {
            socket.setEnabledProtocols(protocols);
        }
        if (suites != null) {
            socket.setEnabledCipherSuites(suites);
        }
        socket.startHandshake();

        _socket_ = socket;
        _input_ = socket.getInputStream();
        _output_ = socket.getOutputStream();
        _reader = new CRLFLineReader(new InputStreamReader(_input_, _DEFAULT_ENCODING));
        _writer = new BufferedWriter(new OutputStreamWriter(_output_, _DEFAULT_ENCODING));
    }

    /**
     * Get the {@link KeyManager} instance.
     * @return The current {@link KeyManager} instance.
     */
    private KeyManager getKeyManager()
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
     * @throws SSLException If the server reply code is not positive.
     * @throws IOException If an I/O error occurs while sending
     * the command or performing the negotiation.
     * @return TRUE if the command and negotiation succeeded.
     */
    public boolean execTLS() throws SSLException, IOException
    {
        if (sendCommand("STLS") != POP3Reply.OK)
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
}

/* kate: indent-width 4; replace-tabs on; */
