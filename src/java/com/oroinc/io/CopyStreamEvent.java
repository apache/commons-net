/***
 * $Id: CopyStreamEvent.java,v 1.1 2002/04/03 01:04:41 brekke Exp $
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

import java.util.*;

/***
 * A CopyStreamEvent is triggered after every write performed by a
 * stream copying operation.  The event stores the number of bytes
 * transferred by the write triggering the event as well as the total
 * number of bytes transferred so far by the copy operation.
 * <p>
 * <p>
 * @see CopyStreamListener
 * @see CopyStreamAdapter
 * @see Util
 * @author Daniel F. Savarese
 ***/

public class CopyStreamEvent extends EventObject {
  public static final long UNKNOWN_STREAM_SIZE = -1;

  private int __bytesTransferred;
  private long __totalBytesTransferred, __streamSize;

  /***
   * Creates a new CopyStreamEvent instance.
   * <p>
   * @param source  The source of the event.
   * @param totalBytesTransferred The total number of bytes transferred so
   *   far during a copy operation.
   * @param bytesTransferred  The number of bytes transferred during the
   *        write that triggered the CopyStreamEvent.
   * @param streamSize  The number of bytes in the stream being copied.
   *          This may be set to <code>UNKNOWN_STREAM_SIZE</code> if the
   *          size is unknown.
   ***/
  public CopyStreamEvent(Object source, long totalBytesTransferred,
			 int bytesTransferred, long streamSize)
  {
    super(source);
    __bytesTransferred = bytesTransferred;
    __totalBytesTransferred = totalBytesTransferred;
  }

  /***
   * Returns the number of bytes transferred by the write that triggered
   * the event.
   * <p>
   * @return The number of bytes transferred by the write that triggered
   * the vent.
   ***/
  public int getBytesTransferred() { return __bytesTransferred; }

  /***
   * Returns the total number of bytes transferred so far by the copy
   * operation.
   * <p>
   * @return The total number of bytes transferred so far by the copy
   * operation.
   ***/
  public long getTotalBytesTransferred() { return __totalBytesTransferred; }

  /***
   * Returns the size of the stream being copied.
   * This may be set to <code>UNKNOWN_STREAM_SIZE</code> if the
   * size is unknown.
   * <p>
   * @return The size of the stream being copied.
   ***/
  public long getStreamSize() { return __streamSize; }

}
