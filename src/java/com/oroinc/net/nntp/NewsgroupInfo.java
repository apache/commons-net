/***
 * $Id: NewsgroupInfo.java,v 1.1 2002/04/03 01:04:35 brekke Exp $
 *
 * NetComponents Internet Protocol Library
 * Copyright (C) 1997-2002  Daniel F. Savarese
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library in the LICENSE file; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 ***/

package com.oroinc.net.nntp;

/***
 * NewsgroupInfo stores information pertaining to a newsgroup returned by
 * the NNTP GROUP, LIST, and NEWGROUPS commands, implemented by
 * <a href="com.oroinc.net.nntp.NNTPClient.html#selectNewsgroup">
 * selectNewsgroup </a>,
 * <a href="com.oroinc.net.nntp.NNTPClient.html#listNewsgroups">
 * listNewsgroups </a>, and
 * <a href="com.oroinc.net.nntp.NNTPClient.html#listNewNewsgroups">
 * listNewNewsgroups </a> respectively.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see NNTPClient
 ***/

public final class NewsgroupInfo {
  /***
   * A constant indicating that the posting permission of a newsgroup is
   * unknown.  For example, the NNTP GROUP command does not return posting
   * information, so NewsgroupInfo instances obtained from that command
   * willhave an UNKNOWN_POSTING_PERMISSION.
   ***/
  public static final int UNKNOWN_POSTING_PERMISSION    = 0;

  /*** A constant indicating that a newsgroup is moderated. ***/
  public static final int MODERATED_POSTING_PERMISSION  = 1;

  /*** A constant indicating that a newsgroup is public and unmoderated. ***/
  public static final int PERMITTED_POSTING_PERMISSION  = 2;

  /*** 
   * A constant indicating that a newsgroup is closed for general posting.
   ***/
  public static final int PROHIBITED_POSTING_PERMISSION = 3;

  private String __newsgroup;
  private int __estimatedArticleCount;
  private int __firstArticle, __lastArticle;
  private int __postingPermission;

  void _setNewsgroup(String newsgroup) { __newsgroup = newsgroup; }

  void _setArticleCount(int count) { __estimatedArticleCount = count; }

  void _setFirstArticle(int first) { __firstArticle = first; }

  void _setLastArticle(int last) { __lastArticle = last; }

  void _setPostingPermission(int permission) {
    __postingPermission = permission;
  }

  /*** 
   * Get the newsgroup name.
   * <p>
   * @return The name of the newsgroup.
   ***/
  public String getNewsgroup() { return __newsgroup; }

  /*** 
   * Get the estimated number of articles in the newsgroup.  The
   * accuracy of this value will depend on the server implementation.
   * <p>
   * @return The estimated number of articles in the newsgroup.
   ***/
  public int getArticleCount() { return __estimatedArticleCount; }

  /*** 
   * Get the number of the first article in the newsgroup.
   * <p>
   * @return The number of the first article in the newsgroup.
   ***/
  public int getFirstArticle() { return __firstArticle; }

  /*** 
   * Get the number of the last article in the newsgroup.
   * <p>
   * @return The number of the last article in the newsgroup.
   ***/
  public int getLastArticle()  { return __lastArticle; }

  /*** 
   * Get the posting permission of the newsgroup.  This will be one of
   * the <code> POSTING_PERMISSION </code> constants.
   * <p>
   * @return The posting permission status of the newsgroup.
   ***/
  public int getPostingPermission() { return __postingPermission; }

  /*
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(__newsgroup);
    buffer.append(' ');
    buffer.append(__lastArticle);
    buffer.append(' ');
    buffer.append(__firstArticle);
    buffer.append(' ');
    switch(__postingPermission) {
      case 1: buffer.append('m'); break;
      case 2: buffer.append('y'); break;
      case 3: buffer.append('n'); break;
    }
    return buffer.toString();
  }
  */
}
