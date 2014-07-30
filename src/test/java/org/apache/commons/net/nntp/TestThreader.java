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

package org.apache.commons.net.nntp;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test the Threader
 */
public class TestThreader {

    @Test
    @SuppressWarnings("deprecation") // test of deprecated method
    public void testNullArray() { // NET-539
        Threader t = new Threader();
        Threadable[] messages=null;
        Assert.assertNull(t.thread(messages));
    }

    @Test
    public void testNullList() {
        Threader t = new Threader();
        List<Threadable> messages=null;
        Assert.assertNull(t.thread(messages));
    }

    @Test
    public void testNullIterable() {
        Threader t = new Threader();
        Iterable<Threadable> messages=null;
        Assert.assertNull(t.thread(messages));
    }

    @SuppressWarnings("deprecation") // test of deprecated method
    @Test
    public void testEmptyArray() { // NET-539
        Threader t = new Threader();
        Threadable[] messages=new Threadable[0];
        Assert.assertNull(t.thread(messages));
    }

    @Test
    public void testEmptyList() { // NET-539
        Threader t = new Threader();
        Threadable[] messages=new Threadable[0];
        final List<Threadable> asList = Arrays.asList(messages);
        Assert.assertNull(t.thread(asList));
    }

    @Test
    public void testEmptyIterable() { // NET-539
        Threader t = new Threader();
        Threadable[] messages=new Threadable[0];
        final Iterable<Threadable> asList = Arrays.asList(messages);
        Assert.assertNull(t.thread(asList));
    }

}
