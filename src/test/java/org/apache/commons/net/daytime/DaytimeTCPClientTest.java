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

package org.apache.commons.net.daytime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DaytimeTCPClientTest {

    private static MockDaytimeTCPServer mockDaytimeTCPServer;

    @AfterAll
    public static void afterAll() throws IOException {
        mockDaytimeTCPServer.stop();
    }

    @BeforeAll
    public static void beforeAll() throws IOException {
        mockDaytimeTCPServer = new MockDaytimeTCPServer();
        mockDaytimeTCPServer.start();
    }

    private static Stream<Arguments> daytimeMockData() {
        return Stream.of(
                Arguments.of("Thursday, February 2, 2006, 13:45:51-PST", ZoneId.of("PST", ZoneId.SHORT_IDS), LocalDateTime.of(2006, 2, 2, 13, 45, 51)),
                Arguments.of("Thursday, January 1, 2004, 00:00:00-UTC", ZoneId.of("UTC"), LocalDate.of(2004, 1, 1).atStartOfDay()),
                Arguments.of("Friday, July 28, 2023, 06:06:50-JST", ZoneId.of("JST", ZoneId.SHORT_IDS), LocalDateTime.of(2023, 7, 28, 6, 6, 50, 999))
        );
    }

    private DaytimeTCPClient daytimeTCPClient;
    private InetAddress localHost;

    @Test
    public void constructDaytimeTcpClient() {
        final DaytimeTCPClient daytimeTCPClient = new DaytimeTCPClient();
        assertEquals(13, daytimeTCPClient.getDefaultPort());
    }

    @ParameterizedTest(name = "getTime() should return <{0}> for date <{2}> and zone <{1}>")
    @Timeout(5)
    @MethodSource("daytimeMockData")
    public void getTime(final String expectedDaytimeString, final ZoneId zoneId, final LocalDateTime localDateTime) throws IOException {
        final Clock mockClock = Clock.fixed(localDateTime.atZone(zoneId).toInstant(), zoneId);
        mockDaytimeTCPServer.enqueue(mockClock);

        daytimeTCPClient = new DaytimeTCPClient();
        daytimeTCPClient.setDefaultTimeout(60000);
        daytimeTCPClient.connect(localHost, mockDaytimeTCPServer.getPort());

        final String time = daytimeTCPClient.getTime();
        assertEquals(expectedDaytimeString, time);
    }

    @BeforeEach
    public void setUp() throws UnknownHostException {
        localHost = InetAddress.getLocalHost();
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (daytimeTCPClient != null && daytimeTCPClient.isConnected()) {
            daytimeTCPClient.disconnect();
        }
    }

}

