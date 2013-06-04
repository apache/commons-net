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
 * Class which wraps an {@code Iterable<String>} of raw article information
 * to generate an {@code Iterable<Article>} of the parsed information.
 * @since 3.0
 */
class ArticleIterator implements Iterator<Article>, Iterable<Article> {

    private  final Iterator<String> stringIterator;

    public ArticleIterator(Iterable<String> iterableString) {
        stringIterator = iterableString.iterator();
    }

//    @Override
    public boolean hasNext() {
        return stringIterator.hasNext();
    }

    /**
     * Get the next Article
     * @return the next {@link Article}, never {@code null}, if unparseable then isDummy()
     * will be true, and the subject will contain the raw info.
     */
//    @Override
    public Article next() {
        String line = stringIterator.next();
        return NNTPClient.__parseArticleEntry(line);
    }

//    @Override
    public void remove() {
        stringIterator.remove();
    }
//    @Override
    public Iterator<Article> iterator() {
        return this;
    }
}
