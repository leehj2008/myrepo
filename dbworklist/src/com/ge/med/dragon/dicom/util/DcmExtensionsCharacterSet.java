/**
 * DcmExtensionsCharacterSet.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.Character.UnicodeBlock;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * The Class DcmExtensionsCharacterSet.
 */
public class DcmExtensionsCharacterSet {
    private static Logger log = Logger.getLogger(DcmExtensionsCharacterSet.class);

    private static final String JIS0208FILE = "JISX0208.TXT";
    private static final String JIS0212FILE = "JISX0212.TXT";
    private static final String JIS0201FILE = "JIS0201.TXT";
    private static final String REPLACENONJIS = new String(new char[] { (char) Integer.parseInt("25CF", 16) });

    /** The Constant CHARSET. */
    public static final HashMap<String, String> CHARSET = new HashMap<String, String>();
    private static final HashMap<String, String> UNI2JIS0208MAP = new HashMap<String, String>();
    private static final HashMap<String, String> UNI2JIS0212MAP = new HashMap<String, String>();
    private static final HashMap<String, String> UNI2JIS0201MAP = new HashMap<String, String>();
    static {
        CHARSET.put("", "US-ASCII");
        CHARSET.put("ISO_IR 100", "ISO-8859-1");
        CHARSET.put("ISO_IR 101", "ISO-8859-2");
        CHARSET.put("ISO_IR 109", "ISO-8859-3");
        CHARSET.put("ISO_IR 110", "ISO-8859-4");
        CHARSET.put("ISO_IR 144", "ISO-8859-5");
        CHARSET.put("ISO_IR 127", "ISO-8859-6");
        CHARSET.put("ISO_IR 126", "ISO-8859-7");
        CHARSET.put("ISO_IR 138", "ISO-8859-8");
        CHARSET.put("ISO_IR 148", "ISO-8859-9");
        CHARSET.put("ISO_IR 13", "JIS_X0201");
        CHARSET.put("ISO_IR 166", "TIS-620");
        CHARSET.put("ISO 2022 IR 6", "US-ASCII");
        CHARSET.put("ISO 2022 IR 100", "ISO-8859-1");
        CHARSET.put("ISO 2022 IR 101", "ISO-8859-2");
        CHARSET.put("ISO 2022 IR 109", "ISO-8859-3");
        CHARSET.put("ISO 2022 IR 110", "ISO-8859-4");
        CHARSET.put("ISO 2022 IR 144", "ISO-8859-5");
        CHARSET.put("ISO 2022 IR 127", "ISO-8859-6");
        CHARSET.put("ISO 2022 IR 126", "ISO-8859-7");
        CHARSET.put("ISO 2022 IR 138", "ISO-8859-8");
        CHARSET.put("ISO 2022 IR 148", "ISO-8859-9");
        CHARSET.put("ISO 2022 IR 13", "JIS_X0201");
        CHARSET.put("ISO 2022 IR 166", "TIS-620");
        CHARSET.put("ISO 2022 IR 87", "JIS0208");
        CHARSET.put("ISO 2022 IR 159", "JIS0212");
        CHARSET.put("ISO 2022 IR 149", "cp949");
        CHARSET.put("ISO_IR 192", "UTF-8");
        CHARSET.put("GB18030", "GB18030");

        loadJIS0208MAP();
        loadJIS0212MAP();
        loadJIS0201MAP();
    }

    private static byte esc = 0x1b;

    private static byte[] usascii = { 0x28, 0x42 };
    private static byte[] iso2022ir100 = { 0x2d, 0x41 };
    private static byte[] iso2022ir101 = { 0x2d, 0x42 };
    private static byte[] iso2022ir109 = { 0x2d, 0x43 };
    private static byte[] iso2022ir110 = { 0x2d, 0x44 };
    private static byte[] iso2022ir144 = { 0x2d, 0x4c };
    private static byte[] iso2022ir127 = { 0x2d, 0x47 };
    private static byte[] iso2022ir126 = { 0x2d, 0x46 };
    private static byte[] iso2022ir138 = { 0x2d, 0x48 };
    private static byte[] iso2022ir148 = { 0x2d, 0x4d };
    // Thai
    private static byte[] iso2022ir166 = { 0x2d, 0x54 };

    // following is used for Japanese
    private static byte[] iso2022ir13katakana = { 0x29, 0x49 };
    private static byte[] iso2022ir13romaji = { 0x28, 0x4a };
    private static byte[] iso2022ir87 = { 0x24, 0x42 };
    private static byte[] iso2022ir159 = { 0x24, 0x28, 0x44 };

    // Korean
    private static byte[] iso2022ir149 = { 0x24, 0x29, 0x43 };

    private static int FIRSTCHARACTER = 1;
    private static int CHARACTER = 2;

    private static byte[] getEscapeSeq(String charset) {
        byte[] escapeSeq;

        if (charset == null || charset.equals("")) {
            escapeSeq = usascii;
        } else if (charset.equals("ISO 2022 IR 87")) {
            escapeSeq = iso2022ir87;
        } else if (charset.equals("ISO 2022 IR 159")) {
            escapeSeq = iso2022ir159;
        } else if (charset.equals("ISO 2022 IR 6")) {
            escapeSeq = usascii;
        } else if (charset.equals("ISO 2022 IR 100")) {
            escapeSeq = iso2022ir100;
        } else if (charset.equals("ISO 2022 IR 101")) {
            escapeSeq = iso2022ir101;
        } else if (charset.equals("ISO 2022 IR 109")) {
            escapeSeq = iso2022ir109;
        } else if (charset.equals("ISO 2022 IR 110")) {
            escapeSeq = iso2022ir110;
        } else if (charset.equals("ISO 2022 IR 144")) {
            escapeSeq = iso2022ir144;
        } else if (charset.equals("ISO 2022 IR 127")) {
            escapeSeq = iso2022ir127;
        } else if (charset.equals("ISO 2022 IR 126")) {
            escapeSeq = iso2022ir126;
        } else if (charset.equals("ISO 2022 IR 138")) {
            escapeSeq = iso2022ir138;
        } else if (charset.equals("ISO 2022 IR 148")) {
            escapeSeq = iso2022ir148;
        } else if (charset.equals("ISO 2022 IR 13")) {
            escapeSeq = iso2022ir13romaji;
        } else if (charset.equals("ISO 2022 IR 166")) {
            escapeSeq = iso2022ir166;
        } else if (charset.equals("ISO 2022 IR 149")) {
            escapeSeq = iso2022ir149;
        } else {
            return null;
        }

        return escapeSeq;
    }

    /**
     * Encode.
     * 
     * @param value
     *            the value
     * @param firstCharacterSet
     *            the first character set
     * @param characterSet
     *            the character set
     * @return the string
     * @throws UnsupportedEncodingException
     *             the unsupported encoding exception
     */
    public static String encode(String value, String firstCharacterSet, String characterSet) throws UnsupportedEncodingException {
        if (characterSet == null) {
            return value;
        }

        StringBuffer sb = new StringBuffer();

        // append shiftin escape sequences
        sb.append(new String(new byte[] { esc }));
        byte[] escapeSeq = getEscapeSeq(characterSet);
        if (escapeSeq == null) {
            return value;
        }
        sb.append(new String(escapeSeq));

        // append the data
        byte[] bytes = value.getBytes(CHARSET.get(characterSet).toString());
        sb.append(new String(bytes));

        // append the shiftout escape sequences
        sb.append(new String(new byte[] { esc }));
        byte[] shiftout = getEscapeSeq(firstCharacterSet);
        sb.append(new String(shiftout));

        System.out.println(sb.toString());

        return sb.toString();
    }

    // Resolve the parser error when there is mix characters in a string. For example, Kan-J + ASCII.
    /**
     * Encode for mix.
     * 
     * @param value
     *            the value
     * @param firstCharacterSet
     *            the first character set
     * @param characterSet
     *            the character set
     * @return the string
     * @throws UnsupportedEncodingException
     *             the unsupported encoding exception
     */
    public static String encodeForMix(String value, String firstCharacterSet, String characterSet) throws UnsupportedEncodingException {
        if (characterSet == null) {
            return value;
        }
        StringBuffer sb = new StringBuffer();
        int priorCharacter = FIRSTCHARACTER;

        for (int i = 0; i < value.length(); i++) {
            StringBuffer sbtemp = new StringBuffer();

            char c = value.charAt(i);
            sbtemp.append(c);

            if (UnicodeBlock.of(c).equals(UnicodeBlock.BASIC_LATIN) || isJIS0201Code(c)) {
                if (priorCharacter == CHARACTER) {
                    // Add shift out escape
                    sb.append(new String(new byte[] { esc }));
                    byte[] shiftout = getEscapeSeq(firstCharacterSet);
                    sb.append(new String(shiftout));
                }
                sb.append(c);
                priorCharacter = FIRSTCHARACTER;
            } else {
                if (priorCharacter == FIRSTCHARACTER) {
                    // Add shift in escape
                    sb.append(new String(new byte[] { esc }));
                    byte[] escapeSeq = getEscapeSeq(characterSet);
                    sb.append(new String(escapeSeq));
                }

                if (characterSet.equals("ISO 2022 IR 87") || characterSet.equals("ISO 2022 IR 159")) {
                    if (!isJISCode(c, characterSet)) {
                        sbtemp.replace(0, 1, "●");
                    }
                }

                // append the data
                byte[] bs = sbtemp.toString().getBytes(CHARSET.get(characterSet).toString());
                sb.append(new String(bs));

                priorCharacter = CHARACTER;

                if (i == value.length() - 1) {
                    // Add shift out escape
                    sb.append(new String(new byte[] { esc }));
                    byte[] shiftout = getEscapeSeq(firstCharacterSet);
                    sb.append(new String(shiftout));
                }
            }
        }

        System.out.println(sb.toString());

        return sb.toString();
    }

    /**
     * Encode.
     * 
     * @param value
     *            the value
     * @param codes
     *            the codes
     * @return the byte[]
     * @throws UnsupportedEncodingException
     *             the unsupported encoding exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static byte[] encode(String value, String[] codes) throws UnsupportedEncodingException, IOException {
        if (codes == null) {
            return value.getBytes("US-ASCII");
        }

        if (codes.length == 1) {
            return value.getBytes(CHARSET.get(codes[0]));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int priorCharacter = FIRSTCHARACTER;

        for (int i = 0; i < value.length(); i++) {
            StringBuffer sbtemp = new StringBuffer();

            char c = value.charAt(i);
            sbtemp.append(c);

            if (UnicodeBlock.of(c).equals(UnicodeBlock.BASIC_LATIN) || isJIS0201Code(c)) {
                if (priorCharacter == CHARACTER) {
                    // Add shift out escape
                    // baos.write(new byte[]{esc});
                    // byte[] shiftout = getEscapeSeq(codes[0]);
                    // baos.write(shiftout);
                    appendEscapeSeq(baos, codes[0]);
                }

                byte valueBytes[] = sbtemp.toString().getBytes(CHARSET.get(codes[0]));

                baos.write(valueBytes);

                priorCharacter = FIRSTCHARACTER;
            } else {
                if (priorCharacter == FIRSTCHARACTER) {
                    // Add shift in escape
                    // baos.write(new byte[]{esc});
                    // byte[] escapeSeq = getEscapeSeq(codes[1]);
                    // baos.write(escapeSeq);

                    appendEscapeSeq(baos, codes[1]);
                }

                if (codes[1].equals("ISO 2022 IR 87") || codes[1].equals("ISO 2022 IR 159")) {
                    if (!isJISCode(c, codes[1])) {
                        sbtemp.replace(0, 1, REPLACENONJIS);
                        System.out.println(sbtemp.toString());
                    }
                }

                // append the data
                byte[] bs = sbtemp.toString().getBytes(CHARSET.get(codes[1]).toString());

                baos.write(bs);

                priorCharacter = CHARACTER;

                if (i == value.length() - 1) {
                    appendEscapeSeq(baos, codes[0]);
                }
            }
        }

        /*
         * byte[] bytes = baos.toByteArray();
         * 
         * for(int i=0; i<bytes.length; i++){ System.out.print(bytes[i]); }
         */

        // System.out.println("after encoded:"+SpecificCharacterSet.valueOf(codes).decode(baos.toByteArray()));

        return baos.toByteArray();
    }

    private static boolean isJIS0201Code(char c) {
        String unicode = charToHex(c);
        if (UNI2JIS0201MAP.containsKey(unicode)) {
            return true;
        }
        return false;
    }

    private static boolean isJISCode(char c, String characterSet) {
        String unicode = charToHex(c);

        if (characterSet.equals("ISO 2022 IR 87")) {
            if (UNI2JIS0208MAP.containsKey(unicode)) {
                return true;
            }
        }

        if (characterSet.equals("ISO 2022 IR 159")) {
            if (UNI2JIS0212MAP.containsKey(unicode)) {
                return true;
            }
        }

        return false;
    }

    private static void loadResourceForCodeMap(String resource, HashMap<String, String> codeMap) throws IOException {
        Properties codeProperties = new Properties();
        InputStream is = null;
        try {
            is = DcmExtensionsCharacterSet.class.getResourceAsStream(resource);
            codeProperties.load(is);
        } finally {
            IOUtils.closeQuietly(is);
        }

        for (Iterator<Object> iter = codeProperties.keySet().iterator(); iter.hasNext();) {
            String nativeCode = (String) iter.next();
            String unicode = codeProperties.getProperty(nativeCode);

            if (nativeCode.startsWith("0x")) {
                nativeCode = nativeCode.replaceAll("0x", "");
            }

            if (unicode.startsWith("0x")) {
                unicode = unicode.replaceAll("0x", "");
            }

            codeMap.put(unicode, nativeCode);
        }
    }

    private static void loadJIS0208MAP() {
        try {
            loadResourceForCodeMap(JIS0208FILE, UNI2JIS0208MAP);
        } catch (IOException ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(), ex);
        }
    }

    private static void loadJIS0212MAP() {
        try {
            loadResourceForCodeMap(JIS0212FILE, UNI2JIS0212MAP);
        } catch (IOException ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(), ex);
        }
    }

    private static void loadJIS0201MAP() {
        try {
            loadResourceForCodeMap(JIS0201FILE, UNI2JIS0201MAP);
        } catch (IOException ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(), ex);
        }
    }

    private static String byteToHex(byte b) {
        // Returns hex String representation of byte b
        char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
        return new String(array).toUpperCase();
    }

    /**
     * Char to hex.
     * 
     * @param c
     *            the c
     * @return the string
     */
    public static String charToHex(char c) {
        int temp = (int) c;
        String hex = Integer.toHexString(temp);

        hex = hex.toUpperCase();

        return hex;
        // Returns hex String representation of char c
        /*
         * byte hi = (byte) (c >>> 8); byte lo = (byte) (c & 0xff); return byteToHex(hi) + byteToHex(lo);
         */
    }

    private static void appendEscapeSeq(ByteArrayOutputStream baos, String code) throws IOException {
        baos.write(new byte[] { esc });
        byte[] shiftout = getEscapeSeq(code);
        baos.write(shiftout);
    }

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        try {
            /*
             * String name = "sdf=河h彅h内=yuサトウ　シン"; String[] codes = new String[]{"ISO 2022 IR 13", "ISO 2022 IR 87"};
             * 
             * byte bytes[] = DcmExtensionsCharacterSet.encode(name, codes);
             * 
             * System.out.println(SpecificCharacterSet.valueOf(codes).decode(bytes));
             */
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
