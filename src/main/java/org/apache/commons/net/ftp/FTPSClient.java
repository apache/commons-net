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


/**
 * 
 * This class extends {@link org.apache.commons.net.ftp.FTPClient} to add
 * the necessary methods that implement SSL/TLS-FTPS.
 *
 */
public class FTPSClient extends FTPClient {

	// Represent the method to the FTP command AUTH...
	private String sslContext;
	
	// Secure context (can be "TLS" or "SSL")
	private SSLContext context;
	
	private String pbsz;
	private String prot;

	/**
	 * Default constructor that selects some default options (TLS encryption)
	 *
	 */
	public FTPSClient() {
		this("JCEKS", "TLS", "password", "0", "P");
	}
	
	
	/**
	 * 
	 * Constructor that initializes the secure connection. 
	 * 
	 * @param keyStoreName Type of instance KeyStore, JKS for Java 1.3 y JCEKS for Java 1.4 
	 * @param sslContext Type of the instance SSLContext, can be SSL or TLS.
	 * @param password The password to access the KeyStore.
	 */
	public FTPSClient(String keyStoreName, String sslContext, String password, String pbsz, String prot) {
		this.sslContext = sslContext;
		this.pbsz = pbsz;
		this.prot = prot;
		
		try {
			KeyStore keyStore = KeyStore.getInstance(keyStoreName);
			
			keyStore.load(null, password.toCharArray());

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			
			keyManagerFactory.init(keyStore, password.toCharArray());

			this.context = SSLContext.getInstance(sslContext);

			this.context.init(
				keyManagerFactory.getKeyManagers(), 
				new TrustManager[] { (TrustManager) new FTPSTrustManager() }, null
			);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @see org.apache.commons.net.SocketClient#connect(java.net.InetAddress, int, java.net.InetAddress, int)
	 */
	public void connect(InetAddress address, int port, InetAddress localAddress, int localPort) throws SocketException, IOException
	{
		super.connect(address, port, localAddress, localPort);
		
		this.secure(this.pbsz,this.prot);
	}

	/**
	 * @see org.apache.commons.net.SocketClient#connect(java.net.InetAddress, int)
	 */
	public void connect(InetAddress address, int port) throws SocketException, IOException
	{
		super.connect(address, port);
		
		this.secure(this.pbsz,this.prot);
	}

	/**
	 * @see org.apache.commons.net.SocketClient#connect(java.lang.String, int, java.net.InetAddress, int)
	 */
	public void connect(String address, int port, InetAddress localAddress, int localPort) throws SocketException, IOException
	{
		super.connect(address, port, localAddress, localPort);
		
		this.secure(this.pbsz,this.prot);
	}

	/**
	 * @see org.apache.commons.net.SocketClient#connect(java.lang.String, int)
	 */
	public void connect(String address, int port) throws SocketException, IOException
	{
		super.connect(address, port);
		
		this.secure(this.pbsz,this.prot);
	}
	
	/**
	 *
	 * Initialize the secure connection with the FTP server, throw the AUTH SSL o TLS command.
	 * Get the socket with the server, starting the "handshake" making the socket, with a layer of securety,
	 * and initializing the stream of connection.
	 * 
	 * 
	 * @param pbsz Protection Buffer Size: "0" is a good value
	 * @param prot Data Channel Protection Level:
	 * Posible values:
	 * C - Clear
	 * S - Safe
	 * E - Confidential 
	 * P - PrivateType of secure connection
	 *  
	 * @throws IOException If there is any problem with the connection.
	 */
	protected void secure(String pbsz, String prot) throws IOException {
		this.sendCommand("AUTH", sslContext);
		
		SSLSocket socket = (SSLSocket)this.context.getSocketFactory().createSocket(this._socket_, this.getRemoteAddress().getHostAddress(), this.getRemotePort(), true);
		
		socket.startHandshake();

		this._socket_ = socket;
		
		this._controlInput_ = new BufferedReader(new InputStreamReader(socket.getInputStream(), getControlEncoding()));
		this._controlOutput_ = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), getControlEncoding()));

		this.setSocketFactory(new FTPSSocketFactory(this.context));

		this.sendCommand("PBSZ", pbsz);
		this.sendCommand("PROT", prot);
	}

	/**
	 * @see org.apache.commons.net.ftp.FTPCliente#_openDataConnection_(java.lang.String, int)
	 */	
	protected Socket _openDataConnection_(int command, String arg) throws IOException {
		Socket socket = super._openDataConnection_(command, arg);
		if (socket != null) {
			((SSLSocket)socket).startHandshake();
		}
		return socket;
	}	

}

