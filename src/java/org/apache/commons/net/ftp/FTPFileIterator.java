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

import java.util.Vector;

/**
 * This class implements a bidirectional iterator over an FTPFileList.
 * Elements may be retrieved one at at time using the hasNext() - next()
 * syntax familiar from Java 2 collections.  Alternatively, entries may
 * be receieved as an array of any requested number of entries or all of them.
 * 
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id: FTPFileIterator.java,v 1.8 2004/01/10 15:36:40 scohen Exp $
 * @see org.apache.commons.net.ftp.FTPFileList
 * @see org.apache.commons.net.ftp.FTPFileEntryParser
 */
class FTPFileIterator 
{
    /**
     * a vector of strings, each representing a possibly valid ftp file
     * entry
     */
    private Vector rawlines;
    
    /**
     * the parser to which this iterator delegates its parsing duties
     */
    private FTPFileEntryParser parser;

    /**
     * constant shorthand for the situation where the raw listing has not 
     * yet been scanned
     */
    private static final int UNINIT = -1;
    
    /**
     * constant shorthand for the situation where the raw listing has been 
     * scanned and found to have no valid entry.
     */
    private static final int DIREMPTY = -2;
    
    /**
     * this iterator's current position within <code>rawlines</code>.
     */
    private int itemptr = 0;
    
    /**
     * number within <code>rawlines</code> of the first valid file entry.
     */
    private int firstGoodEntry = UNINIT;

    /**
     * "Package-private" constructor.  Only the FTPFileList can
     * create an iterator, using it's iterator() method.  The list
     * will be iterated with the list's default parser.
     *
     * @param rawlist the FTPFileList to be iterated
     */
    FTPFileIterator (FTPFileList rawlist)
    {
        this(rawlist, rawlist.getParser());
    }

    /**
     * "Package-private" constructor.  Only the FTPFileList can
     * create an iterator, using it's iterator() method.  The list will be
     * iterated with a supplied parser
     *
     * @param rawlist the FTPFileList to be iterated
     * @param parser the system specific parser for raw FTP entries.
     */
    FTPFileIterator (FTPFileList rawlist,
                            FTPFileEntryParser parser)
    {
        this.rawlines = rawlist.getLines();
        this.parser = parser;
    }

    /**
     * Delegates to this object's parser member the job of parsing an
     * entry.
     * 
     * @param entry  A string containing one entry, as determined by the 
     * parser's getNextEntry() method.
     * 
     * @return an FTPFile object representing this entry or null if it can't be 
     *         parsed as a file
     */
    private FTPFile parseFTPEntry(String entry)
    {
        return this.parser.parseFTPEntry(entry);
    }

    /**
     * Skips over any introductory lines and stuff in the listing that does
     * not represent files, returning the line number of the first entry
     * that does represent a file.
     * 
     * @return the line number within <code>rawlines</code> of the first good 
     * entry in the array or DIREMPTY if there are no good entries.
     */
    private int getFirstGoodEntry()
    {
        FTPFile entry = null;
        for (int iter = 0; iter < this.rawlines.size(); iter++)
        {
            String line = (String) this.rawlines.elementAt(iter);
            entry = parseFTPEntry(line);
            if (null != entry)
            {
                return iter;
            }
        }
        return DIREMPTY;
    }

    /**
     * resets iterator to the beginning of the list.
     */
    private void init()
    {
        this.itemptr = 0;
        this.firstGoodEntry = UNINIT;
    }

    /**
     * shorthand for an empty return value.
     */
    private static final FTPFile[] EMPTY = new FTPFile[0];

    /**
     * Returns a list of FTPFile objects for ALL files listed in the server's
     * LIST output.
     *
     * @return a list of FTPFile objects for ALL files listed in the server's
     * LIST output.
     */
    public FTPFile[] getFiles()
    {
        if (this.itemptr != DIREMPTY)
        {
            init();
        }
        return getNext(0);
    }

    /**
     * Returns an array of at most <code>quantityRequested</code> FTPFile 
     * objects starting at this iterator's current position  within its 
     * associated list. If fewer than <code>quantityRequested</code> such 
     * elements are available, the returned array will have a length equal 
     * to the number of entries at and after after the current position.  
     * If no such entries are found, this array will have a length of 0.
     * 
     * After this method is called the current position is advanced by 
     * either <code>quantityRequested</code> or the number of entries 
     * available after the iterator, whichever is fewer.
     * 
     * @param quantityRequested
     * the maximum number of entries we want to get.  A 0
     * passed here is a signal to get ALL the entries.
     * 
     * @return an array of at most <code>quantityRequested</code> FTPFile 
     * objects starting at the current position of this iterator within its 
     * list and at least the number of elements which  exist in the list at 
     * and after its current position.
     */
    public FTPFile[] getNext(int quantityRequested)
    {

        // if we haven't gotten past the initial junk do so.
        if (this.firstGoodEntry == UNINIT)
        {
            this.firstGoodEntry = getFirstGoodEntry();
        }
        if (this.firstGoodEntry == DIREMPTY)
        {
            return EMPTY;
        }

        int max = this.rawlines.size() - this.firstGoodEntry;

        // now that we know the maximum we can possibly get,
        // resolve a 0 request to ask for that many.

        int howMany = (quantityRequested == 0) ? max : quantityRequested;
        howMany = (howMany + this.itemptr < this.rawlines.size())
                   ? howMany
                   : this.rawlines.size() - this.itemptr;

        FTPFile[] output = new FTPFile[howMany];

        for (int i = 0, e = this.firstGoodEntry + this.itemptr ;
                i < howMany; i++, e++)
        {
            output[i] = parseFTPEntry((String) this.rawlines.elementAt(e));
            this.itemptr++;
        }
        return output;
    }

    /**
     * Method for determining whether getNext() will successfully return a
     * non-null value.
     *
     * @return true if there exist any files after the one currently pointed
     * to by the internal iterator, false otherwise.
     */
    public boolean hasNext()
    {
        int fge = this.firstGoodEntry;
        if (fge == DIREMPTY)
        {
            //directory previously found empty - return false
            return false;
        }
        else if (fge < 0)
        {
            // we haven't scanned the list yet so do it first
            fge = getFirstGoodEntry();
        }
        return fge + this.itemptr < this.rawlines.size();
    }

    /**
     * Returns a single parsed FTPFile object corresponding to the raw input
     * line at this iterator's current position.
     *
     * After this method is called the internal iterator is advanced by one
     * element (unless already at end of list).
     *
     * @return a single FTPFile object corresponding to the raw input line
     * at the position of the internal iterator over the list of raw input
     * lines maintained by this object or null if no such object exists.
     */
    public FTPFile next()
    {
        FTPFile[] file = getNext(1);
        if (file.length > 0)
        {
            return file[0];
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns an array of at most <code>quantityRequested</code> FTPFile 
     * objects starting at the position preceding this iterator's current 
     * position within its associated list. If fewer than 
     * <code>quantityRequested</code> such elements are available, the 
     * returned array will have a length equal to the number of entries after
     * the iterator.  If no such entries are found, this array will have a 
     * length of 0.  The entries will be ordered in the same order as the 
     * list, not reversed.
     *
     * After this method is called the current position is moved back by 
     * either <code>quantityRequested</code> or the number of entries 
     * available before the current position, whichever is fewer.
     * @param quantityRequested the maximum number of entries we want to get.  
     * A 0 passed here is a signal to get ALL the entries.
     * @return  an array of at most <code>quantityRequested</code> FTPFile 
     * objects starting at the position preceding the current position of 
     * this iterator within its list and at least the number of elements which
     * exist in the list prior to its current position.
     */
    public FTPFile[] getPrevious(int quantityRequested)
    {
        int howMany = quantityRequested;
        // can't retreat further than we've previously advanced
        if (howMany > this.itemptr)
        {
            howMany = this.itemptr;
        }
        FTPFile[] output = new FTPFile[howMany];
        for (int i = howMany, e = this.firstGoodEntry + this.itemptr; i > 0;)
        {
            output[--i] = parseFTPEntry((String) this.rawlines.elementAt(--e));
            this.itemptr--;
        }
        return output;
    }

    /**
     * Method for determining whether getPrevious() will successfully return a
     * non-null value.
     *
     * @return true if there exist any files before the one currently pointed
     * to by the internal iterator, false otherwise.
     */
    public boolean hasPrevious()
    {
        int fge = this.firstGoodEntry;
        if (fge == DIREMPTY)
        {
            //directory previously found empty - return false
            return false;
        }
        else if (fge < 0)
        {
            // we haven't scanned the list yet so do it first
            fge = getFirstGoodEntry();
        }

        return this.itemptr > fge;
    }

    /**
     * Returns a single parsed FTPFile object corresponding to the raw input
     * line at the position preceding that of the internal iterator over
     * the list of raw lines maintained by this object
     *
     * After this method is called the internal iterator is retreated by one
     * element (unless it is already at beginning of list).
     * @return a single FTPFile object corresponding to the raw input line
     * at the position immediately preceding that of the internal iterator
     * over the list of raw input lines maintained by this object.
     */
    public FTPFile previous()
    {
        FTPFile[] file = getPrevious(1);
        if (file.length > 0)
        {
            return file[0];
        }
        else
        {
            return null;
        }
    }
}

/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
