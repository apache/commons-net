/***
 * $RCSfile: echo.java,v $ $Revision: 1.1 $ $Date: 2002/04/03 01:04:23 $
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
 * This is an example program demonstrating how to use the EchoTCPClient
 * and EchoUDPClient classes.  This program connects to the default echo
 * service port of a specified server, then reads lines from standard
 * input, writing them to the echo server, and then printing the echo.
 * The default is to use the TCP port.  Use the -udp flag to use the UDP
 * port.
 * <p>
 * Usage: echo [-udp] <hostname>
 * <p>
 ***/
public final class echo {

  public static final void echoTCP(String host) throws IOException {
    EchoTCPClient client = new EchoTCPClient();
    BufferedReader input, echoInput;
    PrintWriter echoOutput;
    String line;

    // We want to timeout if a response takes longer than 60 seconds
    client.setDefaultTimeout(60000);
    client.connect(host);
    System.out.println("Connected to " + host + ".");
    input = new BufferedReader(new InputStreamReader(System.in));
    echoOutput =
      new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
    echoInput =
      new BufferedReader(new InputStreamReader(client.getInputStream()));

    while((line = input.readLine()) != null) {
      echoOutput.println(line);
      System.out.println(echoInput.readLine());
    }

    client.disconnect();
  }

  public static final void echoUDP(String host) throws IOException {
    int length, count;
    byte[] data;
    String line;
    BufferedReader input;
    InetAddress address;
    EchoUDPClient client;

    input = new BufferedReader(new InputStreamReader(System.in));
    address = InetAddress.getByName(host);
    client = new EchoUDPClient();

    client.open();
    // If we don't receive an echo within 5 seconds, assume the packet is lost.
    client.setSoTimeout(5000);
    System.out.println("Ready to echo to " + host + ".");

    // Remember, there are no guarantees about the ordering of returned
    // UDP packets, so there is a chance the output may be jumbled.
    while((line = input.readLine()) != null) {
      data = line.getBytes();
      client.send(data, address);
      count = 0;
	do {
	  try {
	    length = client.receive(data);
	  }
	  // Here we catch both SocketException and InterruptedIOException,
	  // because even though the JDK 1.1 docs claim that
	  // InterruptedIOException is thrown on a timeout, it seems
	  // SocketException is also thrown.
	  catch(SocketException e) {
	    // We timed out and assume the packet is lost.
	    System.err.println(
		       "SocketException: Timed out and dropped packet");
	    break;
	  } catch(InterruptedIOException e) {
	    // We timed out and assume the packet is lost.
	    System.err.println(
		       "InterruptedIOException: Timed out and dropped packet");
	    break;
	  }
	  System.out.print(new String(data, 0, length));
	  count+=length;
	} while(count < data.length);

      System.out.println();
    }

    client.close();
  }


  public static final void main(String[] args) {

    if(args.length == 1) {
      try {
	echoTCP(args[0]);
      } catch(IOException e) {
	e.printStackTrace();
	System.exit(1);
      }
    } else if(args.length == 2 && args[0].equals("-udp")) {
      try {
	echoUDP(args[1]);
      } catch(IOException e) {
	e.printStackTrace();
	System.exit(1);
      }
    } else {
      System.err.println("Usage: echo [-udp] <hostname>");
      System.exit(1);
    }

  }

}

