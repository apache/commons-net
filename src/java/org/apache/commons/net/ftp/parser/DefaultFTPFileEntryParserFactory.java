/*
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.commons.net.ftp.parser;
import org.apache.commons.net.ftp.FTPFileEntryParser;

/**
 * This is the default implementation of the 
 * FTPFileEntryParserFactory interface.  This is the 
 * implementation that will be used by
 * org.apache.commons.net.ftp.FTPClient.listFiles()
 * if no other implementation has been specified.
 * 
 * @see org.apache.commons.net.ftp.FTPClient#listFiles
 * @see org.apache.commons.net.ftp.FTPClient#setParserFactory
 */
public class DefaultFTPFileEntryParserFactory 
implements FTPFileEntryParserFactory 
{
    /**
     * This default implementation of the FTPFileEntryParserFactory
     * interface works according to the following logic:
     * First it attempts to interpret the supplied key as a fully
     * qualified classname of a class implementing the
     * FTPFileEntryParser interface.  If that succeeds, a parser 
     * object of this class is instantiated and is returned.
     * <p>
     * If <code>key</code> is not recognized as a fully qualified
     * classname known to the system, this method will then attempt
     * to see whether it <b>contains</b> a string identifying one of
     * the known parsers.  This comparison is <b>case-insensitive</b>.
     * The intent here is where possible, to select as keys strings
     * which are returned by the SYST command on the systems which
     * the corresponding parser successfully parses.  This enables 
     * this factory to be used in the auto-detection system. 
     * <p/>
     * @param key    should be a fully qualified classname corresponding to
     *               a class implementing the FTPFileEntryParser interface<br/>
     *               OR<br/>
     *               a string containing (case-insensitively) one of the
     *               following keywords:
     *               <ul>
     *               <li><code>unix</code></li>
     *               <li><code>windows</code></li>
     *               <li><code>os/2</code></li>
     *               <li><code>vms</code></li>
     *               </ul>
     * 
     * @return the FTPFileEntryParser corresponding to the supplied key.
     * @exception ParserInitializationException
     *                   thrown if for any reason the factory cannot resolve 
     *                   the supplied key into an FTPFileEntryParser.
     * @see FTPFileEntryParser
     */
    public FTPFileEntryParser createFileEntryParser(String key) 
    {
        Class parserClass = null;
        try {
            parserClass = Class.forName(key);
            return (FTPFileEntryParser) parserClass.newInstance();
        } catch (ClassNotFoundException e) {
            String ukey = null;
            if (null != key) {
                ukey = key.toUpperCase();
            }
            if (ukey.indexOf("UNIX") >= 0) {
                return new UnixFTPEntryParser();
            } else if (ukey.indexOf("VMS") >= 0) {
                return new VMSVersioningFTPEntryParser();
            } else if (ukey.indexOf("WINDOWS") >= 0) {
                return new NTFTPEntryParser();
            } else if (ukey.indexOf("OS/2") >= 0) {
                return new OS2FTPEntryParser();
            } else {
                throw new ParserInitializationException(
                    "Unknown parser type: " + key);
            }
        } catch (ClassCastException e) {
            throw new ParserInitializationException(
                parserClass.getName() 
                + " does not implement the interface " 
                + "org.apache.commons.net.ftp.FTPFileEntryParser.", e);
        } catch (Throwable e) {
            throw new ParserInitializationException(
                "Error initializing parser", e);
        }

    }
}

