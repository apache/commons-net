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
package org.apache.commons.net.ftp.ftp2.parser;

import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.MatchResult;

/**
 * This class is based on the logic of Winston Ojeda's ListParser.  It uses
 * a similar approach to Regular Expressions but is designed to fit within
 * FTPFileEntryParser design rather than the FTPFileListParser design.
 * It is also designed to encapsulate access to the oro.text.regex
 * classes in one place.
 * @author <a href="mailto:scohen@ignitesports.com">Steve Cohen</a>
 * @version $Id$
 */
abstract class MatchApparatus
{
    private String prefix;
    private Pattern pattern = null;
    private PatternMatcher matcher = null;
    private MatchResult result = null;
    
    /**
     * The constructor for a MatchApparatus object.
     * 
     * @param regex  The regular expression with which this object is 
     * initialized.
     * 
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen in 
     * normal conditions.  It it is seen, this is a sign that a subclass has 
     * been created with a bad regular expression.   Since the parser must be 
     * created before use, this means that any bad parser subclasses created 
     * from this will bomb very quickly,  leading to easy detection.  
     */
    MatchApparatus(String regex) 
    {
        try 
        {
            this.prefix = "[" + getClass().getName() + "] ";
            this.matcher = new Perl5Matcher();
            this.pattern = new Perl5Compiler().compile(regex);
        } 
        catch (MalformedPatternException e) 
        {
            throw new IllegalArgumentException (
                "Unparseable regex supplied:  " + regex);
        }
    }


    /**
     * Convenience method delegates to the internal MatchResult's matches()
     * method.
     *
     * @param s the String to be matched
     * @return true if s matches this object's regular expression.
     */
    public boolean matches(String s)
    {
        result = null;
        if (matcher.matches(s.trim(), this.pattern))
        {
            this.result = matcher.getMatch();
        }
        return null != this.result;
    }

    /**
     * Convenience method delegates to the internal MatchResult's groups()
     * method.
     *
     * @return the number of groups() in the internal MatchResult.
     */
    public int getGroupCnt()
    {
        if (this.result == null)
        {
            return 0;
        }
        return this.result.groups();
    }

    /**
     * Convenience method delegates to the internal MatchResult's group()
     * method.  
     *
     * @return the content of the <code>matchnum'th<code> group of the internal
     * match or null if this method is called without a match having been made.
     */
    public String group(int matchnum)
    {
        if (this.result == null)
        {
            return null;
        }
        return this.result.group(matchnum);
    }

    /**
     * For debugging purposes - returns a string shows each match group by 
     * number.
     * 
     * @return a string shows each match group by number.
     */
    public String getGroupsAsString()
    {
        StringBuffer b = new StringBuffer();
        for (int i = 1; i <= this.result.groups(); i++)
        {
            b.append(i).append(") ").append(this.result.group(i))
                .append(System.getProperty("line.separator"));
        }
        return b.toString() ;

    }
}
