package org.apache.commons.net.telnet;

/* ====================================================================
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

/***
 *
 * <p>
 *
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @author Bruno D'Avanzo
 ***/


final class TelnetInputStream extends BufferedInputStream implements Runnable
{
    static final int _STATE_DATA = 0, _STATE_IAC = 1, _STATE_WILL = 2,
                     _STATE_WONT = 3, _STATE_DO = 4, _STATE_DONT = 5,
                     _STATE_SB = 6, _STATE_SE = 7, _STATE_CR = 8, _STATE_IAC_SB = 9;

    private boolean __hasReachedEOF, __isClosed;
    private boolean __readIsWaiting;
    private int __receiveState, __queueHead, __queueTail, __bytesAvailable;
    private int[] __queue;
    private TelnetClient __client;
    private Thread __thread;
    private IOException __ioException;

    /* TERMINAL-TYPE option (start)*/
    private int __suboption[] = new int[256];
    private int __suboption_count = 0;
    /* TERMINAL-TYPE option (end)*/

    private boolean _ayt_flag = false;
    TelnetInputStream(InputStream input, TelnetClient client)
    {
        super(input);
        __client = client;
        __receiveState = _STATE_DATA;
        __isClosed = true;
        __hasReachedEOF = false;
        // Make it 1025, because when full, one slot will go unused, and we
        // want a 1024 byte buffer just to have a round number (base 2 that is)
        //__queue         = new int[1025];
        __queue = new int[2049];
        __queueHead = 0;
        __queueTail = 0;
        __bytesAvailable = 0;
        __ioException = null;
        __readIsWaiting = false;
        __thread = new Thread(this);
    }

    void _start()
    {
        int priority;
        __isClosed = false;
        // Need to set a higher priority in case JVM does not use pre-emptive
        // threads.  This should prevent scheduler induced deadlock (rather than
        // deadlock caused by a bug in this code).
        priority = Thread.currentThread().getPriority() + 1;
        if (priority > Thread.MAX_PRIORITY)
            priority = Thread.MAX_PRIORITY;
        __thread.setPriority(priority);
        __thread.setDaemon(true);
        __thread.start();
    }


    // synchronized(__client) critical sections are to protect against
    // TelnetOutputStream writing through the telnet client at same time
    // as a processDo/Will/etc. command invoked from TelnetInputStream
    // tries to write.
    private int __read() throws IOException
    {
        int ch;

_loop:
        while (true)
        {
            // Exit only when we reach end of stream.
            if ((ch = super.read()) < 0)
                return -1;

            ch = (ch & 0xff);

            /* Code Section added for supporting AYT (start)*/
            synchronized (__client)
            {
                __client._processAYTResponse();
            }
            /* Code Section added for supporting AYT (end)*/

            /* Code Section added for supporting spystreams (start)*/
            __client._spyRead(ch);
            /* Code Section added for supporting spystreams (end)*/

_mainSwitch:
            switch (__receiveState)
            {

            case _STATE_CR:
                if (ch == '\0')
                {
                    // Strip null
                    continue;
                }
                // How do we handle newline after cr?
                //  else if (ch == '\n' && _requestedDont(TelnetOption.ECHO) &&

                // Handle as normal data by falling through to _STATE_DATA case

            case _STATE_DATA:
                if (ch == TelnetCommand.IAC)
                {
                    __receiveState = _STATE_IAC;
                    continue;
                }


                if (ch == '\r')
                {
                    synchronized (__client)
                    {
                        if (__client._requestedDont(TelnetOption.BINARY))
                            __receiveState = _STATE_CR;
                        else
                            __receiveState = _STATE_DATA;
                    }
                }
                else
                    __receiveState = _STATE_DATA;
                break;

            case _STATE_IAC:
                switch (ch)
                {
                case TelnetCommand.WILL:
                    __receiveState = _STATE_WILL;
                    continue;
                case TelnetCommand.WONT:
                    __receiveState = _STATE_WONT;
                    continue;
                case TelnetCommand.DO:
                    __receiveState = _STATE_DO;
                    continue;
                case TelnetCommand.DONT:
                    __receiveState = _STATE_DONT;
                    continue;
                /* TERMINAL-TYPE option (start)*/
                case TelnetCommand.SB:
                    __suboption_count = 0;
                    __receiveState = _STATE_SB;
                    continue;
                /* TERMINAL-TYPE option (end)*/
                case TelnetCommand.IAC:
                    __receiveState = _STATE_DATA;
                    break;
                default:
                    break;
                }
                __receiveState = _STATE_DATA;
                continue;
            case _STATE_WILL:
                synchronized (__client)
                {
                    __client._processWill(ch);
                    __client._flushOutputStream();
                }
                __receiveState = _STATE_DATA;
                continue;
            case _STATE_WONT:
                synchronized (__client)
                {
                    __client._processWont(ch);
                    __client._flushOutputStream();
                }
                __receiveState = _STATE_DATA;
                continue;
            case _STATE_DO:
                synchronized (__client)
                {
                    __client._processDo(ch);
                    __client._flushOutputStream();
                }
                __receiveState = _STATE_DATA;
                continue;
            case _STATE_DONT:
                synchronized (__client)
                {
                    __client._processDont(ch);
                    __client._flushOutputStream();
                }
                __receiveState = _STATE_DATA;
                continue;
            /* TERMINAL-TYPE option (start)*/
            case _STATE_SB:
                switch (ch)
                {
                case TelnetCommand.IAC:
                    __receiveState = _STATE_IAC_SB;
                    continue;
                default:
                    // store suboption char
                    __suboption[__suboption_count++] = ch;
                    break;
                }
                __receiveState = _STATE_SB;
                continue;
            case _STATE_IAC_SB:
                switch (ch)
                {
                case TelnetCommand.SE:
                    synchronized (__client)
                    {
                        __client._processSuboption(__suboption, __suboption_count);
                        __client._flushOutputStream();
                    }
                    __receiveState = _STATE_DATA;
                    continue;
                default:
                    __receiveState = _STATE_SB;
                    break;
                }
                __receiveState = _STATE_DATA;
                continue;
            /* TERMINAL-TYPE option (end)*/
            }

            break;
        }

        return ch;
    }



    public int read() throws IOException
    {
        // Critical section because we're altering __bytesAvailable,
        // __queueHead, and the contents of _queue in addition to
        // testing value of __hasReachedEOF.
        synchronized (__queue)
        {

            while (true)
            {
                if (__ioException != null)
                {
                    IOException e;
                    e = __ioException;
                    __ioException = null;
                    throw e;
                }

                if (__bytesAvailable == 0)
                {
                    // Return -1 if at end of file
                    if (__hasReachedEOF)
                        return -1;

                    // Otherwise, we have to wait for queue to get something
                    __queue.notify();
                    try
                    {
                        __readIsWaiting = true;
                        __queue.wait();
                        __readIsWaiting = false;
                    }
                    catch (InterruptedException e)
                    {
                        throw new IOException("Fatal thread interruption during read.");
                    }
                    continue;
                }
                else
                {
                    int ch;

                    ch = __queue[__queueHead];

                    if (++__queueHead >= __queue.length)
                        __queueHead = 0;

                    --__bytesAvailable;

                    return ch;
                }
            }
        }
    }


    /***
     * Reads the next number of bytes from the stream into an array and
     * returns the number of bytes read.  Returns -1 if the end of the
     * stream has been reached.
     * <p>
     * @param buffer  The byte array in which to store the data.
     * @return The number of bytes read. Returns -1 if the
     *          end of the message has been reached.
     * @exception IOException If an error occurs in reading the underlying
     *            stream.
     ***/
    public int read(byte buffer[]) throws IOException
    {
        return read(buffer, 0, buffer.length);
    }


    /***
     * Reads the next number of bytes from the stream into an array and returns
     * the number of bytes read.  Returns -1 if the end of the
     * message has been reached.  The characters are stored in the array
     * starting from the given offset and up to the length specified.
     * <p>
     * @param buffer The byte array in which to store the data.
     * @param offset  The offset into the array at which to start storing data.
     * @param length   The number of bytes to read.
     * @return The number of bytes read. Returns -1 if the
     *          end of the stream has been reached.
     * @exception IOException If an error occurs while reading the underlying
     *            stream.
     ***/
    public int read(byte buffer[], int offset, int length) throws IOException
    {
        int ch, off;

        if (length < 1)
            return 0;

        // Critical section because run() may change __bytesAvailable
        synchronized (__queue)
        {
            if (length > __bytesAvailable)
                length = __bytesAvailable;
        }

        if ((ch = read()) == -1)
            return -1;

        off = offset;

        do
        {
            buffer[offset++] = (byte)ch;
        }
        while (--length > 0 && (ch = read()) != -1);

        //__client._spyRead(buffer, off, offset - off);
        return (offset - off);
    }


    /*** Returns false.  Mark is not supported. ***/
    public boolean markSupported()
    {
        return false;
    }

    public int available() throws IOException
    {
        // Critical section because run() may change __bytesAvailable
        synchronized (__queue)
        {
            return __bytesAvailable;
        }
    }


    // Cannot be synchronized.  Will cause deadlock if run() is blocked
    // in read because BufferedInputStream read() is synchronized.
    public void close() throws IOException
    {
        // Completely disregard the fact thread may still be running.
        // We can't afford to block on this close by waiting for
        // thread to terminate because few if any JVM's will actually
        // interrupt a system read() from the interrupt() method.
        super.close();

        synchronized (__queue)
        {
            __hasReachedEOF = true;
            __isClosed = true;

            if (__thread.isAlive())
            {
                __thread.interrupt();
            }

            __queue.notifyAll();
        }
    }

    public void run()
    {
        int ch;

        try
        {
_outerLoop:
            while (!__isClosed)
            {
                try
                {
                    if ((ch = __read()) < 0)
                        break;
                }
                catch (InterruptedIOException e)
                {
                    synchronized (__queue)
                    {
                        __ioException = e;
                        __queue.notifyAll();
                        try
                        {
                            __queue.wait(100);
                        }
                        catch (InterruptedException interrupted)
                        {
                            if (__isClosed)
                                break _outerLoop;
                        }
                        continue;
                    }
                }

                // Critical section because we're altering __bytesAvailable,
                // __queueTail, and the contents of _queue.
                synchronized (__queue)
                {
                    while (__bytesAvailable >= __queue.length - 1)
                    {
                        __queue.notify();
                        try
                        {
                            __queue.wait(100);
                        }
                        catch (InterruptedException e)
                        {
                            if (__isClosed)
                                break _outerLoop;
                        }
                    }

                    // Need to do this in case we're not full, but block on a read
                    if (__readIsWaiting)
                    {
                        __queue.notify();
                    }

                    __queue[__queueTail] = ch;
                    ++__bytesAvailable;

                    if (++__queueTail >= __queue.length)
                        __queueTail = 0;
                }
            }
        }
        catch (IOException e)
        {
            synchronized (__queue)
            {
                __ioException = e;
            }
        }

        synchronized (__queue)
        {
            __isClosed = true; // Possibly redundant
            __hasReachedEOF = true;
            __queue.notify();
        }
    }
}
