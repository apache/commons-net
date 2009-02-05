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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

/**
 * Server socket factory for FTPS connections.
 */
public class FTPSServerSocketFactory extends ServerSocketFactory {
    
    private SSLContext context;
    
    public FTPSServerSocketFactory(SSLContext context) {
        this.context = context;
    }
    
    public ServerSocket createServerSocket(int port) throws IOException {
        return this.init(this.context.getServerSocketFactory().createServerSocket(port));
    }

    public ServerSocket createServerSocket(int port, int backlog) throws IOException {
        return this.init(this.context.getServerSocketFactory().createServerSocket(port, backlog));
    }

    public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
        return this.init(this.context.getServerSocketFactory().createServerSocket(port, backlog, ifAddress));
    }
        
    public ServerSocket init(ServerSocket socket) throws IOException {
        ((SSLServerSocket) socket).setUseClientMode(true);
        return socket;
    }
}

