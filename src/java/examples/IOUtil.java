/***
 * $RCSfile: IOUtil.java,v $ $Revision: 1.1 $ $Date: 2002/04/03 01:04:23 $
 *
 * NetComponents Internet Protocol Library
 * Copyright (C) 1997-2002  Daniel F. Savarese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library in the LICENSE file; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 ***/

import java.io.*;

import com.oroinc.io.*;

/***
 * This is a utility class providing a reader/writer capability required
 * by the weatherTelnet, rexec, rshell, and rlogin example programs.
 * The only point of the class is to hold the static method readWrite
 * which spawns a reader thread and a writer thread.  The reader thread
 * reads from a local input source (presumably stdin) and writes the
 * data to a remote output destination.  The writer thread reads from
 * a remote input source and writes to a local output destination.
 * The threads terminate when the remote input source closes.
 * <p>
 ***/

public final class IOUtil {

  public static final void readWrite(final InputStream remoteInput,
				     final OutputStream remoteOutput,
				     final InputStream localInput,
				     final OutputStream localOutput)
  {
    Thread reader, writer;

    reader = new Thread() {
      public void run() {
	int ch;

	try {
	  while(!interrupted() && (ch = localInput.read()) != -1) {
	    remoteOutput.write(ch);
	    remoteOutput.flush();
	  }
	} catch(IOException e) {
	  //e.printStackTrace();
	}
      }
    };


    writer = new Thread() {
      public void run() {
	try {
	  Util.copyStream(remoteInput, localOutput);
	} catch(IOException e) {
	  e.printStackTrace();
	  System.exit(1);
	}
      }
    };


    writer.setPriority(Thread.currentThread().getPriority() + 1);

    writer.start();
    reader.setDaemon(true);
    reader.start();

    try {
      writer.join();
      reader.interrupt();
    } catch(InterruptedException e) {

    }
  }

}

