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

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;

/**
 * An IMAP Client class with authentication support.
 *
 * @see IMAPSClient
 */
public class AuthenticatingIMAPClient extends IMAPSClient {

    /** {@link Mac} algorithm. */
    private static final String MAC_ALGORITHM = "HmacMD5";

    /**
     * The enumeration of currently-supported authentication methods.
     */
    public enum AUTH_METHOD {

        /** The standardized (RFC4616) PLAIN method, which sends the password unencrypted (insecure). */

        PLAIN("PLAIN"),
        /** The standardized (RFC2195) CRAM-MD5 method, which doesn't send the password (secure). */

        CRAM_MD5("CRAM-MD5"),

        /** The standardized Microsoft LOGIN method, which sends the password unencrypted (insecure). */
        LOGIN("LOGIN"),

        /** XOAUTH */
        XOAUTH("XOAUTH"),

        /** XOAUTH 2 */
        XOAUTH2("XOAUTH2");

        private final String authName;

        AUTH_METHOD(final String name) {
            this.authName = name;
        }

        /**
         * Gets the name of the given authentication method suitable for the server.
         *
         * @return The name of the given authentication method suitable for the server.
         */
        public final String getAuthName() {
            return authName;
        }
    }

    /**
     * Constructor for AuthenticatingIMAPClient that delegates to IMAPSClient. Sets security mode to explicit (isImplicit = false).
     */
    public AuthenticatingIMAPClient() {
        this(DEFAULT_PROTOCOL, false);
    }

    /**
     * Constructor for AuthenticatingIMAPClient that delegates to IMAPSClient.
     *
     * @param implicit The security mode (Implicit/Explicit).
     */
    public AuthenticatingIMAPClient(final boolean implicit) {
        this(DEFAULT_PROTOCOL, implicit);
    }

    /**
     * Constructor for AuthenticatingIMAPClient that delegates to IMAPSClient.
     *
     * @param implicit The security mode(Implicit/Explicit).
     * @param ctx      A pre-configured SSL Context.
     */
    public AuthenticatingIMAPClient(final boolean implicit, final SSLContext ctx) {
        this(DEFAULT_PROTOCOL, implicit, ctx);
    }

    /**
     * Constructor for AuthenticatingIMAPClient that delegates to IMAPSClient.
     *
     * @param context A pre-configured SSL Context.
     */
    public AuthenticatingIMAPClient(final SSLContext context) {
        this(false, context);
    }

    /**
     * Constructor for AuthenticatingIMAPClient that delegates to IMAPSClient.
     *
     * @param proto the protocol.
     */
    public AuthenticatingIMAPClient(final String proto) {
        this(proto, false);
    }

    /**
     * Constructor for AuthenticatingIMAPClient that delegates to IMAPSClient.
     *
     * @param proto    the protocol.
     * @param implicit The security mode(Implicit/Explicit).
     */
    public AuthenticatingIMAPClient(final String proto, final boolean implicit) {
        this(proto, implicit, null);
    }

    /**
     * Constructor for AuthenticatingIMAPClient that delegates to IMAPSClient.
     *
     * @param proto    the protocol.
     * @param implicit The security mode(Implicit/Explicit).
     * @param ctx      the context
     */
    public AuthenticatingIMAPClient(final String proto, final boolean implicit, final SSLContext ctx) {
        super(proto, implicit, ctx);
    }

    /**
     * Authenticate to the IMAP server by sending the AUTHENTICATE command with the selected mechanism, using the given user and the given password.
     *
     * @param method   the method name
     * @param user user
     * @param password password
     * @return True if successfully completed, false if not.
     * @throws IOException              If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     * @throws NoSuchAlgorithmException If the CRAM hash algorithm cannot be instantiated by the Java runtime system.
     * @throws InvalidKeyException      If the CRAM hash algorithm failed to use the given password.
     * @throws InvalidKeySpecException  If the CRAM hash algorithm failed to use the given password.
     */
    public boolean auth(final AuthenticatingIMAPClient.AUTH_METHOD method, final String user, final String password)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        if (!IMAPReply.isContinuation(sendCommand(IMAPCommand.AUTHENTICATE, method.getAuthName()))) {
            return false;
        }

        switch (method) {
        case PLAIN: {
            // the server sends an empty response ("+ "), so we don't have to read it.
            final int result = sendData(Base64.getEncoder().encodeToString(("\000" + user + "\000" + password).getBytes(getCharset())));
            if (result == IMAPReply.OK) {
                setState(IMAP.IMAPState.AUTH_STATE);
            }
            return result == IMAPReply.OK;
        }
        case CRAM_MD5: {
            // get the CRAM challenge (after "+ ")
            final byte[] serverChallenge = Base64.getDecoder().decode(getReplyString().substring(2).trim());
            // get the Mac instance
            final Mac hmacMd5 = Mac.getInstance(MAC_ALGORITHM);
            hmacMd5.init(new SecretKeySpec(password.getBytes(getCharset()), MAC_ALGORITHM));
            // compute the result:
            final byte[] hmacResult = convertToHexString(hmacMd5.doFinal(serverChallenge)).getBytes(getCharset());
            // join the byte arrays to form the reply
            final byte[] usernameBytes = user.getBytes(getCharset());
            final byte[] toEncode = new byte[usernameBytes.length + 1 /* the space */ + hmacResult.length];
            System.arraycopy(usernameBytes, 0, toEncode, 0, usernameBytes.length);
            toEncode[usernameBytes.length] = ' ';
            System.arraycopy(hmacResult, 0, toEncode, usernameBytes.length + 1, hmacResult.length);
            // send the reply and read the server code:
            final int result = sendData(Base64.getEncoder().encodeToString(toEncode));
            if (result == IMAPReply.OK) {
                setState(IMAP.IMAPState.AUTH_STATE);
            }
            return result == IMAPReply.OK;
        }
        case LOGIN: {
            // the server sends fixed responses (base64("Username") and
            // base64("Password")), so we don't have to read them.
            if (sendData(Base64.getEncoder().encodeToString(user.getBytes(getCharset()))) != IMAPReply.CONT) {
                return false;
            }
            final int result = sendData(Base64.getEncoder().encodeToString(password.getBytes(getCharset())));
            if (result == IMAPReply.OK) {
                setState(IMAP.IMAPState.AUTH_STATE);
            }
            return result == IMAPReply.OK;
        }
        case XOAUTH:
        case XOAUTH2: {
            final int result = sendData(user);
            if (result == IMAPReply.OK) {
                setState(IMAP.IMAPState.AUTH_STATE);
            }
            return result == IMAPReply.OK;
        }
        }
        return false; // safety check
    }

    /**
     * Authenticate to the IMAP server by sending the AUTHENTICATE command with the selected mechanism, using the given user and the given password.
     *
     * @param method   the method name
     * @param user user
     * @param password password
     * @return True if successfully completed, false if not.
     * @throws IOException              If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     * @throws NoSuchAlgorithmException If the CRAM hash algorithm cannot be instantiated by the Java runtime system.
     * @throws InvalidKeyException      If the CRAM hash algorithm failed to use the given password.
     * @throws InvalidKeySpecException  If the CRAM hash algorithm failed to use the given password.
     */
    public boolean authenticate(final AuthenticatingIMAPClient.AUTH_METHOD method, final String user, final String password)
            throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        return auth(method, user, password);
    }

    /**
     * Converts the given byte array to a String containing the hex values of the bytes. For example, the byte 'A' will be converted to '41', because this is
     * the ASCII code (and the byte value) of the capital letter 'A'.
     *
     * @param a The byte array to convert.
     * @return The resulting String of hex codes.
     */
    private String convertToHexString(final byte[] a) {
        final StringBuilder result = new StringBuilder(a.length * 2);
        for (final byte element : a) {
            if ((element & 0x0FF) <= 15) {
                result.append("0");
            }
            result.append(Integer.toHexString(element & 0x0FF));
        }
        return result.toString();
    }
}

