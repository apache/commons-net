/***
 * $Id: TFTPAckPacket.java,v 1.1 2002/04/03 01:04:38 brekke Exp $
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
 * A final class derived from TFTPPacket definiing the TFTP Acknowledgement
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
 * @see TFTPPacketException
 * @see TFTP
 ***/

public final class TFTPAckPacket extends TFTPPacket {
  /*** The block number being acknowledged by the packet. ***/
  int _blockNumber;

  /***
   * Creates an acknowledgment packet to be sent to a host at a given port
   * acknowledging receipt of a block.
   * <p>
   * @param destination  The host to which the packet is going to be sent.
   * @param port  The port to which the packet is going to be sent.
   * @param blockNumber  The block number being acknowledged.
   ***/
  public TFTPAckPacket(InetAddress destination, int port, int blockNumber) {
    super(TFTPPacket.ACKNOWLEDGEMENT, destination, port);
    _blockNumber = blockNumber;
  }

  /***
   * Creates an acknowledgement packet based from a received
   * datagram.  Assumes the datagram is at least length 4, else an
   * ArrayIndexOutOfBoundsException may be thrown.
   * <p>
   * @param datagram  The datagram containing the received acknowledgement.
   * @throws TFTPPacketException  If the datagram isn't a valid TFTP
   *         acknowledgement packet.
   ***/
  TFTPAckPacket(DatagramPacket datagram) throws TFTPPacketException {
    super(TFTPPacket.ACKNOWLEDGEMENT, datagram.getAddress(),
	  datagram.getPort());
    byte[] data;

    data = datagram.getData();

    if(getType() != data[1])
      throw new TFTPPacketException("TFTP operator code does not match type.");

    _blockNumber = (((data[2] & 0xff) << 8) | (data[3] & 0xff));
  }

  /***
   * This is a method only available within the package for
   * implementing efficient datagram transport by elminating buffering.
   * It takes a datagram as an argument, and a byte buffer in which 
   * to store the raw datagram data.  Inside the method, the data
   * is set as the datagram's data and the datagram returned.
   * <p>
   * @param datagram  The datagram to create.
   * @param data The buffer to store the packet and to use in the datagram.
   * @return The datagram argument.
   ***/
  DatagramPacket _newDatagram(DatagramPacket datagram, byte[] data) {
    data[0] = 0; data[1] = (byte)_type;
    data[2] = (byte)((_blockNumber & 0xffff) >> 8);
    data[3] = (byte)(_blockNumber & 0xff);

    datagram.setAddress(_address);
    datagram.setPort(_port);
    datagram.setData(data);
    datagram.setLength(4);

    return datagram;
  }


  /***
   * This is a method exposed to the programmer in case he
   * wants to implement his own TFTP client instead of using
   * the <a href="com.oroinc.net.tftp.TFTPClient.html#_top_">TFTPClient</a>
   * class.
   * Under normal circumstances, you should not have a need to call this
   * method.  It creates a UDP datagram containing all the TFTP
   * acknowledgement packet data in the proper format.
   * <p>
   * @return A UDP datagram containing the TFTP acknowledgement packet.
   ***/
  public DatagramPacket newDatagram() {
    byte[] data;

    data    = new byte[4];
    data[0] = 0; data[1] = (byte)_type;
    data[2] = (byte)((_blockNumber & 0xffff) >> 8);
    data[3] = (byte)(_blockNumber & 0xff);

    return  new DatagramPacket(data, data.length, _address, _port);
  }


  /***
   * Returns the block number of the acknowledgement.
   * <p>
   * @return The block number of the acknowledgement.
   ***/
  public int getBlockNumber() { return _blockNumber; }


  /*** Sets the block number of the acknowledgement.  ***/
  public void setBlockNumber(int blockNumber) { _blockNumber = blockNumber; }
}

