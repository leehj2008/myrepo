/**
 * IAN.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * Created on 2005-12-20
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.ge.med.dragon.dicom.service;

import org.dcm4che.data.Dataset;

/**
 * The Interface IAN.
 *
 * @author lennon
 * 
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IAN {
	
	/**
	 * Creates the ian.
	 *
	 * @param dataset the dataset
	 * @return true, if successful
	 */
	public boolean createIAN(Dataset dataset);
}
