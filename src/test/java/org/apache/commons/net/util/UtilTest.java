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

package org.apache.commons.net.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;

import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.commons.net.io.Util;
import org.junit.Assert;
import org.junit.Test;

public class UtilTest {

    private final Writer dest = new CharArrayWriter();
    private final Reader source = new CharArrayReader(new char[]{'a'});
    private final InputStream src = new ByteArrayInputStream(new byte[]{'z'});
    private final OutputStream dst = new ByteArrayOutputStream();

    @Test
    public void testcloseQuietly() {
        Util.closeQuietly((Closeable) null);
        Util.closeQuietly((Socket) null);
    }

    @Test
    public void testReader0() throws Exception {
        long streamSize=0;
        int bufferSize=0;
        Util.copyReader(source, dest, bufferSize, streamSize, new CSL(1,1,streamSize));
    }

    @Test
    public void testReader1() throws Exception {
        long streamSize=0;
        int bufferSize=1;
        Util.copyReader(source, dest, bufferSize, streamSize, new CSL(1,1,streamSize));
    }

    @Test
    public void testReader_1() throws Exception {
        long streamSize=0;
        int bufferSize=-1;
        Util.copyReader(source, dest, bufferSize, streamSize, new CSL(1,1,streamSize));
    }

    @Test
    public void testStream0() throws Exception {
        long streamSize=0;
        int bufferSize=0;
        Util.copyStream(src, dst, bufferSize, streamSize, new CSL(1,1,streamSize));
    }

    @Test
    public void testStream1() throws Exception {
        long streamSize=0;
        int bufferSize=1;
        Util.copyStream(src, dst, bufferSize, streamSize, new CSL(1,1,streamSize));
    }

    @Test
    public void testStream_1() throws Exception {
        long streamSize=0;
        int bufferSize=-1;
        Util.copyStream(src, dst, bufferSize, streamSize, new CSL(1,1,streamSize));
    }

    @Test
    public void testNET550_Reader() throws Exception {
        final char[] buff = new char[]{'a', 'b', 'c', 'd'}; // must be multiple of 2
        final int bufflen = buff.length;
        {   // Check buffer size 1 processes in chunks of 1
            Reader rdr = new CharArrayReader(buff);
            final CSLtotal listener = new CSLtotal(bufflen, 1);
            Util.copyReader(rdr, dest, 1, 0, listener); // buffer size 1
            listener.checkExpected();
        }
        {   // Check bufsize 2 uses chunks of 2
            Reader rdr = new CharArrayReader(buff);
            final CSLtotal listener = new CSLtotal(bufflen, 2);
            Util.copyReader(rdr, dest, 2, 0, listener); // buffer size 2
            listener.checkExpected();
        }
        {   // Check bigger size reads the lot
            Reader rdr = new CharArrayReader(buff);
            final CSLtotal listener = new CSLtotal(bufflen, bufflen);
            Util.copyReader(rdr, dest, 20, 0, listener); // buffer size 20
            listener.checkExpected();
        }
        {   // Check negative size reads reads full amount
            Reader rdr = new CharArrayReader(buff);
            final CSLtotal listener = new CSLtotal(bufflen, bufflen);
            Util.copyReader(rdr, dest, -1, 0, listener); // buffer size -1
            listener.checkExpected();
        }
        {   // Check zero size reads reads full amount
            Reader rdr = new CharArrayReader(buff);
            final CSLtotal listener = new CSLtotal(bufflen, bufflen);
            Util.copyReader(rdr, dest, 0, 0, listener); // buffer size -1
            listener.checkExpected();
        }
    }

    @Test
    public void testNET550_Stream() throws Exception {
        final byte[] buff = new byte[]{'a', 'b', 'c', 'd'}; // must be multiple of 2
        final int bufflen = buff.length;
        {   // Check buffer size 1 processes in chunks of 1
            InputStream is = new ByteArrayInputStream(buff);
            final CSLtotal listener = new CSLtotal(bufflen, 1);
            Util.copyStream(is, dst, 1, 0, listener); // buffer size 1
            listener.checkExpected();
        }
        {   // Check bufsize 2 uses chunks of 2
            InputStream is = new ByteArrayInputStream(buff);
            final CSLtotal listener = new CSLtotal(bufflen, 2);
            Util.copyStream(is, dst, 2, 0, listener); // buffer size 2
            listener.checkExpected();
        }
        {   // Check bigger size reads the lot
            InputStream is = new ByteArrayInputStream(buff);
            final CSLtotal listener = new CSLtotal(bufflen, bufflen);
            Util.copyStream(is, dst, 20, 0, listener); // buffer size 20
            listener.checkExpected();
        }
        {   // Check negative size reads reads full amount
            InputStream is = new ByteArrayInputStream(buff);
            final CSLtotal listener = new CSLtotal(bufflen, bufflen);
            Util.copyStream(is, dst, -1, 0, listener); // buffer size -1
            listener.checkExpected();
        }
        {   // Check zero size reads reads full amount
            InputStream is = new ByteArrayInputStream(buff);
            final CSLtotal listener = new CSLtotal(bufflen, bufflen);
            Util.copyStream(is, dst, 0, 0, listener); // buffer size -1
            listener.checkExpected();
        }
    }

    static class CSL implements CopyStreamListener {

        final long expectedTotal;
        final int expectedBytes;
        final long expectedSize;
        CSL(long totalBytesTransferred, int bytesTransferred, long streamSize) {
            this.expectedTotal = totalBytesTransferred;
            this.expectedBytes = bytesTransferred;
            this.expectedSize = streamSize;
        }
        @Override
        public void bytesTransferred(CopyStreamEvent event) {
        }

        @Override
        public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
            Assert.assertEquals("Wrong total", expectedTotal, totalBytesTransferred);
            Assert.assertEquals("Wrong streamSize", expectedSize, streamSize);
            Assert.assertEquals("Wrong bytes", expectedBytes, bytesTransferred);
        }

    }

    // Class to check overall counts as well as batch size
    static class CSLtotal implements CopyStreamListener {

        final long expectedTotal;
        final long expectedBytes;
        volatile long totalBytesTransferredTotal;
        volatile long bytesTransferredTotal;

        CSLtotal(long totalBytesTransferred, long bytesTransferred) {
            this.expectedTotal = totalBytesTransferred;
            this.expectedBytes = bytesTransferred;
        }
        @Override
        public void bytesTransferred(CopyStreamEvent event) {
        }

        @Override
        public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
            Assert.assertEquals("Wrong bytes", expectedBytes, bytesTransferred);
            this.totalBytesTransferredTotal = totalBytesTransferred;
            this.bytesTransferredTotal += bytesTransferred;
        }

        void checkExpected() {
            Assert.assertEquals("Wrong totalBytesTransferred total", expectedTotal, totalBytesTransferredTotal);
            Assert.assertEquals("Total should equal sum of parts", totalBytesTransferredTotal, bytesTransferredTotal);
        }

    }
}
