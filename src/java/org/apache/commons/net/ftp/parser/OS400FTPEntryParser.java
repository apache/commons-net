/*
 * Copyright 2004 The Apache Software Foundation
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

import org.apache.commons.net.ftp.FTPFile;

import java.util.Calendar;

/**
 * @version $Id: OS400FTPEntryParser.java,v 1.5 2004/11/23 12:52:20 rwinston Exp $
 */

public class OS400FTPEntryParser extends RegexFTPFileEntryParserImpl
{
    private static final String REGEX =
        "(\\S+)\\s+"                // user
        + "(\\d+)\\s+"              // size
        + "(\\d\\d)/(\\d\\d)/(\\d\\d)\\s+" // year/month/day
        + "([0-2][0-9]):([0-5][0-9]):([0-5][0-9])\\s+" // hour:minutes:seconds
        + "(\\*\\S+)\\s+"               // *STMF/*DIR
        + "(\\S+/?)\\s*";               // filename

    public OS400FTPEntryParser()
    {
        super(REGEX);
    }

    public FTPFile parseFTPEntry(String entry)
    {

        FTPFile file = new FTPFile();
        file.setRawListing(entry);
        int type;

        if (matches(entry))
        {
            String usr = group(1);
            String filesize = group(2);
            String yr = group(3);
            String mo = group(4);
            String da = group(5);
            String hr = group(6);
            String min = group(7);
            String sec = group(8);
            String typeStr = group(9);
            String name = group(10);

            if (typeStr.equalsIgnoreCase("*STMF"))
            {
                type = FTPFile.FILE_TYPE;
            }
            else if (typeStr.equalsIgnoreCase("*DIR"))
            {
                type = FTPFile.DIRECTORY_TYPE;
            }
            else
            {
                type = FTPFile.UNKNOWN_TYPE;
            }

            file.setType(type);

            file.setUser(usr);

            try
            {
                file.setSize(Long.parseLong(filesize));
            }
            catch (NumberFormatException e)
            {
                // intentionally do nothing
            }

            Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.HOUR_OF_DAY, 0);

            try
            {
                int year = Integer.parseInt(yr, 10);
                if (year < 70)
                {
                    year += 2000;
                }
                else
                {
                    year += 1900;
                }

                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, Integer.parseInt(mo, 10)-1);
                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(da, 10));

                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hr, 10));
                cal.set(Calendar.MINUTE, Integer.parseInt(min, 10));
                cal.set(Calendar.SECOND, Integer.parseInt(sec, 10));

                file.setTimestamp(cal);
            }
            catch (NumberFormatException e)
            {
                // do nothing, date will be uninitialized
            }

            if (name.endsWith("/"))
            {
                name = name.substring(0, name.length() - 1);
            }
            int pos = name.lastIndexOf('/');
            if (pos > -1)
            {
                name = name.substring(pos + 1);
            }

            file.setName(name);

            return file;
        }
        return null;
    }
}
