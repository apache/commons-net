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
package org.apache.commons.net.ftp.ftp2.parser;

import java.util.Calendar;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.ftp2.FTPFileEntryParser;

/**
 * This class is based on UnixFTPEntryParser with some small changes, namely
 * the day and month fields being the transposed.
 * 
 * @author <a href="mailto:bretts@bml.uk.com">Brett Smith</a>
 * @version $Id$
 */
public class AIXFTPEntryParser
  extends MatchApparatus implements FTPFileEntryParser
{
  private static final String MONTHS =
    "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";
  private static final String REGEX =
    "([bcdlf-])"
    + "(((r|-)(w|-)(x|-))((r|-)(w|-)(x|-))((r|-)(w|-)(x|-)))\\s+" // permissions
    + "(\\d+)\\s+" // ln
    + "(\\S+)\\s+" // user
    + "(\\S+)\\s+" // group
    + "(\\d+)\\s+" // size
    + "((?:[0-9])|(?:[0-2][0-9])|(?:3[0-1]))\\s+" // day
    + MONTHS + "\\s+" // month
    + "((\\d\\d\\d\\d)|((?:[01]\\d)|(?:2[0123])):([012345]\\d))\\s" // year
    + "(\\S+)(\\s*.*)"; // file

    
  /**
   * The sole constructor for a AIXFTPEntryParser object.
   * 
   * @exception IllegalArgumentException
   * Thrown if the regular expression is unparseable.  Should not be seen 
   * under normal conditions.  It it is seen, this is a sign that 
   * <code>REGEX</code> is  not a valid regular expression.
   */
  public AIXFTPEntryParser() 
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
        String da = group(19);
        String mo = group(20);
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

        file.setType(type);
           
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
