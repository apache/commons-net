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

package examples.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.net.io.Util;

/**
 * This is a utility class providing a reader/writer capability required
 * by the weatherTelnet, rexec, rshell, and rlogin example programs.
 * <p>
 * It also contains some other common IO methods.
 */

public final class IOUtil
{

    /**
     * This method spawns a reader thread and a writer thread. The reader thread
     * reads from a local input source (presumably stdin) and writes the
     * data to a remote output destination.  The writer thread reads from
     * a remote input source and writes to a local output destination.
     * The threads terminate when the remote input source closes.
     * 
     * @param remoteInput
     * @param remoteOutput
     * @param localInput
     * @param localOutput
     */
    public static final void readWrite(final InputStream remoteInput,
                                       final OutputStream remoteOutput,
                                       final InputStream localInput,
                                       final OutputStream localOutput)
    {
        Thread reader, writer;

        reader = new Thread()
                 {
                     @Override
                     public void run()
                     {
                         int ch;

                         try
                         {
                             while (!interrupted() && (ch = localInput.read()) != -1)
                             {
                                 remoteOutput.write(ch);
                                 remoteOutput.flush();
                             }
                         }
                         catch (IOException e)
                         {
                             //e.printStackTrace();
                         }
                     }
                 }
                 ;


        writer = new Thread()
                 {
                     @Override
                     public void run()
                     {
                         try
                         {
                             Util.copyStream(remoteInput, localOutput);
                         }
                         catch (IOException e)
                         {
                             e.printStackTrace();
                             System.exit(1);
                         }
                     }
                 };


        writer.setPriority(Thread.currentThread().getPriority() + 1);

        writer.start();
        reader.setDaemon(true);
        reader.start();

        try
        {
            writer.join();
            reader.interrupt();
        }
        catch (InterruptedException e)
        {
        }
    }

    /**
     * Closes the object quietly, catching rather than throwing IOException.
     * Intended for use from finally blocks.
     * 
     * @param closeable the object to close, may be {@code null}
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Closes the socket quietly, catching rather than throwing IOException.
     * Intended for use from finally blocks.
     * 
     * @param socket the socket to close, may be {@code null}
     */
    public static void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}

