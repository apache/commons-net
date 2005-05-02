/*
 * Copyright 2001-2005 The Apache Software Foundation
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
package org.apache.commons.net.ftp;

import org.apache.commons.net.ftp.parser.RegexFTPFileEntryParserImpl;

/**
 * This abstract class implements both the older FTPFileListParser and
 * newer FTPFileEntryParser interfaces with default functionality.
 * All the classes in the parser subpackage inherit from this.
 *
 * @author Steve Cohen <scohen@apache.org>
 * @see org.apache.commons.net.ftp.parser.RegexFTPFileEntryParserImpl
 * @deprecated This class is deprecated as of version 1.2 and will be
 *             removed in version 2.0 --
 *             org.apache.commons.net.ftp.RegexFTPFileEntryParserImpl is its
 *             designated replacement.  Class has been renamed, entire
 *             implemenation is in RegexFTPFileEntryParserImpl.
 *
 */
public abstract class FTPFileListParserImpl
    extends RegexFTPFileEntryParserImpl
{
    /**
     * The constructor for a FTPFileListParserImpl object.
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

    public FTPFileListParserImpl(String regex) {
        super(regex);
    }


}

/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
