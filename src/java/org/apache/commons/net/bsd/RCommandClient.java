package org.apache.commons.net.bsd;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.commons.net.io.SocketInputStream;

/***
 * RCommandClient is very similar to
 * <a href="org.apache.commons.net.bsd.RExecClient.html"> RExecClient </a>,
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
 * to the server, call the <a href="#rcommand"> rcommand() </a> method,
 * and then
 * fetch the connection's input, output, and optionally error streams.
 * Interaction with the remote command is controlled entirely through the
 * I/O streams.  Once you have finished processing the streams, you should
 * invoke <a href="org.apache.commons.net.bsd.RExecClient.html#disconnect"> 
 * disconnect() </a> to clean up properly.
 * <p>
 * By default the standard output and standard error streams of the
 * remote process are transmitted over the same connection, readable
 * from the input stream returned by
 * <a href="org.apache.commons.net.bsd.RExecClient.html#getInputStream">
 * getInputStream() </a>.  However, it is
 * possible to tell the rshd daemon to return the standard error
 * stream over a separate connection, readable from the input stream
 * returned by <a href="org.apache.commons.net.bsd.RExecClient.html#getErrorStream">
 * getErrorStream() </a>.  You
 * can specify that a separate connection should be created for standard
 * error by setting the boolean <code> separateErrorStream </code> 
 * parameter of <a href="#rcommand"> rcommand() </a> to <code> true </code>.
 * The standard input of the remote process can be written to through
 * the output stream returned by 
 * <a href="org.apache.commons.net.bsd.RExecClient.html#getOutputStream"> 
 * getOutputSream() </a>.
 * <p>
 * <p>
 * @author Daniel F. Savarese
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
                server = _socketFactory_.createServerSocket(localPort, 1,
                         getLocalAddress());
            }
            catch (SocketException e)
            {
                continue;
            }
            break;
        }

        if (localPort < MIN_CLIENT_PORT)
            throw new BindException("All ports in use.");

        _output_.write(Integer.toString(server.getLocalPort()).getBytes());
        _output_.write('\0');
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
     * Before returning, <a href="org.apache.commons.net.SocketClient.html#_connectAction_"> _connectAction_() </a>
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
            catch (SocketException e)
            {
                continue;
            }
            break;
        }

        if (localPort < MIN_CLIENT_PORT)
            throw new BindException("All ports in use or insufficient permssion.");

        _connectAction_();
    }



    /***
     * Opens a Socket connected to a remote host at the specified port and
     * originating from the current host at a port in a range acceptable
     * to the BSD rshell daemon.
     * Before returning, <a href="org.apache.commons.net.SocketClient.html#_connectAction_"> _connectAction_() </a>
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
    public void connect(InetAddress host, int port)
    throws SocketException, IOException
    {
        connect(host, port, InetAddress.getLocalHost());
    }


    /***
     * Opens a Socket connected to a remote host at the specified port and
     * originating from the current host at a port in a range acceptable
     * to the BSD rshell daemon.
     * Before returning, <a href="org.apache.commons.net.SocketClient.html#_connectAction_"> _connectAction_() </a>
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
    public void connect(String hostname, int port)
    throws SocketException, IOException
    {
        connect(InetAddress.getByName(hostname), port, InetAddress.getLocalHost());
    }


    /***
     * Opens a Socket connected to a remote host at the specified port and
     * originating from the specified local address using a port in a range
     * acceptable to the BSD rshell daemon.
     * Before returning, <a href="org.apache.commons.net.SocketClient.html#_connectAction_"> _connectAction_() </a>
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
     * Before returning, <a href="org.apache.commons.net.SocketClient.html#_connectAction_"> _connectAction_() </a>
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
    public void connect(InetAddress host, int port,
                        InetAddress localAddr, int localPort)
    throws SocketException, IOException, IllegalArgumentException
    {
        if (localPort < MIN_CLIENT_PORT || localPort > MAX_CLIENT_PORT)
            throw new IllegalArgumentException("Invalid port number " + localPort);
        super.connect(host, port, localAddr, localPort);
    }


    /***
     * Opens a Socket connected to a remote host at the specified port and
     * originating from the specified local address and port. The
     * local port must lie between <code> MIN_CLIENT_PORT </code> and
     * <code> MAX_CLIENT_PORT </code> or an IllegalArgumentException will
     * be thrown.
     * Before returning, <a href="org.apache.commons.net.SocketClient.html#_connectAction_"> _connectAction_() </a>
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
    public void connect(String hostname, int port,
                        InetAddress localAddr, int localPort)
    throws SocketException, IOException, IllegalArgumentException
    {
        if (localPort < MIN_CLIENT_PORT || localPort > MAX_CLIENT_PORT)
            throw new IllegalArgumentException("Invalid port number " + localPort);
        super.connect(hostname, port, localAddr, localPort);
    }


    /***
     * Remotely executes a command through the rshd daemon on the server
     * to which the RCommandClient is connected.  After calling this method,
     * you may interact with the remote process through its standard input,
     * output, and error streams.  You will typically be able to detect
     * the termination of the remote process after reaching end of file
     * on its standard output (accessible through
     * <a href="#getInputStream"> getInputStream() </a>.  Disconnecting
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
     * <a href="org.apache.commons.net.bsd.RExecClient.html#setRemoteVerificationEnabled">
     * setRemoteVerificationEnabled()</a>.
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

