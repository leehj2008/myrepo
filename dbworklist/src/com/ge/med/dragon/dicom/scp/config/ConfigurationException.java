/**
 * ConfigurationException.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scp.config;

/**
 * The Class ConfigurationException.
 */
public class ConfigurationException extends Exception{
	
	/**
	 * Instantiates a new configuration exception.
	 */
	public ConfigurationException(){
		super();
	}
	
	/**
	 * Instantiates a new configuration exception.
	 *
	 * @param message the message
	 */
	public ConfigurationException(String message){
		super(message);
	}
}


/******************************************************************************
 * Revision History 
 * [type 'revision' & press Alt + '/' to insert revision block]
 * 
 * 
 ******************************************************************************/
