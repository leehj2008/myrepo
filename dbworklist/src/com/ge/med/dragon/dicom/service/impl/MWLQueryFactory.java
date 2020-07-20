/**
 * MWLQueryFactory.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.service.impl;

import java.sql.SQLException;

import org.dcm4che.data.Dataset;

import com.ge.med.dragon.dicom.scp.config.ConfigurationException;
import com.ge.med.dragon.dicom.service.MWLQuery;

/**
 * A factory for creating MWLQuery objects.
 */
public class MWLQueryFactory {
	
	/**
	 * Gets the single instance of MWLQueryFactory.
	 *
	 * @param type the type
	 * @return single instance of MWLQueryFactory
	 * @throws ConfigurationException the configuration exception
	 * @throws SQLException the sQL exception
	 */
	public static MWLQuery getInstance(String type) throws ConfigurationException, SQLException{
		if(type ==null){
			return null;
		}
		
		if(type.equals("SQL")){
			return new MWLSQLQueryImpl();
		} else if(type.equals("FILE")){
			return new MWLFileQueryImpl();
		} else if(type.equals("TEST")){
			return new MWLReliabilityTestImpl();
		} else if(type.equals("CE")){
			return new MWLCEQueryImpl();
		} else{
			return null;
		}
	}
}
