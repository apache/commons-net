package org.apache.commons.io;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 *    "Apache Turbine", nor may "Apache" appear in their name, without
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

import java.util.EventObject;

/**
 * A CopyStreamEvent is triggered after every write performed by a
 * stream copying operation.  The event stores the number of bytes
 * transferred by the write triggering the event as well as the total
 * number of bytes transferred so far by the copy operation.
 * <p>
 * <p>
 * @see CopyStreamListener
 * @see CopyStreamAdapter
 * @see Util
 * @author <a href="mailto:savarese@apache.org">Daniel F. Savarese</a>
 * @version $Id: CopyStreamEvent.java,v 1.4 2003/01/26 00:21:41 dfs Exp $
 */
public class CopyStreamEvent extends EventObject
{
    /**
     * Constant used to indicate the stream size is unknown.
     */
    public static final long UNKNOWN_STREAM_SIZE = -1;

    private int bytesTransferred;
    private long totalBytesTransferred;
    private long streamSize;

    /**
     * Creates a new CopyStreamEvent instance.
     * @param source  The source of the event.
     * @param totalBytesTransferred The total number of bytes transferred so
     *   far during a copy operation.
     * @param bytesTransferred  The number of bytes transferred during the
     *        write that triggered the CopyStreamEvent.
     * @param streamSize  The number of bytes in the stream being copied.
     *          This may be set to <code>UNKNOWN_STREAM_SIZE</code> if the
     *          size is unknown.
     */
    public CopyStreamEvent(Object source, long totalBytesTransferred,
                           int bytesTransferred, long streamSize)
    {
        super(source);
        this.bytesTransferred = bytesTransferred;
        this.totalBytesTransferred = totalBytesTransferred;
        this.streamSize = streamSize;
    }

    /**
     * Returns the number of bytes transferred by the write that triggered
     * the event.
     * @return The number of bytes transferred by the write that triggered
     * the vent.
     */
    public int getBytesTransferred()
    {
        return bytesTransferred;
    }

    /**
     * Returns the total number of bytes transferred so far by the copy
     * operation.
     * @return The total number of bytes transferred so far by the copy
     * operation.
     */
    public long getTotalBytesTransferred()
    {
        return totalBytesTransferred;
    }

    /**
     * Returns the size of the stream being copied.
     * This may be set to <code>UNKNOWN_STREAM_SIZE</code> if the
     * size is unknown.
     * @return The size of the stream being copied.
     */
    public long getStreamSize()
    {
        return streamSize;
    }
}
