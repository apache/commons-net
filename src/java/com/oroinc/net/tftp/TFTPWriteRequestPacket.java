/***
 * $Id: TFTPWriteRequestPacket.java,v 1.1 2002/04/03 01:04:38 brekke Exp $
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

package com.oroinc.net.tftp;

import java.net.*;

/***
 * A class derived from TFTPRequestPacket definiing a TFTP write request
 * packet type.
 * <p>
 * Details regarding the TFTP protocol and the format of TFTP packets can
 * be found in RFC 783.  But the point of these classes is to keep you
 * from having to worry about the internals.  Additionally, only very
 * few people should have to care about any of the TFTPPacket classes
 * or derived classes.  Almost all users should only be concerned with the
 * <a href="com.oroinc.net.tftp.TFTPClient.html#_top_">TFTPClient</a> class
 * <a href="com.oroinc.net.tftp.TFTPClient.html#receiveFile">receiveFile()</a>
 * and
 * <a href="com.oroinc.net.tftp.TFTPClient.html#sendFile">sendFile()</a>
 * methods.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see TFTPPacket
 * @see TFTPRequestPacket
 * @see TFTPPacketException
 * @see TFTP
 ***/

public final class TFTPWriteRequestPacket extends TFTPRequestPacket {

  /***
   * Creates a write request packet to be sent to a host at a 
   * given port with a filename and transfer mode request.
   * <p>
   * @param destination  The host to which the packet is going to be sent.
   * @param port  The port to which the packet is going to be sent.
   * @param filename The requested filename.
   * @param mode The requested transfer mode.  This should be on of the TFTP
   *        class MODE constants (e.g., TFTP.NETASCII_MODE).
   ***/
  public TFTPWriteRequestPacket(InetAddress destination, int port,
				String filename, int mode) {
    super(destination, port, TFTPPacket.WRITE_REQUEST, filename, mode);
  }

  /***
   * Creates a write request packet of based on a received
   * datagram and assumes the datagram has already been identified as a
   * write request.  Assumes the datagram is at least length 4, else an
   * ArrayIndexOutOfBoundsException may be thrown.
   * <p>
   * @param datagram  The datagram containing the received request.
   * @throws TFTPPacketException  If the datagram isn't a valid TFTP
   *         request packet.
   ***/
  TFTPWriteRequestPacket(DatagramPacket datagram) throws TFTPPacketException {
    super(TFTPPacket.WRITE_REQUEST, datagram);
  }
}
