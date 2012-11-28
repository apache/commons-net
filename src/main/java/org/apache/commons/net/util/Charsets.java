package org.apache.commons.net.util;

import java.nio.charset.Charset;

/**
 * Helps dealing with Charsets.
 * 
 * @since 3.3
 */
public class Charsets {

    /**
     * Returns a charset object for the given charset name.
     * 
     * @param charsetName
     *            The name of the requested charset; may be a canonical name, an alias, or null. If null, return the
     *            default charset.
     * @return A charset object for the named charset
     */
    public static Charset toCharset(String charsetName) {
        return charsetName == null ? Charset.defaultCharset() : Charset.forName(charsetName);
    }

    public static Charset toCharset(String charsetName, String defaultCharsetName) {
        return charsetName == null ? Charset.forName(defaultCharsetName) : Charset.forName(charsetName);
    }
}
