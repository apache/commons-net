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
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import org.apache.commons.net.SocketClient;

/**
 * @author Daniel F. Savarese
 * @author Bruno D'Avanzo
 */

class Telnet extends SocketClient
{
    static final boolean debug =  /*true;*/ false;

    static final boolean debugoptions =  /*true;*/ false;

    static final byte[] _COMMAND_DO = {
                                          (byte)TelnetCommand.IAC, (byte)TelnetCommand.DO
                                      };

    static final byte[] _COMMAND_DONT = {
                                            (byte)TelnetCommand.IAC, (byte)TelnetCommand.DONT
                                        };

    static final byte[] _COMMAND_WILL = {
                                            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WILL
                                        };

    static final byte[] _COMMAND_WONT = {
                                            (byte)TelnetCommand.IAC, (byte)TelnetCommand.WONT
                                        };

    static final byte[] _COMMAND_SB = {
                                          (byte)TelnetCommand.IAC, (byte)TelnetCommand.SB
                                      };

    static final byte[] _COMMAND_SE = {
                                          (byte)TelnetCommand.IAC, (byte)TelnetCommand.SE
                                      };

    static final int _WILL_MASK = 0x01, _DO_MASK = 0x02,
                                  _REQUESTED_WILL_MASK = 0x04, _REQUESTED_DO_MASK = 0x08;

    /* public */
    static final int DEFAULT_PORT =  23;

    int[] _doResponse, _willResponse, _options;

    /* TERMINAL-TYPE option (start)*/
    /***
     * Terminal type option
     ***/
    protected static final int TERMINAL_TYPE = 24;

    /***
     * Send (for subnegotiation)
     ***/
    protected static final int TERMINAL_TYPE_SEND =  1;

    /***
     * Is (for subnegotiation)
     ***/
    protected static final int TERMINAL_TYPE_IS =  0;

    /***
     * Is sequence (for subnegotiation)
     ***/
    static final byte[] _COMMAND_IS = {
                                          (byte)TERMINAL_TYPE, (byte)TERMINAL_TYPE_IS
                                      };

    /***
     * Terminal type
     ***/
    private String terminal_type = null;
    /* TERMINAL-TYPE option (end)*/

    /* open TelnetOptionHandler functionality (start)*/
    /***
     * Array of option handlers
     ***/
    private TelnetOptionHandler _option_handlers[];

    /* open TelnetOptionHandler functionality (end)*/

    /* Code Section added for supporting AYT (start)*/
    /***
     * AYT sequence
     ***/
    static final byte[] _COMMAND_AYT = {
                                          (byte)TelnetCommand.IAC, (byte)TelnetCommand.AYT
                                       };

    /***
     * monitor to wait for AYT
     ***/
    private Object _ayt_monitor = new Object();

    /***
     * flag for AYT
     ***/
    private boolean _ayt_flag = true;
    /* Code Section added for supporting AYT (end)*/

    /***
     * The stream on which to spy
     ***/
    private OutputStream _spystream = null;
    /* public */
    Telnet()
    {
        setDefaultPort(DEFAULT_PORT);
        _doResponse = new int[TelnetOption.MAX_OPTION_VALUE + 1];
        _willResponse = new int[TelnetOption.MAX_OPTION_VALUE + 1];
        _options = new int[TelnetOption.MAX_OPTION_VALUE + 1];
        _option_handlers =
            new TelnetOptionHandler[TelnetOption.MAX_OPTION_VALUE + 1];
    }

    /* TERMINAL-TYPE option (start)*/
    /* public */
    Telnet(String termtype)
    {
        setDefaultPort(DEFAULT_PORT);
        _doResponse = new int[TelnetOption.MAX_OPTION_VALUE + 1];
        _willResponse = new int[TelnetOption.MAX_OPTION_VALUE + 1];
        _options = new int[TelnetOption.MAX_OPTION_VALUE + 1];
        terminal_type = termtype;
        _option_handlers =
            new TelnetOptionHandler[TelnetOption.MAX_OPTION_VALUE + 1];
    }
    /* TERMINAL-TYPE option (end)*/

    boolean _stateIsWill(int option)
    {
        return ((_options[option] & _WILL_MASK) != 0);
    }

    boolean _stateIsWont(int option)
    {
        return !_stateIsWill(option);
    }

    boolean _stateIsDo(int option)
    {
        return ((_options[option] & _DO_MASK) != 0);
    }

    boolean _stateIsDont(int option)
    {
        return !_stateIsDo(option);
    }

    boolean _requestedWill(int option)
    {
        return ((_options[option] & _REQUESTED_WILL_MASK) != 0);
    }

    boolean _requestedWont(int option)
    {
        return !_requestedWill(option);
    }

    boolean _requestedDo(int option)
    {
        return ((_options[option] & _REQUESTED_DO_MASK) != 0);
    }

    boolean _requestedDont(int option)
    {
        return !_requestedDo(option);
    }

    void _setWill(int option)
    {
        _options[option] |= _WILL_MASK;

        /* open TelnetOptionHandler functionality (start)*/
        if (_requestedWill(option))
        {
            if (_option_handlers[option] != null)
            {
                _option_handlers[option].setWill(true);

                int subneg[] =
                    _option_handlers[option].startSubnegotiationLocal();

                if (subneg != null)
                {
                    try
                    {
                        _sendSubnegotiation(subneg);
                    }
                    catch (Exception e)
                    {
                        System.err.println(
                            "Exception in option subnegotiation"
                            + e.getMessage());
                    }
                }
            }
        }
        /* open TelnetOptionHandler functionality (end)*/
    }

    void _setDo(int option)
    {
        _options[option] |= _DO_MASK;

        /* open TelnetOptionHandler functionality (start)*/
        if (_requestedDo(option))
        {
            if (_option_handlers[option] != null)
            {
                _option_handlers[option].setDo(true);

                int subneg[] =
                    _option_handlers[option].startSubnegotiationRemote();

                if(subneg != null)
                {
                    try
                    {
                        _sendSubnegotiation(subneg);
                    }
                    catch (Exception e)
                    {
                        System.err.println("Exception in option subnegotiation"
                            + e.getMessage());
                    }
                }
            }
        }
        /* open TelnetOptionHandler functionality (end)*/
    }

    void _setWantWill(int option)
    {
        _options[option] |= _REQUESTED_WILL_MASK;
    }

    void _setWantDo(int option)
    {
        _options[option] |= _REQUESTED_DO_MASK;
    }

    void _setWont(int option)
    {
        _options[option] &= ~_WILL_MASK;

        /* open TelnetOptionHandler functionality (start)*/
        if (_option_handlers[option] != null)
        {
            _option_handlers[option].setWill(false);
        }
        /* open TelnetOptionHandler functionality (end)*/
    }

    void _setDont(int option)
    {
        _options[option] &= ~_DO_MASK;

        /* open TelnetOptionHandler functionality (start)*/
        if (_option_handlers[option] != null)
        {
            _option_handlers[option].setDo(false);
        }
        /* open TelnetOptionHandler functionality (end)*/
    }

    void _setWantWont(int option)
    {
        _options[option] &= ~_REQUESTED_WILL_MASK;
    }

    void _setWantDont(int option)
    {
        _options[option] &= ~_REQUESTED_DO_MASK;
    }

    void _processDo(int option) throws IOException
    {
        if (debugoptions)
            System.err.println("RECEIVED DO: "
                + TelnetOption.getOption(option));
        boolean acceptNewState = false;

        /* open TelnetOptionHandler functionality (start)*/
        if (_option_handlers[option] != null)
        {
            acceptNewState = _option_handlers[option].getAcceptLocal();
        }
        else
        {
        /* open TelnetOptionHandler functionality (end)*/
            /* TERMINAL-TYPE option (start)*/
            if (option == TERMINAL_TYPE)
            {
                if ((terminal_type != null) && (terminal_type.length() > 0))
                {
                    acceptNewState = true;
                }
            }
            /* TERMINAL-TYPE option (end)*/
        /* open TelnetOptionHandler functionality (start)*/
        }
        /* open TelnetOptionHandler functionality (end)*/

        if (_willResponse[option] > 0)
        {
            --_willResponse[option];
            if (_willResponse[option] > 0 && _stateIsWill(option))
                --_willResponse[option];
        }

        if (_willResponse[option] == 0)
        {
            if (_requestedWont(option))
            {

                switch (option)
                {

                default:
                    break;

                }


                if (acceptNewState)
                {
                    _setWantWill(option);
                    _sendWill(option);
                }
                else
                {
                    ++_willResponse[option];
                    _sendWont(option);
                }
            }
            else
            {
                // Other end has acknowledged option.

                switch (option)
                {

                default:
                    break;

                }

            }
        }

        _setWill(option);
    }


    void _processDont(int option) throws IOException
    {
        if (debugoptions)
            System.err.println("RECEIVED DONT: "
                + TelnetOption.getOption(option));
        if (_willResponse[option] > 0)
        {
            --_willResponse[option];
            if (_willResponse[option] > 0 && _stateIsWont(option))
                --_willResponse[option];
        }

        if (_willResponse[option] == 0 && _requestedWill(option))
        {

            switch (option)
            {

            default:
                break;

            }

            /* Code section modified to correct a BUG  in the negotiation (start)*/
            if ((_stateIsWill(option)) || (_requestedWill(option)))
                _sendWont(option);

            _setWantWont(option);
            /* Code section modified to correct a BUG  in the negotiation (end)*/
        }

        _setWont(option);
    }


    void _processWill(int option) throws IOException
    {
        if (debugoptions)
            System.err.println("RECEIVED WILL: "
                + TelnetOption.getOption(option));
        boolean acceptNewState = false;

        /* open TelnetOptionHandler functionality (start)*/
        if (_option_handlers[option] != null)
        {
            acceptNewState = _option_handlers[option].getAcceptRemote();
        }
        /* open TelnetOptionHandler functionality (end)*/

        if (_doResponse[option] > 0)
        {
            --_doResponse[option];
            if (_doResponse[option] > 0 && _stateIsDo(option))
                --_doResponse[option];
        }

        if (_doResponse[option] == 0 && _requestedDont(option))
        {

            switch (option)
            {

            default:
                break;

            }


            if (acceptNewState)
            {
                _setWantDo(option);
                _sendDo(option);
            }
            else
            {
                ++_doResponse[option];
                _sendDont(option);
            }
        }

        _setDo(option);
    }


    void _processWont(int option) throws IOException
    {
       if (debugoptions)
            System.err.println("RECEIVED WONT: "
                + TelnetOption.getOption(option));
       if (_doResponse[option] > 0)
        {
            --_doResponse[option];
            if (_doResponse[option] > 0 && _stateIsDont(option))
                --_doResponse[option];
        }

        if (_doResponse[option] == 0 && _requestedDo(option))
        {

            switch (option)
            {

            default:
                break;

            }

            /* Code section modified to correct a BUG  in the negotiation (start)*/
            if ((_stateIsDo(option)) || (_requestedDo(option)))
                _sendDont(option);

            _setWantDont(option);
            /* Code section modified to correct a BUG  in the negotiation (end)*/
        }

        _setDont(option);
    }

    /* TERMINAL-TYPE option (start)*/
    void _processSuboption(int suboption[], int suboption_length)
    throws IOException
    {
        if (debug)
            System.err.println("PROCESS SUBOPTION.");

        /* open TelnetOptionHandler functionality (start)*/
        if (suboption_length > 0)
        {
            if (_option_handlers[suboption[0]] != null)
            {
                int response_suboption[] = _option_handlers[suboption[0]].answerSubnegotiation(suboption, suboption_length);
                _sendSubnegotiation(response_suboption);
            }
            else
            {
                if (suboption_length > 1)
                {
                    if (debug)
                    {
                        for(int ii=0; ii<suboption_length; ii++)
                        {
                            System.err.println("SUB[" + ii + "]: " + suboption[ii]);
                        }
                    }
                    if ((suboption[0] == TERMINAL_TYPE)
                        && (suboption[1] == TERMINAL_TYPE_SEND))
                    {
                        _sendTerminalType();
                    }
                }
            }
        }
        /* open TelnetOptionHandler functionality (end)*/
    }

    /***
     * Sends terminal type information
     ***/
    final synchronized void _sendTerminalType()
    throws IOException
    {
        if (debug)
            System.err.println("SEND TERMINAL-TYPE: " + terminal_type);
        if (terminal_type != null)
        {
            _output_.write(_COMMAND_SB);
            _output_.write(_COMMAND_IS);
            _output_.write(terminal_type.getBytes());
            _output_.write(_COMMAND_SE);
            _output_.flush();
        }
    }

    /* TERMINAL-TYPE option (end)*/

    /* open TelnetOptionHandler functionality (start)*/
    /***
     * Manages subnegotiation for Terminal Type
     ***/
    final synchronized void _sendSubnegotiation(int subn[])
    throws IOException
    {
        if (debug)
        {
            System.err.println("SEND SUBNEGOTIATION: ");
            if (subn != null)
            {
                for (int ii=0; ii<subn.length; ii++)
                {
                    System.err.println("subn["  + ii + "]=" + subn[ii]);
                }
            }
        }
        if (subn != null)
        {
            byte byteresp[] = new byte[subn.length];
            for (int ii=0; ii<subn.length; ii++)
                byteresp[ii] = (byte)subn[ii];

            _output_.write(_COMMAND_SB);
            _output_.write(byteresp);
            _output_.write(_COMMAND_SE);

            /* Code Section added for sending the negotiation ASAP (start)*/
            _output_.flush();
            /* Code Section added for sending the negotiation ASAP (end)*/
        }
    }
    /* open TelnetOptionHandler functionality (end)*/

    /* Code Section added for supporting AYT (start)*/
    /***
     * Processes the response of an AYT
     ***/
    final synchronized void _processAYTResponse()
    {
        if (!_ayt_flag)
        {
            synchronized (_ayt_monitor)
            {
                _ayt_flag = true;
                try
                {
                    _ayt_monitor.notifyAll();
                }
                catch (Exception e)
                {
                    System.err.println("Exception notifying:" + e.getMessage());
                }
            }
        }
    }
    /* Code Section added for supporting AYT (end)*/

    protected void _connectAction_() throws IOException
    {
        /* (start). BUGFIX: clean the option info for each connection*/
        for (int ii = 0; ii < TelnetOption.MAX_OPTION_VALUE + 1; ii++)
        {
            _doResponse[ii] = 0;
            _willResponse[ii] = 0;
            _options[ii] = 0;
            if (_option_handlers[ii] != null)
            {
                _option_handlers[ii].setDo(false);
                _option_handlers[ii].setWill(false);
            }
        }
        /* (end). BUGFIX: clean the option info for each connection*/

        super._connectAction_();
        _input_ = new BufferedInputStream(_input_);
        _output_ = new BufferedOutputStream(_output_);

        /* open TelnetOptionHandler functionality (start)*/
        for (int ii = 0; ii < TelnetOption.MAX_OPTION_VALUE + 1; ii++)
        {
            if (_option_handlers[ii] != null)
            {
                if (_option_handlers[ii].getInitLocal())
                {
                    try
                    {
                        _requestWill(_option_handlers[ii].getOptionCode());
                    }
                    catch(IOException e)
                    {
                        System.err.println(
                            "Exception while initializing option: "
                            + e.getMessage());
                    }
                }

                if (_option_handlers[ii].getInitRemote())
                {
                    try
                    {
                        _requestDo(_option_handlers[ii].getOptionCode());
                    }
                    catch (IOException e)
                    {
                        System.err.println(
                            "Exception while initializing option: "
                            + e.getMessage());
                    }
                }
            }
        }
        /* open TelnetOptionHandler functionality (end)*/
    }


    final synchronized void _sendDo(int option)
    throws IOException
    {
        if (debug || debugoptions)
            System.err.println("DO: " + TelnetOption.getOption(option));
        _output_.write(_COMMAND_DO);
        _output_.write(option);

        /* Code Section added for sending the negotiation ASAP (start)*/
        _output_.flush();
        /* Code Section added for sending the negotiation ASAP (end)*/
    }

    final synchronized void _requestDo(int option)
    throws IOException
    {
        if ((_doResponse[option] == 0 && _stateIsDo(option)) ||
                _requestedDo(option))
            return ;
        _setWantDo(option);
        ++_doResponse[option];
        _sendDo(option);
    }

    final synchronized void _sendDont(int option)
    throws IOException
    {
        if (debug || debugoptions)
            System.err.println("DONT: " + TelnetOption.getOption(option));
        _output_.write(_COMMAND_DONT);
        _output_.write(option);

        /* Code Section added for sending the negotiation ASAP (start)*/
        _output_.flush();
        /* Code Section added for sending the negotiation ASAP (end)*/
    }

    final synchronized void _requestDont(int option)
    throws IOException
    {
        if ((_doResponse[option] == 0 && _stateIsDont(option)) ||
                _requestedDont(option))
            return ;
        _setWantDont(option);
        ++_doResponse[option];
        _sendDont(option);
    }


    final synchronized void _sendWill(int option)
    throws IOException
    {
        if (debug || debugoptions)
            System.err.println("WILL: " + TelnetOption.getOption(option));
        _output_.write(_COMMAND_WILL);
        _output_.write(option);

        /* Code Section added for sending the negotiation ASAP (start)*/
        _output_.flush();
        /* Code Section added for sending the negotiation ASAP (end)*/
    }

    final synchronized void _requestWill(int option)
    throws IOException
    {
        if ((_willResponse[option] == 0 && _stateIsWill(option)) ||
                _requestedWill(option))
            return ;
        _setWantWill(option);
        ++_doResponse[option];
        _sendWill(option);
    }

    final synchronized void _sendWont(int option)
    throws IOException
    {
        if (debug || debugoptions)
            System.err.println("WONT: " + TelnetOption.getOption(option));
        _output_.write(_COMMAND_WONT);
        _output_.write(option);

        /* Code Section added for sending the negotiation ASAP (start)*/
        _output_.flush();
        /* Code Section added for sending the negotiation ASAP (end)*/
    }

    final synchronized void _requestWont(int option)
    throws IOException
    {
        if ((_willResponse[option] == 0 && _stateIsWont(option)) ||
                _requestedWont(option))
            return ;
        _setWantWont(option);
        ++_doResponse[option];
        _sendWont(option);
    }

    final synchronized void _sendByte(int b)
    throws IOException
    {
        _output_.write(b);

        /* Code Section added for supporting spystreams (start)*/
        _spyWrite(b);
        /* Code Section added for supporting spystreams (end)*/

    }

    /* Code Section added for supporting AYT (start)*/
    /***
     * Sends an Are You There sequence and waits for the result
     * <p>
     * @param timeout - Time to wait for a response (millis.)
     * <p>
     * @return true if AYT received a response, false otherwise
     ***/
    final boolean _sendAYT(long timeout)
    throws IOException, IllegalArgumentException, InterruptedException
    {
        boolean ret_value = false;
        synchronized (_ayt_monitor)
        {
            synchronized (this)
            {
                _ayt_flag = false;
                _output_.write(_COMMAND_AYT);
                _output_.flush();
            }

            try
            {
                _ayt_monitor.wait(timeout);
                if(_ayt_flag == false)
                {
                    ret_value = false;
                    _ayt_flag = true;
                }
                else
                    ret_value = true;
            }
            catch (IllegalMonitorStateException e)
            {
                System.err.println("Exception processing AYT:"
                    + e.getMessage());
            }
        }

        return (ret_value);
    }
    /* Code Section added for supporting AYT (end)*/

    /* open TelnetOptionHandler functionality (start)*/

    /***
     * Registers a new TelnetOptionHandler for this telnet  to use.
     * <p>
     * @param opthand - option handler to be registered.
     ***/
    void addOptionHandler(TelnetOptionHandler opthand)
    throws InvalidTelnetOptionException
    {
        int optcode = opthand.getOptionCode();
        if (TelnetOption.isValidOption(optcode))
        {
            if (_option_handlers[optcode] == null)
            {
                _option_handlers[optcode] = opthand;
                if (isConnected())
                {
                    if(opthand.getInitLocal())
                    {
                        try
                        {
                            _requestWill(optcode);
                        }
                        catch (IOException e)
                        {
                            System.err.println(
                                "Exception while initializing option: "
                                + e.getMessage());
                        }
                    }

                    if (opthand.getInitRemote())
                    {
                        try
                        {
                            _requestDo(optcode);
                        }
                        catch (IOException e)
                        {
                            System.err.println(
                                "Exception while initializing option: "
                                + e.getMessage());
                        }
                    }
                }
            }
            else
            {
                throw (new InvalidTelnetOptionException(
                    "Already registered option", optcode));
            }
        }
        else
        {
            throw (new InvalidTelnetOptionException(
                "Invalid Option Code", optcode));
        }
    }

    /***
     * Unregisters a  TelnetOptionHandler.
     * <p>
     * @param optcode - Code of the option to be unregistered.
     ***/
    void deleteOptionHandler(int optcode)
    throws InvalidTelnetOptionException
    {
        if (TelnetOption.isValidOption(optcode))
        {
            if (_option_handlers[optcode] == null)
            {
                throw (new InvalidTelnetOptionException(
                    "Unregistered option", optcode));
            }
            else
            {
                TelnetOptionHandler opthand = _option_handlers[optcode];
                _option_handlers[optcode] = null;

                if (opthand.getWill())
                {
                    try
                    {
                        _requestWont(optcode);
                    }
                    catch (IOException e)
                    {
                        System.err.println(
                            "Exception while turning off option: "
                            + e.getMessage());
                    }
                }

                if (opthand.getDo())
                {
                    try
                    {
                        _requestDont(optcode);
                    }
                    catch (IOException e)
                    {
                        System.err.println(
                            "Exception while turning off option: "
                            + e.getMessage());
                    }
                }
            }
        }
        else
        {
            throw (new InvalidTelnetOptionException(
                "Invalid Option Code", optcode));
        }
    }
    /* open TelnetOptionHandler functionality (end)*/

    /* Code Section added for supporting spystreams (start)*/
    /***
     * Registers an OutputStream for spying what's going on in
     * the Telnet session.
     * <p>
     * @param spystream - OutputStream on which session activity
     * will be echoed.
     ***/
    void _registerSpyStream(OutputStream  spystream)
    {
        _spystream = spystream;
    }

    /***
     * Stops spying this Telnet.
     * <p>
     ***/
    void _stopSpyStream()
    {
        _spystream = null;
    }

    /***
     * Sends a read char on the spy stream
     * <p>
     * @param ch - character read from the session
     ***/
    void _spyRead(int ch)
    {
        if (_spystream != null)
        {
            try
            {
                if (ch != (int)'\r')
                {
                    _spystream.write(ch);
                    if (ch == (int)'\n')
                    {
                        _spystream.write((int)'\r');
                    }
                    _spystream.flush();
                }
            }
            catch (Exception e)
            {
                _spystream = null;
            }
        }
    }

    /***
     * Sends a written char on the spy stream
     * <p>
     * @param ch - character written to the session
     ***/
    void _spyWrite(int ch)
    {
        if (!(_stateIsDo(TelnetOption.ECHO)
            && _requestedDo(TelnetOption.ECHO)))
        {
            if (_spystream != null)
            {
                try
                {
                    _spystream.write(ch);
                    _spystream.flush();
                }
                catch (Exception e)
                {
                    _spystream = null;
                }
            }
        }
    }
    /* Code Section added for supporting spystreams (end)*/
}
