/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.nntp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link SimpleNNTPHeader}.
 */
class SimpleNNTPHeaderTestCase {

    @Test
    void testRejectCarriageReturnInConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleNNTPHeader("foobar@foo.invalid", "Subject\rInjected: header"));
    }

    @Test
    void testRejectLineFeedInAddHeaderField() {
        final SimpleNNTPHeader header = new SimpleNNTPHeader("foobar@foo.invalid", "Just testing");
        assertThrows(IllegalArgumentException.class, () -> header.addHeaderField("Organization", "Foobar, Inc.\nInjected: header"));
    }

    @Test
    void testRejectLineFeedInAddNewsgroup() {
        final SimpleNNTPHeader header = new SimpleNNTPHeader("foobar@foo.invalid", "Just testing");
        assertThrows(IllegalArgumentException.class, () -> header.addNewsgroup("alt.test\nInjected: header"));
    }

    @Test
    void testRejectLineFeedInConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new SimpleNNTPHeader("foobar@foo.invalid", "Subject\nInjected: header"));
    }

    @Test
    void testToString() {
        final SimpleNNTPHeader header = new SimpleNNTPHeader("foobar@foo.invalid", "Just testing");
        header.addNewsgroup("alt.test");
        header.addHeaderField("Organization", "Foobar, Inc.");
        assertEquals("From: foobar@foo.invalid\nNewsgroups: alt.test\nSubject: Just testing\nOrganization: Foobar, Inc.\n\n", header.toString());
    }
}
