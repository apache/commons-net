/***
 * $RCSfile: chargen.java,v $ $Revision: 1.1 $ $Date: 2002/04/03 01:04:25 $
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

import java.io.*;
import java.net.*;

import com.oroinc.net.*;

/***
 * This is an example program demonstrating how to use the CharGenTCPClient
 * and CharGenUDPClient classes.  This program connects to the default
 * chargen service port of a specified server, then reads 100 lines from
 * of generated output, writing each line to standard output, and then
 * closes the connection.  The UDP invocation of the program sends 50
 * datagrams, printing the reply to each.
 * The default is to use the TCP port.  Use the -udp flag to use the UDP
 * port.
 * <p>
 * Usage: chargen [-udp] <hostname>
 * <p>
 ***/
public final class chargen {

  public static final void chargenTCP(String host) throws IOException {
    int lines = 100;
    String line;
    CharGenTCPClient client = new CharGenTCPClient();
    BufferedReader chargenInput;

    // We want to timeout if a response takes longer than 60 seconds
    client.setDefaultTimeout(60000);
    client.connect(host);
    chargenInput =
      new BufferedReader(new InputStreamReader(client.getInputStream()));

    // We assume the chargen service outputs lines, but it really doesn't
    // have to, so this code might actually not work if no newlines are
    // present.
    while(lines-- > 0) {
      if((line = chargenInput.readLine()) == null)
	break;
      System.out.println(line);
    }

    client.disconnect();
  }

  public static final void chargenUDP(String host) throws IOException {
    int packets = 50;
    byte[] data;
    InetAddress address;
    CharGenUDPClient client;

    address = InetAddress.getByName(host);
    client  = new CharGenUDPClient();

    client.open();
    // If we don't receive a return packet within 5 seconds, assume
    // the packet is lost.
    client.setSoTimeout(5000);

    while(packets-- > 0) {
      client.send(address);

      try {
	data = client.receive();
      }
      // Here we catch both SocketException and InterruptedIOException,
      // because even though the JDK 1.1 docs claim that
      // InterruptedIOException is thrown on a timeout, it seems
      // SocketException is also thrown.
      catch(SocketException e) {
	// We timed out and assume the packet is lost.
	System.err.println("SocketException: Timed out and dropped packet");
	continue;
      } catch(InterruptedIOException e) {
	// We timed out and assume the packet is lost.
	System.err.println(
	   "InterruptedIOException: Timed out and dropped packet");
	continue;
      }
      System.out.write(data);
      System.out.flush();
   }

    client.close();
  }


  public static final void main(String[] args) {

    if(args.length == 1) {
      try {
	chargenTCP(args[0]);
      } catch(IOException e) {
	e.printStackTrace();
	System.exit(1);
      }
    } else if(args.length == 2 && args[0].equals("-udp")) {
      try {
	chargenUDP(args[1]);
      } catch(IOException e) {
	e.printStackTrace();
	System.exit(1);
      }
    } else {
      System.err.println("Usage: chargen [-udp] <hostname>");
      System.exit(1);
    }

  }

}

