/***
 * $Id: CopyStreamAdapter.java,v 1.1 2002/04/03 01:04:41 brekke Exp $
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

import com.oroinc.util.ListenerList;

/***
 * The CopyStreamAdapter will relay CopyStreamEvents to a list of listeners
 * when either of its bytesTransferred() methods are called.  Its purpose
 * is to facilitate the notification of the progress of a copy operation
 * performed by one of the static copyStream() methods in com.oroinc.io.Util
 * to multiple listeners.  The static copyStream() methods invoke the
 * bytesTransfered(long, int) of a CopyStreamListener for performance
 * reasons and also because multiple listeners cannot be registered given
 * that the methods are static.
 * <p>
 * <p>
 * @see CopyStreamEvent
 * @see CopyStreamListener
 * @see Util
 * @author Daniel F. Savarese
 ***/

public class CopyStreamAdapter implements CopyStreamListener {
  private ListenerList __listeners;

  /***
   * Creates a new copyStreamAdapter.
   ***/
  public CopyStreamAdapter() {
    __listeners = new ListenerList();
  }

  /***
   * This method is invoked by a CopyStreamEvent source after copying
   * a block of bytes from a stream.  The CopyStreamEvent will contain
   * the total number of bytes transferred so far and the number of bytes
   * transferred in the last write.  The CopyStreamAdapater will relay
   * the event to all of its registered listeners, listing itself as the
   * source of the event.
   * <p>
   * @param event The CopyStreamEvent fired by the copying of a block of
   *              bytes.
   ***/
  public void bytesTransferred(CopyStreamEvent event) {
    bytesTransferred(event.getTotalBytesTransferred(),
		     event.getBytesTransferred(),
		     event.getStreamSize());
  }


  /***
   * This method is not part of the JavaBeans model and is used by the
   * static methods in the com.oroinc.io.Util class for efficiency.
   * It is invoked after a block of bytes to inform the listener of the
   * transfer.  The CopyStreamAdapater will create a CopyStreamEvent
   * from the arguments and relay the event to all of its registered
   * listeners, listing itself as the source of the event.
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
			       int bytesTransferred, long streamSize)
  {
    Enumeration listeners;
    CopyStreamEvent event;

    listeners = __listeners.getListeners();

    event = new CopyStreamEvent(this, totalBytesTransferred, bytesTransferred,
				streamSize);

    while(listeners.hasMoreElements())
      ((CopyStreamListener)(listeners.nextElement())).bytesTransferred(event);
  }


  /***
   * Registers a CopyStreamListener to receive CopyStreamEvents.
   * Although this method is not declared to be synchronized, it is
   * implemented in a thread safe manner.
   * <p>
   * @param listener  The CopyStreamlistener to register.
   ***/
  public void addCopyStreamListener(CopyStreamListener listener) {
    __listeners.addListener(listener);
  }

  /***
   * Unregisters a CopyStreamListener.  Although this method is not
   * synchronized, it is implemented in a thread safe manner.
   * <p>
   * @param listener  The CopyStreamlistener to unregister.
   ***/
  public void removeCopyStreamListener(CopyStreamListener listener){
    __listeners.removeListener(listener);
  }
}
