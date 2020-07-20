/**
 * MppsScu.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scu;


import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;

/**
 * The Class MppsScu.
 *
 * @author Fan Yihui
 * @version $Revision: 1.1 $ $Date: 2001/09/08 05:32:52 $
 */

public class MppsScu {

	private static final String[] STATUS = { "IN PROGRESS", "COMPLETED", "DISCONTINUED" };
	
	private static Logger log = Logger.getLogger(MppsScu.class);
	
	private static final String[] NATIVE_TS = { UIDs.ExplicitVRLittleEndian,
        UIDs.ImplicitVRLittleEndian};
	
    private int acTimeout;

    private int dimseTimeout;

    private int soCloseDelay;
	
    private String callingAET = "AE_FAN";
    
    private String host = "FAEN";
    private int port = 104;
    
    private static final int ERR_IO = -3;

    private static final int ERR_ASSOC_NOT_ACCEPTED = -4;

    private static final int ERR_SERVICE_NOT_SUPPORTED = -5;

    private static final int ERR_THREAD = -6;

    private static final int PCID_MPPS = 1;
    
    private static final int[] EXCLUDE_TAGS = { Tags.SOPClassUID, Tags.SOPInstanceUID};
    
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub		
		Dataset createDataset = DcmObjectFactory.getInstance().newDataset();
        
		//PPS Relationship
        DcmElement ssasElement = createDataset.putSQ(Tags.ScheduledStepAttributesSeq);
        Dataset ssasDS = ssasElement.addNewItem();
        
        ssasDS.putXX(Tags.StudyInstanceUID, "1.2.3.000.222.5656");
        ssasDS.putXX(Tags.AccessionNumber, "AccessionID");
        ssasDS.putXX(Tags.RequestedProcedureID, "rpid");
        ssasDS.putXX(Tags.SPSID, "spsid");
        ssasDS.putXX(Tags.PatientName, "Patient Name");
        ssasDS.putXX(Tags.PatientID, "Patient ID");
        ssasDS.putXX(Tags.PatientBirthDate, "19890506");
        ssasDS.putXX(Tags.PatientSex, "M");
        
        //PPS information
        //DcmElement ppsiElemet = createDataset.put
        createDataset.putXX(Tags.PPSID, "ppsid");
        createDataset.putXX(Tags.PerformedStationAET, "AE_FAEN_MPPS");
        createDataset.putXX(Tags.PPSStartDate, "20050812");
        createDataset.putXX(Tags.PPSStartTime, "12:03:04");
        createDataset.putXX(Tags.PPSStatus, "IN PROGRESS");
        
        //Modality
        createDataset.putXX(Tags.Modality, "CT");
        
		//new MppsScu().sendMPPS(true, createDataset, "AE_FAEN");
	}

	/**
	 * Send mpps.
	 *
	 * @param create the create
	 * @param mpps the mpps
	 * @param destination the destination
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ConnectException the connect exception
	 */
	public int sendMPPS(boolean create, Dataset mpps, String destination) throws IOException, ConnectException{
		ActiveAssociation aa = null;

		AssociationFactory af = AssociationFactory.getInstance();
		Association a = af.newRequestor(new Socket(host, port));
		a.setAcTimeout(acTimeout);
		a.setDimseTimeout(dimseTimeout);
		a.setSoCloseDelay(soCloseDelay);

		AAssociateRQ rq = af.newAAssociateRQ();
		rq.setCalledAET(destination);
		rq.setCallingAET(callingAET);
		rq.addPresContext(af.newPresContext(PCID_MPPS,
				UIDs.ModalityPerformedProcedureStep, NATIVE_TS));

		PDU ac = a.connect(rq);
		if (!(ac instanceof AAssociateAC)) {
			log.error("Association not accepted by " + destination + ": " + ac);
			return ERR_ASSOC_NOT_ACCEPTED;
		}
		aa = af.newActiveAssociation(a, null);
		aa.start();
		if (a.getAcceptedTransferSyntaxUID(PCID_MPPS) == null) {
			log
					.error("MPPS Service not supported by remote AE: "
							+ destination);
			return ERR_SERVICE_NOT_SUPPORTED;
		}
		DcmObjectFactory dof = DcmObjectFactory.getInstance();
		Command cmdRq = dof.newCommand();
		if (create) {
			cmdRq.initNCreateRQ(a.nextMsgID(),
					UIDs.ModalityPerformedProcedureStep, mpps
							.getString(Tags.SOPInstanceUID));
		} else {
			cmdRq.initNSetRQ(a.nextMsgID(),
					UIDs.ModalityPerformedProcedureStep, mpps
							.getString(Tags.SOPInstanceUID));
		}
		Dimse dimseRq = af.newDimse(PCID_MPPS, cmdRq, mpps
				.exclude(EXCLUDE_TAGS));
		final Dimse dimseRsp;
		try {
			dimseRsp = aa.invoke(dimseRq).get();
		} catch (InterruptedException ie) {
			log
					.error("Threading error during waiting for response of "
							+ cmdRq);
			return ERR_THREAD;
		}
		final Command cmdRsp = dimseRsp.getCommand();
		
		if (aa != null){
			try {
				aa.release(true);
			} catch (Exception e) {
				log.warn("Failed to release " + aa.getAssociation());
			}
		}
		
		final int status = cmdRsp.getStatus();
		switch (status) {
		case 0x0000:
			return 0;
		case 0x0116:
			log
					.warn("Received Warning Status 116H (=Attribute Value Out of Range) from remote AE "
							+ destination);
			return 0;
		default:
			return status;
		}
	}

	/**
	 * Gets the host.
	 *
	 * @return Returns the host.
	 */
	public String getHost() {
		return host;
	}
	
	/**
	 * Sets the host.
	 *
	 * @param host The host to set.
	 */
	public void setHost(String host) {
		this.host = host;
	}
	
	/**
	 * Gets the port.
	 *
	 * @return Returns the port.
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Sets the port.
	 *
	 * @param port The port to set.
	 */
	public void setPort(int port) {
		this.port = port;
	}
}

