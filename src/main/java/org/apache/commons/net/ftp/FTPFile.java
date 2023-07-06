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

package org.apache.commons.net.ftp;

import java.io.Serializable;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.TimeZone;

/**
 * The FTPFile class is used to represent information about files stored on an FTP server.
 *
 * @see FTPFileEntryParser
 * @see FTPClient#listFiles
 */
public class FTPFile implements Serializable {

    private static final long serialVersionUID = 9010790363003271996L;

    /** A constant indicating an FTPFile is a file. */
    public static final int FILE_TYPE = 0;

    /** A constant indicating an FTPFile is a directory. */
    public static final int DIRECTORY_TYPE = 1;

    /** A constant indicating an FTPFile is a symbolic link. */
    public static final int SYMBOLIC_LINK_TYPE = 2;

    /** A constant indicating an FTPFile is of unknown type. */
    public static final int UNKNOWN_TYPE = 3;

    /** A constant indicating user access permissions. */
    public static final int USER_ACCESS = 0;

    /** A constant indicating group access permissions. */
    public static final int GROUP_ACCESS = 1;

    /** A constant indicating world access permissions. */
    public static final int WORLD_ACCESS = 2;

    /** A constant indicating file/directory read permission. */
    public static final int READ_PERMISSION = 0;

    /** A constant indicating file/directory write permission. */
    public static final int WRITE_PERMISSION = 1;

    /** A constant indicating file execute permission or directory listing permission. */
    public static final int EXECUTE_PERMISSION = 2;

    private int type = UNKNOWN_TYPE;

    /** 0 is invalid as a link count. */
    private int hardLinkCount;

    /** 0 is valid, so use -1. */
    private long size = -1;
    private String rawListing;
    private String user = "";
    private String group = "";
    private String name;
    private String link;

    // TODO Consider changing internal representation to java.time.
    private Calendar calendar;

    /** If this is null, then list entry parsing failed. */
    private final boolean[][] permissions; // e.g. _permissions[USER_ACCESS][READ_PERMISSION]

    /** Creates an empty FTPFile. */
    public FTPFile() {
        permissions = new boolean[3][3];
    }

    /**
     * Constructor for use by {@link FTPListParseEngine} only. Used to create FTPFile entries for failed parses
     *
     * @param rawListing line that could not be parsed.
     * @since 3.4
     */
    FTPFile(final String rawListing) {
        this.permissions = null; // flag that entry is invalid
        this.rawListing = rawListing;
    }

    private char formatType() {
        switch (type) {
        case FILE_TYPE:
            return '-';
        case DIRECTORY_TYPE:
            return 'd';
        case SYMBOLIC_LINK_TYPE:
            return 'l';
        default:
            return '?';
        }
    }

    /**
     * Gets the name of the group owning the file. Sometimes this will be a string representation of the group number.
     *
     * @return The name of the group owning the file.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Gets the number of hard links to this file. This is not to be confused with symbolic links.
     *
     * @return The number of hard links to this file.
     */
    public int getHardLinkCount() {
        return hardLinkCount;
    }

    /**
     * If the FTPFile is a symbolic link, this method returns the name of the file being pointed to by the symbolic link.
     * Otherwise, it returns {@code null}.
     *
     * @return The file pointed to by the symbolic link ({@code null} if the FTPFile is not a symbolic link).
     */
    public String getLink() {
        return link;
    }

    /**
     * Gets the name of the file.
     *
     * @return The name of the file.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the original FTP server raw listing used to initialize the FTPFile.
     *
     * @return The original FTP server raw listing used to initialize the FTPFile.
     */
    public String getRawListing() {
        return rawListing;
    }

    /**
     * Gets the file size in bytes.
     *
     * @return The file size in bytes.
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the file timestamp. This usually the last modification time.
     *
     * @return A Calendar instance representing the file timestamp.
     */
    public Calendar getTimestamp() {
        return calendar;
    }

    /**
     * Gets the file timestamp. This usually the last modification time.
     *
     * @return A Calendar instance representing the file timestamp.
     * @since 3.9.0
     */
    public Instant getTimestampInstant() {
        return calendar == null ? null : calendar.toInstant();
    }

    /**
     * Gets the type of the file (one of the {@code _TYPE} constants), e.g., if it is a directory, a regular file, or a symbolic link.
     *
     * @return The type of the file.
     */
    public int getType() {
        return type;
    }

    /**
     * Gets the name of the user owning the file. Sometimes this will be a string representation of the user number.
     *
     * @return The name of the user owning the file.
     */
    public String getUser() {
        return user;
    }

    /**
     * Tests if the given access group (one of the {@code _ACCESS} constants) has the given access permission (one of the {@code _PERMISSION}
     * constants) to the file.
     *
     * @param access     The access group (one of the {@code _ACCESS} constants)
     * @param permission The access permission (one of the {@code _PERMISSION} constants)
     * @throws ArrayIndexOutOfBoundsException if either of the parameters is out of range
     * @return {@code true} if {@link #isValid()} is {@code true} and the associated permission is set; {@code false} otherwise.
     */
    public boolean hasPermission(final int access, final int permission) {
        if (permissions == null) {
            return false;
        }
        return permissions[access][permission];
    }

    /**
     * Tests if the file is a directory.
     *
     * @return {@code true} if the file is of type {@code DIRECTORY_TYPE}, {@code false} if not.
     */
    public boolean isDirectory() {
        return type == DIRECTORY_TYPE;
    }

    /**
     * Tests if the file is a regular file.
     *
     * @return {@code true} if the file is of type {@code FILE_TYPE}, {@code false} if not.
     */
    public boolean isFile() {
        return type == FILE_TYPE;
    }

    /**
     * Tests if the file is a symbolic link.
     *
     * @return {@code true} if the file is of type {@code SYMBOLIC_LINK_TYPE}, {@code false} if not.
     */
    public boolean isSymbolicLink() {
        return type == SYMBOLIC_LINK_TYPE;
    }

    /**
     * Tests if the type of the file is unknown.
     *
     * @return {@code true} if the file is of type {@code UNKNOWN_TYPE}, {@code false} if not.
     */
    public boolean isUnknown() {
        return type == UNKNOWN_TYPE;
    }

    /**
     * Tests whether an entry is valid or not. If the entry is invalid, only the {@link #getRawListing()} method will be useful. Other methods may fail.
     *
     * Used in conjunction with list parsing that preserves entries that failed to parse.
     *
     * @see FTPClientConfig#setUnparseableEntries(boolean)
     * @return {@code true} if the entry is valid; {@code false} otherwise
     * @since 3.4
     */
    public boolean isValid() {
        return permissions != null;
    }

    private String permissionToString(final int access) {
        final StringBuilder sb = new StringBuilder();
        if (hasPermission(access, READ_PERMISSION)) {
            sb.append('r');
        } else {
            sb.append('-');
        }
        if (hasPermission(access, WRITE_PERMISSION)) {
            sb.append('w');
        } else {
            sb.append('-');
        }
        if (hasPermission(access, EXECUTE_PERMISSION)) {
            sb.append('x');
        } else {
            sb.append('-');
        }
        return sb.toString();
    }

    private void readObject(final java.io.ObjectInputStream in) {
        throw new UnsupportedOperationException("Serialization is not supported");
    }

    /**
     * Sets the name of the group owning the file. This may be a string representation of the group number.
     *
     * @param group The name of the group owning the file.
     */
    public void setGroup(final String group) {
        this.group = group;
    }

    /**
     * Sets the number of hard links to this file. This is not to be confused with symbolic links.
     *
     * @param links The number of hard links to this file.
     */
    public void setHardLinkCount(final int links) {
        this.hardLinkCount = links;
    }

    /**
     * If the FTPFile is a symbolic link, use this method to set the name of the file being pointed to by the symbolic link.
     *
     * @param link The file pointed to by the symbolic link.
     */
    public void setLink(final String link) {
        this.link = link;
    }

    /**
     * Sets the name of the file.
     *
     * @param name The name of the file.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets if the given access group (one of the {@code _ACCESS} constants) has the given access permission (one of the {@code _PERMISSION}
     * constants) to the file.
     *
     * @param access     The access group (one of the {@code _ACCESS} constants)
     * @param permission The access permission (one of the {@code _PERMISSION} constants)
     * @param value      {@code true} if permission is allowed, {@code false} if not.
     * @throws ArrayIndexOutOfBoundsException if either of the parameters is out of range
     */
    public void setPermission(final int access, final int permission, final boolean value) {
        // TODO: only allow permission setting if file is valid
        permissions[access][permission] = value;
    }

    /**
     * Sets the original FTP server raw listing from which the FTPFile was created.
     *
     * @param rawListing The raw FTP server listing.
     */
    public void setRawListing(final String rawListing) {
        this.rawListing = rawListing;
    }

    /**
     * Sets the file size in bytes.
     *
     * @param size The file size in bytes.
     */
    public void setSize(final long size) {
        this.size = size;
    }

    /**
     * Sets the file timestamp. This usually the last modification time. The parameter is not cloned, so do not alter its value after calling this method.
     *
     * @param date A Calendar instance representing the file timestamp.
     */
    public void setTimestamp(final Calendar date) {
        this.calendar = date;
    }

    /**
     * Sets the type of the file ({@code DIRECTORY_TYPE}, {@code FILE_TYPE}, etc.).
     *
     * @param type The integer code representing the type of the file.
     */
    public void setType(final int type) {
        this.type = type;
    }

    /**
     * Sets the name of the user owning the file. This may be a string representation of the user number;
     *
     * @param user The name of the user owning the file.
     */
    public void setUser(final String user) {
        this.user = user;
    }

    /**
     * Gets a string representation of the FTPFile information. This currently mimics the Unix listing format. This method uses the time zone of the Calendar
     * entry, which is the server time zone (if one was provided) otherwise it is the local time zone.
     * <p>
     * Note: if the instance is not valid {@link #isValid()}, no useful information can be returned. In this case, use {@link #getRawListing()} instead.
     * </p>
     *
     * @return A string representation of the FTPFile information.
     * @since 3.0
     */
    public String toFormattedString() {
        return toFormattedString(null);
    }

    /**
     * Gets a string representation of the FTPFile information. This currently mimics the Unix listing format. This method allows the Calendar time zone to be
     * overridden.
     * <p>
     * Note: if the instance is not valid {@link #isValid()}, no useful information can be returned. In this case, use {@link #getRawListing()} instead.
     * </p>
     *
     * @param timezone the time zone to use for displaying the time stamp If {@code null}, then use the Calendar ({@link #getTimestamp()}) entry
     * @return A string representation of the FTPFile information.
     * @since 3.4
     */
    public String toFormattedString(final String timezone) {

        if (!isValid()) {
            return "[Invalid: could not parse file entry]";
        }
        final StringBuilder sb = new StringBuilder();
        try (final Formatter fmt = new Formatter(sb)) {
            sb.append(formatType());
            sb.append(permissionToString(USER_ACCESS));
            sb.append(permissionToString(GROUP_ACCESS));
            sb.append(permissionToString(WORLD_ACCESS));
            fmt.format(" %4d", Integer.valueOf(getHardLinkCount()));
            fmt.format(" %-8s %-8s", getUser(), getGroup());
            fmt.format(" %8d", Long.valueOf(getSize()));
            Calendar timestamp = getTimestamp();
            if (timestamp != null) {
                if (timezone != null) {
                    final TimeZone newZone = TimeZone.getTimeZone(timezone);
                    if (!newZone.equals(timestamp.getTimeZone())) {
                        final Date original = timestamp.getTime();
                        final Calendar newStamp = Calendar.getInstance(newZone);
                        newStamp.setTime(original);
                        timestamp = newStamp;
                    }
                }
                fmt.format(" %1$tY-%1$tm-%1$td", timestamp);
                // Only display time units if they are present
                if (timestamp.isSet(Calendar.HOUR_OF_DAY)) {
                    fmt.format(" %1$tH", timestamp);
                    if (timestamp.isSet(Calendar.MINUTE)) {
                        fmt.format(":%1$tM", timestamp);
                        if (timestamp.isSet(Calendar.SECOND)) {
                            fmt.format(":%1$tS", timestamp);
                            if (timestamp.isSet(Calendar.MILLISECOND)) {
                                fmt.format(".%1$tL", timestamp);
                            }
                        }
                    }
                    fmt.format(" %1$tZ", timestamp);
                }
            }
            sb.append(' ');
            sb.append(getName());
        }
        return sb.toString();
    }

    /*
     * Serialization is unnecessary for this class. Reject attempts to do so until such time as the Serializable attribute can be dropped.
     */

    /**
     * Gets a string representation of the FTPFile information.
     * Delegates to {@link #getRawListing()}
     *
     * @see #getRawListing()
     * @return A string representation of the FTPFile information.
     */
    @Override
    public String toString() {
        return getRawListing();
    }

    private void writeObject(final java.io.ObjectOutputStream out) {
        throw new UnsupportedOperationException("Serialization is not supported");
    }

}
