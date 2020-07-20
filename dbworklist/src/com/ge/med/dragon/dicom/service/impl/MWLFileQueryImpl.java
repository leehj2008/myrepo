/**
 * MWLFileQueryImpl.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.service.impl;

import java.io.IOException;

import org.dcm4che.data.Dataset;

import com.ge.med.dragon.dicom.service.MWLQuery;

/**
 * The Class MWLFileQueryImpl.
 */
public class MWLFileQueryImpl extends MWLQuery{

	/** {@inheritDoc. Override} */
	public Dataset getDataset() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/** {@inheritDoc. Override} */
	public boolean next() throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	/** {@inheritDoc. Override} */
	public void setKeyDataset(Dataset ds) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setCalledAET(String calledAET) {
		this.calledAET=calledAET;
		
	}
	/**
	 * Execute.
	 *
	 * @param keyDS the key ds
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public boolean execute(Dataset keyDS) throws IOException{
		throw new IOException();
	}

	/** {@inheritDoc. Override} */
	public void execute() throws Exception {
		// TODO Auto-generated method stub
		
	}

	/** {@inheritDoc. Override} */
	public void setCallingAET(String callingAET) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Sets the keys.
	 *
	 * @param keys the new keys
	 */
	public void setKeys(Dataset keys) {
		// TODO Auto-generated method stub
		
	}
	
}
