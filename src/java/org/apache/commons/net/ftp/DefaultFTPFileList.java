package org.apache.commons.net.ftp;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2004 The Apache Software Foundation.  All rights
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
import java.util.Vector;

/**
 * This class encapsulates a listing of files from an FTP server.  It is
 * initialized with an input stream which is read and the input split into
 * lines, each of which (after some possible initial verbiage) represents
 * a file on the FTP server.  A parser is also supplied, which is used to
 * iterate through the internal list of lines parsing each into an FTPFile
 * object which is returned to the caller of the iteration methods.  This
 * parser may be replaced with another, allowing the same list to be parsed
 * with different parsers.
 * Parsing takes place on an as-needed basis, basically, the first time a
 * position is iterated over.  This happens at the time of iteration, not
 * prior to it as the older <code>(FTPClient.listFiles()</code> methods did,
 * which required a bigger memory hit.
 * 
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id: DefaultFTPFileList.java,v 1.1 2004/01/09 09:07:03 dfs Exp $
 * @see org.apache.commons.net.ftp.FTPClient#createFileList
 * @see org.apache.commons.net.ftp.FTPFileIterator
 * @see org.apache.commons.net.ftp.FTPFileEntryParser
 */
public class DefaultFTPFileList extends FTPFileList
{
    /**
     * storage for the raw lines of input read from the FTP server
     */
    private Vector lines = null;
    /**
     * the FTPFileEntryParser assigned to be used with this lister
     */
    private FTPFileEntryParser parser;
    /**
     * private status code for an empty directory
     */
    private static final int EMPTY_DIR = -2;

    /**
     * @param parser a <code>FTPFileEntryParser</code> value that knows
     * how to parse the entries returned by a particular FTP site.
     */
    public DefaultFTPFileList (FTPFileEntryParser parser)
    {
        this.parser = parser;
        this.lines = new Vector();
    }

    /**
     * internal method for reading the input into the <code>lines</code> vector.
     * 
     * @param stream The socket stream on which the input will be read.
     * 
     * @exception IOException thrown on any failure to read the stream
     */
    public void readStream(InputStream stream) throws IOException
    {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(stream));

        String line = this.parser.readNextEntry(reader);

        while (line != null)
        {
            this.lines.addElement(line);
            line = this.parser.readNextEntry(reader);
        }
        reader.close();
    }

    /**
     * Accessor for this object's default parser.
     * 
     * @return this object's default parser.
     */
    FTPFileEntryParser getParser()
    {
        return this.parser;
    }

    /**
     * Package private accessor for the collection of raw input lines.
     * 
     * @return vector containing all the raw input lines returned from the FTP 
     * server
     */
    Vector getLines()
    {
        return this.lines;
    }

    /**
     * create an iterator over this list using the parser with which this list 
     * was initally created
     * 
     * @return an iterator over this list using the list's default parser.
     */
    public FTPFileIterator iterator()
    {
        return new DefaultFTPFileIterator(this);
    }
    /**
     * create an iterator over this list using the supplied parser
     * 
     * @param parser The user-supplied parser with which the list is to be 
     * iterated, may be different from this list's default parser.
     * 
     * @return an iterator over this list using the supplied parser.
     */
    public FTPFileIterator iterator(FTPFileEntryParser parser)
    {
        return new DefaultFTPFileIterator(this, parser);
    }


    /**
     * returns an array of FTPFile objects for all the files in the directory 
     * listing
     *
     * @return  an array of FTPFile objects for all the files in the directory 
     * listinge
     */
    public FTPFile[] getFiles()
    {
        return iterator().getFiles();
    }
}

/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
