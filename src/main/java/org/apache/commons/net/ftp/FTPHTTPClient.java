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
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.util.Base64;

/**
 * Experimental attempt at FTP client that tunnels over an HTTP proxy connection.
 *
 * @author rory
 * @since 2.2
 */
public class FTPHTTPClient extends FTPClient {
    private final String proxyHost;
    private final int proxyPort;
    private final String proxyUsername;
    private final String proxyPassword;
    private String host;
    private int port;

    private final byte[] CRLF;
    private final Base64 base64 = new Base64();

    public FTPHTTPClient(String proxyHost, int proxyPort, String proxyUser, String proxyPass) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUser;
        this.proxyPassword = proxyPass;

        try {
            CRLF = "\r\n".getBytes(getControlEncoding());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public FTPHTTPClient(String proxyHost, int proxyPort) {
        this(proxyHost, proxyPort, null, null);
    }


    @Override
    protected Socket _openDataConnection_(int command, String arg)
    throws IOException {
        Socket socket = new Socket(host, port);
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();

        tunnelHandshake(host, port, is, os);

        return socket;
    }

    @Override
    public void connect(String host, int port) throws SocketException,
    IOException {
        this.host = host;
        this.port = port;

        _socket_ = new Socket(proxyHost, proxyPort);
        _input_ = _socket_.getInputStream();
        _output_ = _socket_.getOutputStream();
        try {
            tunnelHandshake(host, port, _input_, _output_);
        }
        catch (Exception e) {
            IOException ioe = new IOException("Could not connect to " + host);
            ioe.initCause(e);
            throw ioe;
        }
    }

    private void tunnelHandshake(String host, int port, InputStream input, OutputStream output) throws IOException,
    UnsupportedEncodingException {
        final String connectString = "CONNECT "  + host + ":" + port  + " HTTP/1.1";

        _output_.write(connectString.getBytes(getControlEncoding()));
        _output_.write(CRLF);

        if (proxyUsername != null && proxyPassword != null) {
            final String header = "Proxy-Authorization: Basic "
                + base64.encode(proxyUsername + ":" + proxyPassword) + "\r\n";
            _output_.write(header.getBytes("UTF-8"));
            _output_.write(CRLF);

            List<String> response = new ArrayList<String>();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(_input_));

            for (String line = reader.readLine(); line != null
            && line.length() > 0; line = reader.readLine()) {
                response.add(line);
            }

            int size = response.size();
            if (size == 0) {
                throw new IOException("No response from proxy");
            }

            String code = null;
            String resp = response.get(0);
            if (resp.startsWith("HTTP/") && resp.length() >= 12) {
                code = resp.substring(9, 12);
            } else {
                throw new IOException("Invalid response from proxy: " + resp);
            }

            if (!"200".equals(code)) {
                StringBuilder msg = new StringBuilder();
                msg.append("HTTPTunnelConnector: connection failed\r\n");
                msg.append("Response received from the proxy:\r\n");
                for (String line : response) {
                    msg.append(line);
                    msg.append("\r\n");
                }
                throw new IOException(msg.toString());
            }
        }
    }
}


