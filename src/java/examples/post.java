/***
 * $RCSfile: post.java,v $ $Revision: 1.1 $ $Date: 2002/04/03 01:04:25 $
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
 * This is an example program using the NNTP package to post an article
 * to the specified newsgroup(s).  It prompts you for header information and
 * a filename to post.
 * <p>
 ***/

public final class post {

  public final static void main(String[] args) {
    String from, subject, newsgroup, filename, server, organization;
    String references;
    BufferedReader stdin;
    FileReader fileReader = null;
    SimpleNNTPHeader header;
    NNTPClient client;

    if(args.length < 1) {
      System.err.println("Usage: post newsserver");
      System.exit(1);
    }

    server = args[0];

    stdin = new BufferedReader(new InputStreamReader(System.in));

    try {
      System.out.print("From: ");
      System.out.flush();

      from = stdin.readLine();

      System.out.print("Subject: ");
      System.out.flush();

      subject = stdin.readLine();

      header = new SimpleNNTPHeader(from, subject);

      System.out.print("Newsgroup: ");
      System.out.flush();

      newsgroup = stdin.readLine();
      header.addNewsgroup(newsgroup);

      while(true) {
	System.out.print("Additional Newsgroup <Hit enter to end>: ");
	System.out.flush();

	// Of course you don't want to do this because readLine() may be null
	newsgroup = stdin.readLine().trim();

	if(newsgroup.length() == 0)
	  break;

	header.addNewsgroup(newsgroup);
      }

      System.out.print("Organization: ");
      System.out.flush();

      organization = stdin.readLine();

      System.out.print("References: ");
      System.out.flush();

      references = stdin.readLine();

      if(organization != null && organization.length() > 0)
	header.addHeaderField("Organization", organization);

      if(references != null && organization.length() > 0)
	header.addHeaderField("References", references);

      header.addHeaderField("X-Newsreader", "NetComponents");

      System.out.print("Filename: ");
      System.out.flush();

      filename = stdin.readLine();

      try {
	fileReader = new FileReader(filename);
      } catch(FileNotFoundException e) {
	System.err.println("File not found. " + e.getMessage());
	System.exit(1);
      }

      client = new NNTPClient();
      client.addProtocolCommandListener(new PrintCommandListener(
					 new PrintWriter(System.out)));

      client.connect(server);

      if(!NNTPReply.isPositiveCompletion(client.getReplyCode())) {
	client.disconnect();
	System.err.println("NNTP server refused connection.");
	System.exit(1);
      }

      if(client.isAllowedToPost()) {
	Writer writer = client.postArticle();

	if(writer != null) {
	  writer.write(header.toString());
	  Util.copyReader(fileReader, writer);
	  writer.close();
	  client.completePendingCommand();
	}
      }

      fileReader.close();

      client.logout();

      client.disconnect();
    } catch(IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}


