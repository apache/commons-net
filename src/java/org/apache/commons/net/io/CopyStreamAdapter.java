package org.apache.commons.net.io;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.Enumeration;
import org.apache.commons.net.util.ListenerList;

/**
 * The CopyStreamAdapter will relay CopyStreamEvents to a list of listeners
 * when either of its bytesTransferred() methods are called.  Its purpose
 * is to facilitate the notification of the progress of a copy operation
 * performed by one of the static copyStream() methods in
 * org.apache.commons.io.Util to multiple listeners.  The static
 * copyStream() methods invoke the
 * bytesTransfered(long, int) of a CopyStreamListener for performance
 * reasons and also because multiple listeners cannot be registered given
 * that the methods are static.
 * <p>
 * <p>
 * @see CopyStreamEvent
 * @see CopyStreamListener
 * @see Util
 * @author <a href="mailto:savarese@apache.org">Daniel F. Savarese</a>
 * @version $Id: CopyStreamAdapter.java,v 1.7 2004/01/01 21:04:20 scohen Exp $
 */
public class CopyStreamAdapter implements CopyStreamListener
{
    private ListenerList internalListeners;

    /**
     * Creates a new copyStreamAdapter.
     */
    public CopyStreamAdapter()
    {
        internalListeners = new ListenerList();
    }

    /**
     * This method is invoked by a CopyStreamEvent source after copying
     * a block of bytes from a stream.  The CopyStreamEvent will contain
     * the total number of bytes transferred so far and the number of bytes
     * transferred in the last write.  The CopyStreamAdapater will relay
     * the event to all of its registered listeners, listing itself as the
     * source of the event.
     * @param event The CopyStreamEvent fired by the copying of a block of
     *              bytes.
     */
    public void bytesTransferred(CopyStreamEvent event)
    {
        bytesTransferred(event.getTotalBytesTransferred(),
                         event.getBytesTransferred(),
                         event.getStreamSize());
    }

    /**
     * This method is not part of the JavaBeans model and is used by the
     * static methods in the org.apache.commons.io.Util class for efficiency.
     * It is invoked after a block of bytes to inform the listener of the
     * transfer.  The CopyStreamAdapater will create a CopyStreamEvent
     * from the arguments and relay the event to all of its registered
     * listeners, listing itself as the source of the event.
     * @param totalBytesTransferred  The total number of bytes transferred
     *         so far by the copy operation.
     * @param bytesTransferred  The number of bytes copied by the most recent
     *          write.
     * @param streamSize The number of bytes in the stream being copied.
     *        This may be equal to CopyStreamEvent.UNKNOWN_STREAM_SIZE if
     *        the size is unknown.
     */
    public void bytesTransferred(long totalBytesTransferred,
                                 int bytesTransferred, long streamSize)
    {
        Enumeration listeners;
        CopyStreamEvent event;

        listeners = internalListeners.getListeners();

        event = new CopyStreamEvent(this,
                                    totalBytesTransferred,
                                    bytesTransferred,
                                    streamSize);

        while (listeners.hasMoreElements())
        {
            ((CopyStreamListener) (listeners.nextElement())).
                bytesTransferred(event);
        }
    }

    /**
     * Registers a CopyStreamListener to receive CopyStreamEvents.
     * Although this method is not declared to be synchronized, it is
     * implemented in a thread safe manner.
     * @param listener  The CopyStreamlistener to register.
     */
    public void addCopyStreamListener(CopyStreamListener listener)
    {
        internalListeners.addListener(listener);
    }

    /**
     * Unregisters a CopyStreamListener.  Although this method is not
     * synchronized, it is implemented in a thread safe manner.
     * @param listener  The CopyStreamlistener to unregister.
     */
    public void removeCopyStreamListener(CopyStreamListener listener)
    {
        internalListeners.removeListener(listener);
    }
}
