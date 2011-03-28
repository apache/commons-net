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

import junit.framework.Assert;

import org.junit.Test;


public class IMAPTest {

    @Test
    public void checkGenerator() {
        IMAP imap = new IMAP();
        String initial = imap.generateCommandID();
        int expected = 1;
        for(int j=0; j < initial.length(); j++) {
            expected *= 26; // letters in alphabet
        }
        int i=0;
        while(true) {
            i++;
            String s = imap.generateCommandID();
            if (initial.equals(s)) { // we've wrapped around completely
                break;
            }
            if (i > expected) {
                break;
            }
        }
        Assert.assertEquals(expected, i);
    }
}
