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
 * This is an implementation of a message threading algorithm, as originally devised by Zamie Zawinski.
 * See <a href="http://www.jwz.org/doc/threading.html">http://www.jwz.org/doc/threading.html</a> for details.
 * For his Java implementation, see <a href="http://lxr.mozilla.org/mozilla/source/grendel/sources/grendel/view/Threader.java">http://lxr.mozilla.org/mozilla/source/grendel/sources/grendel/view/Threader.java</a>
 *
 * @author rwinston <rwinston@checkfree.com>
 *
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Threader {
    private ThreadContainer root;
    private HashMap<String,ThreadContainer> idTable;
    private int bogusIdCount = 0;

    /**
     * The client passes in a list of Threadable objects, and
     * the Threader constructs a connected 'graph' of messages
     * @param messages list of messages to thread
     * @return null if messages == null or root.child == null
     * @since 2.2
     */
    public Threadable thread(List<? extends Threadable> messages) {
        return thread((Iterable<? extends Threadable>)messages);
    }

    /**
     * The client passes in a list of Iterable objects, and
     * the Threader constructs a connected 'graph' of messages
     * @param messages iterable of messages to thread
     * @return null if messages == null or root.child == null
     * @since 3.0
     */
    public Threadable thread(Iterable<? extends Threadable> messages) {
        if (messages == null) {
            return null;
        }

        idTable = new HashMap<String,ThreadContainer>();

        // walk through each Threadable element
        for (Threadable t : messages) {
            if (!t.isDummy()) {
                buildContainer(t);
            }
        }

        root = findRootSet();
        idTable.clear();
        idTable = null;

        pruneEmptyContainers(root);

        root.reverseChildren();
        gatherSubjects();

        if (root.next != null) {
            throw new RuntimeException("root node has a next:" + root);
        }

        for (ThreadContainer r = root.child; r != null; r = r.next) {
            if (r.threadable == null) {
                r.threadable = r.child.threadable.makeDummy();
            }
        }

        Threadable result = (root.child == null ? null : root.child.threadable);
        root.flush();
        root = null;

        return result;
    }

    /**
     *
     * @param threadable
     */
    private void buildContainer(Threadable threadable) {
        String id = threadable.messageThreadId();
        ThreadContainer container = idTable.get(id);

        // A ThreadContainer exists for this id already. This should be a forward reference, but may
        // be a duplicate id, in which case we will need to generate a bogus placeholder id
        if (container != null) {
            if (container.threadable != null) { // oops! duplicate ids...
                id = "<Bogus-id:" + (bogusIdCount++) + ">";
                container = null;
            } else {
                // The container just contained a forward reference to this message, so let's
                // fill in the threadable field of the container with this message
                container.threadable = threadable;
            }
        }

        // No container exists for that message Id. Create one and insert it into the hash table.
        if (container == null) {
            container = new ThreadContainer();
            container.threadable = threadable;
            idTable.put(id, container);
        }

        // Iterate through all of the references and create ThreadContainers for any references that
        // don't have them.
        ThreadContainer parentRef = null;
        {
            String[] references = threadable.messageThreadReferences();
            for (String refString : references)
            {
                ThreadContainer ref = idTable.get(refString);

                // if this id doesnt have a container, create one
                if (ref == null) {
                    ref = new ThreadContainer();
                    idTable.put(refString, ref);
                }

                // Link references together in the order they appear in the References: header,
                // IF they dont have a have a parent already &&
                // IF it will not cause a circular reference
                if ((parentRef != null)
                    && (ref.parent == null)
                    && (parentRef != ref)
                    && !(ref.findChild(parentRef))) {
                    // Link ref into the parent's child list
                    ref.parent = parentRef;
                    ref.next = parentRef.child;
                    parentRef.child = ref;
                }
                parentRef = ref;
            }
        }

        // parentRef is now set to the container of the last element in the references field. make that
        // be the parent of this container, unless doing so causes a circular reference
        if (parentRef != null
            && (parentRef == container || container.findChild(parentRef)))
        {
            parentRef = null;
        }

        // if it has a parent already, its because we saw this message in a References: field, and presumed
        // a parent based on the other entries in that field. Now that we have the actual message, we can
        // throw away the old parent and use this new one
        if (container.parent != null) {
            ThreadContainer rest, prev;

            for (prev = null, rest = container.parent.child;
                rest != null;
                prev = rest, rest = rest.next) {
                if (rest == container) {
                    break;
                }
            }

            if (rest == null) {
                throw new RuntimeException(
                    "Didnt find "
                        + container
                        + " in parent"
                        + container.parent);
            }

            // Unlink this container from the parent's child list
            if (prev == null) {
                container.parent.child = container.next;
            } else {
                prev.next = container.next;
            }

            container.next = null;
            container.parent = null;
        }

        // If we have a parent, link container into the parents child list
        if (parentRef != null) {
            container.parent = parentRef;
            container.next = parentRef.child;
            parentRef.child = container;
        }
    }

    /**
     * Find the root set of all existing ThreadContainers
     * @return root the ThreadContainer representing the root node
     */
    private ThreadContainer findRootSet() {
        ThreadContainer root = new ThreadContainer();
        Iterator<String> iter = idTable.keySet().iterator();

        while (iter.hasNext()) {
            Object key = iter.next();
            ThreadContainer c = idTable.get(key);
            if (c.parent == null) {
                if (c.next != null) {
                    throw new RuntimeException(
                            "c.next is " + c.next.toString());
                }
                c.next = root.child;
                root.child = c;
            }
        }
        return root;
    }

    /**
     * Delete any empty or dummy ThreadContainers
     * @param parent
     */
    private void pruneEmptyContainers(ThreadContainer parent) {
        ThreadContainer container, prev, next;
        for (prev = null, container = parent.child, next = container.next;
            container != null;
            prev = container,
                container = next,
                next = (container == null ? null : container.next)) {

            // Is it empty and without any children? If so,delete it
            if (container.threadable == null && container.child == null) {
                if (prev == null) {
                    parent.child = container.next;
                } else {
                    prev.next = container.next;
                }

                // Set container to prev so that prev keeps its same value the next time through the loop
                container = prev;
            }

            // Else if empty, with kids, and (not at root or only one kid)
            else if (
                container.threadable == null
                    && container.child != null
                    && (container.parent != null
                        || container.child.next == null)) {
                // We have an invalid/expired message with kids. Promote the kids to this level.
                ThreadContainer tail;
                ThreadContainer kids = container.child;

                // Remove this container and replace with 'kids'.
                if (prev == null) {
                    parent.child = kids;
                } else {
                    prev.next = kids;
                }

                // Make each child's parent be this level's parent -> i.e. promote the children. Make the last child's next point to this container's next
                // i.e. splice kids into the list in place of container
                for (tail = kids; tail.next != null; tail = tail.next) {
                    tail.parent = container.parent;
                }

                tail.parent = container.parent;
                tail.next = container.next;

                // next currently points to the item after the inserted items in the chain - reset that so we process the newly
                // promoted items next time round
                next = kids;

                // Set container to prev so that prev keeps its same value the next time through the loop
                container = prev;
            } else if (container.child != null) {
                // A real message , with kids
                // Iterate over the children
                pruneEmptyContainers(container);
            }
        }
    }

    /**
     *  If any two members of the root set have the same subject, merge them. This is to attempt to accomodate messages without References: headers.
     */
    private void gatherSubjects() {

        int count = 0;

        for (ThreadContainer c = root.child; c != null; c = c.next) {
            count++;
        }

        // TODO verify this will avoid rehashing
        HashMap<String, ThreadContainer> subjectTable = new HashMap<String, ThreadContainer>((int) (count * 1.2), (float) 0.9);
        count = 0;

        for (ThreadContainer c = root.child; c != null; c = c.next) {
            Threadable threadable = c.threadable;

            // No threadable? If so, it is a dummy node in the root set.
            // Only root set members may be dummies, and they alway have at least 2 kids
            // Take the first kid as representative of the subject
            if (threadable == null) {
                threadable = c.child.threadable;
            }

            String subj = threadable.simplifiedSubject();

            if (subj == null || subj.length() == 0) {
                continue;
            }

            ThreadContainer old = subjectTable.get(subj);

            // Add this container to the table iff:
            // - There exists no container with this subject
            // - or this is a dummy container and the old one is not - the dummy one is
            // more interesting as a root, so put it in the table instead
            // - The container in the table has a "Re:" version of this subject, and
            // this container has a non-"Re:" version of this subject. The non-"Re:" version
            // is the more interesting of the two.
            if (old == null
                || (c.threadable == null && old.threadable != null)
                || (old.threadable != null
                    && old.threadable.subjectIsReply()
                    && c.threadable != null
                    && !c.threadable.subjectIsReply())) {
                subjectTable.put(subj, c);
                count++;
            }
        }

        // If the table is empty, we're done
        if (count == 0) {
            return;
        }

        // subjectTable is now populated with one entry for each subject which occurs in the
        // root set. Iterate over the root set, and gather together the difference.
        ThreadContainer prev, c, rest;
        for (prev = null, c = root.child, rest = c.next;
            c != null;
            prev = c, c = rest, rest = (rest == null ? null : rest.next)) {
            Threadable threadable = c.threadable;

            // is it a dummy node?
            if (threadable == null) {
                threadable = c.child.threadable;
            }

            String subj = threadable.simplifiedSubject();

            // Dont thread together all subjectless messages
            if (subj == null || subj.length() == 0) {
                continue;
            }

            ThreadContainer old = subjectTable.get(subj);

            if (old == c) { // That's us
                continue;
            }

            // We have now found another container in the root set with the same subject
            // Remove the "second" message from the root set
            if (prev == null) {
                root.child = c.next;
            } else {
                prev.next = c.next;
            }
            c.next = null;

            if (old.threadable == null && c.threadable == null) {
                // both dummies - merge them
                ThreadContainer tail;
                for (tail = old.child;
                    tail != null && tail.next != null;
                    tail = tail.next) {
                    // do nothing
                }

                if (tail != null) { // protect against possible NPE
                    tail.next = c.child;
                }

                for (tail = c.child; tail != null; tail = tail.next) {
                    tail.parent = old;
                }

                c.child = null;
            } else if (
                old.threadable == null
                    || (c.threadable != null
                        && c.threadable.subjectIsReply()
                        && !old.threadable.subjectIsReply())) {
                // Else if old is empty, or c has "Re:" and old does not  ==> make this message a child of old
                c.parent = old;
                c.next = old.child;
                old.child = c;
            } else {
                // else make the old and new messages be children of a new dummy container.
                // We create a new container object for old.msg and empty the old container
                ThreadContainer newc = new ThreadContainer();
                newc.threadable = old.threadable;
                newc.child = old.child;

                for (ThreadContainer tail = newc.child;
                    tail != null;
                    tail = tail.next)
                {
                    tail.parent = newc;
                }

                old.threadable = null;
                old.child = null;

                c.parent = old;
                newc.parent = old;

                // Old is now a dummy- give it 2 kids , c and newc
                old.child = c;
                c.next = newc;
            }
            // We've done a merge, so keep the same prev
            c = prev;
        }

        subjectTable.clear();
        subjectTable = null;

    }


    // DEPRECATED METHODS - for API compatibility only - DO NOT USE

    /**
     * The client passes in an array of Threadable objects, and
     * the Threader constructs a connected 'graph' of messages
     * @param messages array of messages to thread
     * @return null if messages == null or root.child == null
     * @deprecated (2.2) prefer {@link #thread(List)}
     */
    @Deprecated
    public Threadable thread(Threadable[] messages) {
        return thread(java.util.Arrays.asList(messages));
    }

}
