/*
 * Created on Apr 5, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.commons.net.ftp.parser;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author scohen
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite =
            new TestSuite("Test for org.apache.commons.net.ftp.parser");
        //$JUnit-BEGIN$
        suite.addTest(FTPTimestampParserImplTest.suite());
        suite.addTest(OS2FTPEntryParserTest.suite());
        suite.addTest(VMSFTPEntryParserTest.suite());
        suite.addTest(UnixFTPEntryParserTest.suite());
        suite.addTestSuite(DefaultFTPFileEntryParserFactoryTest.class);
        suite.addTest(EnterpriseUnixFTPEntryParserTest.suite());
        suite.addTest(OS400FTPEntryParserTest.suite());
        suite.addTest(NTFTPEntryParserTest.suite());
        //$JUnit-END$
        return suite;
    }
}
