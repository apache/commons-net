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
 *    nor may "Apache" appear in their name, without
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
 * This Class uses the FTPEntryParser class to validate the input string.
 * It also requires the NetComponents library version 1.3.7 or later
 * and the OROMatcher library for the regualar expressions stuff.
 *
 *
 * <P><B>USAGE:</B></P>
 * <LI>Create an instance of VMSFTPEntryParser</LI>
 *   <dd>VMSFTPEntryParser parser = new VMSFTPEntryParser(boolean);
 *  <dd><code>True</code>  = returns all versions of a file with the respective 
 * ;#
 *  <dd><code>False</code> = only the last version will return <B>(Default)</B>
 * <LI>Create an instance of FTPClient</LI>
 *   <dd>FTPClient FTPClientObj = new FTPClient();
 * <LI>Connect to the NODE </LI>
 *   <dd>FTPClientObj.connect();
 * <LI>Login to the NODE </LI>
 *   <dd>FTPClientObj.login(username,password);
 * <LI>Switch directories if you have to</LI>
 *   <dd>FTPClientObj.changeWorkingDirectory(thePath);
 * <LI>You might want to check if you are truly in a VMS System</LI>
 *   <dd>And how do I do that you ask? easy...  VMS is such a wonderful OS 
 * that when we do   <dd><B>String am_I_VMS =  FTPClientObj.getSystemName()</B>
 *   <dd>it returns NULL, while everyone else returns the FTP servername
 * <LI>Call listFiles passing the newly created parser and a filename or a mask
 *  to look for </LI> <dd>FTPClientObj.listFiles(parser,filename);
 * <LI>You'll get back the list as an array of FTPFile objects like this
 *   <dd>FTPFile[] myVMSFiles = FTPClientObj.listFiles(parser,filename);  (or)
 *    <dd>FTPFile[] myVMSFiles = FTPClientObj.listFiles(parser);
 *    <dd>If <code>filename</code> is a filename and versioning is OFF, the 
 * version <dd>you requested will come back without the ;#
 * <P>
 * That's all there is to it.
 * <P>
 * Each FTPFile object is populated just like any other FTPFile
 * object. The only thing not implemented at this time is the file
 * permissions, but I can do it if there is a real need for it.
 * <P>
 * !NOTE/WARNING!:Before you pass the parser to listFiles, make sure you are
 * in the directory that you need to be. This parser will return the filtered
 * files from the directory it is in. This becomes crucial specialy if your
 * goal is to delete the output of the parser.
 * <P>
 * @author  <a href="Winston.Ojeda@qg.com">Winston Ojeda</a>
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id: VMSFTPEntryParser.java,v 1.8 2003/03/02 19:36:43 scohen Exp $
 */
public class VMSFTPEntryParser
            extends MatchApparatus implements FTPFileEntryParser
{
    private String prefix = "[" + getClass().getName() + "] ";
    private boolean versioning;

    /* This is how a VMS LIST output really looks
    
          "1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
          "1-JUN.LIS;2              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
          "DATA.DIR;1               1/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
    */
    private static final String MONTHS =
        "(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)";

    private static final String REGEX =
        "(.*;[0-9]+)\\s*" +
        "(\\d+)/\\d+\\s*" +
        "(\\d{1,2})-" +
        MONTHS +
        "-([0-9]{4})\\s*" +
        "((?:[01]\\d)|(?:2[0-3])):([012345]\\d):([012345]\\d)\\s*" +
        "\\[([0-9$A-Za-z_]+),([0-9$a-zA-Z_]+)\\]\\s*" +
        "(\\([a-zA-Z]*,[a-zA-Z]*,[a-zA-Z]*,[a-zA-Z]*\\))";


    /**
     * Convenience Constructor for a VMSFTPEntryParser object.  Sets the 
     * <code>versioning</code> member false
     * 
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen 
     * under normal conditions.  It it is seen, this is a sign that 
     * <code>REGEX</code> is  not a valid regular expression.
     */
    public VMSFTPEntryParser()
    {
        this(false);
    }
    
    /**
     * Constructor for a VMSFTPEntryParser object.  Sets the versioning member 
     * to the supplied value.
     *  
     * @param versioning Value to which versioning is to be set.
     * 
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen 
     * under normal conditions.  It it is seen, this is a sign that 
     * <code>REGEX</code> is  not a valid regular expression.
     */
    public VMSFTPEntryParser(boolean versioning)
    {
        super(REGEX);
        this.versioning = versioning;
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
        Integer oneBlock = new Integer(512);
        int intBlock = oneBlock.intValue();
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
            String grp = group(9);
            String owner = group(10);

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
            if (versioning) 
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
            Long theSize = new Long(size);
            long sizeInBytes = theSize.longValue() * longBlock;
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
            f.setTimestamp(cal);

            f.setGroup(grp);
            f.setUser(owner);
            //set group and owner
            //Since I don't need the persmissions on this file (RWED), I'll 
            //leave that for further development. 'Cause it will be a bit 
            //elaborate to do it right with VMSes World, Global and so forth.
            return f;
        }
        return null;
    }
}
