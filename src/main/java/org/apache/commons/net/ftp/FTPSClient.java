/*
 * Copyright 2001-2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.ftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

public class FTPSClient extends FTPClient
{
	private static final String PASSWORD = "password";
	
	private SSLContext context;
	
	public FTPSClient()
	{
		try
		{
			KeyStore keyStore = KeyStore.getInstance("JCEKS");
			keyStore.load(null, PASSWORD.toCharArray());

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, PASSWORD.toCharArray());

			this.context = SSLContext.getInstance("TLS");
			this.context.init(keyManagerFactory.getKeyManagers(), new TrustManager[] { new FTPSTrustManager() }, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see org.apache.commons.net.SocketClient#connect(java.net.InetAddress, int, java.net.InetAddress, int)
	 */
	public void connect(InetAddress address, int port, InetAddress localAddress, int localPort) throws SocketException, IOException
	{
		super.connect(address, port, localAddress, localPort);
		
		this.secure();
	}

	/**
	 * @see org.apache.commons.net.SocketClient#connect(java.net.InetAddress, int)
	 */
	public void connect(InetAddress address, int port) throws SocketException, IOException
	{
		super.connect(address, port);
		
		this.secure();
	}

	/**
	 * @see org.apache.commons.net.SocketClient#connect(java.lang.String, int, java.net.InetAddress, int)
	 */
	public void connect(String address, int port, InetAddress localAddress, int localPort) throws SocketException, IOException
	{
		super.connect(address, port, localAddress, localPort);
		
		this.secure();
	}

	/**
	 * @see org.apache.commons.net.SocketClient#connect(java.lang.String, int)
	 */
	public void connect(String address, int port) throws SocketException, IOException
	{
		super.connect(address, port);
		
		this.secure();
	}
	
	public void secure() throws IOException
	{
		this.sendCommand("AUTH", "TLS");
		
		SSLSocket socket = (SSLSocket) this.context.getSocketFactory().createSocket(this._socket_, this.getRemoteAddress().getHostAddress(), this.getRemotePort(), true);
		
		socket.startHandshake();
		
		this._socket_ = socket;
		this._controlInput_ = new BufferedReader(new InputStreamReader(socket.getInputStream(), getControlEncoding()));
		this._controlOutput_ = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), getControlEncoding()));

		this.setSocketFactory(new FTPSSocketFactory(this.context));
		
		this.sendCommand("PBSZ", "0");
		this.sendCommand("PROT", "P");
	}

	/**
	 * @see org.apache.commons.net.ftp.FTPClient#_openDataConnection_(int, java.lang.String)
	 */
	protected Socket _openDataConnection_(int command, String arg) throws IOException
	{
		Socket socket = super._openDataConnection_(command, arg);
		
		if (socket != null)
		{
			((SSLSocket) socket).startHandshake();
		}
		
		return socket;
	}
}
