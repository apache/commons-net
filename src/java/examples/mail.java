/***
 * $RCSfile: mail.java,v $ $Revision: 1.1 $ $Date: 2002/04/03 01:04:23 $
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
import java.util.*;

import com.oroinc.net.smtp.*;
import com.oroinc.io.*;

/***
 * This is an example program using the SMTP package to send a message
 * to the specified recipients.  It prompts you for header information and
 * a filename containing the message.
 * <p>
 ***/

public final class mail {

  public final static void main(String[] args) {
    String sender, recipient, subject, filename, server, cc;
    Vector ccList = new Vector();
    BufferedReader stdin;
    FileReader fileReader = null;
    Writer writer;
    SimpleSMTPHeader header;
    SMTPClient client;
    Enumeration enum;

    if(args.length < 1) {
      System.err.println("Usage: mail smtpserver");
      System.exit(1);
    }

    server = args[0];

    stdin = new BufferedReader(new InputStreamReader(System.in));

    try {
      System.out.print("From: ");
      System.out.flush();

      sender = stdin.readLine();

      System.out.print("To: ");
      System.out.flush();

      recipient = stdin.readLine();

      System.out.print("Subject: ");
      System.out.flush();

      subject = stdin.readLine();

      header = new SimpleSMTPHeader(sender, recipient, subject);


      while(true) {
	System.out.print("CC <enter one address per line, hit enter to end>: ");
	System.out.flush();

	// Of course you don't want to do this because readLine() may be null
	cc = stdin.readLine().trim();

	if(cc.length() == 0)
	  break;

	header.addCC(cc);
	ccList.addElement(cc);
      }

      System.out.print("Filename: ");
      System.out.flush();

      filename = stdin.readLine();

      try {
	fileReader = new FileReader(filename);
      } catch(FileNotFoundException e) {
	System.err.println("File not found. " + e.getMessage());
      }

      client = new SMTPClient();
      client.addProtocolCommandListener(new PrintCommandListener(
				        new PrintWriter(System.out)));

      client.connect(server);

      if(!SMTPReply.isPositiveCompletion(client.getReplyCode())) {
	client.disconnect();
	System.err.println("SMTP server refused connection.");
	System.exit(1);
      }

      client.login();

      client.setSender(sender);
      client.addRecipient(recipient);

      enum = ccList.elements();

      while(enum.hasMoreElements())
	client.addRecipient((String)enum.nextElement());

      writer = client.sendMessageData();

      if(writer != null) {
	writer.write(header.toString());
	Util.copyReader(fileReader, writer);
	writer.close();
	client.completePendingCommand();
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


