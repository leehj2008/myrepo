/**
 * FilterElement.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scp.config;

import org.apache.log4j.Logger;

/**
 * The Class FilterElement.
 */
public class FilterElement {
	private static Logger log = Logger.getLogger(FilterElement.class);
	
	private String columnName;
	private String value;
	private GenerateValue generate;
	
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
	public void setGenerate(String generateC) throws ClassNotFoundException,
			IllegalAccessException, InstantiationException {
		if(generateC == null || generateC.equals("")){
			this.generate = null;
			return;
		}
		
		Class c = Class.forName(generateC);

		this.generate = (GenerateValue) c.newInstance();
	}
	
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
	 * Gets the value.
	 *
	 * @return Returns the value.
	 */
	public String getValue() {
		if(generate == null){
			return value;
		}
		
		try{
			return generate.generate(value);
		} catch(Exception ex){
			log.error(ex.getMessage(), ex);
			return value;
		}
	}
	
	/**
	 * Sets the value.
	 *
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/** {@inheritDoc. Override} */
	public String toString(){
		if(generate == null){
			return columnName + "='" + value +"'";
		} else {
			try{
				return columnName + "='" + generate.generate(value) +"'";
			} catch(Exception ex){
				log.error(ex.getMessage(), ex);
				return "";
			}
		}
	}
}


/******************************************************************************
 * Revision History 
 * [type 'revision' & press Alt + '/' to insert revision block]
 * 
 * 
 ******************************************************************************/
