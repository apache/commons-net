/***
 * $Id: ArticlePointer.java,v 1.1 2002/04/03 01:04:33 brekke Exp $
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
 * This class is a structure used to return article number and unique
 * id information extracted from an NNTP server reply.  You will normally
 * want this information when issuing a STAT command, implemented by
 * <a href="com.oroinc.net.nntp.NNTPClient.html#selectArticle">
 * selectArticle </a>.
 * <p>
 * <p>
 * @author Daniel F. Savarese
 * @see NNTPClient
 ***/

public final class ArticlePointer {
  /*** The number of the referenced article. ***/
  public int articleNumber;
  /***
   * The unique id of the referenced article, including the enclosing
   * &lt and &gt symbols which are technically not part of the
   * identifier, but are required by all NNTP commands taking an
   * article id as an argument.
   ***/
  public String articleId;
}
