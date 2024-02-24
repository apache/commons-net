/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Wrapper class for FTP data channel sockets.
 */
abstract class DelegateSocket extends Socket {

    protected final Socket delegate;

    DelegateSocket(final Socket delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public void bind(final SocketAddress bindpoint) throws IOException {
        delegate.bind(bindpoint);
    }

    @Override
    public synchronized void close() throws IOException {
        delegate.close();
    }

    @Override
    public void connect(final SocketAddress endpoint) throws IOException {
        delegate.connect(endpoint);
    }

    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        delegate.connect(endpoint, timeout);
    }

    @Override
    public SocketChannel getChannel() {
        return delegate.getChannel();
    }

    @Override
    public InetAddress getInetAddress() {
        return delegate.getInetAddress();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new InflaterInputStream(delegate.getInputStream());
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return delegate.getKeepAlive();
    }

    @Override
    public InetAddress getLocalAddress() {
        return delegate.getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        return delegate.getLocalPort();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return delegate.getLocalSocketAddress();
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return delegate.getOOBInline();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new DeflaterOutputStream(delegate.getOutputStream());
    }

    @Override
    public int getPort() {
        return delegate.getPort();
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        return delegate.getReceiveBufferSize();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return delegate.getRemoteSocketAddress();
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return delegate.getReuseAddress();
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        return delegate.getSendBufferSize();
    }

    @Override
    public int getSoLinger() throws SocketException {
        return delegate.getSoLinger();
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        return delegate.getSoTimeout();
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return delegate.getTcpNoDelay();
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return delegate.getTrafficClass();
    }

    @Override
    public boolean isBound() {
        return delegate.isBound();
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }

    @Override
    public boolean isInputShutdown() {
        return delegate.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return delegate.isOutputShutdown();
    }

    @Override
    public void sendUrgentData(final int data) throws IOException {
        delegate.sendUrgentData(data);
    }

    @Override
    public void setKeepAlive(final boolean on) throws SocketException {
        delegate.setKeepAlive(on);
    }

    @Override
    public void setOOBInline(final boolean on) throws SocketException {
        delegate.setOOBInline(on);
    }

    @Override
    public void setPerformancePreferences(final int connectionTime, final int latency, final int bandwidth) {
        delegate.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    @Override
    public synchronized void setReceiveBufferSize(final int size) throws SocketException {
        delegate.setReceiveBufferSize(size);
    }

    @Override
    public void setReuseAddress(final boolean on) throws SocketException {
        delegate.setReuseAddress(on);
    }

    @Override
    public synchronized void setSendBufferSize(final int size) throws SocketException {
        delegate.setSendBufferSize(size);
    }

    @Override
    public void setSoLinger(final boolean on, final int linger) throws SocketException {
        delegate.setSoLinger(on, linger);
    }

    @Override
    public synchronized void setSoTimeout(final int timeout) throws SocketException {
        delegate.setSoTimeout(timeout);
    }

    @Override
    public void setTcpNoDelay(final boolean on) throws SocketException {
        delegate.setTcpNoDelay(on);
    }

    @Override
    public void setTrafficClass(final int tc) throws SocketException {
        delegate.setTrafficClass(tc);
    }

    @Override
    public void shutdownInput() throws IOException {
        delegate.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
        delegate.shutdownOutput();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
