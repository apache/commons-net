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
package org.apache.commons.net.telnet;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Simple stream responder. Waits for strings on an input stream and answers sending corresponfing strings on an output stream. The reader runs in a separate
 * thread.
 */
class TelnetTestResponder implements Runnable {
    InputStream in;
    OutputStream out;
    String inputs[];
    String outputs[];
    long timeout;

    /**
     * Constructs a new instance. Starts a new thread for the reader.
     * <p>
     *
     * @param is        InputStream on which to read.
     * @param os        OutputStream on which to answer.
     * @param inputs    Array of waited for Strings.
     * @param outputs   Array of answers.
     * @param timeout   milliseconds
     */
    public TelnetTestResponder(final InputStream is, final OutputStream os, final String inputs[], final String outputs[], final long timeout) {
        this.in = is;
        this.out = os;
        this.timeout = timeout;
        this.inputs = inputs;
        this.outputs = outputs;
        final Thread reader = new Thread(this);

        reader.start();
    }

    /**
     * Runs the responder
     */
    @Override
    public void run() {
        boolean result = false;
        final byte[] buffer = new byte[32];
        final long starttime = System.currentTimeMillis();

        try {
            final StringBuilder readbytes = new StringBuilder();
            while (!result && System.currentTimeMillis() - starttime < timeout) {
                if (in.available() > 0) {
                    final int ret_read = in.read(buffer);
                    readbytes.append(new String(buffer, 0, ret_read));

                    for (int ii = 0; ii < inputs.length; ii++) {
                        if (readbytes.indexOf(inputs[ii]) >= 0) {
                            Thread.sleep(1000 * ii);
                            out.write(outputs[ii].getBytes());
                            result = true;
                        }
                    }
                } else {
                    Thread.sleep(500);
                }
            }

        } catch (final Exception e) {
            System.err.println("Error while waiting endstring. " + e.getMessage());
        }
    }
}
