/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.net.pop3;

/**
 *
 * The POP3* tests all presume the existence of the following parameters: mailserver: localhost (running on the default port 110) account: username=test;
 * password=password account: username=alwaysempty; password=password. mail: At least four emails in the test account and zero emails in the alwaysempty account
 *
 * If this won't work for you, you can change these parameters in the TestSetupParameters class.
 *
 * The tests were originally run on a default installation of James. Your mileage may vary based on the POP3 server you run the tests against. Some servers are
 * more standards-compliant than others.
 */
public class POP3Constants {
    public static final String user = "test";
    public static final String emptyuser = "alwaysempty";
    public static final String password = "password";

    public static final String mailhost = "localhost";

    /** Cannot be instantiated. */
    private POP3Constants() {
    }
}
