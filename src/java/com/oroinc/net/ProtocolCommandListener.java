/***
 * $Id: ProtocolCommandListener.java,v 1.1 2002/04/03 01:04:26 brekke Exp $
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
 * The ProtocolCommandListener interface coupled with the
 * <a href="com.oroinc.net.ProtocolCommandEvent.html"> ProtocolCommandEvent
 * </a> class facilitate this process.
 * <p>
 * To receive ProtocolCommandEvents, you merely implement the
 * ProtocolCommandListener interface and register the class as a listener
 * with a ProtocolCommandEvent source such as 
 * <a href="com.oroinc.net.ftp.FTPClient.html"> FTPClient </a>.
 * <p>
 * <p>
 * @see ProtocolCommandEvent
 * @see ProtocolCommandSupport
 * @author Daniel F. Savarese
 ***/

public interface ProtocolCommandListener extends EventListener {

  /***
   * This method is invoked by a ProtocolCommandEvent source after
   * sending a protocol command to a server.
   * <p>
   * @param event The ProtocolCommandEvent fired.
   ***/
  public void protocolCommandSent(ProtocolCommandEvent event);

  /***
   * This method is invoked by a ProtocolCommandEvent source after
   * receiving a reply from a server.
   * <p>
   * @param event The ProtocolCommandEvent fired.
   ***/
  public void protocolReplyReceived(ProtocolCommandEvent event);

}
