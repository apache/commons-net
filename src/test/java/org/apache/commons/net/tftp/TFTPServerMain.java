/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.net.tftp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;

/**
 * Main class for TFTPServer. This allows CLI use of the server.
 *
 * @since 3.6
 */
public class TFTPServerMain {

    private static final String USAGE = "Usage: TFTPServerMain [options] [port]\n\n" + "port   - the port to use (default 6901)\n"
            + "\t-p path to server directory (default java.io.tempdir)\n" + "\t-r randomly introduce errors\n" + "\t-v verbose (trace packets)\n";

    public static void main(final String[] args) throws Exception {
        int port = 6901;
        int argc;
        final Map<String, String> opts = new HashMap<>();
        opts.put("-p", FileUtils.getTempDirectoryPath());
        // Parse options
        for (argc = 0; argc < args.length; argc++) {
            final String arg = args[argc];
            if (!arg.startsWith("-")) {
                break;
            }
            if (arg.equals("-v") || arg.equals("-r")) {
                opts.put(arg, arg);
            } else if (arg.equals("-p")) {
                opts.put(arg, args[++argc]);
            } else {
                System.err.println("Error: unrecognized option.");
                System.err.print(USAGE);
                System.exit(1);
            }
        }

        if (argc < args.length) {
            port = Integer.parseInt(args[argc]);
            argc++;
        }
        final boolean verbose = opts.containsKey("-v");
        final boolean randomErrors = opts.containsKey("-r");
        final Random rand = randomErrors ? new Random() : null;

        final File serverDirectory = new File(opts.get("-p"));
        System.out.println("Server directory: " + serverDirectory);
        final TFTPServer tftpS = new TFTPServer(serverDirectory, serverDirectory, port, TFTPServer.ServerMode.GET_AND_PUT, null, null) {
            @Override
            TFTP newTFTP() {
                if (verbose) {
                    return new TFTP() {
                        @Override
                        protected void trace(final String direction, final TFTPPacket packet) {
                            System.out.println(direction + " " + packet.toString());
                        }
                    };
                }
                return new TFTP();
            }

            @Override
            void sendData(final TFTP tftp, final TFTPPacket packet) throws IOException {
                if (rand == null) {
                    super.sendData(tftp, packet);
                    return;
                }
                final int rint = rand.nextInt(10);
                switch (rint) {
                case 0:
                    System.out.println("Bump port " + packet);
                    final int port = packet.getPort();
                    packet.setPort(port + 5);
                    super.sendData(tftp, packet);
                    packet.setPort(port);
                    break;
                case 1:
                    if (packet instanceof TFTPDataPacket) {
                        final TFTPDataPacket data = (TFTPDataPacket) packet;
                        System.out.println("Change data block num");
                        data.blockNumber--;
                        super.sendData(tftp, packet);
                        data.blockNumber++;
                    }
                    if (packet instanceof TFTPAckPacket) {
                        final TFTPAckPacket ack = (TFTPAckPacket) packet;
                        System.out.println("Change ack block num");
                        ack.blockNumber--;
                        super.sendData(tftp, packet);
                        ack.blockNumber++;
                    }
                    break;
                case 2:
                    System.out.println("Drop packet: " + packet);
                    break;
                case 3:
                    System.out.println("Dupe packet: " + packet);
                    super.sendData(tftp, packet);
                    super.sendData(tftp, packet);
                    break;
                default:
                    super.sendData(tftp, packet);
                    break;
                }
            }
        };

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Server shutting down");
                tftpS.close();
                System.out.println("Server exit");
            }
        });
        System.out.println("Started the server on " + port);
        Thread.sleep(99999999L);
    }

}
