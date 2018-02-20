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

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Experimental attempt at FTP client that tunnels over an HTTP proxy connection.
 *
 * @since 2.2
 */
public class FTPSocks5Client extends FTPClient {
	private static final int RESERVED = 0;
	private static final int SOCKS_ADDRESS_TYPE_DOMAIN = 3;
	private static final int SOCKS_METHOD_CONNECT = 1;
	private static final int SOCKS_VERSION = 5;
	private static final int MAX_CHAR_COUNT_ALLOWED = 255;
	private static final String CHARSET = "UTF-8";

	private final String proxyHost;
	private final int proxyPort;
	private final String proxyUsername;
	private final String proxyPassword;
	private String tunnelHost;

	public FTPSocks5Client(String proxyHost, int proxyPort, String proxyUser, String proxyPass) {
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.proxyUsername = proxyUser;
		this.proxyPassword = proxyPass;
		this.tunnelHost = null;
	}

	public FTPSocks5Client(String proxyHost, int proxyPort) {
		this(proxyHost, proxyPort, null, null);
	}

	protected Socket _openDataConnection_(String command, String arg) throws IOException {
		if (getDataConnectionMode() != 2) {
			throw new IllegalStateException("Only passive connection mode supported");
		}
		boolean isInet6Address = getRemoteAddress() instanceof Inet6Address;
		String passiveHost = null;

		boolean attemptEPSV = (isUseEPSVwithIPv4()) || (isInet6Address);
		if ((attemptEPSV) && (epsv() == 229)) {
			_parseExtendedPassiveModeReply((String) this._replyLines.get(0));
			passiveHost = this.tunnelHost;
		} else {
			if (isInet6Address) {
				return null;
			}
			if (pasv() != 227) {
				return null;
			}
			_parsePassiveModeReply((String) this._replyLines.get(0));
			passiveHost = getPassiveHost();
		}
		Socket socket = this._socketFactory_.createSocket(this.proxyHost, this.proxyPort);
		InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();
		tunnelHandshake(passiveHost, getPassivePort(), is, os);
		if ((getRestartOffset() > 0L) && (!restart(getRestartOffset()))) {
			socket.close();
			return null;
		}
		if (!FTPReply.isPositivePreliminary(sendCommand(command, arg))) {
			socket.close();
			return null;
		}
		return socket;
	}

	public void connect(String host, int port) throws SocketException, IOException {
		this._socket_ = this._socketFactory_.createSocket(this.proxyHost, this.proxyPort);
		this._input_ = this._socket_.getInputStream();
		this._output_ = this._socket_.getOutputStream();
		Reader socketIsReader;
		try {
			socketIsReader = tunnelHandshake(host, port, this._input_, this._output_);
		} catch (Exception e) {
			IOException exception = new IOException("Could not connect to " + host + " using port " + port);
			exception.initCause(e);
			throw exception;
		}
		super._connectAction_(socketIsReader);
	}

	private BufferedReader tunnelHandshake(String host, int port, InputStream input, OutputStream output)
			throws IOException, UnsupportedEncodingException {

		this.tunnelHost = host;

		output.write(SOCKS_VERSION);

		boolean isAuthenticationEnabled = proxyUsername != null && proxyPassword != null;

		output.write((isAuthenticationEnabled) ? (new byte[] { 1, 2 }) : (new byte[] { 1, 0 }));

		int socketReadValue = read(input);

		if (socketReadValue != 5) {
			throw new IOException("Invalid proxy response");
		}

		socketReadValue = read(input);

		if (isAuthenticationEnabled) {
			if (socketReadValue != 2) {
				throw new IOException("Proxy doesn't support username/password authentication method");
			}

			byte[] usernameBytes = proxyUsername.getBytes(CHARSET);
			byte[] passwordBytes = proxyPassword.getBytes(CHARSET);

			int userLength = usernameBytes.length;
			int passLength = passwordBytes.length;

			if (userLength > MAX_CHAR_COUNT_ALLOWED) {
				throw new IOException("Username should not exceed " + MAX_CHAR_COUNT_ALLOWED + " chars");
			}
			if (passLength > MAX_CHAR_COUNT_ALLOWED) {
				throw new IOException("Password should not exceed " + MAX_CHAR_COUNT_ALLOWED + " chars");
			}
			output.write(1);
			output.write(userLength);
			output.write(usernameBytes);

			output.write(passLength);
			output.write(passwordBytes);

			socketReadValue = read(input);
			if (socketReadValue != 1) {
				throw new IOException("Invalid proxy response");
			}

			socketReadValue = read(input);
			if (socketReadValue != 0) {
				throw new IOException("Authentication failed");
			}

		} else if (socketReadValue != 0) {
			throw new IOException("Proxy requires authentication");
		}

		output.write(new byte[] { SOCKS_VERSION, SOCKS_METHOD_CONNECT, RESERVED, SOCKS_ADDRESS_TYPE_DOMAIN });

		byte[] domain = host.getBytes(CHARSET);
		if (domain.length > MAX_CHAR_COUNT_ALLOWED) {
			throw new IOException("Domain name should not exceed " + MAX_CHAR_COUNT_ALLOWED + " chars");
		}
		output.write(domain.length);
		output.write(domain);

		output.write(port >> 8);
		output.write(port);

		socketReadValue = read(input);
		if (socketReadValue != 5) {
			throw new IOException("Invalid proxy response");
		}

		socketReadValue = read(input);
		switch (socketReadValue) {
		case 0:
			break;
		case 1:
			throw new IOException("General failure");
		case 2:
			throw new IOException("Connection not allowed by ruleset");
		case 3:
			throw new IOException("Network not reachable");
		case 4:
			throw new IOException("Host not reachable");
		case 5:
			throw new IOException("Connection refused by host");
		case 6:
			throw new IOException("TTL expired");
		case 7:
			throw new IOException("Command not supported / protocol error");
		case 8:
			throw new IOException("Address type not supported");
		default:
			throw new IOException("Invalid proxy response");
		}

		input.skip(1L);

		socketReadValue = read(input);
		if (socketReadValue == 1) {
			input.skip(4L);
		} else if (socketReadValue == 3) {
			socketReadValue = read(input);
			input.skip(socketReadValue);
		} else if (socketReadValue == 4) {
			input.skip(16L);
		} else {
			throw new IOException("Invalid proxy response");
		}

		input.skip(2L);

		BufferedReader reader = new BufferedReader(new InputStreamReader(input, getCharset()));
		return reader;
	}

	private int read(InputStream in) throws IOException {
		int read = in.read();
		if (read < 0) {
			throw new IOException("Connection closed by the proxy");
		}
		return read;
	}
}
