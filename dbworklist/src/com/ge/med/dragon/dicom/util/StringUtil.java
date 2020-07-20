/**
 * StringUtil.java 1.0 2013-3-4
 *
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import com.ge.med.dragon.dicom.scp.config.PersonNameElement;

/**
 * The Class StringUtil.
 */
public final class StringUtil {

    private StringUtil() {

    }

    /**
     * To string array.
     *
     * @param value the value
     * @return the string[]
     */
    public static String[] toStringArray(String value) {
        StringTokenizer st = new StringTokenizer(value, ",");
        String[] str = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreElements()) {
            str[i] = (String) st.nextElement();
            i++;
        }

        return str;
    }

    /**
     * To string array.
     *
     * @param value the value
     * @param delim the delim
     * @return the string[]
     */
    public static String[] toStringArray(String value, String delim) {
        StringTokenizer st = new StringTokenizer(value, delim);
        String[] str;

        int i = 0;
        if (!value.contains(delim)) {
            str = new String[]{value};
            return str;
        } else if (value.startsWith(delim)) {
            str = new String[st.countTokens() + 1];
            str[i] = "";
            i++;
        } else if (value.endsWith(delim)) {
            str = new String[st.countTokens() + 1];
            str[str.length - 1] = "";
        } else {
            str = new String[st.countTokens()];
        }

        while (st.hasMoreElements()) {
            str[i] = (String) st.nextElement();
            i++;
        }

        return str;
    }

    /**
     * To date from str.
     *
     * @param dates the dates
     * @param df    the df
     * @return the date[]
     * @throws ParseException the parse exception
     */
    public static Date[] toDateFromStr(String[] dates, DateFormat df)
            throws ParseException {
        if (dates == null) {
            return null;
        }

        if (df == null) {
            df = new SimpleDateFormat("yyyyMMdd");
        }

        Date[] date = new Date[dates.length];
        for (int i = 0; i < dates.length; i++) {
            date[i] = df.parse(dates[i]);
        }

        return date;
    }

    /**
     * Replace delimiter.
     *
     * @param root      the root
     * @param delimiter the delimiter
     * @return the string
     */
    public static String replaceDelimiter(String root, String delimiter) {
        if (delimiter == null
                || !(delimiter.equals(PersonNameElement.CARET) || delimiter.equals(PersonNameElement.SPACE) || delimiter.equals(PersonNameElement.MULTISPACE))
                || delimiter.length() > 1) {
            return root;
        }

        root = root.replace(PersonNameElement.CARET.charAt(0), delimiter.charAt(0));
        root = root.replace(PersonNameElement.SPACE.charAt(0), delimiter.charAt(0));
        root = root.replace(PersonNameElement.MULTISPACE.charAt(0), delimiter.charAt(0));

        return root;
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        System.out.println(StringUtil.replaceDelimiter("jkdjfksdjf" + PersonNameElement.MULTISPACE + "kdjfkd", PersonNameElement.CARET));
    }
}
