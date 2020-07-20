/**
 * DicomUtil.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;

/**
 * The Class DicomUtil.
 *
 * @author Fan Yihui
 * @version $Revision: 1.1 $ $Date: 2001/09/08 05:32:52 $
 */

public class DicomUtil {
    
    private final static String DATE_FORMAT = "yyyyMMdd";
	
	/**
	 * Adjust dataset.
	 *
	 * @param ds the ds
	 * @param keys the keys
	 */
	public static void adjustDataset(Dataset ds, Dataset keys) {
        for (Iterator it = keys.iterator(); it.hasNext();) {
            DcmElement key = (DcmElement) it.next();
            final int tag = key.tag();
            if (tag == Tags.SpecificCharacterSet) {
            	//ds.putCS(tag, keys.getString(tag));
            	continue;
            }

            final int vr = key.vr();
            DcmElement el = ds.get(tag);
            if (el == null) {
                el = ds.putXX(tag, vr);
            }
            if (vr == VRs.SQ) {
                Dataset keyItem = key.getItem();
                if (keyItem != null) {
                	if (el.isEmpty()) el.addNewItem();
                    for (int i = 0, n = el.vm(); i < n; ++i) {
                        adjustDataset(el.getItem(i), keyItem);
                    }
                }
            }
        }
    }
	
	/**
	 * Adjust dataset.
	 *
	 * @param ds the ds
	 * @param keys the keys
	 * @param encode the encode
	 */
	public static void adjustDataset(Dataset ds, Dataset keys, String[] encode) {
        for (Iterator it = keys.iterator(); it.hasNext();) {
            DcmElement key = (DcmElement) it.next();
            final int tag = key.tag();
            if (tag == Tags.SpecificCharacterSet) {
            	if(!keys.contains(Tags.SpecificCharacterSet) || keys.getString(tag) == null ){
            		ds.putCS(tag, encode);
            	} else {
            		String[] encodes = keys.getStrings(Tags.SpecificCharacterSet);
            		
            		ds.putCS(tag, encodes);
            	}
            	continue;
            }

            final int vr = key.vr();
            DcmElement el = ds.get(tag);
            if (el == null) {
                el = ds.putXX(tag, vr);
            }
            if (vr == VRs.SQ) {
                Dataset keyItem = key.getItem();
                if (keyItem != null) {
                	if (el.isEmpty()) el.addNewItem();
                    for (int i = 0, n = el.vm(); i < n; ++i) {
                        adjustDataset(el.getItem(i), keyItem, encode);
                    }
                }
            }
        }
    }
	
	/**
	 * Put element.
	 *
	 * @param ds the ds
	 * @param vr the vr
	 * @param tag the tag
	 * @param value the value
	 */
	public static void putElement(Dataset ds, int vr, int tag, String value){
		switch (vr) {
		case VRs.AE:
			ds.putAE(tag, value);
			break;
		case VRs.AS:
			ds.putAS(tag, value);
			break;
		case VRs.AT:
			ds.putAT(tag, value);
			break;
		case VRs.CS:
			ds.putCS(tag, value);
			break;
		case VRs.DA:
			value = removeChar(value, '-');
			Date date = null;
			try{
				date = new SimpleDateFormat(DATE_FORMAT).parse(value);
			} catch(ParseException e){
				value = "";
			}
			if(date == null || value == null || value.trim().equals("")){
				value = "19000101";
			} else {
				value = new SimpleDateFormat(DATE_FORMAT).format(date);
			}
			ds.putDA(tag, value);
			break;
		case VRs.DS:
			ds.putDS(tag, value);
			break;
		case VRs.DT:
			ds.putDT(tag, value);
			break;
		case VRs.FD:
			ds.putFD(tag, value);
			break;
		case VRs.FL:
			ds.putFL(tag, value);
			break;
		case VRs.IS:
			ds.putIS(tag, value);
			break;
		case VRs.LO:
			ds.putLO(tag, value);
			break;
		case VRs.LT:
			ds.putLT(tag, value);
			break;
		case VRs.PN:
			ds.putPN(tag, value);
			break;
		case VRs.SH:
			ds.putSH(tag, value);
			break;
		case VRs.SL:
			ds.putSL(tag, value);
			break;
		case VRs.SQ:
			ds.putSQ(tag);
			break;
		case VRs.SS:
			ds.putSS(tag, value);
			break;
		case VRs.ST:
			ds.putST(tag, value);
			break;
		case VRs.TM:
			ds.putTM(tag, value);
			break;
		case VRs.UI:
			ds.putUI(tag, value);
			break;
		case VRs.UL:
			ds.putUL(tag, value);
			break;
		case VRs.US:
			ds.putUS(tag, value);
			break;
		case VRs.UT:
			ds.putUT(tag, value);
			break;
		}
	}
	
	private static String removeChar(String source, char c){
		String s = "";
		for(int i = 0; i<source.length(); i++){
			char cc = source.charAt(i);
			if(cc != c){
				s = s+source.charAt(i);
			}
		}
		
		return s;
	}
}
