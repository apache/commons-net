/***
 * $RCSfile: rexec.java,v $ $Revision: 1.1 $ $Date: 2002/04/03 01:04:25 $
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

import com.oroinc.net.bsd.*;


/***
 * This is an example program demonstrating how to use the RExecClient class.
 * This program connects to an rexec server and requests that the
 * given command be executed on the server.  It then reads input from stdin
 * (this will be line buffered on most systems, so don't expect character
 * at a time interactivity), passing it to the remote process and writes
 * the process stdout and stderr to local stdout.
 * <p>
 * Example: java rexec myhost myusername mypassword "ps -aux"
 * <p>
 * Usage: rexec <hostname> <username> <password> <command>
 * <p>
 ***/

// This class requires the IOUtil support class!
public final class rexec {

  public static final void main(String[] args) {
    String server, username, password, command;
    RExecClient client;

    if(args.length != 4) {
      System.err.println(
		 "Usage: rexec <hostname> <username> <password> <command>");
      System.exit(1);
      return; // so compiler can do proper flow control analysis
    }

    client = new RExecClient();

    server   = args[0];
    username = args[1];
    password = args[2];
    command  = args[3];

    try {
      client.connect(server);
    } catch(IOException e) {
      System.err.println("Could not connect to server.");
      e.printStackTrace();
      System.exit(1);
    }

    try {
      client.rexec(username, password, command);
    } catch(IOException e) {
      try {
	client.disconnect();
      } catch(IOException f) {
      }
      e.printStackTrace();
      System.err.println("Could not execute command.");
      System.exit(1);
    }


    IOUtil.readWrite(client.getInputStream(), client.getOutputStream(),
		     System.in, System.out);

    try {
      client.disconnect();
    } catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    }

    System.exit(0);
  }

}

