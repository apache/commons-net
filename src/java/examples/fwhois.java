/***
 * $RCSfile: fwhois.java,v $ $Revision: 1.1 $ $Date: 2002/04/03 01:04:25 $
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
 * This is an example of how you would implement the Linux fwhois command
 * in Java using NetComponents.  The Java version is much shorter.
 * <p>
 ***/
public final class fwhois {

  public static final void main(String[] args) {
    int index;
    String handle, host;
    InetAddress address = null;
    WhoisClient whois;

    if(args.length != 1) {
      System.err.println("usage: fwhois handle[@<server>]");
      System.exit(1);
    }

    index = args[0].lastIndexOf("@");

    whois = new WhoisClient();
    // We want to timeout if a response takes longer than 60 seconds
    whois.setDefaultTimeout(60000);

    if(index == -1) {
      handle = args[0];
      host   = WhoisClient.DEFAULT_HOST;
    } else {
      handle = args[0].substring(0, index);
      host   = args[0].substring(index + 1);
    }
 
    try {
      address = InetAddress.getByName(host);
    } catch(UnknownHostException e) {
      System.err.println("Error unknown host: " + e.getMessage());
      System.exit(1);
    }

    System.out.println("[" + address.getHostName() + "]");

    try {
      whois.connect(address);
      System.out.print(whois.query(handle));
      whois.disconnect();
    } catch(IOException e) {
      System.err.println("Error I/O exception: " + e.getMessage());
      System.exit(1);
    }
  }

}
