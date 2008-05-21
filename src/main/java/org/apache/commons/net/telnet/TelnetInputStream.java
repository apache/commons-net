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

    private boolean __threaded;

    TelnetInputStream(InputStream input, TelnetClient client,
                      boolean readerThread)
    {
        super(input);
        __client = client;
        __receiveState = _STATE_DATA;
        __isClosed = true;
        __hasReachedEOF = false;
        // Make it 2049, because when full, one slot will go unused, and we
        // want a 2048 byte buffer just to have a round number (base 2 that is)
        __queue = new int[2049];
        __queueHead = 0;
        __queueTail = 0;
        __bytesAvailable = 0;
        __ioException = null;
        __readIsWaiting = false;
        __threaded = false;
        if(readerThread)
            __thread = new Thread(this);
        else
            __thread = null;
    }

    TelnetInputStream(InputStream input, TelnetClient client) {
        this(input, client, true);
    }

    void _start()
    {
        if(__thread == null)
            return;

        int priority;
        __isClosed = false;
        // TODO remove this
        // Need to set a higher priority in case JVM does not use pre-emptive
        // threads.  This should prevent scheduler induced deadlock (rather than
        // deadlock caused by a bug in this code).
        priority = Thread.currentThread().getPriority() + 1;
        if (priority > Thread.MAX_PRIORITY)
            priority = Thread.MAX_PRIORITY;
        __thread.setPriority(priority);
        __thread.setDaemon(true);
        __thread.start();
        __threaded = true;
    }


    // synchronized(__client) critical sections are to protect against
    // TelnetOutputStream writing through the telnet client at same time
    // as a processDo/Will/etc. command invoked from TelnetInputStream
    // tries to write.
    private int __read(boolean mayBlock) throws IOException
    {
        int ch;

_loop:
        while (true)
        {
 
            // If there is no more data AND we were told not to block, just return -2. (More efficient than exception.)
            if(!mayBlock && super.available() == 0)
                return -2;
            
            // Otherwise, exit only when we reach end of stream.
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

    // synchronized(__client) critical sections are to protect against
    // TelnetOutputStream writing through the telnet client at same time
    // as a processDo/Will/etc. command invoked from TelnetInputStream
    // tries to write.
    private void __processChar(int ch) throws InterruptedException
    {
        // Critical section because we're altering __bytesAvailable,
        // __queueTail, and the contents of _queue.
        synchronized (__queue)
        {
            while (__bytesAvailable >= __queue.length - 1)
            {
                // The queue is full. We need to wait before adding any more data to it. Hopefully the stream owner
                // will consume some data soon! 
                if(__threaded)
                {
                    __queue.notify();
                    try
                    {
                        __queue.wait();
                    }
                    catch (InterruptedException e)
                    {
                        throw e;
                    }
                }
                else
                {
                    // We've been asked to add another character to the queue, but it is already full and there's
                    // no other thread to drain it. This should not have happened! 
                    throw new IllegalStateException("Queue is full! Cannot process another character.");
                }
            }

            // Need to do this in case we're not full, but block on a read
            if (__readIsWaiting && __threaded)
            {
                __queue.notify();
            }

            __queue[__queueTail] = ch;
            ++__bytesAvailable;

            if (++__queueTail >= __queue.length)
                __queueTail = 0;
        }
    }

    @Override
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
                    if(__threaded)
                    {
                        __queue.notify();
                        try
                        {
                            __readIsWaiting = true;
                            __queue.wait();
                            __readIsWaiting = false;
                        }
                        catch (InterruptedException e)
                        {
                            throw new InterruptedIOException("Fatal thread interruption during read.");
                        }
                    }
                    else
                    {
                        //__alreadyread = false;
                        __readIsWaiting = true;
                        int ch;
                        boolean mayBlock = true;    // block on the first read only
                        
                        do
                        {
                            try
                            {
                                if ((ch = __read(mayBlock)) < 0)
                                    if(ch != -2)
                                        return (ch);
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
                                    }
                                }
                                return (-1);
                            }


                            try
                            {
                                if(ch != -2)
                                {
                                    __processChar(ch);
                                }
                            }
                            catch (InterruptedException e)
                            {
                                if (__isClosed)
                                    return (-1);
                            }
                            
                            // Reads should not block on subsequent iterations. Potentially, this could happen if the 
                            // remaining buffered socket data consists entirely of Telnet command sequence and no "user" data.
                            mayBlock = false;
                            
                        }
                        // Continue reading as long as there is data available and the queue is not full.
                        while (super.available() > 0 && __bytesAvailable < __queue.length - 1);
                        
                        __readIsWaiting = false;
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

            // Need to explicitly notify() so available() works properly
            if(__bytesAvailable == 0 && __threaded) {
                __queue.notify();
            }
            
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
    @Override
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
    @Override
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
    @Override
    public boolean markSupported()
    {
        return false;
    }

    @Override
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
    @Override
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
            __isClosed      = true;

            if (__thread != null && __thread.isAlive())
            {
                __thread.interrupt();
            }

            __queue.notifyAll();
        }

        __threaded = false;
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
                    if ((ch = __read(true)) < 0)
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
                } catch(RuntimeException re) {
                    // We treat any runtime exceptions as though the
                    // stream has been closed.  We close the
                    // underlying stream just to be sure.
                    super.close();
                    // Breaking the loop has the effect of setting
                    // the state to closed at the end of the method.
                    break _outerLoop;
                }

                try
                {
                    __processChar(ch);
                }
                catch (InterruptedException e)
                {
                    if (__isClosed)
                        break _outerLoop;
                }
            }
        }
        catch (IOException ioe)
        {
            synchronized (__queue)
            {
                __ioException = ioe;
            }
        }

        synchronized (__queue)
        {
            __isClosed      = true; // Possibly redundant
            __hasReachedEOF = true;
            __queue.notify();
        }

        __threaded = false;
    }
}

/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
