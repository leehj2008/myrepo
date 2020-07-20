/**
 * UIDGenerate.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scp.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Class UIDGenerate.
 */
public class UIDGenerate implements GenerateValue{
	
	private DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	
	/**
	 * This method is used to generate the StudyInstanceUID.
	 *
	 * @param root the root
	 * @return the string
	 * @throws Exception the exception
	 */
	public String generate(String root) throws Exception{
	    	if(root == null || root.trim().equals("")){
	    	    	return "";
	    	}
	    
		String uid = root +"." + getSeed();
		
		validateUID(uid);
		
		return uid;
	}
	
	private String getSeed() {
		StringBuffer fileName = new StringBuffer();
		fileName.append(formatter.format(new Date()));
		fileName.append(".");
		fileName.append((int) (Math.random() * 1000));
		return fileName.toString();
	}
	
	private void validateUID(String uid) throws Exception{
		if(uid.length() > 64){
			throw new Exception("Generated UID exceeds 64 characters");
		}
	}
}


/******************************************************************************
 * Revision History 
 * [type 'revision' & press Alt + '/' to insert revision block]
 * 
 * 
 ******************************************************************************/
