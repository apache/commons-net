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

package org.apache.commons.net.bsd;

import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.net.io.SocketInputStream;

/***
 * RCommandClient is very similar to
 * {@link org.apache.commons.net.bsd.RExecClient},
 * from which it is derived, and implements the rcmd() facility that
 * first appeared in 4.2BSD Unix.  rcmd() is the facility used by the rsh
 * (rshell) and other commands to execute a command on another machine
 * from a trusted host without issuing a password.  The trust relationship
 * between two machines is established by the contents of a machine's
 * /etc/hosts.equiv file and a user's .rhosts file.  These files specify
 * from which hosts and accounts on those hosts rcmd() requests will be
 * accepted.  The only additional measure for establishing trust is that
 * all client connections must originate from a port between 512 and 1023.
 * Consequently, there is an upper limit to the number of rcmd connections
 * that can be running simultaneously.   The required ports are reserved
 * ports on Unix systems, and can only be bound by a
 * process running with root permissions (to accomplish this rsh, rlogin,
 * and related commands usualy have the suid bit set).  Therefore, on a
 * Unix system, you will only be able to successfully use the RCommandClient
 * class if the process runs as root.  However, there is no such restriction
 * on Windows95 and some other systems.  The security risks are obvious.
 * However, when carefully used, rcmd() can be very useful when used behind
 * a firewall.
 * <p>
 * As with virtually all of the client classes in org.apache.commons.net, this
 * class derives from SocketClient.  But it overrides most of its connection
 * methods so that the local Socket will originate from an acceptable
 * rshell port.  The way to use RCommandClient is to first connect
 * to the server, call the {@link #rcommand  rcommand() } method,
 * and then
 * fetch the connection's input, output, and optionally error streams.
 * Interaction with the remote command is controlled entirely through the
 * I/O streams.  Once you have finished processing the streams, you should
 * invoke {@link org.apache.commons.net.bsd.RExecClient#disconnect disconnect() }
 *  to clean up properly.
 * <p>
 * By default the standard output and standard error streams of the
 * remote process are transmitted over the same connection, readable
 * from the input stream returned by
 * {@link org.apache.commons.net.bsd.RExecClient#getInputStream getInputStream() }
 * .  However, it is
 * possible to tell the rshd daemon to return the standard error
 * stream over a separate connection, readable from the input stream
 * returned by {@link org.apache.commons.net.bsd.RExecClient#getErrorStream getErrorStream() }
 * .  You
 * can specify that a separate connection should be created for standard
 * error by setting the boolean <code> separateErrorStream </code>
 * parameter of {@link #rcommand  rcommand() } to <code> true </code>.
 * The standard input of the remote process can be written to through
 * the output stream returned by
 * {@link org.apache.commons.net.bsd.RExecClient#getOutputStream getOutputStream() }
 * .
 * <p>
 * <p>
 * @see org.apache.commons.net.SocketClient
 * @see RExecClient
 * @see RLoginClient
 ***/

public class RCommandClient extends RExecClient
{
    /***
     * The default rshell port.  Set to 514 in BSD Unix.
     ***/
    public static final int DEFAULT_PORT = 514;

    /***
     * The smallest port number an rcmd client may use.  By BSD convention
     * this number is 512.
     ***/
    public static final int MIN_CLIENT_PORT = 512;

    /***
     * The largest port number an rcmd client may use.  By BSD convention
     * this number is 1023.
     ***/
    public static final int MAX_CLIENT_PORT = 1023;

    // Overrides method in RExecClient in order to implement proper
    // port number limitations.
    @Override
    InputStream _createErrorStream() throws IOException
    {
        int localPort;
        ServerSocket server;
        Socket socket;

        localPort = MAX_CLIENT_PORT;
        server = null; // Keep compiler from barfing

        for (localPort = MAX_CLIENT_PORT; localPort >= MIN_CLIENT_PORT; --localPort)
        {
            try
            {
                server = _serverSocketFactory_.createServerSocket(localPort, 1,
                         getLocalAddress());
                break; // got a socket
            }
            catch (SocketException e)
            {
                continue;
            }
        }

        if (server == null) {
            throw new BindException("All ports in use.");
        }

        _output_.write(Integer.toString(server.getLocalPort()).getBytes("UTF-8")); // $NON-NLS
        _output_.write(NULL_CHAR);
        _output_.flush();

        socket = server.accept();
        server.close();

        if (isRemoteVerificationEnabled() && !verifyRemote(socket))
        {
            socket.close();
            throw new IOException(
                "Security violation: unexpected connection attempt by " +
                socket.getInetAddress().getHostAddress());
        }

        return (new SocketInputStream(socket, socket.getInputStream()));
    }

    /***
     * The default RCommandClient constructor.  Initializes the
     * default port to <code> DEFAULT_PORT </code>.
     ***/
    public RCommandClient()
    {
        setDefaultPort(DEFAULT_PORT);
    }


    /***
     * Opens a Socket connected to a remote host at the specified port and
     * originating from the specified local address using a port in a range
     * acceptable to the BSD rshell daemon.
     * Before returning, {@link org.apache.commons.net.SocketClient#_connectAction_  _connectAction_() }
     * is called to perform connection initialization actions.
     * <p>
     * @param host  The remote host.
     * @param port  The port to connect to on the remote host.
     * @param localAddr  The local address to use.
     * @exception SocketException If the socket timeout could not be set.
     * @exception BindException If all acceptable rshell ports are in use.
     * @exception IOException If the socket could not be opened.  In most
     *  cases you will only want to catch IOException since SocketException is
     *  derived from it.
     ***/
    public void connect(InetAddress host, int port, InetAddress localAddr)
    throws SocketException, BindException, IOException
    {
        int localPort;

        localPort = MAX_CLIENT_PORT;

        for (localPort = MAX_CLIENT_PORT; localPort >= MIN_CLIENT_PORT; --localPort)
        {
            try
            {
                _socket_ =
                    _socketFactory_.createSocket(host, port, localAddr, localPort);
            }
            catch (BindException be) {
                continue;
            }
            catch (SocketException e)
            {
                continue;
            }
            break;
        }

        if (localPort < MIN_CLIENT_PORT) {
            throw new BindException("All ports in use or insufficient permssion.");
        }

        _connectAction_();
    }



    /***
     * Opens a Socket connected to a remote host at the specified port and
     * originating from the current host at a port in a range acceptable
     * to the BSD rshell daemon.
     * Before returning, {@link org.apache.commons.net.SocketClient#_connectAction_  _connectAction_() }
     * is called to perform connection initialization actions.
     * <p>
     * @param host  The remote host.
     * @param port  The port to connect to on the remote host.
     * @exception SocketException If the socket timeout could not be set.
     * @exception BindException If all acceptable rshell ports are in use.
     * @exception IOException If the socket could not be opened.  In most
     *  cases you will only want to catch IOException since SocketException is
     *  derived from it.
     ***/
    @Override
    public void connect(InetAddress host, int port)
    throws SocketException, IOException
    {
        connect(host, port, InetAddress.getLocalHost());
    }


    /***
     * Opens a Socket connected to a remote host at the specified port and
     * originating from the current host at a port in a range acceptable
     * to the BSD rshell daemon.
     * Before returning, {@link org.apache.commons.net.SocketClient#_connectAction_  _connectAction_() }
     * is called to perform connection initialization actions.
     * <p>
     * @param hostname  The name of the remote host.
     * @param port  The port to connect to on the remote host.
     * @exception SocketException If the socket timeout could not be set.
     * @exception BindException If all acceptable rshell ports are in use.
     * @exception IOException If the socket could not be opened.  In most
     *  cases you will only want to catch IOException since SocketException is
     *  derived from it.
     * @exception UnknownHostException If the hostname cannot be resolved.
     ***/
    @Override
    public void connect(String hostname, int port)
    throws SocketException, IOException, UnknownHostException
    {
        connect(InetAddress.getByName(hostname), port, InetAddress.getLocalHost());
    }


    /***
     * Opens a Socket connected to a remote host at the specified port and
     * originating from the specified local address using a port in a range
     * acceptable to the BSD rshell daemon.
     * Before returning, {@link org.apache.commons.net.SocketClient#_connectAction_  _connectAction_() }
     * is called to perform connection initialization actions.
     * <p>
     * @param hostname  The remote host.
     * @param port  The port to connect to on the remote host.
     * @param localAddr  The local address to use.
     * @exception SocketException If the socket timeout could not be set.
     * @exception BindException If all acceptable rshell ports are in use.
     * @exception IOException If the socket could not be opened.  In most
     *  cases you will only want to catch IOException since SocketException is
     *  derived from it.
     ***/
    public void connect(String hostname, int port, InetAddress localAddr)
    throws SocketException, IOException
    {
        connect(InetAddress.getByName(hostname), port, localAddr);
    }


    /***
     * Opens a Socket connected to a remote host at the specified port and
     * originating from the specified local address and port. The
     * local port must lie between <code> MIN_CLIENT_PORT </code> and
     * <code> MAX_CLIENT_PORT </code> or an IllegalArgumentException will
     * be thrown.
     * Before returning, {@link org.apache.commons.net.SocketClient#_connectAction_  _connectAction_() }
     * is called to perform connection initialization actions.
     * <p>
     * @param host  The remote host.
     * @param port  The port to connect to on the remote host.
     * @param localAddr  The local address to use.
     * @param localPort  The local port to use.
     * @exception SocketException If the socket timeout could not be set.
     * @exception IOException If the socket could not be opened.  In most
     *  cases you will only want to catch IOException since SocketException is
     *  derived from it.
     * @exception IllegalArgumentException If an invalid local port number
     *            is specified.
     ***/
    @Override
    public void connect(InetAddress host, int port,
                        InetAddress localAddr, int localPort)
    throws SocketException, IOException, IllegalArgumentException
    {
        if (localPort < MIN_CLIENT_PORT || localPort > MAX_CLIENT_PORT) {
            throw new IllegalArgumentException("Invalid port number " + localPort);
        }
        super.connect(host, port, localAddr, localPort);
    }


    /***
     * Opens a Socket connected to a remote host at the specified port and
     * originating from the specified local address and port. The
     * local port must lie between <code> MIN_CLIENT_PORT </code> and
     * <code> MAX_CLIENT_PORT </code> or an IllegalArgumentException will
     * be thrown.
     * Before returning, {@link org.apache.commons.net.SocketClient#_connectAction_  _connectAction_() }
     * is called to perform connection initialization actions.
     * <p>
     * @param hostname  The name of the remote host.
     * @param port  The port to connect to on the remote host.
     * @param localAddr  The local address to use.
     * @param localPort  The local port to use.
     * @exception SocketException If the socket timeout could not be set.
     * @exception IOException If the socket could not be opened.  In most
     *  cases you will only want to catch IOException since SocketException is
     *  derived from it.
     * @exception UnknownHostException If the hostname cannot be resolved.
     * @exception IllegalArgumentException If an invalid local port number
     *            is specified.
     ***/
    @Override
    public void connect(String hostname, int port,
                        InetAddress localAddr, int localPort)
    throws SocketException, IOException, IllegalArgumentException, UnknownHostException
    {
        if (localPort < MIN_CLIENT_PORT || localPort > MAX_CLIENT_PORT) {
            throw new IllegalArgumentException("Invalid port number " + localPort);
        }
        super.connect(hostname, port, localAddr, localPort);
    }


    /***
     * Remotely executes a command through the rshd daemon on the server
     * to which the RCommandClient is connected.  After calling this method,
     * you may interact with the remote process through its standard input,
     * output, and error streams.  You will typically be able to detect
     * the termination of the remote process after reaching end of file
     * on its standard output (accessible through
     * {@link #getInputStream  getInputStream() }.  Disconnecting
     * from the server or closing the process streams before reaching
     * end of file will not necessarily terminate the remote process.
     * <p>
     * If a separate error stream is requested, the remote server will
     * connect to a local socket opened by RCommandClient, providing an
     * independent stream through which standard error will be transmitted.
     * The local socket must originate from a secure port (512 - 1023),
     * and rcommand() ensures that this will be so.
     * RCommandClient will also do a simple security check when it accepts a
     * connection for this error stream.  If the connection does not originate
     * from the remote server, an IOException will be thrown.  This serves as
     * a simple protection against possible hijacking of the error stream by
     * an attacker monitoring the rexec() negotiation.  You may disable this
     * behavior with
     * {@link org.apache.commons.net.bsd.RExecClient#setRemoteVerificationEnabled setRemoteVerificationEnabled()}
     * .
     * <p>
     * @param localUsername  The user account on the local machine that is
     *        requesting the command execution.
     * @param remoteUsername  The account name on the server through which to
     *        execute the command.
     * @param command   The command, including any arguments, to execute.
     * @param separateErrorStream True if you would like the standard error
     *        to be transmitted through a different stream than standard output.
     *        False if not.
     * @exception IOException If the rcommand() attempt fails.  The exception
     *            will contain a message indicating the nature of the failure.
     ***/
    public void rcommand(String localUsername, String remoteUsername,
                         String command, boolean separateErrorStream)
    throws IOException
    {
        rexec(localUsername, remoteUsername, command, separateErrorStream);
    }


    /***
     * Same as
     * <code> rcommand(localUsername, remoteUsername, command, false); </code>
     ***/
    public void rcommand(String localUsername, String remoteUsername,
                         String command)
    throws IOException
    {
        rcommand(localUsername, remoteUsername, command, false);
    }

}

