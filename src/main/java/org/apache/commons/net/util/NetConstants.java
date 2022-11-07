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
package org.apache.commons.net.util;

import java.security.cert.X509Certificate;

/**
 * Constants provided as public only for our own implementation, you can consider this private for now.
 *
 * @since 3.8.0
 */
public class NetConstants {

    /**
     * An empty immutable {@code String} array.
     */
    public static final String[] EMPTY_STRING_ARRAY = {};

    /**
     * An empty immutable {@code byte} array.
     */
    public static final byte[] EMPTY_BTYE_ARRAY = {};

    /**
     * An empty immutable {link X509Certificate} array.
     */
    public static final X509Certificate[] EMPTY_X509_CERTIFICATE_ARRAY = {};

    /**
     * The index value when the end of the stream has been reached {@code -1}.
     *
     * @since 3.9.0
     */
    public static final int EOS = -1;

    /**
     * Prevents instantiation.
     */
    private NetConstants() {
    }
}
