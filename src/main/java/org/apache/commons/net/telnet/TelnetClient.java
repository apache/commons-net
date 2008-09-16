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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.io.FromNetASCIIInputStream;
import org.apache.commons.net.io.ToNetASCIIOutputStream;

/***
 * The TelnetClient class implements the simple network virtual
 * terminal (NVT) for the Telnet protocol according to RFC 854.  It
 * does not implement any of the extra Telnet options because it
 * is meant to be used within a Java program providing automated
 * access to Telnet accessible resources.
 * <p>
 * The class can be used by first connecting to a server using the
 * SocketClient
 * {@link org.apache.commons.net.SocketClient#connect connect}
 * method.  Then an InputStream and OutputStream for sending and
 * receiving data over the Telnet connection can be obtained by
 * using the {@link #getInputStream  getInputStream() } and
 * {@link #getOutputStream  getOutputStream() } methods.
 * When you finish using the streams, you must call
 * {@link #disconnect  disconnect } rather than simply
 * closing the streams.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @author Bruno D'Avanzo
 ***/

public class TelnetClient extends Telnet
{
    private InputStream __input;
    private OutputStream __output;
    protected boolean readerThread = true;

    /***
     * Default TelnetClient constructor.
     ***/
    public TelnetClient()
    {
        /* TERMINAL-TYPE option (start)*/
        super ("VT100");
        /* TERMINAL-TYPE option (end)*/
        __input = null;
        __output = null;
    }

    /* TERMINAL-TYPE option (start)*/
    public TelnetClient(String termtype)
    {
        super (termtype);
        __input = null;
        __output = null;
    }
    /* TERMINAL-TYPE option (end)*/

    void _flushOutputStream() throws IOException
    {
        _output_.flush();
    }
    void _closeOutputStream() throws IOException
    {
        _output_.close();
    }

    /***
     * Handles special connection requirements.
     * <p>
     * @exception IOException  If an error occurs during connection setup.
     ***/
    @Override
    protected void _connectAction_() throws IOException
    {
        super._connectAction_();
        InputStream input;
        TelnetInputStream tmp;

        if (FromNetASCIIInputStream.isConversionRequired())
            input = new FromNetASCIIInputStream(_input_);
        else
            input = _input_;


        tmp = new TelnetInputStream(input, this, readerThread);
        if(readerThread)
        {
            tmp._start();
        }
        // __input CANNOT refer to the TelnetInputStream.  We run into
        // blocking problems when some classes use TelnetInputStream, so
        // we wrap it with a BufferedInputStream which we know is safe.
        // This blocking behavior requires further investigation, but right
        // now it looks like classes like InputStreamReader are not implemented
        // in a safe manner.
        __input = new BufferedInputStream(tmp);
        __output = new ToNetASCIIOutputStream(new TelnetOutputStream(this));
    }

    /***
     * Disconnects the telnet session, closing the input and output streams
     * as well as the socket.  If you have references to the
     * input and output streams of the telnet connection, you should not
     * close them yourself, but rather call disconnect to properly close
     * the connection.
     ***/
    @Override
    public void disconnect() throws IOException
    {
        if (__input != null)
            __input.close();
        if (__output != null)
            __output.close();
        super.disconnect();
    }

    /***
     * Returns the telnet connection output stream.  You should not close the
     * stream when you finish with it.  Rather, you should call
     * {@link #disconnect  disconnect }.
     * <p>
     * @return The telnet connection output stream.
     ***/
    public OutputStream getOutputStream()
    {
        return __output;
    }

    /***
     * Returns the telnet connection input stream.  You should not close the
     * stream when you finish with it.  Rather, you should call
     * {@link #disconnect  disconnect }.
     * <p>
     * @return The telnet connection input stream.
     ***/
    public InputStream getInputStream()
    {
        return __input;
    }

    /***
     * Returns the state of the option on the local side.
     * <p>
     * @param option - Option to be checked.
     * <p>
     * @return The state of the option on the local side.
     ***/
    public boolean getLocalOptionState(int option)
    {
        /* BUG (option active when not already acknowledged) (start)*/
        return (_stateIsWill(option) && _requestedWill(option));
        /* BUG (option active when not already acknowledged) (end)*/
    }

    /***
     * Returns the state of the option on the remote side.
     * <p>
     * @param option - Option to be checked.
     * <p>
     * @return The state of the option on the remote side.
     ***/
    public boolean getRemoteOptionState(int option)
    {
        /* BUG (option active when not already acknowledged) (start)*/
        return (_stateIsDo(option) && _requestedDo(option));
        /* BUG (option active when not already acknowledged) (end)*/
    }
    /* open TelnetOptionHandler functionality (end)*/

    /* Code Section added for supporting AYT (start)*/

    /***
     * Sends an Are You There sequence and waits for the result.
     * <p>
     * @throws InterruptedException
     * @throws IllegalArgumentException
     * @throws IOException
     * <p>
     * @param timeout - Time to wait for a response (millis.)
     * <p>
     * @return true if AYT received a response, false otherwise
     ***/
    public boolean sendAYT(long timeout)
    throws IOException, IllegalArgumentException, InterruptedException
    {
        return (_sendAYT(timeout));
    }
    /* Code Section added for supporting AYT (start)*/

    /* open TelnetOptionHandler functionality (start)*/

    /***
     * Registers a new TelnetOptionHandler for this telnet client to use.
     * <p>
     * @param opthand - option handler to be registered.
     * <p>
     * @throws InvalidTelnetOptionException
     ***/
    @Override
    public void addOptionHandler(TelnetOptionHandler opthand)
    throws InvalidTelnetOptionException
    {
        super.addOptionHandler(opthand);
    }
    /* open TelnetOptionHandler functionality (end)*/

    /***
     * Unregisters a  TelnetOptionHandler.
     * <p>
     * @param optcode - Code of the option to be unregistered.
     * <p>
     * @throws InvalidTelnetOptionException
     ***/
    @Override
    public void deleteOptionHandler(int optcode)
    throws InvalidTelnetOptionException
    {
        super.deleteOptionHandler(optcode);
    }

    /* Code Section added for supporting spystreams (start)*/
    /***
     * Registers an OutputStream for spying what's going on in
     * the TelnetClient session.
     * <p>
     * @param spystream - OutputStream on which session activity
     * will be echoed.
     ***/
    public void registerSpyStream(OutputStream  spystream)
    {
        super._registerSpyStream(spystream);
    }

    /***
     * Stops spying this TelnetClient.
     * <p>
     ***/
    public void stopSpyStream()
    {
        super._stopSpyStream();
    }
    /* Code Section added for supporting spystreams (end)*/

    /***
     * Registers a notification handler to which will be sent
     * notifications of received telnet option negotiation commands.
     * <p>
     * @param notifhand - TelnetNotificationHandler to be registered
     ***/
    @Override
    public void registerNotifHandler(TelnetNotificationHandler  notifhand)
    {
        super.registerNotifHandler(notifhand);
    }

    /***
     * Unregisters the current notification handler.
     * <p>
     ***/
    @Override
    public void unregisterNotifHandler()
    {
        super.unregisterNotifHandler();
    }

    /***
     * Sets the status of the reader thread.
     * The reader thread status will apply to all subsequent connections
     * <p>
     * @param flag - true switches the reader thread on, false switches it off
     ***/
    public void setReaderThread(boolean flag)
    {
        readerThread = flag;
    }

    /***
     * Gets the status of the reader thread.
     * <p>
     * @return true if the reader thread is on, false otherwise
     ***/
    public boolean getReaderThread()
    {
        return (readerThread);
    }
}
