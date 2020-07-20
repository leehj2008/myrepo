/**
 * DcmModalityNode.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scp.config;

import java.util.HashMap;
import java.util.List;

import org.dcm4che.dict.Tags;


/**
 * The Class DcmModalityNode.
 */
public class DcmModalityNode {
	private String aeTitle;
	private String[] encode;
	private boolean isUsingMatchKey;
	
	private List filter;
	
	private List tags;
	private List columns;
	
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
	private HashMap tag2ColumnMap;
	private HashMap column2TagMap;
	private HashMap tag2SeqMap;
	
	/**
	 * Gets the ae title.
	 *
	 * @return Returns the aeTitle.
	 */
	public String getAeTitle() {
		return aeTitle;
	}
	
	/**
	 * Sets the ae title.
	 *
	 * @param aeTitle The aeTitle to set.
	 */
	public void setAeTitle(String aeTitle) {
		this.aeTitle = aeTitle;
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
	 * Gets the encode.
	 *
	 * @return Returns the encode.
	 */
	public String[] getEncode() {
		return encode;
	}
	
	/**
	 * Sets the encode.
	 *
	 * @param encode The encode to set.
	 */
	public void setEncode(String[] encode) {
		this.encode = encode;
	}
	
	/**
	 * Gets the filter.
	 *
	 * @return Returns the filter.
	 */
	public List getFilter() {
		return filter;
	}
	
	/**
	 * Sets the filter.
	 *
	 * @param filter The filter to set.
	 */
	public void setFilter(List filter) {
		this.filter = filter;
	}
	
	/**
	 * Checks if is using match key.
	 *
	 * @return Returns the isUsingMatchKey.
	 */
	public boolean isUsingMatchKey() {
		return isUsingMatchKey;
	}
	
	/**
	 * Sets the using match key.
	 *
	 * @param isUsingMatchKey The isUsingMatchKey to set.
	 */
	public void setUsingMatchKey(boolean isUsingMatchKey) {
		this.isUsingMatchKey = isUsingMatchKey;
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
	
	/**
	 * Gets the column by tag.
	 *
	 * @param tag the tag
	 * @return the column by tag
	 */
	public MappingElement getColumnByTag(String tag){
		Object o = tag2ColumnMap.get(tag);
		
		if(o == null){
			return null;
		}
		
		return (MappingElement)o;
	}
	
	/**
	 * Gets the column by tag.
	 *
	 * @param tag the tag
	 * @return the column by tag
	 */
	public MappingElement getColumnByTag(int tag){
		Object o = tag2ColumnMap.get(Tags.toString(tag));

		if (o == null) {
			return null;
		}
		
		return (MappingElement)o;
	}
	
	/**
	 * Gets the person name element by tag.
	 *
	 * @param tag the tag
	 * @return the person name element by tag
	 */
	public PersonNameElement getPersonNameElementByTag(int tag){
		Object o = tag2ColumnMap.get(Tags.toString(tag));

		if (o == null) {
			return null;
		}
		
		return (PersonNameElement)o;
	}
	
	/**
	 * Gets the tag by column.
	 *
	 * @param column the column
	 * @return the tag by column
	 */
	public String getTagByColumn(String column){
		Object o = tag2ColumnMap.get(column);
		
		if(o == null){
			return null;
		}
		
		return o.toString();
	}
	
	/**
	 * Gets the seq by tag.
	 *
	 * @param tag the tag
	 * @return the seq by tag
	 */
	public Sequence getSeqByTag(int tag){
		Object o = tag2SeqMap.get(Tags.toString(tag));

		if (o == null) {
			return null;
		}
		
		return (Sequence)o;
	}
	
	/**
	 * Gets the tag2 seq map.
	 *
	 * @return Returns the tag2SeqMap.
	 */
	public HashMap getTag2SeqMap() {
		return tag2SeqMap;
	}
	
	/**
	 * Sets the tag2 seq map.
	 *
	 * @param tag2SeqMap The tag2SeqMap to set.
	 */
	public void setTag2SeqMap(HashMap tag2SeqMap) {
		this.tag2SeqMap = tag2SeqMap;
	}

	/**
	 * Gets the columns.
	 *
	 * @return the columns
	 */
	public List getColumns() {
		return columns;
	}
	
	/**
	 * Sets the columns.
	 *
	 * @param columns the new columns
	 */
	public void setColumns(List columns) {
		this.columns = columns;
	}
}


/******************************************************************************
 * Revision History 
 * [type 'revision' & press Alt + '/' to insert revision block]
 * 
 * 
 ******************************************************************************/
