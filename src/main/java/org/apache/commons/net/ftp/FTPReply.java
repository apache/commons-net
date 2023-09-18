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

/**
 * FTPReply stores a set of constants for FTP reply codes. To interpret the meaning of the codes, familiarity with RFC 959 is assumed. The mnemonic constant
 * names are transcriptions from the code descriptions of RFC 959.
 * <p>
 * TODO replace with an enum
 * </p>
 */
public final class FTPReply {

    /** Reply code {@value}. */
    public static final int RESTART_MARKER = 110;

    /** Reply code {@value}. */
    public static final int SERVICE_NOT_READY = 120;

    /** Reply code {@value}. */
    public static final int DATA_CONNECTION_ALREADY_OPEN = 125;

    /** Reply code {@value}. */
    public static final int FILE_STATUS_OK = 150;

    /** Reply code {@value}. */
    public static final int COMMAND_OK = 200;

    /** Reply code {@value}. */
    public static final int COMMAND_IS_SUPERFLUOUS = 202;

    /** Reply code {@value}. */
    public static final int SYSTEM_STATUS = 211;

    /** Reply code {@value}. */
    public static final int DIRECTORY_STATUS = 212;

    /** Reply code {@value}. */
    public static final int FILE_STATUS = 213;

    /** Reply code {@value}. */
    public static final int HELP_MESSAGE = 214;

    /** Reply code {@value}. */
    public static final int NAME_SYSTEM_TYPE = 215;

    /** Reply code {@value}. */
    public static final int SERVICE_READY = 220;

    /** Reply code {@value}. */
    public static final int SERVICE_CLOSING_CONTROL_CONNECTION = 221;

    /** Reply code {@value}. */
    public static final int DATA_CONNECTION_OPEN = 225;

    /** Reply code {@value}. */
    public static final int CLOSING_DATA_CONNECTION = 226;

    /** Reply code {@value}. */
    public static final int ENTERING_PASSIVE_MODE = 227;

    /**
     * Reply code {@value}.
     *
     * @since 2.2
     */
    public static final int ENTERING_EPSV_MODE = 229;

    /** Reply code {@value}. */
    public static final int USER_LOGGED_IN = 230;

    /** Reply code {@value}. */
    public static final int FILE_ACTION_OK = 250;

    /** Reply code {@value}. */
    public static final int PATHNAME_CREATED = 257;

    /** Reply code {@value}. */
    public static final int NEED_PASSWORD = 331;

    /** Reply code {@value}. */
    public static final int NEED_ACCOUNT = 332;

    /** Reply code {@value}. */
    public static final int FILE_ACTION_PENDING = 350;

    /** Reply code {@value}. */
    public static final int SERVICE_NOT_AVAILABLE = 421;

    /** Reply code {@value}. */
    public static final int CANNOT_OPEN_DATA_CONNECTION = 425;

    /** Reply code {@value}. */
    public static final int TRANSFER_ABORTED = 426;

    /** Reply code {@value}. */
    public static final int FILE_ACTION_NOT_TAKEN = 450;

    /** Reply code {@value}. */
    public static final int ACTION_ABORTED = 451;

    /** Reply code {@value}. */
    public static final int INSUFFICIENT_STORAGE = 452;

    /** Reply code {@value}. */
    public static final int UNRECOGNIZED_COMMAND = 500;

    /** Reply code {@value}. */
    public static final int SYNTAX_ERROR_IN_ARGUMENTS = 501;

    /** Reply code {@value}. */
    public static final int COMMAND_NOT_IMPLEMENTED = 502;

    /** Reply code {@value}. */
    public static final int BAD_COMMAND_SEQUENCE = 503;

    /** Reply code {@value}. */
    public static final int COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER = 504;

    /** Reply code {@value}. */
    public static final int NOT_LOGGED_IN = 530;

    /** Reply code {@value}. */
    public static final int NEED_ACCOUNT_FOR_STORING_FILES = 532;

    /** Reply code {@value}. */
    public static final int FILE_UNAVAILABLE = 550;

    /** Reply code {@value}. */
    public static final int PAGE_TYPE_UNKNOWN = 551;

    /** Reply code {@value}. */
    public static final int STORAGE_ALLOCATION_EXCEEDED = 552;

    /** Reply code {@value}. */
    public static final int FILE_NAME_NOT_ALLOWED = 553;

    // FTPS Reply Codes

    /**
     * FTPS reply code {@value}
     *
     * @since 2.0
     */
    public static final int SECURITY_DATA_EXCHANGE_COMPLETE = 234;

    /**
     * FTPS reply code {@value}
     *
     * @since 2.0
     */
    public static final int SECURITY_DATA_EXCHANGE_SUCCESSFULLY = 235;

    /**
     * FTPS reply code {@value}
     *
     * @since 2.0
     */
    public static final int SECURITY_MECHANISM_IS_OK = 334;

    /**
     * FTPS reply code {@value}
     *
     * @since 2.0
     */
    public static final int SECURITY_DATA_IS_ACCEPTABLE = 335;

    /**
     * FTPS reply code {@value}
     *
     * @since 2.0
     */
    public static final int UNAVAILABLE_RESOURCE = 431;

    /**
     * FTPS reply code {@value}
     *
     * @since 2.0
     */
    public static final int BAD_TLS_NEGOTIATION_OR_DATA_ENCRYPTION_REQUIRED = 522;

    /**
     * FTPS reply code {@value}
     *
     * @since 2.0
     */
    public static final int DENIED_FOR_POLICY_REASONS = 533;

    /**
     * FTPS reply code {@value}
     *
     * @since 2.0
     */
    public static final int REQUEST_DENIED = 534;

    /**
     * FTPS reply code {@value}
     *
     * @since 2.0
     */
    public static final int FAILED_SECURITY_CHECK = 535;

    /**
     * FTPS reply code {@value}
     *
     * @since 2.0
     */
    public static final int REQUESTED_PROT_LEVEL_NOT_SUPPORTED = 536;

    /**
     * IPv6 error codes.
     * <p>
     * Note this is also used as an FTPS error code reply
     * </p>
     *
     * @since 2.2
     */
    public static final int EXTENDED_PORT_FAILURE = 522;

    /**
     * Tests if a reply code is a negative permanent response. All codes beginning with a 5 are negative permanent responses. The FTP server will send a
     * negative permanent response on the failure of a command that cannot be reattempted with success.
     *
     * @param reply The reply code to test.
     * @return True if a reply code is a negative permanent response, false if not.
     */
    public static boolean isNegativePermanent(final int reply) {
        return reply >= 500 && reply < 600;
    }

    /**
     * Tests if a reply code is a negative transient response. All codes beginning with a 4 are negative transient responses. The FTP server will send a
     * negative transient response on the failure of a command that can be reattempted with success.
     *
     * @param reply The reply code to test.
     * @return True if a reply code is a negative transient response, false if not.
     */
    public static boolean isNegativeTransient(final int reply) {
        return reply >= 400 && reply < 500;
    }

    /**
     * Tests if a reply code is a positive completion response. All codes beginning with a 2 are positive completion responses. The FTP server will send a
     * positive completion response on the final successful completion of a command.
     *
     * @param reply The reply code to test.
     * @return True if a reply code is a positive completion response, false if not.
     */
    public static boolean isPositiveCompletion(final int reply) {
        return reply >= 200 && reply < 300;
    }

    /**
     * Tests if a reply code is a positive intermediate response. All codes beginning with a 3 are positive intermediate responses. The FTP server will send
     * a positive intermediate response on the successful completion of one part of a multipart sequence of commands. For example, after a successful USER
     * command, a positive intermediate response will be sent to indicate that the server is ready for the PASS command.
     *
     * @param reply The reply code to test.
     * @return True if a reply code is a positive intermediate response, false if not.
     */
    public static boolean isPositiveIntermediate(final int reply) {
        return reply >= 300 && reply < 400;
    }

    /**
     * Tests if a reply code is a positive preliminary response. All codes beginning with a 1 are positive preliminary responses. Postitive preliminary
     * responses are used to indicate tentative success. No further commands can be issued to the FTP server after a positive preliminary response until a
     * follow-up response is received from the server.
     *
     * @param reply The reply code to test.
     * @return True if a reply code is a positive preliminary response, false if not.
     */
    public static boolean isPositivePreliminary(final int reply) {
        return reply >= 100 && reply < 200;
    }

    /**
     * Tests if a reply code is a protected response.
     *
     * @param reply The reply code to test.
     * @return True if a reply code is a protected response, false if not.
     * @since 3.0
     */
    public static boolean isProtectedReplyCode(final int reply) {
        // actually, only 3 protected reply codes are
        // defined in RFC 2228: 631, 632 and 633.
        return reply >= 600 && reply < 700;
    }

    /** Cannot be instantiated. */
    private FTPReply() {
    }

}
