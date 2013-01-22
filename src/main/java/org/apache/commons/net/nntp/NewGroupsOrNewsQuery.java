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

package org.apache.commons.net.nntp;

import java.util.Calendar;

/***
 * The NewGroupsOrNewsQuery class.  This is used to issue NNTP NEWGROUPS and
 * NEWNEWS queries, implemented by
 * {@link org.apache.commons.net.nntp.NNTPClient#listNewNewsgroups listNewNewsGroups }
 *  and
 * {@link org.apache.commons.net.nntp.NNTPClient#listNewNews listNewNews }
 *  respectively.  It prevents you from having to format
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
 * @see NNTPClient
 ***/

public final class NewGroupsOrNewsQuery
{
    private final String __date, __time;
    private StringBuffer __distributions;
    private StringBuffer __newsgroups;
    private final boolean __isGMT;


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
        StringBuilder buffer;

        __distributions = null;
        __newsgroups = null;
        __isGMT = gmt;

        buffer = new StringBuilder();

        // Get year
        num = date.get(Calendar.YEAR);
        str = Integer.toString(num);
        num = str.length();

        if (num >= 2) {
            buffer.append(str.substring(num - 2));
        } else {
            buffer.append("00");
        }

        // Get month
        num = date.get(Calendar.MONTH) + 1;
        str = Integer.toString(num);
        num = str.length();

        if (num == 1) {
            buffer.append('0');
            buffer.append(str);
        } else if (num == 2) {
            buffer.append(str);
        } else {
            buffer.append("01");
        }

        // Get day
        num = date.get(Calendar.DAY_OF_MONTH);
        str = Integer.toString(num);
        num = str.length();

        if (num == 1) {
            buffer.append('0');
            buffer.append(str);
        } else if (num == 2) {
            buffer.append(str);
        } else {
            buffer.append("01");
        }

        __date = buffer.toString();

        buffer.setLength(0);

        // Get hour
        num = date.get(Calendar.HOUR_OF_DAY);
        str = Integer.toString(num);
        num = str.length();

        if (num == 1) {
            buffer.append('0');
            buffer.append(str);
        } else if (num == 2) {
            buffer.append(str);
        } else {
            buffer.append("00");
        }

        // Get minutes
        num = date.get(Calendar.MINUTE);
        str = Integer.toString(num);
        num = str.length();

        if (num == 1) {
            buffer.append('0');
            buffer.append(str);
        } else if (num == 2) {
            buffer.append(str);
        } else {
            buffer.append("00");
        }


        // Get seconds
        num = date.get(Calendar.SECOND);
        str = Integer.toString(num);
        num = str.length();

        if (num == 1) {
            buffer.append('0');
            buffer.append(str);
        } else if (num == 2) {
            buffer.append(str);
        } else {
            buffer.append("00");
        }

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
        if (__newsgroups != null) {
            __newsgroups.append(',');
        } else {
            __newsgroups = new StringBuffer();
        }
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
        if (__distributions != null) {
            __distributions.append(',');
        } else {
            __distributions = new StringBuffer();
        }
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
