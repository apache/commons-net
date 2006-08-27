/**
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.ftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Vector;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * FTP over SSL processing.
 * 
 * <p>For example:
 * <p>
 * <code>
 *  FTPSClient client = new FTPSClient();
 *	client.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
 *	client.connect("127.0.0.1");
 *	client.login(username, password);
 *	
 *	for (FTPFile file : client.listFiles()) {
 *		System.out.printf("%s [%d bytes]\n", file.getName(), file.getSize());
 *	}
 *	
 * 	client.disconnect();
 *	</code>
 * 	</p>
 */
public class FTPSClient extends FTPClient {

    /** keystore algorithm name. */
    public static String KEYSTORE_ALGORITHM;
    /** truststore algorithm name. */
    public static String TRUSTSTORE_ALGORITHM;
    /** provider name. */
    public static String PROVIDER;
    /** truststore type. */
    public static String STORE_TYPE;

    /** The value that I can set in PROT command */
    private static final String[] PROT_COMMAND_VALUE = {"C","E","S","P"}; 
    /** Default PROT Command */
    private static final String DEFAULT_PROT = "C";
    /** Default protocol name */
    private static final String DEFAULT_PROTOCOL = "TLS";

    /** The security mode. (True - Implicit Mode / False - Explicit Mode) */
    private boolean isImplicit;
    /** The use SSL/TLS protocol. */
    private String protocol = DEFAULT_PROTOCOL;
    /** The AUTH Command value */
    private String auth = DEFAULT_PROTOCOL;
    /** The KeyManager object. */
    private KeyManager[] keyManager = null;
    /** The TrustManager object */
    private TrustManager[] trustManager = null;
    /** The context object. */
    private SSLContext context;
    /** The socket object. */
    private Socket planeSocket;
    /** The established socket flag. */
    private boolean isCreation = true;
    /** The use client mode flag. */
    private boolean isClientMode = true;
    /** The need client auth flag. */
    private boolean isNeedClientAuth = false;
    /** The want client auth flag. */
    private boolean isWantClientAuth = false;
    /** The cipher suites */
    private String[] suites = null;
    /** The protocol versions */
    private String[] protocols = null;

    /**
     * Constructor for FTPSClient.
     * @throws NoSuchAlgorithmException A requested cryptographic algorithm 
     * is not available in the environment.
     */
    public FTPSClient() throws NoSuchAlgorithmException {
        this.protocol = DEFAULT_PROTOCOL;
        this.isImplicit = false;
        context = SSLContext.getInstance(protocol);
    }

    /**
     * Constructor for FTPSClient.
     * @param isImplicit The secutiry mode(Implicit/Explicit).
     * @throws NoSuchAlgorithmException A requested cryptographic algorithm 
     * is not available in the environment.
     */
    public FTPSClient(boolean isImplicit) throws NoSuchAlgorithmException {
        this.protocol = DEFAULT_PROTOCOL;
        this.isImplicit = isImplicit;
        context = SSLContext.getInstance(protocol);
    }

    /**
     * Constructor for FTPSClient.
     * @param conType The context type
     * @throws NoSuchAlgorithmException A requested cryptographic algorithm 
     * is not available in the environment.
     */
    public FTPSClient(String protocol) throws NoSuchAlgorithmException {
        this.protocol = protocol;
        this.isImplicit = false;
        context = SSLContext.getInstance(protocol);
    }

    /**
     * Constructor for FTPSClient.
     * @param conType The context type
     * @param isImplicit The secutiry mode(Implicit/Explicit).
     * @throws NoSuchAlgorithmException A requested cryptographic algorithm 
     * is not available in the environment.
     */
    public FTPSClient(String protocol, boolean isImplicit) 
            throws NoSuchAlgorithmException {
        this.protocol = protocol;
        this.isImplicit = isImplicit;
        context = SSLContext.getInstance(protocol);
    }

    /**
     * Create KeyManager[] object.
     * @param ks The KeyStore objects.
     * @param storePass The Store password.
     * @throws NoSuchAlgorithmException A requested cryptographic 
     * algorithm is not available in the environment.
     * @throws NoSuchProviderException A requested cryptographic provider 
     * is not available in the environment.
     * @throws UnrecoverableKeyException This exception is thrown 
     * if a key in the keystore cannot be recovered.
     * @throws KeyStoreException This is the generic KeyStore exception.
     * @throws KeyManagementException It is the generic KeyManager exception.
     */
    public void createKeyManager(KeyStore ks, String storePass)
            throws NoSuchAlgorithmException, NoSuchProviderException,
            KeyStoreException,UnrecoverableKeyException,KeyManagementException{
        if (ks == null) {
            keyManager = null;
            return;
        }
        if (KEYSTORE_ALGORITHM == null)
            KEYSTORE_ALGORITHM = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory kmf;
        if (PROVIDER == null) {
            kmf = KeyManagerFactory.getInstance(KEYSTORE_ALGORITHM);
        } else {
            kmf = KeyManagerFactory.getInstance(KEYSTORE_ALGORITHM, PROVIDER);
        }
        if (kmf == null) {
            keyManager = null;
            return;
        }
        kmf.init(ks, storePass.toCharArray());
        keyManager = kmf.getKeyManagers();
        context.init(keyManager, trustManager, null);
    }

    /**
     * Create TrustManager[] object.
     * @param ks The KeyStore object.
     * @throws NoSuchAlgorithmException A requested cryptographic algorithm 
     * is not available in the environment.
     * @throws NoSuchProviderException A requested cryptographic provider 
     * is not available in the environment.
     * @throws KeyStoreException This is the generic KeyStore exception.
     * @throws KeyManagementException It is the generic KeyManager exception.
     */
    public void createTrustManager(KeyStore ks) 
            throws NoSuchAlgorithmException, NoSuchProviderException, 
            KeyStoreException, KeyManagementException {
        if (ks == null) trustManager = null;
        if (TRUSTSTORE_ALGORITHM == null)
            TRUSTSTORE_ALGORITHM = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf;
        if (PROVIDER == null) {
            tmf = TrustManagerFactory.getInstance(TRUSTSTORE_ALGORITHM);
        } else {
            tmf = TrustManagerFactory.getInstance(
                    TRUSTSTORE_ALGORITHM, PROVIDER);
        }
        if (tmf == null) {
            trustManager = null;
            return;
        }
        tmf.init(ks);
        trustManager = tmf.getTrustManagers();
        context.init(keyManager, trustManager, null);
    }

    /**
     * Create TrustManager[] object.
     * @param _ks The KeyStore objects.
     * @throws KeyStoreException This is the generic KeyStore exception.
     * @throws CertificateException This exception indicates one of 
     * a variety of certificate problems.
     * @throws NoSuchAlgorithmException A requested cryptographic algorithm 
     * is not available in the environment.
     * @throws NoSuchProviderException A requested cryptographic provider 
     * is not available in the environment.
     * @throws KeyManagementException It is the generic KeyManager exception.
     * @throws IOException
     */
    public void createTrustManager(Vector ks) throws KeyStoreException, 
            NoSuchAlgorithmException, CertificateException, 
            IOException, NoSuchProviderException, KeyManagementException {
        if (ks == null) {
            trustManager = null;
            return;
        }
        KeyStore _ks;
        if (STORE_TYPE == null) {
            _ks = KeyStore.getInstance(KeyStore.getDefaultType());
        } else {
            _ks = KeyStore.getInstance(STORE_TYPE);
        }
        _ks.load(null, null);
        int n = 0;
        // as for every keystore
        for (int i = 0; i < ks.size(); i++) {
            // as for every alias
            KeyStore wks = ((KeyStore) ks.get(i));
            for (Enumeration e = wks.aliases(); e.hasMoreElements();) {
                String alias = (String) e.nextElement();
                _ks.setCertificateEntry(String.valueOf(n), 
                        wks.getCertificate(alias));
                n++;
            }
        }
        createTrustManager(_ks);
    }

    /**
     * Set AUTH command use value.
     * This processing is done before connected processing.
     * @param auth AUTH command use value.
     */
    public void setAuthValue(String auth) {
        this.auth = auth;
    }

    /**
     * Return AUTH command use value.
     * @return AUTH command use value.
     */
    public String getAuthValue() {
        return this.auth;
    }

    /**
     * I work to be connected. Opens a Socket connected to a remote host 
     * at the specified port and originating from the current host at 
     * a system assigned port.
     * @param address The name of the remote host.
     * @param port The port to connect to on the remote host.
     * @throws SocketException If the socket timeout could not be set.
     * @throws IOException If the socket could not be opened.
     * In most cases you will only want to catch IOException since 
     * SocketException is derived from it.
     * @see org.apache.commons.net.SocketClient#connect(java.lang.String, int)
     */
    public void connect(String address, int port) 
            throws SocketException, IOException {
        super.connect(address, port);
    }

    /**
     * I work to be connected. Opens a Socket connected to a remote host 
     * at the specified port and originating from the current host at 
     * a system assigned port.
     * @param address The name of the remote host.
     * @param port The port to connect to on the remote host.
     * @throws SocketException If the socket timeout could not be set.
     * @throws IOException If the socket could not be opened.
     * In most cases you will only want to catch IOException since 
     * SocketException is derived from it.
     * @see org.apache.commons.net.SocketClient 
     * #connect(java.net.InetAddress, int)
     */
    public void connect(InetAddress address, int port) 
            throws SocketException, IOException {
        super.connect(address, port);
    }

    /**
     * I work to be connected. Opens a Socket connected to a remote host 
     * at the specified port and originating from the specified 
     * local address and port.
     * @param address The name of the remote host.
     * @param port The port to connect to on the remote host.
     * @param localAddress The local address to use.
     * @param localPort The local port to use.
     * @throws SocketException If the socket timeout could not be set.
     * @throws IOException If the socket could not be opened.
     * In most cases you will only want to catch IOException since 
     * SocketException is derived from it.
     * @see org.apache.commons.net.SocketClient
     * #connect(java.net.InetAddress, int, java.net.InetAddress, int)
     */
    public void connect(InetAddress address, int port, 
            InetAddress localAddress, int localPort) 
            throws SocketException, IOException {
        super.connect(address, port, localAddress, localPort);
    }

    /**
     * I work to be connected. Opens a Socket connected to a remote host 
     * at the specified port and originating from the specified 
     * local address and port.
     * @param address The name of the remote host.
     * @param port The port to connect to on the remote host.
     * @param localAddress The local address to use.
     * @param localPort The local port to use.
     * @throws SocketException If the socket timeout could not be set.
     * @throws IOException If the socket could not be opened. 
     * In most cases you will only want to catch IOException since 
     * SocketException is derived from it.
     * @see org.apache.commons.net.SocketClient 
     * #connect(java.lang.String, int, java.net.InetAddress, int)
     */
    public void connect(String address, int port, InetAddress localAddress,
            int localPort) throws SocketException, IOException {
        super.connect(address, port, localAddress, localPort);
    }

    /**
     * Because there are so many connect() methods, 
     * the _connectAction_() method is provided as a means of performing 
     * some action immediately after establishing a connection, 
     * rather than reimplementing all of the connect() methods.
     * @throws IOException If it throw by _connectAction_.
     * @see org.apache.commons.net.SocketClient#_connectAction_()
     */
    protected void _connectAction_() throws IOException {
        // Implicit mode.
        if (isImplicit) sslNegotiation();
        super._connectAction_();
        // Explicit mode.
        if (!isImplicit) {
            execAUTH();
            sslNegotiation();
        }
    }

    /**
     * I carry out an AUTH command.
     * @throws SSLException If it server reply code not equal "234" and "334".
     * @throws IOException If an I/O error occurs while either sending 
     * the command.
     */
    private void execAUTH() throws SSLException, IOException {
        int replyCode = sendCommand(
                FTPSCommand._commands[FTPSCommand.AUTH], auth);
        if (FTPSReply.SECURITY_MECHANISM_IS_OK == replyCode) {
            // replyCode = 334
            // I carry out an ADAT command.
        } else if (FTPSReply.SECURITY_DATA_EXCHANGE_COMPLETE != replyCode) {
            throw new SSLException(getReplyString());
        }
    }

    /**
     * SSL/TLS negotiation. I acquire an SSL socket of a control 
     * connection and carry out handshake processing.
     * @throws IOException A handicap breaks out by sever negotiation.
     */
    private void sslNegotiation() throws IOException {
        // Evacuation not ssl socket.
        planeSocket = _socket_;

        SSLSocketFactory ssf = context.getSocketFactory();
        String ip = _socket_.getInetAddress().getHostAddress();
        int port = _socket_.getPort();
        SSLSocket socket = 
            (SSLSocket) ssf.createSocket(_socket_, ip, port, true);
        socket.setEnableSessionCreation(isCreation);
        socket.setUseClientMode(isClientMode);
        // server mode
        if (!isClientMode) {
            socket.setNeedClientAuth(isNeedClientAuth);
            socket.setWantClientAuth(isWantClientAuth);
        }
        if (protocols != null) socket.setEnabledProtocols(protocols);
        if (suites != null) socket.setEnabledCipherSuites(suites);

        socket.startHandshake();

        _socket_ = socket;
        _controlInput_ = new BufferedReader(new InputStreamReader(
                socket .getInputStream(), getControlEncoding()));
        _controlOutput_ = new BufferedWriter(new OutputStreamWriter(
                socket.getOutputStream(), getControlEncoding()));
    }

    /**
     * Controls whether new SSL session may be established by this socket.
     * @param isCreation The established socket flag.
     */
    public void setEnabledSessionCreation(boolean isCreation) {
        this.isCreation = isCreation;
    }

    /**
     * Returns true if new SSL sessions may be established by this socket.
     * When a socket does not have a ssl socket, This return False.
     * @return true - Indicates that sessions may be created;
     * this is the default. 
     * false - indicates that an existing session must be resumed.
     */
    public boolean getEnableSeeionCreation() {
        if (_socket_ instanceof SSLSocket) 
            return ((SSLSocket)_socket_).getEnableSessionCreation();
        return false;
    }

    /**
     * Configures the socket to require client authentication.
     * @param isNeedClientAuth The need client auth flag.
     */
    public void setNeedClientAuth(boolean isNeedClientAuth) {
        this.isNeedClientAuth = isNeedClientAuth;
    }

    /**
     * Returns true if the socket will require client authentication.
     * When a socket does not have a ssl socket, This return False.
     * @return true - If the server mode socket should request 
     * that the client authenticate itself.
     */
    public boolean getNeedClientAuth() {
        if (_socket_ instanceof SSLSocket) 
            return ((SSLSocket)_socket_).getNeedClientAuth();
        return false;
    }

    /**
     * Configures the socket to request client authentication, 
     * but only if such a request is appropriate to the cipher 
     * suite negotiated.
     * @param isWantClientAuth The want client auth flag.
     */
    public void setWantClientAuth(boolean isWantClientAuth) {
        this.isWantClientAuth = isWantClientAuth;
    }

    /**
     * Returns true if the socket will request client authentication.
     * When a socket does not have a ssl socket, This return False.
     * @return true - If the server mode socket should request 
     * that the client authenticate itself.
     */
    public boolean getWantClientAuth() {
        if (_socket_ instanceof SSLSocket) 
            return ((SSLSocket)_socket_).getWantClientAuth();
        return false;
    }

    /**
     * Configures the socket to use client (or server) mode in its first 
     * handshake.
     * @param isClientMode The use client mode flag.
     */
    public void setUseClientMode(boolean isClientMode) {
        this.isClientMode = isClientMode;
    }

    /**
     * Returns true if the socket is set to use client mode 
     * in its first handshake.
     * When a socket does not have a ssl socket, This return False.
     * @return true - If the socket should start its first handshake 
     * in "client" mode.
     */
    public boolean getUseClientMode() {
        if (_socket_ instanceof SSLSocket) 
            return ((SSLSocket)_socket_).getUseClientMode();
        return false;
    }

    /**
     * Controls which particular cipher suites are enabled for use on this 
     * connection. I perform setting before a server negotiation.
     * @param suites The cipher suites.
     */
    public void setEnabledCipherSuites(String[] suites) {
        this.suites = suites;
    }

    /**
     * Returns the names of the cipher suites which could be enabled 
     * for use on this connection.
     * When a socket does not have a ssl socket, This return null.
     * @return An array of cipher suite names.
     */
    public String[] getEnabledCipherSuites() {
        if (_socket_ instanceof SSLSocket) 
            return ((SSLSocket)_socket_).getEnabledCipherSuites();
        return null;
    }

    /**
     * Controls which particular protocol versions are enabled for use on this
     * connection. I perform setting before a server negotiation.
     * @param protocols The protocol versions.
     */
    public void setEnabledProtocols(String[] protocols) {
        this.protocols = protocols;
    }

    /**
     * Returns the names of the protocol versions which are currently 
     * enabled for use on this connection.
     * When a socket does not have a ssl socket, This return null.
     * @return An array of protocols.
     */
    public String[] getEnabledProtocols() {
        if (_socket_ instanceof SSLSocket) 
            return ((SSLSocket)_socket_).getEnabledProtocols();
        return null;
    }

    /**
     * I carry out an PBSZ command. pbsz value: 0 to (2^32)-1 decimal integer.
     * @param pbsz Protection Buffer Size.
     * @throws SSLException If it server reply code not equal "200".
     * @throws IOException If an I/O error occurs while either sending 
     * the command.
     */
    public void execPBSZ(long pbsz) throws SSLException, IOException {
        if (pbsz < 0 || 4294967295L < pbsz) 
            throw new IllegalArgumentException();
        if (FTPSReply.COMMAND_OK != sendCommand(
                FTPSCommand._commands[FTPSCommand.PBSZ],String.valueOf(pbsz)))
            throw new SSLException(getReplyString());
    }

    /**
     * I carry out an PROT command.</br>
     * C - Clear</br>
     * S - Safe(SSL protocol only)</br>
     * E - Confidential(SSL protocol only)</br>
     * P - Private
     * @param prot Data Channel Protection Level.
     * @throws SSLException If it server reply code not equal "200".
     * @throws IOException If an I/O error occurs while either sending 
     * the command.
     */
    public void execPROT(String prot) throws SSLException, IOException {
        if (prot == null) prot = DEFAULT_PROT;
        if (!checkPROTValue(prot)) throw new IllegalArgumentException();
        if (FTPSReply.COMMAND_OK != sendCommand(
                FTPSCommand._commands[FTPSCommand.PROT], prot)) 
            throw new SSLException(getReplyString());
        if (DEFAULT_PROT.equals(prot)) {
            setSocketFactory(null);
        } else {
            setSocketFactory(new FTPSSocketFactory(context));
        }
    }

    /**
     * I check the value that I can set in PROT Command value.
     * @param prot Data Channel Protection Level.
     * @return True - A set point is right / False - A set point is not right
     */
    private boolean checkPROTValue(String prot) {
        for (int p = 0; p < PROT_COMMAND_VALUE.length; p++) {
            if (PROT_COMMAND_VALUE[p].equals(prot)) return true;
        }
        return false;
    }

    /**
     * I carry out an ftp command.
     * When a CCC command was carried out, I steep socket and SocketFactory 
     * in a state of not ssl.
     * @parm command ftp command.
     * @return server reply.
     * @throws IOException If an I/O error occurs while either sending 
     * the command.
     * @see org.apache.commons.net.ftp.FTP#sendCommand(java.lang.String)
     */
    public int sendCommand(String command, String args) throws IOException {
        int repCode = super.sendCommand(command, args);
        if (FTPSCommand._commands[FTPSCommand.CCC].equals(command)) {
            if (FTPSReply.COMMAND_OK == repCode) {
                _socket_ = planeSocket;
                setSocketFactory(null);
            } else {
                throw new SSLException(getReplyString());
            }
        }
        return repCode;
    }

    /**
     * I return a socket of the data connection that I acquired. 
     * When I ssl it and communicate, I return the SSL socket which 
     * carried out handshake processing.
     * @pram command The text representation of the FTP command to send.
     * @param arg The arguments to the FTP command. 
     * If this parameter is set to null, then the command is sent with 
     * no argument.
     * @return A Socket corresponding to the established data connection. 
     * Null is returned if an FTP protocol error is reported at any point 
     * during the establishment and initialization of the connection.
     * @throws IOException If there is any problem with the connection.
     * @see org.apache.commons.net.ftp.FTPCliente
     * #_openDataConnection_(java.lang.String, int)
     */
    protected Socket _openDataConnection_(int command, String arg)
            throws IOException {
        Socket socket = super._openDataConnection_(command, arg);
        if (socket != null && socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket)socket;
            sslSocket.setUseClientMode(isClientMode);
            sslSocket.setEnableSessionCreation(isCreation);
            // server mode
            if (!isClientMode) {
                sslSocket.setNeedClientAuth(isNeedClientAuth);
                sslSocket.setWantClientAuth(isWantClientAuth);
            }
            if (suites != null)
                sslSocket.setEnabledCipherSuites(suites);
            if (protocols != null)
                sslSocket.setEnabledProtocols(protocols);
            sslSocket.startHandshake();
        }
        return socket;
    }
}
