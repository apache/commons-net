/***
 * $RCSfile: newsgroups.java,v $ $Revision: 1.1 $ $Date: 2002/04/03 01:04:25 $
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

import com.oroinc.net.nntp.*;
import com.oroinc.io.*;

/***
 * This is a trivial example using the NNTP package to approximate the
 * Unix newsgroups command.  It merely connects to the specified news
 * server and issues fetches the list of newsgroups stored by the server.
 * On servers that store a lot of newsgroups, this command can take a very
 * long time (listing upwards of 30,000 groups).
 * <p>
 ***/

public final class newsgroups {

  public final static void main(String[] args) {
    NNTPClient client;
    NewsgroupInfo[] list;

    if(args.length < 1) {
      System.err.println("Usage: newsgroups newsserver");
      System.exit(1);
    }

    client = new NNTPClient();

    try {
      client.connect(args[0]);

      list = client.listNewsgroups();

      if(list != null) {
	for(int i=0; i < list.length; i++)
	  System.out.println(list[i].getNewsgroup());
      } else {
	System.err.println("LIST command failed.");
	System.err.println("Server reply: " + client.getReplyString());
      }
    } catch(IOException e) {
      e.printStackTrace();
    } finally {
      try {
	if(client.isConnected())
	  client.disconnect();
      } catch(IOException e) {
	System.err.println("Error disconnecting from server.");
	e.printStackTrace();
	System.exit(1);
      }
    }

  }

}


