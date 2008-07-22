/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.ftp.parser;

import org.apache.commons.net.ftp.Configurable;
import org.apache.commons.net.ftp.FTPClientConfig;
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
	private FTPClientConfig config = null;

	/**
     * This default implementation of the FTPFileEntryParserFactory
     * interface works according to the following logic:
     * First it attempts to interpret the supplied key as a fully
     * qualified classname of a class implementing the
     * FTPFileEntryParser interface.  If that succeeds, a parser
     * object of this class is instantiated and is returned; 
     * otherwise it attempts to interpret the key as an identirier
     * commonly used by the FTP SYST command to identify systems.
     * <p/>
     * If <code>key</code> is not recognized as a fully qualified
     * classname known to the system, this method will then attempt
     * to see whether it <b>contains</b> a string identifying one of
     * the known parsers.  This comparison is <b>case-insensitive</b>.
     * The intent here is where possible, to select as keys strings
     * which are returned by the SYST command on the systems which
     * the corresponding parser successfully parses.  This enables
     * this factory to be used in the auto-detection system.
     * <p/>
     *
     * @param key    should be a fully qualified classname corresponding to
     *               a class implementing the FTPFileEntryParser interface<br/>
     *               OR<br/>
     *               a string containing (case-insensitively) one of the
     *               following keywords:
     *               <ul>
     *               <li>{@link FTPClientConfig#SYST_UNIX UNIX}</li>
     *               <li>{@link FTPClientConfig#SYST_NT WINDOWS}</li>
     *               <li>{@link FTPClientConfig#SYST_OS2 OS/2}</li>
     *               <li>{@link FTPClientConfig#SYST_OS400 OS/400}</li>
     *               <li>{@link FTPClientConfig#SYST_VMS VMS}</li>
     *               <li>{@link FTPClientConfig#SYST_MVS MVS}</li>
     *               <li>{@link FTPClientConfig#SYST_NETWARE}</li>
     *               </ul>
     * @return the FTPFileEntryParser corresponding to the supplied key.
     * @throws ParserInitializationException thrown if for any reason the factory cannot resolve
     *                   the supplied key into an FTPFileEntryParser.
     * @see FTPFileEntryParser
     */
    public FTPFileEntryParser createFileEntryParser(String key)
    {
    	if (key == null)
    		throw new ParserInitializationException("Parser key cannot be null");
    		
        Class<?> parserClass = null;
        FTPFileEntryParser parser = null;
        try
        {
            parserClass = Class.forName(key);
            parser = (FTPFileEntryParser) parserClass.newInstance();
        }
        catch (ClassNotFoundException e)
        {
            try 
            {
	            String ukey = null;
	            if (null != key)
	            {
	                ukey = key.toUpperCase(java.util.Locale.ENGLISH);
	            }
	            if ((ukey.indexOf(FTPClientConfig.SYST_UNIX) >= 0) 
	            		|| (ukey.indexOf(FTPClientConfig.SYST_L8) >= 0))
	            {
	                parser = createUnixFTPEntryParser();
	            }
	            else if (ukey.indexOf(FTPClientConfig.SYST_VMS) >= 0)
	            {
	                parser = createVMSVersioningFTPEntryParser();
	            }
	            else if (ukey.indexOf(FTPClientConfig.SYST_NT) >= 0)
	            {
	                parser = createNTFTPEntryParser();
	            }
	            else if (ukey.indexOf(FTPClientConfig.SYST_OS2) >= 0)
	            {
	                parser = createOS2FTPEntryParser();
	            }
	            else if (ukey.indexOf(FTPClientConfig.SYST_OS400) >= 0 ||
	            		ukey.indexOf(FTPClientConfig.SYST_AS400) >= 0)
	            {
	                parser = createOS400FTPEntryParser();
	            }
	            else if (ukey.indexOf(FTPClientConfig.SYST_MVS) >= 0)
	            {
	                parser = createMVSEntryParser();
	        	}
	            else if (ukey.indexOf(FTPClientConfig.SYST_NETWARE) >= 0) 
	            {
	            	parser = createNetwareFTPEntryParser();
	            }
	            else
	            {
	                throw new ParserInitializationException("Unknown parser type: " + key);
	            }
            } 
            catch (NoClassDefFoundError nf) {
                    throw new ParserInitializationException("Error initializing parser", nf);
            }

        }
        catch (NoClassDefFoundError e)
        { 	
            throw new ParserInitializationException("Error initializing parser", e);
        }
        catch (ClassCastException e)
        {
            throw new ParserInitializationException(parserClass.getName()
                + " does not implement the interface "
                + "org.apache.commons.net.ftp.FTPFileEntryParser.", e);
        }
        catch (Throwable e)
        {
            throw new ParserInitializationException("Error initializing parser", e);
        }

        if (parser instanceof Configurable) {
            ((Configurable)parser).configure(this.config);
        }    
        return parser;
    }
    
    /**
     * <p>Implementation extracts a key from the supplied 
     * {@link  FTPClientConfig FTPClientConfig}
     * parameter and creates an object implementing the
     * interface FTPFileEntryParser and uses the supplied configuration
     * to configure it.
     * </p><p>
     * Note that this method will generally not be called in scenarios
     * that call for autodetection of parser type but rather, for situations
     * where the user knows that the server uses a non-default configuration
     * and knows what that configuration is.
     * </p>
     * @param config  A {@link  FTPClientConfig FTPClientConfig}  
     * used to configure the parser created
     *
     * @return the @link  FTPFileEntryParser FTPFileEntryParser} so created.
     * @exception ParserInitializationException
     *                   Thrown on any exception in instantiation
     * @since 1.4
     */
	public FTPFileEntryParser createFileEntryParser(FTPClientConfig config) 
	throws ParserInitializationException 
	{
	    this.config = config;
		String key = config.getServerSystemKey();
		return createFileEntryParser(key);
	}


    public FTPFileEntryParser createUnixFTPEntryParser()
    {
        return new UnixFTPEntryParser();
    }

    public FTPFileEntryParser createVMSVersioningFTPEntryParser()
    {
        return new VMSVersioningFTPEntryParser();
    }
    
    public FTPFileEntryParser createNetwareFTPEntryParser() {
    	return new NetwareFTPEntryParser();
    }

    public FTPFileEntryParser createNTFTPEntryParser()
    {
    	if (config != null && FTPClientConfig.SYST_NT.equals(
    	        config.getServerSystemKey())) 
    	{
            return new NTFTPEntryParser();
    	} else {
            return new CompositeFileEntryParser(new FTPFileEntryParser[]
	   	        {
	   	            new NTFTPEntryParser(),
	   	            new UnixFTPEntryParser()
	   	        });
    	}
    }
    
     public FTPFileEntryParser createOS2FTPEntryParser()
    {
        return new OS2FTPEntryParser();
    }

    public FTPFileEntryParser createOS400FTPEntryParser()
    {
    	if (config != null && 
    	        FTPClientConfig.SYST_OS400.equals(config.getServerSystemKey())) 
    	{
            return new OS400FTPEntryParser();
    	} else {
	        return new CompositeFileEntryParser(new FTPFileEntryParser[]
	            {
	                new OS400FTPEntryParser(),
	                new UnixFTPEntryParser()
	            });
    	}
    }

    public FTPFileEntryParser createMVSEntryParser()
    {
        return new MVSFTPEntryParser();
    }


	
}

