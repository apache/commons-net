/***
 * $RCSfile: finger.java,v $ $Revision: 1.1 $ $Date: 2002/04/03 01:04:23 $
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
 * This is an example of how you would implement the finger command
 * in Java using NetComponents.  The Java version is much shorter.
 * But keep in mind that the Unix finger command reads all sorts of
 * local files to output local finger information.  This program only
 * queries the finger daemon.
 * <p>
 * The -l flag is used to request long output from the server.
 * <p>
 ***/
public final class finger {

  public static final void main(String[] args) {
    boolean longOutput = false;
    int arg = 0, index;
    String handle, host;
    FingerClient finger;
    InetAddress address = null;

    // Get flags.  If an invalid flag is present, exit with usage message.
    while(arg < args.length && args[arg].startsWith("-")) {
      if(args[arg].equals("-l"))
	longOutput = true;
      else {
	System.err.println("usage: finger [-l] [[[handle][@<server>]] ...]");
	System.exit(1);
      }
      ++arg;
    }


    finger = new FingerClient();
    // We want to timeout if a response takes longer than 60 seconds
    finger.setDefaultTimeout(60000);

    if(arg >= args.length) {
      // Finger local host

      try {
	address = InetAddress.getLocalHost();
      } catch(UnknownHostException e) {
	System.err.println("Error unknown host: " + e.getMessage());
	System.exit(1);
      }

      try {
	finger.connect(address);
	System.out.print(finger.query(longOutput));
	finger.disconnect();
      } catch(IOException e) {
	System.err.println("Error I/O exception: " + e.getMessage());
	System.exit(1);
      }

      return;
    }

    // Finger each argument
    while(arg < args.length) {

      index = args[arg].lastIndexOf("@");

      if(index == -1) {
	handle = args[arg];
	try {
	  address = InetAddress.getLocalHost();
	} catch(UnknownHostException e) {
	  System.err.println("Error unknown host: " + e.getMessage());
	  System.exit(1);
	}
      } else {
	handle = args[arg].substring(0, index);
	host   = args[arg].substring(index + 1);

	try {
	  address = InetAddress.getByName(host);
	} catch(UnknownHostException e) {
	  System.err.println("Error unknown host: " + e.getMessage());
	  System.exit(1);
	}
      }
 
      System.out.println("[" + address.getHostName() + "]");

      try {
	finger.connect(address);
	System.out.print(finger.query(longOutput, handle));
	finger.disconnect();
      } catch(IOException e) {
	System.err.println("Error I/O exception: " + e.getMessage());
	System.exit(1);
      }

      ++arg;
      if(arg != args.length)
	System.out.print("\n");
    }
  }
}

