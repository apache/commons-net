package org.apache.commons.net.ftp.ftp2;

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
 *    "Apache Turbine", nor may "Apache" appear in their name, without
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
import java.util.Enumeration;
import java.util.Vector;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.ftp2.FTPFileEntryParser;

/**
 * FTPFileList.java
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
 * @author <a href="mailto:stevecoh1@attbi.com">Steve Cohen</a>
 * @version $Id: FTPFileList.java,v 1.1 2002/04/29 03:55:32 brekke Exp $
 * @see FTPClient2#listFiles
 * @see FTPClient2#createFileList
 */
public class FTPFileList
{
    private Vector lines = null;
    private FTPFileEntryParser parser;
    private static final int EMPTY_DIR = -2;

    /**
     * The only constructor for FTPFileList, private because
     * construction only invoked at createFTPFileList()
     *
     * @param parser a <code>FTPFileEntryParser</code> value that knows
     * how to parse the entries returned by a particular FTP site.
     */
    private FTPFileList (FTPFileEntryParser parser)
    {
        this.parser = parser;
        this.lines = new Vector();
    }

    /**
     * The only way to create an <code> FTPFileList</code> object.  Invokes 
     * the private constructor and then reads the stream  supplied stream to
     * build the intermediate array of "lines" which will later be parsed
     * into <code>FTPFile</code> object.
     *
     * @param stream The input stream created by reading the socket on which 
     * the output of the LIST command was returned
     * @param parser the default <code>FTPFileEntryParser</code> to be used
     * by this object.  This may later be changed using the init() method.
     * @param sleepInterval - amount of time in milliseconds to sleep
     *                        between lines read.
     * @return the <code>FTPFileList</code> created, with an initialized
     * of unparsed lines of output.  Will be null if the listing cannot
     * be read from the stream.
     */
    public static FTPFileList create( InputStream stream,
                                      FTPFileEntryParser parser)
    {
        try
        {
            FTPFileList list = new FTPFileList(parser);
            list.readStream(stream);
            return list;

        }
        catch (IOException e)
        {
            System.out.println("IOException " + e.getMessage());
            return null;
        }
    }

    private void readStream(InputStream stream) throws IOException
    {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(stream));

        String line = reader.readLine();

        while (line != null)
        {
            this.lines.addElement(line);
            line = reader.readLine();
        }
        reader.close();
    }

    FTPFileEntryParser getParser()
    {
        return this.parser;
    }

    Vector getLines()
    {
        return this.lines;
    }

    public FTPFileIterator iterator()
    {
        return new FTPFileIterator(this);
    }
    public FTPFileIterator iterator(FTPFileEntryParser parser)
    {
        return new FTPFileIterator(this, parser);
    }

    private FTPFile parseFTPEntry(String entry)
    {
        return this.parser.parseFTPEntry(entry);
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
        return iterator().getNext(0);
    }
}
