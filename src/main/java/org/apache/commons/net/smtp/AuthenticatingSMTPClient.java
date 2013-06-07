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

package org.apache.commons.net.smtp;

import java.io.IOException;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;

import org.apache.commons.net.util.Base64;


/**
 * An SMTP Client class with authentication support (RFC4954).
 *
 * @see SMTPClient
 * @since 3.0
 */
public class AuthenticatingSMTPClient extends SMTPSClient
{
    /**
     * The default AuthenticatingSMTPClient constructor.
     * Creates a new Authenticating SMTP Client.
     * @throws NoSuchAlgorithmException
     */
    public AuthenticatingSMTPClient() throws NoSuchAlgorithmException
    {
        super();
    }

    /**
     * Overloaded constructor that takes a protocol specification
     * @param protocol The protocol to use
     * @throws NoSuchAlgorithmException
     */
    public AuthenticatingSMTPClient(String protocol) throws NoSuchAlgorithmException {
        super(protocol);
    }

    /**
     * Overloaded constructor that takes a protocol specification and the implicit argument
     * @param proto the protocol.
     * @param implicit The security mode, {@code true} for implicit, {@code false} for explicit
     * @since 3.3
     */
    public AuthenticatingSMTPClient(String proto, boolean implicit)
    {
      super(proto, implicit);
    }

    /**
     * Overloaded constructor that takes the protocol specification, the implicit argument and encoding
     * @param proto the protocol.
     * @param implicit The security mode, {@code true} for implicit, {@code false} for explicit
     * @param encoding the encoding
     * @since 3.3
     */
    public AuthenticatingSMTPClient(String proto, boolean implicit, String encoding)
    {
      super(proto, implicit, encoding);
    }

    /**
     * Overloaded constructor that takes the implicit argument, and using {@link #DEFAULT_PROTOCOL} i.e. TLS
     * @param implicit The security mode, {@code true} for implicit, {@code false} for explicit
     * @param ctx A pre-configured SSL Context.
     * @since 3.3
     */
    public AuthenticatingSMTPClient(boolean implicit, SSLContext ctx)
    {
      super(implicit, ctx);
    }

    /**
     * Overloaded constructor that takes a protocol specification and encoding
     * @param protocol The protocol to use
     * @param encoding The encoding to use
     * @throws NoSuchAlgorithmException
     * @since 3.3
     */
    public AuthenticatingSMTPClient(String protocol, String encoding) throws NoSuchAlgorithmException {
        super(protocol, false, encoding);
    }

    /***
     * A convenience method to send the ESMTP EHLO command to the server,
     * receive the reply, and return the reply code.
     * <p>
     * @param hostname The hostname of the sender.
     * @return The reply code received from the server.
     * @exception SMTPConnectionClosedException
     *      If the SMTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send SMTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending the
     *      command or receiving the server reply.
     ***/
    public int ehlo(String hostname) throws IOException
    {
        return sendCommand(SMTPCommand.EHLO, hostname);
    }

    /***
     * Login to the ESMTP server by sending the EHLO command with the
     * given hostname as an argument.  Before performing any mail commands,
     * you must first login.
     * <p>
     * @param hostname  The hostname with which to greet the SMTP server.
     * @return True if successfully completed, false if not.
     * @exception SMTPConnectionClosedException
     *      If the SMTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send SMTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean elogin(String hostname) throws IOException
    {
        return SMTPReply.isPositiveCompletion(ehlo(hostname));
    }


    /***
     * Login to the ESMTP server by sending the EHLO command with the
     * client hostname as an argument.  Before performing any mail commands,
     * you must first login.
     * <p>
     * @return True if successfully completed, false if not.
     * @exception SMTPConnectionClosedException
     *      If the SMTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send SMTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     ***/
    public boolean elogin() throws IOException
    {
        String name;
        InetAddress host;

        host = getLocalAddress();
        name = host.getHostName();

        if (name == null) {
            return false;
        }

        return SMTPReply.isPositiveCompletion(ehlo(name));
    }

    /***
     * Returns the integer values of the enhanced reply code of the last SMTP reply.
     * @return The integer values of the enhanced reply code of the last SMTP reply.
     *  First digit is in the first array element.
     ***/
    public int[] getEnhancedReplyCode()
    {
        String reply = getReplyString().substring(4);
        String[] parts = reply.substring(0, reply.indexOf(' ')).split ("\\.");
        int[] res = new int[parts.length];
        for (int i = 0; i < parts.length; i++)
        {
            res[i] = Integer.parseInt (parts[i]);
        }
        return res;
    }

    /***
     * Authenticate to the SMTP server by sending the AUTH command with the
     * selected mechanism, using the given username and the given password.
     *
     * @param method the method to use, one of the {@link AuthenticatingSMTPClient.AUTH_METHOD} enum values
     * @param username the user name.
     *        If the method is XOAUTH, then this is used as the plain text oauth protocol parameter string
     *        which is Base64-encoded for transmission.
     * @param password the password for the username.
     *        Ignored for XOAUTH.
     *
     * @return True if successfully completed, false if not.
     * @exception SMTPConnectionClosedException
     *      If the SMTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send SMTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @exception NoSuchAlgorithmException If the CRAM hash algorithm
     *      cannot be instantiated by the Java runtime system.
     * @exception InvalidKeyException If the CRAM hash algorithm
     *      failed to use the given password.
     * @exception InvalidKeySpecException If the CRAM hash algorithm
     *      failed to use the given password.
     ***/
    public boolean auth(AuthenticatingSMTPClient.AUTH_METHOD method,
                        String username, String password)
                        throws IOException, NoSuchAlgorithmException,
                        InvalidKeyException, InvalidKeySpecException
    {
        if (!SMTPReply.isPositiveIntermediate(sendCommand(SMTPCommand.AUTH,
                AUTH_METHOD.getAuthName(method)))) {
            return false;
        }

        if (method.equals(AUTH_METHOD.PLAIN))
        {
            // the server sends an empty response ("334 "), so we don't have to read it.
            return SMTPReply.isPositiveCompletion(sendCommand(
                    Base64.encodeBase64StringUnChunked(("\000" + username + "\000" + password).getBytes(getCharsetName())) // Java 1.6 can use getCharset()
                ));
        }
        else if (method.equals(AUTH_METHOD.CRAM_MD5))
        {
            // get the CRAM challenge
            byte[] serverChallenge = Base64.decodeBase64(getReplyString().substring(4).trim());
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
            return SMTPReply.isPositiveCompletion(sendCommand(
                Base64.encodeBase64StringUnChunked(toEncode)));
        }
        else if (method.equals(AUTH_METHOD.LOGIN))
        {
            // the server sends fixed responses (base64("Username") and
            // base64("Password")), so we don't have to read them.
            if (!SMTPReply.isPositiveIntermediate(sendCommand(
                Base64.encodeBase64StringUnChunked(username.getBytes(getCharsetName()))))) { // Java 1.6 can use getCharset()
                return false;
            }
            return SMTPReply.isPositiveCompletion(sendCommand(
                Base64.encodeBase64StringUnChunked(password.getBytes(getCharsetName())))); // Java 1.6 can use getCharset()
        }
        else if (method.equals(AUTH_METHOD.XOAUTH))
        {
            return SMTPReply.isPositiveIntermediate(sendCommand(
                    Base64.encodeBase64StringUnChunked(username.getBytes(getCharsetName())) // Java 1.6 can use getCharset()
            ));
        } else {
            return false; // safety check
        }
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
        PLAIN,
        /** The standarised (RFC2195) CRAM-MD5 method, which doesn't send the password (secure). */
        CRAM_MD5,
        /** The unstandarised Microsoft LOGIN method, which sends the password unencrypted (insecure). */
        LOGIN,
        /** XOAuth method which accepts a signed and base64ed OAuth URL. */
        XOAUTH;

        /**
         * Gets the name of the given authentication method suitable for the server.
         * @param method The authentication method to get the name for.
         * @return The name of the given authentication method suitable for the server.
         */
        public static final String getAuthName(AUTH_METHOD method)
        {
            if (method.equals(AUTH_METHOD.PLAIN)) {
                return "PLAIN";
            } else if (method.equals(AUTH_METHOD.CRAM_MD5)) {
                return "CRAM-MD5";
            } else if (method.equals(AUTH_METHOD.LOGIN)) {
                return "LOGIN";
            } else if (method.equals(AUTH_METHOD.XOAUTH)) {
                return "XOAUTH";
            } else {
                return null;
            }
        }
    }
}

/* kate: indent-width 4; replace-tabs on; */
