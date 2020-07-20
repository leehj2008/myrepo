/**
 * Katakana2RomanJ.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scp.config;

import com.ge.med.dragon.dicom.util.StringUtil;
import com.ge.med.dragon.ris.utils.Kana2Roman;

/**
 * The Class Katakana2RomanJ.
 */
public class Katakana2RomanJ  implements GenerateValue{
	static{
		Kana2Roman.initialize();
	}
	
	/** {@inheritDoc. Override} */
	public String generate(String root) throws Exception {
		root = StringUtil.replaceDelimiter(root, "^");
		
		String[] values = StringUtil.toStringArray(root, "^");
		
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<values.length; i++){
			sb.append(Kana2Roman.kana2Roman(values[i]));
			
			if(i != values.length - 1){
				sb.append("^");
			}
		}
		
		return sb.toString().toUpperCase(); 
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String args[]){
	    try{
	        System.out.println(new Katakana2RomanJ().generate("サトウ"+PersonNameElement.MULTISPACE+"シン"));
	        
	    } catch(Exception ex){
	        
	    }
	}
}
