/***
 * $Id: CopyStreamException.java,v 1.1 2002/04/03 01:04:41 brekke Exp $
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


package com.oroinc.io;

import java.io.*;


/***
 * The CopyStreamException class is thrown by the com.oroinc.io.Util
 * copyStream() methods.  It stores the number of bytes confirmed to
 * have been transferred before an I/O error as well as the IOException
 * responsible for the failure of a copy operation.
 * <p>
 * <p>
 * @see Util
 * @author Daniel F. Savarese
 ***/

public class CopyStreamException extends IOException {
  private long __bytesTransferred;
  private IOException __exception;

  /***
   * Creates a new CopyStreamException instance.
   * <p>
   * @param message  A message describing the error.
   * @param bytesTransferred  The total number of bytes transferred before
   *        an exception was thrown in a copy operation.
   * @param exception  The IOException thrown during a copy operation.
   ***/
  public CopyStreamException(String message, long bytesTransferred,
			     IOException exception)
  {
    super(message);
    __bytesTransferred = bytesTransferred;
    __exception = exception;
  }

  /***
   * Returns the total number of bytes confirmed to have been transferred by a 
   * failed copy operation.
   * <p>
   * @return The total number of bytes confirmed to have been transferred by a 
   * failed copy operation.
   ***/
  public long getTotalBytesTransferred() { return __bytesTransferred; }


  /***
   * Returns the IOException responsible for the failure of a copy operation.
   * <p>
   * @return The IOException responsible for the failure of a copy operation.
   ***/
  public IOException getIOException() { return __exception; }

}
