package org.apache.commons.net;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/***
 * The DatagramSocketClient provides the basic operations that are required
 * of client objects accessing datagram sockets.  It is meant to be
 * subclassed to avoid having to rewrite the same code over and over again
 * to open a socket, close a socket, set timeouts, etc.  Of special note
 * is the <a href="#setDatagramSocketFactory"> setDatagramSocketFactory </a> 
 * method, which allows you to control the type of DatagramSocket the
 * DatagramSocketClient creates for network communications.  This is
 * especially useful for adding things like proxy support as well as better
 * support for applets.  For
 * example, you could create a
 * <a href="org.apache.commons.net.DatagramSocketFactory.html">
 * DatagramSocketFactory </a> that
 * requests browser security capabilities before creating a socket.
 * All classes derived from DatagramSocketClient should use the
 * <a href="#_socketFactory_"> _socketFactory_ </a> member variable to
 * create DatagramSocket instances rather than instantiating
 * them by directly invoking a constructor.  By honoring this contract
 * you guarantee that a user will always be able to provide his own
 * Socket implementations by substituting his own SocketFactory.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see DatagramSocketFactory
 ***/

public abstract class DatagramSocketClient
{
    /***
     * The default DatagramSocketFactory shared by all DatagramSocketClient
     * instances.
     ***/
    private static final DatagramSocketFactory __DEFAULT_SOCKET_FACTORY =
        new DefaultDatagramSocketFactory();

    /*** The timeout to use after opening a socket. ***/
    protected int _timeout_;

    /*** The datagram socket used for the connection. ***/
    protected DatagramSocket _socket_;

    /***
     * A status variable indicating if the client's socket is currently open.
     ***/
    protected boolean _isOpen_;

    /*** The datagram socket's DatagramSocketFactory. ***/
    protected DatagramSocketFactory _socketFactory_;

    /***
     * Default constructor for DatagramSocketClient.  Initializes
     * _socket_ to null, _timeout_ to 0, and _isOpen_ to false.
     ***/
    public DatagramSocketClient()
    {
        _socket_ = null;
        _timeout_ = 0;
        _isOpen_ = false;
        _socketFactory_ = __DEFAULT_SOCKET_FACTORY;
    }


    /***
     * Opens a DatagramSocket on the local host at the first available port.
     * Also sets the timeout on the socket to the default timeout set
     * by <a href="#setDefaultTimeout"> setDefaultTimeout() </a>.
     * <p>
     * _isOpen_ is set to true after calling this method and _socket_
     * is set to the newly opened socket.
     * <p>
     * @exception SocketException If the socket could not be opened or the
     *   timeout could not be set.
     ***/
    public void open() throws SocketException
    {
        _socket_ = _socketFactory_.createDatagramSocket();
        _socket_.setSoTimeout(_timeout_);
        _isOpen_ = true;
    }


    /***
     * Opens a DatagramSocket on the local host at a specified port.
     * Also sets the timeout on the socket to the default timeout set
     * by <a href="#setDefaultTimeout"> setDefaultTimeout() </a>.
     * <p>
     * _isOpen_ is set to true after calling this method and _socket_
     * is set to the newly opened socket.
     * <p>
     * @param port The port to use for the socket.
     * @exception SocketException If the socket could not be opened or the
     *   timeout could not be set.
     ***/
    public void open(int port) throws SocketException
    {
        _socket_ = _socketFactory_.createDatagramSocket(port);
        _socket_.setSoTimeout(_timeout_);
        _isOpen_ = true;
    }


    /***
     * Opens a DatagramSocket at the specified address on the local host
     * at a specified port.
     * Also sets the timeout on the socket to the default timeout set
     * by <a href="#setDefaultTimeout"> setDefaultTimeout() </a>.
     * <p>
     * _isOpen_ is set to true after calling this method and _socket_
     * is set to the newly opened socket.
     * <p>
     * @param port The port to use for the socket.
     * @param laddr  The local address to use.
     * @exception SocketException If the socket could not be opened or the
     *   timeout could not be set.
     ***/
    public void open(int port, InetAddress laddr) throws SocketException
    {
        _socket_ = _socketFactory_.createDatagramSocket(port, laddr);
        _socket_.setSoTimeout(_timeout_);
        _isOpen_ = true;
    }



    /***
     * Closes the DatagramSocket used for the connection.
     * You should call this method after you've finished using the class
     * instance and also before you call <a href="#open">open() </a>
     * again.   _isOpen_ is set to false and  _socket_ is set to null.
     * If you call this method when the client socket is not open,
     * a NullPointerException is thrown.
     ***/
    public void close()
    {
        _socket_.close();
        _socket_ = null;
        _isOpen_ = false;
    }


    /***
     * Returns true if the client has a currently open socket.
     * <p>
     * @return True if the client has a curerntly open socket, false otherwise.
     ***/
    public boolean isOpen()
    {
        return _isOpen_;
    }


    /***
     * Set the default timeout in milliseconds to use when opening a socket.
     * After a call to open, the timeout for the socket is set using this value.
     * This method should be used prior to a call to <a href="#open">open()</a>
     * and should not be confused with <a href="#setSoTimeout">setSoTimeout()</a>
     * which operates on the currently open socket.  _timeout_ contains
     * the new timeout value.
     * <p>
     * @param timeout  The timeout in milliseconds to use for the datagram socket
     *                 connection.
     ***/
    public void setDefaultTimeout(int timeout)
    {
        _timeout_ = timeout;
    }


    /***
     * Returns the default timeout in milliseconds that is used when
     * opening a socket.
     * <p>
     * @return The default timeout in milliseconds that is used when
     *         opening a socket.
     ***/
    public int getDefaultTimeout()
    {
        return _timeout_;
    }


    /***
     * Set the timeout in milliseconds of a currently open connection.
     * Only call this method after a connection has been opened
     * by <a href="#open">open()</a>.
     * <p>
     * @param timeout  The timeout in milliseconds to use for the currently
     *                 open datagram socket connection.
     ***/
    public void setSoTimeout(int timeout) throws SocketException
    {
        _socket_.setSoTimeout(timeout);
    }


    /***
     * Returns the timeout in milliseconds of the currently opened socket.
     * If you call this method when the client socket is not open,
     * a NullPointerException is thrown.
     * <p>
     * @return The timeout in milliseconds of the currently opened socket.
     ***/
    public int getSoTimeout() throws SocketException
    {
        return _socket_.getSoTimeout();
    }


    /***
     * Returns the port number of the open socket on the local host used
     * for the connection.  If you call this method when the client socket
     * is not open, a NullPointerException is thrown.
     * <p>
     * @return The port number of the open socket on the local host used
     *         for the connection.
     ***/
    public int getLocalPort()
    {
        return _socket_.getLocalPort();
    }


    /***
     * Returns the local address to which the client's socket is bound.
     * If you call this method when the client socket is not open, a
     * NullPointerException is thrown.
     * <p>
     * @return The local address to which the client's socket is bound.
     ***/
    public InetAddress getLocalAddress()
    {
        return _socket_.getLocalAddress();
    }


    /***
     * Sets the DatagramSocketFactory used by the DatagramSocketClient
     * to open DatagramSockets.  If the factory value is null, then a default
     * factory is used (only do this to reset the factory after having
     * previously altered it).
     * <p>
     * @param factory  The new DatagramSocketFactory the DatagramSocketClient
     * should use.
     ***/
    public void setDatagramSocketFactory(DatagramSocketFactory factory)
    {
        if (factory == null)
            _socketFactory_ = __DEFAULT_SOCKET_FACTORY;
        else
            _socketFactory_ = factory;
    }
}
