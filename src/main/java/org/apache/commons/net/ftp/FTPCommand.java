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
 * FTPCommand stores a set of constants for FTP command codes. To interpret the meaning of the codes, familiarity with RFC 959 is assumed. The mnemonic constant
 * names are transcriptions from the code descriptions of RFC 959. For those who think in terms of the actual FTP commands, a set of constants such as
 * {@link #USER USER } are provided where the constant name is the same as the FTP command.
 *
 * @deprecated use {@link FTPCmd} instead
 */
@Deprecated
public final class FTPCommand {

    /** Command code {@value}. */
    public static final int USER = 0;

    /** Command code {@value}. */
    public static final int PASS = 1;

    /** Command code {@value}. */
    public static final int ACCT = 2;

    /** Command code {@value}. */
    public static final int CWD = 3;

    /** Command code {@value}. */
    public static final int CDUP = 4;

    /** Command code {@value}. */
    public static final int SMNT = 5;

    /** Command code {@value}. */
    public static final int REIN = 6;

    /** Command code {@value}. */
    public static final int QUIT = 7;

    /** Command code {@value}. */
    public static final int PORT = 8;

    /** Command code {@value}. */
    public static final int PASV = 9;

    /** Command code {@value}. */
    public static final int TYPE = 10;

    /** Command code {@value}. */
    public static final int STRU = 11;

    /** Command code {@value}. */
    public static final int MODE = 12;

    /** Command code {@value}. */
    public static final int RETR = 13;

    /** Command code {@value}. */
    public static final int STOR = 14;

    /** Command code {@value}. */

    /** Command code {@value}. */
    public static final int STOU = 15;

    /** Command code {@value}. */
    public static final int APPE = 16;

    /** Command code {@value}. */

    /** Command code {@value}. */
    public static final int ALLO = 17;

    /** Command code {@value}. */
    public static final int REST = 18;

    /** Command code {@value}. */
    public static final int RNFR = 19;

    /** Command code {@value}. */
    public static final int RNTO = 20;

    /** Command code {@value}. */
    public static final int ABOR = 21;

    /** Command code {@value}. */
    public static final int DELE = 22;

    /** Command code {@value}. */
    public static final int RMD = 23;

    /** Command code {@value}. */
    public static final int MKD = 24;

    /** Command code {@value}. */
    public static final int PWD = 25;

    /** Command code {@value}. */
    public static final int LIST = 26;

    /** Command code {@value}. */
    public static final int NLST = 27;

    /** Command code {@value}. */
    public static final int SITE = 28;

    /** Command code {@value}. */
    public static final int SYST = 29;

    /** Command code {@value}. */
    public static final int STAT = 30;

    /** Command code {@value}. */

    /** Command code {@value}. */
    public static final int HELP = 31;

    /** Command code {@value}. */
    public static final int NOOP = 32;


    /**
     * Command code {@value}.
     *
     * @since 2.0
     */
    public static final int MDTM = 33;

    /**
     * Command code {@value}.
     *
     * @since 2.2
     */
    public static final int FEAT = 34;

    /**
     * Command code {@value}.
     *
     * @since 2.0
     */
    public static final int MFMT = 35;

    /**
     * Command code {@value}.
     *
     * @since 2.0
     */
    public static final int EPSV = 36;

    /**
     * Command code {@value}.
     *
     * @since 2.0
     */
    public static final int EPRT = 37;

    /**
     * Machine parseable list for a directory.
     *
     * @since 3.0
     */
    public static final int MLSD = 38;

    /**
     * Machine parseable list for a single file.
     *
     * @since 3.0
     */
    public static final int MLST = 39;

    // Must agree with final entry above; used to check array size
    private static final int LAST = MLST;

    /** Alias for {@value}. */
    public static final int USERNAME = USER;

    /** Alias for {@value}. */
    public static final int PASSWORD = PASS;

    /** Alias for {@value}. */
    public static final int ACCOUNT = ACCT;

    /** Alias for {@value}. */
    public static final int CHANGE_WORKING_DIRECTORY = CWD;

    /** Alias for {@value}. */
    public static final int CHANGE_TO_PARENT_DIRECTORY = CDUP;

    /** Alias for {@value}. */
    public static final int STRUCTURE_MOUNT = SMNT;

    /** Alias for {@value}. */
    public static final int REINITIALIZE = REIN;

    /** Alias for {@value}. */
    public static final int LOGOUT = QUIT;

    /** Alias for {@value}. */
    public static final int DATA_PORT = PORT;

    /** Alias for {@value}. */
    public static final int PASSIVE = PASV;

    /** Alias for {@value}. */
    public static final int REPRESENTATION_TYPE = TYPE;

    /** Alias for {@value}. */
    public static final int FILE_STRUCTURE = STRU;

    /** Alias for {@value}. */
    public static final int TRANSFER_MODE = MODE;

    /** Alias for {@value}. */
    public static final int RETRIEVE = RETR;

    /** Alias for {@value}. */
    public static final int STORE = STOR;

    /** Alias for {@value}. */
    public static final int STORE_UNIQUE = STOU;

    /** Alias for {@value}. */
    public static final int APPEND = APPE;

    /** Alias for {@value}. */
    public static final int ALLOCATE = ALLO;

    /** Alias for {@value}. */
    public static final int RESTART = REST;

    /** Alias for {@value}. */
    public static final int RENAME_FROM = RNFR;

    /** Alias for {@value}. */
    public static final int RENAME_TO = RNTO;

    /** Alias for {@value}. */
    public static final int ABORT = ABOR;

    /** Alias for {@value}. */
    public static final int DELETE = DELE;

    /** Alias for {@value}. */
    public static final int REMOVE_DIRECTORY = RMD;

    /** Alias for {@value}. */
    public static final int MAKE_DIRECTORY = MKD;

    /** Alias for {@value}. */
    public static final int PRINT_WORKING_DIRECTORY = PWD;

    // public static final int LIST = LIST;

    /** Alias for {@value}. */
    public static final int NAME_LIST = NLST;

    /** Alias for {@value}. */
    public static final int SITE_PARAMETERS = SITE;

    /** Alias for {@value}. */
    public static final int SYSTEM = SYST;

    /** Alias for {@value}. */
    public static final int STATUS = STAT;

    // public static final int HELP = HELP;
    // public static final int NOOP = NOOP;

    /**
     * Alias for {@value}.
     * @since 2.0
     */
    public static final int MOD_TIME = MDTM;

    /**
     * Alias for {@value}.
     * @since 2.2
     */
    public static final int FEATURES = FEAT;

    /**
     * Alias for {@value}.
     * @since 2.2
     */
    public static final int GET_MOD_TIME = MDTM;

    /**
     * Alias for {@value}.
     * @since 2.2
     */
    public static final int SET_MOD_TIME = MFMT;

    private static final String[] COMMANDS = { "USER", "PASS", "ACCT", "CWD", "CDUP", "SMNT", "REIN", "QUIT", "PORT", "PASV", "TYPE", "STRU", "MODE", "RETR",
            "STOR", "STOU", "APPE", "ALLO", "REST", "RNFR", "RNTO", "ABOR", "DELE", "RMD", "MKD", "PWD", "LIST", "NLST", "SITE", "SYST", "STAT", "HELP", "NOOP",
            "MDTM", "FEAT", "MFMT", "EPSV", "EPRT", "MLSD", "MLST" };

    /**
     * Default access needed for Unit test.
     */
    static void checkArray() {
        final int expectedLength = LAST + 1;
        if (COMMANDS.length != expectedLength) {
            throw new IllegalStateException("Incorrect COMMANDS array. Should have length " + expectedLength + " found " + COMMANDS.length);
        }
    }

    /**
     * Retrieve the FTP protocol command string corresponding to a specified command code.
     *
     * @param command The command code.
     * @return The FTP protcol command string corresponding to a specified command code.
     */
    public static String getCommand(final int command) {
        return COMMANDS[command];
    }

    /** Cannot be instantiated. */
    private FTPCommand() {
    }
}
