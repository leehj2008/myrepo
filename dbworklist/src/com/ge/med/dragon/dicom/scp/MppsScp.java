/**
 * MppsScp.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scp;

import java.io.IOException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;

import com.ge.med.dragon.dicom.service.MPPS;

/**
 * The Class MppsScp.
 *
 * @author Fan Yihui
 * @version $Revision: 1.1 $ $Date: 2001/09/08 05:32:52 $
 */

public class MppsScp extends DcmServiceBase {
	private MPPS mppsI;
	
	/** The Constant IN_PROGRESS. */
	public static final String IN_PROGRESS = "IN PROGRESS";

	/** The Constant COMPLETED. */
	public static final String COMPLETED = "COMPLETED";

	/** The Constant DISCONTINUED. */
	public static final String DISCONTINUED = "DISCONTINUED";

    private static final int[] TYPE1_NCREATE_ATTR = {
            Tags.ScheduledStepAttributesSeq, Tags.PPSID,
            Tags.PerformedStationAET, Tags.PPSStartDate, Tags.PPSStartTime,
            Tags.PPSStatus, Tags.Modality};

    private static final int[] ONLY_NCREATE_ATTR = {
            Tags.ScheduledStepAttributesSeq, Tags.PatientName, Tags.PatientID,
            Tags.PatientBirthDate, Tags.PatientSex, Tags.PPSID,
            Tags.PerformedStationAET, Tags.PerformedStationName,
            Tags.PerformedLocation, Tags.PPSStartDate, Tags.PPSStartTime,
            Tags.Modality, Tags.StudyID};

    private static final int[] TYPE1_FINAL_ATTR = { Tags.PPSEndDate,
            Tags.PPSEndTime, Tags.PerformedSeriesSeq};
	
	protected Dataset doNCreate(ActiveAssociation assoc, Dimse rq,
			Command rspCmd) throws IOException, DcmServiceException {
		Dataset mppsDS = rq.getDataset(); // read out dataset
		
		final String cuid = rspCmd.getAffectedSOPClassUID();
		final String iuid = rspCmd.getAffectedSOPInstanceUID();

		checkCreateAttributs(mppsDS);
		mppsDS.putUI(Tags.SOPClassUID, cuid);
		mppsDS.putUI(Tags.SOPInstanceUID, iuid);
		
		boolean success = false;
		
		if(mppsI!=null){
			success = mppsI.createMPPS(mppsDS);
		}
		
		if(!success){
			throw  new DcmServiceException(Status.ProcessingFailure,
                    "Create Mpps failure!");
		}
		
		return null;
	}
	
	protected Dataset doNSet(ActiveAssociation assoc, Dimse rq, Command rspCmd) throws IOException, DcmServiceException {
		final Command cmd = rq.getCommand();
		final Dataset mppsDS = rq.getDataset();
		final String iuid = cmd.getRequestedSOPInstanceUID();
		
		checkSetAttributs(mppsDS);
		mppsDS.putUI(Tags.SOPInstanceUID, iuid);
		
		boolean success = false;
		
		if(mppsI!=null){
			success = mppsI.updateMPPS(mppsDS);
		}
		
		if(!success){
			throw  new DcmServiceException(Status.ProcessingFailure,
                    "Update Mpps failure!");
		}
		
		return null;
	}
	
	protected void doCEcho(ActiveAssociation arg0, Dimse rq, Command rspCmd)
			throws IOException, DcmServiceException {
		rspCmd.putUS(Tags.Status, SUCCESS);
	}
	
	private void checkCreateAttributs(Dataset mpps) throws DcmServiceException {
        for (int i = 0; i < TYPE1_NCREATE_ATTR.length; ++i) {
            if (mpps.vm(TYPE1_NCREATE_ATTR[i]) <= 0)
                    throw new DcmServiceException(Status.MissingAttributeValue,
                            "Missing Type 1 Attribute "
                                    + Tags.toString(TYPE1_NCREATE_ATTR[i]));
        }
        DcmElement ssaSq = mpps.get(Tags.ScheduledStepAttributesSeq);
        for (int i = 0, n = ssaSq.vm(); i < n; ++i) {
            if (ssaSq.getItem(i).vm(Tags.StudyInstanceUID) <= 0)
                    throw new DcmServiceException(Status.MissingAttributeValue,
                            "Missing Study Instance UID in Scheduled Step Attributes Seq.");
        }
        if (!IN_PROGRESS.equals(mpps.getString(Tags.PPSStatus)))
                throw new DcmServiceException(Status.InvalidAttributeValue);
    }

    private void checkSetAttributs(Dataset mpps) throws DcmServiceException {
        for (int i = 0; i < ONLY_NCREATE_ATTR.length; ++i) {
            if (mpps.vm(ONLY_NCREATE_ATTR[i]) >= 0)
                    throw new DcmServiceException(Status.ProcessingFailure,
                            "Cannot update attribute "
                                    + Tags.toString(ONLY_NCREATE_ATTR[i]));
        }
        final String status = mpps.getString(Tags.PPSStatus);
        if (status == null || status.equals(IN_PROGRESS)) return;
        if (!status.equals(COMPLETED) && !status.equals(DISCONTINUED))
                throw new DcmServiceException(Status.InvalidAttributeValue,
                        "Invalid MPPS Status: " + status);
        for (int i = 0; i < TYPE1_FINAL_ATTR.length; ++i) {
            if (mpps.vm(TYPE1_FINAL_ATTR[i]) <= 0)
                    throw new DcmServiceException(Status.MissingAttributeValue,
                            "Missing Type 1 Attribute "
                                    + Tags.toString(TYPE1_FINAL_ATTR[i]));
        }
    }

	
    /**
     * Gets the mpps i.
     *
     * @return the mpps i
     */
    public MPPS getMppsI() {
		return mppsI;
	}

	/**
	 * Sets the mpps i.
	 *
	 * @param mppsI the new mpps i
	 */
	public void setMppsI(MPPS mppsI) {
		this.mppsI = mppsI;
	}
	
}
