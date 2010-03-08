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

package org.apache.commons.net.io;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;

public class DotTerminatedMessageReaderTest extends TestCase {
	
	private DotTerminatedMessageReader reader;
	private StringBuilder str = new StringBuilder();
	private char[] buf = new char[64];
	
	public void testReadSimpleString() throws IOException {
		final String test = "Hello World!\r\n.\r\n";
		reader = new DotTerminatedMessageReader(new StringReader(test));
		
		int read = 0;
		while ((read = reader.read(buf)) != -1) {
			str.append(buf, 0, read-1);
		}
		
		assertEquals(str.toString(), "Hello World!");
	}
	
	public void testEmbeddedNewlines() throws IOException {
		final String test = "Hello\r\nWorld\nA\rB\r\n.\r\n";
		reader = new DotTerminatedMessageReader(new StringReader(test));
		
		int read = 0;
		while ((read = reader.read(buf)) != -1) {
			str.append(buf, 0, read-1);
		}
		
		assertEquals(str.toString(), "Hello\nWorld\nA\rB");
	}
	
	public void testDoubleCrBeforeDot() throws IOException {
		final String test = "Hello World!\r\r\n.\r\n";
		reader = new DotTerminatedMessageReader(new StringReader(test));
		
		int read = 0;
		while ((read = reader.read(buf)) != -1) {
			str.append(buf, 0, read-1);
		}
		
		assertEquals("Hello World!\r",str.toString());
	}

}
