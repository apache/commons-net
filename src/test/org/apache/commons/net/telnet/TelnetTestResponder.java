package org.apache.commons.net.telnet;

/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.InputStream;
import java.io.OutputStream;


/***
 * Simple stream responder.
 * Waits for strings on an input stream and answers
 * sending corresponfing strings on an output stream.
 * The reader runs in a separate thread.
 * <p>
 * @author Bruno D'Avanzo
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
     * @param inputs - Array of answers.
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
    public void run()
    {
        boolean result = false;
        byte buffer[] = new byte[32];
        long starttime = System.currentTimeMillis();

        try
        {
            String readbytes = new String();
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
