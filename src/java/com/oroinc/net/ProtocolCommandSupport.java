/***
 * $Id: ProtocolCommandSupport.java,v 1.1 2002/04/03 01:04:26 brekke Exp $
 *
 * NetComponents Internet Protocol Library
 * Copyright (C) 1997-2002  Daniel F. Savarese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library in the LICENSE file; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 ***/

package com.oroinc.net;

import java.util.*;

import com.oroinc.util.ListenerList;

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

public class ProtocolCommandSupport implements java.io.Serializable {
  private Object __source;
  private ListenerList __listeners;

  /***
   * Creates a ProtocolCommandSupport instant using the indicated source
   * as the source of fired ProtocolCommandEvents.
   * <p>
   * @param source  The source to use for all generated ProtocolCommandEvents.
   ***/
  public ProtocolCommandSupport(Object source) {
    __listeners = new ListenerList();
    __source = source;
  }


  /***
   * Fires a ProtocolCommandEvent signalling the sending of a command to all
   * registered listeners, invoking their
   * <a href="com.oroinc.net.ProtocolCommandListener.html#protocolCommandSent">
   * protocolCommandSent() </a> methods.
   * <p>
   * @param command The string representation of the command type sent, not
   *      including the arguments (e.g., "STAT" or "GET").
   * @param message The entire command string verbatim as sent to the server,
   *        including all arguments.
   ***/
  public void fireCommandSent(String command, String message){
    Enumeration enum;
    ProtocolCommandEvent event;
    ProtocolCommandListener listener;

    enum = __listeners.getListeners();

    event = new ProtocolCommandEvent(__source, command, message);

    while(enum.hasMoreElements()) {
      listener = (ProtocolCommandListener)enum.nextElement();
      listener.protocolCommandSent(event);
    }
  }

  /***
   * Fires a ProtocolCommandEvent signalling the reception of a command reply
   * to all registered listeners, invoking their
   * <a href="com.oroinc.net.ProtocolCommandListener.html#protocolReplyReceived">
   * protocolReplyReceived() </a> methods.
   * <p>
   * @param replyCode The integer code indicating the natureof the reply.
   *   This will be the protocol integer value for protocols
   *   that use integer reply codes, or the reply class constant
   *   corresponding to the reply for protocols like POP3 that use
   *   strings like OK rather than integer codes (i.e., POP3Repy.OK).
   * @param message The entire reply as received from the server.
   ***/
  public void fireReplyReceived(int replyCode, String message){
    Enumeration enum;
    ProtocolCommandEvent event;
    ProtocolCommandListener listener;

    enum = __listeners.getListeners();

    event = new ProtocolCommandEvent(__source, replyCode, message);

    while(enum.hasMoreElements()) {
      listener = (ProtocolCommandListener)enum.nextElement();
      listener.protocolReplyReceived(event);
    }
  }

  /***
   * Adds a ProtocolCommandListener.
   * <p>
   * @param listener  The ProtocolCommandListener to add.
   ***/
  public void addProtocolCommandListener(ProtocolCommandListener listener){
    __listeners.addListener(listener);
  }

  /***
   * Removes a ProtocolCommandListener.
   * <p>
   * @param listener  The ProtocolCommandListener to remove.
   ***/
  public void removeProtocolCommandListener(ProtocolCommandListener listener){
    __listeners.removeListener(listener);
  }


  /***
   * Returns the number of ProtocolCommandListeners currently registered.
   * <p>
   * @return The number of ProtocolCommandListeners currently registered.
   ***/
  public int getListenerCount() { return __listeners.getListenerCount(); }

}

