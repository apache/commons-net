/*
 * Copyright 2001-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net;
import java.util.EventListener;

/***
 * There exists a large class of IETF protocols that work by sending an
 * ASCII text command and arguments to a server, and then receiving an
 * ASCII text reply.  For debugging and other purposes, it is extremely
 * useful to log or keep track of the contents of the protocol messages.
 * The ProtocolCommandListener interface coupled with the
 * <a href="org.apache.commons.net.ProtocolCommandEvent.html"> ProtocolCommandEvent
 * </a> class facilitate this process.
 * <p>
 * To receive ProtocolCommandEvents, you merely implement the
 * ProtocolCommandListener interface and register the class as a listener
 * with a ProtocolCommandEvent source such as 
 * <a href="org.apache.commons.net.ftp.FTPClient.html"> FTPClient </a>.
 * <p>
 * <p>
 * @see ProtocolCommandEvent
 * @see ProtocolCommandSupport
 * @author Daniel F. Savarese
 ***/

public interface ProtocolCommandListener extends EventListener
{

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
