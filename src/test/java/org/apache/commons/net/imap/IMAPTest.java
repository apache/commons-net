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
 *
 */

package org.apache.commons.net.imap;

import org.junit.Assert;

import org.junit.Test;


public class IMAPTest {

    @Test
    public void checkGenerator() {
        // This test assumes:
        // - 26 letters in the generator alphabet
        // - the generator uses a fixed size tag
        IMAP imap = new IMAP();
        String initial = imap.generateCommandID();
        int expected = 1;
        for(int j=0; j < initial.length(); j++) {
            expected *= 26; // letters in alphabet
        }
        int i=0;
        boolean matched=false;
        while(i <= expected+10) { // don't loop forever, but allow it to pass go!
            i++;
            String s = imap.generateCommandID();
            matched = initial.equals(s);
            if (matched) { // we've wrapped around completely
                break;
            }
        }
        Assert.assertEquals(expected, i);
        Assert.assertTrue("Expected to see the original value again", matched);
    }
}
