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

import java.io.Serializable;
import java.util.Calendar;

/***
 * The FTPFile class is used to represent information about files stored
 * on an FTP server.  Because there is no standard representation for
 * file information on FTP servers, it may not always be possible to
 * extract all the information that can be represented by FTPFile, or
 * it may even be possible to extract more information.  In cases where
 * more information can be extracted, you will want to subclass FTPFile
 * and implement your own <a href="org.apache.commons.net.ftp.FTPFileListParser.html">
 * FTPFileListParser </a> to extract the information.
 * However, most FTP servers return file information in a format that
 * can be completely parsed by
 * <a href="org.apache.commons.net.ftp.DefaultFTPFileListParser.html">
 * DefaultFTPFileListParser </a> and stored in FTPFile.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see FTPFileListParser
 * @see DefaultFTPFileListParser
 * @see FTPClient#listFiles
 ***/

public class FTPFile implements Serializable
{
    /** A constant indicating an FTPFile is a file. ***/
    public static final int FILE_TYPE = 0;
    /** A constant indicating an FTPFile is a directory. ***/
    public static final int DIRECTORY_TYPE = 1;
    /** A constant indicating an FTPFile is a symbolic link. ***/
    public static final int SYMBOLIC_LINK_TYPE = 2;
    /** A constant indicating an FTPFile is of unknown type. ***/
    public static final int UNKNOWN_TYPE = 3;

    /** A constant indicating user access permissions. ***/
    public static final int USER_ACCESS = 0;
    /** A constant indicating group access permissions. ***/
    public static final int GROUP_ACCESS = 1;
    /** A constant indicating world access permissions. ***/
    public static final int WORLD_ACCESS = 2;

    /** A constant indicating file/directory read permission. ***/
    public static final int READ_PERMISSION = 0;
    /** A constant indicating file/directory write permission. ***/
    public static final int WRITE_PERMISSION = 1;
    /**
     * A constant indicating file execute permission or directory listing
     * permission.
     ***/
    public static final int EXECUTE_PERMISSION = 2;

    int _type, _hardLinkCount;
    long _size;
    String _rawListing, _user, _group, _name, _link;
    Calendar _date;
    boolean[] _permissions[];

    /*** Creates an empty FTPFile. ***/
    public FTPFile()
    {
        _permissions = new boolean[3][3];
        _rawListing = null;
        _type = UNKNOWN_TYPE;
        _hardLinkCount = 0;
        _size = 0;
        _user = null;
        _group = null;
        _date = null;
        _name = null;
    }


    /***
     * Set the original FTP server raw listing from which the FTPFile was
     * created.
     * <p>
     * @param rawListing  The raw FTP server listing.
     ***/
    public void setRawListing(String rawListing)
    {
        _rawListing = rawListing;
    }

    /***
     * Get the original FTP server raw listing used to initialize the FTPFile.
     * <p>
     * @return The original FTP server raw listing used to initialize the
     *         FTPFile.
     ***/
    public String getRawListing()
    {
        return _rawListing;
    }


    /***
     * Determine if the file is a directory.
     * <p>
     * @return True if the file is of type <code>DIRECTORY_TYPE</code>, false if
     *         not.
     ***/
    public boolean isDirectory()
    {
        return (_type == DIRECTORY_TYPE);
    }

    /***
     * Determine if the file is a regular file.
     * <p>
     * @return True if the file is of type <code>FILE_TYPE</code>, false if
     *         not.
     ***/
    public boolean isFile()
    {
        return (_type == FILE_TYPE);
    }

    /***
     * Determine if the file is a symbolic link.
     * <p>
     * @return True if the file is of type <code>UNKNOWN_TYPE</code>, false if
     *         not.
     ***/
    public boolean isSymbolicLink()
    {
        return (_type == SYMBOLIC_LINK_TYPE);
    }

    /***
     * Determine if the type of the file is unknown.
     * <p>
     * @return True if the file is of type <code>UNKNOWN_TYPE</code>, false if
     *         not.
     ***/
    public boolean isUnknown()
    {
        return (_type == UNKNOWN_TYPE);
    }


    /***
     * Set the type of the file (<code>DIRECTORY_TYPE</code>,
     * <code>FILE_TYPE</code>, etc.).
     * <p>
     * @param type  The integer code representing the type of the file.
     ***/
    public void setType(int type)
    {
        _type = type;
    }


    /***
     * Return the type of the file (one of the <code>_TYPE</code> constants),
     * e.g., if it is a directory, a regular file, or a symbolic link.
     * <p>
     * @return The type of the file.
     ***/
    public int getType()
    {
        return _type;
    }


    /***
     * Set the name of the file.
     * <p>
     * @param name  The name of the file.
     ***/
    public void setName(String name)
    {
        _name = name;
    }

    /***
     * Return the name of the file.
     * <p>
     * @return The name of the file.
     ***/
    public String getName()
    {
        return _name;
    }


    /***
     * Set the file size in bytes.
     * <p>
     * @param The file size in bytes.
     ***/
    public void setSize(long size)
    {
        _size = size;
    }


    /***
     * Return the file size in bytes.
     * <p>
     * @return The file size in bytes.
     ***/
    public long getSize()
    {
        return _size;
    }


    /***
     * Set the number of hard links to this file.  This is not to be
     * confused with symbolic links.
     * <p>
     * @param links  The number of hard links to this file.
     ***/
    public void setHardLinkCount(int links)
    {
        _hardLinkCount = links;
    }


    /***
     * Return the number of hard links to this file.  This is not to be
     * confused with symbolic links.
     * <p>
     * @return The number of hard links to this file.
     ***/
    public int getHardLinkCount()
    {
        return _hardLinkCount;
    }


    /***
     * Set the name of the group owning the file.  This may be
     * a string representation of the group number.
     * <p>
     * @param group The name of the group owning the file.
     ***/
    public void setGroup(String group)
    {
        _group = group;
    }


    /***
     * Returns the name of the group owning the file.  Sometimes this will be
     * a string representation of the group number.
     * <p>
     * @return The name of the group owning the file.
     ***/
    public String getGroup()
    {
        return _group;
    }


    /***
     * Set the name of the user owning the file.  This may be
     * a string representation of the user number;
     * <p>
     * @param user The name of the user owning the file.
     ***/
    public void setUser(String user)
    {
        _user = user;
    }

    /***
     * Returns the name of the user owning the file.  Sometimes this will be
     * a string representation of the user number.
     * <p>
     * @return The name of the user owning the file.
     ***/
    public String getUser()
    {
        return _user;
    }


    /***
     * If the FTPFile is a symbolic link, use this method to set the name of the
     * file being pointed to by the symbolic link.
     * <p>
     * @param link  The file pointed to by the symbolic link.
     ***/
    public void setLink(String link)
    {
        _link = link;
    }


    /***
     * If the FTPFile is a symbolic link, this method returns the name of the
     * file being pointed to by the symbolic link.  Otherwise it returns null.
     * <p>
     * @return The file pointed to by the symbolic link (null if the FTPFile
     *         is not a symbolic link).
     ***/
    public String getLink()
    {
        return _link;
    }


    /***
     * Set the file timestamp.  This usually the last modification time.
     * The parameter is not cloned, so do not alter its value after calling
     * this method.
     * <p>
     * @param date A Calendar instance representing the file timestamp.
     ***/
    public void setTimestamp(Calendar date)
    {
        _date = date;
    }


    /***
     * Returns the file timestamp.  This usually the last modification time.
     * <p>
     * @return A Calendar instance representing the file timestamp.
     ***/
    public Calendar getTimestamp()
    {
        return _date;
    }


    /***
     * Set if the given access group (one of the <code> _ACCESS </code>
     * constants) has the given access permission (one of the
     * <code> _PERMISSION </code> constants) to the file.
     * <p>
     * @param access The access group (one of the <code> _ACCESS </code>
     *               constants)
     * @param permission The access permission (one of the 
     *               <code> _PERMISSION </code> constants)
     * @param value  True if permission is allowed, false if not.
     ***/
    public void setPermission(int access, int permission, boolean value)
    {
        _permissions[access][permission] = value;
    }


    /***
     * Determines if the given access group (one of the <code> _ACCESS </code>
     * constants) has the given access permission (one of the
     * <code> _PERMISSION </code> constants) to the file.
     * <p>
     * @param access The access group (one of the <code> _ACCESS </code>
     *               constants)
     * @param permission The access permission (one of the 
     *               <code> _PERMISSION </code> constants)
     ***/
    public boolean hasPermission(int access, int permission)
    {
        return _permissions[access][permission];
    }


    /***
     * Returns a string representation of the FTPFile information.  This
     * will be the raw FTP server listing that was used to initialize the
     * FTPFile instance.
     * <p>
     * @return A string representation of the FTPFile information.
     ***/
    public String toString()
    {
        return _rawListing;
    }

}
