/***
 * $Id: RelayPath.java,v 1.1 2002/04/03 01:04:36 brekke Exp $
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

package com.oroinc.net.smtp;

import java.util.*;

/***
 * A class used to represent forward and reverse relay paths.  The
 * SMTP MAIL command requires a reverse relay path while the SMTP RCPT
 * command requires a forward relay path.  See RFC 821 for more details.
 * In general, you will not have to deal with relay paths.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see SMTPClient
 ***/

public final class RelayPath {
  Vector _path;
  String _emailAddress;

  /*** 
   * Create a relay path with the specified email address as the ultimate
   * destination.
   * <p>
   * @param emailAddress The destination email address.
   ***/
  public RelayPath(String emailAddress) {
    _path = new Vector();
    _emailAddress = emailAddress;
  }

  /***
   * Add a mail relay host to the relay path.  Hosts are added left to
   * right.  For example, the following will create the path
   * <code><b> &lt @bar.com,@foo.com:foobar@foo.com &gt </b></code>
   * <pre>
   * path = new RelayPath("foobar@foo.com");
   * path.addRelay("bar.com");
   * path.addRelay("foo.com");
   * </pre>
   * <p>
   * @param hostname The host to add to the relay path.
   ***/
  public void addRelay(String hostname) {
    _path.addElement(hostname);
  }

  /***
   * Return the properly formatted string representation of the relay path.
   * <p>
   * @return The properly formatted string representation of the relay path.
   ***/
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    Enumeration hosts;

    buffer.append('<');

    hosts = _path.elements();

    if(hosts.hasMoreElements()) {
      buffer.append('@');
      buffer.append((String)hosts.nextElement());

      while(hosts.hasMoreElements()) {
	buffer.append(",@");
	buffer.append((String)hosts.nextElement());
      }
      buffer.append(':');
    }

    buffer.append(_emailAddress);
    buffer.append('>');

    return buffer.toString();
  }

}
