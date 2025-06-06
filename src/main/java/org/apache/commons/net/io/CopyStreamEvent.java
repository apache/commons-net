/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.io;

import java.util.EventObject;

/**
 * A CopyStreamEvent is triggered after every write performed by a stream copying operation. The event stores the number of bytes transferred by the write
 * triggering the event as well as the total number of bytes transferred so far by the copy operation.
 *
 *
 * @see CopyStreamListener
 * @see CopyStreamAdapter
 * @see Util
 */
public class CopyStreamEvent extends EventObject {
    private static final long serialVersionUID = -964927635655051867L;

    /**
     * Constant used to indicate the stream size is unknown.
     */
    public static final long UNKNOWN_STREAM_SIZE = -1;

    /**
     * The number of bytes transferred during the write that triggered the CopyStreamEvent.
     */
    private final int bytesTransferred;

    /**
     * The total number of bytes transferred so far during a copy operation.
     */
    private final long totalBytesTransferred;

    /**
     * The number of bytes in the stream being copied. This may be set to {@code UNKNOWN_STREAM_SIZE} if the size is unknown.
     */
    private final long streamSize;

    /**
     * Constructs a new instance.
     *
     * @param source                The source of the event.
     * @param totalBytesTransferred The total number of bytes transferred so far during a copy operation.
     * @param bytesTransferred      The number of bytes transferred during the write that triggered the CopyStreamEvent.
     * @param streamSize            The number of bytes in the stream being copied. This may be set to {@code UNKNOWN_STREAM_SIZE} if the size is unknown.
     */
    public CopyStreamEvent(final Object source, final long totalBytesTransferred, final int bytesTransferred, final long streamSize) {
        super(source);
        this.bytesTransferred = bytesTransferred;
        this.totalBytesTransferred = totalBytesTransferred;
        this.streamSize = streamSize;
    }

    /**
     * Gets the number of bytes transferred by the write that triggered the event.
     *
     * @return The number of bytes transferred by the write that triggered the vent.
     */
    public int getBytesTransferred() {
        return bytesTransferred;
    }

    /**
     * Gets the size of the stream being copied. This may be set to {@code UNKNOWN_STREAM_SIZE} if the size is unknown.
     *
     * @return The size of the stream being copied.
     */
    public long getStreamSize() {
        return streamSize;
    }

    /**
     * Gets the total number of bytes transferred so far by the copy operation.
     *
     * @return The total number of bytes transferred so far by the copy operation.
     */
    public long getTotalBytesTransferred() {
        return totalBytesTransferred;
    }

    /**
     * @since 3.0
     */
    @Override
    public String toString() {
        return getClass().getName() + "[source=" + source + ", total=" + totalBytesTransferred + ", bytes=" + bytesTransferred + ", size=" + streamSize + "]";
    }
}
