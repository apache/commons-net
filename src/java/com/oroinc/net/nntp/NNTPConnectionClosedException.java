/***
 * $Id: NNTPConnectionClosedException.java,v 1.1 2002/04/03 01:04:36 brekke Exp $
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

import java.io.*;

/***
 * NNTPConnectionClosedException is used to indicate the premature or
 * unexpected closing of an NNTP connection resulting from a 
 * <a href="com.oroinc.net.nntp.NNTPReply.html#SERVICE_DISCONTINUED">
 * NNTPReply.SERVICE_DISCONTINUED </a> response (NNTP reply code 400) to a
 * failed NNTP command.  This exception is derived from IOException and
 * therefore may be caught either as an IOException or specifically as an
 * NNTPConnectionClosedException.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see NNTP
 * @see NNTPClient
 ***/

public final class NNTPConnectionClosedException extends IOException {

  /*** Constructs a NNTPConnectionClosedException with no message ***/
  public NNTPConnectionClosedException() {
    super();
  }

  /*** 
   * Constructs a NNTPConnectionClosedException with a specified message.
   * <p>
   * @param message  The message explaining the reason for the exception.
   ***/
  public NNTPConnectionClosedException(String message) {
    super(message);
  }

}
