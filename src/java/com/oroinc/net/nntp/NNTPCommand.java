/***
 * $Id: NNTPCommand.java,v 1.1 2002/04/03 01:04:33 brekke Exp $
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

package com.oroinc.net.nntp;

import java.net.*;
import java.io.*;

/***
 * NNTPCommand stores a set of constants for NNTP command codes.  To interpret
 * the meaning of the codes, familiarity with RFC 977 is assumed.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class NNTPCommand {

  public static final int ARTICLE   = 0;
  public static final int BODY      = 1;
  public static final int GROUP     = 2;
  public static final int HEAD      = 3;
  public static final int HELP      = 4;
  public static final int IHAVE     = 5;
  public static final int LAST      = 6;
  public static final int LIST      = 7;
  public static final int NEWGROUPS = 8;
  public static final int NEWNEWS   = 9;
  public static final int NEXT      = 10;
  public static final int POST      = 11;
  public static final int QUIT      = 12;
  public static final int SLAVE     = 13;
  public static final int STAT      = 14;


  // Cannot be instantiated
  private NNTPCommand() {}

  static final String[] _commands =  {
    "ARTICLE", "BODY", "GROUP", "HEAD", "HELP", "IHAVE", "LAST", "LIST",
    "NEWGROUPS", "NEWNEWS", "NEXT", "POST", "QUIT", "SLAVE", "STAT"
  };


  /***
   * Retrieve the NNTP protocol command string corresponding to a specified
   * command code.
   * <p>
   * @param The command code.
   * @return The NNTP protcol command string corresponding to a specified
   *         command code.
   ***/
  public static final String getCommand(int command) {
    return _commands[command];
  }

}
