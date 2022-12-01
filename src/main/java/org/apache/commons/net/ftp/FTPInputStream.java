package org.apache.commons.net.ftp;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FTPInputStream extends FilterInputStream {
  private final FTPClient ftpClient;
  protected FTPInputStream(InputStream in, FTPClient ftpClient) {
    super(in);
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
