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

package org.apache.commons.net.ftp;

import junit.framework.TestCase;

public class FTPClientTest extends TestCase {

    private static final String[] TESTS = {
        "257 /path/without/quotes",
            "/path/without/quotes",

        "257 \"/path/with/delimiting/quotes/without/commentary\"",
              "/path/with/delimiting/quotes/without/commentary",

        "257 \"/path/with/quotes\"\" /inside/but/without/commentary\"",
              "/path/with/quotes\" /inside/but/without/commentary",

        "257 \"/path/with/quotes\"\" /inside/string\" and with commentary",
              "/path/with/quotes\" /inside/string",

        "257 \"/path/with/quotes\"\" /inside/string\" and with commentary that also \"contains quotes\"",
              "/path/with/quotes\" /inside/string",

        "257 \"/path/without/trailing/quote", // invalid syntax, return all after reply code prefix
            "\"/path/without/trailing/quote",

        "257 root is current directory.", // NET-442
            "root is current directory.",

        "257 \"/\"", // NET-502
              "/",
    };

    public FTPClientTest(String name) {
        super(name);
    }

    public void testParseClient() {
        for(int i=0; i<TESTS.length; i+=2) {
            assertEquals("Failed to parse",TESTS[i+1], FTPClient.__parsePathname(TESTS[i]));
        }
    }

}
