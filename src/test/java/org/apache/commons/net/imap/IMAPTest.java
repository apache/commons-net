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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class IMAPTest {

    private static Stream<String> mailboxNamesToBeQuoted() {
        return Stream.of(
                "",
                " ",
                "\"",
                "\"\"",
                "\\/  ",
                "Hello\", ",
                "\" World!",
                "Hello\",\" World!"
        );
    }

    @Test
    public void checkGenerator() {
        // This test assumes:
        // - 26 letters in the generator alphabet
        // - the generator uses a fixed size tag
        final IMAP imap = new IMAP();
        final String initial = imap.generateCommandID();
        int expected = 1;
        for (int j = 0; j < initial.length(); j++) {
            expected *= 26; // letters in alphabet
        }
        int i = 0;
        boolean matched = false;
        while (i <= expected + 10) { // don't loop forever, but allow it to pass go!
            i++;
            final String s = imap.generateCommandID();
            matched = initial.equals(s);
            if (matched) { // we've wrapped around completely
                break;
            }
        }
        assertEquals(expected, i);
        assertTrue(matched, "Expected to see the original value again");
    }

    @Test
    public void constructDefaultIMAP() {
        final IMAP imap = new IMAP();
        assertAll(
                () -> assertEquals(IMAP.DEFAULT_PORT, imap.getDefaultPort()),
                () -> assertEquals(IMAP.IMAPState.DISCONNECTED_STATE, imap.getState()),
                () -> assertEquals(0, imap.getReplyStrings().length)
        );
    }

    @ParameterizedTest(name = "String `{0}` should be quoted")
    @MethodSource("mailboxNamesToBeQuoted")
    public void quoteMailboxName(final String input) {
        final String quotedMailboxName = IMAP.quoteMailboxName(input);
        assertAll(
                () -> assertTrue(quotedMailboxName.startsWith("\""), "quoted string should start with quotation mark"),
                () -> assertTrue(quotedMailboxName.endsWith("\""), "quoted string should end with quotation mark")
        );
    }

    @Test
    public void quoteMailboxNameNullInput() {
        assertNull(IMAP.quoteMailboxName(null));
    }

    @Test
    public void quoteMailboxNoQuotingIfNoSpacePresent() {
        final String stringToQuote = "Foobar\"";
        assertEquals(stringToQuote, IMAP.quoteMailboxName(stringToQuote));
    }

    @Test
    public void trueChunkListener() {
        assertTrue(IMAP.TRUE_CHUNK_LISTENER.chunkReceived(new IMAP()));
    }

}
