package org.apache.commons.net;

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

import java.util.EventObject;

/***
 * There exists a large class of IETF protocols that work by sending an
 * ASCII text command and arguments to a server, and then receiving an
 * ASCII text reply.  For debugging and other purposes, it is extremely
 * useful to log or keep track of the contents of the protocol messages.
 * The ProtocolCommandEvent class coupled with the
 * <a href="org.apache.commons.net.ProtocolCommandListener.html"> 
 * ProtocolCommandListener </a> interface facilitate this process.
 * <p>
 * <p>
 * @see ProtocolCommandListener
 * @see ProtocolCommandSupport
 * @author Daniel F. Savarese
 ***/

public class ProtocolCommandEvent extends EventObject
{
    private int __replyCode;
    private boolean __isCommand;
    private String __message, __command;

    /***
     * Creates a ProtocolCommandEvent signalling a command was sent to
     * the server.  ProtocolCommandEvents created with this constructor
     * should only be sent after a command has been sent, but before the
     * reply has been received.
     * <p>
     * @param source  The source of the event.
     * @param command The string representation of the command type sent, not
     *      including the arguments (e.g., "STAT" or "GET").
     * @param message The entire command string verbatim as sent to the server,
     *        including all arguments.
     ***/
    public ProtocolCommandEvent(Object source, String command, String message)
    {
        super(source);
        __replyCode = 0;
        __message = message;
        __isCommand = true;
        __command = command;
    }


    /***
     * Creates a ProtocolCommandEvent signalling a reply to a command was
     * received.  ProtocolCommandEvents created with this constructor
     * should only be sent after a complete command reply has been received
     * fromt a server.
     * <p>
     * @param source  The source of the event.
     * @param replyCode The integer code indicating the natureof the reply.
     *   This will be the protocol integer value for protocols
     *   that use integer reply codes, or the reply class constant
     *   corresponding to the reply for protocols like POP3 that use
     *   strings like OK rather than integer codes (i.e., POP3Repy.OK).
     * @param message The entire reply as received from the server.
     ***/
    public ProtocolCommandEvent(Object source, int replyCode, String message)
    {
        super(source);
        __replyCode = replyCode;
        __message = message;
        __isCommand = false;
        __command = null;
    }

    /***
     * Returns the string representation of the command type sent (e.g., "STAT"
     * or "GET").  If the ProtocolCommandEvent is a reply event, then null
     * is returned.
     * <p>
     * @return The string representation of the command type sent, or null
     *         if this is a reply event.
     ***/
    public String getCommand()
    {
        return __command;
    }


    /***
     * Returns the reply code of the received server reply.  Undefined if
     * this is not a reply event.
     * <p>
     * @return The reply code of the received server reply.  Undefined if
     *         not a reply event.
     ***/
    public int getReplyCode()
    {
        return __replyCode;
    }

    /***
     * Returns true if the ProtocolCommandEvent was generated as a result
     * of sending a command.
     * <p>
     * @return true If the ProtocolCommandEvent was generated as a result
     * of sending a command.  False otherwise.
     ***/
    public boolean isCommand()
    {
        return __isCommand;
    }

    /***
     * Returns true if the ProtocolCommandEvent was generated as a result
     * of receiving a reply.
     * <p>
     * @return true If the ProtocolCommandEvent was generated as a result
     * of receiving a reply.  False otherwise.
     ***/
    public boolean isReply()
    {
        return !isCommand();
    }

    /***
     * Returns the entire message sent to or received from the server.
     * <p>
     * @return The entire message sent to or received from the server.
     ***/
    public String getMessage()
    {
        return __message;
    }
}
