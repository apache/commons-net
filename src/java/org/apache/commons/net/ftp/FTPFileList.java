/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

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
 * @version $Id$
 * @see org.apache.commons.net.ftp.FTPClient#createFileList
 * @see org.apache.commons.net.ftp.FTPFileIterator
 * @see org.apache.commons.net.ftp.FTPFileEntryParser
 * @see org.apache.commons.net.ftp.FTPListParseEngine
 * @deprecated This class is deprecated as of version 1.2 and will be
 * removed in version 2.0 -- use FTPFileParseEngine instead.
 */
public class FTPFileList
{
    /**
     * storage for the raw lines of input read from the FTP server
     */
    private LinkedList lines = null;
    /**
     * the FTPFileEntryParser assigned to be used with this lister
     */
    private FTPFileEntryParser parser;
    /**
     * private status code for an empty directory
     */
    private static final int EMPTY_DIR = -2;

    /**
     * The only constructor for FTPFileList, private because
     * construction only invoked at create()
     *
     * @param parser a <code>FTPFileEntryParser</code> value that knows
     * how to parse the entries returned by a particular FTP site.
     * @param encoding The encoding to use.
     */
    private FTPFileList (FTPFileEntryParser parser, String encoding)
    {
        this.parser = parser;
        this.lines = new LinkedList();
    }

    /**
     * The only way to create an <code>FTPFileList</code> object.  Invokes
     * the private constructor and then reads the stream  supplied stream to
     * build the intermediate array of "lines" which will later be parsed
     * into <code>FTPFile</code> object.
     *
     * @param stream The input stream created by reading the socket on which
     * the output of the LIST command was returned
     * @param parser the default <code>FTPFileEntryParser</code> to be used
     * by this object.  This may later be changed using the init() method.
     * @param encoding The encoding to use
     *
     * @return the <code>FTPFileList</code> created, with an initialized
     * of unparsed lines of output.  Will be null if the listing cannot
     * be read from the stream.
     * @exception IOException
     *                   Thrown on any failure to read from the socket.
     */
    public static FTPFileList create(InputStream stream,
                                      FTPFileEntryParser parser,
									  String encoding)
            throws IOException
    {
        FTPFileList list = new FTPFileList(parser, encoding);
        list.readStream(stream, encoding);
        parser.preParse(list.lines);
        return list;
    }
    
    /**
     * The only way to create an <code>FTPFileList</code> object.  Invokes
     * the private constructor and then reads the stream  supplied stream to
     * build the intermediate array of "lines" which will later be parsed
     * into <code>FTPFile</code> object.
     *
     * @param stream The input stream created by reading the socket on which
     * the output of the LIST command was returned
     * @param parser the default <code>FTPFileEntryParser</code> to be used
     * by this object.  This may later be changed using the init() method.
     *
     * @return the <code>FTPFileList</code> created, with an initialized
     * of unparsed lines of output.  Will be null if the listing cannot
     * be read from the stream.
     * @exception IOException
     *                   Thrown on any failure to read from the socket.
     *
     * @deprecated The version of this method which takes an encoding should be used.
    */
    public static FTPFileList create(InputStream stream, 
    								  FTPFileEntryParser parser)
    	throws IOException
    {
    	return create(stream, parser, null);
    }
    
    

    /**
     * internal method for reading the input into the <code>lines</code> vector.
     *
     * @param stream The socket stream on which the input will be read.
     * @param encoding The encoding to use.
     *
     * @exception IOException thrown on any failure to read the stream
     */
    public void readStream(InputStream stream, String encoding) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encoding));

        String line = this.parser.readNextEntry(reader);

        while (line != null)
        {
            this.lines.add(line);
            line = this.parser.readNextEntry(reader);
        }
        reader.close();
    }
    
    /**
	 * internal method for reading the input into the <code>lines</code> vector.
	 *
	 * @param stream The socket stream on which the input will be read.
	 *
	 * @exception IOException thrown on any failure to read the stream
	 *
	 * @deprecated The version of this method which takes an encoding should be used.
	*/
	public void readStream(InputStream stream) throws IOException
	{
	 readStream(stream, null);
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
    List getLines()
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
        return new FTPFileIterator(this);
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
        return new FTPFileIterator(this, parser);
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
