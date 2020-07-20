/**
 * Configuration.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * <description> 
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision: 1.1 $ $Date: 2001/09/08 05:32:52 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go 
 *            beyond the cvs commit message
 * </ul>
 */
class Configuration extends Properties
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   /**
	 * 
	 */
	private static final long serialVersionUID = -8820202002145600149L;

// Static --------------------------------------------------------  
   private static String replace(String val, String from, String to) {
      return from.equals(val) ? to : val;
   }
   
   /**
    * Instantiates a new configuration.
    *
    * @param file the file
    */
   public Configuration(File file){
	   InputStream in = null;
	   
	   try {
	         load(new FileInputStream(file));
	      } catch (Exception e) {
	         throw new RuntimeException("Could not load configuration from "
	               + file, e);
	      } finally {
	         if (in != null) {
	            try { in.close(); } catch (IOException ignore) {}
	         }
	      }
   }
   
   // Constructors --------------------------------------------------
   /**
    * Instantiates a new configuration.
    *
    * @param url the url
    */
   public Configuration(URL url) {
      InputStream in = null;
      try {
         load(in = url.openStream());
      } catch (Exception e) {
         throw new RuntimeException("Could not load configuration from "
               + url, e);
      } finally {
         if (in != null) {
            try { in.close(); } catch (IOException ignore) {}
         }
      }
   }
   
   // Public --------------------------------------------------------
   /**
    * Gets the property.
    *
    * @param key the key
    * @param defaultValue the default value
    * @param replace the replace
    * @param to the to
    * @return the property
    */
   public String getProperty(String key, String defaultValue,
                             String replace, String to) {
      return replace(getProperty(key, defaultValue), replace, to);
   }
   
   /**
    * Tokenize.
    *
    * @param s the s
    * @param result the result
    * @return the list
    */
   public List tokenize(String s, List result) {
      StringTokenizer stk = new StringTokenizer(s, ", ");
      while (stk.hasMoreTokens()) {
         String tk = stk.nextToken();
         if (tk.startsWith("$")) {
            tokenize(getProperty(tk.substring(1),""), result);
         } else {
            result.add(tk);
         }
      }
      return result;
   }
   
   /**
    * Tokenize.
    *
    * @param s the s
    * @return the string[]
    */
   public String[] tokenize(String s) {
      if (s == null)
         return null;
      
      List l = tokenize(s, new LinkedList());      
      return (String[])l.toArray(new String[l.size()]);
   }
       
}
