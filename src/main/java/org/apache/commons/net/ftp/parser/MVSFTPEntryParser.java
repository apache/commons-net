/*
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.commons.net.ftp.parser;

import java.text.ParseException;
import java.util.List;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;

/**
 * Implementation of FTPFileEntryParser and FTPFileListParser for IBM zOS/MVS Systems.
 *
 * @author <a href="henrik.sorensen@balcab.ch">Henrik Sorensen</a>
 * 
 * wagely based on earlier work done by:
 * @author <a href="jnadler@srcginc.com">Jeff Nadler</a>
 * @author <a href="wnoto@openfinance.com">William Noto</a>

 * @version $Id$
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 */
public class MVSFTPEntryParser extends ConfigurableFTPFileEntryParserImpl {
	private static final boolean DEBUG_PARSER = true; // false;

	public static final int UNKNOWN_LIST_TYPE = -1;
	public static final int FILE_LIST_TYPE = 0;
	public static final int MEMBER_LIST_TYPE = 1;
	public static final int UNIX_LIST_TYPE = 2;
	public static final int JES_LEVEL1_LIST_TYPE = 3;
	public static final int JES_LEVEL2_LIST_TYPE = 4;
	private int isType = UNKNOWN_LIST_TYPE;

	private UnixFTPEntryParser unixFTPEntryParser;

	/**
	 * Dates are ignored for file lists, but are used for member 
	 * lists where possible
	 */
	static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd HH:mm"; // 2001/09/18 13:52

	/**
	 * Matches these entries:
	 *	Volume Unit    Referred Ext Used Recfm Lrecl BlkSz Dsorg Dsname
	 *	B10142 3390   2006/03/20  2   31  F       80    80  PS   MDI.OKL.WORK
	 * 
	 */
	/*public static final String REGEX_FILELIST = "\\S+\\s+" + //volume ignored
	"\\S+\\s+" + //unit - ignored
	"\\S+\\s+" + //access date - ignored
	"\\S+\\s+" + //extents -ignored
	"\\S+\\s+" + //used - ignored
	"[FV]\\S*\\s+" + //recfm - must start with F or V
	"\\S+\\s+" + //logical record length -ignored
	"\\S+\\s+" + //block size - ignored
	"(PS|PO|PO-E)\\s+" + // Dataset organisation. Many exist 
	//but only support: PS, PO, PO-E
	"(\\S+)\\s*"; // Dataset Name (file name)*/
	public static final String REGEX_FILELIST = "(.*)\\s+([^\\s]+)\\s*";

	/**
	 * Matches these entries: 
	 *   Name      VV.MM   Created       Changed      Size  Init   Mod   Id
	 *   TBSHELF   01.03 2002/09/12 2002/10/11 09:37    11    11     0 KIL001
	 */
	private static final String REGEX_MEMBERLIST = "(\\S+)\\s+" + //name
	"\\S+\\s+" + //version, modification (ignored)
	"\\S+\\s+" + //create date (ignored)
	"(\\S+)\\s+" + // modification date
	"(\\S+)\\s+" + // modification time
	"\\S+\\s+" + //size in lines (ignored)
	"\\S+\\s+" + //size in lines at creation(ignored)
	"\\S+\\s+" + //lines modified (ignored)
	"\\S+\\s*"; // id of user who modified (ignored)

	/**
	 * Matches these entries, note: no header: 
	 *   IBMUSER1  JOB01906  OUTPUT    3 Spool Files
	 *   012345678901234567890123456789012345678901234
	 *             1         2         3         4
	 */
	private static final String REGEX_JESLEVEL1LIST = "(\\S+)\\s+" + //job name ignored
	"(\\S+)\\s+" + //job number 
	"(\\S+)\\s+" + //job status (OUTPUT,INPUT,ACTIVE)
	"(\\S+)\\s+" + //number of spool files
	"(\\S+)\\s+" + //Text "Spool" ignored
	"(\\S+)\\s*" //Text "Files" ignored
	;

	/**
	 * TODO: the JES INTERFACE LEVEL 2 parser is still work in progress. 
	 * Matches these entries:
	 *            1         2         3         4
	 *  012345678901234567890123456789012345678901234567890
	 *ftp> quote site filetype=jes
	 200 SITE command was accepted
	 ftp> ls
	 200 Port request OK.
	 125 List started OK for JESJOBNAME=IBMUSER*, JESSTATUS=ALL and JESOWNER=IBMUSER
	 JOBNAME  JOBID    OWNER    STATUS CLASS
	 IBMUSER1 JOB01906 IBMUSER  OUTPUT A        RC=0000 3 spool files
	 IBMUSER  TSU01830 IBMUSER  OUTPUT TSU      ABEND=522 3 spool files
	 250 List completed successfully.
	 ftp> ls job01906
	 200 Port request OK.
	 125 List started OK for JESJOBNAME=IBMUSER*, JESSTATUS=ALL and JESOWNER=IBMUSER
	 JOBNAME  JOBID    OWNER    STATUS CLASS
	 IBMUSER1 JOB01906 IBMUSER  OUTPUT A        RC=0000
	 --------
	 ID  STEPNAME PROCSTEP C DDNAME   BYTE-COUNT
	 001 JES2              A JESMSGLG       858
	 002 JES2              A JESJCL         128
	 003 JES2              A JESYSMSG       443
	 3 spool files
	 250 List completed successfully.
	 */

	private static final String REGEX_JESLEVEL2LIST = "(\\S+)\\s+" + //job name ignored
	"(\\S+)\\s+" + //job number 
	"(\\S+)\\s+" + //job status (OUTPUT,INPUT,ACTIVE)
	"(\\S+)\\s+" + //number of spool files
	"(\\S+)\\s+" + //Text "Spool" ignored
	"(\\S+)\\s*" //Text "Files" ignored
	;

	/* ---------------------------------------------------------------------
	 * Very brief and incomplete description of the zOS/MVS-filesystem. 
	 * (Note: "zOS" is the operating system on the mainframe, 
	 *        and is the new name for MVS)
	 *         
	 * The filesystem on the mainframe does not have hierarchal structure as 
	 * for example the unix filesystem.
	 * For a more comprehensive description, please refer to the IBM manuals
	 * 
	 * @LINK: http://publibfp.boulder.ibm.com/cgi-bin/bookmgr/BOOKS/dgt2d440/CONTENTS
	 * 
	 * 
	 * Dataset names
	 * =============
	 * 
	 * A dataset name consist of a number of qualifiers separated by '.',
	 * each qualifier can be at most 8 characters, and the total length
	 * of a dataset can be max 44 characters including the dots.
	 * 
	 * 
	 * Dataset organisation
	 * ====================
	 * 
	 * A dataset represents a piece of storage allocated on one or more disks. 
	 * The structure of the storage is described with the field dataset organinsation (DSORG).
	 * There are a number of dataset organisations, but only two are usable for FTP transfer.
	 * 
	 * DSORG:
	 * PS: sequential, or flat file
	 * PO: partitioned dataset
	 * PO-E: extended partitioned dataset
	 * 
	 * The PS file is just a flat file, as you would find it on the unix
	 * file system.
	 * 
	 * The PO and PO-E files, can be compared to a single level directory structure.
	 * A PO file consist of a number of dataset members, or files if you
	 * will. It is possible to CD into the file, and to retrieve the 
	 * individual members.
	 * 
	 * 
	 * Dataset record format
	 * =====================
	 * 
	 * The physical layout of the dataset is described on the dataset itself.
	 * There are a number of record formats (RECFM), but just a few is relavant for 
	 * the FTP transfer.
	 * 
	 * Any one beginning with either F or V can safely used by FTP transfer.
	 * All others should only be used with great care, so this version will just
	 * ignore the other record formats.
	 * F means a fixed number of records per allocated storage, and V means a variable 
	 * number of records.
	 * 
	 * 
	 * Other notes
	 * ===========
	 *
	 * The file system supports automatically backup and retrieval of datasets. If a 
	 * file is backed up, the ftp LIST command will return:
	 *	ARCIVE Not Direct Access Device                          KJ.IOP998.ERROR.PL.UNITTEST
	 *
	 *
	 * Implementation notes
	 * ====================
	 *
	 * Only datasets that have dsorg PS, PO or PO-E and have recfm 
	 * beginning with F or V, is fully parsed.
	 * 
	 * The following fields in FTPFile is used:
	 * FTPFile.Rawlisting: Always set.
	 * FTPFile.Type: DIRECTORY_TYPE or FILE_TYPE or UNKNOWN
	 * FTPFile.Name: name
	 * FTPFile.Timestamp: change time or null
	 * 
	 */

	/**
	 * The sole constructor for a MVSFTPEntryParser object.
	 *
	 */
	public MVSFTPEntryParser() {
		super(""); // note the regex is set in preParse.
	}

	/**
	 * Parses a line of an z/OS - MVS FTP server file listing and converts it into a
	 * usable format in the form of an <code> FTPFile </code> instance.  If the
	 * file listing line doesn't describe a file, then <code> null </code> is returned.
	 * Otherwise a <code> FTPFile </code> instance representing the file is returned.
	 * 
	 * @param entry A line of text from the file listing
	 * @return An FTPFile instance corresponding to the supplied entry
	 */
	public FTPFile parseFTPEntry(String entry) {
		boolean isParsed;
		FTPFile f = new FTPFile();

		logPrintln("MVSFTPEntryParser entry >" + entry + "<");

		isParsed = false;
		if (isType == FILE_LIST_TYPE)
			isParsed = parseFileList(f, entry);
		else if (isType == MEMBER_LIST_TYPE) {
			isParsed = parseMemberList(f, entry);
			if (!isParsed)
				isParsed = parseSimpleEntry(f, entry);
		} else if (isType == UNIX_LIST_TYPE) {
			isParsed = parseUnixList(f, entry);
		} else if (isType == JES_LEVEL1_LIST_TYPE) {
			isParsed = parseJeslevel1List(f, entry);
		}

		if (isParsed)
			logPrintln("Name "
					+ f.getName()
					+ ", type "
					+ f.getType()
					+ ", timestamp "
					+ (f.getTimestamp() != null ? f.getTimestamp().getTime()
							.toString() : "null") + ", rawlisting "
							+ f.getRawListing());
		else {
			logPrintln("Entry did not parse ...");
			f = null; /* as per calling convention, parser must return null */
		}

		return f;
	}

	/**
	 * Parse entries representing a dataset list.
	 * Only datasets with DSORG PS or PO or PO-E 
	 * and with RECFM F* or V* will be parsed.
	 * 
	 * Format of ZOS/MVS file list:
	 *     0    1         2      3    4    5      6     7    8     9 
	 *	Volume Unit    Referred Ext Used Recfm Lrecl BlkSz Dsorg Dsname
	 *	B10142 3390   2006/03/20  2   31  F       80    80  PS   MDI.OKL.WORK
	 *	ARCIVE Not Direct Access Device                          KJ.IOP998.ERROR.PL.UNITTEST
	 *	B1N231 3390   2006/03/20  1   15  VB     256 27998  PO   PLU
	 *	B1N231 3390   2006/03/20  1   15  VB     256 27998  PO-E PLB
	 * 
	 * -----------------------------------
	 * [0] Volume
	 * [1] Unit
	 * [2] Referred
	 * [3] Ext: number of extents
	 * [4] Used
	 * [5] Recfm: Record format
	 * [6] Lrecl: Logical record length
	 * [7] BlkSz: Block size
	 * [8] Dsorg: Dataset organisation. Many exists but only support: PS, PO, PO-E
	 * [9] Dsname: Dataset name
	 * 
	 * Note: When volume is ARCIVE, it means the dataset is stored somewhere in
	 *       a tape archive. These entries is currently not supported by this
	 *       parser. A null value is returned.
	 *       
	 * @param f: will be updated with Name, Type, Timestamp if parsed.
	 * @param zosDirectoryEntry
	 * @return true: entry was parsed, false: entry was not parsed.
	 */
	private boolean parseFileList(FTPFile file, String entry) {
		logPrintln("MvsFilelistFTPEntryParser " + entry);
		if (matches(entry)) {
			file.setRawListing(entry);
			String name = group(2);
			String dsorg = group(1);
			logPrint(" parsed name " + name + ", dsorg " + dsorg);
			file.setName(name);

			//DSORG
			if ("PS".equals(dsorg)) {
				file.setType(FTPFile.FILE_TYPE);
				logPrintln("... is a file ");
			} else if ("PO".equals(dsorg) || "PO-E".equals(dsorg)) {
				// regex already ruled out anything other than
				// PO or PO-E 
				file.setType(FTPFile.DIRECTORY_TYPE);
				logPrintln("... is a directory ");
			} else {
				logPrintln("... is currently not supported.");
				return false;
			}

			return true;
		}

		return false;
	}

	/**
	 * Parse entries within a partitioned dataset.
	 * 
	 * Format of a memberlist within a PDS:
	 *    0         1        2          3        4     5     6      7    8
	 *   Name      VV.MM   Created       Changed      Size  Init   Mod   Id
	 *   TBSHELF   01.03 2002/09/12 2002/10/11 09:37    11    11     0 KIL001
	 *   TBTOOL    01.12 2002/09/12 2004/11/26 19:54    51    28     0 KIL001
	 *
	 * -------------------------------------------
	 * [0] Name
	 * [1] VV.MM: Version . modification
	 * [2] Created: yyyy / MM / dd
	 * [3,4] Changed: yyyy / MM / dd  HH:mm
	 * [5] Size: number of lines
	 * [6] Init: number of lines when first created
	 * [7] Mod: number of modified lines a last save
	 * [8] Id: User id for last update
	 * 
	 * 
	 * @param f: will be updated with Name, Type and Timestamp if parsed.
	 * @param zosDirectoryEntry
	 * @return true: entry was parsed, false: entry was not parsed.
	 */
	private boolean parseMemberList(FTPFile file, String entry) {

		logPrintln("MvsMemberlistFTPEntryParser " + entry);
		if (matches(entry)) {
			file.setRawListing(entry);
			String name = group(1);
			String datestr = group(2) + " " + group(3);
			logPrintln(" parsed name " + name + ", datestr " + datestr);
			file.setName(name);
			file.setType(FTPFile.FILE_TYPE);
			try {
				file.setTimestamp(super.parseTimestamp(datestr));
			} catch (ParseException e) { // just ignore parsing errors.
				//return null;  // this is a parsing failure too.
			}
			return true;
		}

		return false;
	}

	/**
	 * Assigns the name to the first word of the entry.
	 * Only to be used from a safe context, for example from
	 * a memberlist, where the regex for some reason fails.
	 * Then just assign the name field of FTPFile. 
	 * @param file
	 * @param entry
	 * @return
	 */
	private boolean parseSimpleEntry(FTPFile file, String entry) {

		logPrint("SimpleEntryParser " + entry);
		if (entry != null && entry.length() > 0) {
			file.setRawListing(entry);
			String name = entry.split(" ")[0];
			logPrintln(" parsed name " + name);
			file.setName(name);
			file.setType(FTPFile.FILE_TYPE);

			return true;
		}

		return false;
	}

	/**
	 * Parse the entry as a standard unix file.
	 * Using the UnixFTPEntryParser.
	 * @param file
	 * @param entry
	 * @return true: entry is parsed, false: entry could not be parsed.
	 */
	private boolean parseUnixList(FTPFile file, String entry) {
		file = unixFTPEntryParser.parseFTPEntry(entry);
		if (file == null)
			return false;
		return true;
	}

	/**
	 * Parse entries within a partitioned dataset.
	 * 
	 * Format of a memberlist within a PDS:
	 *    0         1        2          3        4     5     6      7    8
	 *   Name      VV.MM   Created       Changed      Size  Init   Mod   Id
	 *   TBSHELF   01.03 2002/09/12 2002/10/11 09:37    11    11     0 KIL001
	 *   TBTOOL    01.12 2002/09/12 2004/11/26 19:54    51    28     0 KIL001
	 *
	 * -------------------------------------------
	 * [0] Name
	 * [1] VV.MM: Version . modification
	 * [2] Created: yyyy / MM / dd
	 * [3,4] Changed: yyyy / MM / dd  HH:mm
	 * [5] Size: number of lines
	 * [6] Init: number of lines when first created
	 * [7] Mod: number of modified lines a last save
	 * [8] Id: User id for last update
	 * 
	 * 
	 * @param f: will be updated with Name, Type and Timestamp if parsed.
	 * @param zosDirectoryEntry
	 * @return true: entry was parsed, false: entry was not parsed.
	 */
	private boolean parseJeslevel1List(FTPFile file, String entry) {

		logPrintln("JES Level 1 listFTPEntryParser >" + entry + "<");
		if (matches(entry)) {
			logPrintln("JES Level 1 matches = true");
			logPrintln("group(0)=" + group(0));
			logPrintln("group(1)=" + group(1));
			logPrintln("group(2)=" + group(2));
			logPrintln("group(3)=" + group(3));
			logPrintln("group(4)=" + group(4));
			if (group(3).equalsIgnoreCase("OUTPUT")) {
				file.setRawListing(entry);
				String name = group(2); /* Job Number, used by GET */
				logPrintln(" parsed name " + name);

				file.setName(name);
				file.setType(FTPFile.FILE_TYPE);
				return true;
			}
		}

		return false;
	}

	/**
	 * preParse is called as part of the interface. Per definition is is called
	 * before the parsing takes place.
	 * Three kind of lists is recognize:
	 * 	z/OS-MVS File lists
	 *  z/OS-MVS Member lists
	 *  unix file lists  
	 */
	public List preParse(List orig) {
		// simply remove the header line.  Composite logic will take care of the two different types of 
		// list in short order.
		if (orig != null && orig.size() > 0) {
			String header = (String) orig.get(0);
			if (header.indexOf("Volume") >= 0 && header.indexOf("Dsname") >= 0) {
				setParseType(FILE_LIST_TYPE);
			} 
			else if (header.indexOf("Name") >= 0 && header.indexOf("Id") >= 0) {
				setParseType(MEMBER_LIST_TYPE);
			} 
			else if (header.indexOf("total") == 0) {
				setParseType(UNIX_LIST_TYPE);
			} 
			else if (header.indexOf("Spool Files") >= 30) {
				setParseType(JES_LEVEL1_LIST_TYPE);
				orig.remove(0);
			} 
			else if (header.indexOf("JOBNAME") == 0
					&& header.indexOf("JOBID") > 8) {// header contains JOBNAME JOBID OWNER STATUS CLASS
				setParseType(JES_LEVEL2_LIST_TYPE);
			} 
			else {
				isType = UNKNOWN_LIST_TYPE;
			}
		}
		return orig;
	}
	
	/**
	 * 
	 * @param parseType
	 * @throws IllegalArgumentException
	 */
	public void setParseType(int parseType) throws IllegalArgumentException {
		if (parseType == FILE_LIST_TYPE) {
			super.setRegex(REGEX_FILELIST);
		}
		else if (parseType == MEMBER_LIST_TYPE) {
			super.setRegex(REGEX_MEMBERLIST);
		}
		else if (parseType == UNIX_LIST_TYPE) {
			unixFTPEntryParser = new UnixFTPEntryParser();
		}
		else if (parseType == JES_LEVEL1_LIST_TYPE) {
			super.setRegex(REGEX_JESLEVEL1LIST);
		}
		else if (parseType == JES_LEVEL2_LIST_TYPE) {
			super.setRegex(REGEX_JESLEVEL2LIST);
		}
		else {
			throw new IllegalArgumentException("Argument must be a valid type!");
		}
		
		isType = parseType;
	}

	/* 
	 * @return
	 */
	protected FTPClientConfig getDefaultConfiguration() {
		return new FTPClientConfig(FTPClientConfig.SYST_MVS,
				DEFAULT_DATE_FORMAT, null, null, null, null);
	}

	/**
	 * poor-mans logging ... 
	 * @param string
	 */
	private void logPrint(String string) {
		if (DEBUG_PARSER)
			System.out.print(string);
	}

	private void logPrintln(String string) {
		if (DEBUG_PARSER)
			System.out.println(string);
	}
}
