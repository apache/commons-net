/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.imap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.stream.Stream;

import org.apache.commons.net.MalformedServerReplyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class IMAPReplyTest {

    private static Stream<String> invalidLiteralCommands() {
        return Stream.of(
                "",
                "{",
                "}",
                "{}",
                "{foobar}",
                "STORE +FLAGS.SILENT \\DELETED {",
                "STORE +FLAGS.SILENT \\DELETED }",
                "STORE +FLAGS.SILENT \\DELETED {-1}",
                "STORE +FLAGS.SILENT \\DELETED {-10}",
                "STORE +FLAGS.SILENT \\DELETED {-2147483648}"
        );
    }

    private static Stream<Arguments> literalCommands() {
        return Stream.of(
                Arguments.of(310, "A003 APPEND saved-messages (\\Seen) {310}"),
                Arguments.of(6, "A284 SEARCH CHARSET UTF-8 TEXT {6}"),
                Arguments.of(7, "FRED FOOBAR {7}"),
                Arguments.of(102856, "A044 BLURDYBLOOP {102856}"),
                Arguments.of(342, "* 12 FETCH (BODY[HEADER] {342}"),
                Arguments.of(0, "X999 LOGIN {0}"),
                Arguments.of(Integer.MAX_VALUE, "X999 LOGIN {2147483647}")
        );
    }

    @Test
    public void getReplyCodeBadLine() throws IOException {
        final String badLine = "A044 BAD No such command as \"FOOBAR\"";
        assertEquals(IMAPReply.BAD, IMAPReply.getReplyCode(badLine));
    }

    @Test
    public void getReplyCodeContinuationLine() throws IOException {
        final String continuationLine = "+ Ready for additional command text";
        assertEquals(IMAPReply.CONT, IMAPReply.getReplyCode(continuationLine));
    }

    @Test
    public void getReplyCodeMalformedLine() {
        final String malformedTaggedLine = "A064 FOO-BAR 0";
        final MalformedServerReplyException replyException = assertThrows(MalformedServerReplyException.class, () -> IMAPReply.getReplyCode(malformedTaggedLine));
        assertEquals("Received unexpected IMAP protocol response from server: 'A064 FOO-BAR 0'.", replyException.getMessage());
    }

    @Test
    public void getReplyCodeNoLine() throws IOException {
        final String noLine = "A223 NO COPY failed: disk is full";
        assertEquals(IMAPReply.NO, IMAPReply.getReplyCode(noLine));
    }

    @Test
    public void getReplyCodeOkLine() throws IOException {
        final String okLine = "A001 OK LOGIN completed";
        assertEquals(IMAPReply.OK, IMAPReply.getReplyCode(okLine));
    }

    @Test
    public void getUntaggedReplyCodeBadLine() throws IOException {
        final String badLine = "* BAD Empty command line";
        assertEquals(IMAPReply.BAD, IMAPReply.getUntaggedReplyCode(badLine));
    }

    @Test
    public void getUntaggedReplyCodeContinuationLine() throws IOException {
        final String continuationLine = "+ Ready for additional command text";
        assertEquals(IMAPReply.CONT, IMAPReply.getUntaggedReplyCode(continuationLine));
    }

    @Test
    public void getUntaggedReplyCodeMalformedLine() {
        // invalid experimental comm response (missing X prefix)
        final String malformedUntaggedLine = "* FOO-BAR hello-world";
        final MalformedServerReplyException replyException = assertThrows(MalformedServerReplyException.class, () -> IMAPReply.getUntaggedReplyCode(malformedUntaggedLine));
        assertEquals("Received unexpected IMAP protocol response from server: '* FOO-BAR hello-world'.", replyException.getMessage());
    }

    @Test
    public void getUntaggedReplyCodeNoLine() throws IOException {
        final String noLine = "* NO Disk is 98% full, please delete unnecessary data";
        assertEquals(IMAPReply.NO, IMAPReply.getUntaggedReplyCode(noLine));
    }

    @Test
    public void getUntaggedReplyCodeOkLine() throws IOException {
        final String okLine = "* OK Salvage successful, no data lost";
        assertEquals(IMAPReply.OK, IMAPReply.getUntaggedReplyCode(okLine));
    }

    @Test
    public void isContinuationReplyCode() {
        final int replyCode = 3;
        assertTrue(IMAPReply.isContinuation(replyCode));
    }

    @Test
    public void isContinuationReplyCodeInvalidCode() {
        final int invalidContinuationReplyCode = 1;
        assertFalse(IMAPReply.isContinuation(invalidContinuationReplyCode));
    }

    @Test
    public void isContinuationReplyLine() {
        final String replyLine = "+FLAGS completed";
        assertTrue(IMAPReply.isContinuation(replyLine));
    }

    @Test
    public void isContinuationReplyLineInvalidLine() {
        final String invalidContinuationReplyLine = "* 22 EXPUNGE";
        assertFalse(IMAPReply.isContinuation(invalidContinuationReplyLine));
    }

    @Test
    public void isSuccessReplyCode() {
        final int successfulReplyCode = 0;
        assertTrue(IMAPReply.isSuccess(successfulReplyCode));
    }

    @Test
    public void isSuccessReplyCodeUnsuccessfulCode() {
        final int unsuccessfulReplyCode = 2;
        assertFalse(IMAPReply.isSuccess(unsuccessfulReplyCode));
    }

    @Test
    public void isUntaggedReplyLine() {
        final String replyLine = "* 18 EXISTS";
        assertTrue(IMAPReply.isUntagged(replyLine));
    }

    @Test
    public void isUntaggedReplyLineInvalidLine() {
        final String taggedLine = "a001 OK LOGOUT completed";
        assertFalse(IMAPReply.isUntagged(taggedLine));
    }

    @ParameterizedTest(name = "reply line `{1}` contains literal {0}")
    @MethodSource("literalCommands")
    public void literalCount(final int expectedLiteral, final String replyLine) {
        assertEquals(expectedLiteral, IMAPReply.literalCount(replyLine));
    }

    @ParameterizedTest(name = "reply line `{0}` does not contain any literal")
    @MethodSource("invalidLiteralCommands")
    public void literalCountInvalid(final String replyLine) {
        assertEquals(-1, IMAPReply.literalCount(replyLine));
    }

}
