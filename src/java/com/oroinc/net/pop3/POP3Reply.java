/***
 * $Id: POP3Reply.java,v 1.1 2002/04/03 01:04:33 brekke Exp $
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
 * POP3Reply stores POP3 reply code constants.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 ***/

public final class POP3Reply {
  /*** The reply code indicating success of an operation. ***/
  public static int OK    = 0;

  /*** The reply code indicating failure of an operation. ***/
  public static int ERROR = 1;

  // Cannot be instantiated.
  private POP3Reply() {}
}
