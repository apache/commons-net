/*
 * Copyright 2001-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.PrintWriter;
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
        Reader reader = null;
        Article[] articles = new Article[0];
        reader = (DotTerminatedMessageReader)
            client.retrieveArticleInfo(lowArticleNumber, highArticleNumber);

        if (reader != null) {
            String theInfo = readerToString(reader);
            StringTokenizer st = new StringTokenizer(theInfo, "\n");

            // Extract the article information
            // Mandatory format (from NNTP RFC 2980) is :
            // Subject\tAuthor\tDate\tID\tReference(s)\tByte Count\tLine Count

            int count = st.countTokens();
            articles = new Article[count];
            int index = 0;

            while (st.hasMoreTokens()) {
                StringTokenizer stt = new StringTokenizer(st.nextToken(), "\t");
                Article article = new Article();
                article.setArticleNumber(Integer.parseInt(stt.nextToken()));
                article.setSubject(stt.nextToken());
                article.setFrom(stt.nextToken());
                article.setDate(stt.nextToken());
                article.setArticleId(stt.nextToken());
                article.addHeaderField("References", stt.nextToken());
                articles[index++] = article;
            }
        } else {
            return null;
        }
			
        return articles;
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
