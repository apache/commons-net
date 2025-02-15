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
 * Enumerates FTP commands.
 *
 * @since 3.3
 */
public enum FTPCmd {

    /** FTP command ABOR. */
    ABOR,

    /** FTP command ACCT. */
    ACCT,

    /** FTP command ALLO. */
    ALLO,

    /** FTP command APPE. */
    APPE,

    /** FTP command CDUP. */
    CDUP,

    /** FTP command CWD. */
    CWD,

    /** FTP command DELE. */
    DELE,

    /** FTP command EPRT. */
    EPRT,

    /** FTP command EPSV. */
    EPSV,

    /** FTP command <a href="https://datatracker.ietf.org/doc/html/rfc2389#section-3">FEAT</a>. */
    FEAT,

    /** FTP command HELP. */
    HELP,

    /** FTP command LIST. */
    LIST,

    /** FTP command MDTM. */
    MDTM,

    /** FTP command MFMT. */
    MFMT,

    /** FTP command MKD. */
    MKD,

    /** FTP command MLSD. */
    MLSD,

    /** FTP command MLST. */
    MLST,

    /** FTP command MODE. */
    MODE,

    /** FTP command NLST. */
    NLST,

    /** FTP command NOOP. */
    NOOP,

    /**
     * FTP command <a href="https://datatracker.ietf.org/doc/html/rfc2389#section-4">OPTS</a>.
     *
     * @since 3.12.0
     */
    OPTS,

    /** FTP command PASS. */
    PASS,

    /** FTP command PASV. */
    PASV,

    /** FTP command PORT. */
    PORT,

    /** FTP command PWD. */
    PWD,

    /** FTP command QUIT. */
    QUIT,

    /** FTP command REIN. */
    REIN,

    /** FTP command REST. */
    REST,

    /** FTP command RETR. */
    RETR,

    /** FTP command RMD. */
    RMD,

    /** FTP command RNFR. */
    RNFR,

    /** FTP command RNTO. */
    RNTO,

    /** FTP command SITE. */
    SITE,

    /**
     * FTP command SIZE.
     *
     * @since 3.7
     */
    SIZE,

    /** FTP command SMNT. */
    SMNT,

    /** FTP command STAT. */
    STAT,

    /** FTP command STOR. */
    STOR,

    /** FTP command STOU. */
    STOU,

    /** FTP command STRU. */
    STRU,

    /** FTP command SYST. */
    SYST,

    /** FTP command TYPE. */
    TYPE,

    /** FTP command USER. */
    USER;

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
    public String getCommand() {
        return name();
    }

}
