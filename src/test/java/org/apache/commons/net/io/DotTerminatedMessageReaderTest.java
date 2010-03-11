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
	final static String SEP = System.getProperty("line.separator");
	
	public void testReadSimpleStringCrLfLineEnding() throws IOException {
		final String test = "Hello World!\r\n.\r\n";
		reader = new DotTerminatedMessageReader(new StringReader(test));
		reader.LS_CHARS = new char[]{'\r','\n'};
		
		int read = 0;
		while ((read = reader.read(buf)) != -1) {
			str.append(buf, 0, read);
		}
		
		assertEquals("Hello World!" + String.valueOf(reader.LS_CHARS), str.toString());
	}
	
	public void testReadSimpleStringLfLineEnding() throws IOException {
		final String test = "Hello World!\r\n.\r\n";
		reader = new DotTerminatedMessageReader(new StringReader(test));
		reader.LS_CHARS = new char[]{'\n'};
		
		int read = 0;
		while ((read = reader.read(buf)) != -1) {
			str.append(buf, 0, read);
		}
		
		assertEquals("Hello World!" + String.valueOf(reader.LS_CHARS), str.toString());
	}
	
	public void testEmbeddedNewlines() throws IOException {
		final String test = "Hello\r\nWorld\nA\rB\r\n.\r\n";
		reader = new DotTerminatedMessageReader(new StringReader(test));
		
		int read = 0;
		while ((read = reader.read(buf)) != -1) {
			str.append(buf, 0, read);
		}
		
		assertEquals(str.toString(), "Hello\r\nWorld\nA\rB" + SEP);
	}
	
	public void testDoubleCrBeforeDot() throws IOException {
		final String test = "Hello World!\r\r\n.\r\n";
		reader = new DotTerminatedMessageReader(new StringReader(test));
		
		int read = 0;
		while ((read = reader.read(buf)) != -1) {
			str.append(buf, 0, read);
		}
		
		assertEquals("Hello World!\r" + SEP,str.toString());
	}

}
