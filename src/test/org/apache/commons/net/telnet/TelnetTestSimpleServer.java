package org.apache.commons.net.telnet;

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

import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/***
 * Simple TCP server.
 * Waits for connections on a TCP port in a separate thread.
 * <p>
 * @author Bruno D'Avanzo
 ***/
public class TelnetTestSimpleServer implements Runnable
{
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    Thread listener = null;

    /***
     * test of client-driven subnegotiation.
     * <p>
     * @param port - server port on which to listen.
     ***/
    public TelnetTestSimpleServer(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);

        listener = new Thread (this);

        listener.start();
    }

    /***
     * Run for the thread. Waits for new connections
     ***/
    public void run()
    {
        boolean bError = false;
        while(!bError)
        {
            try
            {
                clientSocket = serverSocket.accept();
                synchronized (clientSocket)
                {
                    try
                    {
                        clientSocket.wait();
                    }
                    catch (Exception e)
                    {
                        System.err.println("Exception in wait, "+ e.getMessage());
                    }
                    try
                    {
                        clientSocket.close();
                    }
                    catch (Exception e)
                    {
                        System.err.println("Exception in close, "+ e.getMessage());
                    }
                }
            }
            catch (IOException e)
            {
                bError = true;
            }
        }

        try
        {
            serverSocket.close();
        }
        catch (Exception e)
        {
            System.err.println("Exception in close, "+ e.getMessage());
        }
    }


    /***
     * Disconnects the client socket
     ***/
    public void disconnect()
    {
        synchronized (clientSocket)
        {
            try
            {
                clientSocket.notify();
            }
            catch (Exception e)
            {
                System.err.println("Exception in notify, "+ e.getMessage());
            }
        }
    }

    /***
     * Stop the listener thread
     ***/
    public void stop()
    {
        listener.interrupt();
        try
        {
            serverSocket.close();
        }
        catch (Exception e)
        {
            System.err.println("Exception in close, "+ e.getMessage());
        }
    }

    /***
     * Gets the input stream for the client socket
     ***/
    public InputStream getInputStream() throws IOException
    {
        if(clientSocket != null)
        {
            return(clientSocket.getInputStream());
        }
        else
        {
            return(null);
        }
    }

    /***
     * Gets the output stream for the client socket
     ***/
    public OutputStream getOutputStream() throws IOException
    {
        if(clientSocket != null)
        {
            return(clientSocket.getOutputStream());
        }
        else
        {
            return(null);
        }
    }
}
