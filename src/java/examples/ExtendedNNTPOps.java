/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.net.io.DotTerminatedMessageReader;
import org.apache.commons.net.nntp.NNTPClient;
import org.apache.commons.net.nntp.NewsgroupInfo;


public class ExtendedNNTPOps {

    // simple class that encapsulates some basic info about an NNTP article
    class Article {
        private int articleNumber;
        private String subject;
        private String date;
        private String articleId;
		
        private String from;
        private StringBuffer header;
		
        public Article() 
        {
            header = new StringBuffer();
        }
			
        public void addHeaderField(String name, String val) {
            header.append(name);
            header.append(": ");
            header.append(val);
            header.append('\n');
        }

        public String getArticleId() {
            return articleId;
        }
		
        public int getArticleNumber() {
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
		
        public void setArticleNumber(int i) {
            articleNumber = i;
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
    }

    NNTPClient client;
	
    public ExtendedNNTPOps() {
        client = new NNTPClient();
        client.addProtocolCommandListener(new PrintCommandListener(
                                          new PrintWriter(System.out)));
    }
	
    private Article[] getArticleInfo(int lowArticleNumber,
                                     int highArticleNumber)
        throws IOException 
    {
        ArrayList articles = new ArrayList();
        Reader reader = null;

        reader = (DotTerminatedMessageReader)
            client.retrieveArticleInfo(lowArticleNumber, highArticleNumber);

        if (reader != null) {
            String theInfo = readerToString(reader);
            StringTokenizer st = new StringTokenizer(theInfo, "\n");

            // Extract the article information
            // Mandatory format (from NNTP RFC 2980) is :
            // Subject\tAuthor\tDate\tID\tReference(s)\tByte Count\tLine Count
            while (st.hasMoreTokens()) {
                StringTokenizer stt = new StringTokenizer(st.nextToken(), "\t");
                Article article = new Article();
                article.setArticleNumber(Integer.parseInt(stt.nextToken()));
                article.setSubject(stt.nextToken());
                article.setFrom(stt.nextToken());
                article.setDate(stt.nextToken());
                article.setArticleId(stt.nextToken());
                article.addHeaderField("References", stt.nextToken());
                articles.add(article);
            }
        } else {
            return null;
        }
			
        return (Article[]) articles.toArray(new Article[articles.size()]);
    }
		
    private String readerToString(Reader reader) 
    {
        String temp;
        StringBuffer sb = null;
        BufferedReader bufReader = new BufferedReader(reader);
			
        sb = new StringBuffer();
        try 
            {
                temp = bufReader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    sb.append("\n");
                    temp = bufReader.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
			
        return sb.toString();
    }

    public void demo(String host, String user, String password) {
        try {
            client.connect(host);
		
            // AUTHINFO USER/AUTHINFO PASS
            boolean success = client.authenticate(user, password);
            if(success) 
		{
                    System.out.println("Authentication succeeded");
		}
            else 
		{
                    System.out.println("Authentication failed, error =" + 
                                       client.getReplyString());
		}
	
	    // XOVER
	    NewsgroupInfo testGroup = new NewsgroupInfo();
            client.selectNewsgroup("alt.test", testGroup);
            int lowArticleNumber = testGroup.getFirstArticle();
            int highArticleNumber = testGroup.getLastArticle();
	    Article[] articles =
                getArticleInfo(lowArticleNumber, highArticleNumber);
	    
	    for(int i =0; i < articles.length; ++i)
                {
                    System.out.println(articles[i].getSubject());	
                }
	    
	    // LIST ACTIVE 
	    NewsgroupInfo[] fanGroups = client.listNewsgroups("alt.fan.*");
	    for(int i = 0; i < fanGroups.length; ++i)
                {
                    System.out.println(fanGroups[i].getNewsgroup());
                }
	    
	    
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ExtendedNNTPOps ops;

        if(args.length != 3) {
            System.err.println(
                   "usage: ExtendedNNTPOps nntpserver username password");
            System.exit(1);
        }

        ops = new ExtendedNNTPOps();
        ops.demo(args[0], args[1], args[2]);
    }
	
}

/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
