/***
 * $Id: FTPFileListParser.java,v 1.1 2002/04/03 01:04:28 brekke Exp $
 *
 * NetComponents Internet Protocol Library
 * Copyright (C) 1997-2002  Daniel F. Savarese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library in the LICENSE file; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 ***/

package com.oroinc.net.ftp;

import java.io.*;

/***
 * FTPFileListParser defines the interface for parsing FTP file listings
 * and converting that information into an array of
 * <a href="com.oroinc.net.ftp.FTPFile.html"> FTPFile </a> instances.
 * Sometimes you will want to parse unusual listing formats, in which
 * case you would create your own implementation of FTPFileListParser and
 * if necessary, subclass FTPFile.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see FTPFile
 * @see FTPClient#listFiles
 ***/

public interface FTPFileListParser {

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
  public FTPFile[] parseFileList(InputStream listStream) throws IOException;

} 
