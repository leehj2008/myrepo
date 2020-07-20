/**
 * MappingElement.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scp.config;


/**
 * The Class MappingElement.
 */
public class MappingElement {
	private String columnName;
	private String root;
	
	private GenerateValue generate;
	
	/**
	 * Gets the column name.
	 *
	 * @return Returns the columnName.
	 */
	public String getColumnName() {
		return columnName;
	}
	
	/**
	 * Sets the column name.
	 *
	 * @param columnName The columnName to set.
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	/**
	 * Gets the root.
	 *
	 * @return Returns the Value.
	 */
	public String getRoot() {
		return root;
	}
	
	/**
	 * Sets the root.
	 *
	 * @param defaultValue the new root
	 */
	public void setRoot(String defaultValue) {
		this.root = defaultValue;
	}
	
	/**
	 * Gets the generate.
	 *
	 * @return Returns the generate.
	 */
	public GenerateValue getGenerate() {
		return generate;
	}
	
	/**
	 * Sets the generate.
	 *
	 * @param generateC the new generate
	 * @throws ClassNotFoundException the class not found exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InstantiationException the instantiation exception
	 */
	public void setGenerate(String generateC) throws ClassNotFoundException, IllegalAccessException, InstantiationException{
		if(generateC == null || generateC.equals("")){
			this.generate = null;
			return;
		}
		
		this.generate = (GenerateValue)Class.forName(generateC).newInstance();
	}
}


/******************************************************************************
 * Revision History 
 * [type 'revision' & press Alt + '/' to insert revision block]
 * 
 * 
 ******************************************************************************/
