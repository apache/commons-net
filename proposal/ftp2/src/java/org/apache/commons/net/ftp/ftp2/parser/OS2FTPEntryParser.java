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
 * This Class uses the ListParser class to validate the input string.
 * It also requires the NetComponents library version 1.3.7 or later
 * and the OROMatcher library for the regualar expressions stuff.
 *
 *
 * <P><B>USAGE:</B></P>
 * <LI>Create an instance of OS2FTPEntryParser</LI>
 *   <dd>OS2FTPEntryParser parser = new OS2FTPEntryParser();
 * <LI>Create an instance of FTPClient</LI>
 *   <dd>FTPClient FTPClientObj = new FTPClient();
 * <LI>Connect to the NODE </LI>
 *   <dd>FTPClientObj.connect();
 * <LI>Login to the NODE </LI>
 *   <dd>FTPClientObj.login(username,password);
 * <LI>Switch directories if you have to</LI>
 *   <dd>FTPClientObj.changeWorkingDirectory(thePath);
 * <LI>You might want to check if you are truly in a OS2 System</LI>
 *   <dd><B>String am_I_OS2 =  FTPClientObj.getSystemName()</B>
 *    <dd>parse am_I_OS2 to find out
 * <LI>Call listFiles passing the newly created parser and a filename or a mask 
 * to look for </LI>
 *   <dd>FTPClientObj.listFiles(parser,filename);
 * <LI>You'll get back the list as an array of FTPFiles like this
 *   <dd>FTPFile[] myOS2Files = FTPClientObj.listFiles(parser,filename);  (or)
 *    <dd>FTPFile[] myOS2Files = FTPClientObj.listFiles(parser);
 * <P>
 * That's all there is to it.
 * <P>
 * Each FTPFile object is populated just like any other FTPFile
 * object. The only thing not implemented at this time is the file
 * permissions, but I can do it if there is a real need for it.
 * <P>
 * !NOTE/WARNING!:Before you pass the parser to listFiles, make sure you are 
 * in the directory that you need to be. This parser will return the filtered
 * files from the directory it is in. This becomes specially crucial if your
 * goal is to delete the output of the parser.
 * <P>
 * @author  <a href="Winston.Ojeda@qg.com">Winston Ojeda</a>
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id: OS2FTPEntryParser.java,v 1.11 2004/06/29 04:54:29 dfs Exp $
 * @see org.apache.commons.net.ftp.FTPFileListParser
 */
public class OS2FTPEntryParser
            extends MatchApparatus implements FTPFileEntryParser

{
    private static final String REGEX =
        "(\\s+|[0-9]+)\\s*" +
        "(\\s+|[A-Z]+)\\s*" +
        "(DIR|\\s+)\\s*" +
        "((?:0[1-9])|(?:1[0-2]))-" +
        "((?:0[1-9])|(?:[1-2]\\d)|(?:3[0-1]))-" +
        "(\\d\\d)\\s*" +
        "(?:([0-1]\\d)|(?:2[0-3])):" +
        "([0-5]\\d)\\s*" +
        "(\\S.*)";
    
    /**
     * The sole constructor for a OS2FTPEntryParser object.
     * 
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen 
     * under normal conditions.  It it is seen, this is a sign that 
     * <code>REGEX</code> is  not a valid regular expression.
     */
    public OS2FTPEntryParser() 
    {
        super(REGEX);
    }


    /**
     * Parses a line of an OS2 FTP server file listing and converts it into a
     * usable format in the form of an <code> FTPFile </code> instance.  If the
     * file listing line doesn't describe a file, <code> null </code> is
     * returned, otherwise a <code> FTPFile </code> instance representing the
     * files in the directory is returned.
     * <p>
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    public FTPFile parseFTPEntry(String entry)
    {

        FTPFile f = new FTPFile();
        if (matches(entry))
        {
            String size = group(1);
            String attrib = group(2);
            String dirString = group(3);
            String mo = group(4);
            String da = group(5);
            String yr = group(6);
            String hr = group(7);
            String min = group(8);
            String name = group(9);

            //is it a DIR or a file
            if (dirString.trim().equals("DIR") || attrib.trim().equals("DIR"))
            {
                f.setType(FTPFile.DIRECTORY_TYPE);
            }
            else
            {
                f.setType(FTPFile.FILE_TYPE);
            }

            Calendar cal = Calendar.getInstance();


            //convert all the calendar stuff to ints
            int month = new Integer(mo).intValue() - 1;
            int day = new Integer(da).intValue();
            int year = new Integer(yr).intValue() + 2000;
            int hour = new Integer(hr).intValue();
            int minutes = new Integer(min).intValue();

            // Y2K stuff? this will break again in 2080 but I will
            // be sooooo dead anyways who cares.
            // SMC - IS OS2's directory date REALLY still not Y2K-compliant?
            if (year > 2080) 
            {
                year -= 100;
            }

            //set the calendar
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MINUTE, minutes);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DATE, day);
            cal.set(Calendar.MONTH, month);
            f.setTimestamp(cal);

            //set the name
            f.setName(name.trim());

            //set the size
            Long theSize = new Long(size.trim());
            theSize = new Long(String.valueOf(theSize.intValue()));
            f.setSize(theSize.longValue());

            return (f);
        }
        return null;

    }
}
