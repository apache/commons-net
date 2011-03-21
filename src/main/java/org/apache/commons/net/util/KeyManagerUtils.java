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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509ExtendedKeyManager;

import org.apache.commons.net.io.Util;

/**
 * General KeyManager utilities
 * <p>
 * How to use with a client certificate:
 * <pre>
 * KeyStore ks = KeyManagerUtils.createKeyStore("JKS","/path/to/privatekeystore.jks","storepassword");
 * KeyManager km = KeyManagerUtils.createClientKeyManager(ks, "privatekeyalias", "keypassword");
 * FTPSClient cl = new FTPSClient();
 * cl.setKeyManager(km);
 * cl.connect(...);
 * </pre>
 * @since 3.0
 */
public final class KeyManagerUtils {

    private KeyManagerUtils(){
        // Not instantiable
    }

    /**
     * Create a keystore.
     * 
     * @param ksType the type, e.g. JKS
     * @param keyStorePath the pathname of the store
     * @param keyStorePass the password for the store
     * @return the key store, loaded and ready for use.
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static KeyStore createKeyStore(String ksType, String keyStorePath, String keyStorePass)
        throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance(ksType);
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(keyStorePath);
            ks.load(stream, keyStorePass.toCharArray());
        } finally {
            Util.closeQuietly(stream);
        }
        return ks;
    }

    /**
     * Create a client key manager which returns a particular key.
     * Does not handle server keys.
     *  
     * @param keyStore the keystore
     * @param keyAlias the alias of the key to use
     * @param keyPass the password of the key to use
     * @return the customised X509KeyManager
     */
    public static KeyManager createClientKeyManager(KeyStore keyStore, String keyAlias, String keyPass) {
        return new X509KeyManager(keyAlias,  keyStore, keyPass);

    }

    private static class X509KeyManager extends X509ExtendedKeyManager  {

        private final KeyStore keyStore;

        private final String keyAlias;

        private final String keyPass;

        X509KeyManager(String keyAlias, KeyStore keyStore, String keyPass){
            this.keyAlias = keyAlias;
            this.keyStore = keyStore;
            this.keyPass = keyPass;
        }

        public String chooseClientAlias(String[] keyType, Principal[] issuers,
                Socket socket) {
            return keyAlias;
        }

        public X509Certificate[] getCertificateChain(String alias) {
            try {
                return new X509Certificate[]{(X509Certificate) keyStore.getCertificate(alias)};
            } catch (GeneralSecurityException e) {
                return null;
            }
        }

        public String[] getClientAliases(String keyType, Principal[] issuers) {
            return new String[]{ keyAlias};
        }

        public PrivateKey getPrivateKey(String alias) {
            try {
                return (PrivateKey) keyStore.getKey(alias, keyPass.toCharArray());
            } catch (GeneralSecurityException e) {
                return null;
            }
        }

        public String[] getServerAliases(String keyType, Principal[] issuers) {
            return null;
        }

        public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
            return null;
        }

    }
}
