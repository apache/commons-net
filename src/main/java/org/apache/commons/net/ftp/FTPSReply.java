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

/**
 * I confirm a response cord of a command added in FTPS.
 */
public final class FTPSReply {
    public static final int CODE_200 = 200;
    public static final int CODE_234 = 234;
    public static final int CODE_235 = 235;
    public static final int CODE_334 = 334;
    public static final int CODE_335 = 335;
    public static final int CODE_421 = 421;
    public static final int CODE_431 = 431;
    public static final int CODE_500 = 500;
    public static final int CODE_501 = 501;
    public static final int CODE_502 = 502;
    public static final int CODE_503 = 503;
    public static final int CODE_504 = 504;
    public static final int CODE_530 = 530;
    public static final int CODE_533 = 533;
    public static final int CODE_534 = 534;
    public static final int CODE_535 = 535;
    public static final int CODE_536 = 536;
    
    public static final int COMMAND_OK = CODE_200;
    public static final int SECURITY_DATA_EXCHANGE_COMPLETE = CODE_234;
    public static final int SECURITY_DATA_EXCHANGE_SUCCESSFULLY = CODE_235;
    public static final int SECURITY_MECHANISM_IS_OK = CODE_334;
    public static final int SECURITY_DATA_IS_ACCEPTABLE = CODE_335;
    public static final int SERVICE_NOT_AVAILABLE = CODE_421;
    public static final int UNAVAILABLE_RESOURCE = CODE_431;
    public static final int UNRECOGNIZED_COMMAND = CODE_500;
    public static final int SYNTAX_ERROR_IN_ARGUMENTS = CODE_501;
    public static final int COMMAND_NOT_IMPLEMENTED = CODE_502;
    public static final int BAD_COMMAND_SEQUENCE = CODE_503;
    public static final int COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER=CODE_504;
    public static final int NOT_LOGGED_IN = CODE_530;
    public static final int DENIED_FOR_POLICY_REASONS = CODE_533;
    public static final int REQUEST_DENIED = CODE_534;
    public static final int FAILED_SECURITY_CHECK = CODE_535;
    public static final int REQUESTED_PROT_LEVEL_NOT_SUPPORTED = CODE_536;

    /**
     * Determine if a reply code is a positive preliminary response.  All
     * codes beginning with a 1 are positive preliminary responses.
     * Postitive preliminary responses are used to indicate tentative success.
     * No further commands can be issued to the FTP server after a positive
     * preliminary response until a follow up response is received from the
     * server.
     * <p>
     * @param reply The reply code.
     * @return True if a reply code is a postive preliminary pesponse, 
     *  false if not.
     */
    public static boolean isPositivePreliminary(int reply) {
        return (reply >= 100 && reply < 200);
    }

    /**
     * Determine if a reply code is a positive completion response.  All
     * codes beginning with a 2 are positive completion responses.
     * The FTP server will send a positive completion response on the final
     * successful completion of a command.
     * <p>
     * @param reply  The reply code.
     * @return True if a reply code is a postive completion response,
     *  false if not.
     */
    public static boolean isPositiveCompletion(int reply) {
        return (reply >= 200 && reply < 300);
    }

    /**
     * Determine if a reply code is a positive intermediate response.  All
     * codes beginning with a 3 are positive intermediate responses.
     * The FTP server will send a positive intermediate response on the
     * successful completion of one part of a multi-part sequence of
     * commands.  For example, after a successful USER command, a positive
     * intermediate response will be sent to indicate that the server is
     * ready for the PASS command.
     * <p>
     * @param reply The reply code.
     * @return True if a reply code is a postive intermediate response,
     *  false if not.
     */
    public static boolean isPositiveIntermediate(int reply) {
        return (reply >= 300 && reply < 400);
    }

    /**
     * Determine if a reply code is a negative transient response.  All
     * codes beginning with a 4 are negative transient responses.
     * The FTP server will send a negative transient response on the
     * failure of a command that can be reattempted with success.
     * <p>
     * @param reply The reply code.
     * @return True if a reply code is a negative transient response,
     *  false if not.
     */
    public static boolean isNegativeTransient(int reply) {
        return (reply >= 400 && reply < 500);
    }

    /**
     * Determine if a reply code is a negative permanent response.  All
     * codes beginning with a 5 are negative permanent responses.
     * The FTP server will send a negative permanent response on the
     * failure of a command that cannot be reattempted with success.
     * <p>
     * @param reply The reply code.
     * @return True if a reply code is a negative permanent response,
     *  false if not.
     */
    public static boolean isNegativePermanent(int reply) {
        return (reply >= 500 && reply < 600);
    }
}
