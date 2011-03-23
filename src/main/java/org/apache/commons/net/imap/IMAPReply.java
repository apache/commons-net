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

package org.apache.commons.net.imap;

/**
 * IMAPReply stores IMAP reply code constants.
 */

public final class IMAPReply
{
    /** The reply code indicating success of an operation. */
    public static final int OK = 0;

    /** The reply code indicating failure of an operation. */
    public static final int NO = 1;

    /** The reply code indicating command rejection. */
    public static final int BAD = 2;

    /** The reply code indicating intermediate command processing stage. */
    public static final int OK_INT = 3;

    /** The reply code indicating a continuation mark. */
    public static final int CONT = 4;

    /** The reply String indicating success of an operation. */
    public static final String OK_String = "OK";

    /** The reply String indicating failure of an operation. */
    public static final String NO_String = "NO";

    /** The reply String indicating command rejection. */
    public static final String BAD_String = "BAD";

    /** The reply String indicating intermediate command processing stage. */
    public static final String OK_INT_String = "*";

    /** The reply String indicating a continuation mark. */
    public static final String CONT_String = "+";

    // Cannot be instantiated.
    private IMAPReply()
    {}
}

/* kate: indent-width 4; replace-tabs on; */
