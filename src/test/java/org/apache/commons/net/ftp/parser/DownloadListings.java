/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.commons.net.ftp.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.Socket;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPCmd;
import org.apache.commons.net.io.Util;

/**
 * Sample class to download LIST and MLSD listings from list of ftp sites.
 */
public class DownloadListings extends FTPClient {

    // Also used by MLDSComparison
    static final String DOWNLOAD_DIR = "target/ftptest";

    private PrintCommandListener listener;
    private PrintWriter out;

    private boolean open(String host, int port) throws Exception{
        System.out.println("Connecting to "+host);
        out = new PrintWriter(new FileWriter(new File(DOWNLOAD_DIR, host+"_info.txt")));
        listener = new PrintCommandListener(out);
        addProtocolCommandListener(listener);
        setConnectTimeout(30000);
        try {
            connect(host, port);
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        enterLocalPassiveMode(); // this is reset by connect
        System.out.println("Logging in to "+host);
        return login("anonymous", "user@localhost");
    }

    private void info() throws IOException {
        syst();
        help();
        feat();
        removeProtocolCommandListener(listener);
    }

    private void download(String path, FTPCmd command, File filename) throws Exception {
        Socket socket;
        if ((socket = _openDataConnection_(command, getListArguments(path))) == null) {
            System.out.println(getReplyString());
            return;
        }
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = new FileOutputStream(filename);
        Util.copyStream(inputStream, outputStream );
        inputStream.close();
        socket.close();
        outputStream.close();

        if (!completePendingCommand())
        {
            System.out.println(getReplyString());
        }
    }

    public static void main(String[] args) throws Exception {
        String host;// = "ftp.funet.fi";
        int port = 21;
        String path;// = "/";

        new File(DOWNLOAD_DIR).mkdirs();
        DownloadListings self = new DownloadListings();
        OutputStream os = new FileOutputStream(new File(DOWNLOAD_DIR, "session.log"));
        self.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(os), true));

        Reader is = new FileReader("mirrors.list");
        BufferedReader rdr = new BufferedReader(is);
        String line;
        while((line=rdr.readLine()) != null){
            if (line.startsWith("ftp")){
                String []parts = line.split("\\s+");
                String target = parts[2];
                host = target.substring("ftp://".length());
                int slash = host.indexOf('/');
                path = host.substring(slash);
                host = host.substring(0,slash);
                System.out.println(host+ " "+path);
                if (self.open(host, port)) {
                    try {
                        self.info();
                        self.download(path, FTPCmd.LIST, new File(DOWNLOAD_DIR, host+"_list.txt"));
                        self.download(path, FTPCmd.MLSD, new File(DOWNLOAD_DIR, host+"_mlsd.txt"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        self.disconnect();
                    }
                    self.removeProtocolCommandListener(self.listener);
                    self.out.close();
                }
            }
        }
        os.close();
        rdr.close();
    }
}
