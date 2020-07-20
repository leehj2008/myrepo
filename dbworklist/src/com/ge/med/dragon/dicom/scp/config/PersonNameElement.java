/**
 * PersonNameElement.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scp.config;

import java.util.ArrayList;

/**
 * The Class PersonNameElement.
 */
public class PersonNameElement {
	
	/** The caret. */
	public static String CARET = new String(new byte[]{0x5E});;
	
	/** The space. */
	public static String SPACE = new String(new byte[]{0x20});;
	
	/** The multispace. */
	public static String MULTISPACE = new String(new char[]{(char) Integer.parseInt("3000", 16)});
	
	private ArrayList<MappingElement> componentList = new ArrayList<MappingElement>();
	private String delimiter = " ";
	
	/**
	 * Gets the delimiter.
	 *
	 * @return the delimiter
	 */
	public String getDelimiter() {
		return delimiter;
	}
	
	/**
	 * Sets the delimiter.
	 *
	 * @param delimiter the new delimiter
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
	/**
	 * Adds the component.
	 *
	 * @param me the me
	 */
	public void addComponent(MappingElement me){
		componentList.add(me);
	}
	
	/**
	 * Gets the component list.
	 *
	 * @return the component list
	 */
	public ArrayList<MappingElement> getComponentList(){
		return componentList;
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){
		System.out.println(PersonNameElement.CARET);
		System.out.println(PersonNameElement.SPACE);
		System.out.println(PersonNameElement.MULTISPACE);
	}
}
