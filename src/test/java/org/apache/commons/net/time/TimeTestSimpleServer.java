package org.apache.commons.net.time;

/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * The TimetSimpleServer class is a simple TCP implementation of a server
 * for the Time Protocol described in RFC 868.
 * <p>
 * Listens for TCP socket connections on the time protocol port and writes
 * the local time to socket outputStream as 32-bit integer of seconds 
 * since midnight on 1 January 1900 GMT.
 * See <A HREF="ftp://ftp.rfc-editor.org/in-notes/rfc868.txt"> the spec </A> for
 * details.
 * <p>
 * Note this is for <B>debugging purposes only</B> and not meant to be run as a realiable time service.
 *
 * @author Jason Mathews, MITRE Corporation
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/24 21:51:48 $
 */
public class TimeTestSimpleServer implements Runnable
{

    /**
     * baseline time 1900-01-01T00:00:00 UTC
     */
    public static final long SECONDS_1900_TO_1970 = 2208988800L;

    /*** The default time port.  It is set to 37 according to RFC 868. ***/
    public static final int DEFAULT_PORT = 37;

    private ServerSocket server;
    private int port;
    private boolean running = false;

    /**
     * Default constructor for TimetSimpleServer.
     * Initializes port to defaul time port.
     */
    public TimeTestSimpleServer()
    {
        port = DEFAULT_PORT;
    }

    /**
     * Constructor for TimetSimpleServer given a specific port.
     */
    public TimeTestSimpleServer(int port)
    {
        this.port = port;
    }

    public void connect() throws IOException
    {
        if (server == null)
        {
            server = new ServerSocket(port);
        }
    }

    public int getPort()
    {
        return server == null ? port : server.getLocalPort();
    }

    public boolean isRunning()
    {
        return running;
    }

    /**
     * Start time service and provide time to client connections.
     * @throws IOException
     */
    public void start() throws IOException
    {
        if (server == null)
	{
            connect();
	}
	if (!running)
	{
	    running = true;
	    new Thread(this).start();
	}
    }

    public void run()
    {
        Socket socket = null;
        while (running)
        {
            try
            {
                socket = server.accept();
                DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                // add 500 ms to round off to nearest second
                int time = (int) ((System.currentTimeMillis() + 500) / 1000 + SECONDS_1900_TO_1970);
                os.writeInt(time);
                os.flush();
            } catch (IOException e)
            {
            } finally
            {
                if (socket != null)
                    try
                    {
                        socket.close();  // force closing of the socket
                    } catch (IOException e)
                    {
                        System.err.println("close socket error: " + e);
                    }
            }
        }
    }

    /**
     * Close server socket.
     */
    public void stop()
    {
        running = false;
        if (server != null)
        {
            try
            {
                server.close();  // force closing of the socket
            } catch (IOException e)
            {
                System.err.println("close socket error: " + e);
            }
            server = null;
        }
    }

    public static void main(String[] args)
    {
        TimeTestSimpleServer server = new TimeTestSimpleServer();
        try
        {
            server.start();
        } catch (IOException e)
        {
        }
    }

}
