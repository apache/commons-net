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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.StringTokenizer;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;

/**
 * Implementation FTPFileEntryParser and FTPFileListParser for VMS Systems.
 * This is a sample of VMS LIST output
 *
 *  "1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
 *  "1-JUN.LIS;2              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
 *  "DATA.DIR;1               1/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
 * <P><B>
 * Note: VMSFTPEntryParser can only be instantiated through the
 * DefaultFTPParserFactory by classname.  It will not be chosen
 * by the autodetection scheme.
 * </B>
 * <P>
 *
 * @author  <a href="Winston.Ojeda@qg.com">Winston Ojeda</a>
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @author <a href="sestegra@free.fr">Stephane ESTE-GRACIAS</a>
 * @version $Id: VMSFTPEntryParser.java,v 1.25 2004/11/23 12:52:20 rwinston Exp $
 *
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
 */
public class VMSFTPEntryParser extends RegexFTPFileEntryParserImpl
{


    /**
     * months abbreviations looked for by this parser.  Also used
     * to determine <b>which</b> month has been matched by the parser.
     */
    private static final String MONTHS =
        "(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)";

    /**
     * this is the regular expression used by this parser.
     */
    private static final String REGEX =
        "(.*;[0-9]+)\\s*"
        + "(\\d+)/\\d+\\s*"
        + "(\\d{1,2})-"
        + MONTHS
        + "-([0-9]{4})\\s*"
        + "((?:[01]\\d)|(?:2[0-3])):([012345]\\d):([012345]\\d)\\s*"
        + "\\[(([0-9$A-Za-z_]+)|([0-9$A-Za-z_]+),([0-9$a-zA-Z_]+))\\]?\\s*"
        + "\\([a-zA-Z]*,[a-zA-Z]*,[a-zA-Z]*,[a-zA-Z]*\\)";



    /**
     * Constructor for a VMSFTPEntryParser object.
     *
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen
     * under normal conditions.  It it is seen, this is a sign that
     * <code>REGEX</code> is  not a valid regular expression.
     */
    public VMSFTPEntryParser()
    {
        super(REGEX);
    }


    /***
     * Parses an FTP server file listing and converts it into a usable format
     * in the form of an array of <code> FTPFile </code> instances.  If the
     * file list contains no files, <code> null </code> should be
     * returned, otherwise an array of <code> FTPFile </code> instances
     * representing the files in the directory is returned.
     * <p>
     * @param listStream The InputStream from which the file list should be
     *        read.
     * @return The list of file information contained in the given path.  null
     *     if the list could not be obtained or if there are no files in
     *     the directory.
     * @exception IOException  If an I/O error occurs reading the listStream.
     ***/
    public FTPFile[] parseFileList(InputStream listStream) throws IOException {
        FTPListParseEngine engine = new FTPListParseEngine(this);
        engine.readServerList(listStream);
        return engine.getFiles();
    }



    /**
     * Parses a line of a VMS FTP server file listing and converts it into a
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
        //one block in VMS equals 512 bytes
        long longBlock = 512;

        if (matches(entry))
        {
            FTPFile f = new FTPFile();
            f.setRawListing(entry);
            String name = group(1);
            String size = group(2);
            String day = group(3);
            String mo = group(4);
            String yr = group(5);
            String hr = group(6);
            String min = group(7);
            String sec = group(8);
            String owner = group(9);
            String grp;
            String user;
            StringTokenizer t = new StringTokenizer(owner, ",");
            switch (t.countTokens()) {
                case 1:
                    grp  = null;
                    user = t.nextToken();
                    break;
                case 2:
                    grp  = t.nextToken();
                    user = t.nextToken();
                    break;
                default:
                    grp  = null;
                    user = null;
            }

            if (name.lastIndexOf(".DIR") != -1)
            {
                f.setType(FTPFile.DIRECTORY_TYPE);
            }
            else
            {
                f.setType(FTPFile.FILE_TYPE);
            }
            //set FTPFile name
            //Check also for versions to be returned or not
            if (isVersioning())
            {
                f.setName(name);
            }
            else
            {
                name = name.substring(0, name.lastIndexOf(";"));
                f.setName(name);
            }
            //size is retreived in blocks and needs to be put in bytes
            //for us humans and added to the FTPFile array
            long sizeInBytes = Long.parseLong(size) * longBlock;
            f.setSize(sizeInBytes);

            //set the date
            Calendar cal = Calendar.getInstance();

            cal.clear();

            cal.set(Calendar.DATE, new Integer(day).intValue());
            cal.set(Calendar.MONTH, MONTHS.indexOf(mo) / 4);
            cal.set(Calendar.YEAR, new Integer(yr).intValue());
            cal.set(Calendar.HOUR_OF_DAY, new Integer(hr).intValue());
            cal.set(Calendar.MINUTE, new Integer(min).intValue());
            cal.set(Calendar.SECOND, new Integer(sec).intValue());
	    cal.set(Calendar.MILLISECOND, 0);
            f.setTimestamp(cal);

            f.setGroup(grp);
            f.setUser(user);
            //set group and owner
            //Since I don't need the persmissions on this file (RWED), I'll
            //leave that for further development. 'Cause it will be a bit
            //elaborate to do it right with VMSes World, Global and so forth.
            return f;
        }
        return null;
    }


    /**
     * Reads the next entry using the supplied BufferedReader object up to
     * whatever delemits one entry from the next.   This parser cannot use
     * the default implementation of simply calling BufferedReader.readLine(),
     * because one entry may span multiple lines.
     *
     * @param reader The BufferedReader object from which entries are to be
     * read.
     *
     * @return A string representing the next ftp entry or null if none found.
     * @exception IOException thrown on any IO Error reading from the reader.
     */
    public String readNextEntry(BufferedReader reader) throws IOException
    {
        String line = reader.readLine();
        StringBuffer entry = new StringBuffer();
        while (line != null)
        {
            if (line.startsWith("Directory") || line.startsWith("Total")) {
                line = reader.readLine();
                continue;
            }

            entry.append(line);
            if (line.trim().endsWith(")"))
            {
                break;
            }
            line = reader.readLine();
        }
        return (entry.length() == 0 ? null : entry.toString());
    }

    protected boolean isVersioning() {
        return false;
    }

}

/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
