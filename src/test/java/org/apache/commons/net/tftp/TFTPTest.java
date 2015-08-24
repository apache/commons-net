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
package org.apache.commons.net.tftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.tftp.TFTPServer.ServerMode;

import junit.framework.TestCase;

/**
 * Test the TFTP Server and TFTP Client by creating some files in the system temp folder and then
 * uploading and downloading them.
 */
public class TFTPTest extends TestCase
{
    private static final int SERVER_PORT = 6902;
    private static TFTPServer tftpS;
    private static final File serverDirectory = new File(System.getProperty("java.io.tmpdir"));
    private static final String filePrefix = "tftp-";
    private static final File[] files = new File[8];

    static int testsLeftToRun = 6;

    // only want to do this once...
    static
    {
        try
        {
            files[0] = createFile(new File(serverDirectory, filePrefix + "empty.txt"), 0);
            files[1] = createFile(new File(serverDirectory, filePrefix + "small.txt"), 1);
            files[2] = createFile(new File(serverDirectory, filePrefix + "511.txt"), 511);
            files[3] = createFile(new File(serverDirectory, filePrefix + "512.txt"), 512);
            files[4] = createFile(new File(serverDirectory, filePrefix + "513.txt"), 513);
            files[5] = createFile(new File(serverDirectory, filePrefix + "med.txt"), 1000 * 1024);
            files[6] = createFile(new File(serverDirectory, filePrefix + "big.txt"), 5000 * 1024);
            files[7] = createFile(new File(serverDirectory, filePrefix + "huge.txt"), 37000 * 1024);

            // Start the server
            tftpS = new TFTPServer(serverDirectory, serverDirectory, SERVER_PORT, ServerMode.GET_AND_PUT,
                    null, null);
            tftpS.setSocketTimeout(2000);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    protected void tearDown() throws Exception
    {
        testsLeftToRun--;
        if (testsLeftToRun <= 0)
        {
            if (tftpS != null)
            {
                tftpS.shutdown();
            }
            for (File file : files)
            {
                file.delete();
            }
        }
        super.tearDown();
    }

    /*
     * Create a file, size specified in bytes
     */
    private static File createFile(File file, int size) throws IOException
    {
        OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
        byte[] temp = "0".getBytes();
        for (int i = 0; i < size; i++)
        {
            os.write(temp);
        }
        os.close();
        return file;
    }

    public void testTFTPBinaryDownloads() throws Exception
    {
        // test with the smaller files
        for (int i = 0; i < 6; i++)
        {
            testDownload(TFTP.BINARY_MODE, files[i]);
        }
    }

    public void testASCIIDownloads() throws Exception
    {
        // test with the smaller files
        for (int i = 0; i < 6; i++)
        {
            try {
                testDownload(TFTP.ASCII_MODE, files[i]);
            } catch (IOException e) {
                fail("Entry "+i+" Error "+e.toString());
            }

        }
    }

    public void testTFTPBinaryUploads() throws Exception
    {
        // test with the smaller files
        for (int i = 0; i < 6; i++)
        {
            testUpload(TFTP.BINARY_MODE, files[i]);
        }
    }

    public void testASCIIUploads() throws Exception
    {
        // test with the smaller files
        for (int i = 0; i < 6; i++)
        {
            testUpload(TFTP.ASCII_MODE, files[i]);
        }
    }

    public void testHugeUploads() throws Exception
    {
        for (int i = 5; i < files.length; i++)
        {
            testUpload(TFTP.BINARY_MODE, files[i]);
        }
    }

    public void testHugeDownloads() throws Exception
    {
        // test with the smaller files
        for (int i = 5; i < files.length; i++)
        {
            testDownload(TFTP.BINARY_MODE, files[i]);
        }
    }

    private void testDownload(int mode, File file) throws IOException
    {
        // Create our TFTP instance to handle the file transfer.
        TFTPClient tftp = new TFTPClient();
        tftp.open();
        tftp.setSoTimeout(2000);

        File out = new File(serverDirectory, filePrefix + "download");

        // cleanup old failed runs
        out.delete();
        assertTrue("Couldn't clear output location", !out.exists());

        FileOutputStream output = new FileOutputStream(out);

        tftp.receiveFile(file.getName(), mode, output, "localhost", SERVER_PORT);
        output.close();

        assertTrue("file not created", out.exists());
        assertTrue("files not identical on file " + file, filesIdentical(out, file));

        // delete the downloaded file
        out.delete();
    }

    private void testUpload(int mode, File file) throws Exception
    {
        // Create our TFTP instance to handle the file transfer.
        TFTPClient tftp = new TFTPClient();
        tftp.open();
        tftp.setSoTimeout(2000);

        File in = new File(serverDirectory, filePrefix + "upload");
        // cleanup old failed runs
        in.delete();
        assertTrue("Couldn't clear output location", !in.exists());

        FileInputStream fis = new FileInputStream(file);
        tftp.sendFile(in.getName(), mode, fis, "localhost", SERVER_PORT);
        fis.close();

        // need to give the server a bit of time to receive our last packet, and
        // close out its file buffers, etc.
        Thread.sleep(100);
        assertTrue("file not created", in.exists());
        assertTrue("files not identical on file " + file, filesIdentical(file, in));

        in.delete();
    }

    private boolean filesIdentical(File a, File b) throws IOException
    {
        if (!a.exists() || !b.exists())
        {
            return false;
        }

        if (a.length() != b.length())
        {
            return false;
        }

        InputStream fisA = new BufferedInputStream(new FileInputStream(a));
        InputStream fisB = new BufferedInputStream(new FileInputStream(b));

        int aBit = fisA.read();
        int bBit = fisB.read();

        while (aBit != -1)
        {
            if (aBit != bBit)
            {
                fisA.close();
                fisB.close();
                return false;
            }
            aBit = fisA.read();
            bBit = fisB.read();
        }

        fisA.close();
        fisB.close();
        return true;
    }
}
