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
package org.apache.commons.net.telnet;

import java.io.InputStream;
import java.io.OutputStream;


/***
 * Simple stream responder.
 * Waits for strings on an input stream and answers
 * sending corresponfing strings on an output stream.
 * The reader runs in a separate thread.
 ***/
public class TelnetTestResponder implements Runnable
{
    InputStream _is;
    OutputStream _os;
    String _inputs[], _outputs[];
    long _timeout;

    /***
     * Constructor.
     * Starts a new thread for the reader.
     * <p>
     * @param is - InputStream on which to read.
     * @param os - OutputStream on which to answer.
     * @param inputs - Array of waited for Strings.
     * @param outputs - Array of answers.
     * @param timeout - milliseconds
     ***/
    public TelnetTestResponder(InputStream is, OutputStream os, String inputs[], String outputs[], long timeout)
    {
        _is = is;
        _os = os;
        _timeout = timeout;
        _inputs = inputs;
        _outputs = outputs;
        Thread reader = new Thread (this);

        reader.start();
    }

    /***
     * Runs the responder
     ***/
    @Override
    public void run()
    {
        boolean result = false;
        byte buffer[] = new byte[32];
        long starttime = System.currentTimeMillis();

        try
        {
            String readbytes = "";
            while(!result &&
                  ((System.currentTimeMillis() - starttime) < _timeout))
            {
                if(_is.available() > 0)
                {
                    int ret_read = _is.read(buffer);
                    readbytes = readbytes + new String(buffer, 0, ret_read);

                    for(int ii=0; ii<_inputs.length; ii++)
                    {
                        if(readbytes.indexOf(_inputs[ii]) >= 0)
                        {
                            Thread.sleep(1000 * ii);
                            _os.write(_outputs[ii].getBytes());
                            result = true;
                        }
                    }
                }
                else
                {
                    Thread.sleep(500);
                }
            }

        }
        catch (Exception e)
        {
            System.err.println("Error while waiting endstring. " + e.getMessage());
        }
    }
}
