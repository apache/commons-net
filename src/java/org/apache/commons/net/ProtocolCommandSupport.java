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

import java.io.Serializable;
import java.util.Enumeration;
import org.apache.commons.net.util.ListenerList;

/***
 * ProtocolCommandSupport is a convenience class for managing a list of
 * ProtocolCommandListeners and firing ProtocolCommandEvents.  You can
 * simply delegate ProtocolCommandEvent firing and listener
 * registering/unregistering tasks to this class.
 * <p>
 * <p>
 * @see ProtocolCommandEvent
 * @see ProtocolCommandListener
 * @author Daniel F. Savarese
 ***/

public class ProtocolCommandSupport implements Serializable
{
    private Object __source;
    private ListenerList __listeners;

    /***
     * Creates a ProtocolCommandSupport instant using the indicated source
     * as the source of fired ProtocolCommandEvents.
     * <p>
     * @param source  The source to use for all generated ProtocolCommandEvents.
     ***/
    public ProtocolCommandSupport(Object source)
    {
        __listeners = new ListenerList();
        __source = source;
    }


    /***
     * Fires a ProtocolCommandEvent signalling the sending of a command to all
     * registered listeners, invoking their
     * <a href="org.apache.commons.net.ProtocolCommandListener.html#protocolCommandSent">
     * protocolCommandSent() </a> methods.
     * <p>
     * @param command The string representation of the command type sent, not
     *      including the arguments (e.g., "STAT" or "GET").
     * @param message The entire command string verbatim as sent to the server,
     *        including all arguments.
     ***/
    public void fireCommandSent(String command, String message)
    {
        Enumeration enum;
        ProtocolCommandEvent event;
        ProtocolCommandListener listener;

        enum = __listeners.getListeners();

        event = new ProtocolCommandEvent(__source, command, message);

        while (enum.hasMoreElements())
        {
            listener = (ProtocolCommandListener)enum.nextElement();
            listener.protocolCommandSent(event);
        }
    }

    /***
     * Fires a ProtocolCommandEvent signalling the reception of a command reply
     * to all registered listeners, invoking their
     * <a href="org.apache.commons.net.ProtocolCommandListener.html#protocolReplyReceived">
     * protocolReplyReceived() </a> methods.
     * <p>
     * @param replyCode The integer code indicating the natureof the reply.
     *   This will be the protocol integer value for protocols
     *   that use integer reply codes, or the reply class constant
     *   corresponding to the reply for protocols like POP3 that use
     *   strings like OK rather than integer codes (i.e., POP3Repy.OK).
     * @param message The entire reply as received from the server.
     ***/
    public void fireReplyReceived(int replyCode, String message)
    {
        Enumeration enum;
        ProtocolCommandEvent event;
        ProtocolCommandListener listener;

        enum = __listeners.getListeners();

        event = new ProtocolCommandEvent(__source, replyCode, message);

        while (enum.hasMoreElements())
        {
            listener = (ProtocolCommandListener)enum.nextElement();
            listener.protocolReplyReceived(event);
        }
    }

    /***
     * Adds a ProtocolCommandListener.
     * <p>
     * @param listener  The ProtocolCommandListener to add.
     ***/
    public void addProtocolCommandListener(ProtocolCommandListener listener)
    {
        __listeners.addListener(listener);
    }

    /***
     * Removes a ProtocolCommandListener.
     * <p>
     * @param listener  The ProtocolCommandListener to remove.
     ***/
    public void removeProtocolCommandListener(ProtocolCommandListener listener)
    {
        __listeners.removeListener(listener);
    }


    /***
     * Returns the number of ProtocolCommandListeners currently registered.
     * <p>
     * @return The number of ProtocolCommandListeners currently registered.
     ***/
    public int getListenerCount()
    {
        return __listeners.getListenerCount();
    }

}

