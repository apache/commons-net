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
import java.io.IOException;
import java.net.Socket;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPCommand;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.ftp2.parser.UnixFTPEntryParser;

/**
 * FTPClient2.java
 * This class is derived by design from Daniel Savarese's FTPClient.
 * It is designed to have all the same functionality, but add support 
 * for a new alternative design for File List Parsing.  There is no reason 
 * why this  could not have been included in FTPClient, but it is a fairly 
 * sizable chunk  of code and given that the community process for this 
 * project is still  a work in progress I thought it would be better to keep a
 * fairly  clear line between the old and the new; it's less confusing that 
 * way.
 *
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id: FTPClient2.java,v 1.8 2004/02/29 10:23:18 scolebourne Exp $
 */
public class FTPClient2 extends FTPClient
{
    private FTPFileEntryParser defaultParser;
    /**
     * The only constructor for this class.
     */
    public FTPClient2()
    {
        super();
        this.defaultParser = new UnixFTPEntryParser();
    }
    
    /**
     * Using a programmer specified <code> FTPFileEntryParser </code>, obtain a 
     * list of file information for a directory or information for
     * just a single file.  This information is obtained through the LIST
     * command.  The contents of the returned array is determined by the
     * <code> FTPFileEntryParser </code> used.
     * The server may or may not expand glob expressions.  You should avoid
     * using glob expressions because the return format for glob listings
     * differs from server to server and will likely cause this method to fail.
     * <p>
     * @param parser The <code> FTPFileEntryParser </code> that should be
     *         used to parse the server file listing.   
     * @param pathname  The file or directory to list.
     * @return The list of file information contained in the given path in
     *         the format determined by the <code> parser </code> parameter.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     */
    public FTPFile[] listFiles(FTPFileEntryParser parser, String pathname)
    throws IOException
    {
        FTPFileList lister = createFileList(pathname, parser);
        FTPFile[] list = lister.getFiles();
        return list;
    }

    /**
     * Using the <code> DefaultFTPFileListEntryParser </code>, obtain a list of
     * file information
     * for a directory or information for just a single file.  This information
     * is obtained through the LIST command.  If the given
     * pathname is a directory and contains no files, <code> null </code> is
     * returned, otherwise an array of <code> FTPFile </code> instances
     * representing the files in the directory is returned.
     * If the pathname corresponds to a file, only the information for that
     * file will be contained in the array (which will be of length 1).  The
     * server may or may not expand glob expressions.  You should avoid using
     * glob expressions because the return format for glob listings differs
     * from server to server and will likely cause this method to fail.
     * <p>
     * @param pathname  The file or directory to list.
     * @return The list of file information contained in the given path.  null
     *     if the list could not be obtained or if there are no files in
     *     the directory.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     */
    public FTPFile[] listFiles(String pathname) throws IOException
    {
        return listFiles(this.defaultParser, pathname);
    }
    
    /**
     * Using the <code> DefaultFTPFileEntryParser </code>, obtain a list of 
     * file information for the current working directory.  This information
     * is obtained through the LIST command.  If the given
     * current directory contains no files null is returned, otherwise an 
     * array of <code> FTPFile </code> instances representing the files in the
     * directory is returned.
     * <p>
     * @return The list of file information contained in the current working
     *     directory.  null if the list could not be obtained or if there are
     *     no files in the directory.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     */
    public FTPFile[] listFiles() throws IOException
    {
        return listFiles(this.defaultParser, null);
    }

    /**
     * Using the default <code> FTPFileEntryParser </code>,  initialize an 
     * object containing a raw file information for the current working 
     * directory.  This information is obtained through  the LIST command.  
     * This object is then capable of being iterated to return a sequence 
     * of FTPFile objects with information filled in by the
     * <code> FTPFileEntryParser </code> used.
     * The server may or may not expand glob expressions.  You should avoid
     * using glob expressions because the return format for glob listings
     * differs from server to server and will likely cause this method to fail.
     * <p>
     * @return An iteratable object that holds the raw information and is 
     * capable of providing parsed FTPFile objects, one for each file containing
     * information contained in the given path in the format determined by the 
     * <code> parser </code> parameter.   Null will be returned if a 
     * data connection cannot be opened.  If the current working directory
     * contains no files, an empty array will be the return.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @see FTPFileList
     */
    public FTPFileList createFileList() throws IOException
    {
        return createFileList(null, this.defaultParser);
    }

    /**
     * Using the default <code> FTPFileEntryParser </code>, 
     * initialize an object containing a raw file information for a directory 
     * or information for a single file.  This information is obtained through 
     * the LIST command.  This object is then capable of being iterated to 
     * return a sequence of FTPFile objects with information filled in by the
     * <code> FTPFileEntryParser </code> used.
     * The server may or may not expand glob expressions.  You should avoid
     * using glob expressions because the return format for glob listings
     * differs from server to server and will likely cause this method to fail.
     * <p>
     * @param basedir The file or directory to list.
     * @return An iteratable object that holds the raw information and is 
     * capable of providing parsed FTPFile objects, one for each file containing
     * information contained in the given path in the format determined by the 
     * <code> parser </code> parameter.   Null will be returned if a 
     * data connection cannot be opened.  If the supplied path contains
     * no files, an empty array will be the return.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @see FTPFileList
     */
    public FTPFileList createFileList(String basedir) throws IOException
    {
        return createFileList(basedir, this.defaultParser);
    }

    /**
     * Using a programmer specified <code> FTPFileEntryParser </code>, 
     * initialize an object containing a raw file information for the 
     * current working directory.  This information is obtained through 
     * the LIST command.  This object is then capable of being iterated to 
     * return a sequence of FTPFile objects with information filled in by the
     * <code> FTPFileEntryParser </code> used.
     * The server may or may not expand glob expressions.  You should avoid
     * using glob expressions because the return format for glob listings
     * differs from server to server and will likely cause this method to fail.
     * <p>
     * @param parser The <code> FTPFileEntryParser </code> that should be
     *         used to parse each server file listing.   
     * @return An iteratable object that holds the raw information and is 
     * capable of providing parsed FTPFile objects, one for each file containing
     * information contained in the given path in the format determined by the 
     * <code> parser </code> parameter.   Null will be returned if a 
     * data connection cannot be opened.  If the current working directory 
     * contains no files, an empty array will be the return.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @see FTPFileList
     */
    public FTPFileList createFileList(FTPFileEntryParser parser)
    throws IOException
    {
        return createFileList(null, parser);
    }

    /**
     * Using a programmer specified <code> FTPFileEntryParser </code>, 
     * initialize an object containing a raw file information for a directory 
     * or information for a single file.  This information is obtained through 
     * the LIST command.  This object is then capable of being iterated to 
     * return a sequence of FTPFile objects with information filled in by the
     * <code> FTPFileEntryParser </code> used.
     * The server may or may not expand glob expressions.  You should avoid
     * using glob expressions because the return format for glob listings
     * differs from server to server and will likely cause this method to fail.
     * <p>
     * @param parser The <code> FTPFileEntryParser </code> that should be
     *         used to parse each server file listing.   
     * @param pathname  The file or directory to list.
     * @return An iteratable object that holds the raw information and is 
     * capable of providing parsed FTPFile objects, one for each file containing
     * information contained in the given path in the format determined by the 
     * <code> parser </code> parameter.  Null will be returned if a 
     * data connection cannot be opened.  If the supplied path contains
     * no files, an empty array will be the return.
     * @exception FTPConnectionClosedException
     *      If the FTP server prematurely closes the connection as a result
     *      of the client being idle or some other reason causing the server
     *      to send FTP reply code 421.  This exception may be caught either
     *      as an IOException or independently as itself.
     * @exception IOException  If an I/O error occurs while either sending a
     *      command to the server or receiving a reply from the server.
     * @see FTPFileList
     */
    public FTPFileList createFileList(String pathname,
                                      FTPFileEntryParser parser)
    throws IOException
    {
        Socket socket;
        FTPFile[] results;

        if ((socket = __openDataConnection(FTPCommand.LIST, pathname)) == null)
        {
            return null;
        }

        FTPFileList list =
            FTPFileList.create(socket.getInputStream(), parser);

        socket.close();

        completePendingCommand();

        return list;
    }
}
