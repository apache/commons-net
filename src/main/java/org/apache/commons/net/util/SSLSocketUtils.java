/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.commons.net.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.net.ssl.SSLSocket;

/**
 * General utilities for SSLSocket.
 *
 * @since 3.4
 */
public class SSLSocketUtils {
    private SSLSocketUtils() {
        // Not instantiable
    }

    /**
     * Enable the HTTPS endpoint identification algorithm on an SSLSocket.
     *
     * @param socket the SSL socket
     * @return {@code true} on success (this is only supported on Java 1.7+)
     */
    public static boolean enableEndpointNameVerification(final SSLSocket socket) {
        try {
            final Class<?> cls = Class.forName("javax.net.ssl.SSLParameters");
            final Method setEndpointIdentificationAlgorithm = cls
                .getDeclaredMethod("setEndpointIdentificationAlgorithm", String.class);
            final Method getSSLParameters = SSLSocket.class.getDeclaredMethod("getSSLParameters");
            final Method setSSLParameters = SSLSocket.class.getDeclaredMethod("setSSLParameters", cls);
            if (setEndpointIdentificationAlgorithm != null && getSSLParameters != null && setSSLParameters != null) {
                final Object sslParams = getSSLParameters.invoke(socket);
                if (sslParams != null) {
                    setEndpointIdentificationAlgorithm.invoke(sslParams, "HTTPS");
                    setSSLParameters.invoke(socket, sslParams);
                    return true;
                }
            }
        } catch (final SecurityException | ClassNotFoundException | NoSuchMethodException | IllegalArgumentException |
            IllegalAccessException | InvocationTargetException e) { // Ignored
        }
        return false;
    }
}
