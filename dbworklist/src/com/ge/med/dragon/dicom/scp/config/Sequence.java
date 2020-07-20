/**
 * Sequence.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scp.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Class Sequence.
 */
public class Sequence {
	private String tag;
	private boolean isRequired = false;
	
	private HashMap tag2ColumnMap;
	private HashMap column2TagMap;
	
	private List tags = new ArrayList();
	
	/**
	 * Gets the tags.
	 *
	 * @return Returns the tags.
	 */
	public List getTags() {
		return tags;
	}
	
	/**
	 * Sets the tags.
	 *
	 * @param tags The tags to set.
	 */
	public void setTags(List tags) {
		this.tags = tags;
	}
	
	/**
	 * Gets the column2 tag map.
	 *
	 * @return Returns the column2TagMap.
	 */
	public HashMap getColumn2TagMap() {
		return column2TagMap;
	}
	
	/**
	 * Sets the column2 tag map.
	 *
	 * @param column2TagMap The column2TagMap to set.
	 */
	public void setColumn2TagMap(HashMap column2TagMap) {
		this.column2TagMap = column2TagMap;
	}
	
	/**
	 * Checks if is required.
	 *
	 * @return Returns the isRequired.
	 */
	public boolean isRequired() {
		return isRequired;
	}
	
	/**
	 * Sets the required.
	 *
	 * @param isRequired The isRequired to set.
	 */
	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}
	
	/**
	 * Gets the tag.
	 *
	 * @return Returns the tag.
	 */
	public String getTag() {
		return tag;
	}
	
	/**
	 * Sets the tag.
	 *
	 * @param tag The tag to set.
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	/**
	 * Gets the tag2 column map.
	 *
	 * @return Returns the tag2ColumnMap.
	 */
	public HashMap getTag2ColumnMap() {
		return tag2ColumnMap;
	}
	
	/**
	 * Sets the tag2 column map.
	 *
	 * @param tag2ColumnMap The tag2ColumnMap to set.
	 */
	public void setTag2ColumnMap(HashMap tag2ColumnMap) {
		this.tag2ColumnMap = tag2ColumnMap;
	}
}


/******************************************************************************
 * Revision History 
 * [type 'revision' & press Alt + '/' to insert revision block]
 * 
 * 
 ******************************************************************************/
