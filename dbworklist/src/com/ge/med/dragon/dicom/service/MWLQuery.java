/**
 * MWLQuery.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.service;

import org.dcm4che.data.Dataset;

/**
 * The Class MWLQuery.
 */
public abstract class MWLQuery implements Query{
	protected Dataset keys = null;
	protected String callingAET = null;
	protected String calledAET = null;
	
	/**
	 * Sets the calling aet.
	 *
	 * @param callingAET the new calling aet
	 */
	public abstract void setCallingAET(String callingAET);
	/**
	 * Sets the calling aet.
	 *
	 * @param callingAET the new calling aet
	 */
	public abstract void setCalledAET(String calledAET);
	
	/**
	 * Execute.
	 *
	 * @throws Exception the exception
	 */
	public abstract void execute() throws Exception;
}
