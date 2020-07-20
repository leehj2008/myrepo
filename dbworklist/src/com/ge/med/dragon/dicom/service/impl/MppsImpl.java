/**
 * MppsImpl.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.service.impl;

import com.ge.med.dragon.dicom.service.MPPS;
import com.ge.med.dragon.dicom.util.ConfParser;
import com.ge.med.dragon.dicom.util.JdbcConfiguration;
import com.ge.med.util.DatabaseUtil;
import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * The Class MppsImpl.
 */
public class MppsImpl extends JFrame implements MPPS{
	private static Logger log = Logger.getLogger(MppsImpl.class);
	
	Vector<List<String>> tableData = new Vector<List<String>>();
	
	
	/**
	 * Instantiates a new mpps impl.
	 */
	public MppsImpl() {
		initialUI();
		this.setSize(800,600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	private void initialUI(){
		JScrollPane scrollPane = new JScrollPane();

		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerSize(5);
		splitPane.add(scrollPane);
		
		this.setContentPane(splitPane);
	}
	
	public boolean createMPPS(Dataset ds){
		Connection con = null;
		Statement stmt = null;
		try {
			List<String> v = new ArrayList<String>();
			for (int i = 0; i < ConfParser.mppsElements.size(); i++) {
				int tag = Tags.valueOf(ConfParser.mppsElements.get(i).toString());
				String value = ds.getString(tag);
				v.add(value);
			}

			//to get accession number
			String accessionNo = null;
			Dataset spsAttributeSQDs = ds.getItem(Tags.ScheduledStepAttributesSeq);
			for (int i = 0; i < ConfParser.mppsSequenceElements.size(); i++) {
				int tag = Tags.valueOf(ConfParser.mppsSequenceElements.get(i).toString());
				String value = spsAttributeSQDs.getString(tag);
				
				if(tag == Tags.AccessionNumber){
					accessionNo = value;
				}
				v.add(value);
			}

			if(accessionNo != null){
				String url = JdbcConfiguration.getInstance().getUrl();
				String driver = JdbcConfiguration.getInstance().getDriver();
				String username = JdbcConfiguration.getInstance().getUsername();
				String password = JdbcConfiguration.getInstance().getPassword();
				
				Class.forName(driver);

				con = DriverManager.getConnection(url, username, password);
				stmt = con.createStatement();
				stmt.executeUpdate("update ExamInfo set ExamSatus='CANCELLED' where ExamAccessionID='"+accessionNo+"'");
			}

			tableData.add(v);

			
			return true;
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return false;
		} finally{
            DatabaseUtil.safeCloseStatement(stmt);
            DatabaseUtil.safeCloseConnection(con);
		}
	}

	public boolean updateMPPS(Dataset ds) {
		try {
			String sop = ds.getString(Tags.SOPInstanceUID);

			int selectRow = -1;

			if (selectRow == -1) {
				return false;
			}

            List<String> v = tableData.get(selectRow);
			String value = ds.getString(Tags.PPSStatus);

			return true;
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return false;
		}
	}
}


/******************************************************************************
 * Revision History 
 * [type 'revision' & press Alt + '/' to insert revision block]
 * 
 * 
 * 
 *     Copyright 2004 GE Healthcare Systems
 *     All rights reserved. 
 ******************************************************************************/
