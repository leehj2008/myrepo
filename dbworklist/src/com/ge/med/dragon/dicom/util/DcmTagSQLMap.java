/**
 * DcmTagSQLMap.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.util;

import java.util.HashMap;
import java.util.Hashtable;

import org.dcm4che.dict.Tags;

import com.ge.med.dragon.dicom.scp.config.ConfigurationException;
import com.ge.med.dragon.dicom.scp.config.DcmModalityNode;

/**
 * The Class DcmTagSQLMap.
 *
 * @author Fan Yihui
 * @version $Revision: 1.2 $ $Date: 2001/12/14 03:44:33 $
 */

public class DcmTagSQLMap {
	private static Hashtable column2TagHashtable = new Hashtable();
	private static Hashtable sexCodesTable = new Hashtable();
	private static HashMap modalityNodeMap = new HashMap();
	
	/**
	 * Gets the map.
	 *
	 * @return the map
	 */
	public Hashtable getMap(){
		return column2TagHashtable;
	}
	
	/**
	 * Put modality node map.
	 *
	 * @param aet the aet
	 * @param dmn the dmn
	 */
	public static void putModalityNodeMap(String aet, DcmModalityNode dmn){
		modalityNodeMap.put(aet, dmn);
	}
	
	/**
	 * Gets the mwl map by aet.
	 *
	 * @param aet the aet
	 * @return the mwl map by aet
	 * @throws ConfigurationException the configuration exception
	 */
	public static DcmModalityNode getMwlMapByAet(String aet) throws ConfigurationException {
		Object o = modalityNodeMap.get(aet);
		if (o == null) {
			throw new ConfigurationException("The AETitle " + aet
					+ " can not be found in the MwlScp.xml file.");
		}

		return (DcmModalityNode) o;
	}
	
	/**
	 * Put c2 tag.
	 *
	 * @param key the key
	 * @param object the object
	 */
	public static void putC2Tag(String key, String object){
		column2TagHashtable.put(key, object);
	}
	
	/**
	 * Put sex code.
	 *
	 * @param key the key
	 * @param object the object
	 */
	public static void putSexCode(String key, String object){
		sexCodesTable.put(key, object);
	}
	
	/**
	 * Gets the column name.
	 *
	 * @param tag the tag
	 * @return the column name
	 */
	public static String getColumnName(int tag){
		Object o = column2TagHashtable.get(Tags.toString(tag));

		if (o == null) {
			return null;
		}
		
		return o.toString();
	}
	
	/**
	 * Gets the dCM sex code.
	 *
	 * @param value the value
	 * @return the dCM sex code
	 */
	public static String getDCMSexCode(String value) {
		Object o = sexCodesTable.get(value);

		if (o == null) {
			return "O";
		}

		String sex = o.toString();

		if (!sex.equals("F") && !sex.equals("M") && !sex.equals("O")) {
			sex = "O";
		}

		return sex;
	}
}
