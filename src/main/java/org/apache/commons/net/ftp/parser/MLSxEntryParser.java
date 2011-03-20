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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;

/**
 * Parser class for MSLT and MLSD replies.
 *
 * @since 3.0
 */
public class MLSxEntryParser extends FTPFileEntryParserImpl
{
    //    Format taken from RFC 3659:

    //    entry            = [ facts ] SP pathname
    //    facts            = 1*( fact ";" )
    //    fact             = factname "=" value
    //    factname         = "Size" / "Modify" / "Create" /
    //                       "Type" / "Unique" / "Perm" /
    //                       "Lang" / "Media-Type" / "CharSet" /
    //                       os-depend-fact / local-fact
    //    os-depend-fact   = <IANA assigned OS name> "." token
    //    local-fact       = "X." token
    //    value            = *SCHAR

    // Sample os-depend-fact:
    //    UNIX.group=0;UNIX.mode=0755;UNIX.owner=0;

    // A single control response entry (MLST) is returned with a leading space;
    // multiple (data) entries are returned without any leading spaces.

   private static final MLSxEntryParser PARSER = new MLSxEntryParser();

    private static final HashMap<String, Integer> TYPE_TO_INT = new HashMap<String, Integer>();
    static {
        TYPE_TO_INT.put("file", Integer.valueOf(FTPFile.FILE_TYPE));
        TYPE_TO_INT.put("cdir", Integer.valueOf(FTPFile.DIRECTORY_TYPE)); // listed directory
        TYPE_TO_INT.put("pdir", Integer.valueOf(FTPFile.DIRECTORY_TYPE)); // a parent dir
        TYPE_TO_INT.put("dir", Integer.valueOf(FTPFile.DIRECTORY_TYPE)); // dir or sub-dir
    }

    private static int UNIX_GROUPS[] = { // Groups in order of mode digits
        FTPFile.USER_ACCESS,
        FTPFile.GROUP_ACCESS,
        FTPFile.WORLD_ACCESS,
    };

    private static int UNIX_PERMS[][] = { // perm bits, broken down
/* 0 */  {},
/* 1 */  {FTPFile.EXECUTE_PERMISSION},
/* 2 */  {FTPFile.WRITE_PERMISSION},
/* 3 */  {FTPFile.EXECUTE_PERMISSION, FTPFile.WRITE_PERMISSION},
/* 4 */  {FTPFile.READ_PERMISSION},
/* 5 */  {FTPFile.READ_PERMISSION, FTPFile.EXECUTE_PERMISSION},
/* 6 */  {FTPFile.READ_PERMISSION, FTPFile.WRITE_PERMISSION},
/* 7 */  {FTPFile.READ_PERMISSION, FTPFile.WRITE_PERMISSION, FTPFile.EXECUTE_PERMISSION},
    };

    public MLSxEntryParser()
    {
        super();
    }

    /**
     * Override preParse() phase as it is not needed.
     */
    // TODO can be removed if NET-381 is implemented
    @Override
    public List<String> preParse(List<String> orig) {
        return orig;
    }

    public FTPFile parseFTPEntry(String entry) {
        String parts[] = entry.split(" ",2); // Path may contain space
        if (parts.length != 2) {
            return null;
        }
        FTPFile file = new FTPFile();
        file.setRawListing(entry);
        file.setName(parts[1]);
        String[] facts = parts[0].split(";");
        boolean hasUnixMode = parts[0].toLowerCase(Locale.ENGLISH).contains("unix.mode=");
        for(String fact : facts) {
            String []factparts = fact.split("=");
// Sample missing permission
// drwx------   2 mirror   mirror       4096 Mar 13  2010 subversion
// modify=20100313224553;perm=;type=dir;unique=811U282598;UNIX.group=500;UNIX.mode=0700;UNIX.owner=500; subversion
            if (factparts.length != 2) {
                continue; // nothing to do here
            }
            String factname = factparts[0].toLowerCase(Locale.ENGLISH);
            String factvalue = factparts[1];
            String valueLowerCase = factvalue.toLowerCase(Locale.ENGLISH);
            if ("size".equals(factname)) {
                file.setSize(Long.parseLong(factvalue));
            }
            else if ("sizd".equals(factname)) { // Directory size
                file.setSize(Long.parseLong(factvalue));
            }
            else if ("modify".equals(factname)) {
                // YYYYMMDDHHMMSS[.sss]
                SimpleDateFormat sdf; // Not thread-safe
                if (factvalue.contains(".")){
                    sdf = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
                } else {
                    sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                }

                GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT")); // TODO are these thread-safe?
                try {
                    gc.setTime(sdf.parse(factvalue));
                } catch (ParseException e) {
                    // TODO ??
                }
                file.setTimestamp(gc);
            }
            else if ("type".equals(factname)) {
                    Integer intType = TYPE_TO_INT.get(valueLowerCase);
                    if (intType == null) {
                        // TODO System.out.println(factvalue+ "? in "+entry);
                    } else {
                        file.setType(intType.intValue());
                    }
            }
            else if (factname.startsWith("unix.")) {
                String unixfact = factname.substring("unix.".length()).toLowerCase(Locale.ENGLISH);
                if ("group".equals(unixfact)){
                    file.setGroup(factvalue);
                } else if ("owner".equals(unixfact)){
                    file.setUser(factvalue);
                } else if ("mode".equals(unixfact)){ // e.g. 0[1]755
                    int off = factvalue.length()-4; // only parse last 3 digits
                    for(int i=1; i<=3; i++){
                        int ch = factvalue.charAt(off+i)-'0';
                        for(int p : UNIX_PERMS[ch]) {
                            file.setPermission(UNIX_GROUPS[i-1], p, true);
                        }
                    }
                    file.setUser(factvalue);
                } // mode
            } // unix.
            else if (!hasUnixMode && "perm".equals(factname)) { // skip if we have the UNIX.mode
                //              perm-fact    = "Perm" "=" *pvals
                //              pvals        = "a" / "c" / "d" / "e" / "f" /
                //                             "l" / "m" / "p" / "r" / "w"
                for(char c : valueLowerCase.toCharArray()) {
                    // TODO these are mostly just guesses at present
                    switch (c) {
                        case 'a':     // (file) may APPEnd
                            file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
                            break;
                        case 'c':     // (dir) files may be created in the dir
                            file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
                            break;
                        case 'd':     // deletable
                            file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
                            break;
                        case 'e':     // (dir) can change to this dir
                            file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, true);
                            break;
                        case 'f':     // (file) renamable
                            // ?? file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
                            break;
                        case 'l':     // (dir) can be listed
                            file.setPermission(FTPFile.USER_ACCESS, FTPFile.EXECUTE_PERMISSION, true);
                            break;
                        case 'm':     // (dir) can create directory here
                            file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
                            break;
                        case 'p':     // (dir) entries may be deleted
                            file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
                            break;
                        case 'r':     // (files) file may be RETRieved
                            file.setPermission(FTPFile.USER_ACCESS, FTPFile.READ_PERMISSION, true);
                            break;
                        case 'w':     // (files) file may be STORed
                            file.setPermission(FTPFile.USER_ACCESS, FTPFile.WRITE_PERMISSION, true);
                            break;
                        default:
                            return null; // TODO?
                    }
                }
            }
        }
        return file;
    }

    public static FTPFile parseEntry(String entry) {
        return PARSER.parseFTPEntry(entry);
    }

    public static  MLSxEntryParser getInstance() {
        return PARSER;
    }
}
