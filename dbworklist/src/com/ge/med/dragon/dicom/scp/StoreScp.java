/**
 * StoreScp.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;

/**
 * The Class StoreScp.
 *
 * @author Fan Yihui
 * @version $Revision: 1.1 $ $Date: 2001/09/08 05:32:52 $
 */

public class StoreScp extends DcmServiceBase{
	private static Logger log = Logger.getLogger(StoreScp.class);

	private  int PROCESSING_FAILURE = 0x0101;
	//private  int CLASS_INSTANCE_CONFLICT = 0x0119;
	private  int MISSING_UID = 0xA900;
	private  int MISMATCH_UID = 0xA901;
	private  int CANNOT_UNDERSTAND = 0xC000;
	
	protected DcmParserFactory parserFact = DcmParserFactory.getInstance();
	private File archiveDir = new File("_archive");
	
	private OutputStream openOutputStream(File file) throws IOException
	{
		File parent = file.getParentFile();
		if (!parent.exists()){
			parent.mkdir();
		}
		
		return new BufferedOutputStream(new FileOutputStream(file));
	}
	
	private void  storeToFile(DcmParser parser, Dataset ds, File file, DcmEncodeParam encParam){
		OutputStream outs = null;
		try{
			outs = openOutputStream(file);
			ds.writeFile(outs, encParam);
			if (parser.getReadTag() == Tags.PixelData)
			{
				ds.writeHeader(outs, encParam, parser.getReadTag(), parser.getReadVR(), parser.getReadLength());
				copy(parser.getInputStream(), outs);
			}
		}catch (IOException ie){
			ie.printStackTrace();
		}
		finally{
			try
			{
				if(outs!=null)
					outs.close();
			}
			catch (IOException ignore)
			{
			}
		}
	}
	
	private void copy(InputStream ins, OutputStream outs) throws IOException{
		int c;
		byte[] buffer = new byte[512];
		while ((c = ins.read( buffer, 0, buffer.length)) != - 1)
		{
			outs.write(buffer, 0, c);
		}
	}
	
	private File toFile(Dataset ds) throws Exception
	{
		String studyInstUID = null;
		try
		{
			studyInstUID = ds.getString(Tags.StudyInstanceUID);
			if (studyInstUID == null)
			{
				throw new DcmServiceException(MISSING_UID, "Missing Study Instance UID");
			}
			if (ds.vm(Tags.SeriesInstanceUID) <= 0)
			{
				throw new DcmServiceException(MISSING_UID, "Missing Series Instance UID");
			}
			String instUID = ds.getString(Tags.SOPInstanceUID);
			if (instUID == null)
			{
				throw new DcmServiceException(MISSING_UID, "Missing SOP Instance UID");
			}
			String classUID = ds.getString(Tags.SOPClassUID);
			if (classUID == null)
			{
				throw new DcmServiceException(MISSING_UID, "Missing SOP Class UID");
			}
			if (!instUID.equals(ds.getFileMetaInfo().getMediaStorageSOPInstanceUID()))
			{
				throw new DcmServiceException(MISMATCH_UID, "SOP Instance UID in Dataset differs from Affected SOP Instance UID");
			}
			if (!classUID.equals(ds.getFileMetaInfo().getMediaStorageSOPClassUID()))
			{
				throw new DcmServiceException(MISMATCH_UID, "SOP Class UID in Dataset differs from Affected SOP Class UID");
			}
		}
		catch (Exception e)
		{
			throw new DcmServiceException(CANNOT_UNDERSTAND, e);
		}
		
		String pid = toFileID(ds, Tags.PatientID);
		File dir = archiveDir;
		
		dir = new File(dir.getAbsolutePath() + "\\" + pid);
		if(!dir.exists()){
			dir.mkdir();
		}
		
		dir = new File(dir.getAbsolutePath() + "\\" + studyInstUID);
		
		if(!dir.exists()){
			dir.mkdir();
		}
		
		dir = new File(dir.getAbsolutePath() + "\\" + toFileID(ds, Tags.SeriesNumber));
		
		if(!dir.exists()){
			dir.mkdir();
		}
		
		File file = new File(dir.getAbsolutePath() + "\\" + toFileID(ds, Tags.InstanceNumber) + ".dcm");						
		return file;
	}
	
	private String toFileID(Dataset ds, int tag)
	{
		try
		{
			String s = ds.getString(tag);
			if (s == null || s.length() == 0)
				return "__NULL__";
			char[] ins = s.toUpperCase().toCharArray();
			char[] outs = new char[Math.min(8, ins.length)];
			for (int i = 0; i < outs.length; ++i)
			{
				outs[i] = ins[i] >= '0' && ins[i] <= '9' || ins[i] >= 'A' && ins[i] <= 'Z'?ins[i]:'_';
			}
			return new String(outs);
		}
		catch (Exception e)
		{
			return "__ERR__";
		}
	}
	
	protected void doCStore(ActiveAssociation assoc, Dimse rq, Command rspCmd) throws IOException, DcmServiceException {
		Command rqCmd = rq.getCommand();
		InputStream ins = rq.getDataAsStream();
		try
		{
			String instUID = rqCmd.getAffectedSOPInstanceUID();
			String classUID = rqCmd.getAffectedSOPClassUID();
			DcmDecodeParam decParam = DcmDecodeParam.valueOf(rq.getTransferSyntaxUID());
			Dataset ds = objFact.newDataset();
			DcmParser parser = parserFact.newDcmParser(ins);
			parser.setDcmHandler(ds.getDcmHandler());
			parser.parseDataset(decParam, Tags.PixelData);
			ds.setFileMetaInfo( objFact.newFileMetaInfo(classUID, instUID, rq.getTransferSyntaxUID()) );
			File file = toFile(ds);
			storeToFile(parser, ds, file, (DcmEncodeParam) decParam);
			rspCmd.putUS(Tags.Status, SUCCESS);
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
			try{
				throw new DcmServiceException(PROCESSING_FAILURE, e);
			} catch(Exception ex){
			}
		}
		finally
		{
			try{
				ins.close();
			} catch(Exception ex){
			}
		}
	}
	
	/*protected void doCStore(ActiveAssociation assoc, Dimse rq, Command rspCmd) throws IOException, DcmServiceException 
	{
		
	}*/
}
