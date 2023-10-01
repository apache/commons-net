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
 * A placeholder utility class, used for constructing a tree of Threadables Original implementation by Jamie Zawinski. See the Grendel source for more details
 * <a href="http://lxr.mozilla.org/mozilla/source/grendel/sources/grendel/view/Threader.java#511">here</a> Threadable objects
 */
class NntpThreadContainer {
    Threadable threadable;
    NntpThreadContainer parent;
//    NntpThreadContainer prev;
    NntpThreadContainer next;
    NntpThreadContainer child;

    /**
     *
     * @param target
     * @return true if child is under self's tree. Detects circular references
     */
    boolean findChild(final NntpThreadContainer target) {
        if (child == null) {
            return false;
        }
        if (child == target) {
            return true;
        }
        return child.findChild(target);
    }

    // Copy the NntpThreadContainer tree structure down into the underlying Threadable objects
    // (Make the Threadable tree look like the NntpThreadContainer tree)
    // TODO convert this to an iterative function - this can blow the stack
    // with very large Threadable trees
    void flush() {
        if (parent != null && threadable == null) {
            throw new IllegalStateException("no threadable in " + this.toString());
        }

        parent = null;

        if (threadable != null) {
            threadable.setChild(child == null ? null : child.threadable);
        }

        if (child != null) {
            child.flush();
            child = null;
        }

        if (threadable != null) {
            threadable.setNext(next == null ? null : next.threadable);
        }

        if (next != null) {
            next.flush();
            next = null;
        }

        threadable = null;
    }

    /**
     * Reverse the entire set of children
     *
     */
    void reverseChildren() {
        if (child != null) {
            NntpThreadContainer kid, prev, rest;
            for (prev = null, kid = child, rest = kid.next; kid != null; prev = kid, kid = rest, rest = rest == null ? null : rest.next) {
                kid.next = prev;
            }

            child = prev;

            // Do it for the kids
            for (kid = child; kid != null; kid = kid.next) {
                kid.reverseChildren();
            }
        }
    }
}
