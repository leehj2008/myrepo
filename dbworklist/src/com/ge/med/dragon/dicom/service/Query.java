/**
 * Query.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.service;

import org.dcm4che.data.Dataset;
import org.dcm4cheri.data.DcmObjectFactoryImpl;

/**
 * The Interface Query.
 *
 * @author Fan Yihui
 * @version $Revision: 1.1 $ $Date: 2001/09/08 05:32:52 $
 */

public interface Query {
	
	/** The dof. */
	public static DcmObjectFactoryImpl dof = new DcmObjectFactoryImpl();
	
	/**
	 * Next.
	 *
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean next() throws Exception;
	
	/**
	 * Gets the dataset.
	 *
	 * @return the dataset
	 * @throws Exception the exception
	 */
	public Dataset getDataset() throws Exception;
	
	/**
	 * Sets the key dataset.
	 *
	 * @param ds the new key dataset
	 */
	public void setKeyDataset(Dataset ds);
}
