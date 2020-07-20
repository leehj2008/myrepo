/**
 * MWLReliabilityTestImpl.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.service.impl;

import java.util.ArrayList;
import java.util.Iterator;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRs;

import com.ge.med.dragon.dicom.service.MWLQuery;
import com.ge.med.dragon.dicom.util.DicomUtil;

/**
 * The Class MWLReliabilityTestImpl.
 */
public class MWLReliabilityTestImpl extends MWLQuery{

	ArrayList resultDsList = new ArrayList();
	
	Iterator resultDs = null;
	
	private Dataset currentDs = null;
	
	/**
	 * Instantiates a new mWL reliability test impl.
	 */
	public MWLReliabilityTestImpl(){
	}
	
	/** {@inheritDoc. Override} */
	public void setCallingAET(String callingAET) {
		
	}
	@Override
	public void setCalledAET(String calledAET) {
		this.calledAET=calledAET;
		
	}
	/** {@inheritDoc. Override} */
	public void execute() throws Exception{
		if(keys == null){
			throw new Exception("The key dataset is null.");
		}
		
		try{
			execute(keys);
		}catch(Exception se){
			throw se;
		}
		
		resultDs = resultDsList.iterator();
		currentDs = (Dataset)resultDs.next();
	}
	
	/** {@inheritDoc. Override} */
	public boolean next() throws Exception {
		if(resultDs == null){
			return false;
		}
		
		if(resultDs.hasNext()){
			currentDs = (Dataset)resultDs.next();
			return true;
		}
		
		return false;
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/** {@inheritDoc. Override} */
	public Dataset getDataset() throws Exception {
		Dataset dcmds = dof.newDataset();
		
		//String encode = "ISO 2022 IR 87";
		
		//DicomUtil.adjustDataset(dcmds, keys, encode);
		DicomUtil.adjustDataset(dcmds, keys);
		
		for (Iterator it = keys.iterator(); it.hasNext();) {
			DcmElement dcmelement = (DcmElement) it.next();
			int tag = dcmelement.tag();
			int vr = dcmelement.vr();

			if(tag == Tags.SPSSeq){
				Dataset spsItem = dcmds.getItem(Tags.SPSSeq);
				
				Dataset currentSps = currentDs.getItem(Tags.SPSSeq);
				
				for(Iterator itj = keys.getItem(Tags.SPSSeq).iterator(); itj.hasNext();){
					DcmElement spsElement = (DcmElement) itj.next();
					int stag = spsElement.tag();
					int svr = spsElement.vr();
					if (svr != VRs.SQ) {
						String value = currentSps.getString(stag);
						
						if (stag == Tags.SPSStartTime) {
							
							DicomUtil.putElement(spsItem, svr, stag, value);
						}

						if (stag == Tags.SPSDescription) {
							
							DicomUtil.putElement(spsItem, svr, stag, value);
						}

						if (stag == Tags.SPSStartDate) {
							
							DicomUtil.putElement(spsItem, svr, stag, value);
						}

						if (stag == Tags.Modality) {
							
							DicomUtil.putElement(spsItem, svr, stag, value);
						}
						
						

						//DicomUtil.putElement(spsItem, svr, stag, value);
					}
				}
			} else  if(vr!=VRs.SQ){
				try {
					String value = currentDs.getString(tag);
					
					
					if(tag == Tags.PatientID){
						DicomUtil.putElement(dcmds, vr, tag, value);
					}
					
					if(tag == Tags.PatientName){
						
						DicomUtil.putElement(dcmds, vr, tag, value);
					}
					
					if(tag == Tags.PatientBirthDate){
						
						DicomUtil.putElement(dcmds, vr, tag, value);
					}
					
					if(tag == Tags.AccessionNumber){
						
						DicomUtil.putElement(dcmds, vr, tag, value);
					}
					
					if(tag == Tags.StudyInstanceUID){
						
						DicomUtil.putElement(dcmds, vr, tag, value);
					}
					
					if(tag == Tags.PatientSex){
						
						DicomUtil.putElement(dcmds, vr, tag, value);
					}
					
					if(tag == Tags.PatientWeight){
						
						DicomUtil.putElement(dcmds, vr, tag, value);
					}
					
					//DicomUtil.putElement(dcmds, vr, tag, value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println(dcmds.getString(Tags.PatientName));
		
		return dcmds.subSet(keys);
	}

	/** {@inheritDoc. Override} */
	public void setKeyDataset(Dataset ds) {
		this.keys = ds;
	}

	private boolean execute(Dataset keyDS) throws Exception {
		System.out.println("The scu encode is "+ keyDS.getString(Tags.SpecificCharacterSet));
		
		Dataset spsItem = keys.getItem(Tags.SPSSeq);
		//SQElement sqElement = (SQElement)keys.get(Tags.SPSSeq);
		//sqElement.addItem(item);
		
		int patientID = 100000;
		String patientName = "";
		String birthday = "19510429";
		String sex = "M";
		
		int weight = 50;
		
		int accNo = 1000000;
		
		String UID = "1.2.840.1.999999.111.3";
		
		for(int i=0; i<100; i++){
			Dataset ds = dof.newDataset();
			
			//DicomUtil.adjustDataset(ds, keys, "ISO 2022 IR 13"+"\\"+"ISO 2022 IR 87");
			DicomUtil.adjustDataset(ds, keys);
			
			for (Iterator it = keys.iterator(); it.hasNext();) {
				DcmElement dcmelement = (DcmElement) it.next();
				int tag = dcmelement.tag();
				int vr = dcmelement.vr();

				if(tag == Tags.SPSSeq){
					Dataset spsDs = ds.getItem(Tags.SPSSeq);
					
					for(Iterator itj = spsItem.iterator(); itj.hasNext();){
						DcmElement spsElement = (DcmElement) itj.next();
						int stag = spsElement.tag();
						//System.out.println(Tags.toString(stag));
						int svr = spsElement.vr();
						if (svr != VRs.SQ) {
							String value = spsItem.getString(stag);

							if (stag == Tags.SPSStartTime) {
								value = "10:00:00-10:30:00";
								DicomUtil.putElement(spsDs, svr, stag, value);
							}

							if (stag == Tags.SPSDescription) {
								value = "This is a test.";
								DicomUtil.putElement(spsDs, svr, stag, value);
							}

							if (stag == Tags.SPSStartDate) {
								value = "20060410";
								DicomUtil.putElement(spsDs, svr, stag, value);
							}

							if (stag == Tags.Modality) {
								value = "CT";
								DicomUtil.putElement(spsDs, svr, stag, value);
							}
						}
						
					}
				} else if(vr!=VRs.SQ){
					String value = keys.getString(tag);
					//System.out.println(Tags.toString(tag));
					
					if(tag == Tags.PatientID){
						value = String.valueOf(patientID + i);
						DicomUtil.putElement(ds, vr, tag, value);
					}
					
					if(tag == Tags.PatientName){
						value = patientName +" "+ i;
						DicomUtil.putElement(ds, vr, tag, value);
					}
					
					if(tag == Tags.PatientBirthDate){
						value = birthday;
						DicomUtil.putElement(ds, vr, tag, value);
					}
					
					if(tag == Tags.AccessionNumber){
						value = String.valueOf(accNo + i);
						DicomUtil.putElement(ds, vr, tag, value);
					}
					
					if(tag == Tags.StudyInstanceUID){
						value = UID +"."+i;
						DicomUtil.putElement(ds, vr, tag, value);
					}
					
					if(tag == Tags.PatientSex){
						if(sex.equals("M")){
							sex = "F";
						} else {
							sex = "M";
						}
						
						value = sex.toString();
						DicomUtil.putElement(ds, vr, tag, value);
					}
					
					if(tag == Tags.PatientWeight){
						value = String.valueOf(weight + i);
						DicomUtil.putElement(ds, vr, tag, value);
					}
				}
			}
			
			resultDsList.add(ds);
		}
		
		return true;
	}
	
}
