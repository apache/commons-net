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
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.commons.net.io.SocketInputStream;
import org.apache.commons.net.SocketClient;
import java.io.OutputStream;

/***
 * RExecClient implements the rexec() facility that first appeared in
 * 4.2BSD Unix.  This class will probably only be of use for connecting
 * to Unix systems and only when the rexecd daemon is configured to run,
 * which is a rarity these days because of the security risks involved.
 * However, rexec() can be very useful for performing administrative tasks
 * on a network behind a firewall.
 * <p>
 * As with virtually all of the client classes in org.apache.commons.net, this
 * class derives from SocketClient, inheriting its connection methods.
 * The way to use RExecClient is to first connect
 * to the server, call the <a href="#rexec"> rexec() </a> method, and then
 * fetch the connection's input, output, and optionally error streams.
 * Interaction with the remote command is controlled entirely through the
 * I/O streams.  Once you have finished processing the streams, you should
 * invoke <a href="#disconnect"> disconnect() </a> to clean up properly.
 * <p>
 * By default the standard output and standard error streams of the
 * remote process are transmitted over the same connection, readable
 * from the input stream returned by
 * <a href="#getInputStream"> getInputStream() </a>.  However, it is
 * possible to tell the rexecd daemon to return the standard error
 * stream over a separate connection, readable from the input stream
 * returned by <a href="#getErrorStream"> getErrorStream() </a>.  You
 * can specify that a separate connection should be created for standard
 * error by setting the boolean <code> separateErrorStream </code> 
 * parameter of <a href="#rexec"> rexec() </a> to <code> true </code>.
 * The standard input of the remote process can be written to through
 * the output stream returned by 
 * <a href="#getOutputStream"> getOutputSream() </a>.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see SocketClient
 * @see RCommandClient
 * @see RLoginClient
 ***/

public class RExecClient extends SocketClient
{
    /***
     * The default rexec port.  Set to 512 in BSD Unix.
     ***/
    public static final int DEFAULT_PORT = 512;

    private boolean __remoteVerificationEnabled;

    /***
     * If a separate error stream is requested, <code>_errorStream_</code>
     * will point to an InputStream from which the standard error of the
     * remote process can be read (after a call to rexec()).  Otherwise,
     * <code> _errorStream_ </code> will be null.
     ***/
    protected InputStream _errorStream_;

    // This can be overridden in local package to implement port range
    // limitations of rcmd and rlogin
    InputStream _createErrorStream() throws IOException
    {
        ServerSocket server;
        Socket socket;

        server = _socketFactory_.createServerSocket(0, 1, getLocalAddress());

        _output_.write(Integer.toString(server.getLocalPort()).getBytes());
        _output_.write('\0');
        _output_.flush();

        socket = server.accept();
        server.close();

        if (__remoteVerificationEnabled && !verifyRemote(socket))
        {
            socket.close();
            throw new IOException(
                "Security violation: unexpected connection attempt by " +
                socket.getInetAddress().getHostAddress());
        }

        return (new SocketInputStream(socket, socket.getInputStream()));
    }


    /***
     * The default RExecClient constructor.  Initializes the
     * default port to <code> DEFAULT_PORT </code>.
     ***/
    public RExecClient()
    {
        _errorStream_ = null;
        setDefaultPort(DEFAULT_PORT);
    }


    /***
     * Returns the InputStream from which the standard outputof the remote
     * process can be read.  The input stream will only be set after a
     * successful rexec() invocation.
     * <p>
     * @return The InputStream from which the standard output of the remote
     * process can be read.
     ***/
    public InputStream getInputStream()
    {
        return _input_;
    }


    /***
     * Returns the OutputStream through which the standard input of the remote
     * process can be written.  The output stream will only be set after a
     * successful rexec() invocation.
     * <p>
     * @return The OutputStream through which the standard input of the remote
     * process can be written.
     ***/
    public OutputStream getOutputStream()
    {
        return _output_;
    }


    /***
     * Returns the InputStream from which the standard error of the remote
     * process can be read if a separate error stream is requested from
     * the server.  Otherwise, null will be returned.  The error stream
     * will only be set after a successful rexec() invocation.
     * <p>
     * @return The InputStream from which the standard error of the remote
     * process can be read if a separate error stream is requested from
     * the server.  Otherwise, null will be returned.
     ***/
    public InputStream getErrorStream()
    {
        return _errorStream_;
    }


    /***
     * Remotely executes a command through the rexecd daemon on the server
     * to which the RExecClient is connected.  After calling this method,
     * you may interact with the remote process through its standard input,
     * output, and error streams.  You will typically be able to detect
     * the termination of the remote process after reaching end of file
     * on its standard output (accessible through
     * <a href="#getInputStream"> getInputStream() </a>.    Disconnecting
     * from the server or closing the process streams before reaching
     * end of file will not necessarily terminate the remote process.
     * <p>
     * If a separate error stream is requested, the remote server will
     * connect to a local socket opened by RExecClient, providing an
     * independent stream through which standard error will be transmitted.
     * RExecClient will do a simple security check when it accepts a
     * connection for this error stream.  If the connection does not originate
     * from the remote server, an IOException will be thrown.  This serves as
     * a simple protection against possible hijacking of the error stream by
     * an attacker monitoring the rexec() negotiation.  You may disable this 
     * behavior with <a href="#setRemoteVerificationEnabled">
     * setRemoteVerificationEnabled()</a>.
     * <p>
     * @param username  The account name on the server through which to execute
     *                  the command.
     * @param password  The plain text password of the user account.
     * @param command   The command, including any arguments, to execute.
     * @param separateErrorStream True if you would like the standard error
     *        to be transmitted through a different stream than standard output.
     *        False if not.
     * @exception IOException If the rexec() attempt fails.  The exception
     *            will contain a message indicating the nature of the failure.
     ***/
    public void rexec(String username, String password,
                      String command, boolean separateErrorStream)
    throws IOException
    {
        int ch;

        if (separateErrorStream)
        {
            _errorStream_ = _createErrorStream();
        }
        else
        {
            _output_.write('\0');
        }

        _output_.write(username.getBytes());
        _output_.write('\0');
        _output_.write(password.getBytes());
        _output_.write('\0');
        _output_.write(command.getBytes());
        _output_.write('\0');
        _output_.flush();

        ch = _input_.read();
        if (ch > 0)
        {
            StringBuffer buffer = new StringBuffer();

            while ((ch = _input_.read()) != -1 && ch != '\n')
                buffer.append((char)ch);

            throw new IOException(buffer.toString());
        }
        else if (ch < 0)
        {
            throw new IOException("Server closed connection.");
        }
    }


    /***
     * Same as <code> rexec(username, password, command, false); </code>
     ***/
    public void rexec(String username, String password,
                      String command)
    throws IOException
    {
        rexec(username, password, command, false);
    }

    /***
     * Disconnects from the server, closing all associated open sockets and
     * streams.
     * <p>
     * @exception IOException If there an error occurs while disconnecting.
     ***/
    public void disconnect() throws IOException
    {
        if (_errorStream_ != null)
            _errorStream_.close();
        _errorStream_ = null;
        super.disconnect();
    }


    /***
     * Enable or disable verification that the remote host connecting to
     * create a separate error stream is the same as the host to which
     * the standard out stream is connected.  The default is for verification 
     * to be enabled.  You may set this value at any time, whether the
     * client is currently connected or not.
     * <p>
     * @param enable True to enable verification, false to disable verification.
     ***/
    public final void setRemoteVerificationEnabled(boolean enable)
    {
        __remoteVerificationEnabled = enable;
    }

    /***
     * Return whether or not verification of the remote host providing a
     * separate error stream is enabled.  The default behavior is for
     * verification to be enabled.
     * <p>
     * @return True if verification is enabled, false if not.
     ***/
    public final boolean isRemoteVerificationEnabled()
    {
        return __remoteVerificationEnabled;
    }

}

