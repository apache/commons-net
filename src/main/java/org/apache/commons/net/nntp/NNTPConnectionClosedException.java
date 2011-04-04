/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.nntp;

import java.io.IOException;

/***
 * NNTPConnectionClosedException is used to indicate the premature or
 * unexpected closing of an NNTP connection resulting from a
 * {@link org.apache.commons.net.nntp.NNTPReply#SERVICE_DISCONTINUED NNTPReply.SERVICE_DISCONTINUED }
 *  response (NNTP reply code 400) to a
 * failed NNTP command.  This exception is derived from IOException and
 * therefore may be caught either as an IOException or specifically as an
 * NNTPConnectionClosedException.
 * <p>
 * <p>
 * @see NNTP
 * @see NNTPClient
 ***/

public final class NNTPConnectionClosedException extends IOException
{

    private static final long serialVersionUID = 1029785635891040770L;

    /*** Constructs a NNTPConnectionClosedException with no message ***/
    public NNTPConnectionClosedException()
    {
        super();
    }

    /***
     * Constructs a NNTPConnectionClosedException with a specified message.
     * <p>
     * @param message  The message explaining the reason for the exception.
     ***/
    public NNTPConnectionClosedException(String message)
    {
        super(message);
    }

}
