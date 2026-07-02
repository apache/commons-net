/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

class DotTerminatedMessageWriterTest {

    @Test
    void testDoubleLeadingDotOnFirstLine() throws IOException {
        // A leading dot on the first line must be doubled, otherwise it is
        // indistinguishable from the message terminator.
        assertEquals("..\r\n.\r\n", write("."));
        assertEquals("..hidden\r\n.\r\n", write(".hidden"));
    }

    @Test
    void testDoubleLeadingDotOnLaterLine() throws IOException {
        assertEquals("a\r\n..b\r\n.\r\n", write("a\r\n.b"));
    }

    @Test
    void testFirstLineDotCannotTerminateEarly() throws IOException {
        // "." CR LF as the first line would otherwise end the message and let the
        // following text be read as protocol commands.
        assertEquals("..\r\nINJECT\r\n.\r\n", write(".\r\nINJECT"));
    }

    private String write(final String message) throws IOException {
        final StringWriter sw = new StringWriter();
        try (DotTerminatedMessageWriter writer = new DotTerminatedMessageWriter(sw)) {
            writer.write(message);
        }
        return sw.toString();
    }
}
