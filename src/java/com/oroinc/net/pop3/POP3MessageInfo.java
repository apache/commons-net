/***
 * $Id: POP3MessageInfo.java,v 1.1 2002/04/03 01:04:33 brekke Exp $
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

package com.oroinc.net.pop3;

import java.io.*;
import java.net.*;
import java.util.*;

/***
 * POP3MessageInfo is used to return information about messages stored on
 * a POP3 server.  Its fields are used to mean slightly different things
 * depending on the information being returned.
 * <p>
 * In response to a status command, <code> number </code>
 * contains the number of messages in the mailbox, <code> size </code> 
 * contains the size of the mailbox in bytes, and <code> identifier </code>
 * is null.
 * <p>
 * In response to a message listings, <code> number </code>
 * contains the message number, <code> size </code> contains the
 * size of the message in bytes, and <code> identifier </code> is null.
 * <p>
 * In response to unique identifier listings, <code> number </code> contains
 * the message number, <code> size </code> is undefined, and
 * <code> identifier </code> contains the message's unique identifier.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class POP3MessageInfo {
  public int number;
  public int size;
  public String identifier;

  /***
   * Creates a POP3MessageInfo instance with <code>number</code> and
   * <code> size </code> set to 0, and <code>identifier</code> set to
   * null.
   ***/
  public POP3MessageInfo() {
    number = size = 0;
    identifier = null;
  }

  /***
   * Creates a POP3MessageInfo instance with <code>number</code> set
   * to <code> num </code>, <code> size </code> set to <code> octets </code>,
   * and <code>identifier</code> set to null.
   ***/
  public POP3MessageInfo(int num, int octets) {
    number     = num;
    size       = octets;
    identifier = null;
  }

  /***
   * Creates a POP3MessageInfo instance with <code>number</code> set
   * to <code> num </code>, <code> size </code> undefined,
   * and <code>identifier</code> set to <code>uid</code>.
   ***/
  public POP3MessageInfo(int num, String uid) {
    number     = num;
    size       = -1;
    identifier = uid;
  }
}
