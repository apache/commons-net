package org.apache.commons.net.nntp;

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

import java.util.Calendar;

/***
 * The NewGroupsOrNewsQuery class.  This is used to issue NNTP NEWGROUPS and
 * NEWNEWS queries, implemented by
 * <a href="org.apache.commons.net.nntp.NNTPClient.html#listNewNewsgroups">
 * listNewNewsGroups </a> and
 * <a href="org.apache.commons.net.nntp.NNTPClient.html#listNewNews">
 * listNewNews </a> respectively.  It prevents you from having to format
 * date, time, distribution, and newgroup arguments.
 * <p>
 * You might use the class as follows:
 * <pre>
 * query = new NewsGroupsOrNewsQuery(new GregorianCalendar(97, 11, 15), false);
 * query.addDistribution("comp");
 * NewsgroupInfo[] newsgroups = client.listNewgroups(query);
 * </pre>
 * This will retrieve the list of newsgroups starting with the comp.
 * distribution prefix created since midnight 11/15/97.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see NNTPClient
 ***/

public final class NewGroupsOrNewsQuery
{
    private String __date, __time;
    private StringBuffer __distributions;
    private StringBuffer __newsgroups;
    private boolean __isGMT;


    /***
     * Creates a new query using the given time as a reference point.
     * <p>
     * @param date  The date since which new groups or news have arrived.
     * @param gmt   True if the date should be considered as GMT, false if not.
     ***/
    public NewGroupsOrNewsQuery(Calendar date, boolean gmt)
    {
        int num;
        String str;
        StringBuffer buffer;

        __distributions = null;
        __newsgroups = null;
        __isGMT = gmt;

        buffer = new StringBuffer();

        // Get year
        num = date.get(Calendar.YEAR);
        str = Integer.toString(num);
        num = str.length();

        if (num >= 2)
            buffer.append(str.substring(num - 2));
        else
            buffer.append("00");

        // Get month
        num = date.get(Calendar.MONTH) + 1;
        str = Integer.toString(num);
        num = str.length();

        if (num == 1)
        {
            buffer.append('0');
            buffer.append(str);
        }
        else if (num == 2)
            buffer.append(str);
        else
            buffer.append("01");

        // Get day
        num = date.get(Calendar.DAY_OF_MONTH);
        str = Integer.toString(num);
        num = str.length();

        if (num == 1)
        {
            buffer.append('0');
            buffer.append(str);
        }
        else if (num == 2)
            buffer.append(str);
        else
            buffer.append("01");

        __date = buffer.toString();

        buffer.setLength(0);

        // Get hour
        num = date.get(Calendar.HOUR_OF_DAY);
        str = Integer.toString(num);
        num = str.length();

        if (num == 1)
        {
            buffer.append('0');
            buffer.append(str);
        }
        else if (num == 2)
            buffer.append(str);
        else
            buffer.append("00");

        // Get minutes
        num = date.get(Calendar.MINUTE);
        str = Integer.toString(num);
        num = str.length();

        if (num == 1)
        {
            buffer.append('0');
            buffer.append(str);
        }
        else if (num == 2)
            buffer.append(str);
        else
            buffer.append("00");


        // Get seconds
        num = date.get(Calendar.SECOND);
        str = Integer.toString(num);
        num = str.length();

        if (num == 1)
        {
            buffer.append('0');
            buffer.append(str);
        }
        else if (num == 2)
            buffer.append(str);
        else
            buffer.append("00");

        __time = buffer.toString();
    }


    /***
     * Add a newsgroup to the list of newsgroups being queried.  Newsgroups
     * added this way are only meaningful to the NEWNEWS command.  Newsgroup
     * names may include the <code> * </code> wildcard, as in
     * <code>comp.lang.* </code> or <code> comp.lang.java.* </code>.  Adding
     * at least one newsgroup is mandatory for the NEWNEWS command.
     * <p>
     * @param newsgroup  The newsgroup to add to the list of groups to be
     *                   checked for new news.
     ***/
    public void addNewsgroup(String newsgroup)
    {
        if (__newsgroups != null)
            __newsgroups.append(',');
        else
            __newsgroups = new StringBuffer();
        __newsgroups.append(newsgroup);
    }


    /***
     * Add a newsgroup to the list of newsgroups being queried, but indicate
     * that group should not be checked for new news.  Newsgroups
     * added this way are only meaningful to the NEWNEWS command.
     * Newsgroup names may include the <code> * </code> wildcard, as in
     * <code>comp.lang.* </code> or <code> comp.lang.java.* </code>.
     * <p>
     * The following would create a query that searched for new news in
     * all comp.lang.java newsgroups except for comp.lang.java.advocacy.
     * <pre>
     * query.addNewsgroup("comp.lang.java.*");
     * query.omitNewsgroup("comp.lang.java.advocacy");
     * </pre>
     * <p>
     * @param newsgroup  The newsgroup to add to the list of groups to be
     *                   checked for new news, but which should be omitted from
     *                   the search for new news..
     ***/
    public void omitNewsgroup(String newsgroup)
    {
        addNewsgroup("!" + newsgroup);
    }


    /***
     * Add a distribution group to the query.  The distribution part of a
     * newsgroup is the segment of the name preceding the first dot (e.g.,
     * comp, alt, rec).  Only those newsgroups matching one of the
     * distributions or, in the case of NEWNEWS, an article in a newsgroup
     * matching one of the distributions, will be reported as a query result.
     * Adding distributions is purely optional.
     * <p>
     * @param distribution A distribution to add to the query.
     ***/
    public void addDistribution(String distribution)
    {
        if (__distributions != null)
            __distributions.append(',');
        else
            __distributions = new StringBuffer();
        __distributions.append(distribution);
    }

    /***
     * Return the NNTP query formatted date (year, month, day in the form
     * YYMMDD.
     * <p>
     * @return The NNTP query formatted date.
     ***/
    public String getDate()
    {
        return __date;
    }

    /***
     * Return the NNTP query formatted time (hour, minutes, seconds in the form
     * HHMMSS.
     * <p>
     * @return The NNTP query formatted time.
     ***/
    public String getTime()
    {
        return __time;
    }

    /***
     * Return whether or not the query date should be treated as GMT.
     * <p>
     * @return True if the query date is to be treated as GMT, false if not.
     ***/
    public boolean isGMT()
    {
        return __isGMT;
    }

    /***
     * Return the comma separated list of distributions.  This may be null
     * if there are no distributions.
     * <p>
     * @return The list of distributions, which may be null if no distributions
     *         have been specified.
     ***/
    public String getDistributions()
    {
        return (__distributions == null ? null : __distributions.toString());
    }

    /***
     * Return the comma separated list of newsgroups.  This may be null
     * if there are no newsgroups
     * <p>
     * @return The list of newsgroups, which may be null if no newsgroups
     *         have been specified.
     ***/
    public String getNewsgroups()
    {
        return (__newsgroups == null ? null : __newsgroups.toString());
    }
}
