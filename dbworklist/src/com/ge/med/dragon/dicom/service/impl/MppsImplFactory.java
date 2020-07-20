/**
 * MppsImplFactory.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.service.impl;

import com.ge.med.dragon.dicom.service.MPPS;

/**
 * A factory for creating MppsImpl objects.
 *
 * @author Fan Yihui
 * @version $Revision: 1.1 $ $Date: 2001/09/08 05:32:52 $
 */

public class MppsImplFactory {
	
	/**
	 * Gets the mpps impl.
	 *
	 * @param className the class name
	 * @return the mpps impl
	 * @throws ClassNotFoundException the class not found exception
	 * @throws InstantiationException the instantiation exception
	 * @throws IllegalAccessException the illegal access exception
	 */
	public static MPPS getMppsImpl(String className) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Class mpps = Class.forName(className);
		return (MPPS) mpps.newInstance();
	}
}
