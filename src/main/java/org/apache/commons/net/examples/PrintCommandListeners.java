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

package org.apache.commons.net.examples;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.io.Util;

/**
 * Factory of {@link PrintCommandListener} for examples.
 */
public class PrintCommandListeners {

    /**
     * Creates a new PrintCommandListener on system out.
     *
     * @return a new PrintCommandListener on system out.
     */
    public static PrintCommandListener sysOutPrintCommandListener() {
        return new PrintCommandListener(Util.newPrintWriter(System.out), true);
    }

    /**
     * Constructs a new instance.
     *
     * @deprecated Will be private in the next major release.
     */
    @Deprecated
    public PrintCommandListeners() {
        // empty
    }

}
