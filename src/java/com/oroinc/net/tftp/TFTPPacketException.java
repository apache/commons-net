/***
 * $Id: TFTPPacketException.java,v 1.1 2002/04/03 01:04:38 brekke Exp $
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

/***
 * A class used to signify the occurrence of an error in the creation of
 * a TFTP packet.  It is not declared final so that it may be subclassed
 * to identify more specific errors.  You would only want to do this if
 * you were building your own TFTP client or server on top of the
 * <a href="com.oroinc.net.tftp.TFTP.html#_top_">TFTP</a> 
 * class if you
 * wanted more functionality than the 
 * <a href="com.oroinc.net.tftp.TFTPClient.html#receiveFile">receiveFile()</a>
 * and
 * <a href="com.oroinc.net.tftp.TFTPClient.html#sendFile">sendFile()</a>
 * methods provide.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see TFTPPacket
 * @see TFTP
 ***/

public class TFTPPacketException extends Exception {

  /***
   * Simply calls the corresponding constructor of its superclass.
   ***/
  public TFTPPacketException() {
    super();
  }

  /***
   * Simply calls the corresponding constructor of its superclass.
   ***/
  public TFTPPacketException(String message) {
    super(message);
  }
}
