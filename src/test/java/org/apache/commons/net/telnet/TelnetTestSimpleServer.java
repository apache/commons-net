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
package org.apache.commons.net.telnet;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/***
 * Simple TCP server.
 * Waits for connections on a TCP port in a separate thread.
 ***/
public class TelnetTestSimpleServer implements Runnable
{
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    Thread listener = null;

    /*
     * test of client-driven subnegotiation.
     * <p>
     * @param port - server port on which to listen.
     * @throws IOException on error
     */
    public TelnetTestSimpleServer(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);

        listener = new Thread (this);

        listener.start();
    }

    @Override
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


    public void disconnect()
    {
        if (clientSocket == null) {
            return;
        }
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
