package org.apache.commons.net.ftp.ftp2.parser;

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

/**
 * UnixFTPEntryParserTest.java
 * Tests the UnixFTPEntryParser
 *
 * @author <a href="mailto:scohen@stevecoh1@attbi.com">Steve Cohen</a>
 * @versionn $Id: UnixFTPEntryParserTest.java,v 1.1 2002/04/29 03:55:32 brekke Exp $
 */
public class UnixFTPEntryParserTest extends FTPParseTestFramework
{
    private static final String[] goodsamples = {
                "drwxr-xr-x   2 root     root         4096 Mar  2 15:13 zxbox",
                "drwxr-xr-x   2 root     root         4096 Aug 24  2001 zxjdbc",
                "drwxr-xr-x   2 root     root         4096 Jan  4 00:03 zziplib",
                "drwxr-xr-x   2 root     99           4096 Feb 23  2001 zzplayer",
                "drwxr-xr-x   2 root     root         4096 Aug  6  2001 zztpp",
                "-rw-r--r--   1 14       staff       80284 Aug 22  2000 zxJDBC-1.2.3.tar.gz",
                "-rw-r--r--   1 14       staff      119926 Aug 22  2000 zxJDBC-1.2.3.zip",
                "-rw-r--r--   1 ftp      nogroup     83853 Jan 22  2001 zxJDBC-1.2.4.tar.gz",
                "-rw-r--r--   1 ftp      nogroup    126552 Jan 22  2001 zxJDBC-1.2.4.zip",
                "-rw-r--r--   1 root     root       111325 Apr 27  2001 zxJDBC-2.0.1b1.tar.gz",
                "-rw-r--r--   1 root     root       190144 Apr 27  2001 zxJDBC-2.0.1b1.zip"
            };
    private static final String[] badsamples = {
                "zrwxr-xr-x   2 root     root         4096 Mar  2 15:13 zxbox",
                "dxrwr-xr-x   2 root     root         4096 Aug 24  2001 zxjdbc",
                "drwxr-xr-x   2 root     root         4096 Jam  4 00:03 zziplib",
                "drwxr-xr-x   2 root     99           4096 Feb 23 30:01 zzplayer",
                "drwxr-xr-x   2 root     root         4096 Aug 36  2001 zztpp",
                "-rw-r--r--   1 14       staff       80284 Aug 22  zxJDBC-1.2.3.tar.gz",
                "-rw-r--r--   1 14       staff      119:26 Aug 22  2000 zxJDBC-1.2.3.zip",
                "-rw-r--r--   1 ftp      no group    83853 Jan 22  2001 zxJDBC-1.2.4.tar.gz",
                "-rw-r--r--   1ftp       nogroup    126552 Jan 22  2001 zxJDBC-1.2.4.zip",
                "-rw-r--r--   1 root     root       111325 Apr -7 18:79 zxJDBC-2.0.1b1.tar.gz",
            };




    public UnixFTPEntryParserTest (String name)
    {
        super(name, new UnixFTPEntryParser());
    }

    public void testPositive() throws Exception
    {
        _testPositive(goodsamples);
    }
    public void testNegative() throws Exception
    {
        _testNegative(badsamples);

    }
}
