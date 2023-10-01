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
 */

package org.apache.commons.net.util;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * General utilities for SSLContext.
 *
 * @since 3.0
 */
public class SSLContextUtils {

    /**
     * Create and initialize an SSLContext.
     *
     * @param protocol     the protocol used to instantiate the context
     * @param keyManager   the key manager, may be {@code null}
     * @param trustManager the trust manager, may be {@code null}
     * @return the initialized context.
     * @throws IOException this is used to wrap any {@link GeneralSecurityException} that occurs
     */
    public static SSLContext createSSLContext(final String protocol, final KeyManager keyManager, final TrustManager trustManager) throws IOException {
        return createSSLContext(protocol, keyManager == null ? null : new KeyManager[] { keyManager },
                trustManager == null ? null : new TrustManager[] { trustManager });
    }

    /**
     * Create and initialize an SSLContext.
     *
     * @param protocol      the protocol used to instantiate the context
     * @param keyManagers   the array of key managers, may be {@code null} but array entries must not be {@code null}
     * @param trustManagers the array of trust managers, may be {@code null} but array entries must not be {@code null}
     * @return the initialized context.
     * @throws IOException this is used to wrap any {@link GeneralSecurityException} that occurs
     */
    public static SSLContext createSSLContext(final String protocol, final KeyManager[] keyManagers, final TrustManager[] trustManagers) throws IOException {
        final SSLContext ctx;
        try {
            ctx = SSLContext.getInstance(protocol);
            ctx.init(keyManagers, trustManagers, /* SecureRandom */ null);
        } catch (final GeneralSecurityException e) {
            final IOException ioe = new IOException("Could not initialize SSL context");
            ioe.initCause(e);
            throw ioe;
        }
        return ctx;
    }

    private SSLContextUtils() {
        // Not instantiable
    }
}
