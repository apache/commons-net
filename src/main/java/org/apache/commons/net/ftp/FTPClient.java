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
package org.apache.commons.net.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.MalformedServerReplyException;
import org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory;
import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;
import org.apache.commons.net.ftp.parser.MLSxEntryParser;
import org.apache.commons.net.io.CRLFLineReader;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.commons.net.io.FromNetASCIIInputStream;
import org.apache.commons.net.io.SocketOutputStream;
import org.apache.commons.net.io.ToNetASCIIOutputStream;
import org.apache.commons.net.io.Util;
import org.apache.commons.net.util.NetConstants;

/**
 * FTPClient encapsulates all the functionality necessary to store and retrieve files from an FTP server. This class takes care of all low level details of
 * interacting with an FTP server and provides a convenient higher level interface. As with all classes derived from
 * {@link org.apache.commons.net.SocketClient}, you must first connect to the server with {@link org.apache.commons.net.SocketClient#connect connect} before
 * doing anything, and finally {@link org.apache.commons.net.SocketClient#disconnect() disconnect} after you're completely finished interacting with the server.
 * Then you need to check the FTP reply code to see if the connection was successful. For example:
 *
 * <pre>
 *    FTPClient ftp = new FTPClient();
 *    FTPClientConfig config = new FTPClientConfig();
 *    config.setXXX(YYY); // change required options
 *    // for example config.setServerTimeZoneId("Pacific/Pitcairn")
 *    ftp.configure(config );
 *    boolean error = false;
 *    try {
 *      int reply;
 *      String server = "ftp.example.com";
 *      ftp.connect(server);
 *      System.out.println("Connected to " + server + ".");
 *      System.out.print(ftp.getReplyString());
 *
 *      // After connection attempt, you should check the reply code to verify
 *      // success.
 *      reply = ftp.getReplyCode();
 *
 *      if (!FTPReply.isPositiveCompletion(reply)) {
 *        ftp.disconnect();
 *        System.err.println("FTP server refused connection.");
 *        System.exit(1);
 *      }
 *      ... // transfer files
 *      ftp.logout();
 *    } catch (IOException e) {
 *      error = true;
 *      e.printStackTrace();
 *    } finally {
 *      if (ftp.isConnected()) {
 *        try {
 *          ftp.disconnect();
 *        } catch (IOException ioe) {
 *          // do nothing
 *        }
 *      }
 *      System.exit(error ? 1 : 0);
 *    }
 * </pre>
 * <p>
 * Immediately after connecting is the only real time you need to check the reply code (because connect is of type void). The convention for all the FTP command
 * methods in FTPClient is such that they either return a boolean value or some other value. The boolean methods return true on a successful completion reply
 * from the FTP server and false on a reply resulting in an error condition or failure. The methods returning a value other than boolean return a value
 * containing the higher level data produced by the FTP command, or null if a reply resulted in an error condition or failure. If you want to access the exact
 * FTP reply code causing a success or failure, you must call {@link org.apache.commons.net.ftp.FTP#getReplyCode getReplyCode} after a success or failure.
 * </p>
 * <p>
 * The default settings for FTPClient are for it to use {@link FTP#ASCII_FILE_TYPE}, {@link FTP#NON_PRINT_TEXT_FORMAT}, {@link FTP#STREAM_TRANSFER_MODE}, and
 * {@link FTP#FILE_STRUCTURE}. The only file types directly supported are {@link FTP#ASCII_FILE_TYPE} and {@link FTP#BINARY_FILE_TYPE}. Because there are at
 * least 4 different EBCDIC encodings, we have opted not to provide direct support for EBCDIC. To transfer EBCDIC and other unsupported file types you must
 * create your own filter InputStreams and OutputStreams and wrap them around the streams returned or required by the FTPClient methods. FTPClient uses the
 * {@link ToNetASCIIOutputStream NetASCII} filter streams to provide transparent handling of ASCII files. We will consider incorporating EBCDIC support if there
 * is enough demand.
 * </p>
 * <p>
 * {@link FTP#NON_PRINT_TEXT_FORMAT}, {@link FTP#STREAM_TRANSFER_MODE}, and {@link FTP#FILE_STRUCTURE} are the only supported formats, transfer modes, and file
 * structures.
 * </p>
 * <p>
 * Because the handling of sockets on different platforms can differ significantly, the FTPClient automatically issues a new PORT (or EPRT) command prior to
 * every transfer requiring that the server connect to the client's data port. This ensures identical problem-free behavior on Windows, Unix, and Macintosh
 * platforms. Additionally, it relieves programmers from having to issue the PORT (or EPRT) command themselves and dealing with platform dependent issues.
 * </p>
 * <p>
 * Additionally, for security purposes, all data connections to the client are verified to ensure that they originated from the intended party (host and port).
 * If a data connection is initiated by an unexpected party, the command will close the socket and throw an IOException. You may disable this behavior with
 * {@link #setRemoteVerificationEnabled setRemoteVerificationEnabled()}.
 * </p>
 * <p>
 * You should keep in mind that the FTP server may choose to prematurely close a connection if the client has been idle for longer than a given time period
 * (usually 900 seconds). The FTPClient class will detect a premature FTP server connection closing when it receives a
 * {@link org.apache.commons.net.ftp.FTPReply#SERVICE_NOT_AVAILABLE FTPReply.SERVICE_NOT_AVAILABLE} response to a command. When that occurs, the FTP class
 * method encountering that reply will throw an {@link org.apache.commons.net.ftp.FTPConnectionClosedException}. {@link FTPConnectionClosedException} is a
 * subclass of {@code IOException} and therefore need not be caught separately, but if you are going to catch it separately, its catch block must appear before
 * the more general {@code IOException} catch block. When you encounter an {@link org.apache.commons.net.ftp.FTPConnectionClosedException} , you must disconnect
 * the connection with {@link #disconnect disconnect()} to properly clean up the system resources used by FTPClient. Before disconnecting, you may check the
 * last reply code and text with {@link org.apache.commons.net.ftp.FTP#getReplyCode getReplyCode}, {@link org.apache.commons.net.ftp.FTP#getReplyString
 * getReplyString }, and {@link org.apache.commons.net.ftp.FTP#getReplyStrings getReplyStrings}. You may avoid server disconnections while the client is idle by
 * periodically sending NOOP commands to the server.
 * </p>
 * <p>
 * Rather than list it separately for each method, we mention here that every method communicating with the server and throwing an IOException can also throw a
 * {@link org.apache.commons.net.MalformedServerReplyException} , which is a subclass of IOException. A MalformedServerReplyException will be thrown when the
 * reply received from the server deviates enough from the protocol specification that it cannot be interpreted in a useful manner despite attempts to be as
 * lenient as possible.
 * </p>
 * <p>
 * Listing API Examples Both paged and unpaged examples of directory listings are available, as follows:
 * </p>
 * <p>
 * Unpaged (whole list) access, using a parser accessible by auto-detect:
 * </p>
 *
 * <pre>
 * FTPClient f = new FTPClient();
 * f.connect(server);
 * f.login(user, password);
 * FTPFile[] files = f.listFiles(directory);
 * </pre>
 * <p>
 * Paged access, using a parser not accessible by auto-detect. The class defined in the first parameter of initateListParsing should be derived from
 * org.apache.commons.net.FTPFileEntryParser:
 * </p>
 *
 * <pre>
 * FTPClient f = new FTPClient();
 * f.connect(server);
 * f.login(user, password);
 * FTPListParseEngine engine = f.initiateListParsing("com.whatever.YourOwnParser", directory);
 *
 * while (engine.hasNext()) {
 *     FTPFile[] files = engine.getNext(25); // "page size" you want
 *     // do whatever you want with these files, display them, etc.
 *     // expensive FTPFile objects not created until needed.
 * }
 * </pre>
 * <p>
 * Paged access, using a parser accessible by auto-detect:
 * </p>
 *
 * <pre>
 * FTPClient f = new FTPClient();
 * f.connect(server);
 * f.login(user, password);
 * FTPListParseEngine engine = f.initiateListParsing(directory);
 *
 * while (engine.hasNext()) {
 *     FTPFile[] files = engine.getNext(25); // "page size" you want
 *     // do whatever you want with these files, display them, etc.
 *     // expensive FTPFile objects not created until needed.
 * }
 * </pre>
 * <p>
 * For examples of using FTPClient on servers whose directory listings
 * </p>
 * <ul>
 * <li>use languages other than English</li>
 * <li>use date formats other than the American English "standard" {@code MM d yyyy}</li>
 * <li>are in different time zones and you need accurate timestamps for dependency checking as in Ant</li>
 * </ul>
 * see {@link FTPClientConfig FTPClientConfig}.
 * <p>
 * <strong>Control channel keep-alive feature</strong>:
 * </p>
 * <p>
 * <strong>Please note:</strong> this does not apply to the methods where the user is responsible for writing or reading the data stream, i.e.
 * {@link #retrieveFileStream(String)} , {@link #storeFileStream(String)} and the other xxxFileStream methods
 * </p>
 * <p>
 * During file transfers, the data connection is busy, but the control connection is idle. FTP servers know that the control connection is in use, so won't
 * close it through lack of activity, but it's a lot harder for network routers to know that the control and data connections are associated with each other.
 * Some routers may treat the control connection as idle, and disconnect it if the transfer over the data connection takes longer than the allowable idle time
 * for the router.
 * <p>
 * One solution to this is to send a safe command (i.e. NOOP) over the control connection to reset the router's idle timer. This is enabled as follows:
 * </p>
 *
 * <pre>
 * // Set timeout to 5 minutes
 * ftpClient.setControlKeepAliveTimeout(Duration.ofMinutes(5));
 * </pre>
 *
 * <p>
 * This will cause the file upload/download methods to send a NOOP approximately every 5 minutes. The following public methods support this:
 * </p>
 * <ul>
 * <li>{@link #retrieveFile(String, OutputStream)}</li>
 * <li>{@link #appendFile(String, InputStream)}</li>
 * <li>{@link #storeFile(String, InputStream)}</li>
 * <li>{@link #storeUniqueFile(InputStream)}</li>
 * <li>{@link #storeUniqueFileStream(String)}</li>
 * </ul>
 * <p>
 * This feature does not apply to the methods where the user is responsible for writing or reading the data stream, i.e. {@link #retrieveFileStream(String)} ,
 * {@link #storeFileStream(String)} and the other xxxFileStream methods. In such cases, the user is responsible for keeping the control connection alive if
 * necessary.
 * </p>
 * <p>
 * The implementation currently uses a {@link CopyStreamListener} which is passed to the
 * {@link Util#copyStream(InputStream, OutputStream, int, long, CopyStreamListener, boolean)} method, so the timing is partially dependent on how long each
 * block transfer takes.
 * </p>
 * <p>
 * <strong>This keep-alive feature is optional; if it does not help or causes problems then don't use it.</strong>
 * </p>
 *
 * @see #FTP_SYSTEM_TYPE
 * @see #SYSTEM_TYPE_PROPERTIES
 * @see FTP
 * @see FTPConnectionClosedException
 * @see FTPFileEntryParser
 * @see FTPFileEntryParserFactory
 * @see DefaultFTPFileEntryParserFactory
 * @see FTPClientConfig
 * @see org.apache.commons.net.MalformedServerReplyException
 */
public class FTPClient extends FTP implements Configurable {

    private static final class CSL implements CopyStreamListener {

        private final FTPClient parent;
        private final long idleMillis;
        private final int currentSoTimeoutMillis;

        private long lastIdleTimeMillis = System.currentTimeMillis();
        private int notAcked;
        private int acksAcked;
        private int ioErrors;

        CSL(final FTPClient parent, final Duration idleDuration, final Duration maxWaitDuration) throws SocketException {
            this.idleMillis = idleDuration.toMillis();
            this.parent = parent;
            this.currentSoTimeoutMillis = parent.getSoTimeout();
            parent.setSoTimeout(DurationUtils.toMillisInt(maxWaitDuration));
        }

        @Override
        public void bytesTransferred(final CopyStreamEvent event) {
            bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
        }

        @Override
        public void bytesTransferred(final long totalBytesTransferred, final int bytesTransferred, final long streamSize) {
            final long nowMillis = System.currentTimeMillis();
            if (nowMillis - lastIdleTimeMillis > idleMillis) {
                try {
                    parent.__noop();
                    acksAcked++;
                } catch (final SocketTimeoutException e) {
                    notAcked++;
                } catch (final IOException e) {
                    ioErrors++;
                    // Ignored
                }
                lastIdleTimeMillis = nowMillis;
            }
        }

        int[] cleanUp() throws IOException {
            final int remain = notAcked;
            try {
                while (notAcked > 0) {
                    parent.getReply(); // we do want to see these
                    notAcked--; // only decrement if actually received
                }
            } catch (final SocketTimeoutException e) { // NET-584
                // ignored
            } finally {
                parent.setSoTimeout(currentSoTimeoutMillis);
            }
            return new int[] { acksAcked, remain, notAcked, ioErrors }; // debug counts
        }

    }

    /**
     * Strategy interface for updating host names received from FTP server for passive NAT workaround.
     *
     * @since 3.6
     */
    public interface HostnameResolver {

        /**
         * Resolves a host name.
         *
         * @param hostname the hostname to resolve.
         * @return The resolved hostname.
         * @throws UnknownHostException if the host is unknown.
         */
        String resolve(String hostname) throws UnknownHostException;
    }

    /**
     * Default strategy for passive NAT workaround (site-local replies are replaced.)
     *
     * @since 3.6
     */
    public static class NatServerResolverImpl implements HostnameResolver {
        private final FTPClient client;

        /**
         * Constructs a new instance.
         *
         * @param client the FTP client.
         */
        public NatServerResolverImpl(final FTPClient client) {
            this.client = client;
        }

        @Override
        public String resolve(final String hostname) throws UnknownHostException {
            String newHostname = hostname;
            final InetAddress host = InetAddress.getByName(newHostname);
            // reply is a local address, but target is not - assume NAT box changed the PASV reply
            if (host.isSiteLocalAddress()) {
                final InetAddress remote = client.getRemoteAddress();
                if (!remote.isSiteLocalAddress()) {
                    newHostname = remote.getHostAddress();
                }
            }
            return newHostname;
        }
    }

    private static final class PropertiesSingleton {
        static final Properties PROPERTIES = loadResourceProperties(SYSTEM_TYPE_PROPERTIES);
    }

    /**
     * The system property ({@value}) which can be used to override the system type.<br>
     * If defined, the value will be used to create any automatically created parsers.
     *
     * @since 3.0
     */
    public static final String FTP_SYSTEM_TYPE = "org.apache.commons.net.ftp.systemType";

    /**
     * The system property ({@value}) which can be used as the default system type.<br>
     * If defined, the value will be used if the SYST command fails.
     *
     * @since 3.1
     */
    public static final String FTP_SYSTEM_TYPE_DEFAULT = "org.apache.commons.net.ftp.systemType.default";

    /**
     * The system property that defines the default for {@link #isIpAddressFromPasvResponse()}. This property, if present, configures the default for the
     * following: If the client receives the servers response for a PASV request, then that response will contain an IP address. If this property is true, then
     * the client will use that IP address, as requested by the server. This is compatible to version {@code 3.8.0}, and before. If this property is false, or
     * absent, then the client will ignore that IP address, and instead use the remote address of the control connection.
     *
     * @see #isIpAddressFromPasvResponse()
     * @see #setIpAddressFromPasvResponse(boolean)
     * @since 3.9.0
     */
    public static final String FTP_IP_ADDRESS_FROM_PASV_RESPONSE = "org.apache.commons.net.ftp.ipAddressFromPasvResponse";

    /**
     * The name of an optional systemType properties file ({@value}), which is loaded using {@link Class#getResourceAsStream(String)}.<br>
     * The entries are the systemType (as determined by {@link FTPClient#getSystemType}) and the values are the replacement type or parserClass, which is passed
     * to {@link FTPFileEntryParserFactory#createFileEntryParser(String)}.<br>
     * For example:
     *
     * <pre>
     * Plan 9=Unix
     * OS410=org.apache.commons.net.ftp.parser.OS400FTPEntryParser
     * </pre>
     *
     * @since 3.0
     */
    public static final String SYSTEM_TYPE_PROPERTIES = "/systemType.properties";

    /**
     * A constant indicating the FTP session is expecting all transfers to occur between the client (local) and server and that the server should connect to the
     * client's data port to initiate a data transfer. This is the default data connection mode when and FTPClient instance is created.
     */
    public static final int ACTIVE_LOCAL_DATA_CONNECTION_MODE = 0;

    /**
     * A constant indicating the FTP session is expecting all transfers to occur between two remote servers and that the server the client is connected to
     * should connect to the other server's data port to initiate a data transfer.
     */
    public static final int ACTIVE_REMOTE_DATA_CONNECTION_MODE = 1;

    /**
     * A constant indicating the FTP session is expecting all transfers to occur between the client (local) and server and that the server is in passive mode,
     * requiring the client to connect to the server's data port to initiate a transfer.
     */
    public static final int PASSIVE_LOCAL_DATA_CONNECTION_MODE = 2;

    /**
     * A constant indicating the FTP session is expecting all transfers to occur between two remote servers and that the server the client is connected to is in
     * passive mode, requiring the other server to connect to the first server's data port to initiate a data transfer.
     */
    public static final int PASSIVE_REMOTE_DATA_CONNECTION_MODE = 3;

    /** Pattern for PASV mode responses. Groups: (n,n,n,n),(n),(n) */
    private static final Pattern PARMS_PAT = Pattern.compile("(\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3}),(\\d{1,3}),(\\d{1,3})");

    private static Properties getOverrideProperties() {
        return PropertiesSingleton.PROPERTIES;
    }

    static Properties loadResourceProperties(final String systemTypeProperties) {
        Properties properties = null;
        if (systemTypeProperties != null) {
            try (InputStream inputStream = FTPClient.class.getResourceAsStream(systemTypeProperties)) {
                if (inputStream != null) {
                    properties = new Properties();
                    properties.load(inputStream);
                }
            } catch (final IOException ignore) {
                // ignore
            }
        }
        return properties;
    }

    /**
     * Parse the path from a CWD reply.
     * <p>
     * According to <a href="http://www.ietf.org/rfc/rfc959.txt">RFC959</a>, it should be the same as for MKD i.e.
     * {@code 257<space>"<directory-name>"[<space>commentary]} where any double-quotes in {@code <directory-name>} are doubled. Unlike MKD, the commentary is
     * optional.
     * </p>
     * <p>
     * However, see NET-442 for an exception.
     * </p>
     *
     * @param reply
     * @return the path, without enclosing quotes, or the full string after the reply code and space if the syntax is invalid (i.e. enclosing quotes are
     *         missing or embedded quotes are not doubled)
     */
    // package protected for access by test cases
    static String parsePathname(final String reply) {
        final String param = reply.substring(REPLY_CODE_LEN + 1);
        if (param.startsWith("\"")) {
            final StringBuilder sb = new StringBuilder(param.length());
            boolean quoteSeen = false;
            // start after initial quote
            for (int i = 1; i < param.length(); i++) {
                final char ch = param.charAt(i);
                if (ch == '"') {
                    if (quoteSeen) {
                        sb.append(ch);
                        quoteSeen = false;
                    } else {
                        // don't output yet, in case doubled
                        quoteSeen = true;
                    }
                } else {
                    if (quoteSeen) { // found lone trailing quote within string
                        return sb.toString();
                    }
                    sb.append(ch); // just another character
                }
            }
            if (quoteSeen) { // found lone trailing quote at end of string
                return sb.toString();
            }
        }
        // malformed reply, return all after reply code and space
        return param;
    }

    private int dataConnectionMode;
    private Duration dataTimeout = Duration.ofMillis(-1);

    private int passivePort;
    private String passiveHost;
    private final Random random = new Random();
    private int activeMinPort;
    private int activeMaxPort;
    private InetAddress activeExternalHost;

    /** Overrides activeExternalHost in EPRT/PORT commands. */
    private InetAddress reportActiveExternalHost;

    /** The address to bind to on passive connections, if necessary. */
    private InetAddress passiveLocalHost;
    private int fileType;
    @SuppressWarnings("unused") // fields are written, but currently not read
    private int formatOrByteSize;
    @SuppressWarnings("unused") // field is written, but currently not read
    private int fileStructure;
    private int fileTransferMode;

    private boolean remoteVerificationEnabled = true;

    private long restartOffset;

    private FTPFileEntryParserFactory parserFactory = new DefaultFTPFileEntryParserFactory();

    /**
     * For buffered data streams.
     */
    private int bufferSize;

    private int sendDataSocketBufferSize;

    private int receiveDataSocketBufferSize;

    private boolean listHiddenFiles;

    /**
     * Whether to attempt EPSV with an IPv4 connection.
     */
    private boolean useEPSVwithIPv4;

    /**
     * A cached value that should not be referenced directly except when assigned in getSystemName and initDefaults.
     */
    private String systemName;

    /**
     * A cached value that should not be referenced directly except when assigned in listFiles(String, String) and initDefaults.
     */
    private FTPFileEntryParser entryParser;

    /**
     * Key used to create the parser; necessary to ensure that the parser type is not ignored.
     */
    private String entryParserKey;

    private FTPClientConfig ftpClientConfig;

    /**
     * Listener used by store/retrieve methods to handle keepalive.
     */
    private CopyStreamListener copyStreamListener;

    /**
     * How long to wait before sending another control keep-alive message.
     */
    private Duration controlKeepAliveTimeout = Duration.ZERO;

    /**
     * How long to wait for keepalive message replies before continuing. Most FTP servers don't seem to support concurrent control and data connection usage.
     */
    private Duration controlKeepAliveReplyTimeout = Duration.ofSeconds(1);

    /**
     * Debug counts for NOOP acks.
     */
    private int[] cslDebug;

    /**
     * Enable or disable replacement of internal IP in passive mode. Default enabled using {code NatServerResolverImpl}.
     */
    private HostnameResolver passiveNatWorkaroundStrategy = new NatServerResolverImpl(this);

    /** Controls the automatic server encoding detection (only UTF-8 supported). */
    private boolean autoDetectEncoding;

    /** Map of FEAT responses. If null, has not been initialized. */
    private HashMap<String, Set<String>> featuresMap;

    private boolean ipAddressFromPasvResponse = Boolean.getBoolean(FTP_IP_ADDRESS_FROM_PASV_RESPONSE);

    /**
     * Default FTPClient constructor. Creates a new FTPClient instance with the data connection mode set to {@link #ACTIVE_LOCAL_DATA_CONNECTION_MODE}, the file
     * type set to {@link FTP#ASCII_FILE_TYPE}, the file format set to {@link FTP#NON_PRINT_TEXT_FORMAT}, the file structure set to {@link FTP#FILE_STRUCTURE},
     * and the transfer mode set to {@link FTP#STREAM_TRANSFER_MODE}.
     * <p>
     * The list parsing auto-detect feature can be configured to use lenient future dates (short dates may be up to one day in the future) as follows:
     * </p>
     *
     * <pre>
     * FTPClient ftp = new FTPClient();
     * FTPClientConfig config = new FTPClientConfig();
     * config.setLenientFutureDates(true);
     * ftp.configure(config);
     * </pre>
     */
    public FTPClient() {
        initDefaults();
    }

    @Override
    protected void _connectAction_() throws IOException {
        _connectAction_(null);
    }

    /**
     * @param socketIsReader the reader to reuse (if non-null)
     * @throws IOException on error
     * @since 3.4
     */
    @Override
    protected void _connectAction_(final Reader socketIsReader) throws IOException {
        super._connectAction_(socketIsReader); // sets up _input_ and _output_
        initDefaults();
        // must be after super._connectAction_(), because otherwise we get an
        // Exception claiming we're not connected
        if (autoDetectEncoding) {
            final ArrayList<String> oldReplyLines = new ArrayList<>(_replyLines);
            final int oldReplyCode = _replyCode;
            // UTF-8 appears to be the default
            final Charset utf8 = StandardCharsets.UTF_8;
            if (hasFeature("UTF8") || hasFeature(utf8.name())) {
                setControlEncoding(utf8);
                _controlInput_ = new CRLFLineReader(new InputStreamReader(_input_, getControlEncoding()));
                _controlOutput_ = new BufferedWriter(new OutputStreamWriter(_output_, getControlEncoding()));
            }
            // restore the original reply (server greeting)
            _replyLines.clear();
            _replyLines.addAll(oldReplyLines);
            _replyCode = oldReplyCode;
            _newReplyString = true;
        }
    }

    /**
     * Establishes a data connection with the FTP server, returning a Socket for the connection if successful. If a restart offset has been set with
     * {@link #setRestartOffset(long)}, a REST command is issued to the server with the offset as an argument before establishing the data connection. Active
     * mode connections also cause a local PORT command to be issued.
     *
     * @param command The int representation of the FTP command to send.
     * @param arg     The arguments to the FTP command. If this parameter is set to null, then the command is sent with no argument.
     * @return A Socket corresponding to the established data connection. Null is returned if an FTP protocol error is reported at any point during the
     *         establishment and initialization of the connection.
     * @throws IOException If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     * @since 3.3
     */
    protected Socket _openDataConnection_(final FTPCmd command, final String arg) throws IOException {
        return _openDataConnection_(command.getCommand(), arg);
    }

    /**
     * Establishes a data connection with the FTP server, returning a Socket for the connection if successful. If a restart offset has been set with
     * {@link #setRestartOffset(long)}, a REST command is issued to the server with the offset as an argument before establishing the data connection. Active
     * mode connections also cause a local PORT command to be issued.
     *
     * @param command The int representation of the FTP command to send.
     * @param arg     The arguments to the FTP command. If this parameter is set to null, then the command is sent with no argument.
     * @return A Socket corresponding to the established data connection. Null is returned if an FTP protocol error is reported at any point during the
     *         establishment and initialization of the connection.
     * @throws IOException If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     * @deprecated (3.3) Use {@link #_openDataConnection_(FTPCmd, String)} instead
     */
    @Deprecated
    protected Socket _openDataConnection_(final int command, final String arg) throws IOException {
        return _openDataConnection_(FTPCommand.getCommand(command), arg);
    }

    /**
     * Establishes a data connection with the FTP server, returning a Socket for the connection if successful. If a restart offset has been set with
     * {@link #setRestartOffset(long)}, a REST command is issued to the server with the offset as an argument before establishing the data connection. Active
     * mode connections also cause a local PORT command to be issued.
     *
     * @param command The text representation of the FTP command to send.
     * @param arg     The arguments to the FTP command. If this parameter is set to null, then the command is sent with no argument.
     * @return A Socket corresponding to the established data connection. Null is returned if an FTP protocol error is reported at any point during the
     *         establishment and initialization of the connection.
     * @throws IOException If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     * @since 3.1
     */
    protected Socket _openDataConnection_(final String command, final String arg) throws IOException {
        if (dataConnectionMode != ACTIVE_LOCAL_DATA_CONNECTION_MODE && dataConnectionMode != PASSIVE_LOCAL_DATA_CONNECTION_MODE) {
            return null;
        }
        final boolean isInet6Address = getRemoteAddress() instanceof Inet6Address;
        final Socket socket;
        final int soTimeoutMillis = DurationUtils.toMillisInt(dataTimeout);
        if (dataConnectionMode == ACTIVE_LOCAL_DATA_CONNECTION_MODE) {
            // if no activePortRange was set (correctly) -> getActivePort() = 0
            // -> new ServerSocket(0) -> bind to any free local port
            try (ServerSocket server = _serverSocketFactory_.createServerSocket(getActivePort(), 1, getHostAddress())) {
                // Try EPRT only if remote server is over IPv6, if not use PORT,
                // because EPRT has no advantage over PORT on IPv4.
                // It could even have the disadvantage,
                // that EPRT will make the data connection fail, because
                // today's intelligent NAT Firewalls are able to
                // substitute IP addresses in the PORT command,
                // but might not be able to recognize the EPRT command.
                if (isInet6Address) {
                    if (!FTPReply.isPositiveCompletion(eprt(getReportHostAddress(), server.getLocalPort()))) {
                        return null;
                    }
                } else if (!FTPReply.isPositiveCompletion(port(getReportHostAddress(), server.getLocalPort()))) {
                    return null;
                }
                if (restartOffset > 0 && !restart(restartOffset)) {
                    return null;
                }
                if (!FTPReply.isPositivePreliminary(sendCommand(command, arg))) {
                    return null;
                }
                // For now, let's just use the data timeout value for waiting for
                // the data connection. It may be desirable to let this be a
                // separately configurable value. In any case, we really want
                // to allow preventing the accept from blocking indefinitely.
                if (soTimeoutMillis >= 0) {
                    server.setSoTimeout(soTimeoutMillis);
                }
                socket = wrapOnDeflate(server.accept());
                // Ensure the timeout is set before any commands are issued on the new socket
                if (soTimeoutMillis >= 0) {
                    socket.setSoTimeout(soTimeoutMillis);
                }
                if (receiveDataSocketBufferSize > 0) {
                    socket.setReceiveBufferSize(receiveDataSocketBufferSize);
                }
                if (sendDataSocketBufferSize > 0) {
                    socket.setSendBufferSize(sendDataSocketBufferSize);
                }
            }
        } else {
            // We must be in PASSIVE_LOCAL_DATA_CONNECTION_MODE
            // Try EPSV command first on IPv6 - and IPv4 if enabled.
            // When using IPv4 with NAT it has the advantage
            // to work with more rare configurations.
            // E.g. if FTP server has a static PASV address (external network)
            // and the client is coming from another internal network.
            // In that case the data connection after PASV command would fail,
            // while EPSV would make the client succeed by taking just the port.
            final boolean attemptEPSV = isUseEPSVwithIPv4() || isInet6Address;
            if (attemptEPSV && epsv() == FTPReply.ENTERING_EPSV_MODE) {
                _parseExtendedPassiveModeReply(_replyLines.get(0));
            } else {
                if (isInet6Address) {
                    return null; // Must use EPSV for IPV6
                }
                // If EPSV failed on IPV4, revert to PASV
                if (pasv() != FTPReply.ENTERING_PASSIVE_MODE) {
                    return null;
                }
                _parsePassiveModeReply(_replyLines.get(0));
            }
            socket = wrapOnDeflate(_socketFactory_.createSocket());
            if (receiveDataSocketBufferSize > 0) {
                socket.setReceiveBufferSize(receiveDataSocketBufferSize);
            }
            if (sendDataSocketBufferSize > 0) {
                socket.setSendBufferSize(sendDataSocketBufferSize);
            }
            if (passiveLocalHost != null) {
                socket.bind(new InetSocketAddress(passiveLocalHost, 0));
            }
            // For now, let's just use the data timeout value for waiting for
            // the data connection. It may be desirable to let this be a
            // separately configurable value. In any case, we really want
            // to allow preventing the accept from blocking indefinitely.
            if (soTimeoutMillis >= 0) {
                socket.setSoTimeout(soTimeoutMillis);
            }
            socket.connect(new InetSocketAddress(passiveHost, passivePort), connectTimeout);
            if (restartOffset > 0 && !restart(restartOffset)) {
                socket.close();
                return null;
            }
            if (!FTPReply.isPositivePreliminary(sendCommand(command, arg))) {
                socket.close();
                return null;
            }
        }
        if (remoteVerificationEnabled && !verifyRemote(socket)) {
            // Grab the host before we close the socket to avoid NET-663
            final String socketHostAddress = getHostAddress(socket);
            final String remoteHostAddress = getHostAddress(_socket_);
            IOUtils.closeQuietly(socket);
            throw new IOException("Host attempting data connection " + socketHostAddress + " is not same as server " + remoteHostAddress);
        }
        return socket;
    }

    /**
     * Parses a reply.
     *
     * @param reply the reply to parse.
     * @throws MalformedServerReplyException if the reply is malformed.
     */
    protected void _parseExtendedPassiveModeReply(String reply) throws MalformedServerReplyException {
        reply = reply.substring(reply.indexOf('(') + 1, reply.indexOf(')')).trim();
        final char delim1 = reply.charAt(0);
        final char delim2 = reply.charAt(1);
        final char delim3 = reply.charAt(2);
        final char delim4 = reply.charAt(reply.length() - 1);
        if (delim1 != delim2 || delim2 != delim3 || delim3 != delim4) {
            throw new MalformedServerReplyException("Could not parse extended passive host information.\nServer Reply: " + reply);
        }
        final int port;
        try {
            port = Integer.parseInt(reply.substring(3, reply.length() - 1));
        } catch (final NumberFormatException e) {
            throw new MalformedServerReplyException("Could not parse extended passive host information.\nServer Reply: " + reply);
        }
        // in EPSV mode, the passive host address is implicit
        passiveHost = getRemoteAddress().getHostAddress();
        passivePort = port;
    }

    /**
     * Parses a reply.
     *
     * @param reply the reply to parse
     * @throws MalformedServerReplyException if the server reply does not match (n,n,n,n),(n),(n)
     * @since 3.1
     */
    protected void _parsePassiveModeReply(final String reply) throws MalformedServerReplyException {
        final Matcher m = PARMS_PAT.matcher(reply);
        if (!m.find()) {
            throw new MalformedServerReplyException("Could not parse passive host information.\nServer Reply: " + reply);
        }
        final int pasvPort;
        // Fix up to look like IP address
        String pasvHost = "0,0,0,0".equals(m.group(1)) ? _socket_.getInetAddress().getHostAddress() : m.group(1).replace(',', '.');
        try {
            final int oct1 = Integer.parseInt(m.group(2));
            final int oct2 = Integer.parseInt(m.group(3));
            pasvPort = oct1 << 8 | oct2;
        } catch (final NumberFormatException e) {
            throw new MalformedServerReplyException("Could not parse passive port information.\nServer Reply: " + reply);
        }
        if (isIpAddressFromPasvResponse()) {
            // Pre-3.9.0 behavior
            if (passiveNatWorkaroundStrategy != null) {
                try {
                    final String newPassiveHost = passiveNatWorkaroundStrategy.resolve(pasvHost);
                    if (!pasvHost.equals(newPassiveHost)) {
                        fireReplyReceived(0, "[Replacing PASV mode reply address " + passiveHost + " with " + newPassiveHost + "]\n");
                        pasvHost = newPassiveHost;
                    }
                } catch (final UnknownHostException e) { // Should not happen as we are passing in an IP address
                    throw new MalformedServerReplyException("Could not parse passive host information.\nServer Reply: " + reply);
                }
            }
        } else if (_socket_ == null) {
            pasvHost = null; // For unit testing.
        } else {
            // If a PROXY is configured, use the IP given by the reply as it is.
            Proxy proy = getProxy();
            if (proy == null) {
                pasvHost = _socket_.getInetAddress().getHostAddress();
            }
        }
        passiveHost = pasvHost;
        passivePort = pasvPort;
    }

    /**
     * Retrieves data to an output stream for the given command.
     *
     * @param command the command to get
     * @param remote  the remote file name
     * @param local   The local OutputStream to which to write the file.
     * @return true if successful
     * @throws IOException on error
     * @since 3.1
     */
    protected boolean _retrieveFile(final String command, final String remote, final OutputStream local) throws IOException {
        final Socket socket = _openDataConnection_(command, remote);
        if (socket == null) {
            return false;
        }
        InputStream input = null;
        CSL csl = null;
        try {
            try {
                if (fileType == ASCII_FILE_TYPE) {
                    input = new FromNetASCIIInputStream(getBufferedInputStream(socket.getInputStream()));
                } else {
                    input = getBufferedInputStream(socket.getInputStream());
                }
                if (DurationUtils.isPositive(controlKeepAliveTimeout)) {
                    csl = new CSL(this, controlKeepAliveTimeout, controlKeepAliveReplyTimeout);
                }
                // Treat everything else as binary for now
                Util.copyStream(input, local, getBufferSize(), CopyStreamEvent.UNKNOWN_STREAM_SIZE, mergeListeners(csl), false);
            } finally {
                IOUtils.closeQuietly(input);
            }
            // Get the transfer response
            return completePendingCommand();
        } finally {
            IOUtils.closeQuietly(socket);
            if (csl != null) {
                cslDebug = csl.cleanUp(); // fetch any outstanding keepalive replies
            }
        }
    }

    /**
     * Retrieves data in an input stream for the given command.
     *
     * @param command the command to send
     * @param remote  the remote file name
     * @return the stream from which to read the file
     * @throws IOException on error
     * @since 3.1
     */
    protected InputStream _retrieveFileStream(final String command, final String remote) throws IOException {
        final Socket socket = _openDataConnection_(command, remote);
        if (socket == null) {
            return null;
        }
        final InputStream input;
        if (fileType == ASCII_FILE_TYPE) {
            // We buffer ASCII transfers because the buffering has to
            // be interposed between FromNetASCIIOutputSream and the underlying
            // socket input stream. We don't buffer binary transfers
            // because we don't want to impose a buffering policy on the
            // programmer if possible. Programmers can decide on their
            // own if they want to wrap the SocketInputStream we return
            // for file types other than ASCII.
            input = new FromNetASCIIInputStream(getBufferedInputStream(socket.getInputStream()));
        } else {
            input = socket.getInputStream();
        }
        return new org.apache.commons.net.io.SocketInputStream(socket, input);
    }

    /**
     * Stores the given stream.
     *
     * @param command the command to send
     * @param remote  the remote file name
     * @param local   The local InputStream from which to read the data to be written/appended to the remote file.
     * @return true if successful
     * @throws IOException on error
     * @since 3.1
     */
    protected boolean _storeFile(final String command, final String remote, final InputStream local) throws IOException {
        final Socket socket = _openDataConnection_(command, remote);
        if (socket == null) {
            return false;
        }
        final OutputStream output;
        if (fileType == ASCII_FILE_TYPE) {
            output = new ToNetASCIIOutputStream(getBufferedOutputStream(socket.getOutputStream()));
        } else {
            output = getBufferedOutputStream(socket.getOutputStream());
        }
        CSL csl = null;
        if (DurationUtils.isPositive(controlKeepAliveTimeout)) {
            csl = new CSL(this, controlKeepAliveTimeout, controlKeepAliveReplyTimeout);
        }
        // Treat everything else as binary for now
        try {
            Util.copyStream(local, output, getBufferSize(), CopyStreamEvent.UNKNOWN_STREAM_SIZE, mergeListeners(csl), false);
            output.close(); // ensure the file is fully written
            socket.close(); // done writing the file
            // Get the transfer response
            return completePendingCommand();
        } catch (final IOException e) {
            IOUtils.closeQuietly(output); // ignore close errors here
            IOUtils.closeQuietly(socket); // ignore close errors here
            throw e;
        } finally {
            if (csl != null) {
                cslDebug = csl.cleanUp(); // fetch any outstanding keepalive replies
            }
        }
    }

    /**
     * Gets the the output stream.
     *
     * @param command the command to send
     * @param remote  the remote file name
     * @return the output stream.
     * @throws IOException on error
     * @since 3.1
     */
    protected OutputStream _storeFileStream(final String command, final String remote) throws IOException {
        final Socket socket = _openDataConnection_(command, remote);
        if (socket == null) {
            return null;
        }
        final OutputStream output;
        if (fileType == ASCII_FILE_TYPE) {
            // We buffer ASCII transfers because the buffering has to
            // be interposed between ToNetASCIIOutputSream and the underlying
            // socket output stream. We don't buffer binary transfers
            // because we don't want to impose a buffering policy on the
            // programmer if possible. Programmers can decide on their
            // own if they want to wrap the SocketOutputStream we return
            // for file types other than ASCII.
            output = new ToNetASCIIOutputStream(getBufferedOutputStream(socket.getOutputStream()));
        } else {
            output = socket.getOutputStream();
        }
        return new SocketOutputStream(socket, output);
    }

    /**
     * Aborts a transfer in progress.
     *
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean abort() throws IOException {
        return FTPReply.isPositiveCompletion(abor());
    }

    /**
     * Allocates a number of bytes on the server for the next file transfer.
     *
     * @param bytes The number of bytes which the server should allocate.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean allocate(final int bytes) throws IOException {
        return FTPReply.isPositiveCompletion(allo(bytes));
    }

    /**
     * Allocates space on the server for the next file transfer.
     *
     * @param bytes      The number of bytes which the server should allocate.
     * @param recordSize The size of a file record.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean allocate(final int bytes, final int recordSize) throws IOException {
        return FTPReply.isPositiveCompletion(allo(bytes, recordSize));
    }

    /**
     * Allocates a number of bytes on the server for the next file transfer.
     *
     * @param bytes The number of bytes which the server should allocate.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean allocate(final long bytes) throws IOException {
        return FTPReply.isPositiveCompletion(allo(bytes));
    }

    /**
     * Allocates space on the server for the next file transfer.
     *
     * @param bytes      The number of bytes which the server should allocate.
     * @param recordSize The size of a file record.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean allocate(final long bytes, final int recordSize) throws IOException {
        return FTPReply.isPositiveCompletion(allo(bytes, recordSize));
    }

    /**
     * Appends to a file on the server with the given name, taking input from the given InputStream. This method does NOT close the given InputStream. If the
     * current file type is ASCII, line separators in the file are transparently converted to the NETASCII format (i.e., you should not attempt to create a
     * special InputStream to do this).
     *
     * @param remote The name of the remote file.
     * @param local  The local InputStream from which to read the data to be appended to the remote file.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException                  If the FTP server prematurely closes the connection as a result of the client being idle or some
     *                                                       other reason causing the server to send FTP reply code 421. This exception may be caught either as
     *                                                       an IOException or independently as itself.
     * @throws org.apache.commons.net.io.CopyStreamException If an I/O error occurs while actually transferring the file. The CopyStreamException allows you to
     *                                                       determine the number of bytes transferred and the IOException causing the error. This exception may
     *                                                       be caught either as an IOException or independently as itself.
     * @throws IOException                                   If an I/O error occurs while either sending a command to the server or receiving a reply from the
     *                                                       server.
     */
    public boolean appendFile(final String remote, final InputStream local) throws IOException {
        return storeFile(FTPCmd.APPE, remote, local);
    }

    /**
     * Returns an OutputStream through which data can be written to append to a file on the server with the given name. If the current file type is ASCII, the
     * returned OutputStream will convert line separators in the file to the NETASCII format (i.e., you should not attempt to create a special OutputStream to
     * do this). You must close the OutputStream when you finish writing to it. The OutputStream itself will take care of closing the parent data connection
     * socket upon being closed.
     * <p>
     * <strong>To finalize the file transfer you must call {@link #completePendingCommand completePendingCommand} and check its return value to verify
     * success.</strong> If this is not done, subsequent commands may behave unexpectedly.
     * </p>
     *
     * @param remote The name of the remote file.
     * @return An OutputStream through which the remote file can be appended. If the data connection cannot be opened (e.g., the file does not exist), null is
     *         returned (in which case you may check the reply code to determine the exact reason for failure).
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public OutputStream appendFileStream(final String remote) throws IOException {
        return storeFileStream(FTPCmd.APPE, remote);
    }

    /**
     * Change to the parent directory of the current working directory.
     *
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean changeToParentDirectory() throws IOException {
        return FTPReply.isPositiveCompletion(cdup());
    }

    /**
     * Changes the current working directory of the FTP session.
     *
     * @param path The new current working directory.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean changeWorkingDirectory(final String path) throws IOException {
        return FTPReply.isPositiveCompletion(cwd(path));
    }

    /**
     * There are a few FTPClient methods that do not complete the entire sequence of FTP commands to complete a transaction. These commands require some action
     * by the programmer after the reception of a positive intermediate command. After the programmer's code completes its actions, it must call this method to
     * receive the completion reply from the server and verify the success of the entire transaction.
     * <p>
     * For example,
     * </p>
     *
     * <pre>
     * InputStream input;
     * OutputStream output;
     * input  = new FileInputStream("foobaz.txt");
     * output = ftp.storeFileStream("foobar.txt")
     * if (!FTPReply.isPositiveIntermediate(ftp.getReplyCode())) {
     *     input.close();
     *     output.close();
     *     ftp.logout();
     *     ftp.disconnect();
     *     System.err.println("File transfer failed.");
     *     System.exit(1);
     * }
     * Util.copyStream(input, output);
     * input.close();
     * output.close();
     * // Must call completePendingCommand() to finish command.
     * if (!ftp.completePendingCommand()) {
     *     ftp.logout();
     *     ftp.disconnect();
     *     System.err.println("File transfer failed.");
     *     System.exit(1);
     * }
     * </pre>
     *
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean completePendingCommand() throws IOException {
        return FTPReply.isPositiveCompletion(getReply());
    }

    /**
     * Implements the {@link Configurable} interface. In the case of this class, configuring merely makes the config object available for the factory methods
     * that construct parsers.
     *
     * @param ftpClientConfig {@link FTPClientConfig} object used to provide non-standard configurations to the parser.
     * @since 1.4
     */
    @Override
    public void configure(final FTPClientConfig ftpClientConfig) {
        this.ftpClientConfig = ftpClientConfig;
    }

    // package access for test purposes
    void createParser(final String parserKey) throws IOException {
        // We cache the value to avoid creation of a new object every
        // time a file listing is generated.
        // Note: we don't check against a null parserKey (NET-544)
        if (entryParser == null || parserKey != null && !entryParserKey.equals(parserKey)) {
            if (null != parserKey) {
                // if a parser key was supplied in the parameters,
                // use that to create the parser
                entryParser = parserFactory.createFileEntryParser(parserKey);
                entryParserKey = parserKey;
            } else if (ftpClientConfig != null && !ftpClientConfig.getServerSystemKey().isEmpty()) {
                // if no parserKey was supplied, check for a configuration
                // in the params, and if it has a non-empty system type, use that.
                entryParser = parserFactory.createFileEntryParser(ftpClientConfig);
                entryParserKey = ftpClientConfig.getServerSystemKey();
            } else {
                // if a parserKey hasn't been supplied, and a configuration
                // hasn't been supplied, and the override property is not set
                // then autodetect by calling
                // the SYST command and use that to choose the parser.
                final String systemType = getSystemTypeOverride();
                if (ftpClientConfig != null) { // system type must have been empty above
                    entryParser = parserFactory.createFileEntryParser(new FTPClientConfig(systemType, ftpClientConfig));
                } else {
                    entryParser = parserFactory.createFileEntryParser(systemType);
                }
                entryParserKey = systemType;
            }
        }
    }

    /**
     * Deletes a file on the FTP server.
     *
     * @param path The path of the file to be deleted.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean deleteFile(final String path) throws IOException {
        return FTPReply.isPositiveCompletion(dele(path));
    }

    /**
     * Closes the connection to the FTP server and restores connection parameters to the default values.
     *
     * @throws IOException If an error occurs while disconnecting.
     */
    @Override
    public void disconnect() throws IOException {
        super.disconnect();
        initDefaults();
    }

    /**
     * Issue a command and wait for the reply.
     * <p>
     * Should only be used with commands that return replies on the command channel - do not use for LIST, NLST, MLSD etc.
     * </p>
     *
     * @param command The command to invoke
     * @param params  The parameters string, may be {@code null}
     * @return True if successfully completed, false if not, in which case call {@link #getReplyCode()} or {@link #getReplyString()} to get the reason.
     * @throws IOException If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     * @since 3.0
     */
    public boolean doCommand(final String command, final String params) throws IOException {
        return FTPReply.isPositiveCompletion(sendCommand(command, params));
    }

    /**
     * Issue a command and wait for the reply, returning it as an array of strings.
     * <p>
     * Should only be used with commands that return replies on the command channel - do not use for LIST, NLST, MLSD etc.
     * </p>
     *
     * @param command The command to invoke
     * @param params  The parameters string, may be {@code null}
     * @return The array of replies, or {@code null} if the command failed, in which case call {@link #getReplyCode()} or {@link #getReplyString()} to get the
     *         reason.
     *
     * @throws IOException If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     * @since 3.0
     */
    public String[] doCommandAsStrings(final String command, final String params) throws IOException {
        final boolean success = FTPReply.isPositiveCompletion(sendCommand(command, params));
        if (success) {
            return getReplyStrings();
        }
        return null;
    }

    /**
     * Sets the current data connection mode to {@code ACTIVE_LOCAL_DATA_CONNECTION_MODE}. No communication with the FTP server is conducted, but this causes
     * all future data transfers to require the FTP server to connect to the client's data port. Additionally, to accommodate differences between socket
     * implementations on different platforms, this method causes the client to issue a PORT command before every data transfer.
     */
    public void enterLocalActiveMode() {
        dataConnectionMode = ACTIVE_LOCAL_DATA_CONNECTION_MODE;
        passiveHost = null;
        passivePort = -1;
    }

    /**
     * Sets the current data connection mode to {@code PASSIVE_LOCAL_DATA_CONNECTION_MODE}. Use this method only for data transfers between the client and
     * server. This method causes a PASV (or EPSV) command to be issued to the server before the opening of every data connection, telling the server to open a
     * data port to which the client will connect to conduct data transfers. The FTPClient will stay in {@link #PASSIVE_LOCAL_DATA_CONNECTION_MODE} until the
     * mode is changed by calling some other method such as {@link #enterLocalActiveMode enterLocalActiveMode()}
     * <p>
     * <strong>N.B.</strong> currently calling any connect method will reset the mode to ACTIVE_LOCAL_DATA_CONNECTION_MODE.
     * </p>
     */
    public void enterLocalPassiveMode() {
        dataConnectionMode = PASSIVE_LOCAL_DATA_CONNECTION_MODE;
        // These will be set when just before a data connection is opened
        // in _openDataConnection_()
        passiveHost = null;
        passivePort = -1;
    }

    /**
     * Sets the current data connection mode to {@code ACTIVE_REMOTE_DATA_CONNECTION}. Use this method only for server to server data transfers. This method
     * issues a PORT command to the server, indicating the other server and port to which it should connect for data transfers. You must call this method before
     * EVERY server to server transfer attempt. The FTPClient will NOT automatically continue to issue PORT commands. You also must remember to call
     * {@link #enterLocalActiveMode enterLocalActiveMode()} if you wish to return to the normal data connection mode.
     *
     * @param host The passive mode server accepting connections for data transfers.
     * @param port The passive mode server's data port.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean enterRemoteActiveMode(final InetAddress host, final int port) throws IOException {
        if (FTPReply.isPositiveCompletion(port(host, port))) {
            dataConnectionMode = ACTIVE_REMOTE_DATA_CONNECTION_MODE;
            passiveHost = null;
            passivePort = -1;
            return true;
        }
        return false;
    }

    /**
     * Sets the current data connection mode to {@code PASSIVE_REMOTE_DATA_CONNECTION_MODE}. Use this method only for server to server data transfers. This
     * method issues a PASV command to the server, telling it to open a data port to which the active server will connect to conduct data transfers. You must
     * call this method before EVERY server to server transfer attempt. The FTPClient will NOT automatically continue to issue PASV commands. You also must
     * remember to call {@link #enterLocalActiveMode enterLocalActiveMode()} if you wish to return to the normal data connection mode.
     *
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean enterRemotePassiveMode() throws IOException {
        if (pasv() != FTPReply.ENTERING_PASSIVE_MODE) {
            return false;
        }
        dataConnectionMode = PASSIVE_REMOTE_DATA_CONNECTION_MODE;
        _parsePassiveModeReply(_replyLines.get(0));
        return true;
    }

    /**
     * Queries the server for supported features. The server may reply with a list of server-supported extensions. For example, a typical client-server
     * interaction might be (from RFC 2389):
     *
     * <pre>
        C&gt; feat
        S&gt; 211-Extensions supported:
        S&gt;  MLST size*;create;modify*;perm;media-type
        S&gt;  SIZE
        S&gt;  COMPRESSION
        S&gt;  MDTM
        S&gt; 211 END
     * </pre>
     *
     * @see <a href="http://www.faqs.org/rfcs/rfc2389.html">http://www.faqs.org/rfcs/rfc2389.html</a>
     * @return True if successfully completed, false if not.
     * @throws IOException on error
     * @since 2.2
     */
    public boolean features() throws IOException {
        return FTPReply.isPositiveCompletion(feat());
    }

    /**
     * Queries the server for a supported feature, and returns its value (if any). Caches the parsed response to avoid resending the command repeatedly.
     *
     * @param feature the feature to check
     * @return if the feature is present, returns the feature value or the empty string if the feature exists but has no value. Returns {@code null} if the
     *         feature is not found or the command failed. Check {@link #getReplyCode()} or {@link #getReplyString()} if so.
     * @throws IOException on error
     * @since 3.0
     */
    public String featureValue(final String feature) throws IOException {
        final String[] values = featureValues(feature);
        if (values != null) {
            return values[0];
        }
        return null;
    }

    /**
     * Queries the server for a supported feature, and returns its values (if any). Caches the parsed response to avoid resending the command repeatedly.
     *
     * @param feature the feature to check
     * @return if the feature is present, returns the feature values (empty array if none) Returns {@code null} if the feature is not found or the command
     *         failed. Check {@link #getReplyCode()} or {@link #getReplyString()} if so.
     * @throws IOException on error
     * @since 3.0
     */
    public String[] featureValues(final String feature) throws IOException {
        if (!initFeatureMap()) {
            return null;
        }
        final Set<String> entries = featuresMap.get(feature.toUpperCase(Locale.ENGLISH));
        if (entries != null) {
            return entries.toArray(NetConstants.EMPTY_STRING_ARRAY);
        }
        return null;
    }

    /**
     * Gets the client port for active mode.
     *
     * @return The client port for active mode.
     */
    int getActivePort() {
        if (activeMinPort > 0 && activeMaxPort >= activeMinPort) {
            if (activeMaxPort == activeMinPort) {
                return activeMaxPort;
            }
            // Get a random port between the min and max port range
            return random.nextInt(activeMaxPort - activeMinPort + 1) + activeMinPort;
        }
        // default port
        return 0;
    }

    /**
     * Gets whether automatic server encoding detection is enabled.
     *
     * @return true, if automatic server encoding detection is enabled.
     */
    public boolean getAutodetectUTF8() {
        return autoDetectEncoding;
    }

    private InputStream getBufferedInputStream(final InputStream inputStream) {
        if (bufferSize > 0) {
            return new BufferedInputStream(inputStream, bufferSize);
        }
        return new BufferedInputStream(inputStream);
    }

    private OutputStream getBufferedOutputStream(final OutputStream outputStream) {
        if (bufferSize > 0) {
            return new BufferedOutputStream(outputStream, bufferSize);
        }
        return new BufferedOutputStream(outputStream);
    }

    /**
     * Gets the current internal buffer size for buffered data streams.
     *
     * @return The current buffer size.
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Gets how long to wait for control keep-alive message replies.
     *
     * @deprecated Use {@link #getControlKeepAliveReplyTimeoutDuration()}.
     * @return wait time in milliseconds.
     * @since 3.0
     */
    @Deprecated
    public int getControlKeepAliveReplyTimeout() {
        return DurationUtils.toMillisInt(controlKeepAliveReplyTimeout);
    }

    /**
     * Gets how long to wait for control keep-alive message replies.
     *
     * @return wait time.
     * @since 3.9.0
     */
    public Duration getControlKeepAliveReplyTimeoutDuration() {
        return controlKeepAliveReplyTimeout;
    }

    /**
     * Gets the time to wait between sending control connection keepalive messages when processing file upload or download.
     * <p>
     * See the class Javadoc section "Control channel keep-alive feature"
     * </p>
     *
     * @deprecated Use {@link #getControlKeepAliveTimeoutDuration()}.
     * @return the number of seconds between keepalive messages.
     * @since 3.0
     */
    @Deprecated
    public long getControlKeepAliveTimeout() {
        return controlKeepAliveTimeout.getSeconds();
    }

    /**
     * Gets the time to wait between sending control connection keepalive messages when processing file upload or download.
     * <p>
     * See the class Javadoc section "Control channel keep-alive feature"
     * </p>
     *
     * @return the duration between keepalive messages.
     * @since 3.9.0
     */
    public Duration getControlKeepAliveTimeoutDuration() {
        return controlKeepAliveTimeout;
    }

    /**
     * Gets the currently active listener.
     *
     * @return the listener, may be {@code null}
     * @since 3.0
     */
    public CopyStreamListener getCopyStreamListener() {
        return copyStreamListener;
    }

    /**
     * Gets the CSL debug array.
     * <p>
     * <strong>For debug use only</strong>
     * </p>
     * <p>
     * Currently, it contains:
     * </p>
     * <ul>
     * <li>successfully acked NOOPs at end of transfer</li>
     * <li>unanswered NOOPs at end of transfer</li>
     * <li>unanswered NOOPs after fetching additional replies</li>
     * <li>Number of IOErrors ignored</li>
     * </ul>
     *
     * @deprecated 3.7 For testing only; may be dropped or changed at any time
     * @return the debug array
     */
    @Deprecated // only for use in testing
    public int[] getCslDebug() {
        return cslDebug;
    }

    /**
     * Gets the current data connection mode (one of the {@code _DATA_CONNECTION_MODE} constants).
     *
     * @return The current data connection mode (one of the {@code _DATA_CONNECTION_MODE} constants).
     */
    public int getDataConnectionMode() {
        return dataConnectionMode;
    }

    /**
     * Gets the timeout to use when reading from the data connection. This timeout will be set immediately after opening the data connection, provided that the
     * value is &ge; 0.
     * <p>
     * <strong>Note:</strong> the timeout will also be applied when calling accept() whilst establishing an active local data connection.
     * </p>
     *
     * @return The default timeout used when opening a data connection socket. The value 0 means an infinite timeout.
     * @since 3.9.0
     */
    public Duration getDataTimeout() {
        return dataTimeout;
    }

    // Method for use by unit test code only
    FTPFileEntryParser getEntryParser() {
        return entryParser;
    }

    /**
     * Gets the host address for active mode; allows the local address to be overridden.
     *
     * @return activeExternalHost if non-null, else getLocalAddress()
     * @see #setActiveExternalIPAddress(String)
     */
    InetAddress getHostAddress() {
        if (activeExternalHost != null) {
            return activeExternalHost;
        }
        // default local address
        return getLocalAddress();
    }

    /**
     * Gets the adjusted string with "-a" added if necessary.
     *
     * @param pathName the initial path
     * @return the adjusted string with "-a" added if necessary.
     * @since 2.0
     */
    protected String getListArguments(final String pathName) {
        if (getListHiddenFiles()) {
            if (pathName != null) {
                final StringBuilder sb = new StringBuilder(pathName.length() + 3);
                sb.append("-a ");
                sb.append(pathName);
                return sb.toString();
            }
            return "-a";
        }
        return pathName;
    }

    /**
     * Gets whether to list hidden files.
     *
     * @see #setListHiddenFiles(boolean)
     * @return whether to list hidden files.
     * @since 2.0
     */
    public boolean getListHiddenFiles() {
        return listHiddenFiles;
    }

    /**
     * Gets a file modification time.
     * <p>
     * Issue the FTP MDTM command (not supported by all servers) to retrieve the last modification time of a file. The modification string should be in the ISO
     * 3077 form "yyyyMMDDhhmmss(.xxx)?". The timestamp represented should also be in GMT, but not all FTP servers honor this.
     * </p>
     *
     * @param path The file path to query.
     * @return A string representing the last file modification time in {@code yyyyMMDDhhmmss} format.
     * @throws IOException if an I/O error occurs.
     * @since 2.0
     */
    public String getModificationTime(final String path) throws IOException {
        if (FTPReply.isPositiveCompletion(mdtm(path))) {
            // skip the return code (e.g. 213) and the space
            return getReplyString(0).substring(4);
        }
        return null;
    }

    /**
     * Gets the hostname or IP address (in the form of a string) returned by the server when entering passive mode. If not in passive mode, returns null.
     * This method only returns a valid value AFTER a data connection has been opened after a call to {@link #enterLocalPassiveMode enterLocalPassiveMode()}.
     * This is because FTPClient sends a PASV command to the server only just before opening a data connection, and not when you call
     * {@link #enterLocalPassiveMode enterLocalPassiveMode()}.
     *
     * @return The passive host name if in passive mode, otherwise null.
     */
    public String getPassiveHost() {
        return passiveHost;
    }

    /**
     * Gets the local IP address in passive mode. Useful when there are multiple network cards.
     *
     * @return The local IP address in passive mode.
     */
    public InetAddress getPassiveLocalIPAddress() {
        return passiveLocalHost;
    }

    /**
     * Gets the data port of the passive host if we are in passive mode. This method only returns a valid value AFTER a data connection has been opened after a
     * call to {@link #enterLocalPassiveMode enterLocalPassiveMode()}. This is because FTPClient sends a PASV command to the server only just before opening a
     * data connection, and not when you call {@link #enterLocalPassiveMode enterLocalPassiveMode()}.
     *
     * @return The data port of the passive server. If not in passive mode, undefined.
     */
    public int getPassivePort() {
        return passivePort;
    }

    /**
     * Gets the value to be used for the data socket SO_RCVBUF option.
     *
     * @return The current buffer size.
     * @since 3.3
     */
    public int getReceiveDataSocketBufferSize() {
        return receiveDataSocketBufferSize;
    }

    /**
     * Gets the reported host address for active mode EPRT/PORT commands; allows override of {@link #getHostAddress()}.
     *
     * Useful for FTP Client behind Firewall NAT.
     *
     * @return reportActiveExternalHost if non-null, else getHostAddress();
     */
    InetAddress getReportHostAddress() {
        if (reportActiveExternalHost != null) {
            return reportActiveExternalHost;
        }
        return getHostAddress();
    }

    /**
     * Gets the restart offset.
     *
     * @return offset The offset into the remote file at which to start the next file transfer.
     */
    public long getRestartOffset() {
        return restartOffset;
    }

    /**
     * Gets the value to be used for the data socket SO_SNDBUF option.
     *
     * @return The current buffer size.
     * @since 3.3
     */
    public int getSendDataSocketBufferSize() {
        return sendDataSocketBufferSize;
    }

    /**
     * Gets the size for a path.
     * <p>
     * Issue the FTP SIZE command to the server for a given path. This should produce the size of the file.
     * </p>
     *
     * @param path the file name
     * @return The size information returned by the server; {@code null} if there was an error
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     * @since 3.7
     */
    public String getSize(final String path) throws IOException {
        if (FTPReply.isPositiveCompletion(size(path))) {
            return getReplyString(0).substring(4); // skip the return code (e.g. 213) and the space
        }
        return null;
    }

    /**
     * Gets the status of the server.
     * <p>
     * Issue the FTP STAT command to the server.
     * </p>
     * @return The status information returned by the server.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public String getStatus() throws IOException {
        if (FTPReply.isPositiveCompletion(stat())) {
            return getReplyString();
        }
        return null;
    }

    /**
     * Gets the status of the server for a given path.
     * <p>
     * Issue the FTP STAT command to the server for a given path. This should produce a listing of the file or directory.
     * </p>
     *
     * @param path the file name
     * @return The status information returned by the server.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public String getStatus(final String path) throws IOException {
        if (FTPReply.isPositiveCompletion(stat(path))) {
            return getReplyString();
        }
        return null;
    }

    /**
     * Gets the system name.
     *
     * @return the name
     * @throws IOException on error
     * @deprecated Use {@link #getSystemType()} instead
     */
    @Deprecated
    public String getSystemName() throws IOException {
        if (systemName == null && FTPReply.isPositiveCompletion(syst())) {
            systemName = _replyLines.get(_replyLines.size() - 1).substring(4);
        }
        return systemName;
    }

    /**
     * Gets the system type from the server and returns the string. This value is cached for the duration of the connection after the first call to this
     * method. In other words, only the first time that you invoke this method will it issue a SYST command to the FTP server. FTPClient will remember the value
     * and return the cached value until a call to disconnect.
     * <p>
     * If the SYST command fails, and the system property {@link #FTP_SYSTEM_TYPE_DEFAULT} is defined, then this is used instead.
     * </p>
     *
     * @return The system type obtained from the server. Never null.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server (and the
     *                                      default system type property is not defined)
     * @since 2.2
     */
    public String getSystemType() throws IOException {
        // if (syst() == FTPReply.NAME_SYSTEM_TYPE)
        // Technically, we should expect a NAME_SYSTEM_TYPE response, but
        // in practice FTP servers deviate, so we soften the condition to
        // a positive completion.
        if (systemName == null) {
            if (FTPReply.isPositiveCompletion(syst())) {
                // Assume that response is not empty here (cannot be null)
                systemName = _replyLines.get(_replyLines.size() - 1).substring(4);
            } else {
                // Check if the user has provided a default for when the SYST command fails
                final String systDefault = System.getProperty(FTP_SYSTEM_TYPE_DEFAULT);
                if (systDefault == null) {
                    throw new IOException("Unable to determine system type - response: " + getReplyString());
                }
                systemName = systDefault;
            }
        }
        return systemName;
    }

    /**
     * Gets the system type from the {@link #FTP_SYSTEM_TYPE} system property, or the server (@link #getSystemType()}, or the {@link #SYSTEM_TYPE_PROPERTIES}
     * property file.
     *
     * @return The system type obtained from the server.
     * @throws IOException If an I/O error occurs while either sending a command to the server or receiving a reply from the server (and the default system type
     *                     property is not defined)
     * @since 3.12.0
     */
    public String getSystemTypeOverride() throws IOException {
        String systemType = System.getProperty(FTP_SYSTEM_TYPE);
        if (systemType == null) {
            systemType = getSystemType(); // cannot be null
            final Properties override = getOverrideProperties();
            if (override != null) {
                final String newType = override.getProperty(systemType);
                if (newType != null) {
                    systemType = newType;
                }
            }
        }
        return systemType;
    }

    /**
     * Queries the server for a supported feature. Caches the parsed response to avoid resending the command repeatedly.
     *
     * @param feature the name of the feature; it is converted to upper case.
     * @return {@code true} if the feature is present, {@code false} if the feature is not present or the {@link #feat()} command failed. Check
     *         {@link #getReplyCode()} or {@link #getReplyString()} if it is necessary to distinguish these cases.
     *
     * @throws IOException on error
     * @since 3.8.0
     */
    public boolean hasFeature(final FTPCmd feature) throws IOException {
        return hasFeature(feature.name());
    }

    /**
     * Queries the server for a supported feature. Caches the parsed response to avoid resending the command repeatedly.
     *
     * @param feature the name of the feature; it is converted to upper case.
     * @return {@code true} if the feature is present, {@code false} if the feature is not present or the {@link #feat()} command failed. Check
     *         {@link #getReplyCode()} or {@link #getReplyString()} if it is necessary to distinguish these cases.
     *
     * @throws IOException on error
     * @since 3.0
     */
    public boolean hasFeature(final String feature) throws IOException {
        if (!initFeatureMap()) {
            return false;
        }
        return featuresMap.containsKey(feature.toUpperCase(Locale.ENGLISH));
    }

    /**
     * Queries the server for a supported feature with particular value, for example "AUTH SSL" or "AUTH TLS". Caches the parsed response to avoid resending the
     * command repeatedly.
     *
     * @param feature the name of the feature; it is converted to upper case.
     * @param value   the value to find.
     * @return {@code true} if the feature is present, {@code false} if the feature is not present or the {@link #feat()} command failed. Check
     *         {@link #getReplyCode()} or {@link #getReplyString()} if it is necessary to distinguish these cases.
     *
     * @throws IOException on error
     * @since 3.0
     */
    public boolean hasFeature(final String feature, final String value) throws IOException {
        if (!initFeatureMap()) {
            return false;
        }
        final Set<String> entries = featuresMap.get(feature.toUpperCase(Locale.ENGLISH));
        if (entries != null) {
            return entries.contains(value);
        }
        return false;
    }

    private void initDefaults() {
        dataConnectionMode = ACTIVE_LOCAL_DATA_CONNECTION_MODE;
        passiveHost = null;
        passivePort = -1;
        activeExternalHost = null;
        reportActiveExternalHost = null;
        activeMinPort = 0;
        activeMaxPort = 0;
        fileType = ASCII_FILE_TYPE;
        fileStructure = FILE_STRUCTURE;
        formatOrByteSize = NON_PRINT_TEXT_FORMAT;
        fileTransferMode = STREAM_TRANSFER_MODE;
        restartOffset = 0;
        systemName = null;
        entryParser = null;
        entryParserKey = "";
        featuresMap = null;
    }

    /*
     * Initializes the feature map if not already created.
     */
    private boolean initFeatureMap() throws IOException {
        if (featuresMap == null) {
            // Don't create map here, because next line may throw exception
            final int replyCode = feat();
            if (replyCode == FTPReply.NOT_LOGGED_IN) { // 503
                return false; // NET-518; don't create empty map
            }
            final boolean success = FTPReply.isPositiveCompletion(replyCode);
            // init the map here, so we don't keep trying if we know the command will fail
            featuresMap = new HashMap<>();
            if (!success) {
                return false;
            }
            for (final String line : _replyLines) {
                if (line.startsWith(" ")) { // it's a FEAT entry
                    String key;
                    String value = "";
                    final int varsep = line.indexOf(' ', 1);
                    if (varsep > 0) {
                        key = line.substring(1, varsep);
                        value = line.substring(varsep + 1);
                    } else {
                        key = line.substring(1);
                    }
                    key = key.toUpperCase(Locale.ENGLISH);
                    final Set<String> entries = featuresMap.computeIfAbsent(key, k -> new HashSet<>());
                    entries.add(value);
                }
            }
        }
        return true;
    }

    /**
     * Using the default autodetect mechanism, initialize an FTPListParseEngine object containing a raw file information for the current working directory on
     * the server This information is obtained through the LIST command. This object is then capable of being iterated to return a sequence of FTPFile objects
     * with information filled in by the {@code FTPFileEntryParser} used.
     * <p>
     * This method differs from using the listFiles() methods in that expensive FTPFile objects are not created until needed which may be an advantage on large
     * lists.
     * </p>
     *
     * @return A FTPListParseEngine object that holds the raw information and is capable of providing parsed FTPFile objects, one for each file containing
     *         information contained in the given path in the format determined by the {@code parser} parameter. Null will be returned if a data connection
     *         cannot be opened. If the current working directory contains no files, an empty array will be the return.
     *
     * @throws FTPConnectionClosedException                                    If the FTP server prematurely closes the connection as a result of the client
     *                                                                         being idle or some other reason causing the server to send FTP reply code 421.
     *                                                                         This exception may be caught either as an IOException or independently as itself.
     * @throws IOException                                                     If an I/O error occurs while either sending a command to the server or receiving
     *                                                                         a reply from the server.
     * @throws org.apache.commons.net.ftp.parser.ParserInitializationException Thrown if the autodetect mechanism cannot resolve the type of system we are
     *                                                                         connected with.
     * @see FTPListParseEngine
     */
    public FTPListParseEngine initiateListParsing() throws IOException {
        return initiateListParsing((String) null);
    }

    /**
     * private method through which all listFiles() and initiateListParsing methods pass once a parser is determined.
     *
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     * @see FTPListParseEngine
     */
    private FTPListParseEngine initiateListParsing(final FTPFileEntryParser parser, final String path) throws IOException {
        final Socket socket = _openDataConnection_(FTPCmd.LIST, getListArguments(path));
        final FTPListParseEngine engine = new FTPListParseEngine(parser, ftpClientConfig);
        if (socket == null) {
            return engine;
        }
        try {
            engine.readServerList(socket.getInputStream(), getControlEncoding());
        } finally {
            IOUtils.closeQuietly(socket);
        }
        completePendingCommand();
        return engine;
    }

    /**
     * Using the default autodetect mechanism, initialize an FTPListParseEngine object containing a raw file information for the supplied directory. This
     * information is obtained through the LIST command. This object is then capable of being iterated to return a sequence of FTPFile objects with information
     * filled in by the {@link FTPFileEntryParser} used.
     * <p>
     * The server may or may not expand glob expressions. You should avoid using glob expressions because the return format for glob listings differs from
     * server to server and will likely cause this method to fail.
     * </p>
     * <p>
     * This method differs from using the listFiles() methods in that expensive FTPFile objects are not created until needed which may be an advantage on large
     * lists.
     * </p>
     *
     * <pre>
     * FTPClient f = FTPClient();
     * f.connect(server);
     * f.login(username, password);
     * FTPListParseEngine engine = f.initiateListParsing(directory);
     *
     * while (engine.hasNext()) {
     *     FTPFile[] files = engine.getNext(25); // "page size" you want
     *     // do whatever you want with these files, display them, etc.
     *     // expensive FTPFile objects not created until needed.
     * }
     * </pre>
     *
     * @param path the starting directory
     * @return A FTPListParseEngine object that holds the raw information and is capable of providing parsed FTPFile objects, one for each file containing
     *         information contained in the given path in the format determined by the {@code parser} parameter. Null will be returned if a data connection
     *         cannot be opened. If the current working directory contains no files, an empty array will be the return.
     *
     * @throws FTPConnectionClosedException                                    If the FTP server prematurely closes the connection as a result of the client
     *                                                                         being idle or some other reason causing the server to send FTP reply code 421.
     *                                                                         This exception may be caught either as an IOException or independently as itself.
     * @throws IOException                                                     If an I/O error occurs while either sending a command to the server or receiving
     *                                                                         a reply from the server.
     * @throws org.apache.commons.net.ftp.parser.ParserInitializationException Thrown if the autodetect mechanism cannot resolve the type of system we are
     *                                                                         connected with.
     * @see FTPListParseEngine
     */
    public FTPListParseEngine initiateListParsing(final String path) throws IOException {
        return initiateListParsing((String) null, path);
    }

    /**
     * Using the supplied parser key, initialize an FTPListParseEngine object containing a raw file information for the supplied directory. This information is
     * obtained through the LIST command. This object is then capable of being iterated to return a sequence of FTPFile objects with information filled in by
     * the {@link FTPFileEntryParser} used.
     * <p>
     * The server may or may not expand glob expressions. You should avoid using glob expressions because the return format for glob listings differs from
     * server to server and will likely cause this method to fail.
     * </p>
     * <p>
     * This method differs from using the listFiles() methods in that expensive FTPFile objects are not created until needed which may be an advantage on large
     * lists.
     * </p>
     *
     * @param parserKey A string representing a designated code or fully-qualified class name of an {@link FTPFileEntryParser} that should be used to parse each
     *                  server file listing. May be {@code null}, in which case the code checks first the system property {@link #FTP_SYSTEM_TYPE}, and if that
     *                  is not defined the SYST command is used to provide the value. To allow for arbitrary system types, the return from the SYST command is
     *                  used to look up an alias for the type in the {@link #SYSTEM_TYPE_PROPERTIES} properties file if it is available.
     * @param path  the starting directory
     * @return A FTPListParseEngine object that holds the raw information and is capable of providing parsed FTPFile objects, one for each file containing
     *         information contained in the given path in the format determined by the {@code parser} parameter. Null will be returned if a data connection
     *         cannot be opened. If the current working directory contains no files, an empty array will be the return.
     *
     * @throws FTPConnectionClosedException                                    If the FTP server prematurely closes the connection as a result of the client
     *                                                                         being idle or some other reason causing the server to send FTP reply code 421.
     *                                                                         This exception may be caught either as an IOException or independently as itself.
     * @throws IOException                                                     If an I/O error occurs while either sending a command to the server or receiving
     *                                                                         a reply from the server.
     * @throws org.apache.commons.net.ftp.parser.ParserInitializationException Thrown if the parserKey parameter cannot be resolved by the selected parser
     *                                                                         factory. In the DefaultFTPEntryParserFactory, this will happen when parserKey is
     *                                                                         neither the fully qualified class name of a class implementing the interface
     *                                                                         org.apache.commons.net.ftp.FTPFileEntryParser nor a string containing one of the
     *                                                                         recognized keys mapping to such a parser or if class loader security issues
     *                                                                         prevent its being loaded.
     * @see FTPListParseEngine
     */
    public FTPListParseEngine initiateListParsing(final String parserKey, final String path) throws IOException {
        createParser(parserKey); // create and cache parser
        return initiateListParsing(entryParser, path);
    }

    /**
     * Initiate list parsing for MLSD listings in the current working directory.
     *
     * @return the engine
     * @throws IOException on error
     */
    public FTPListParseEngine initiateMListParsing() throws IOException {
        return initiateMListParsing(null);
    }

    /**
     * Initiate list parsing for MLSD listings.
     *
     * @param path the path from where to MLSD.
     * @return the engine.
     * @throws IOException on error
     */
    public FTPListParseEngine initiateMListParsing(final String path) throws IOException {
        final Socket socket = _openDataConnection_(FTPCmd.MLSD, path);
        final FTPListParseEngine engine = new FTPListParseEngine(MLSxEntryParser.getInstance(), ftpClientConfig);
        if (socket == null) {
            return engine;
        }
        try {
            engine.readServerList(socket.getInputStream(), getControlEncoding());
        } finally {
            IOUtils.closeQuietly(socket);
            completePendingCommand();
        }
        return engine;
    }

    /**
     * Tests whether the IP address from the server's response should be used. Until 3.9.0, this has always been the case. Beginning with 3.9.0, that IP
     * address will be silently ignored, and replaced with the remote IP address of the control connection, unless this configuration option is given, which
     * restores the old behavior. To enable this by default, use the system property {@link FTPClient#FTP_IP_ADDRESS_FROM_PASV_RESPONSE}.
     *
     * @return True, if the IP address from the server's response will be used (pre-3.9 compatible behavior), or false (ignore that IP address).
     * @see FTPClient#FTP_IP_ADDRESS_FROM_PASV_RESPONSE
     * @see #setIpAddressFromPasvResponse(boolean)
     * @since 3.9.0
     */
    public boolean isIpAddressFromPasvResponse() {
        return ipAddressFromPasvResponse;
    }

    /**
     * Tests whether or not verification of the remote host participating in data connections is enabled. The default behavior is for verification to be
     * enabled.
     *
     * @return True if verification is enabled, false if not.
     */
    public boolean isRemoteVerificationEnabled() {
        return remoteVerificationEnabled;
    }

    /**
     * Tests whether to attempt using EPSV with IPv4. Default (if not set) is {@code false}
     *
     * @return true if EPSV shall be attempted with IPv4.
     * @since 2.2
     */
    public boolean isUseEPSVwithIPv4() {
        return useEPSVwithIPv4;
    }

    /**
     * Using the default system autodetect mechanism, obtain a list of directories contained in the current working directory.
     * <p>
     * This information is obtained through the LIST command. The contents of the returned array is determined by the{@link FTPFileEntryParser} used.
     * </p>
     * <p>
     * The LIST command does not generally return very precise timestamps. For recent files, the response usually contains hours and minutes (not seconds).
     * For older files, the output may only contain a date. If the server supports it, the MLSD command returns timestamps with a precision of seconds, and may
     * include milliseconds. See {@link #mlistDir()}
     * </p>
     *
     * @return The list of directories contained in the current directory in the format determined by the autodetection mechanism.
     * @throws FTPConnectionClosedException                                    If the FTP server prematurely closes the connection as a result of the client
     *                                                                         being idle or some other reason causing the server to send FTP reply code 421.
     *                                                                         This exception may be caught either as an IOException or independently as itself.
     * @throws IOException                                                     If an I/O error occurs while either sending a command to the server or receiving
     *                                                                         a reply from the server.
     * @throws org.apache.commons.net.ftp.parser.ParserInitializationException Thrown if the parserKey parameter cannot be resolved by the selected parser
     *                                                                         factory. In the DefaultFTPEntryParserFactory, this will happen when parserKey is
     *                                                                         neither the fully qualified class name of a class implementing the interface
     *                                                                         org.apache.commons.net.ftp.FTPFileEntryParser nor a string containing one of the
     *                                                                         recognized keys mapping to such a parser or if class loader security issues
     *                                                                         prevent its being loaded.
     * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.FTPFileEntryParser
     * @since 3.0
     */
    public FTPFile[] listDirectories() throws IOException {
        return listDirectories((String) null);
    }

    /**
     * Using the default system autodetect mechanism, obtain a list of directories contained in the specified directory.
     * <p>
     * This information is obtained through the LIST command. The contents of the returned array is determined by the{@link FTPFileEntryParser} used.
     * </p>
     * <p>
     * The LIST command does not generally return very precise timestamps. For recent files, the response usually contains hours and minutes (not seconds).
     * For older files, the output may only contain a date. If the server supports it, the MLSD command returns timestamps with a precision of seconds, and may
     * include milliseconds. See {@link #mlistDir()}
     * </p>
     *
     * @param parent the starting directory
     * @return The list of directories contained in the specified directory in the format determined by the autodetection mechanism.
     * @throws FTPConnectionClosedException                                    If the FTP server prematurely closes the connection as a result of the client
     *                                                                         being idle or some other reason causing the server to send FTP reply code 421.
     *                                                                         This exception may be caught either as an IOException or independently as itself.
     * @throws IOException                                                     If an I/O error occurs while either sending a command to the server or receiving
     *                                                                         a reply from the server.
     * @throws org.apache.commons.net.ftp.parser.ParserInitializationException Thrown if the parserKey parameter cannot be resolved by the selected parser
     *                                                                         factory. In the DefaultFTPEntryParserFactory, this will happen when parserKey is
     *                                                                         neither the fully qualified class name of a class implementing the interface
     *                                                                         org.apache.commons.net.ftp.FTPFileEntryParser nor a string containing one of the
     *                                                                         recognized keys mapping to such a parser or if class loader security issues
     *                                                                         prevent its being loaded.
     * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.FTPFileEntryParser
     * @since 3.0
     */
    public FTPFile[] listDirectories(final String parent) throws IOException {
        return listFiles(parent, FTPFileFilters.DIRECTORIES);
    }

    /**
     * Using the default system autodetect mechanism, obtain a list of file information for the current working directory.
     * <p>
     * This information is obtained through the LIST command. The contents of the returned array is determined by the {@link FTPFileEntryParser} used.
     * </p>
     * <p>
     * The LIST command does not generally return very precise timestamps. For recent files, the response usually contains hours and minutes (not seconds).
     * For older files, the output may only contain a date. If the server supports it, the MLSD command returns timestamps with a precision of seconds, and may
     * include milliseconds. See {@link #mlistDir()}
     * </p>
     *
     * @return The list of file information contained in the current directory in the format determined by the autodetection mechanism. <strong>NOTE:</strong>
     *         This array may contain null members if any of the individual file listings failed to parse. The caller should check each entry for null before
     *         referencing it.
     * @throws FTPConnectionClosedException                                    If the FTP server prematurely closes the connection as a result of the client
     *                                                                         being idle or some other reason causing the server to send FTP reply code 421.
     *                                                                         This exception may be caught either as an IOException or independently as itself.
     * @throws IOException                                                     If an I/O error occurs while either sending a command to the server or receiving
     *                                                                         a reply from the server.
     * @throws org.apache.commons.net.ftp.parser.ParserInitializationException Thrown if the parserKey parameter cannot be resolved by the selected parser
     *                                                                         factory. In the DefaultFTPEntryParserFactory, this will happen when parserKey is
     *                                                                         neither the fully qualified class name of a class implementing the interface
     *                                                                         org.apache.commons.net.ftp.FTPFileEntryParser nor a string containing one of the
     *                                                                         recognized keys mapping to such a parser or if class loader security issues
     *                                                                         prevent its being loaded.
     * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.FTPFileEntryParser
     */
    public FTPFile[] listFiles() throws IOException {
        return listFiles((String) null);
    }

    /**
     * Using the default system autodetect mechanism, obtain a list of file information for the current working directory or for just a single file.
     * <p>
     * This information is obtained through the LIST command. The contents of the returned array is determined by the {@link FTPFileEntryParser} used.
     * </p>
     * <p>
     * The LIST command does not generally return very precise timestamps. For recent files, the response usually contains hours and minutes (not seconds).
     * For older files, the output may only contain a date. If the server supports it, the MLSD command returns timestamps with a precision of seconds, and may
     * include milliseconds. See {@link #mlistDir()}
     * </p>
     *
     * @param path The file or directory to list. Since the server may or may not expand glob expressions, using them here is not recommended and may well
     *                 cause this method to fail. Also, some servers treat a leading '-' as being an option. To avoid this interpretation, use an absolute
     *                 path or prefix the path with ./ (Unix style servers). Some servers may support "--" as meaning end of options, in which case "--
     *                 -xyz" should work.
     * @return The list of file information contained in the given path in the format determined by the autodetection mechanism
     * @throws FTPConnectionClosedException                                    If the FTP server prematurely closes the connection as a result of the client
     *                                                                         being idle or some other reason causing the server to send FTP reply code 421.
     *                                                                         This exception may be caught either as an IOException or independently as itself.
     * @throws IOException                                                     If an I/O error occurs while either sending a command to the server or receiving
     *                                                                         a reply from the server.
     * @throws org.apache.commons.net.ftp.parser.ParserInitializationException Thrown if the parserKey parameter cannot be resolved by the selected parser
     *                                                                         factory. In the DefaultFTPEntryParserFactory, this will happen when parserKey is
     *                                                                         neither the fully qualified class name of a class implementing the interface
     *                                                                         org.apache.commons.net.ftp.FTPFileEntryParser nor a string containing one of the
     *                                                                         recognized keys mapping to such a parser or if class loader security issues
     *                                                                         prevent its being loaded.
     * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.FTPFileEntryParser
     */
    public FTPFile[] listFiles(final String path) throws IOException {
        return initiateListParsing((String) null, path).getFiles();
    }

    /**
     * Version of {@link #listFiles(String)} which allows a filter to be provided. For example: {@code listFiles("site", FTPFileFilters.DIRECTORY);}
     *
     * @param path the initial path, may be null
     * @param filter   the filter, non-null
     * @return the array of FTPFile entries.
     * @throws IOException on error
     * @since 2.2
     */
    public FTPFile[] listFiles(final String path, final FTPFileFilter filter) throws IOException {
        return initiateListParsing((String) null, path).getFiles(filter);
    }

    /**
     * Fetches the system help information from the server and returns the full string.
     *
     * @return The system help string obtained from the server. null if the information could not be obtained.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public String listHelp() throws IOException {
        return FTPReply.isPositiveCompletion(help()) ? getReplyString() : null;
    }

    /**
     * Fetches the help information for a given command from the server and returns the full string.
     *
     * @param command The command on which to ask for help.
     * @return The command help string obtained from the server. null if the information could not be obtained.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public String listHelp(final String command) throws IOException {
        return FTPReply.isPositiveCompletion(help(command)) ? getReplyString() : null;
    }

    /**
     * Obtain a list of file names in the current working directory This information is obtained through the NLST command. If the current directory contains no
     * files, a zero length array is returned only if the FTP server returned a positive completion code, otherwise, null is returned (the FTP server returned a
     * 550 error No files found.). If the directory is not empty, an array of file names in the directory is returned.
     *
     * @return The list of file names contained in the current working directory. null if the list could not be obtained. If there are no file names in the
     *         directory, a zero-length array is returned.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public String[] listNames() throws IOException {
        return listNames(null);
    }

    /**
     * Obtain a list of file names in a directory (or just the name of a given file, which is not particularly useful). This information is obtained through the
     * NLST command. If the given path is a directory and contains no files, a zero length array is returned only if the FTP server returned a positive
     * completion code, otherwise null is returned (the FTP server returned a 550 error No files found.). If the directory is not empty, an array of file names
     * in the directory is returned. If the path corresponds to a file, only that file will be listed. The server may or may not expand glob expressions.
     *
     * @param path The file or directory to list. Warning: the server may treat a leading '-' as an option introducer. If so, try using an absolute path, or
     *                 prefix the path with ./ (Unix style servers). Some servers may support "--" as meaning end of options, in which case "-- -xyz" should
     *                 work.
     * @return The list of file names contained in the given path. null if the list could not be obtained. If there are no file names in the directory, a
     *         zero-length array is returned.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public String[] listNames(final String path) throws IOException {
        final List<String> results;
        try (Socket socket = _openDataConnection_(FTPCmd.NLST, getListArguments(path))) {
            if (socket == null) {
                return null;
            }
            try (InputStream in = socket.getInputStream()) {
                results = IOUtils.readLines(in, getControlEncoding());
            }
        }
        if (completePendingCommand()) {
            return results.toArray(NetConstants.EMPTY_STRING_ARRAY);
        }
        return null;
    }

    /**
     * Login to the FTP server using the provided user and password.
     *
     * @param user     The user name to login under.
     * @param password The password to use.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean login(final String user, final String password) throws IOException {
        user(user);
        if (FTPReply.isPositiveCompletion(_replyCode)) {
            return true;
        }
        // If we get here, we either have an error code, or an intermediate
        // reply requesting password.
        if (!FTPReply.isPositiveIntermediate(_replyCode)) {
            return false;
        }
        return FTPReply.isPositiveCompletion(pass(password));
    }

    /**
     * Login to the FTP server using the provided username, password, and account. If no account is required by the server, only the username and password, the
     * account information is not used.
     *
     * @param user     The user name to login under.
     * @param password The password to use.
     * @param account  The account to use.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean login(final String user, final String password, final String account) throws IOException {
        user(user);
        if (FTPReply.isPositiveCompletion(_replyCode)) {
            return true;
        }
        // If we get here, we either have an error code, or an intermediate
        // reply requesting password.
        if (!FTPReply.isPositiveIntermediate(_replyCode)) {
            return false;
        }
        pass(password);
        if (FTPReply.isPositiveCompletion(_replyCode)) {
            return true;
        }
        if (!FTPReply.isPositiveIntermediate(_replyCode)) {
            return false;
        }
        return FTPReply.isPositiveCompletion(acct(account));
    }

    /**
     * Logout of the FTP server by sending the QUIT command.
     *
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean logout() throws IOException {
        return FTPReply.isPositiveCompletion(quit());
    }

    /**
     * Creates a new subdirectory on the FTP server in the current directory (if a relative path is given) or where specified (if an absolute path is
     * given).
     *
     * @param path The path of the directory to create.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean makeDirectory(final String path) throws IOException {
        return FTPReply.isPositiveCompletion(mkd(path));
    }

    /**
     * Issue the FTP MDTM command (not supported by all servers) to retrieve the last modification time of a file. The modification string should be in the ISO
     * 3077 form "yyyyMMDDhhmmss(.xxx)?". The timestamp represented should also be in GMT, but not all FTP servers honor this.
     *
     * @param path The file path to query.
     * @return A Calendar representing the last file modification time, may be {@code null}. The Calendar timestamp will be null if a parse error occurs.
     * @throws IOException if an I/O error occurs.
     * @since 3.8.0
     */
    public Calendar mdtmCalendar(final String path) throws IOException {
        final String modificationTime = getModificationTime(path);
        if (modificationTime != null) {
            return MLSxEntryParser.parseGMTdateTime(modificationTime);
        }
        return null;
    }

    /**
     * Issue the FTP MDTM command (not supported by all servers) to retrieve the last modification time of a file. The modification string should be in the ISO
     * 3077 form "yyyyMMDDhhmmss(.xxx)?". The timestamp represented should also be in GMT, but not all FTP servers honor this.
     *
     * @param path The file path to query.
     * @return A FTPFile representing the last file modification time, may be {@code null}. The FTPFile timestamp will be null if a parse error occurs.
     * @throws IOException if an I/O error occurs.
     * @since 3.4
     */
    public FTPFile mdtmFile(final String path) throws IOException {
        final String modificationTime = getModificationTime(path);
        if (modificationTime != null) {
            final FTPFile file = new FTPFile();
            file.setName(path);
            file.setRawListing(modificationTime);
            file.setTimestamp(MLSxEntryParser.parseGMTdateTime(modificationTime));
            return file;
        }
        return null;
    }

    /**
     * Issue the FTP MDTM command (not supported by all servers) to retrieve the last modification time of a file. The modification string should be in the ISO
     * 3077 form "yyyyMMDDhhmmss(.xxx)?". The timestamp represented should also be in GMT, but not all FTP servers honor this.
     *
     * @param path The file path to query.
     * @return An Instant representing the last file modification time, may be {@code null}. The Instant timestamp will be null if a parse error occurs.
     * @throws IOException if an I/O error occurs.
     * @since 3.9.0
     */
    public Instant mdtmInstant(final String path) throws IOException {
        final String modificationTime = getModificationTime(path);
        if (modificationTime != null) {
            return MLSxEntryParser.parseGmtInstant(modificationTime);
        }
        return null;
    }

    /**
     * Merge two copystream listeners, either or both of which may be null.
     *
     * @param local the listener used by this class, may be null
     * @return a merged listener or a single listener or null
     * @since 3.0
     */
    private CopyStreamListener mergeListeners(final CopyStreamListener local) {
        if (local == null) {
            return copyStreamListener;
        }
        if (copyStreamListener == null) {
            return local;
        }
        // Both are non-null
        final CopyStreamAdapter merged = new CopyStreamAdapter();
        merged.addCopyStreamListener(local);
        merged.addCopyStreamListener(copyStreamListener);
        return merged;
    }

    /**
     * Generate a directory listing for the current directory using the MLSD command.
     *
     * @return the array of file entries
     * @throws IOException on error
     * @since 3.0
     */
    public FTPFile[] mlistDir() throws IOException {
        return mlistDir(null);
    }

    /**
     * Generate a directory listing using the MLSD command.
     *
     * @param path the directory name, may be {@code null}
     * @return the array of file entries
     * @throws IOException on error
     * @since 3.0
     */
    public FTPFile[] mlistDir(final String path) throws IOException {
        return initiateMListParsing(path).getFiles();
    }

    /**
     * Generate a directory listing using the MLSD command.
     *
     * @param path the directory name, may be {@code null}
     * @param filter   the filter to apply to the responses
     * @return the array of file entries
     * @throws IOException on error
     * @since 3.0
     */
    public FTPFile[] mlistDir(final String path, final FTPFileFilter filter) throws IOException {
        return initiateMListParsing(path).getFiles(filter);
    }

    /**
     * Gets file details using the MLST command
     *
     * @param path the file or directory to list, may be {@code null}
     * @return the file details, may be {@code null}
     * @throws IOException on error
     * @since 3.0
     */
    public FTPFile mlistFile(final String path) throws IOException {
        final boolean success = FTPReply.isPositiveCompletion(sendCommand(FTPCmd.MLST, path));
        if (success) {
            String reply = getReplyString(1);
            // some FTP server reply not contains space before fact(s)
            if (reply.charAt(0) != ' ') {
                reply = " " + reply;
            }
            /*
             * check the response makes sense. Must have space before fact(s) and between fact(s) and file name Fact(s) can be absent, so at least 3 chars are
             * needed.
             */
            if (reply.length() < 3) {
                throw new MalformedServerReplyException("Invalid server reply (MLST): '" + reply + "'");
            }
            // some FTP server reply contains more than one space before fact(s)
            final String entry = reply.replaceAll("^\\s+", ""); // skip leading space for parser
            return MLSxEntryParser.parseEntry(entry);
        }
        return null;
    }

    /**
     * Returns the path of the current working directory.
     *
     * @return The path of the current working directory. If it cannot be obtained, returns null.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public String printWorkingDirectory() throws IOException {
        if (pwd() != FTPReply.PATHNAME_CREATED) {
            return null;
        }
        return parsePathname(_replyLines.get(_replyLines.size() - 1));
    }

    /**
     * Reinitialize the FTP session. Not all FTP servers support this command, which issues the FTP REIN command.
     *
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     * @since 3.4 (made public)
     */
    public boolean reinitialize() throws IOException {
        rein();
        if (FTPReply.isPositiveCompletion(_replyCode) || FTPReply.isPositivePreliminary(_replyCode) && FTPReply.isPositiveCompletion(getReply())) {
            initDefaults();
            return true;
        }
        return false;
    }

    // For server to server transfers
    /**
     * Initiate a server to server file transfer. This method tells the server to which the client is connected to append to a given file on the other server.
     * The other server must have had a {@code remoteRetrieve} issued to it by another FTPClient.
     *
     * @param fileName The name of the file to be appended to, or if the file does not exist, the name to call the file being stored.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean remoteAppend(final String fileName) throws IOException {
        if (dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE || dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE) {
            return FTPReply.isPositivePreliminary(appe(fileName));
        }
        return false;
    }

    /**
     * Initiate a server to server file transfer. This method tells the server to which the client is connected to retrieve a given file from the other server.
     *
     * @param fileName The name of the file to retrieve.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean remoteRetrieve(final String fileName) throws IOException {
        if (dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE || dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE) {
            return FTPReply.isPositivePreliminary(retr(fileName));
        }
        return false;
    }

    /**
     * Initiate a server to server file transfer. This method tells the server to which the client is connected to store a file on the other server using the
     * given file name. The other server must have had a {@code remoteRetrieve} issued to it by another FTPClient.
     *
     * @param fileName The name to call the file that is to be stored.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean remoteStore(final String fileName) throws IOException {
        if (dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE || dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE) {
            return FTPReply.isPositivePreliminary(stor(fileName));
        }
        return false;
    }

    /**
     * Initiate a server to server file transfer. This method tells the server to which the client is connected to store a file on the other server using a
     * unique file name. The other server must have had a {@code remoteRetrieve} issued to it by another FTPClient. Many FTP servers require that a base file
     * name be given from which the unique file name can be derived. For those servers use the other version of {@code remoteStoreUnique}
     *
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean remoteStoreUnique() throws IOException {
        if (dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE || dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE) {
            return FTPReply.isPositivePreliminary(stou());
        }
        return false;
    }

    /**
     * Initiate a server to server file transfer. This method tells the server to which the client is connected to store a file on the other server using a
     * unique file name based on the given file name. The other server must have had a {@code remoteRetrieve} issued to it by another FTPClient.
     *
     * @param fileName The name on which to base the file name of the file that is to be stored.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean remoteStoreUnique(final String fileName) throws IOException {
        if (dataConnectionMode == ACTIVE_REMOTE_DATA_CONNECTION_MODE || dataConnectionMode == PASSIVE_REMOTE_DATA_CONNECTION_MODE) {
            return FTPReply.isPositivePreliminary(stou(fileName));
        }
        return false;
    }

    /**
     * Removes a directory on the FTP server (if empty).
     *
     * @param path The path of the directory to remove.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean removeDirectory(final String path) throws IOException {
        return FTPReply.isPositiveCompletion(rmd(path));
    }

    /**
     * Renames a remote file.
     *
     * @param from The name of the remote file to rename.
     * @param to   The new name of the remote file.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean rename(final String from, final String to) throws IOException {
        if (!FTPReply.isPositiveIntermediate(rnfr(from))) {
            return false;
        }
        return FTPReply.isPositiveCompletion(rnto(to));
    }

    /**
     * Restart a {@code STREAM_TRANSFER_MODE} file transfer starting from the given offset. This will only work on FTP servers supporting the REST comand for
     * the stream transfer mode. However, most FTP servers support this. Any subsequent file transfer will start reading or writing the remote file from the
     * indicated offset.
     *
     * @param offset The offset into the remote file at which to start the next file transfer.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     * @since 3.1 (changed from private to protected)
     */
    protected boolean restart(final long offset) throws IOException {
        restartOffset = 0;
        return FTPReply.isPositiveIntermediate(rest(Long.toString(offset)));
    }

    /**
     * Retrieves a named file from the server and writes it to the given OutputStream. This method does NOT close the given OutputStream. If the current file
     * type is ASCII, line separators in the file are converted to the local representation.
     * <p>
     * Note: if you have used {@link #setRestartOffset(long)}, the file data will start from the selected offset.
     *
     * @param remote The name of the remote file.
     * @param local  The local OutputStream to which to write the file.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException                  If the FTP server prematurely closes the connection as a result of the client being idle or some
     *                                                       other reason causing the server to send FTP reply code 421. This exception may be caught either as
     *                                                       an IOException or independently as itself.
     * @throws org.apache.commons.net.io.CopyStreamException If an I/O error occurs while actually transferring the file. The CopyStreamException allows you to
     *                                                       determine the number of bytes transferred and the IOException causing the error. This exception may
     *                                                       be caught either as an IOException or independently as itself.
     * @throws IOException                                   If an I/O error occurs while either sending a command to the server or receiving a reply from the
     *                                                       server.
     */
    public boolean retrieveFile(final String remote, final OutputStream local) throws IOException {
        return _retrieveFile(FTPCmd.RETR.getCommand(), remote, local);
    }

    /**
     * Returns an InputStream from which a named file from the server can be read. If the current file type is ASCII, the returned InputStream will convert line
     * separators in the file to the local representation. You must close the InputStream when you finish reading from it. The InputStream itself will take care
     * of closing the parent data connection socket upon being closed.
     * <p>
     * <strong>To finalize the file transfer you must call {@link #completePendingCommand completePendingCommand} and check its return value to verify
     * success.</strong> If this is not done, subsequent commands may behave unexpectedly.
     * <p>
     * Note: if you have used {@link #setRestartOffset(long)}, the file data will start from the selected offset.
     *
     * @param remote The name of the remote file.
     * @return An InputStream from which the remote file can be read. If the data connection cannot be opened (e.g., the file does not exist), null is returned
     *         (in which case you may check the reply code to determine the exact reason for failure).
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public InputStream retrieveFileStream(final String remote) throws IOException {
        return _retrieveFileStream(FTPCmd.RETR.getCommand(), remote);
    }

    /**
     * Sends a NOOP command to the FTP server. This is useful for preventing server timeouts.
     *
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean sendNoOp() throws IOException {
        return FTPReply.isPositiveCompletion(noop());
    }

    /**
     * Send a site specific command.
     *
     * @param arguments The site specific command and arguments.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean sendSiteCommand(final String arguments) throws IOException {
        return FTPReply.isPositiveCompletion(site(arguments));
    }

    /**
     * Sets the external IP address in active mode. Useful when there are multiple network cards.
     *
     * @param ipAddress The external IP address of this machine.
     * @throws UnknownHostException if the ipAddress cannot be resolved
     * @since 2.2
     */
    public void setActiveExternalIPAddress(final String ipAddress) throws UnknownHostException {
        activeExternalHost = InetAddress.getByName(ipAddress);
    }

    /**
     * Sets the client side port range in active mode.
     *
     * @param activeMinPort The lowest available port (inclusive).
     * @param activeMaxPort The highest available port (inclusive).
     * @since 2.2
     */
    public void setActivePortRange(final int activeMinPort, final int activeMaxPort) {
        this.activeMinPort = activeMinPort;
        this.activeMaxPort = activeMaxPort;
    }

    /**
     * Sets automatic server encoding detection (only UTF-8 supported).
     * <p>
     * Does not affect existing connections; must be invoked before a connection is established.
     * </p>
     *
     * @param autoDetectEncoding If true, automatic server encoding detection will be enabled.
     */
    public void setAutodetectUTF8(final boolean autoDetectEncoding) {
        this.autoDetectEncoding = autoDetectEncoding;
    }

    /**
     * Sets the internal buffer size for buffered data streams.
     *
     * @param bufferSize The size of the buffer. Use a non-positive value to use the default.
     */
    public void setBufferSize(final int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * Sets the duration to wait for control keep-alive message replies.
     *
     * @param timeout duration to wait (defaults to 1,000). Zero (or less) disables.
     * @since 3.0
     * @see #setControlKeepAliveTimeout(Duration)
     */
    public void setControlKeepAliveReplyTimeout(final Duration timeout) {
        controlKeepAliveReplyTimeout = DurationUtils.zeroIfNull(timeout);
    }

    /**
     * Sets the duration to wait for control keep-alive message replies.
     *
     * @deprecated Use {@link #setControlKeepAliveReplyTimeout(Duration)}.
     * @param timeoutMillis number of milliseconds to wait (defaults to 1,000).
     * @since 3.0
     * @see #setControlKeepAliveTimeout(long)
     */
    @Deprecated
    public void setControlKeepAliveReplyTimeout(final int timeoutMillis) {
        controlKeepAliveReplyTimeout = Duration.ofMillis(timeoutMillis);
    }

    /**
     * Sets the duration to wait between sending control connection keepalive messages when processing file upload or download.
     * <p>
     * See the class Javadoc section "Control channel keep-alive feature"
     * </p>
     *
     * @param controlIdle the duration to wait between keepalive messages. Zero (or less) disables.
     * @since 3.9.0
     * @see #setControlKeepAliveReplyTimeout(Duration)
     */
    public void setControlKeepAliveTimeout(final Duration controlIdle) {
        controlKeepAliveTimeout = DurationUtils.zeroIfNull(controlIdle);
    }

    /**
     * Sets the duration to wait between sending control connection keepalive messages when processing file upload or download.
     * <p>
     * See the class Javadoc section "Control channel keep-alive feature"
     * </p>
     *
     * @deprecated Use {@link #setControlKeepAliveTimeout(Duration)}.
     * @param controlIdleSeconds the wait in seconds between keepalive messages. Zero (or less) disables.
     * @since 3.0
     * @see #setControlKeepAliveReplyTimeout(int)
     */
    @Deprecated
    public void setControlKeepAliveTimeout(final long controlIdleSeconds) {
        controlKeepAliveTimeout = Duration.ofSeconds(controlIdleSeconds);
    }

    /**
     * Sets the listener to be used when performing store/retrieve operations. The default value (if not set) is {@code null}.
     *
     * @param copyStreamListener to be used, may be {@code null} to disable
     * @since 3.0
     */
    public void setCopyStreamListener(final CopyStreamListener copyStreamListener) {
        this.copyStreamListener = copyStreamListener;
    }

    /**
     * Sets the timeout to use when reading from the data connection. This timeout will be set immediately after opening the data connection, provided that the
     * value is &ge; 0.
     * <p>
     * <strong>Note:</strong> the timeout will also be applied when calling accept() whilst establishing an active local data connection.
     * </p>
     *
     * @param timeout The default timeout that is used when opening a data connection socket. The value 0 (or null) means an infinite timeout.
     * @since 3.9.0
     */
    public void setDataTimeout(final Duration timeout) {
        dataTimeout = DurationUtils.zeroIfNull(timeout);
    }

    /**
     * Sets the timeout in milliseconds to use when reading from the data connection. This timeout will be set immediately after opening the data connection,
     * provided that the value is &ge; 0.
     * <p>
     * <strong>Note:</strong> the timeout will also be applied when calling accept() whilst establishing an active local data connection.
     * </p>
     *
     * @deprecated Use {@link #setDataTimeout(Duration)}.
     * @param timeoutMillis The default timeout in milliseconds that is used when opening a data connection socket. The value 0 means an infinite timeout.
     */
    @Deprecated
    public void setDataTimeout(final int timeoutMillis) {
        dataTimeout = Duration.ofMillis(timeoutMillis);
    }

    /**
     * Sets the file structure. The default structure is {@link FTP#FILE_STRUCTURE} if this method is never called or if a connect method is called.
     *
     * @param fileStructure The structure of the file (one of the FTP class {@code _STRUCTURE} constants).
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean setFileStructure(final int fileStructure) throws IOException {
        if (FTPReply.isPositiveCompletion(stru(fileStructure))) {
            this.fileStructure = fileStructure;
            return true;
        }
        return false;
    }

    /**
     * Sets the transfer mode. The default transfer mode {@link FTP#STREAM_TRANSFER_MODE} if this method is never called or if a connect method is called.
     *
     * @param fileTransferMode The new transfer mode to use (one of the FTP class {@code _TRANSFER_MODE} constants).
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean setFileTransferMode(final int fileTransferMode) throws IOException {
        if (FTPReply.isPositiveCompletion(mode(fileTransferMode))) {
            this.fileTransferMode = fileTransferMode;
            return true;
        }
        return false;
    }

    /**
     * Sets the file type to be transferred. This should be one of {@link FTP#ASCII_FILE_TYPE}, {@link FTP#BINARY_FILE_TYPE}, etc. The file type only needs to
     * be set when you want to change the type. After changing it, the new type stays in effect until you change it again. The default file type is
     * {@link FTP#ASCII_FILE_TYPE} if this method is never called.
     * <p>
     * The server default is supposed to be ASCII (see RFC 959), however many ftp servers default to BINARY. <b>To ensure correct operation with all servers,
     * always specify the appropriate file type after connecting to the server.</b>
     * </p>
     * <p>
     * <strong>N.B.</strong> currently calling any connect method will reset the type to {@link FTP#ASCII_FILE_TYPE}.
     * </p>
     *
     * @param fileType The {@code _FILE_TYPE} constant indicating the type of file.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean setFileType(final int fileType) throws IOException {
        if (FTPReply.isPositiveCompletion(type(fileType))) {
            this.fileType = fileType;
            this.formatOrByteSize = NON_PRINT_TEXT_FORMAT;
            return true;
        }
        return false;
    }

    /**
     * Sets the file type to be transferred and the format. The type should be one of {@link FTP#ASCII_FILE_TYPE}, {@link FTP#BINARY_FILE_TYPE}, etc. The file
     * type only needs to be set when you want to change the type. After changing it, the new type stays in effect until you change it again. The default file
     * type is {@link FTP#ASCII_FILE_TYPE} if this method is never called.
     * <p>
     * The server default is supposed to be ASCII (see RFC 959), however many ftp servers default to BINARY. <b>To ensure correct operation with all servers,
     * always specify the appropriate file type after connecting to the server.</b>
     * </p>
     * <p>
     * The format should be one of the FTP class {@code TEXT_FORMAT} constants, or if the type is {@link FTP#LOCAL_FILE_TYPE}, the format should be the byte
     * size for that type. The default format is {@link FTP#NON_PRINT_TEXT_FORMAT} if this method is never called.
     * </p>
     * <p>
     * <strong>N.B.</strong> currently calling any connect method will reset the type to {@link FTP#ASCII_FILE_TYPE} and the formatOrByteSize to
     * {@link FTP#NON_PRINT_TEXT_FORMAT}.
     * </p>
     *
     * @param fileType         The {@code _FILE_TYPE} constant indicating the type of file.
     * @param formatOrByteSize The format of the file (one of the {@code _FORMAT} constants). In the case of {@code LOCAL_FILE_TYPE}, the byte size.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean setFileType(final int fileType, final int formatOrByteSize) throws IOException {
        if (FTPReply.isPositiveCompletion(type(fileType, formatOrByteSize))) {
            this.fileType = fileType;
            this.formatOrByteSize = formatOrByteSize;
            return true;
        }
        return false;
    }

    /**
     * Sets whether the IP address from the server's response should be used. Until 3.9.0, this has always been the case. Beginning with 3.9.0, that IP address
     * will be silently ignored, and replaced with the remote IP address of the control connection, unless this configuration option is given, which restores
     * the old behavior. To enable this by default, use the system property {@link FTPClient#FTP_IP_ADDRESS_FROM_PASV_RESPONSE}.
     *
     * @param ipAddressFromPasvResponse True, if the IP address from the server's response should be used (pre-3.9.0 compatible behavior), or false (ignore that
     *                                  IP address).
     * @see FTPClient#FTP_IP_ADDRESS_FROM_PASV_RESPONSE
     * @see #isIpAddressFromPasvResponse
     * @since 3.9.0
     */
    public void setIpAddressFromPasvResponse(final boolean ipAddressFromPasvResponse) {
        this.ipAddressFromPasvResponse = ipAddressFromPasvResponse;
    }

    /**
     * Sets whether to get hidden files when {@link #listFiles} too. A {@code LIST -a} will be issued to the ftp server. It
     * depends on your ftp server if you need to call this method, also don't expect to get rid of hidden files if you call this method with "false".
     *
     * @param listHiddenFiles true if hidden files should be listed
     * @since 2.0
     */
    public void setListHiddenFiles(final boolean listHiddenFiles) {
        this.listHiddenFiles = listHiddenFiles;
    }

    /**
     * Sets the last modified time of a file.
     * <p>
     * Issue the FTP MFMT command (not supported by all servers) which
     * The timestamp should be in the form {@code yyyyMMDDhhmmss}. It should also be in GMT, but not all servers honor this.
     * </p>
     * <p>
     * An FTP server would indicate its support of this feature by including "MFMT" in its response to the FEAT command, which may be retrieved by
     * FTPClient.features()
     * </p>
     *
     * @param path    The file path for which last modified time is to be changed.
     * @param timeval The timestamp to set to, in {@code yyyyMMDDhhmmss} format.
     * @return true if successfully set, false if not
     * @throws IOException if an I/O error occurs.
     * @since 2.2
     * @see <a href="https://tools.ietf.org/html/draft-somers-ftp-mfxx-04">https://tools.ietf.org/html/draft-somers-ftp-mfxx-04</a>
     */
    public boolean setModificationTime(final String path, final String timeval) throws IOException {
        return FTPReply.isPositiveCompletion(mfmt(path, timeval));
    }

    /**
     * Sets the factory used for parser creation to the supplied factory object.
     *
     * @param parserFactory factory object used to create FTPFileEntryParsers
     * @see org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory
     * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
     */
    public void setParserFactory(final FTPFileEntryParserFactory parserFactory) {
        this.parserFactory = parserFactory;
    }

    /**
     * Sets the local IP address to use in passive mode. Useful when there are multiple network cards.
     *
     * @param passiveLocalHost The local IP address of this machine.
     */
    public void setPassiveLocalIPAddress(final InetAddress passiveLocalHost) {
        this.passiveLocalHost = passiveLocalHost;
    }

    /**
     * Sets the local IP address to use in passive mode. Useful when there are multiple network cards.
     *
     * @param ipAddress The local IP address of this machine.
     * @throws UnknownHostException if the ipAddress cannot be resolved
     */
    public void setPassiveLocalIPAddress(final String ipAddress) throws UnknownHostException {
        this.passiveLocalHost = InetAddress.getByName(ipAddress);
    }

    /**
     * Sets the passive mode NAT workaround. If enabled, a site-local PASV mode reply address will be replaced with the remote host address to which
     * the PASV mode request was sent (unless that is also a site local address). This gets around the problem that some NAT boxes may change the reply.
     * <p>
     * The default is true, i.e. site-local replies are replaced.
     * </p>
     *
     * @deprecated (3.6) use {@link #setPassiveNatWorkaroundStrategy(HostnameResolver)} instead
     * @param enabled true to enable replacing internal IP's in passive mode.
     */
    @Deprecated
    public void setPassiveNatWorkaround(final boolean enabled) {
        this.passiveNatWorkaroundStrategy = enabled ? new NatServerResolverImpl(this) : null;
    }

    /**
     * Sets the workaround strategy to replace the PASV mode reply addresses. This gets around the problem that some NAT boxes may change the reply.
     *
     * The default implementation is {@link NatServerResolverImpl}, i.e. site-local replies are replaced.
     *
     * @param passiveNatWorkaroundStrategy strategy to replace internal IP's in passive mode or null to disable the workaround (i.e. use PASV mode reply
     *                                     address.)
     * @since 3.6
     */
    public void setPassiveNatWorkaroundStrategy(final HostnameResolver passiveNatWorkaroundStrategy) {
        this.passiveNatWorkaroundStrategy = passiveNatWorkaroundStrategy;
    }

    /**
     * Sets the value to be used for the data socket SO_RCVBUF option. If the value is positive, the option will be set when the data socket has been created.
     *
     * @param receiveDataSocketBufferSize The size of the buffer, zero or negative means the value is ignored.
     * @since 3.3
     */
    public void setReceieveDataSocketBufferSize(final int receiveDataSocketBufferSize) {
        this.receiveDataSocketBufferSize = receiveDataSocketBufferSize;
    }

    /**
     * Enable or disable verification that the remote host taking part of a data connection is the same as the host to which the control connection is attached.
     * The default is for verification to be enabled. You may set this value at any time, whether the FTPClient is currently connected or not.
     *
     * @param remoteVerificationEnabled True to enable verification, false to disable verification.
     */
    public void setRemoteVerificationEnabled(final boolean remoteVerificationEnabled) {
        this.remoteVerificationEnabled = remoteVerificationEnabled;
    }

    /**
     * Sets the external IP address to report in EPRT/PORT commands in active mode. Useful when there are multiple network cards.
     *
     * @param ipAddress The external IP address of this machine.
     * @throws UnknownHostException if the ipAddress cannot be resolved
     * @since 3.1
     * @see #getReportHostAddress()
     */
    public void setReportActiveExternalIPAddress(final String ipAddress) throws UnknownHostException {
        this.reportActiveExternalHost = InetAddress.getByName(ipAddress);
    }

    /**
     * Sets the restart offset for file transfers.
     * <p>
     * The restart command is not sent to the server immediately. It is sent when a data connection is created as part of a subsequent command. The restart
     * marker is reset to zero after use.
     * </p>
     * <p>
     * <strong>Note: This method should only be invoked immediately prior to the transfer to which it applies.</strong>
     * </p>
     *
     * @param offset The offset into the remote file at which to start the next file transfer. This must be a value greater than or equal to zero.
     */
    public void setRestartOffset(final long offset) {
        if (offset >= 0) {
            restartOffset = offset;
        }
    }

    /**
     * Sets the value to be used for the data socket SO_SNDBUF option. If the value is positive, the option will be set when the data socket has been created.
     *
     * @param sendDataSocketBufferSize The size of the buffer, zero or negative means the value is ignored.
     * @since 3.3
     */
    public void setSendDataSocketBufferSize(final int sendDataSocketBufferSize) {
        this.sendDataSocketBufferSize = sendDataSocketBufferSize;
    }

    /**
     * Sets whether to use EPSV with IPv4. Might be worth enabling in some circumstances.
     *
     * For example, when using IPv4 with NAT it may work with some rare configurations. E.g. if FTP server has a static PASV address (external network) and the
     * client is coming from another internal network. In that case the data connection after PASV command would fail, while EPSV would make the client succeed
     * by taking just the port.
     *
     * @param useEPSVwithIPv4 value to set.
     * @since 2.2
     */
    public void setUseEPSVwithIPv4(final boolean useEPSVwithIPv4) {
        this.useEPSVwithIPv4 = useEPSVwithIPv4;
    }

    private boolean storeFile(final FTPCmd command, final String remote, final InputStream local) throws IOException {
        return _storeFile(command.getCommand(), remote, local);
    }

    /**
     * Stores a file on the server using the given name and taking input from the given InputStream. This method does NOT close the given InputStream. If the
     * current file type is ASCII, line separators in the file are transparently converted to the NETASCII format (i.e., you should not attempt to create a
     * special InputStream to do this).
     *
     * @param remote The name to give the remote file.
     * @param local  The local InputStream from which to read the file.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException                  If the FTP server prematurely closes the connection as a result of the client being idle or some
     *                                                       other reason causing the server to send FTP reply code 421. This exception may be caught either as
     *                                                       an IOException or independently as itself.
     * @throws org.apache.commons.net.io.CopyStreamException If an I/O error occurs while actually transferring the file. The CopyStreamException allows you to
     *                                                       determine the number of bytes transferred and the IOException causing the error. This exception may
     *                                                       be caught either as an IOException or independently as itself.
     * @throws IOException                                   If an I/O error occurs while either sending a command to the server or receiving a reply from the
     *                                                       server.
     */
    public boolean storeFile(final String remote, final InputStream local) throws IOException {
        return storeFile(FTPCmd.STOR, remote, local);
    }

    private OutputStream storeFileStream(final FTPCmd command, final String remote) throws IOException {
        return _storeFileStream(command.getCommand(), remote);
    }

    /**
     * Returns an OutputStream through which data can be written to store a file on the server using the given name. If the current file type is ASCII, the
     * returned OutputStream will convert line separators in the file to the NETASCII format (i.e., you should not attempt to create a special OutputStream to
     * do this). You must close the OutputStream when you finish writing to it. The OutputStream itself will take care of closing the parent data connection
     * socket upon being closed.
     * <p>
     * <strong>To finalize the file transfer you must call {@link #completePendingCommand completePendingCommand} and check its return value to verify
     * success.</strong> If this is not done, subsequent commands may behave unexpectedly.
     * </p>
     *
     * @param remote The name to give the remote file.
     * @return An OutputStream through which the remote file can be written. If the data connection cannot be opened (e.g., the file does not exist), null is
     *         returned (in which case you may check the reply code to determine the exact reason for failure).
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public OutputStream storeFileStream(final String remote) throws IOException {
        return storeFileStream(FTPCmd.STOR, remote);
    }

    /**
     * Stores a file on the server using a unique name assigned by the server and taking input from the given InputStream. This method does NOT close the given
     * InputStream. If the current file type is ASCII, line separators in the file are transparently converted to the NETASCII format (i.e., you should not
     * attempt to create a special InputStream to do this).
     *
     * @param local The local InputStream from which to read the file.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException                  If the FTP server prematurely closes the connection as a result of the client being idle or some
     *                                                       other reason causing the server to send FTP reply code 421. This exception may be caught either as
     *                                                       an IOException or independently as itself.
     * @throws org.apache.commons.net.io.CopyStreamException If an I/O error occurs while actually transferring the file. The CopyStreamException allows you to
     *                                                       determine the number of bytes transferred and the IOException causing the error. This exception may
     *                                                       be caught either as an IOException or independently as itself.
     * @throws IOException                                   If an I/O error occurs while either sending a command to the server or receiving a reply from the
     *                                                       server.
     */
    public boolean storeUniqueFile(final InputStream local) throws IOException {
        return storeFile(FTPCmd.STOU, null, local);
    }

    /**
     * Stores a file on the server using a unique name derived from the given name and taking input from the given InputStream. This method does NOT close the
     * given InputStream. If the current file type is ASCII, line separators in the file are transparently converted to the NETASCII format (i.e., you should
     * not attempt to create a special InputStream to do this).
     *
     * @param remote The name on which to base the unique name given to the remote file.
     * @param local  The local InputStream from which to read the file.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException                  If the FTP server prematurely closes the connection as a result of the client being idle or some
     *                                                       other reason causing the server to send FTP reply code 421. This exception may be caught either as
     *                                                       an IOException or independently as itself.
     * @throws org.apache.commons.net.io.CopyStreamException If an I/O error occurs while actually transferring the file. The CopyStreamException allows you to
     *                                                       determine the number of bytes transferred and the IOException causing the error. This exception may
     *                                                       be caught either as an IOException or independently as itself.
     * @throws IOException                                   If an I/O error occurs while either sending a command to the server or receiving a reply from the
     *                                                       server.
     */
    public boolean storeUniqueFile(final String remote, final InputStream local) throws IOException {
        return storeFile(FTPCmd.STOU, remote, local);
    }

    /**
     * Returns an OutputStream through which data can be written to store a file on the server using a unique name assigned by the server. If the current file
     * type is ASCII, the returned OutputStream will convert line separators in the file to the NETASCII format (i.e., you should not attempt to create a
     * special OutputStream to do this). You must close the OutputStream when you finish writing to it. The OutputStream itself will take care of closing the
     * parent data connection socket upon being closed.
     * <p>
     * <strong>To finalize the file transfer you must call {@link #completePendingCommand completePendingCommand} and check its return value to verify
     * success.</strong> If this is not done, subsequent commands may behave unexpectedly.
     * </p>
     *
     * @return An OutputStream through which the remote file can be written. If the data connection cannot be opened (e.g., the file does not exist), null is
     *         returned (in which case you may check the reply code to determine the exact reason for failure).
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public OutputStream storeUniqueFileStream() throws IOException {
        return storeFileStream(FTPCmd.STOU, null);
    }

    /**
     * Returns an OutputStream through which data can be written to store a file on the server using a unique name derived from the given name. If the current
     * file type is ASCII, the returned OutputStream will convert line separators in the file to the NETASCII format (i.e., you should not attempt to create a
     * special OutputStream to do this). You must close the OutputStream when you finish writing to it. The OutputStream itself will take care of closing the
     * parent data connection socket upon being closed.
     * <p>
     * <strong>To finalize the file transfer you must call {@link #completePendingCommand completePendingCommand} and check its return value to verify
     * success.</strong> If this is not done, subsequent commands may behave unexpectedly.
     * </p>
     *
     * @param remote The name on which to base the unique name given to the remote file.
     * @return An OutputStream through which the remote file can be written. If the data connection cannot be opened (e.g., the file does not exist), null is
     *         returned (in which case you may check the reply code to determine the exact reason for failure).
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public OutputStream storeUniqueFileStream(final String remote) throws IOException {
        return storeFileStream(FTPCmd.STOU, remote);
    }

    /**
     * Issue the FTP SMNT command.
     *
     * @param path The path to mount.
     * @return True if successfully completed, false if not.
     * @throws FTPConnectionClosedException If the FTP server prematurely closes the connection as a result of the client being idle or some other reason
     *                                      causing the server to send FTP reply code 421. This exception may be caught either as an IOException or
     *                                      independently as itself.
     * @throws IOException                  If an I/O error occurs while either sending a command to the server or receiving a reply from the server.
     */
    public boolean structureMount(final String path) throws IOException {
        return FTPReply.isPositiveCompletion(smnt(path));
    }

    private Socket wrapOnDeflate(final Socket plainSocket) {
        switch (fileTransferMode) {
        case DEFLATE_TRANSFER_MODE:
            return new DeflateSocket(plainSocket);
        // Experiment, not in an RFC?
        // case GZIP_TRANSFER_MODE:
        // return new GZIPSocket(plainSocket);
        default:
            return plainSocket;
        }
    }
}
