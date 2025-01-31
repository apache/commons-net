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

package org.apache.commons.net.ftp;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.net.util.TrustManagerUtils;
import org.bouncycastle.jsse.BCExtendedSSLSession;
import org.bouncycastle.jsse.BCSSLSocket;
import org.bouncycastle.jsse.provider.BouncyCastleJsseProvider;

public class FTPSClientSSLSessionReuse extends FTPSClient {

    // Flag to push hostname in data SSL Socket
    // Need this for SNI. Not needed in filezilla-server docker version
    // Probably because localhost disable SNI verification
    protected boolean needHostForSni = false;

    public void setNeedHostForSni(boolean needHostForSni) {
        this.needHostForSni = needHostForSni;
    }

    public FTPSClientSSLSessionReuse(boolean isImplicit) throws Exception {
        super(isImplicit, createSSLContext(null));
        setEnabledProtocols(new String[] { "TLSv1.2" });
        setUseEPSVwithIPv4(true);

        setIpAddressFromPasvResponse(true);
        setPassiveNatWorkaroundStrategy(new HostnameResolver() {

            @Override
            public String resolve(String hostname) throws UnknownHostException {
                return _hostname_;
            }
        });

    }

    private static SSLContext createSSLContext(String hostname) throws Exception {
        SSLContext context = SSLContext.getInstance("TLS", new BouncyCastleJsseProvider());
        context.init(null, new TrustManager[] { TrustManagerUtils.getValidateServerCertificateTrustManager() }, new SecureRandom());
        return context;
    }

    @Override
    protected void _prepareDataSocket_(final Socket socket) throws IOException {
        if (_socket_ instanceof BCSSLSocket) {
            BCSSLSocket sslSocket = (BCSSLSocket) _socket_;
            BCExtendedSSLSession bcSession = sslSocket.getBCSession();
            if (bcSession != null && bcSession.isValid() && socket instanceof BCSSLSocket) {
                BCSSLSocket dataSslSocket = (BCSSLSocket) socket;
                dataSslSocket.setBCSessionToResume(bcSession);
                if (needHostForSni) {
                    dataSslSocket.setHost(bcSession.getPeerHost());
                }
            }
        }
    }
}
