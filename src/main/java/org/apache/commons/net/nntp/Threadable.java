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

/**
 * A placeholder interface for threadable message objects.
 *
 * @param <T> The Threadable implementation.
 */
public interface Threadable<T extends Threadable<T>> {

    /**
     * Tests whether this is a dummy instance.
     *
     * @return whether this is a dummy instance.
     */
    boolean isDummy();

    /**
     * Creates a dummy instance.
     *
     * @return a dummy instance.
     */
    T makeDummy();

    /**
     * Gets an ID.
     * @return an ID.
     */
    String messageThreadId();

    /**
     * Gets reference strings.
     * @return  reference strings.
     */
    String[] messageThreadReferences();

    /**
     * Sets the child instance.
     *
     * @param child  the child instance.
     */
    void setChild(T child);

    /**
     * Sets the next instance.
     * @param next  the next instance.
     */
    void setNext(T next);

    /**
     * Gets the simplified subject.
     *
     * @return the simplified subject.
     */
    String simplifiedSubject();

    /**
     * Tests whether this is a reply.
     *
     * @return whether this is a reply.
     */
    boolean subjectIsReply();
}
