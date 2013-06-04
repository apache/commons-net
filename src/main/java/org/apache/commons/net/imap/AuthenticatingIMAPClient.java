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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import org.apache.commons.net.util.Base64;

/**
 * An IMAP Client class with authentication support.
 * @see IMAPSClient
 */
public class AuthenticatingIMAPClient extends IMAPSClient
{
    /**
     * Constructor for AuthenticatingIMAPClient that delegates to IMAPSClient.
     * Sets security mode to explicit (isImplicit = false).
     */
    public AuthenticatingIMAPClient()
    {
        this(DEFAULT_PROTOCOL, false);
    }

    /**
     * Constructor for AuthenticatingIMAPClient that delegates to IMAPSClient.
     * @param implicit The security mode (Implicit/Explicit).
     */
    public AuthenticatingIMAPClient(boolean implicit)
    {
        this(DEFAULT_PROTOCOL, implicit);
    }

    /**
     * Constructor for AuthenticatingIMAPClient that delegates to IMAPSClient.
     * @param proto the protocol.
     */
    public AuthenticatingIMAPClient(String proto)
    {
        this(proto, false);
    }

    /**
     * Constructor for AuthenticatingIMAPClient that delegates to IMAPSClient.
     * @param proto the protocol.
     * @param implicit The security mode(Implicit/Explicit).
     */
    public AuthenticatingIMAPClient(String proto, boolean implicit)
    {
        this(proto, implicit, null);
    }

    /**
     * Constructor for AuthenticatingIMAPClient that delegates to IMAPSClient.
     * @param proto the protocol.
     * @param implicit The security mode(Implicit/Explicit).
     */
    public AuthenticatingIMAPClient(String proto, boolean implicit, SSLContext ctx)
    {
        super(proto, implicit, ctx);
    }

    /**
     * Constructor for AuthenticatingIMAPClient that delegates to IMAPSClient.
     * @param implicit The security mode(Implicit/Explicit).
     * @param ctx A pre-configured SSL Context.
     */
    public AuthenticatingIMAPClient(boolean implicit, SSLContext ctx)
    {
        this(DEFAULT_PROTOCOL, implicit, ctx);
    }

    /**
     * Constructor for AuthenticatingIMAPClient that delegates to IMAPSClient.
     * @param context A pre-configured SSL Context.
     */
    public AuthenticatingIMAPClient(SSLContext context)
    {
        this(false, context);
    }

    /**
     * Authenticate to the IMAP server by sending the AUTHENTICATE command with the
     * selected mechanism, using the given username and the given password.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @exception NoSuchAlgorithmException If the CRAM hash algorithm
     *      cannot be instantiated by the Java runtime system.
     * @exception InvalidKeyException If the CRAM hash algorithm
     *      failed to use the given password.
     * @exception InvalidKeySpecException If the CRAM hash algorithm
     *      failed to use the given password.
     */
    public boolean authenticate(AuthenticatingIMAPClient.AUTH_METHOD method,
                        String username, String password)
                        throws IOException, NoSuchAlgorithmException,
                        InvalidKeyException, InvalidKeySpecException
    {
        return auth(method, username, password);
    }

    /**
     * Authenticate to the IMAP server by sending the AUTHENTICATE command with the
     * selected mechanism, using the given username and the given password.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @exception NoSuchAlgorithmException If the CRAM hash algorithm
     *      cannot be instantiated by the Java runtime system.
     * @exception InvalidKeyException If the CRAM hash algorithm
     *      failed to use the given password.
     * @exception InvalidKeySpecException If the CRAM hash algorithm
     *      failed to use the given password.
     */
    public boolean auth(AuthenticatingIMAPClient.AUTH_METHOD method,
                        String username, String password)
                        throws IOException, NoSuchAlgorithmException,
                        InvalidKeyException, InvalidKeySpecException
    {
        if (!IMAPReply.isContinuation(sendCommand(IMAPCommand.AUTHENTICATE, method.getAuthName())))
        {
            return false;
        }

        switch (method) {
            case PLAIN:
            {
                // the server sends an empty response ("+ "), so we don't have to read it.
                int result = sendData(
                    Base64.encodeBase64StringUnChunked(("\000" + username + "\000" + password)
                            .getBytes(getCharsetName())));  // Java 1.6 can use getCharset()
                if (result == IMAPReply.OK)
                {
                    setState(IMAP.IMAPState.AUTH_STATE);
                }
                return result == IMAPReply.OK;
            }
            case CRAM_MD5:
            {
                // get the CRAM challenge (after "+ ")
                byte[] serverChallenge = Base64.decodeBase64(getReplyString().substring(2).trim());
                // get the Mac instance
                Mac hmac_md5 = Mac.getInstance("HmacMD5");
                hmac_md5.init(new SecretKeySpec(password.getBytes(getCharsetName()), "HmacMD5")); // Java 1.6 can use getCharset()
                // compute the result:
                byte[] hmacResult = _convertToHexString(hmac_md5.doFinal(serverChallenge)).getBytes(getCharsetName()); // Java 1.6 can use getCharset()
                // join the byte arrays to form the reply
                byte[] usernameBytes = username.getBytes(getCharsetName()); // Java 1.6 can use getCharset()
                byte[] toEncode = new byte[usernameBytes.length + 1 /* the space */ + hmacResult.length];
                System.arraycopy(usernameBytes, 0, toEncode, 0, usernameBytes.length);
                toEncode[usernameBytes.length] = ' ';
                System.arraycopy(hmacResult, 0, toEncode, usernameBytes.length + 1, hmacResult.length);
                // send the reply and read the server code:
                int result = sendData(Base64.encodeBase64StringUnChunked(toEncode));
                if (result == IMAPReply.OK)
                {
                    setState(IMAP.IMAPState.AUTH_STATE);
                }
                return result == IMAPReply.OK;
            }
            case LOGIN:
            {
                // the server sends fixed responses (base64("Username") and
                // base64("Password")), so we don't have to read them.
                if (sendData(
                    Base64.encodeBase64StringUnChunked(username.getBytes(getCharsetName()))) != IMAPReply.CONT) // Java 1.6 can use getCharset()
                {
                    return false;
                }
                int result = sendData(Base64.encodeBase64StringUnChunked(password.getBytes(getCharsetName()))); // Java 1.6 can use getCharset()
                if (result == IMAPReply.OK)
                {
                    setState(IMAP.IMAPState.AUTH_STATE);
                }
                return result == IMAPReply.OK;
            }
            case XOAUTH:
            {
                int result = sendData(username);
                if (result == IMAPReply.OK)
                {
                    setState(IMAP.IMAPState.AUTH_STATE);
                }
                return result == IMAPReply.OK;
            }
        }
        return false; // safety check
    }

    /**
     * Converts the given byte array to a String containing the hex values of the bytes.
     * For example, the byte 'A' will be converted to '41', because this is the ASCII code
     * (and the byte value) of the capital letter 'A'.
     * @param a The byte array to convert.
     * @return The resulting String of hex codes.
     */
    private String _convertToHexString(byte[] a)
    {
        StringBuilder result = new StringBuilder(a.length*2);
        for (byte element : a)
        {
            if ( (element & 0x0FF) <= 15 ) {
                result.append("0");
            }
            result.append(Integer.toHexString(element & 0x0FF));
        }
        return result.toString();
    }

    /**
     * The enumeration of currently-supported authentication methods.
     */
    public static enum AUTH_METHOD
    {
        /** The standarised (RFC4616) PLAIN method, which sends the password unencrypted (insecure). */
        PLAIN("PLAIN"),
        /** The standarised (RFC2195) CRAM-MD5 method, which doesn't send the password (secure). */
        CRAM_MD5("CRAM-MD5"),
        /** The unstandarised Microsoft LOGIN method, which sends the password unencrypted (insecure). */
        LOGIN("LOGIN"),
        /** XOAUTH */
        XOAUTH("XOAUTH");

        private final String authName;

        private AUTH_METHOD(String name){
            this.authName=name;
        }
        /**
         * Gets the name of the given authentication method suitable for the server.
         * @return The name of the given authentication method suitable for the server.
         */
        public final String getAuthName()
        {
            return authName;
        }
    }
}
/* kate: indent-width 4; replace-tabs on; */
