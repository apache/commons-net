/***
 * $RCSfile: daytime.java,v $ $Revision: 1.1 $ $Date: 2002/04/03 01:04:23 $
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
 * This is an example program demonstrating how to use the DaytimeTCP
 * and DaytimeUDP classes.
 * This program connects to the default daytime service port of a
 * specified server, retrieves the daytime, and prints it to standard output.
 * The default is to use the TCP port.  Use the -udp flag to use the UDP
 * port.
 * <p>
 * Usage: daytime [-udp] <hostname>
 * <p>
 ***/
public final class daytime {

  public static final void daytimeTCP(String host) throws IOException {
    DaytimeTCPClient client = new DaytimeTCPClient();

    // We want to timeout if a response takes longer than 60 seconds
    client.setDefaultTimeout(60000);
    client.connect(host);
    System.out.println(client.getTime().trim());
    client.disconnect();
  }

  public static final void daytimeUDP(String host) throws IOException {
    DaytimeUDPClient client = new DaytimeUDPClient();

    // We want to timeout if a response takes longer than 60 seconds
    client.setDefaultTimeout(60000);
    client.open();
    System.out.println(client.getTime(InetAddress.getByName(host)).trim());
    client.close();
  }


  public static final void main(String[] args) {

    if(args.length == 1) {
      try {
	daytimeTCP(args[0]);
      } catch(IOException e) {
	e.printStackTrace();
	System.exit(1);
      }
    } else if(args.length == 2 && args[0].equals("-udp")) {
      try {
	daytimeUDP(args[1]);
      } catch(IOException e) {
	e.printStackTrace();
	System.exit(1);
      }
    } else {
      System.err.println("Usage: daytime [-udp] <hostname>");
      System.exit(1);
    }

  }

}

