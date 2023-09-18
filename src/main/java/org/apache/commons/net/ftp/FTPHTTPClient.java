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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Inet6Address;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Experimental attempt at FTP client that tunnels over an HTTP proxy connection.
 *
 * @since 2.2
 */
public class FTPHTTPClient extends FTPClient {

    private static final byte[] CRLF = { '\r', '\n' };
    private final String proxyHost;
    private final int proxyPort;
    private final String proxyUsername;
    private final String proxyPassword;
    private final Charset charset;
    private String tunnelHost; // Save the host when setting up a tunnel (needed for EPSV)

    /**
     * Create an instance using the UTF-8 encoding, with no proxy credentials.
     *
     * @param proxyHost the hostname to use
     * @param proxyPort the port to use
     */
    public FTPHTTPClient(final String proxyHost, final int proxyPort) {
        this(proxyHost, proxyPort, null, null);
    }

    /**
     * Create an instance using the specified encoding, with no proxy credentials.
     *
     * @param proxyHost the hostname to use
     * @param proxyPort the port to use
     * @param encoding  the encoding to use
     */
    public FTPHTTPClient(final String proxyHost, final int proxyPort, final Charset encoding) {
        this(proxyHost, proxyPort, null, null, encoding);
    }

    /**
     * Create an instance using the UTF-8 encoding
     *
     * @param proxyHost the hostname to use
     * @param proxyPort the port to use
     * @param proxyUser the user name for the proxy
     * @param proxyPass the password for the proxy
     */
    public FTPHTTPClient(final String proxyHost, final int proxyPort, final String proxyUser, final String proxyPass) {
        this(proxyHost, proxyPort, proxyUser, proxyPass, StandardCharsets.UTF_8);
    }

    /**
     * Create an instance with the specified encoding
     *
     * @param proxyHost the hostname to use
     * @param proxyPort the port to use
     * @param proxyUser the user name for the proxy
     * @param proxyPass the password for the proxy
     * @param encoding  the encoding to use
     */
    public FTPHTTPClient(final String proxyHost, final int proxyPort, final String proxyUser, final String proxyPass, final Charset encoding) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUser;
        this.proxyPassword = proxyPass;
        this.tunnelHost = null;
        this.charset = encoding;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if connection mode is not passive
     * @deprecated (3.3) Use {@link FTPClient#_openDataConnection_(FTPCmd, String)} instead
     */
    // Kept to maintain binary compatibility
    // Not strictly necessary, but Clirr complains even though there is a super-impl
    @Override
    @Deprecated
    protected Socket _openDataConnection_(final int command, final String arg) throws IOException {
        return super._openDataConnection_(command, arg);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if connection mode is not passive
     * @since 3.1
     */
    @Override
    protected Socket _openDataConnection_(final String command, final String arg) throws IOException {
        // Force local passive mode, active mode not supported by through proxy
        if (getDataConnectionMode() != PASSIVE_LOCAL_DATA_CONNECTION_MODE) {
            throw new IllegalStateException("Only passive connection mode supported");
        }

        final boolean isInet6Address = getRemoteAddress() instanceof Inet6Address;
        String passiveHost;

        final boolean attemptEPSV = isUseEPSVwithIPv4() || isInet6Address;
        if (attemptEPSV && epsv() == FTPReply.ENTERING_EPSV_MODE) {
            _parseExtendedPassiveModeReply(_replyLines.get(0));
            passiveHost = this.tunnelHost;
        } else {
            if (isInet6Address) {
                return null; // Must use EPSV for IPV6
            }
            // If EPSV failed on IPV4, revert to PASV
            if (pasv() != FTPReply.ENTERING_PASSIVE_MODE) {
                return null;
            }
            _parsePassiveModeReply(_replyLines.get(0));
            passiveHost = this.getPassiveHost();
        }

        final Socket socket = _socketFactory_.createSocket(proxyHost, proxyPort);
        final InputStream is = socket.getInputStream();
        final OutputStream os = socket.getOutputStream();
        tunnelHandshake(passiveHost, this.getPassivePort(), is, os);
        if (getRestartOffset() > 0 && !restart(getRestartOffset())) {
            socket.close();
            return null;
        }

        if (!FTPReply.isPositivePreliminary(sendCommand(command, arg))) {
            socket.close();
            return null;
        }

        return socket;
    }

    @Override
    public void connect(final String host, final int port) throws SocketException, IOException {

        _socket_ = _socketFactory_.createSocket(proxyHost, proxyPort);
        _input_ = _socket_.getInputStream();
        _output_ = _socket_.getOutputStream();
        final Reader socketIsReader;
        try {
            socketIsReader = tunnelHandshake(host, port, _input_, _output_);
        } catch (final Exception e) {
            final IOException ioe = new IOException("Could not connect to " + host + " using port " + port);
            ioe.initCause(e);
            throw ioe;
        }
        super._connectAction_(socketIsReader);
    }

    private BufferedReader tunnelHandshake(final String host, final int port, final InputStream input, final OutputStream output)
            throws IOException, UnsupportedEncodingException {
        final String connectString = "CONNECT " + host + ":" + port + " HTTP/1.1";
        final String hostString = "Host: " + host + ":" + port;

        this.tunnelHost = host;
        output.write(connectString.getBytes(charset));
        output.write(CRLF);
        output.write(hostString.getBytes(charset));
        output.write(CRLF);

        if (proxyUsername != null && proxyPassword != null) {
            final String auth = proxyUsername + ":" + proxyPassword;
            final String header = "Proxy-Authorization: Basic " + Base64.getEncoder().encodeToString(auth.getBytes(charset));
            output.write(header.getBytes(charset));
        }
        output.write(CRLF);

        final List<String> response = new ArrayList<>();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, getCharset()));

        for (String line = reader.readLine(); line != null && !line.isEmpty(); line = reader.readLine()) {
            response.add(line);
        }

        final int size = response.size();
        if (size == 0) {
            throw new IOException("No response from proxy");
        }

        String code;
        final String resp = response.get(0);
        if (!resp.startsWith("HTTP/") || resp.length() < 12) {
            throw new IOException("Invalid response from proxy: " + resp);
        }
        code = resp.substring(9, 12);

        if (!"200".equals(code)) {
            final StringBuilder msg = new StringBuilder();
            msg.append("HTTPTunnelConnector: connection failed\r\n");
            msg.append("Response received from the proxy:\r\n");
            for (final String line : response) {
                msg.append(line);
                msg.append("\r\n");
            }
            throw new IOException(msg.toString());
        }
        return reader;
    }
}
