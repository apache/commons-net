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

/***
 * The TelnetOptionHandler class is the base class to be used
 * for implementing handlers for telnet options.
 * <p>
 * TelnetOptionHandler implements basic option handling
 * functionality and defines abstract methods that must be
 * implemented to define subnegotiation behaviour.
 * <p>
 * @author Bruno D'Avanzo
 ***/
public abstract class TelnetOptionHandler
{
    /***
     * Option code
     ***/
    private int optionCode = -1;

    /***
     * true if the option should be activated on the local side
     ***/
    private boolean initialLocal = false;

    /***
     * true if the option should be activated on the remote side
     ***/
    private boolean initialRemote = false;

    /***
     * true if the option should be accepted on the local side
     ***/
    private boolean acceptLocal = false;

    /***
     * true if the option should be accepted on the remote side
     ***/
    private boolean acceptRemote = false;

    /***
     * true if the option is active on the local side
     ***/
    private boolean doFlag = false;

    /***
     * true if the option is active on the remote side
     ***/
    private boolean willFlag = false;

    /***
     * Constructor for the TelnetOptionHandler. Allows defining desired
     * initial setting for local/remote activation of this option and
     * behaviour in case a local/remote activation request for this
     * option is received.
     * <p>
     * @param optcode - Option code.
     * @param initlocal - if set to true, a WILL is sent upon connection.
     * @param initremote - if set to true, a DO is sent upon connection.
     * @param acceptlocal - if set to true, any DO request is accepted.
     * @param acceptremote - if set to true, any WILL request is accepted.
     ***/
    public TelnetOptionHandler(int optcode,
                                boolean initlocal,
                                boolean initremote,
                                boolean acceptlocal,
                                boolean acceptremote)
    {
        optionCode = optcode;
        initialLocal = initlocal;
        initialRemote = initremote;
        acceptLocal = acceptlocal;
        acceptRemote = acceptremote;
    }


    /***
     * Returns the option code for this option.
     * <p>
     * @return Option code.
     ***/
    public int getOptionCode()
    {
        return (optionCode);
    }

    /***
     * Returns a boolean indicating whether to accept a DO
     * request coming from the other end
     * <p>
     * @return true if a DO request shall be accepted.
     ***/
    public boolean getAcceptLocal()
    {
        return (acceptLocal);
    }

    /***
     * Returns a boolean indicating whether to accept a WILL
     * request coming from the other end
     * <p>
     * @return true if a WILL request shall be accepted.
     ***/
    public boolean getAcceptRemote()
    {
        return (acceptRemote);
    }

    /***
     * Set behaviour of the option for DO requests coming from
     * the other end.
     * <p>
     * @param accept - if true, subsequent DO requests will be accepted.
     ***/
    public void setAcceptLocal(boolean accept)
    {
        acceptLocal = accept;
    }

    /***
     * Set behaviour of the option for WILL requests coming from
     * the other end.
     * <p>
     * @param accept - if true, subsequent WILL requests will be accepted.
     ***/
    public void setAcceptRemote(boolean accept)
    {
        acceptRemote = accept;
    }

    /***
     * Returns a boolean indicating whether to send a WILL request
     * to the other end upon connection.
     * <p>
     * @return true if a WILL request shall be sent upon connection.
     ***/
    public boolean getInitLocal()
    {
        return (initialLocal);
    }

    /***
     * Returns a boolean indicating whether to send a DO request
     * to the other end upon connection.
     * <p>
     * @return true if a DO request shall be sent upon connection.
     ***/
    public boolean getInitRemote()
    {
        return (initialRemote);
    }

    /***
     * Tells this option whether to send a WILL request upon connection
     * <p>
     * @param init - if true, a WILL request will be sent upon subsequent
     * connections.
     ***/
    public void setInitLocal(boolean init)
    {
        initialLocal = init;
    }

    /***
     * Tells this option whether to send a DO request upon connection
     * <p>
     * @param init - if true, a DO request will be sent upon subsequent
     * connections.
     ***/
    public void setInitRemote(boolean init)
    {
        initialRemote = init;
    }

    /***
     * Method called upon reception of a subnegotiation for this option
     * coming from the other end.
     * Must be implemented by the actual TelnetOptionHandler to specify
     * which response must be sent for the subnegotiation request.
     * <p>
     * @param suboptionData - the sequence received, whithout IAC SB & IAC SE
     * @param suboptionLength - the length of data in suboption_data
     * <p>
     * @return response to be sent to the subnegotiation sequence. TelnetClient
     * will add IAC SB & IAC SE. null means no response
     ***/
    public abstract int[] answerSubnegotiation(int suboptionData[],
                            int suboptionLength);

    /***
     * This method is invoked whenever this option is acknowledged active on
     * the local end (TelnetClient sent a WILL, remote side sent a DO).
     * The method is used to specify a subnegotiation sequence that will be
     * sent by TelnetClient when the option is activated.
     * <p>
     * @return subnegotiation sequence to be sent by TelnetClient. TelnetClient
     * will add IAC SB & IAC SE. null means no subnegotiation.
     ***/
    public abstract int[] startSubnegotiationLocal();

    /***
     * This method is invoked whenever this option is acknowledged active on
     * the remote end (TelnetClient sent a DO, remote side sent a WILL).
     * The method is used to specify a subnegotiation sequence that will be
     * sent by TelnetClient when the option is activated.
     * <p>
     * @return subnegotiation sequence to be sent by TelnetClient. TelnetClient
     * will add IAC SB & IAC SE. null means no subnegotiation.
     ***/
    public abstract int[] startSubnegotiationRemote();

    /***
     * Returns a boolean indicating whether a WILL request sent to the other
     * side has been acknowledged.
     * <p>
     * @return true if a WILL sent to the other side has been acknowledged.
     ***/
    boolean getWill()
    {
        return willFlag;
    }

    /***
     * Tells this option whether a WILL request sent to the other
     * side has been acknowledged (invoked by TelnetClient).
     * <p>
     * @param state - if true, a WILL request has been acknowledged.
     ***/
    void setWill(boolean state)
    {
        willFlag = state;
    }

    /***
     * Returns a boolean indicating whether a DO request sent to the other
     * side has been acknowledged.
     * <p>
     * @return true if a DO sent to the other side has been acknowledged.
     ***/
    boolean getDo()
    {
        return doFlag;
    }


    /***
     * Tells this option whether a DO request sent to the other
     * side has been acknowledged (invoked by TelnetClient).
     * <p>
     * @param state - if true, a DO request has been acknowledged.
     ***/
    void setDo(boolean state)
    {
        doFlag = state;
    }
}
