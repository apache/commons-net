package org.apache.commons.net.smtp;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SimpleSMTPHeaderTestCase {

    private SimpleSMTPHeader header;

    @Before
    public void setUp() {
        header = new SimpleSMTPHeader("from@here.invalid", "to@there.invalid", "Test email");
    }

    @Test
    public void testToString() {
        assertNotNull(header);
        // Note that the DotTerminatedMessageWriter converts LF to CRLF
        assertEquals("From: from@here.invalid\nTo: to@there.invalid\nSubject: Test email\n\n", header.toString());
    }

    @Test
    public void testToStringNoSubject() {
        SimpleSMTPHeader hdr = new SimpleSMTPHeader("from@here.invalid", "to@there.invalid", null);
        assertNotNull(hdr);
        // Note that the DotTerminatedMessageWriter converts LF to CRLF
        assertEquals("From: from@here.invalid\nTo: to@there.invalid\n\n", hdr.toString());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testToStringNoFrom() {
        new SimpleSMTPHeader(null, null, null);
    }

    @Test
    public void testToStringNoTo() {
        SimpleSMTPHeader hdr = new SimpleSMTPHeader("from@here.invalid", null, null);
        assertNotNull(hdr);
        // Note that the DotTerminatedMessageWriter converts LF to CRLF
        assertEquals("From: from@here.invalid\n\n", hdr.toString());
    }
}
