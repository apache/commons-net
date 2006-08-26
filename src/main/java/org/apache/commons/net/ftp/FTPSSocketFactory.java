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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import org.apache.commons.net.SocketFactory;

public class FTPSSocketFactory implements SocketFactory
{
	private SSLContext context;
	
	public FTPSSocketFactory(SSLContext context)
	{
		this.context = context;
	}
	
	public Socket createSocket(String address, int port) throws UnknownHostException, IOException
	{
		return this.init(this.context.getSocketFactory().createSocket(address, port));
	}

	public Socket createSocket(InetAddress address, int port) throws IOException
	{
		return this.init(this.context.getSocketFactory().createSocket(address, port));
	}

	public Socket createSocket(String address, int port, InetAddress localAddress, int localPort) throws UnknownHostException, IOException
	{
		return this.init(this.context.getSocketFactory().createSocket(address, port, localAddress, localPort));
	}

	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException
	{
		return this.init(this.context.getSocketFactory().createSocket(address, port, localAddress, localPort));
	}
	
	public ServerSocket createServerSocket(int port) throws IOException
	{
		return this.init(this.context.getServerSocketFactory().createServerSocket(port));
	}

	public ServerSocket createServerSocket(int port, int backlog) throws IOException
	{
		return this.init(this.context.getServerSocketFactory().createServerSocket(port, backlog));
	}

	public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException
	{
		return this.init(this.context.getServerSocketFactory().createServerSocket(port, backlog, ifAddress));
	}
	
	public Socket init(Socket socket) throws IOException
	{
		((SSLSocket) socket).startHandshake();
		
		return socket;
	}
	
	public ServerSocket init(ServerSocket socket) throws IOException
	{
		((SSLServerSocket) socket).setUseClientMode(true);
		
		return socket;
	}
}
