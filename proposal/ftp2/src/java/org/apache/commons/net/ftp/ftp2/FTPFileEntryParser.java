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
package org.apache.commons.net.ftp.ftp2;
import org.apache.commons.net.ftp.FTPFile;

/**
 * FTPFileEntryParser defines the interface for parsing a single FTP file
 * listing and converting that information into an 
 * <a href="org.apache.commons.net.ftp.FTPFile.html"> FTPFile </a> instance.
 * Sometimes you will want to parse unusual listing formats, in which
 * case you would create your own implementation of FTPFileEntryParser and
 * if necessary, subclass FTPFile.
 *
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id: FTPFileEntryParser.java,v 1.8 2004/02/29 10:23:18 scolebourne Exp $
 * @see org.apache.commons.net.ftp.FTPFile
 * @see FTPClient2#listFiles
 */
public interface FTPFileEntryParser
{
    /**
     * Parses a line of an FTP server file listing and converts it into a usable
     * format in the form of an <code> FTPFile </code> instance.  If the
     * file listing line doesn't describe a file, <code> null </code> should be
     * returned, otherwise a <code> FTPFile </code> instance representing the
     * files in the directory is returned.
     * <p>
     * @param listEntry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    public FTPFile parseFTPEntry(String listEntry);
}
