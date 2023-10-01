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

package org.apache.commons.net.ftp.parser;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;

/**
 * Parser class for MSLT and MLSD replies. See RFC 3659.
 * <p>
 * Format is as follows:
 * </p>
 *
 * <pre>
 * entry            = [ facts ] SP pathname
 * facts            = 1*( fact ";" )
 * fact             = factname "=" value
 * factname         = "Size" / "Modify" / "Create" /
 *                    "Type" / "Unique" / "Perm" /
 *                    "Lang" / "Media-Type" / "CharSet" /
 * os-depend-fact / local-fact
 * os-depend-fact   = {IANA assigned OS name} "." token
 * local-fact       = "X." token
 * value            = *SCHAR
 *
 * Sample os-depend-fact:
 * UNIX.group=0;UNIX.mode=0755;UNIX.owner=0;
 * </pre>
 * <p>
 * A single control response entry (MLST) is returned with a leading space; multiple (data) entries are returned without any leading spaces. The parser requires
 * that the leading space from the MLST entry is removed. MLSD entries can begin with a single space if there are no facts.
 * </p>
 *
 * @since 3.0
 */
public class MLSxEntryParser extends FTPFileEntryParserImpl {
    // This class is immutable, so a single instance can be shared.
    private static final MLSxEntryParser INSTANCE = new MLSxEntryParser();

    private static final HashMap<String, Integer> TYPE_TO_INT = new HashMap<>();
    static {
        TYPE_TO_INT.put("file", Integer.valueOf(FTPFile.FILE_TYPE));
        TYPE_TO_INT.put("cdir", Integer.valueOf(FTPFile.DIRECTORY_TYPE)); // listed directory
        TYPE_TO_INT.put("pdir", Integer.valueOf(FTPFile.DIRECTORY_TYPE)); // a parent dir
        TYPE_TO_INT.put("dir", Integer.valueOf(FTPFile.DIRECTORY_TYPE)); // dir or sub-dir
    }

    private static final int[] UNIX_GROUPS = { // Groups in order of mode digits
            FTPFile.USER_ACCESS, FTPFile.GROUP_ACCESS, FTPFile.WORLD_ACCESS, };

    private static final int[][] UNIX_PERMS = { // perm bits, broken down by octal int value
            /* 0 */ {}, /* 1 */ { FTPFile.EXECUTE_PERMISSION }, /* 2 */ { FTPFile.WRITE_PERMISSION },
            /* 3 */ { FTPFile.EXECUTE_PERMISSION, FTPFile.WRITE_PERMISSION }, /* 4 */ { FTPFile.READ_PERMISSION },
            /* 5 */ { FTPFile.READ_PERMISSION, FTPFile.EXECUTE_PERMISSION }, /* 6 */ { FTPFile.READ_PERMISSION, FTPFile.WRITE_PERMISSION },
            /* 7 */ { FTPFile.READ_PERMISSION, FTPFile.WRITE_PERMISSION, FTPFile.EXECUTE_PERMISSION }, };

    public static MLSxEntryParser getInstance() {
        return INSTANCE;
    }

    public static FTPFile parseEntry(final String entry) {
        return INSTANCE.parseFTPEntry(entry);
    }

    /**
     * Parse a GMT time stamp of the form yyyyMMDDHHMMSS[.sss]
     *
     * @param timestamp the date-time to parse
     * @return a Calendar entry, may be {@code null}
     * @since 3.4
     */
    public static Calendar parseGMTdateTime(final String timestamp) {
        final SimpleDateFormat dateFormat;
        final boolean hasMillis;
        if (timestamp.contains(".")) {
            dateFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
            hasMillis = true;
        } else {
            dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            hasMillis = false;
        }
        final TimeZone gmtTimeZone = TimeZone.getTimeZone("GMT");
        // both time zones need to be set for the parse to work OK
        dateFormat.setTimeZone(gmtTimeZone);
        final GregorianCalendar gCalendar = new GregorianCalendar(gmtTimeZone);
        final ParsePosition pos = new ParsePosition(0);
        dateFormat.setLenient(false); // We want to parse the whole string
        final Date parsed = dateFormat.parse(timestamp, pos);
        if (pos.getIndex() != timestamp.length()) {
            return null; // did not fully parse the input
        }
        gCalendar.setTime(parsed);
        if (!hasMillis) {
            gCalendar.clear(Calendar.MILLISECOND); // flag up missing ms units
        }
        return gCalendar;
    }

    /**
     * Parse a GMT time stamp of the form yyyyMMDDHHMMSS[.sss]
     *
     * @param timestamp the date-time to parse
     * @return a Calendar entry, may be {@code null}
     * @since 3.9.0
     */
    public static Instant parseGmtInstant(final String timestamp) {
        return parseGMTdateTime(timestamp).toInstant();
    }

    /**
     * Create the parser for MSLT and MSLD listing entries This class is immutable, so one can use {@link #getInstance()} instead.
     */
    public MLSxEntryParser() {
    }

    // perm-fact = "Perm" "=" *pvals
    // pvals = "a" / "c" / "d" / "e" / "f" /
    // "l" / "m" / "p" / "r" / "w"
    private void doUnixPerms(final FTPFile file, final String valueLowerCase) {
        for (final char c : valueLowerCase.toCharArray()) {
            // TODO these are mostly just guesses at present
            switch (c) {
            case 'a': // (file) may APPEnd
                file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
                break;
            case 'c': // (dir) files may be created in the dir
                file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
                break;
            case 'd': // deletable
                file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
                break;
            case 'e': // (dir) can change to this dir
                file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, true);
                break;
            case 'f': // (file) renamable
                // ?? file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
                break;
            case 'l': // (dir) can be listed
                file.setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, true);
                break;
            case 'm': // (dir) can create directory here
                file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
                break;
            case 'p': // (dir) entries may be deleted
                file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
                break;
            case 'r': // (files) file may be RETRieved
                file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, true);
                break;
            case 'w': // (files) file may be STORed
                file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
                break;
            default:
                break;
            // ignore unexpected flag for now.
            } // switch
        } // each char
    }

    @Override
    public FTPFile parseFTPEntry(final String entry) {
        if (entry.startsWith(" ")) {// leading space means no facts are present
            if (entry.length() > 1) { // is there a path name?
                final FTPFile file = new FTPFile();
                file.setRawListing(entry);
                file.setName(entry.substring(1));
                return file;
            }
            return null; // Invalid - no pathname

        }
        final String[] parts = entry.split(" ", 2); // Path may contain space
        if (parts.length != 2 || parts[1].isEmpty()) {
            return null; // no space found or no file name
        }
        final String factList = parts[0];
        if (!factList.endsWith(";")) {
            return null;
        }
        final FTPFile file = new FTPFile();
        file.setRawListing(entry);
        file.setName(parts[1]);
        final String[] facts = factList.split(";");
        final boolean hasUnixMode = parts[0].toLowerCase(Locale.ENGLISH).contains("unix.mode=");
        for (final String fact : facts) {
            final String[] factparts = fact.split("=", -1); // Don't drop empty values
// Sample missing permission
// drwx------   2 mirror   mirror       4096 Mar 13  2010 subversion
// modify=20100313224553;perm=;type=dir;unique=811U282598;UNIX.group=500;UNIX.mode=0700;UNIX.owner=500; subversion
            if (factparts.length != 2) {
                return null; // invalid - there was no "=" sign
            }
            final String factname = factparts[0].toLowerCase(Locale.ENGLISH);
            final String factvalue = factparts[1];
            if (factvalue.isEmpty()) {
                continue; // nothing to see here
            }
            final String valueLowerCase = factvalue.toLowerCase(Locale.ENGLISH);
            if ("size".equals(factname) || "sizd".equals(factname)) {
                file.setSize(Long.parseLong(factvalue));
            } else if ("modify".equals(factname)) {
                final Calendar parsed = parseGMTdateTime(factvalue);
                if (parsed == null) {
                    return null;
                }
                file.setTimestamp(parsed);
            } else if ("type".equals(factname)) {
                final Integer intType = TYPE_TO_INT.get(valueLowerCase);
                if (intType == null) {
                    file.setType(FTPFile.UNKNOWN_TYPE);
                } else {
                    file.setType(intType.intValue());
                }
            } else if (factname.startsWith("unix.")) {
                final String unixfact = factname.substring("unix.".length()).toLowerCase(Locale.ENGLISH);
                if ("group".equals(unixfact)) {
                    file.setGroup(factvalue);
                } else if ("owner".equals(unixfact)) {
                    file.setUser(factvalue);
                } else if ("mode".equals(unixfact)) { // e.g. 0[1]755
                    final int off = factvalue.length() - 3; // only parse last 3 digits
                    for (int i = 0; i < 3; i++) {
                        final int ch = factvalue.charAt(off + i) - '0';
                        if (ch >= 0 && ch <= 7) { // Check it's valid octal
                            for (final int p : UNIX_PERMS[ch]) {
                                file.setPermission(UNIX_GROUPS[i], p, true);
                            }
                        } else {
                            // TODO should this cause failure, or can it be reported somehow?
                        }
                    } // digits
                } // mode
            } // unix.
            else if (!hasUnixMode && "perm".equals(factname)) { // skip if we have the UNIX.mode
                doUnixPerms(file, valueLowerCase);
            } // process "perm"
        } // each fact
        return file;
    }
}
