package org.apache.commons.net.ftp;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Vector;

/***
 * DefaultFTPFileListParser is the default implementation of
 * <a href="org.apache.commons.net.ftp.FTPFileListParser.html"> FTPFileListParser </a>
 * used by <a href="org.apache.commons.net.ftp.FTPClient.html"> FTPClient </a>
 * to parse file listings.
 * Sometimes you will want to parse unusual listing formats, in which
 * case you would create your own implementation of FTPFileListParser and
 * if necessary, subclass FTPFile.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see FTPFileListParser
 * @see FTPFile
 * @see FTPClient#listFiles
 ***/

public final class DefaultFTPFileListParser implements FTPFileListParser
{


    // end is one beyond end
    private int __charArrayToInt(char[] arr, int start, int end)
    {
        int value = 0, decimal;
        decimal = 1;
        while (end-- > start)
        {
            value += (decimal * (arr[end] - '0'));
            decimal *= 10;
        }
        return value;
    }

    private long __charArrayToLong(char[] arr, int start, int end)
    {
        long value = 0, decimal;
        decimal = 1;
        while (end-- > start)
        {
            value += (decimal * (arr[end] - '0'));
            decimal *= 10;
        }
        return value;
    }

    private int __skipWhitespace(char[] cToken, int start)
    {
        while (start < cToken.length && Character.isWhitespace(cToken[start]))
            ++start;
        return start;
    }

    private int __skipDigits(char[] cToken, int start)
    {
        while (start < cToken.length && Character.isDigit(cToken[start]))
            ++start;
        return start;
    }

    private int __skipNonWhitespace(char[] cToken, int start)
    {
        while (start < cToken.length && !Character.isWhitespace(cToken[start]))
            ++start;
        return start;
    }

    private int __skipNonWhitespaceToLower(char[] cToken, int start)
    {
        while (start < cToken.length && !Character.isWhitespace(cToken[start]))
        {
            cToken[start] = Character.toLowerCase(cToken[start]);
            ++start;
        }
        return start;
    }


    /***
     * Parses an FTP server listing entry (a single line) and returns an
     * FTPFile instance with the resulting information.  If the entry could
     * not be parsed, returns null.
     * <p>
     * @param entry  A single line of an FTP server listing with the
     *                 end of line truncated.
     * @return An FTPFile instance representing the file information.  If
     *         the entry could not be parsed, returns null.
     ***/
    public FTPFile parseFTPEntry(String entry)
    {
        int access, start, end, type, month, year, hour, minutes;
        boolean isDevice;
        Calendar date;
        String sToken;
        char cToken[];
        FTPFile file;

        try
        {
            cToken = entry.toCharArray();

            file = new FTPFile();
            file.setRawListing(entry);

            isDevice = (cToken[0] == 'b' || cToken[0] == 'c');

            switch (cToken[0])
            {
            case 'd':
                type = FTPFile.DIRECTORY_TYPE;
                break;
            case 'l':
                type = FTPFile.SYMBOLIC_LINK_TYPE;
                break;
            default:
                type = FTPFile.FILE_TYPE;
                break;
            }

            file.setType(type);

            for (access = 0, start = 1; access < 3; access++)
            {
                // We use != '-' so we avoid having to check for suid and sticky bits
                file.setPermission(access, FTPFile.READ_PERMISSION,
                                   (cToken[start++] != '-'));
                file.setPermission(access, FTPFile.WRITE_PERMISSION,
                                   (cToken[start++] != '-'));
                file.setPermission(access, FTPFile.EXECUTE_PERMISSION,
                                   (cToken[start++] != '-'));
            }

            start = __skipWhitespace(cToken, start);
            end = __skipDigits(cToken, start);
            file.setHardLinkCount(__charArrayToInt(cToken, start, end));

            start = __skipWhitespace(cToken, end);
            end = __skipNonWhitespace(cToken, start);
            // Get user and group
            file.setUser(new String(cToken, start, end - start));

            start = __skipWhitespace(cToken, end);
            end = __skipNonWhitespace(cToken, start);
            file.setGroup(new String(cToken, start, end - start));

            // Get size, if block or character device, set size to zero and skip
            // next two tokens.
            if (isDevice)
            {
                start = __skipWhitespace(cToken, end);
                end = __skipNonWhitespace(cToken, start);
                start = __skipWhitespace(cToken, end);
                end = __skipNonWhitespace(cToken, start);
                // Don't explicitly set size because it is zero by default
            }
            else
            {
                start = __skipWhitespace(cToken, end);
                end = __skipDigits(cToken, start);
                file.setSize(__charArrayToLong(cToken, start, end));
            }

            start = __skipWhitespace(cToken, end);
            end = __skipNonWhitespaceToLower(cToken, start);

            // Get month
            switch (cToken[start])
            {
            case 'a':
                if (cToken[end - 1] == 'r')
                    month = 3;
                else
                    month = 7;
                break;
            case 'd':
                month = 11;
                break;
            case 'f':
                month = 1;
                break;
            case 'j':
                if (cToken[end - 1] == 'l')
                    month = 6;
                else if (cToken[start + 1] == 'a')
                    month = 0;
                else
                    month = 5;
                break;
            case 'm':
                if (cToken[end - 1] == 'y')
                    month = 4;
                else
                    month = 2;
                break;
            case 'n':
                month = 10;
                break;
            case 'o':
                month = 9;
                break;
            case 's':
                month = 8;
                break;
            default:
                month = 0;
                break;
            }

            // Get day, and store in access
            start = __skipWhitespace(cToken, end);
            end = __skipDigits(cToken, start);
            access = __charArrayToInt(cToken, start, end);

            start = __skipWhitespace(cToken, end);
            end = __skipDigits(cToken, start);

            date = Calendar.getInstance();

            try
            {
                // If token contains a :, it must be a time, otherwise a year
                if (cToken[end] == ':')
                {
                    year = date.get(Calendar.YEAR);
                    hour = date.get(Calendar.MONTH);
                    if (hour < month)
                        --year;
                    hour = __charArrayToInt(cToken, start, end);
                    start = end + 1;
                    end = __skipDigits(cToken, start);
                    minutes = __charArrayToInt(cToken, start, end);
                }
                else
                {
                    // Have to set minutes or compiler will complain not initialized
                    hour = minutes = -1;
                    year = __charArrayToInt(cToken, start, end);
                }

                date.clear();
                date.set(Calendar.YEAR, year);
                date.set(Calendar.MONTH, month);
                date.set(Calendar.DATE, access);

                if (hour != -1)
                {
                    date.set(Calendar.HOUR, hour);
                    date.set(Calendar.MINUTE, minutes);
                }

            }
            catch (IllegalArgumentException e)
            {
                // Do nothing
            }

            file.setTimestamp(date);

            // This is dangerous, but we're going to assume there is only one
            // space after the date.  Most servers seem to use that format and
            // we need to be able to preserve leading spacesin filenames.
            //start = __skipWhitespace(cToken, end);
            start = end + 1;
            end = __skipNonWhitespace(cToken, start);

            if (end >= cToken.length)
            {
                file.setName(new String(cToken, start, end - start));
                return file;
            }

            // Now we have to deal with the possibilities of symbolic links and
            // filenames with spaces.  The filename and/or link may contain
            // spaces, numbers, or appear like the date entry, group, etc.,

            sToken = new String(cToken, start, cToken.length - start);

            if (type == FTPFile.SYMBOLIC_LINK_TYPE)
            {
                end = sToken.indexOf(" -> ");
                // Give up if no link indicator is present
                if (end == -1)
                {
                    file.setName(sToken);
                    return file;
                }

                file.setName(sToken.substring(0, end));
                file.setLink(sToken.substring(end + 4));
                return file;
            }

            // For other cases, just take the entire token

            file.setName(sToken);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return null;
        }
        catch (StringIndexOutOfBoundsException e)
        {
            return null;
        }

        return file;
    }


    /***
     * Parses an FTP server file listing and converts it into a usable format
     * in the form of an array of <code> FTPFile </code> instances.  If the
     * file list contains no files, <code> null </code> is returned, otherwise
     * an array of <code> FTPFile </code> instances representing the files in 
     * the directory is returned.
     * <p>
     * @param listStream The InputStream from which the file list should be
     *        read.
     * @return The list of file information contained in the given path.  null
     *     if the list could not be obtained or if there are no files in
     *     the directory.
     * @exception IOException  If an I/O error occurs reading the listStream.
     ***/
    public FTPFile[] parseFileList(InputStream listStream) throws IOException
    {
        String line;
        Vector results;
        BufferedReader reader;
        FTPFile entry;

        reader = new BufferedReader(new InputStreamReader(listStream));

        if ((line = reader.readLine()) == null)
        {
            results = null;
        }
        else
        {
            results = new Vector();

            // This is to handle a line at the beginning of the listing
            // that says "total xx" or "Gemstat xx" or something else.
            if (line.toLowerCase().startsWith("total"))
                line = reader.readLine();
            else
            {
                if ((entry = parseFTPEntry(line)) != null)
                    results.addElement(entry);
                line = reader.readLine();
            }

            while (line != null)
            {
                if (line.length() == 0 || (entry = parseFTPEntry(line)) == null)
                {
                    results = null;
                    break;
                }
                results.addElement(entry);
                line = reader.readLine();
            }
        }

        // Finish reading from stream just in case
        if (line != null)
            while ((line = reader.readLine()) != null)
                ;

        reader.close();

        if (results != null)
        {
            FTPFile[] result;

            result = new FTPFile[results.size()];
            if (result.length > 0)
                results.copyInto(result);
            return result;
        }

        return null;
    }

}
