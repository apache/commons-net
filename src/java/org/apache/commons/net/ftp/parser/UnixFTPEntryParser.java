/*
 * Copyright 2001-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.ftp.parser;
import java.util.Calendar;
import org.apache.commons.net.ftp.FTPFile;

/**
 * Implementation FTPFileEntryParser and FTPFileListParser for standard
 * Unix Systems.
 *
 * This class is based on the logic of Daniel Savarese's
 * DefaultFTPListParser, but adapted to use regular expressions and to fit the
 * new FTPFileEntryParser interface.
 * @author <a href="mailto:scohen@ignitesports.com">Steve Cohen</a>
 * @version $Id: UnixFTPEntryParser.java,v 1.17 2004/06/22 02:30:33 scohen Exp $
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 */
public class UnixFTPEntryParser extends RegexFTPFileEntryParserImpl
{
    /**
     * months abbreviations looked for by this parser.  Also used
     * to determine which month is matched by the parser
     */
    private static final String MONTHS =
        "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";

    /**
     * this is the regular expression used by this parser.
     *
     * Permissions:
     *    r   the file is readable
     *    w   the file is writable
     *    x   the file is executable
     *    -   the indicated permission is not granted
     *    L   mandatory locking occurs during access (the set-group-ID bit is
     *        on and the group execution bit is off)
     *    s   the set-user-ID or set-group-ID bit is on, and the corresponding
     *        user or group execution bit is also on
     *    S   undefined bit-state (the set-user-ID bit is on and the user
     *        execution bit is off)
     *    t   the 1000 (octal) bit, or sticky bit, is on [see chmod(1)], and
     *        execution is on
     *    T   the 1000 bit is turned on, and execution is off (undefined bit-
     *        state)
     */
    private static final String REGEX =
        "([bcdlfmpSs-])"
        + "(((r|-)(w|-)([xsStTL-]))((r|-)(w|-)([xsStTL-]))((r|-)(w|-)([xsStTL-])))\\s+"
        + "(\\d+)\\s+"
        + "(\\S+)\\s+"
        + "(?:(\\S+)\\s+)?"
        + "(\\d+)\\s+"
        + MONTHS + "\\s+"
        + "((?:[0-9])|(?:[0-2][0-9])|(?:3[0-1]))\\s+"
        + "((\\d\\d\\d\\d)|((?:[01]\\d)|(?:2[0123])|(?:[1-9])):([012345]\\d))\\s+"
        + "(\\S+)(\\s*.*)";


    /**
     * The sole constructor for a UnixFTPEntryParser object.
     *
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen
     * under normal conditions.  It it is seen, this is a sign that
     * <code>REGEX</code> is  not a valid regular expression.
     */
    public UnixFTPEntryParser()
    {
        super(REGEX);
    }

    /**
     * Parses a line of a unix (standard) FTP server file listing and converts
     * it into a usable format in the form of an <code> FTPFile </code>
     * instance.  If the file listing line doesn't describe a file,
     * <code> null </code> is returned, otherwise a <code> FTPFile </code>
     * instance representing the files in the directory is returned.
     * <p>
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    public FTPFile parseFTPEntry(String entry)
    {

        FTPFile file = new FTPFile();
        file.setRawListing(entry);
        int type;
        boolean isDevice = false;

        if (matches(entry))
        {
            String typeStr = group(1);
            String hardLinkCount = group(15);
            String usr = group(16);
            String grp = group(17);
            String filesize = group(18);
            String mo = group(19);
            String da = group(20);
            String yr = group(22);
            String hr = group(23);
            String min = group(24);
            String name = group(25);
            String endtoken = group(26);

            // bcdlfmpSs-
            switch (typeStr.charAt(0))
            {
            case 'd':
                type = FTPFile.DIRECTORY_TYPE;
                break;
            case 'l':
                type = FTPFile.SYMBOLIC_LINK_TYPE;
                break;
            case 'b':
            case 'c':
                isDevice = true;
                // break; - fall through
            case 'f':
            case '-':
            	type = FTPFile.FILE_TYPE;
            	break;
            default:
                type = FTPFile.UNKNOWN_TYPE;
            }

            file.setType(type);

            int g = 4;
            for (int access = 0; access < 3; access++, g += 4)
            {
                // Use != '-' to avoid having to check for suid and sticky bits
                file.setPermission(access, FTPFile.READ_PERMISSION,
                                   (!group(g).equals("-")));
                file.setPermission(access, FTPFile.WRITE_PERMISSION,
                                   (!group(g + 1).equals("-")));

                String execPerm = group(g + 2);
                if (!execPerm.equals("-") && !Character.isUpperCase(execPerm.charAt(0)))
                {
                    file.setPermission(access, FTPFile.EXECUTE_PERMISSION, true);
                }
                else
                {
                    file.setPermission(access, FTPFile.EXECUTE_PERMISSION, false);
                }
            }

            if (!isDevice)
            {
                try
                {
                    file.setHardLinkCount(Integer.parseInt(hardLinkCount));
                }
                catch (NumberFormatException e)
                {
                    // intentionally do nothing
                }
            }

            file.setUser(usr);
            file.setGroup(grp);

            try
            {
                file.setSize(Integer.parseInt(filesize));
            }
            catch (NumberFormatException e)
            {
                // intentionally do nothing
            }

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);

            try
            {
                int pos = MONTHS.indexOf(mo);
                int month = pos / 4;

                if (null != yr)
                {
                    // it's a year
                    cal.set(Calendar.YEAR, Integer.parseInt(yr));
                }
                else
                {
                    // it must be  hour/minute or we wouldn't have matched
                    int year = cal.get(Calendar.YEAR);
                    // if the month we're reading is greater than now, it must
                    // be last year
                    if (cal.get(Calendar.MONTH) < month)
                    {
                        year--;
                    }
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hr));
                    cal.set(Calendar.MINUTE, Integer.parseInt(min));
                }
                cal.set(Calendar.MONTH, month);

                cal.set(Calendar.DATE, Integer.parseInt(da));
                file.setTimestamp(cal);
            }
            catch (NumberFormatException e)
            {
                // do nothing, date will be uninitialized
            }
            if (null == endtoken)
            {
                file.setName(name);
            }
            else
            {
                // oddball cases like symbolic links, file names
                // with spaces in them.
                name += endtoken;
                if (type == FTPFile.SYMBOLIC_LINK_TYPE)
                {

                    int end = name.indexOf(" -> ");
                    // Give up if no link indicator is present
                    if (end == -1)
                    {
                        file.setName(name);
                    }
                    else
                    {
                        file.setName(name.substring(0, end));
                        file.setLink(name.substring(end + 4));
                    }

                }
                else
                {
                    file.setName(name);
                }
            }
            return file;
        }
        return null;
    }
}
