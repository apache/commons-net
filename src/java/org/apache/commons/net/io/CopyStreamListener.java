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

import java.util.EventListener;

/**
 * The CopyStreamListener class can accept CopyStreamEvents to keep track
 * of the progress of a stream copying operation.  However, it is currently
 * not used that way within NetComponents for performance reasons.  Rather
 * the bytesTransferred(long, int) method is called directly rather than
 * passing an event to bytesTransferred(CopyStreamEvent), saving the creation
 * of a CopyStreamEvent instance.  Also, the only place where
 * CopyStreamListener is currently used within NetComponents is in the
 * static methods of the uninstantiable org.apache.commons.io.Util class, which
 * would preclude the use of addCopyStreamListener and
 * removeCopyStreamListener methods.  However, future additions may use the
 * JavaBean event model, which is why the hooks have been included from the
 * beginning.
 * <p>
 * <p>
 * @see CopyStreamEvent
 * @see CopyStreamAdapter
 * @see Util
 * @author <a href="mailto:savarese@apache.org">Daniel F. Savarese</a>
 * @version $Id: CopyStreamListener.java,v 1.4 2003/01/26 00:21:41 dfs Exp $
 */
public interface CopyStreamListener extends EventListener
{
    /**
     * This method is invoked by a CopyStreamEvent source after copying
     * a block of bytes from a stream.  The CopyStreamEvent will contain
     * the total number of bytes transferred so far and the number of bytes
     * transferred in the last write.
     * @param event The CopyStreamEvent fired by the copying of a block of
     *              bytes.
     */
    public void bytesTransferred(CopyStreamEvent event);


    /**
     * This method is not part of the JavaBeans model and is used by the
     * static methods in the org.apache.commons.io.Util class for efficiency.
     * It is invoked after a block of bytes to inform the listener of the
     * transfer.
     * @param totalBytesTransferred  The total number of bytes transferred
     *         so far by the copy operation.
     * @param bytesTransferred  The number of bytes copied by the most recent
     *          write.
     * @param streamSize The number of bytes in the stream being copied.
     *        This may be equal to CopyStreamEvent.UNKNOWN_STREAM_SIZE if
     *        the size is unknown.
     */
    public void bytesTransferred(long totalBytesTransferred,
                                 int bytesTransferred,
                                 long streamSize);
}
