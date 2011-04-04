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

package org.apache.commons.net.pop3;

/***
 * POP3Reply stores POP3 reply code constants.
 * <p>
 * <p>
 ***/

public final class POP3Reply
{
    /*** The reply code indicating success of an operation. ***/
    public static final int OK = 0;

    /*** The reply code indicating failure of an operation. ***/
    public static final int ERROR = 1;

    /**
     * The reply code indicating intermediate response to a command.
     * @since 3.0
     */
    public static final int OK_INT = 2;

    // Cannot be instantiated.
    private POP3Reply()
    {}
}
