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

import java.util.ArrayList;

/**
 * This is a class that contains the basic state needed for message retrieval and threading.
 * With thanks to Jamie  Zawinski <jwz@jwz.org>
 * @author rwinston <rwinston@apache.org>
 */
public class Article implements Threadable {
    private long articleNumber;
    private String subject;
    private String date;
    private String articleId;
    private String simplifiedSubject;
    private String from;
    private ArrayList<String> references;
    private boolean isReply = false;

    public Article kid, next;

    public Article() {
        articleNumber = -1; // isDummy
    }

    /**
     * Adds a message-id to the list of messages that this message references (i.e. replies to)
     * @param msgId
     */
    public void addReference(String msgId) {
        if (msgId == null || msgId.length() == 0) {
            return;
        }
        if (references == null) {
            references = new ArrayList<String>();
        }
        isReply = true;
        for(String s : msgId.split(" ")) {
            references.add(s);
        }
    }

    /**
     * Returns the MessageId references as an array of Strings
     * @return an array of message-ids
     */
    public String[] getReferences() {
        if (references == null) {
            return new String[0];
        }
        return references.toArray(new String[references.size()]);
    }

    /**
     * Attempts to parse the subject line for some typical reply signatures, and strip them out
     *
     */
    private void simplifySubject() {
            int start = 0;
            String subject = getSubject();
            int len = subject.length();

            boolean done = false;

            while (!done) {
                done = true;

                // skip whitespace
                // "Re: " breaks this
                while (start < len && subject.charAt(start) == ' ') {
                    start++;
                }

                if (start < (len - 2)
                    && (subject.charAt(start) == 'r' || subject.charAt(start) == 'R')
                    && (subject.charAt(start + 1) == 'e' || subject.charAt(start + 1) == 'E')) {

                    if (subject.charAt(start + 2) == ':') {
                        start += 3; // Skip "Re:"
                        done = false;
                    } else if (
                        start < (len - 2)
                        &&
                        (subject.charAt(start + 2) == '[' || subject.charAt(start + 2) == '(')) {

                        int i = start + 3;

                        while (i < len && subject.charAt(i) >= '0' && subject.charAt(i) <= '9') {
                            i++;
                        }

                        if (i < (len - 1)
                            && (subject.charAt(i) == ']' || subject.charAt(i) == ')')
                            && subject.charAt(i + 1) == ':')
                        {
                            start = i + 2;
                            done = false;
                        }
                    }
                }

                if ("(no subject)".equals(simplifiedSubject)) {
                    simplifiedSubject = "";
                }

                int end = len;

                while (end > start && subject.charAt(end - 1) < ' ') {
                    end--;
                }

                if (start == 0 && end == len) {
                    simplifiedSubject = subject;
                } else {
                    simplifiedSubject = subject.substring(start, end);
                }
            }
        }

    /**
     * Recursive method that traverses a pre-threaded graph (or tree)
     * of connected Article objects and prints them out.
     * @param article the root of the article 'tree'
     * @param depth the current tree depth
     */
    public static void printThread(Article article, int depth) {
            for (int i = 0; i < depth; ++i) {
                System.out.print("==>");
            }
            System.out.println(article.getSubject() + "\t" + article.getFrom()+"\t"+article.getArticleId());
            if (article.kid != null) {
                printThread(article.kid, depth + 1);
            }
            if (article.next != null) {
                printThread(article.next, depth);
            }
    }

    public String getArticleId() {
        return articleId;
    }

    public long getArticleNumberLong() {
        return articleNumber;
    }

    public String getDate() {
        return date;
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public void setArticleId(String string) {
        articleId = string;
    }

    public void setArticleNumber(long l) {
        articleNumber = l;
    }

    public void setDate(String string) {
        date = string;
    }

    public void setFrom(String string) {
        from = string;
    }

    public void setSubject(String string) {
        subject = string;
    }


//    @Override
    public boolean isDummy() {
        return (articleNumber == -1);
    }

//    @Override
    public String messageThreadId() {
        return articleId;
    }

//    @Override
    public String[] messageThreadReferences() {
        return getReferences();
    }

//    @Override
    public String simplifiedSubject() {
        if(simplifiedSubject == null) {
            simplifySubject();
        }
        return simplifiedSubject;
    }


//    @Override
    public boolean subjectIsReply() {
        return isReply;
    }


//    @Override
    public void setChild(Threadable child) {
        this.kid = (Article) child;
        flushSubjectCache();
    }

    private void flushSubjectCache() {
        simplifiedSubject = null;
    }


//    @Override
    public void setNext(Threadable next) {
        this.next = (Article)next;
        flushSubjectCache();
    }


//    @Override
    public Threadable makeDummy() {
        return new Article();
    }

    @Override
    public String toString(){ // Useful for Eclipse debugging
        return articleNumber + " " +articleId + " " + subject;
    }

    // DEPRECATED METHODS - for API compatibility only - DO NOT USE

    @Deprecated
    public int getArticleNumber() {
        return (int) articleNumber;
    }

    @Deprecated
    public void setArticleNumber(int a) {
        articleNumber = a;
    }
    @Deprecated

    public void addHeaderField(String name, String val) {
    }

}
