/***
 * $Id: ProtocolCommandEvent.java,v 1.1 2002/04/03 01:04:25 brekke Exp $
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

/***
 * There exists a large class of IETF protocols that work by sending an
 * ASCII text command and arguments to a server, and then receiving an
 * ASCII text reply.  For debugging and other purposes, it is extremely
 * useful to log or keep track of the contents of the protocol messages.
 * The ProtocolCommandEvent class coupled with the
 * <a href="com.oroinc.net.ProtocolCommandListener.html"> 
 * ProtocolCommandListener </a> interface facilitate this process.
 * <p>
 * <p>
 * @see ProtocolCommandListener
 * @see ProtocolCommandSupport
 * @author Daniel F. Savarese
 ***/

public class ProtocolCommandEvent extends EventObject {
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
  public ProtocolCommandEvent(Object source, String command, String message){
    super(source);
    __replyCode = 0;
    __message = message;
    __isCommand = true;
    __command   = command;
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
  public ProtocolCommandEvent(Object source, int replyCode, String message){
    super(source);
    __replyCode = replyCode;
    __message = message;
    __isCommand = false;
    __command   = null;
  }

  /***
   * Returns the string representation of the command type sent (e.g., "STAT"
   * or "GET").  If the ProtocolCommandEvent is a reply event, then null
   * is returned.
   * <p>
   * @return The string representation of the command type sent, or null
   *         if this is a reply event.
   ***/
  public String getCommand() { return __command; }


  /***
   * Returns the reply code of the received server reply.  Undefined if
   * this is not a reply event.
   * <p>
   * @return The reply code of the received server reply.  Undefined if
   *         not a reply event.
   ***/
  public int getReplyCode()  { return __replyCode; }

  /***
   * Returns true if the ProtocolCommandEvent was generated as a result
   * of sending a command.
   * <p>
   * @return true If the ProtocolCommandEvent was generated as a result
   * of sending a command.  False otherwise.
   ***/
  public boolean isCommand() { return __isCommand; }

  /***
   * Returns true if the ProtocolCommandEvent was generated as a result
   * of receiving a reply.
   * <p>
   * @return true If the ProtocolCommandEvent was generated as a result
   * of receiving a reply.  False otherwise.
   ***/
  public boolean isReply()   { return !isCommand(); }

  /***
   * Returns the entire message sent to or received from the server.
   * <p>
   * @return The entire message sent to or received from the server.
   ***/
  public String getMessage() { return __message; }
}
