/**
 * TimeGenerate.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scp.config;


/**
 * The Class TimeGenerate.
 */
public class TimeGenerate implements GenerateValue {
	//private static SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:dd");
	
	/** {@inheritDoc. Override} */
	public String generate(String root) throws Exception{
		if(root == null || root.trim().equals("")){
			return "";
		}
	    	String time = removeDuplicateChars(root, "-"); 
	    
	    	if(time == null || time.equals("")){
	    		return "";
	    	}
	    	
	    	if(time.length() == 5){
	    	    time = time + ":00";
	    	}
	    	
	    	time = time.replaceAll(":", "") + ".0000";
	    	
		return time;
	}
	
	private String removeDuplicateChars(String original, String removeChar){
		if(original == null){
			return "";
		}
		
		if(original.equals("")){
			return original;
		}
		
		if(original.charAt(0) == ' '){
			return "";
		}
		
		if(original.indexOf(removeChar)==-1){
			if(original.length() == 4){
				original = "0"+original;
			}
			return original;
		}
		
		String value = original.substring(0, original.indexOf(removeChar));
		if(value.length() == 4){
			value = "0"+value;
		}
		return value;
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public  static void main(String[] args){
		try{
			System.out.println(new TimeGenerate().generate("12:30--13:00"));
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
