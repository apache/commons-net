package org.apache.commons.net.tftp;

import java.io.*;
import junit.framework.*;

/**
 * Some basic tests to ensure that the TFTP Server is honoring its read/write mode, and preventing
 * files from being read or written from outside of the assigned roots.
 * 
 * @author <A HREF="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</A>
 * 
 */
public class TFTPServerPathTests extends TestCase
{
	String filePrefix = "tftp-";
	File serverDirectory = new File(System.getProperty("java.io.tmpdir"));

	public void testReadOnly() throws IOException
	{
		// Start a read-only server
		TFTPServer tftpS = new TFTPServer(serverDirectory, serverDirectory, 6900,
				TFTPServer.GET_ONLY, null, null);

		// Create our TFTP instance to handle the file transfer.
		TFTPClient tftp = new TFTPClient();
		tftp.open();
		tftp.setSoTimeout(2000);

		// make a file to work with.
		File file = new File(serverDirectory, filePrefix + "source.txt");
		file.createNewFile();

		// Read the file from the tftp server.
		File out = new File(serverDirectory, filePrefix + "out");

		// cleanup old failed runs
		out.delete();
		assertTrue("Couldn't clear output location", !out.exists());

		FileOutputStream output = new FileOutputStream(out);

		tftp.receiveFile(file.getName(), TFTP.BINARY_MODE, output, "localhost", 6900);
		output.close();

		assertTrue("file not created", out.exists());

		out.delete();

		FileInputStream fis = new FileInputStream(file);
		try
		{
			tftp.sendFile(out.getName(), TFTP.BINARY_MODE, fis, "localhost", 6900);
			fail("Server allowed write");
		}
		catch (IOException e)
		{
			// expected path
		}
		fis.close();
		file.delete();
		tftpS.shutdown();
	}

	public void testWriteOnly() throws IOException
	{
		// Start a write-only server
		TFTPServer tftpS = new TFTPServer(serverDirectory, serverDirectory, 6900,
				TFTPServer.PUT_ONLY, null, null);

		// Create our TFTP instance to handle the file transfer.
		TFTPClient tftp = new TFTPClient();
		tftp.open();
		tftp.setSoTimeout(2000);

		// make a file to work with.
		File file = new File(serverDirectory, filePrefix + "source.txt");
		file.createNewFile();

		File out = new File(serverDirectory, filePrefix + "out");

		// cleanup old failed runs
		out.delete();
		assertTrue("Couldn't clear output location", !out.exists());

		FileOutputStream output = new FileOutputStream(out);

		try
		{
			tftp.receiveFile(file.getName(), TFTP.BINARY_MODE, output, "localhost", 6900);
			fail("Server allowed read");
		}
		catch (IOException e)
		{
			// expected path
		}
		output.close();
		out.delete();

		FileInputStream fis = new FileInputStream(file);
		tftp.sendFile(out.getName(), TFTP.BINARY_MODE, fis, "localhost", 6900);

		fis.close();

		assertTrue("file not created", out.exists());

		// cleanup
		file.delete();
		out.delete();
		tftpS.shutdown();
	}

	public void testWriteOutsideHome() throws IOException
	{
		// Start a server
		TFTPServer tftpS = new TFTPServer(serverDirectory, serverDirectory, 6900,
				TFTPServer.GET_AND_PUT, null, null);

		// Create our TFTP instance to handle the file transfer.
		TFTPClient tftp = new TFTPClient();
		tftp.open();

		File file = new File(serverDirectory, filePrefix + "source.txt");
		file.createNewFile();

		assertFalse("test construction error", new File(serverDirectory, "../foo").exists());

		FileInputStream fis = new FileInputStream(file);
		try
		{
			tftp.sendFile("../foo", TFTP.BINARY_MODE, fis, "localhost", 6900);
			fail("Server allowed write!");
		}
		catch (IOException e)
		{
			// expected path
		}

		fis.close();

		assertFalse("file created when it should not have been",
				new File(serverDirectory, "../foo").exists());

		// cleanup
		file.delete();

		tftpS.shutdown();
	}
}
