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
 */

package org.apache.commons.net.ftp;

/**
 * @since 3.3
 */
public enum FTPCmd {
    ABOR, ACCT, ALLO, APPE, CDUP, CWD, DELE, EPRT, EPSV, FEAT, HELP, LIST, MDTM, MFMT, MKD, MLSD, MLST, MODE, NLST, NOOP, PASS, PASV, PORT, PWD, QUIT, REIN,
    REST, RETR, RMD, RNFR, RNTO, SITE,
    /** @since 3.7 */
    SIZE, SMNT, STAT, STOR, STOU, STRU, SYST, TYPE, USER;

    // Aliases

    /** Alias. */
    public static final FTPCmd ABORT = ABOR;

    /** Alias. */
    public static final FTPCmd ACCOUNT = ACCT;

    /** Alias. */
    public static final FTPCmd ALLOCATE = ALLO;

    /** Alias. */
    public static final FTPCmd APPEND = APPE;

    /** Alias. */
    public static final FTPCmd CHANGE_TO_PARENT_DIRECTORY = CDUP;

    /** Alias. */
    public static final FTPCmd CHANGE_WORKING_DIRECTORY = CWD;

    /** Alias. */
    public static final FTPCmd DATA_PORT = PORT;

    /** Alias. */
    public static final FTPCmd DELETE = DELE;

    /** Alias. */
    public static final FTPCmd FEATURES = FEAT;

    /** Alias. */
    public static final FTPCmd FILE_STRUCTURE = STRU;

    /** Alias. */
    public static final FTPCmd GET_MOD_TIME = MDTM;

    /** Alias. */
    public static final FTPCmd LOGOUT = QUIT;

    /** Alias. */
    public static final FTPCmd MAKE_DIRECTORY = MKD;

    /** Alias. */
    public static final FTPCmd MOD_TIME = MDTM;

    /** Alias. */
    public static final FTPCmd NAME_LIST = NLST;

    /** Alias. */
    public static final FTPCmd PASSIVE = PASV;

    /** Alias. */
    public static final FTPCmd PASSWORD = PASS;

    /** Alias. */
    public static final FTPCmd PRINT_WORKING_DIRECTORY = PWD;

    /** Alias. */
    public static final FTPCmd REINITIALIZE = REIN;

    /** Alias. */
    public static final FTPCmd REMOVE_DIRECTORY = RMD;

    /** Alias. */
    public static final FTPCmd RENAME_FROM = RNFR;

    /** Alias. */
    public static final FTPCmd RENAME_TO = RNTO;

    /** Alias. */
    public static final FTPCmd REPRESENTATION_TYPE = TYPE;

    /** Alias. */
    public static final FTPCmd RESTART = REST;

    /** Alias. */
    public static final FTPCmd RETRIEVE = RETR;

    /** Alias. */
    public static final FTPCmd SET_MOD_TIME = MFMT;

    /** Alias. */
    public static final FTPCmd SITE_PARAMETERS = SITE;

    /** Alias. */
    public static final FTPCmd STATUS = STAT;

    /** Alias. */
    public static final FTPCmd STORE = STOR;

    /** Alias. */
    public static final FTPCmd STORE_UNIQUE = STOU;

    /** Alias. */
    public static final FTPCmd STRUCTURE_MOUNT = SMNT;

    /** Alias. */
    public static final FTPCmd SYSTEM = SYST;

    /** Alias. */
    public static final FTPCmd TRANSFER_MODE = MODE;

    /** Alias. */
    public static final FTPCmd USERNAME = USER;

    /**
     * Retrieve the FTP protocol command string corresponding to a specified command code.
     *
     * @return The FTP protcol command string corresponding to a specified command code.
     */
    public final String getCommand() {
        return this.name();
    }

}
