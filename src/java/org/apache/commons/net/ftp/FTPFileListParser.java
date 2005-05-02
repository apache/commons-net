/*
 * Copyright 2001-2005 The Apache Software Foundation
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
package org.apache.commons.net.ftp;
import java.io.IOException;
import java.io.InputStream;

/***
 * FTPFileListParser defines the interface for parsing FTP file listings
 * and converting that information into an array of
 * {@link org.apache.commons.net.ftp.FTPFile} instances.
 * Sometimes you will want to parse unusual listing formats, in which
 * case you would create your own implementation of FTPFileListParser and
 * if necessary, subclass FTPFile.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see FTPFile
 * @see FTPClient#listFiles
 * @deprecated This interface is deprecated as of version 1.2 and will be
 * removed in version 2.0 -- use FTPFileEntryParser instead.
 ***/

public interface FTPFileListParser
{

    /***
     * Parses an FTP server file listing and converts it into a usable format
     * in the form of an array of <code> FTPFile </code> instances.  If the
     * file list contains no files, <code> null </code> should be
     * returned, otherwise an array of <code> FTPFile </code> instances
     * representing the files in the directory is returned.
     * <p>
     * @param listStream The InputStream from which the file list should be
     *        read.
     * @param encoding The encoding to use.
     * @return The list of file information contained in the given path.  null
     *     if the list could not be obtained or if there are no files in
     *     the directory.
     * @exception IOException  If an I/O error occurs reading the listStream.
     ***/
    FTPFile[] parseFileList(InputStream listStream, String encoding) throws IOException;
    
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
    FTPFile[] parseFileList(InputStream listStream) throws IOException;

}
