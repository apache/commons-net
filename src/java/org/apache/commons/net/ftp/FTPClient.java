package org.apache.commons.net.ftp;

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import org.apache.commons.net.io.FromNetASCIIInputStream;
import org.apache.commons.net.io.ToNetASCIIOutputStream;
import org.apache.commons.net.io.Util;
import org.apache.commons.net.MalformedServerReplyException;

/***
 * FTPClient encapsulates all the functionality necessary to store and
 * retrieve files from an FTP server.  This class takes care of all
 * low level details of interacting with an FTP server and provides
 * a convenient higher level interface.  As with all classes derived
 * from <a href="org.apache.commons.net.SocketClient.html"> SocketClient </a>,
 * you must first connect to the server with 
 * <a href="org.apache.commons.net.SocketClient.html#connect"> connect </a>
 * before doing anything, and finally
 * <a href="org.apache.commons.net.SocketClient.html#disconnect"> disconnect </a>
 * after you're completely finished interacting with the server.
 * Then you need to check the FTP reply code to see if the connection
 * was successful.  For example:
 * <pre>
 *    try {
 *      int reply;
 *      ftp.connect("ftp.foobar.com");
 *      System.out.println("Connected to " + server + ".");
 *      System.out.print(ftp.getReplyString());
 *
 *      // After connection attempt, you should check the reply code to verify
 *      // success.
 *      reply = ftp.getReplyCode();
 *
 *      if(!FTPReply.isPositiveCompletion(reply)) {
 *        ftp.disconnect();
 *        System.err.println("FTP server refused connection.");
 *        System.exit(1);
 *      }
 *    } catch(IOException e) {
 *      if(ftp.isConnected()) {
 *        try {
 *          ftp.disconnect();
 *        } catch(IOException f) {
 *          // do nothing
 *        }
 *      }
 *      System.err.println("Could not connect to server.");
 *      e.printStackTrace();
 *      System.exit(1);
 *    }
 * </pre>
 * <p>
 * Immediately after connecting is the only real time you need to check the
 * reply code (because connect is of type void).  The convention for all the
 * FTP command methods in FTPClient is such that they either return a
 * boolean value or some other value.
 * The boolean methods return true on a successful completion reply from
 * the FTP server and false on a reply resulting in an error condition or
 * failure.  The methods returning a value other than boolean return a value
 * containing the higher level data produced by the FTP command, or null if a
 * reply resulted in an error condition or failure.  If you want to access
 * the exact FTP reply code causing a success or failure, you must call
 * <a href="org.apache.commons.net.ftp.FTP.html#getReplyCode"> getReplyCode </a> after
 * a success or failure.
 * <p>
 * The default settings for FTPClient are for it to use
 * <code> FTP.ASCII_FILE_TYPE </code>,
 * <code> FTP.NON_PRINT_TEXT_FORMAT </code>,
 * <code> FTP.STREAM_TRANSFER_MODE </code>, and
 * <code> FTP.FILE_STRUCTURE </code>.  The only file types directly supported
 * are <code> FTP.ASCII_FILE_TYPE </code> and
 * <code> FTP.IMAGE_FILE_TYPE </code> (which is the same as 
 * <code> FTP.BINARY_FILE_TYPE </code>).  Because there are at lest 4
 * different EBCDIC encodings, we have opted not to provide direct support
 * for EBCDIC.  To transfer EBCDIC and other unsupported file types you
 * must create your own filter InputStreams and OutputStreams and wrap
 * them around the streams returned or required by the FTPClient methods.
 * FTPClient uses the NetASCII filter streams in
 * <a href="Package-org.apache.commons.net.io.html"> org.apache.commons.net.io </a> to provide
 * transparent handling of ASCII files.  We will consider incorporating
 * EBCDIC support if there is enough demand.
 * <p>
 * <code> FTP.NON_PRINT_TEXT_FORMAT </code>,
 * <code> FTP.STREAM_TRANSFER_MODE </code>, and
 * <code> FTP.FILE_STRUCTURE </code> are the only supported formats,
 * transfer modes, and file structures.
 * <p>
 * Because the handling of sockets on different platforms can differ
 * significantly, the FTPClient automatically issues a new PORT command
 * prior to every transfer requiring that the server connect to the client's
 * data port.  This ensures identical problem-free behavior on Windows, Unix,
 * and Macintosh platforms.  Additionally, it relieves programmers from
 * having to issue the PORT command themselves and dealing with platform
 * dependent issues.
 * <p>
 * Additionally, for security purposes, all data connections to the
 * client are verified to ensure that they originated from the intended
 * party (host and port).  If a data connection is initiated by an unexpected
 * party, the command will close the socket and throw an IOException.  You
 * may disable this behavior with
 * <a href="#setRemoteVerificationEnabled">setRemoteVerificationEnabled()</a>.
 * <p>
 * You should keep in mind that the FTP server may choose to prematurely
 * close a connection if the client has been idle for longer than a
 * given time period (usually 900 seconds).  The FTPClient class will detect a
 * premature FTP server connection closing when it receives a
 * <a href="org.apache.commons.net.ftp.FTPReply.html#SERVICE_NOT_AVAILABLE">
 * FTPReply.SERVICE_NOT_AVAILABLE </a> response to a command.
 * When that occurs, the FTP class method encountering that reply will throw
 * an <a href="org.apache.commons.net.ftp.FTPConnectionClosedException.html">
 * FTPConnectionClosedException </a>.
 * <code>FTPConnectionClosedException</code>
 * is a subclass of <code> IOException </code> and therefore need not be
 * caught separately, but if you are going to catch it separately, its
 * catch block must appear before the more general <code> IOException </code>
 * catch block.  When you encounter an
 * <a href="org.apache.commons.net.ftp.FTPConnectionClosedException.html">
 * FTPConnectionClosedException </a>, you must disconnect the connection with
 * <a href="#disconnect"> disconnect() </a> to properly clean up the
 * system resources used by FTPClient.  Before disconnecting, you may check the
 * last reply code and text with
 * <a href="org.apache.commons.net.ftp.FTP.html#getReplyCode"> getReplyCode </a>,
 * <a href="org.apache.commons.net.ftp.FTP.html#getReplyString"> getReplyString </a>,
 * and
 * <a href="org.apache.commons.net.ftp.FTP.html#getReplyStrings"> getReplyStrings</a>.
 * You may avoid server disconnections while the client is idle by
 * periodicaly sending NOOP commands to the server.
 * <p>
 * Rather than list it separately for each method, we mention here that
 * every method communicating with the server and throwing an IOException
 * can also throw a 
 * <a href="org.apache.commons.net.MalformedServerReplyException.html">
 * MalformedServerReplyException </a>, which is a subclass
 * of IOException.  A MalformedServerReplyException will be thrown when
 * the reply received from the server deviates enough from the protocol 
 * specification that it cannot be interpreted in a useful manner despite
 * attempts to be as lenient as possible.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see FTP
 * @see FTPConnectionClosedException
 * @see DefaultFTPFileListParser
 * @see org.apache.commons.net.MalformedServerReplyException
 ***/

public class FTPClient extends FTP
{
    /***
     * A constant indicating the FTP session is expecting all transfers
     * to occur between the client (local) and server and that the server
     * should connect to the client's data port to initiate a data transfer.
     * This is the default data connection mode when and FTPClient instance
     * is created.
     ***/
    public static final int ACTIVE_LOCAL_DATA_CONNECTION_MODE = 0;
    /***
     * A constant indicating the FTP session is expecting all transfers
     * to occur between two remote servers and that the server
     * the client is connected to should connect to the other server's
     * data port to initiate a data transfer.
     ***/
    public static final int ACTIVE_REMOTE_DATA_CONNECTION_MODE = 1;
    /***
     * A constant indicating the FTP session is expecting all transfers
     * to occur between the client (local) and server and that the server
     * is in passive mode, requiring the client to connect to the 
     * server's data port to initiate a transfer.
     ***/
    public static final int PASSIVE_LOCAL_DATA_CONNECTION_MODE = 2;
    /***
     * A constant indicating the FTP session is expecting all transfers
     * to occur between two remote servers and that the server
     * the client is connected to is in passive mode, requiring the other
     * server to connect to the first server's data port to initiate a data
     * transfer.
     ***/
    public static final int PASSIVE_REMOTE_DATA_CONNECTION_MODE = 3;

    private int __dataConnectionMode, __dataTimeout;
    private int __passivePort;
    private String __passiveHost;
    private int __fileType, __fileFormat, __fileStructure, __fileTransferMode;
    private boolean __remoteVerificationEnabled;
    private FTPFileListParser __fileListParser;
    private long __restartOffset;

    /***
     * Default FTPClient constructor.  Creates a new FTPClient instance
     * with the data connection mode set to 
     * <code> ACTIVE_LOCAL_DATA_CONNECTION_MODE </code>, the file type
     * set to <code> FTP.ASCII_FILE_TYPE </code>, the
     * file format set to <code> FTP.NON_PRINT_TEXT_FORMAT </code>, 
     * the file structure set to <code> FTP.FILE_STRUCTURE </code>, and
     * the transfer mode set to <code> FTP.STREAM_TRANSFER_MODE </code>.
     ***/
    public FTPClient()
    {
        __initDefaults();
        __fileListParser = new DefaultFTPFileListParser();
        __dataTimeout = -1;
        __remoteVerificationEnabled = true;
    }


    private void __initDefaults()
    {
        __dataConnectionMode = ACTIVE_LOCAL_DATA_CONNECTION_MODE;
        __passiveHost = null;
        __passivePort = -1;
        __fileType = FTP.ASCII_FILE_TYPE;
        __fileStructure = FTP.FILE_STRUCTURE;
        __fileFormat = FTP.NON_PRINT_TEXT_FORMAT;
        __fileTransferMode = FTP.STREAM_TRANSFER_MODE;
    }

    private String __parsePathname(String reply)
    {
        int begin, end;

        begin = reply.indexOf('"') + 1;
        end = reply.indexOf('"', begin);

        return reply.substring(begin, end);
    }


    private void __parsePassiveModeReply(String reply)
    throws MalformedServerReplyException
    {
        int i, index, lastIndex;
        String octet1, octet2;
        StringBuffer host;

        reply = reply.substring(reply.indexOf('(') + 1,
                                reply.indexOf(')')).trim();

        host = new StringBuffer(24);
        lastIndex = 0;
        index = reply.indexOf(',');
        host.append(reply.substring(lastIndex, index));

        for (i = 0; i < 3; i++)
        {
            host.append('.');
            lastIndex = index + 1;
            index = reply.indexOf(',', lastIndex);
            host.append(reply.substring(lastIndex, index));
        }

        lastIndex = index + 1;
        index = reply.indexOf(',', lastIndex);

        octet1 = reply.substring(lastIndex, index);
        octet2 = reply.substring(index + 1);

        // index and lastIndex now used as temporaries
        try
        {
            index = Integer.parseInt(octet1);
            lastIndex = Integer.parseInt(octet2);
        }
        catch (NumberFormatException e)
        {
            throw new MalformedServerReplyException(
                "Could not parse passive host information.\nServer Reply: " + reply);
        }

        index <<= 8;
        index |= lastIndex;

        __passiveHost = host.toString();
        __passivePort = index;
    }

    private boolean __storeFile(int command, String remote, InputStream local)
    throws IOException
    {
        OutputStream output;
        Socket socket;

        if ((socket = _openDataConnection_(command, remote)) == null)
            return false;

        // TODO: Buffer size may have to be adjustable in future to tune
        // performance.
        output = new BufferedOutputStream(socket.getOutputStream(),
                                          Util.DEFAULT_COPY_BUFFER_SIZE);
        if (__fileType == ASCII_FILE_TYPE)
            output = new ToNetASCIIOutputStream(output);
        // Treat everything else as binary for now
        try
        {
            Util.copyStream(local, output);
        }
        catch (IOException e)
        {
            try
            {
                socket.close();
            }
            catch (IOException f)
            {}
            throw e;
        }
        output.close();
        socket.close();
        return completePendingCommand();
    }

    private OutputStream __storeFileStream(int command, String remote)
    throws IOException
    {
        OutputStream output;
        Socket socket;

        if ((socket = _openDataConnection_(command, remote)) == null)
            return null;

        output = socket.getOutputStream();
        if (__fileType == ASCII_FILE_TYPE) {
          // We buffer ascii transfers because the buffering has to
          // be interposed between ToNetASCIIOutputSream and the underlying
          // socket output stream.  We don't buffer binary transfers
          // because we don't want to impose a buffering policy on the
          // programmer if possible.  Programmers can decide on their
          // own if they want to wrap the SocketOutputStream we return
          // for file types other than ASCII.
          output = new BufferedOutputStream(output,
                                            Util.DEFAULT_COPY_BUFFER_SIZE);
          output = new ToNetASCIIOutputStream(output);
          
        }
        return new org.apache.commons.net.io.SocketOutputStream(socket, output);
    }


    /***
     * Establishes a data connection with the FTP server, returning
     * a Socket for the connection if successful.  If a restart
     * offset has been set with {@link #setRestartOffset(long)},
     * a REST command is issued to the server with the offset as
     * an argument before establishing the data connection.  Active
     * mode connections also cause a local PORT command to be issued.
     * <p>
     * @param command  The text representation of the FTP command to send.
     * @param args The arguments to the FTP command.  If this parameter is
     *             set to null, then the command is sent with no argument.
     * @return A Socket corresponding to the established data connection.
     *         Null is returned if an FTP protocol error is reported at
     *         any point during the establishment and initialization of
     *         the connection.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    protected Socket _openDataConnection_(int command, String arg)
      throws IOException
    {
        Socket socket;

        if (__dataConnectionMode != ACTIVE_LOCAL_DATA_CONNECTION_MODE &&
                __dataConnectionMode != PASSIVE_LOCAL_DATA_CONNECTION_MODE)
            return null;

        if (__dataConnectionMode == ACTIVE_LOCAL_DATA_CONNECTION_MODE)
        {
            ServerSocket server;
            server = _socketFactory_.createServerSocket(0, 1, getLocalAddress());

            if (!FTPReply.isPositiveCompletion(port(getLocalAddress(),
                                                    server.getLocalPort())))
            {
                server.close();
                return null;
            }

            if ((__restartOffset > 0) && !restart(__restartOffset))
            {
                server.close();
                return null;
            }

            if (!FTPReply.isPositivePreliminary(sendCommand(command, arg)))
            {
                server.close();
                return null;
            }

            // For now, let's just use the data timeout value for waiting for
            // the data connection.  It may be desirable to let this be a
            // separately configurable value.  In any case, we really want
            // to allow preventing the accept from blocking indefinitely.
            if (__dataTimeout >= 0)
                server.setSoTimeout(__dataTimeout);
            socket = server.accept();
            server.close();
        }
        else
        { // We must be in PASSIVE_LOCAL_DATA_CONNECTION_MODE

            if (pasv() != FTPReply.ENTERING_PASSIVE_MODE)
                return null;

            __parsePassiveModeReply((String)_replyLines.elementAt(0));

            socket = _socketFactory_.createSocket(__passiveHost, __passivePort);
            if ((__restartOffset > 0) && !restart(__restartOffset))
            {
                socket.close();
                return null;
            }

            if (!FTPReply.isPositivePreliminary(sendCommand(command, arg)))
            {
                socket.close();
                return null;
            }
        }

        if (__remoteVerificationEnabled && !verifyRemote(socket))
        {
            InetAddress host1, host2;

            host1 = socket.getInetAddress();
            host2 = getRemoteAddress();

            socket.close();

            throw new IOException(
                "Host attempting data connection " + host1.getHostAddress() +
                " is not same as server " + host2.getHostAddress());
        }

        if (__dataTimeout >= 0)
            socket.setSoTimeout(__dataTimeout);

        return socket;
    }


    protected void _connectAction_() throws IOException
    {
        super._connectAction_();
        __initDefaults();
    }


    /***
     * Sets the timeout in milliseconds to use when reading from the
     * data connection.  This timeout will be set immediately after
     * opening the data connection.
     * <p>
     * @param  timeout The default timeout in milliseconds that is used when
     *        opening a data connection socket.
     ***/
    public void setDataTimeout(int timeout)
    {
        __dataTimeout = timeout;
    }


    /***
     * Closes the connection to the FTP server and restores
     * connection parameters to the default values.
     * <p>
     * @exception IOException If an error occurs while disconnecting.
     ***/
    public void disconnect() throws IOException
    {
        super.disconnect();
        __initDefaults();
    }


    /***
     * Enable or disable verification that the remote host taking part
     * of a data connection is the same as the host to which the control
     * connection is attached.  The default is for verification to be
     * enabled.  You may set this value at any time, whether the
     * FTPClient is currently connected or not.
     * <p>
     * @param enable True to enable verification, false to disable verification.
     ***/
    public void setRemoteVerificationEnabled(boolean enable)
    {
        __remoteVerificationEnabled = enable;
    }

    /***
     * Return whether or not verification of the remote host participating
     * in data connections is enabled.  The default behavior is for
     * verification to be enabled.
     * <p>
     * @return True if verification is enabled, false if not.
     ***/
    public boolean isRemoteVerificationEnabled()
    {
        return __remoteVerificationEnabled;
    }

    /***
     * Login to the FTP server using the provided username and password.
     * <p>
     * @param username The username to login under.
     * @param password The password to use.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean login(String username, String password) throws IOException
    {
        user(username);

        if (FTPReply.isPositiveCompletion(_replyCode))
            return true;

        // If we get here, we either have an error code, or an intermmediate
        // reply requesting password.
        if (!FTPReply.isPositiveIntermediate(_replyCode))
            return false;

        return FTPReply.isPositiveCompletion(pass(password));
    }


    /***
     * Login to the FTP server using the provided username, password,
     * and account.  If no account is required by the server, only
     * the username and password, the account information is not used.
     * <p>
     * @param username The username to login under.
     * @param password The password to use.
     * @param account  The account to use.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean login(String username, String password, String account)
    throws IOException
    {
        user(username);

        if (FTPReply.isPositiveCompletion(_replyCode))
            return true;

        // If we get here, we either have an error code, or an intermmediate
        // reply requesting password.
        if (!FTPReply.isPositiveIntermediate(_replyCode))
            return false;

        pass(password);

        if (FTPReply.isPositiveCompletion(_replyCode))
            return true;

        if (!FTPReply.isPositiveIntermediate(_replyCode))
            return false;

        return FTPReply.isPositiveCompletion(acct(account));
    }

    /***
     * Logout of the FTP server by sending the QUIT command.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean logout() throws IOException
    {
        return FTPReply.isPositiveCompletion(quit());
    }


    /***
     * Change the current working directory of the FTP session.
     * <p>
     * @param pathname  The new current working directory.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean changeWorkingDirectory(String pathname) throws IOException
    {
        return FTPReply.isPositiveCompletion(cwd(pathname));
    }


    /***
     * Change to the parent directory of the current working directory.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean changeToParentDirectory() throws IOException
    {
        return FTPReply.isPositiveCompletion(cdup());
    }


    /***
     * Issue the FTP SMNT command.
     * <p>
     * @param pathname The pathname to mount.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean structureMount(String pathname) throws IOException
    {
        return FTPReply.isPositiveCompletion(smnt(pathname));
    }

    /***
     * Reinitialize the FTP session.  Not all FTP servers support this
     * command, which issues the FTP REIN command.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    boolean reinitialize() throws IOException
    {
        rein();

        if (FTPReply.isPositiveCompletion(_replyCode) ||
                (FTPReply.isPositivePreliminary(_replyCode) &&
                 FTPReply.isPositiveCompletion(getReply())))
        {

            __initDefaults();

            return true;
        }

        return false;
    }


    /***
     * Set the current data connection mode to
     * <code>ACTIVE_LOCAL_DATA_CONNECTION_MODE</code>.  No communication
     * with the FTP server is conducted, but this causes all future data
     * transfers to require the FTP server to connect to the client's
     * data port.  Additionally, to accommodate differences between socket
     * implementations on different platforms, this method causes the
     * client to issue a PORT command before every data transfer.
     ***/
    public void enterLocalActiveMode()
    {
        __dataConnectionMode = ACTIVE_LOCAL_DATA_CONNECTION_MODE;
        __passiveHost = null;
        __passivePort = -1;
    }


    /***
     * Set the current data connection mode to 
     * <code> PASSIVE_LOCAL_DATA_CONNECTION_MODE </code>.  Use this
     * method only for data transfers between the client and server.
     * This method causes a PASV command to be issued to the server
     * before the opening of every data connection, telling the server to
     * open a data port to which the client will connect to conduct
     * data transfers.  The FTPClient will stay in
     * <code> PASSIVE_LOCAL_DATA_CONNECTION_MODE </code> until the
     * mode is changed by calling some other method such as
     * <a href="#enterLocalActiveMode"> enterLocalActiveMode() </a>
     ***/
    public void enterLocalPassiveMode()
    {
        __dataConnectionMode = PASSIVE_LOCAL_DATA_CONNECTION_MODE;
        // These will be set when just before a data connection is opened
        // in _openDataConnection_()
        __passiveHost = null;
        __passivePort = -1;
    }


    /***
     * Set the current data connection mode to
     * <code> ACTIVE_REMOTE_DATA_CONNECTION </code>.  Use this method only
     * for server to server data transfers.  This method issues a PORT
     * command to the server, indicating the other server and port to which
     * it should connect for data transfers.  You must call this method
     * before EVERY server to server transfer attempt.  The FTPClient will
     * NOT automatically continue to issue PORT commands.  You also
     * must remember to call
     * <a href="#enterLocalActiveMode"> enterLocalActiveMode() </a> if you
     * wish to return to the normal data connection mode.
     * <p>
     * @param host The passive mode server accepting connections for data
     *             transfers.
     * @param port The passive mode server's data port.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean enterRemoteActiveMode(InetAddress host, int port)
    throws IOException
    {
        if (FTPReply.isPositiveCompletion(port(host, port)))
        {
            __dataConnectionMode = ACTIVE_REMOTE_DATA_CONNECTION_MODE;
            __passiveHost = null;
            __passivePort = -1;
            return true;
        }
        return false;
    }

    /***
     * Set the current data connection mode to 
     * <code> PASSIVE_REMOTE_DATA_CONNECTION_MODE </code>.  Use this
     * method only for server to server data transfers.
     * This method issues a PASV command to the server, telling it to
     * open a data port to which the active server will connect to conduct
     * data transfers.  You must call this method
     * before EVERY server to server transfer attempt.  The FTPClient will
     * NOT automatically continue to issue PASV commands.  You also
     * must remember to call
     * <a href="#enterLocalActiveMode"> enterLocalActiveMode() </a> if you
     * wish to return to the normal data connection mode.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean enterRemotePassiveMode() throws IOException
    {
        if (pasv() != FTPReply.ENTERING_PASSIVE_MODE)
            return false;

        __dataConnectionMode = PASSIVE_REMOTE_DATA_CONNECTION_MODE;
        __parsePassiveModeReply((String)_replyLines.elementAt(0));

        return true;
    }

    /***
     * Returns the hostname or IP address (in the form of a string) returned
     * by the server when entering passive mode.  If not in passive mode,
     * returns null.  This method only returns a valid value AFTER a
     * data connection has been opened after a call to
     * <a href="#enterLocalPassiveMode">enterLocalPassiveMode()</a>.
     * This is because FTPClient sends a PASV command to the server only
     * just before opening a data connection, and not when you call
     * <a href="#enterLocalPassiveMode">enterLocalPassiveMode()</a>.
     * <p>
     * @return The passive host name if in passive mode, otherwise null.
     ***/
    public String getPassiveHost()
    {
        return __passiveHost;
    }

    /***
     * If in passive mode, returns the data port of the passive host.
     * This method only returns a valid value AFTER a
     * data connection has been opened after a call to
     * <a href="#enterLocalPassiveMode">enterLocalPassiveMode()</a>.
     * This is because FTPClient sends a PASV command to the server only
     * just before opening a data connection, and not when you call
     * <a href="#enterLocalPassiveMode">enterLocalPassiveMode()</a>.
     * <p>
     * @return The data port of the passive server.  If not in passive
     *         mode, undefined.
     ***/
    public int getPassivePort()
    {
        return __passivePort;
    }


    /***
     * Returns the current data connection mode (one of the
     * <code> _DATA_CONNECTION_MODE </code> constants.
     * <p>
     * @return The current data connection mode (one of the
     * <code> _DATA_CONNECTION_MODE </code> constants.
     ***/
    public int getDataConnectionMode()
    {
        return __dataConnectionMode;
    }


    /***
     * Sets the file type to be transferred.  This should be one of 
     * <code> FTP.ASCII_FILE_TYPE </code>, <code> FTP.IMAGE_FILE_TYPE </code>,
     * etc.  The file type only needs to be set when you want to change the
     * type.  After changing it, the new type stays in effect until you change
     * it again.  The default file type is <code> FTP.ASCII_FILE_TYPE </code>
     * if this method is never called.
     * <p>
     * @param fileType The <code> _FILE_TYPE </code> constant indcating the
     *                 type of file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean setFileType(int fileType) throws IOException
    {
        if (FTPReply.isPositiveCompletion(type(fileType)))
        {
            __fileType = fileType;
            __fileFormat = FTP.NON_PRINT_TEXT_FORMAT;
            return true;
        }
        return false;
    }


    /***
     * Sets the file type to be transferred and the format.  The type should be
     * one of  <code> FTP.ASCII_FILE_TYPE </code>,
     * <code> FTP.IMAGE_FILE_TYPE </code>, etc.  The file type only needs to
     * be set when you want to change the type.  After changing it, the new
     * type stays in effect until you change it again.  The default file type
     * is <code> FTP.ASCII_FILE_TYPE </code> if this method is never called.
     * The format should be one of the FTP class <code> TEXT_FORMAT </code>
     * constants, or if the type is <code> FTP.LOCAL_FILE_TYPE </code>, the
     * format should be the byte size for that type.  The default format
     * is <code> FTP.NON_PRINT_TEXT_FORMAT </code> if this method is never
     * called.
     * <p>
     * @param fileType The <code> _FILE_TYPE </code> constant indcating the
     *                 type of file.
     * @param formatOrByteSize  The format of the file (one of the
     *              <code>_FORMAT</code> constants.  In the case of
     *              <code>LOCAL_FILE_TYPE</code>, the byte size.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean setFileType(int fileType, int formatOrByteSize)
    throws IOException
    {
        if (FTPReply.isPositiveCompletion(type(fileType, formatOrByteSize)))
        {
            __fileType = fileType;
            __fileFormat = formatOrByteSize;
            return true;
        }
        return false;
    }


    /***
     * Sets the file structure.  The default structure is
     * <code> FTP.FILE_STRUCTURE </code> if this method is never called.
     * <p>
     * @param structure  The structure of the file (one of the FTP class
     *         <code>_STRUCTURE</code> constants).
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean setFileStructure(int structure) throws IOException
    {
        if (FTPReply.isPositiveCompletion(stru(structure)))
        {
            __fileStructure = structure;
            return true;
        }
        return false;
    }


    /***
     * Sets the transfer mode.  The default transfer mode
     * <code> FTP.STREAM_TRANSFER_MODE </code> if this method is never called.
     * <p>
     * @param mode  The new transfer mode to use (one of the FTP class
     *         <code>_TRANSFER_MODE</code> constants).
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean setFileTransferMode(int mode) throws IOException
    {
        if (FTPReply.isPositiveCompletion(mode(mode)))
        {
            __fileTransferMode = mode;
            return true;
        }
        return false;
    }


    /***
     * Initiate a server to server file transfer.  This method tells the
     * server to which the client is connected to retrieve a given file from
     * the other server.
     * <p>
     * @param filename  The name of the file to retrieve.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean remoteRetrieve(String filename) throws IOException
    {
        if (__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE ||
                __dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)
            return FTPReply.isPositivePreliminary(retr(filename));
        return false;
    }


    /***
     * Initiate a server to server file transfer.  This method tells the
     * server to which the client is connected to store a file on
     * the other server using the given filename.  The other server must
     * have had a <code> remoteRetrieve </code> issued to it by another
     * FTPClient.
     * <p>
     * @param filename  The name to call the file that is to be stored.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean remoteStore(String filename) throws IOException
    {
        if (__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE ||
                __dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)
            return FTPReply.isPositivePreliminary(stor(filename));
        return false;
    }


    /***
     * Initiate a server to server file transfer.  This method tells the
     * server to which the client is connected to store a file on
     * the other server using a unique filename based on the given filename.
     * The other server must have had a <code> remoteRetrieve </code> issued
     * to it by another FTPClient.
     * <p>
     * @param filename  The name on which to base the filename of the file
     *                  that is to be stored.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean remoteStoreUnique(String filename) throws IOException
    {
        if (__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE ||
                __dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)
            return FTPReply.isPositivePreliminary(stou(filename));
        return false;
    }


    /***
     * Initiate a server to server file transfer.  This method tells the
     * server to which the client is connected to store a file on
     * the other server using a unique filename.
     * The other server must have had a <code> remoteRetrieve </code> issued
     * to it by another FTPClient.  Many FTP servers require that a base
     * filename be given from which the unique filename can be derived.  For
     * those servers use the other version of <code> remoteStoreUnique</code>
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean remoteStoreUnique() throws IOException
    {
        if (__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE ||
                __dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)
            return FTPReply.isPositivePreliminary(stou());
        return false;
    }

    // For server to server transfers
    /***
     * Initiate a server to server file transfer.  This method tells the
     * server to which the client is connected to append to a given file on
     * the other server.  The other server must have had a
     * <code> remoteRetrieve </code> issued to it by another FTPClient.
     * <p>
     * @param filename  The name of the file to be appended to, or if the
     *        file does not exist, the name to call the file being stored.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean remoteAppend(String filename) throws IOException
    {
        if (__dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE ||
                __dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE)
            return FTPReply.isPositivePreliminary(stor(filename));
        return false;
    }

    /***
     * There are a few FTPClient methods that do not complete the
     * entire sequence of FTP commands to complete a transaction.  These
     * commands require some action by the programmer after the reception
     * of a positive intermediate command.  After the programmer's code
     * completes its actions, it must call this method to receive
     * the completion reply from the server and verify the success of the
     * entire transaction.
     * <p>
     * For example, 
     * <pre>
     * InputStream input;
     * OutputStream output;
     * input  = new FileInputStream("foobaz.txt");
     * output = ftp.storeFileStream("foobar.txt")
     * if(!FTPReply.isPositiveIntermediate(ftp.getReplyCode())) {
     *     input.close();
     *     output.close();
     *     ftp.logout();
     *     ftp.disconnect();
     *     System.err.println("File transfer failed.");
     *     System.exit(1);
     * }
     * Util.copyStream(input, output);
     * input.close();
     * output.close();
     * // Must call completePendingCommand() to finish command.
     * if(!ftp.completePendingCommand()) {
     *     ftp.logout();
     *     ftp.disconnect();
     *     System.err.println("File transfer failed.");
     *     System.exit(1);
     * }
     * </pre>
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean completePendingCommand() throws IOException
    {
        return FTPReply.isPositiveCompletion(getReply());
    }


    /***
     * Retrieves a named file from the server and writes it to the given
     * OutputStream.  This method does NOT close the given OutputStream.
     * If the current file type is ASCII, line separators in the file are
     * converted to the local representation.
     * <p>
     * @param remote  The name of the remote file.
     * @param local   The local OutputStream to which to write the file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception CopyStreamException  If an I/O error occurs while actually
     *      transferring the file.  The CopyStreamException allows you to
     *      determine the number of bytes transferred and the IOException
     *      causing the error.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean retrieveFile(String remote, OutputStream local)
    throws IOException
    {
        InputStream input;
        Socket socket;

        if ((socket = _openDataConnection_(FTPCommand.RETR, remote)) == null)
            return false;

        // TODO: Buffer size may have to be adjustable in future to tune
        // performance.
        input = new BufferedInputStream(socket.getInputStream(),
                                        Util.DEFAULT_COPY_BUFFER_SIZE);
        if (__fileType == ASCII_FILE_TYPE)
          input = new FromNetASCIIInputStream(input);
        // Treat everything else as binary for now
        try
        {
            Util.copyStream(input, local);
        }
        catch (IOException e)
        {
            try
            {
                socket.close();
            }
            catch (IOException f)
            {}
            throw e;
        }
        socket.close();
        return completePendingCommand();
    }

    /***
     * Returns an InputStream from which a named file from the server
     * can be read.  If the current file type is ASCII, the returned
     * InputStream will convert line separators in the file to
     * the local representation.  You must close the InputStream when you
     * finish reading from it.  The InputStream itself will take care of
     * closing the parent data connection socket upon being closed.  To
     * finalize the file transfer you must call
     * <a href="#completePendingCommand"> completePendingCommand </a> and
     * check its return value to verify success.
     * <p>
     * @param remote  The name of the remote file.
     * @return An InputStream from which the remote file can be read.  If
     *      the data connection cannot be opened (e.g., the file does not
     *      exist), null is returned (in which case you may check the reply
     *      code to determine the exact reason for failure).
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public InputStream retrieveFileStream(String remote) throws IOException
    {
        InputStream input;
        Socket socket;

        if ((socket = _openDataConnection_(FTPCommand.RETR, remote)) == null)
            return null;

        input = socket.getInputStream();
        if (__fileType == ASCII_FILE_TYPE) {
          // We buffer ascii transfers because the buffering has to
          // be interposed between FromNetASCIIOutputSream and the underlying
          // socket input stream.  We don't buffer binary transfers
          // because we don't want to impose a buffering policy on the
          // programmer if possible.  Programmers can decide on their
          // own if they want to wrap the SocketInputStream we return
          // for file types other than ASCII.
          input = new BufferedInputStream(input,
                                          Util.DEFAULT_COPY_BUFFER_SIZE);
          input = new FromNetASCIIInputStream(input);
        }
        return new org.apache.commons.net.io.SocketInputStream(socket, input);
    }


    /***
     * Stores a file on the server using the given name and taking input
     * from the given InputStream.  This method does NOT close the given
     * InputStream.  If the current file type is ASCII, line separators in
     * the file are transparently converted to the NETASCII format (i.e.,
     * you should not attempt to create a special InputStream to do this).
     * <p>
     * @param remote  The name to give the remote file.
     * @param local   The local InputStream from which to read the file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception CopyStreamException  If an I/O error occurs while actually
     *      transferring the file.  The CopyStreamException allows you to
     *      determine the number of bytes transferred and the IOException
     *      causing the error.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean storeFile(String remote, InputStream local)
    throws IOException
    {
        return __storeFile(FTPCommand.STOR, remote, local);
    }


    /***
     * Returns an OutputStream through which data can be written to store
     * a file on the server using the given name.  If the current file type
     * is ASCII, the returned OutputStream will convert line separators in
     * the file to the NETASCII format  (i.e., you should not attempt to
     * create a special OutputStream to do this).  You must close the
     * OutputStream when you finish writing to it.  The OutputStream itself
     * will take care of closing the parent data connection socket upon being
     * closed.  To finalize the file transfer you must call
     * <a href="#completePendingCommand"> completePendingCommand </a> and
     * check its return value to verify success.
     * <p>
     * @param remote  The name to give the remote file.
     * @return An OutputStream through which the remote file can be written.  If
     *      the data connection cannot be opened (e.g., the file does not
     *      exist), null is returned (in which case you may check the reply
     *      code to determine the exact reason for failure).
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public OutputStream storeFileStream(String remote) throws IOException
    {
        return __storeFileStream(FTPCommand.STOR, remote);
    }

    /***
     * Appends to a file on the server with the given name, taking input
     * from the given InputStream.  This method does NOT close the given
     * InputStream.  If the current file type is ASCII, line separators in
     * the file are transparently converted to the NETASCII format (i.e.,
     * you should not attempt to create a special InputStream to do this).
     * <p>
     * @param remote  The name of the remote file.
     * @param local   The local InputStream from which to read the data to
     *                be appended to the remote file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception CopyStreamException  If an I/O error occurs while actually
     *      transferring the file.  The CopyStreamException allows you to
     *      determine the number of bytes transferred and the IOException
     *      causing the error.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean appendFile(String remote, InputStream local)
    throws IOException
    {
        return __storeFile(FTPCommand.APPE, remote, local);
    }

    /***
     * Returns an OutputStream through which data can be written to append
     * to a file on the server with the given name.  If the current file type
     * is ASCII, the returned OutputStream will convert line separators in
     * the file to the NETASCII format  (i.e., you should not attempt to
     * create a special OutputStream to do this).  You must close the
     * OutputStream when you finish writing to it.  The OutputStream itself
     * will take care of closing the parent data connection socket upon being
     * closed.  To finalize the file transfer you must call
     * <a href="#completePendingCommand"> completePendingCommand </a> and
     * check its return value to verify success.
     * <p>
     * @param remote  The name of the remote file.
     * @return An OutputStream through which the remote file can be appended.
     *      If the data connection cannot be opened (e.g., the file does not
     *      exist), null is returned (in which case you may check the reply
     *      code to determine the exact reason for failure).
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public OutputStream appendFileStream(String remote) throws IOException
    {
        return __storeFileStream(FTPCommand.APPE, remote);
    }

    /***
     * Stores a file on the server using a unique name derived from the
     * given name and taking input
     * from the given InputStream.  This method does NOT close the given
     * InputStream.  If the current file type is ASCII, line separators in
     * the file are transparently converted to the NETASCII format (i.e.,
     * you should not attempt to create a special InputStream to do this).
     * <p>
     * @param remote  The name on which to base the unique name given to
     *                the remote file.
     * @param local   The local InputStream from which to read the file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception CopyStreamException  If an I/O error occurs while actually
     *      transferring the file.  The CopyStreamException allows you to
     *      determine the number of bytes transferred and the IOException
     *      causing the error.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean storeUniqueFile(String remote, InputStream local)
    throws IOException
    {
        return __storeFile(FTPCommand.STOU, remote, local);
    }


    /***
     * Returns an OutputStream through which data can be written to store
     * a file on the server using a unique name derived from the given name.
     * If the current file type
     * is ASCII, the returned OutputStream will convert line separators in
     * the file to the NETASCII format  (i.e., you should not attempt to
     * create a special OutputStream to do this).  You must close the
     * OutputStream when you finish writing to it.  The OutputStream itself
     * will take care of closing the parent data connection socket upon being
     * closed.  To finalize the file transfer you must call
     * <a href="#completePendingCommand"> completePendingCommand </a> and
     * check its return value to verify success.
     * <p>
     * @param remote  The name on which to base the unique name given to
     *                the remote file.
     * @return An OutputStream through which the remote file can be written.  If
     *      the data connection cannot be opened (e.g., the file does not
     *      exist), null is returned (in which case you may check the reply
     *      code to determine the exact reason for failure).
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public OutputStream storeUniqueFileStream(String remote) throws IOException
    {
        return __storeFileStream(FTPCommand.STOU, remote);
    }

    /***
     * Stores a file on the server using a unique name assigned by the
     * server and taking input from the given InputStream.  This method does
     * NOT close the given
     * InputStream.  If the current file type is ASCII, line separators in
     * the file are transparently converted to the NETASCII format (i.e.,
     * you should not attempt to create a special InputStream to do this).
     * <p>
     * @param remote  The name to give the remote file.
     * @param local   The local InputStream from which to read the file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception CopyStreamException  If an I/O error occurs while actually
     *      transferring the file.  The CopyStreamException allows you to
     *      determine the number of bytes transferred and the IOException
     *      causing the error.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean storeUniqueFile(InputStream local) throws IOException
    {
        return __storeFile(FTPCommand.STOU, null, local);
    }

    /***
     * Returns an OutputStream through which data can be written to store
     * a file on the server using a unique name assigned by the server.
     * If the current file type
     * is ASCII, the returned OutputStream will convert line separators in
     * the file to the NETASCII format  (i.e., you should not attempt to
     * create a special OutputStream to do this).  You must close the
     * OutputStream when you finish writing to it.  The OutputStream itself
     * will take care of closing the parent data connection socket upon being
     * closed.  To finalize the file transfer you must call
     * <a href="#completePendingCommand"> completePendingCommand </a> and
     * check its return value to verify success.
     * <p>
     * @param remote  The name to give the remote file.
     * @return An OutputStream through which the remote file can be written.  If
     *      the data connection cannot be opened (e.g., the file does not
     *      exist), null is returned (in which case you may check the reply
     *      code to determine the exact reason for failure).
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public OutputStream storeUniqueFileStream() throws IOException
    {
        return __storeFileStream(FTPCommand.STOU, null);
    }

    /***
     * Reserve a number of bytes on the server for the next file transfer.
     * <p>
     * @param bytes  The number of bytes which the server should allocate.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean allocate(int bytes) throws IOException
    {
        return FTPReply.isPositiveCompletion(allo(bytes));
    }


    /***
     * Reserve space on the server for the next file transfer.
     * <p>
     * @param bytes  The number of bytes which the server should allocate.
     * @param bytes  The size of a file record.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean allocate(int bytes, int recordSize) throws IOException
    {
        return FTPReply.isPositiveCompletion(allo(bytes, recordSize));
    }


    /***
     * Restart a <code>STREAM_TRANSFER_MODE</code> file transfer starting
     * from the given offset.  This will only work on FTP servers supporting
     * the REST comand for the stream transfer mode.  However, most FTP
     * servers support this.  Any subsequent file transfer will start
     * reading or writing the remote file from the indicated offset.
     * <p>
     * @param offset  The offset into the remote file at which to start the
     *           next file transfer.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    private boolean restart(long offset) throws IOException
    {
        __restartOffset = 0;
        return FTPReply.isPositiveIntermediate(rest(Long.toString(offset)));
    }

    /***
     * Sets the restart offset.  The restart command is sent to the server
     * only before sending the file transfer command.  When this is done,
     * the restart marker is reset to zero.
     * <p>
     * @param offset  The offset into the remote file at which to start the
     *           next file transfer.  This must be a value greater than or
     *           equal to zero.
     ***/
    public void setRestartOffset(long offset)
    {
        if (offset >= 0)
            __restartOffset = offset;
    }

    /***
     * Fetches the restart offset.
     * <p>
     * @return offset  The offset into the remote file at which to start the
     *           next file transfer.
     ***/
    public long getRestartOffset()
    {
        return __restartOffset;
    }



    /***
     * Renames a remote file.
     * <p>
     * @param from  The name of the remote file to rename.
     * @param to    The new name of the remote file.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean rename(String from, String to) throws IOException
    {
        if (!FTPReply.isPositiveIntermediate(rnfr(from)))
            return false;

        return FTPReply.isPositiveCompletion(rnto(to));
    }


    /***
     * Abort a transfer in progress.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean abort() throws IOException
    {
        return FTPReply.isPositiveCompletion(abor());
    }

    /***
     * Deletes a file on the FTP server.
     * <p>
     * @param pathname   The pathname of the file to be deleted.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean deleteFile(String pathname) throws IOException
    {
        return FTPReply.isPositiveCompletion(dele(pathname));
    }


    /***
     * Removes a directory on the FTP server (if empty).
     * <p>
     * @param pathname  The pathname of the directory to remove.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean removeDirectory(String pathname) throws IOException
    {
        return FTPReply.isPositiveCompletion(rmd(pathname));
    }


    /***
     * Creates a new subdirectory on the FTP server in the current directory
     * (if a relative pathname is given) or where specified (if an absolute
     * pathname is given).
     * <p>
     * @param pathname The pathname of the directory to create.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean makeDirectory(String pathname) throws IOException
    {
        return FTPReply.isPositiveCompletion(mkd(pathname));
    }


    /***
     * Returns the pathname of the current working directory.
     * <p>
     * @return The pathname of the current working directory.  If it cannot
     *         be obtained, returns null.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public String printWorkingDirectory() throws IOException
    {
        if (pwd() != FTPReply.PATHNAME_CREATED)
            return null;

        return __parsePathname((String)_replyLines.elementAt(0));
    }


    /***
     * Send a site specific command.
     * <p>
     * @param argument  The site specific command and arguments.
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean sendSiteCommand(String arguments) throws IOException
    {
        return FTPReply.isPositiveCompletion(site(arguments));
    }


    /***
     * Fetches the system type name from the server and returns the string.
     * <p>
     * @return The system type name obtained from the server.  null if the
     *       information could not be obtained.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *  command to the server or receiving a reply from the server.
     ***/
    public String getSystemName() throws IOException
    {
      //if (syst() == FTPReply.NAME_SYSTEM_TYPE)
      // Technically, we should expect a NAME_SYSTEM_TYPE response, but
      // in practice FTP servers deviate, so we soften the condition to
      // a positive completion.
        if (FTPReply.isPositiveCompletion(syst()))
            return ((String)_replyLines.elementAt(0)).substring(4);

        return null;
    }


    /***
     * Fetches the system help information from the server and returns the
     * full string.
     * <p>
     * @return The system help string obtained from the server.  null if the
     *       information could not be obtained.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *  command to the server or receiving a reply from the server.
     ***/
    public String listHelp() throws IOException
    {
        if (FTPReply.isPositiveCompletion(help()))
            return getReplyString();
        return null;
    }


    /***
     * Fetches the help information for a given command from the server and
     * returns the full string.
     * <p>
     * @param  The command on which to ask for help.
     * @return The command help string obtained from the server.  null if the
     *       information could not be obtained.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *  command to the server or receiving a reply from the server.
     ***/
    public String listHelp(String command) throws IOException
    {
        if (FTPReply.isPositiveCompletion(help(command)))
            return getReplyString();
        return null;
    }


    /***
     * Sends a NOOP command to the FTP server.  This is useful for preventing
     * server timeouts.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean sendNoOp() throws IOException
    {
        return FTPReply.isPositiveCompletion(noop());
    }


    /***
     * Obtain a list of filenames in a directory (or just the name of a given
     * file, which is not particularly useful).  This information is obtained
     * through the NLST command.  If the given pathname is a directory and
     * contains no files,  a zero length array is returned only
     * if the FTP server returned a positive completion code, otherwise
     * null is returned (the FTP server returned a 550 error No files found.).
     * If the directory is not empty, an array of filenames in the directory is
     * returned. If the pathname corresponds
     * to a file, only that file will be listed.  The server may or may not
     * expand glob expressions.
     * <p>
     * @param pathname  The file or directory to list.
     * @return The list of filenames contained in the given path.  null if
     *     the list could not be obtained.  If there are no filenames in
     *     the directory, a zero-length array is returned.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public String[] listNames(String pathname) throws IOException
    {
        String line;
        Socket socket;
        BufferedReader reader;
        Vector results;

        if ((socket = _openDataConnection_(FTPCommand.NLST, pathname)) == null)
            return null;

        reader =
            new BufferedReader(new InputStreamReader(socket.getInputStream()));

        results = new Vector();
        while ((line = reader.readLine()) != null)
            results.addElement(line);
        reader.close();
        socket.close();

        if (completePendingCommand())
        {
            String[] result;
            result = new String[results.size()];
            results.copyInto(result);
            return result;
        }

        return null;
    }


    /***
     * Obtain a list of filenames in the current working directory
     * This information is obtained through the NLST command.  If the current
     * directory contains no files, a zero length array is returned only
     * if the FTP server returned a positive completion code, otherwise,
     * null is returned (the FTP server returned a 550 error No files found.).
     * If the directory is not empty, an array of filenames in the directory is
     * returned.
     * <p>
     * @return The list of filenames contained in the current working
     *     directory.  null if the list could not be obtained.
     *     If there are no filenames in the directory, a zero-length array
     *     is returned.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public String[] listNames() throws IOException
    {
        return listNames(null);
    }



    /***
     * Using a programmer specified <code> FTPFileListParser </code>, obtain a 
     * list of file information for a directory or information for
     * just a single file.  This information is obtained through the LIST
     * command.  The contents of the returned array is determined by the
     * <code> FTPFileListParser </code> used.
     * The server may or may not expand glob expressions.  You should avoid
     * using glob expressions because the return format for glob listings
     * differs from server to server and will likely cause this method to fail.
     * <p>
     * @param parser The <code> FTPFileListParser </code> that should be
     *         used to parse the server file listing.   
     * @param pathname  The file or directory to list.
     * @return The list of file information contained in the given path in
     *         the format determined by the <code> parser </code> parameter.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public FTPFile[] listFiles(FTPFileListParser parser, String pathname)
    throws IOException
    {
        Socket socket;
        FTPFile[] results;

        if ((socket = _openDataConnection_(FTPCommand.LIST, pathname)) == null)
            return null;

        results = parser.parseFileList(socket.getInputStream());

        socket.close();

        completePendingCommand();

        return results;
    }


    /***
     * Using a programmer specified <code> FTPFileListParser </code>,
     * obtain a list of file information for the current working directory.
     * This information is obtained through the LIST command.
     * The contents of the array returned is determined by the
     * <code> FTPFileListParser </code> used.
     * <p>
     * @param parser The <code> FTPFileListParser </code> that should be
     *         used to parse the server file listing.   
     * @return The list of file information contained in the given path in
     *         the format determined by the <code> parser </code> parameter.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public FTPFile[] listFiles(FTPFileListParser parser) throws IOException
    {
        return listFiles(parser, null);
    }


    /***
     * Using the <code> DefaultFTPFileListParser </code>, obtain a list of
     * file information
     * for a directory or information for just a single file.  This information
     * is obtained through the LIST command.  If the given
     * pathname is a directory and contains no files, <code> null </code> is
     * returned, otherwise an array of <code> FTPFile </code> instances
     * representing the files in the directory is returned.
     * If the pathname corresponds to a file, only the information for that
     * file will be contained in the array (which will be of length 1).  The
     * server may or may not expand glob expressions.  You should avoid using
     * glob expressions because the return format for glob listings differs
     * from server to server and will likely cause this method to fail.
     * <p>
     * @param pathname  The file or directory to list.
     * @return The list of file information contained in the given path.  null
     *     if the list could not be obtained or if there are no files in
     *     the directory.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public FTPFile[] listFiles(String pathname) throws IOException
    {
        return listFiles(__fileListParser, pathname);
    }

    /***
     * Using the <code> DefaultFTPFileListParser </code>, obtain a list of 
     * file information for the current working directory.  This information
     * is obtained through the LIST command.  If the given
     * current directory contains no files null is returned, otherwise an 
     * array of <code> FTPFile </code> instances representing the files in the
     * directory is returned.
     * <p>
     * @return The list of file information contained in the current working
     *     directory.  null if the list could not be obtained or if there are
     *     no files in the directory.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public FTPFile[] listFiles() throws IOException
    {
        return listFiles(__fileListParser);
    }

    /**
     * Using a programmer specified <code> FTPFileEntryParser </code>,
     * initialize an object containing a raw file information for the
     * current working directory.  This information is obtained through
     * the LIST command.  This object is then capable of being iterated to
     * return a sequence of FTPFile objects with information filled in by the
     * <code> FTPFileEntryParser </code> used.
     * <p>
     * The server may or may not expand glob expressions.  You should avoid
     * using glob expressions because the return format for glob listings
     * differs from server to server and will likely cause this method to fail.
     * <p>
     * This method differs from using the listFiles() methods in that
     * expensive FTPFile objects are not created until needed which may be
     * an advantage on large lists.
     * 
     * @param parser The <code> FTPFileEntryParser </code> that should be
     *               used to parse each server file listing.
     * 
     * @return An iteratable object that holds the raw information and is
     *         capable of providing parsed FTPFile objects, one for each file containing
     *         information contained in the given path in the format determined by the
     *         <code> parser </code> parameter.   Null will be returned if a
     *         data connection cannot be opened.  If the current working directory
     *         contains no files, an empty array will be the return.
     * @example <pre>
     *    FTPClient f=FTPClient();
     *    f.connect(server);
     *    f.login(username, password);
     *    FTPFileList list = createFTPFileList(directory, parser);
     *    FTPFileIterator iter = list.iterator();
     * 
     *    while (iter.hasNext()) {
     *       FTPFile[] files = iter.getNext(25);  // "page size" you want
     *       //do whatever you want with these files, display them, etc.
     *       //expensive FTPFile objects not created until needed.
     *    }
     * </pre>
     * 
     * @exception FTPConnectionClosedException
     *                   If the FTP server prematurely closes the connection as a result
     *                   of the client being idle or some other reason causing the server
     *                   to send FTP reply code 421.  This exception may be caught either
     *                   as an IOException or independently as itself.
     * @exception IOException
     *                   If an I/O error occurs while either sending a
     *                   command to the server or receiving a reply from the server.
     * @see FTPFileList
     */
    public FTPFileList createFileList(FTPFileEntryParser parser)
    throws IOException
    {
        return createFileList(null, parser);
    }

    /**
     * Using a programmer specified <code> FTPFileEntryParser </code>, 
     * initialize an object containing a raw file information for a directory 
     * or information for a single file.  This information is obtained through 
     * the LIST command.  This object is then capable of being iterated to 
     * return a sequence of FTPFile objects with information filled in by the
     * <code> FTPFileEntryParser </code> used.
     * The server may or may not expand glob expressions.  You should avoid
     * using glob expressions because the return format for glob listings
     * differs from server to server and will likely cause this method to fail.
     * <p>
     * @param parser The <code> FTPFileEntryParser </code> that should be
     *         used to parse each server file listing.   
     * @param pathname  The file or directory to list.
     * @return An iteratable object that holds the raw information and is 
     * capable of providing parsed FTPFile objects, one for each file containing
     * information contained in the given path in the format determined by the 
     * <code> parser </code> parameter.  Null will be returned if a 
     * data connection cannot be opened.  If the supplied path contains
     * no files, an empty array will be the return.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @see FTPFileList
     */
    public FTPFileList createFileList(String pathname,
                                      FTPFileEntryParser parser)
    throws IOException
    {
        Socket socket;

        if ((socket = _openDataConnection_(FTPCommand.LIST, pathname)) == null)
        {
            return null;
        }

        FTPFileList list =
            FTPFileList.create(socket.getInputStream(), parser);

        socket.close();

        completePendingCommand();

        return list;
    }

    /***
     * Issue the FTP STAT command to the server.
     * <p>
     * @return The status information returned by the server.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public String getStatus() throws IOException
    {
        if (FTPReply.isPositiveCompletion(stat()))
            return getReplyString();
        return null;
    }


    /***
     * Issue the FTP STAT command to the server for a given pathname.  This
     * should produce a listing of the file or directory.
     * <p>
     * @return The status information returned by the server.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public String getStatus(String pathname) throws IOException
    {
        if (FTPReply.isPositiveCompletion(stat(pathname)))
            return getReplyString();
        return null;
    }


}
