/***
 * $RCSfile: server2serverFTP.java,v $ $Revision: 1.1 $ $Date: 2002/04/03 01:04:23 $
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

import com.oroinc.net.ProtocolCommandListener;
import com.oroinc.net.ftp.*;

/***
 * This is an example program demonstrating how to use the FTPClient class.
 * This program arranges a server to server file transfer that transfers
 * a file from host1 to host2.  Keep in mind, this program might only work
 * if host2 is the same as the host you run it on (for security reasons, 
 * some ftp servers only allow PORT commands to be issued with a host
 * argument equal to the client host).
 * <p>
 * Usage: ftp <host1> <user1> <pass1> <file1> <host2> <user2> <pass2> <file2>
 * <p>
 ***/
public final class server2serverFTP {

  public static final void main(String[] args) {
    String server1, username1, password1, file1;
    String server2, username2, password2, file2;
    FTPClient ftp1, ftp2;
    ProtocolCommandListener listener;

    if(args.length < 8) {
      System.err.println(
 "Usage: ftp <host1> <user1> <pass1> <file1> <host2> <user2> <pass2> <file2>"
 );  
      System.exit(1);
    }

    server1   = args[0];
    username1 = args[1];
    password1 = args[2];
    file1     = args[3];
    server2   = args[4];
    username2 = args[5];
    password2 = args[6];
    file2     = args[7];

    listener = new PrintCommandListener(new PrintWriter(System.out));
    ftp1 = new FTPClient();
    ftp1.addProtocolCommandListener(listener);
    ftp2 = new FTPClient();
    ftp2.addProtocolCommandListener(listener);

    try {
      int reply;
      ftp1.connect(server1);
      System.out.println("Connected to " + server1 + ".");

      reply = ftp1.getReplyCode();

      if(!FTPReply.isPositiveCompletion(reply)) {
	ftp1.disconnect();
	System.err.println("FTP server1 refused connection.");
	System.exit(1);
      }
    } catch(IOException e) {
      if(ftp1.isConnected()) {
	try {
	  ftp1.disconnect();
	} catch(IOException f) {
	  // do nothing
	}
      }
      System.err.println("Could not connect to server1.");
      e.printStackTrace();
      System.exit(1);
    }

    try {
      int reply;
      ftp2.connect(server2);
      System.out.println("Connected to " + server2 + ".");

      reply = ftp2.getReplyCode();

      if(!FTPReply.isPositiveCompletion(reply)) {
	ftp2.disconnect();
	System.err.println("FTP server2 refused connection.");
	System.exit(1);
      }
    } catch(IOException e) {
      if(ftp2.isConnected()) {
	try {
	  ftp2.disconnect();
	} catch(IOException f) {
	  // do nothing
	}
      }
      System.err.println("Could not connect to server2.");
      e.printStackTrace();
      System.exit(1);
    }

  __main:
    try {
      if(!ftp1.login(username1, password1)){
	System.err.println("Could not login to " + server1);
	break __main;
      }

      if(!ftp2.login(username2, password2)) {
	System.err.println("Could not login to " + server2);
	break __main;
      }

      // Let's just assume success for now.
      ftp2.enterRemotePassiveMode();

      ftp1.enterRemoteActiveMode(InetAddress.getByName(ftp2.getPassiveHost()),
				 ftp2.getPassivePort());

      // Although you would think the store command should be sent to server2
      // first, in reality, ftp servers like wu-ftpd start accepting data
      // connections right after entering passive mode.  Additionally, they
      // don't even send the positive preliminary reply until after the
      // transfer is completed (in the case of passive mode transfers).
      // Therefore, calling store first would hang waiting for a preliminary
      // reply.
      if(ftp1.remoteRetrieve(file1) && ftp2.remoteStoreUnique(file2)) {
      //      if(ftp1.remoteRetrieve(file1) && ftp2.remoteStore(file2)) {
	// We have to fetch the positive completion reply.
	ftp1.completePendingCommand();
	ftp2.completePendingCommand();
      } else {
	System.err.println(
	   "Couldn't initiate transfer.  Check that filenames are valid.");
	break __main;
      }

    } catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    } finally {
      try {
	if(ftp1.isConnected()) {
	  ftp1.logout();
	  ftp1.disconnect();
	}
      } catch(IOException e) {
	// do nothing
      }

      try {
	if(ftp2.isConnected()) {
	  ftp2.logout();
	  ftp2.disconnect();
	}
      } catch(IOException e) {
	// do nothing
      }
    }
  }
}
