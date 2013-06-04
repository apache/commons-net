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

package org.apache.commons.net.nntp;

import java.util.Iterator;
/**
 * Class which wraps an {@code Iterable<String>} of raw newgroup information
 * to generate an {@code Iterable<NewsgroupInfo>} of the parsed information.
 * @since 3.0
 */
class NewsgroupIterator implements Iterator<NewsgroupInfo>, Iterable<NewsgroupInfo> {

    private  final Iterator<String> stringIterator;

    public NewsgroupIterator(Iterable<String> iterableString) {
        stringIterator = iterableString.iterator();
    }

//    @Override
    public boolean hasNext() {
        return stringIterator.hasNext();
    }

//    @Override
    public NewsgroupInfo next() {
        String line = stringIterator.next();
        return NNTPClient.__parseNewsgroupListEntry(line);
    }

//    @Override
    public void remove() {
        stringIterator.remove();
    }

//    @Override
    public Iterator<NewsgroupInfo> iterator() {
        return this;
    }
}
