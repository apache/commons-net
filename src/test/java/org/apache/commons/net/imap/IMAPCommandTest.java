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

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class IMAPCommandTest {

    private static Stream<Arguments> imapCommands() {
        return Stream.of(
                Arguments.of("CAPABILITY", IMAPCommand.CAPABILITY),
                Arguments.of("NOOP", IMAPCommand.NOOP),
                Arguments.of("LOGOUT", IMAPCommand.LOGOUT),
                Arguments.of("STARTTLS", IMAPCommand.STARTTLS),
                Arguments.of("AUTHENTICATE", IMAPCommand.AUTHENTICATE),
                Arguments.of("LOGIN", IMAPCommand.LOGIN),
                Arguments.of("XOAUTH", IMAPCommand.XOAUTH),
                Arguments.of("SELECT", IMAPCommand.SELECT),
                Arguments.of("EXAMINE", IMAPCommand.EXAMINE),
                Arguments.of("CREATE", IMAPCommand.CREATE),
                Arguments.of("DELETE", IMAPCommand.DELETE),
                Arguments.of("RENAME", IMAPCommand.RENAME),
                Arguments.of("SUBSCRIBE", IMAPCommand.SUBSCRIBE),
                Arguments.of("UNSUBSCRIBE", IMAPCommand.UNSUBSCRIBE),
                Arguments.of("LIST", IMAPCommand.LIST),
                Arguments.of("LSUB", IMAPCommand.LSUB),
                Arguments.of("STATUS", IMAPCommand.STATUS),
                Arguments.of("APPEND", IMAPCommand.APPEND),
                Arguments.of("CHECK", IMAPCommand.CHECK),
                Arguments.of("CLOSE", IMAPCommand.CLOSE),
                Arguments.of("EXPUNGE", IMAPCommand.EXPUNGE),
                Arguments.of("SEARCH", IMAPCommand.SEARCH),
                Arguments.of("FETCH", IMAPCommand.FETCH),
                Arguments.of("STORE", IMAPCommand.STORE),
                Arguments.of("COPY", IMAPCommand.COPY),
                Arguments.of("UID", IMAPCommand.UID)
        );
    }

    @ParameterizedTest(name = "Command for IMAPCommand::{1} should be `{0}`")
    @MethodSource("imapCommands")
    public void getCommand(final String expectedCommand, final IMAPCommand command) {
        assertEquals(expectedCommand, IMAPCommand.getCommand(command));
    }

}
