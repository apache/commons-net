package org.apache.commons.net.ftp.ftp2.parser;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.Calendar;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.ftp2.FTPFileEntryParser;

/**
 * This class is based on the logic of Daniel Savarese's
 * DefaultFTPListParser, but adapted to use regular expressions and to fit the
 * new FTPFileEntryParser interface.
 * @author <a href="mailto:scohen@ignitesports.com">Steve Cohen</a>
 * @version $Id: UnixFTPEntryParser.java,v 1.1 2002/04/29 03:55:32 brekke Exp $
 */
public class UnixFTPEntryParser
            extends MatchApparatus implements FTPFileEntryParser
{
    private static final String months =
        "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";
    private static final String regEx =
        "([bcdlf])"
        + "(((r|-)(w|-)(x|-))((r|-)(w|-)(x|-))((r|-)(w|-)(x|-)))\\s*"
        + "(\\d*)\\s*"
        + "(\\S*)\\s*"
        + "(\\S*)\\s*"
        + "(\\d*)\\s*"
        + months + "\\s*"
        + "((?:[012]\\d*)|(?:3[01]))\\s*"
        + "((\\d\\d\\d\\d)|((?:[01]\\d)|(?:2[0123])):([012345]\\d))\\s"
        + "(\\S*)(\\s*.*)";
    public String getRegEx()
    {
        return this.regEx;
    }

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
            default:
                type = FTPFile.FILE_TYPE;
            }
            int g = 4;
            for (int access = 0; access < 3; access++, g += 4)
            {
                // Use != '-' to avoid having to check for suid and sticky bits
                file.setPermission(access, FTPFile.READ_PERMISSION,
                                   (!group(g).equals("-")));
                file.setPermission(access, FTPFile.WRITE_PERMISSION,
                                   (!group(g + 1).equals("-")));
                file.setPermission(access, FTPFile.EXECUTE_PERMISSION,
                                   (!group(g + 2).equals("-")));
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
                int pos = months.indexOf(mo);
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
