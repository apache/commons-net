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

package org.apache.commons.net.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EventListener;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link ListenerList}.
 */
public class ListenerListTest {

    static class EventListenerImpl implements EventListener {
        // empty
    }

    @Test
    public void testAdd() {
        final EventListenerImpl eventListenerImpl = new EventListenerImpl();
        final ListenerList listenerList = new ListenerList();
        listenerList.addListener(eventListenerImpl);
        assertEquals(1, listenerList.getListenerCount());
    }

    @Test
    public void testConstructor() {
        assertEquals(0, new ListenerList().getListenerCount());
    }

    @Test
    public void testIterator() {
        final EventListenerImpl eventListenerImpl = new EventListenerImpl();
        final ListenerList listenerList = new ListenerList();
        listenerList.addListener(eventListenerImpl);
        final Iterator<EventListener> iterator = listenerList.iterator();
        assertTrue(iterator.hasNext());
        assertSame(eventListenerImpl, iterator.next());
    }
    @Test
    public void testRemove() {
        final EventListenerImpl eventListenerImpl = new EventListenerImpl();
        final ListenerList listenerList = new ListenerList();
        listenerList.addListener(eventListenerImpl);
        assertEquals(1, listenerList.getListenerCount());
        listenerList.removeListener(eventListenerImpl);
        assertEquals(0, listenerList.getListenerCount());
        listenerList.iterator();
    }

}
