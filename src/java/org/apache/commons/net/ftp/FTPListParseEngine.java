
package org.apache.commons.net.ftp;
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


/**
 * This class handles the entire process of parsing a listing of
 * file entries from the server.
 * <p>
 * This object defines a two-part parsing mechanism.  
 * <p>
 * The first part is comprised of reading the raw input into an internal 
 * list of strings.  Every item in this list corresponds to an actual 
 * file.  All extraneous matter emitted by the server will have been 
 * removed by the end of this phase.  This is accomplished in conjunction 
 * with the FTPFileEntryParser associated with this engine, by calling 
 * its methods <code>readNextEntry()</code> - which handles the issue of 
 * what delimits one entry from another, usually but not always a line 
 * feed and <code>preParse()</code> - which handles removal of 
 * extraneous matter such as the preliminary lines of a listing, removal
 * of duplicates on versioning systems, etc.
 * <p>
 * The second part is composed of the actual parsing, again in conjunction
 * with the particular parser used by this engine.  This is controlled
 * by an iterator over the internal list of strings.  This may be done
 * either in block mode, by calling the <code>getNext()</code> and 
 * <code>getPrevious()</code> methods to provide "paged" output of less
 * than the whole list at one time, or by calling the 
 * <code>getFiles()</code> method to return the entire list.
 * <p>
 * Examples:
 * <p>
 * Paged access:
 * <pre>
 *    FTPClient f=FTPClient();
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPListParseEngine engine = f.initiateListParsing(directory, parser);
 * 
 *    while (engine.hasNext()) {
 *       FTPFile[] files = engine.getNext(25);  // "page size" you want
 *       //do whatever you want with these files, display them, etc.
 *       //expensive FTPFile objects not created until needed.
 *    }
 * </pre>
 */
public class FTPListParseEngine {
	private List entries = new LinkedList();
	private ListIterator _internalIterator; 
	
	FTPFileEntryParser parser = null;
	
	public FTPListParseEngine(FTPFileEntryParser parser) {
		this.parser = parser;
	}
	
    /**
     * handle the iniitial reading and preparsing of the list returned by
     * the server.  After this method has completed, this object will contain
     * a list of unparsed entries (Strings) each referring to a unique file
     * on the server.
     * 
     * @param stream input stream provided by the server socket.
     * 
     * @exception IOException
     *                   thrown on any failure to read from the sever.
     */
	public void readServerList(InputStream stream)
	throws IOException
	{
		this.entries = new LinkedList();
		readStream(stream);
        this.parser.preParse(this.entries);
		resetIterator();
	} 
	
	
    /**
     * Internal method for reading the input into the <code>entries</code> list.
     * After this method has completed, <code>entries</code> will contain a 
     * collection of entries (as defined by 
     * <code>FTPFileEntryParser.readNextEntry()</code>), but this may contain
     * various non-entry preliminary lines from the server output, duplicates, 
     * and other data that will not be part of the final listing.
     * 
     * @param stream The socket stream on which the input will be read.
     * 
     * @exception IOException
     *                   thrown on any failure to read the stream
     */
	private void readStream(InputStream stream) throws IOException
	{
		BufferedReader reader =
			new BufferedReader(new InputStreamReader(stream));

		String line = this.parser.readNextEntry(reader);

		while (line != null)
		{
			this.entries.add(line);
			line = this.parser.readNextEntry(reader);
		}
		reader.close();
	}
	
    /**
     * Returns an array of at most <code>quantityRequested</code> FTPFile 
     * objects starting at this object's internal iterator's current position.
     * If fewer than <code>quantityRequested</code> such 
     * elements are available, the returned array will have a length equal 
     * to the number of entries at and after after the current position.  
     * If no such entries are found, this array will have a length of 0.
     * 
     * After this method is called this object's internal iterator is advanced 
     * by a number of positions equal to the size of the array returned. 
     * 
     * @param quantityRequested
     * the maximum number of entries we want to get.  
     * 
     * @return an array of at most <code>quantityRequested</code> FTPFile 
     * objects starting at the current position of this iterator within its 
     * list and at least the number of elements which  exist in the list at 
     * and after its current position.
     */
	public FTPFile[] getNext(int quantityRequested) {
		List tmpResults = new LinkedList();
		int count = quantityRequested;
		while (count > 0 && this._internalIterator.hasNext()) {
			String entry = (String) this._internalIterator.next();
			FTPFile temp = this.parser.parseFTPEntry(entry);
			tmpResults.add(temp);
			count--;
		}
		return (FTPFile[]) tmpResults.toArray(new FTPFile[0]);
		
	}
	
    /**
     * Returns an array of at most <code>quantityRequested</code> FTPFile 
     * objects starting at this object's internal iterator's current position,
     * and working back toward the beginning.
     * 
     * If fewer than <code>quantityRequested</code> such 
     * elements are available, the returned array will have a length equal 
     * to the number of entries at and after after the current position.  
     * If no such entries are found, this array will have a length of 0.
     * 
     * After this method is called this object's internal iterator is moved 
     * back by a number of positions equal to the size of the array returned. 
     * 
     * @param quantityRequested
     * the maximum number of entries we want to get.  
     * 
     * @return an array of at most <code>quantityRequested</code> FTPFile 
     * objects starting at the current position of this iterator within its 
     * list and at least the number of elements which  exist in the list at 
     * and after its current position.  This array will be in the same order
     * as the underlying list (not reversed).
     */
	public FTPFile[] getPrevious(int quantityRequested) {
		List tmpResults = new LinkedList();
		int count = quantityRequested;
		while (count > 0 && this._internalIterator.hasPrevious()) {
			String entry = (String) this._internalIterator.previous();
			FTPFile temp = this.parser.parseFTPEntry(entry);
			tmpResults.add(0,temp);
			count--;
		}
		return (FTPFile[]) tmpResults.toArray(new FTPFile[0]);
	}

    /**
     * Returns an array of FTPFile objects containing the whole list of
     * files returned by the server as read by this object's parser.
     * 
     * @return an array of FTPFile objects containing the whole list of
     *         files returned by the server as read by this object's parser.
     * @exception IOException
     */
	public FTPFile[] getFiles() 
	throws IOException
	{
		List tmpResults = new LinkedList();
        Iterator iter = this.entries.iterator();
		while (iter.hasNext()) {
			String entry = (String) iter.next();
			FTPFile temp = this.parser.parseFTPEntry(entry);
			tmpResults.add(temp);
		}
		return (FTPFile[]) tmpResults.toArray(new FTPFile[0]);
		
	}
	
    /**
     * convenience method to allow clients to know whether this object's
     * internal iterator's current position is at the end of the list.
     * 
     * @return true if internal iterator is not at end of list, false 
     * otherwise.
     */
	public boolean hasNext() {
		return _internalIterator.hasNext();
	}
	
    /**
     * convenience method to allow clients to know whether this object's
     * internal iterator's current position is at the beginning of the list.
     * 
     * @return true if internal iterator is not at beginning of list, false
     * otherwise.
     */
	public boolean hasPrevious() {
		return _internalIterator.hasPrevious();
	}
	
    /**
     * resets this object's internal iterator to the beginning of the list.
     */
	public void resetIterator() {
		this._internalIterator = this.entries.listIterator();
	}
}
