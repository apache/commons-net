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

package org.apache.commons.net.tftp;

/***
 * A class used to signify the occurrence of an error in the creation of
 * a TFTP packet.  It is not declared final so that it may be subclassed
 * to identify more specific errors.  You would only want to do this if
 * you were building your own TFTP client or server on top of the
 * {@link org.apache.commons.net.tftp.TFTP}
 * class if you
 * wanted more functionality than the
 * {@link org.apache.commons.net.tftp.TFTPClient#receiveFile receiveFile()}
 * and
 * {@link org.apache.commons.net.tftp.TFTPClient#sendFile sendFile()}
 * methods provide.
 * <p>
 * <p>
 * @see TFTPPacket
 * @see TFTP
 ***/

public class TFTPPacketException extends Exception
{

    private static final long serialVersionUID = -8114699256840851439L;

    /***
     * Simply calls the corresponding constructor of its superclass.
     ***/
    public TFTPPacketException()
    {
        super();
    }

    /***
     * Simply calls the corresponding constructor of its superclass.
     ***/
    public TFTPPacketException(String message)
    {
        super(message);
    }
}
