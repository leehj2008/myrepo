/**
 * MPPS.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.service;

import org.dcm4che.data.Dataset;

/**
 * The Interface MPPS.
 *
 * @author Fan Yihui
 * @version $Revision: 1.1 $ $Date: 2001/09/08 05:32:52 $
 */

public interface MPPS {
	
	/**
	 * Creates the mpps.
	 *
	 * @param ds the ds
	 * @return true, if successful
	 */
	public boolean createMPPS(Dataset ds);
	
	/**
	 * Update mpps.
	 *
	 * @param ds the ds
	 * @return true, if successful
	 */
	public boolean updateMPPS(Dataset ds);
}
