/**
 * DragonScp.java 1.0 2013-3-4
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
package com.ge.med.dragon.dicom.scp;

import java.io.IOException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;

import com.ge.med.dragon.dicom.service.IAN;
import com.ge.med.dragon.dicom.service.MPPS;

/**
 * The Class DragonScp.
 *
 * @author fan yihui
 * 
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DragonScp extends DcmServiceBase {
	private MPPS mppsI;
	private IAN ian;

	/** The Constant IN_PROGRESS. */
	public static final String IN_PROGRESS = "IN PROGRESS";

	/** The Constant COMPLETED. */
	public static final String COMPLETED = "COMPLETED";

	/** The Constant DISCONTINUED. */
	public static final String DISCONTINUED = "DISCONTINUED";
	
	/** The Constant ONLINE. */
	public static final String ONLINE = "ONLINE";
	
	/** The Constant NEARLINE. */
	public static final String NEARLINE = "NEARLINE";
	
	/** The Constant OFFLINE. */
	public static final String OFFLINE = "OFFLINE";
	
	/** The Constant UNAVAILABLE. */
	public static final String UNAVAILABLE = "UNAVAILABLE";

	private static final int[] TYPE1_NCREATE_ATTR = {
			Tags.ScheduledStepAttributesSeq, Tags.PPSID,
			Tags.PerformedStationAET, Tags.PPSStartDate, Tags.PPSStartTime,
			Tags.PPSStatus, Tags.Modality };

	private static final int[] ONLY_NCREATE_ATTR = {
			Tags.ScheduledStepAttributesSeq, Tags.PatientName, Tags.PatientID,
			Tags.PatientBirthDate, Tags.PatientSex, Tags.PPSID,
			Tags.PerformedStationAET, Tags.PerformedStationName,
			Tags.PerformedLocation, Tags.PPSStartDate, Tags.PPSStartTime,
			Tags.Modality, Tags.StudyID };
	
	private static final int[] IAN_NCREATE_ATTR = {Tags.StudyID, Tags.RefSeriesSeq};
	
	private static final int[] IAN_REFSOPSEQ_ATTR = {Tags.RefSOPClassUID, Tags.RefSOPInstanceUID,
			Tags.InstanceAvailability, Tags.RetrieveAET};

	private static final int[] TYPE1_FINAL_ATTR = { Tags.PPSEndDate,
			Tags.PPSEndTime, Tags.PerformedSeriesSeq };

	protected Dataset doNCreate(ActiveAssociation assoc, Dimse rq,
			Command rspCmd) throws IOException, DcmServiceException {
		Dataset dataset = rq.getDataset(); // read out dataset

		final String cuid = rspCmd.getAffectedSOPClassUID();
		final String iuid = rspCmd.getAffectedSOPInstanceUID();
		
		boolean success = false;
		
		if (cuid.equals(UIDs.ModalityPerformedProcedureStep)) {
			checkCreateAttributs(dataset);
			dataset.putUI(Tags.SOPClassUID, cuid);
			dataset.putUI(Tags.SOPInstanceUID, iuid);

			if (mppsI != null) {
				success = mppsI.createMPPS(dataset);
			}
			
			if (!success) {
				throw new DcmServiceException(Status.ProcessingFailure,
						"Create Mpps failure!");
			}
		} else if(cuid.equals(UIDs.InstanceAvailabilityNotificationSOPClass)){
			checkIANAttributs(dataset);
			
			if(ian != null){
				success = ian.createIAN(dataset);
			}
			
			if (!success) {
				throw new DcmServiceException(Status.ProcessingFailure,
						"Create IAN failure!");
			}
		}

		return null;
	}

	protected Dataset doNSet(ActiveAssociation assoc, Dimse rq, Command rspCmd)
			throws IOException, DcmServiceException {
		final Command cmd = rq.getCommand();
		final Dataset mppsDS = rq.getDataset();
		final String iuid = cmd.getRequestedSOPInstanceUID();

		checkSetAttributs(mppsDS);
		mppsDS.putUI(Tags.SOPInstanceUID, iuid);

		boolean success = false;

		if (mppsI != null) {
			success = mppsI.updateMPPS(mppsDS);
		}

		if (!success) {
			throw new DcmServiceException(Status.ProcessingFailure,
					"Update Mpps failure!");
		}

		return null;
	}

	protected void doCEcho(ActiveAssociation arg0, Dimse rq, Command rspCmd)
			throws IOException, DcmServiceException {
		rspCmd.putUS(Tags.Status, SUCCESS);
	}

	private void checkIANAttributs(Dataset dataset) throws DcmServiceException {
		for (int i = 0; i < IAN_NCREATE_ATTR.length; ++i) {
			if (dataset.vm(IAN_NCREATE_ATTR[i]) <= 0)
				throw new DcmServiceException(Status.MissingAttributeValue,
						"Missing Type 1 Attribute "
								+ Tags.toString(IAN_NCREATE_ATTR[i]));
		}
		DcmElement refSeriesSeq = dataset.get(Tags.RefSeriesSeq);
		for (int i = 0, n = refSeriesSeq.vm(); i < n; ++i) {
			if (refSeriesSeq.getItem(i).vm(Tags.SeriesInstanceUID) <= 0)
				throw new DcmServiceException(Status.MissingAttributeValue,
						"Missing Series Instance UID in Referenced Series Sequence.");
			if (refSeriesSeq.getItem(i).vm(Tags.RefSOPSeq) <= 0)
				throw new DcmServiceException(Status.MissingAttributeValue,
						"Missing Referenced SOP Sequence in Referenced Series Sequence.");
		}
		
		Dataset refSopSeqDS = refSeriesSeq.getItem(Tags.RefSOPSeq);
		
		for (int i = 0; i < IAN_REFSOPSEQ_ATTR.length; i++) {
			if (refSopSeqDS.vm(IAN_REFSOPSEQ_ATTR[i]) <= 0)
				throw new DcmServiceException(Status.MissingAttributeValue,
						"Missing Type 1 Attribute "
								+ Tags.toString(IAN_REFSOPSEQ_ATTR[i]));

		}
		
		if (!ONLINE.equals(refSopSeqDS.getString(Tags.InstanceAvailability))
				&& !NEARLINE.equals(refSopSeqDS
						.getString(Tags.InstanceAvailability))
				&& !OFFLINE.equals(refSopSeqDS
						.getString(Tags.InstanceAvailability))
				&& !UNAVAILABLE.equals(refSopSeqDS
						.getString(Tags.InstanceAvailability)))
			throw new DcmServiceException(Status.InvalidAttributeValue);
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
		if (status == null || status.equals(IN_PROGRESS))
			return;
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
