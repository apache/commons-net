package org.apache.commons.net.telnet;

/* ====================================================================
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
 * <a href="org.apache.commons.net.SocketClient.html#connect">connect</a>
 * method.  Then an InputStream and OutputStream for sending and
 * receiving data over the Telnet connection can be obtained by
 * using the <a href="#getInputStream"> getInputStream() </a> and
 * <a href="#getOutputStream"> getOutputStream() </a> methods.
 * When you finish using the streams, you must call
 * <a href="#disconnect"> disconnect </a> rather than simply
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
    protected void _connectAction_() throws IOException
    {
        super._connectAction_();
        InputStream input;
        TelnetInputStream tmp;

        if (FromNetASCIIInputStream.isConversionRequired())
            input = new FromNetASCIIInputStream(_input_);
        else
            input = _input_;


        tmp = new TelnetInputStream(input, this);
        tmp._start();
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
    public void disconnect() throws IOException
    {
        __input.close();
        __output.close();
        super.disconnect();
    }

    /***
     * Returns the telnet connection output stream.  You should not close the
     * stream when you finish with it.  Rather, you should call
     * <a href="#disconnect"> disconnect </a>.
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
     * <a href="#disconnect"> disconnect </a>.
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
     * Sends an Are You There sequence and waits for the result
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
     * @throws InvalidTelnetOptionException
     * <p>
     * @param opthand - option handler to be registered.
     ***/
    public void addOptionHandler(TelnetOptionHandler opthand)
    throws InvalidTelnetOptionException
    {
        super.addOptionHandler(opthand);
    }
    /* open TelnetOptionHandler functionality (end)*/

    /***
     * Unregisters a  TelnetOptionHandler.
     * <p>
     * @throws InvalidTelnetOptionException
     * <p>
     * @param optcode - Code of the option to be unregistered.
     ***/
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
    public void registerNotifHandler(TelnetNotificationHandler  notifhand)
    {
        super.registerNotifHandler(notifhand);
    }

    /***
     * Unregisters the current notification handler.
     * <p>
     ***/
    public void unregisterNotifHandler()
    {
        super.unregisterNotifHandler();
    }
}
