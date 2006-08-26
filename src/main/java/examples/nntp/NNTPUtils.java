package examples.nntp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.StringTokenizer;

import org.apache.commons.net.io.DotTerminatedMessageReader;
import org.apache.commons.net.nntp.Article;
import org.apache.commons.net.nntp.NNTPClient;

/**
 * 
 * Some convenience methods for NNTP example classes.
 * 
 * @author Rory Winston <rwinston@checkfree.com>
 */
public class NNTPUtils {

	/**
	 * Given an {@link NNTPClient} instance, and an integer range of messages, return 
	 * an array of {@link Article} instances.
	 * @param client 
	 * @param lowArticleNumber
	 * @param highArticleNumber
	 * @return Article[] An array of Article
	 * @throws IOException
	 */
	public  static Article[] getArticleInfo(NNTPClient client, int lowArticleNumber, int highArticleNumber)
			throws IOException {
			Reader reader = null;
			Article[] articles = null;
			reader =
				(DotTerminatedMessageReader) client.retrieveArticleInfo(
					lowArticleNumber,
					highArticleNumber);

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
		
	
	/**
	 * Convert a {@link Reader} instance to a String
	 * @param reader The Reader instance
	 * @return String
	 */		
	public static String readerToString(Reader reader) {
		String temp = null;
		StringBuffer sb = null;
		BufferedReader bufReader = new BufferedReader(reader);

		sb = new StringBuffer();
		try {
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
}
