/***
 * $Id: CopyStreamListener.java,v 1.1 2002/04/03 01:04:41 brekke Exp $
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
 * The CopyStreamListener class can accept CopyStreamEvents to keep track
 * of the progress of a stream copying operation.  However, it is currently
 * not used that way within NetComponents for performance reasons.  Rather
 * the bytesTransferred(long, int) method is called directly rather than
 * passing an event to bytesTransferred(CopyStreamEvent), saving the creation
 * of a CopyStreamEvent instance.  Also, the only place where
 * CopyStreamListener is currently used within NetComponents is in the
 * static methods of the uninstantiable com.oroinc.io.Util class, which
 * would preclude the use of addCopyStreamListener and
 * removeCopyStreamListener methods.  However, future additions may use the
 * JavaBean event model, which is why the hooks have been included from the
 * beginning.
 * <p>
 * <p>
 * @see CopyStreamEvent
 * @see CopyStreamAdapter
 * @see Util
 * @author Daniel F. Savarese
 ***/

public interface CopyStreamListener extends EventListener {

  /***
   * This method is invoked by a CopyStreamEvent source after copying
   * a block of bytes from a stream.  The CopyStreamEvent will contain
   * the total number of bytes transferred so far and the number of bytes
   * transferred in the last write.
   * <p>
   * @param event The CopyStreamEvent fired by the copying of a block of
   *              bytes.
   ***/
  public void bytesTransferred(CopyStreamEvent event);


  /***
   * This method is not part of the JavaBeans model and is used by the
   * static methods in the com.oroinc.io.Util class for efficiency.
   * It is invoked after a block of bytes to inform the listener of the
   * transfer.
   * <p>
   * @param totalBytesTransferred  The total number of bytes transferred
   *         so far by the copy operation.
   * @param bytesTransferred  The number of bytes copied by the most recent
   *          write.
   * @param streamSize The number of bytes in the stream being copied.
   *        This may be equal to CopyStreamEvent.UNKNOWN_STREAM_SIZE if
   *        the size is unknown.
   ***/
  public void bytesTransferred(long totalBytesTransferred,
			       int bytesTransferred,
			       long streamSize);
}
