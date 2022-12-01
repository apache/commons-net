package org.apache.commons.net.ftp;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FTPOutputStream extends FilterOutputStream {
  private final FTPClient ftpClient;
  public FTPOutputStream(OutputStream out, FTPClient ftpClient) {
    super(out);
    this.ftpClient = ftpClient;
  }

  @Override
  public void close() throws IOException {
    super.close();
    if(!ftpClient.completePendingCommand()) {
      throw new IOException("FTP Client was unable to complete pending command");
    }
  }
}
