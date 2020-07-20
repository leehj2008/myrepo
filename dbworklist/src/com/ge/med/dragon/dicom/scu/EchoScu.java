/**
 * EchoScu.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
/*
 * Created on 2005-10-18
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.ge.med.dragon.dicom.scu;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.dcm4che.data.Command;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.PDU;

/**
 * The Class EchoScu.
 *
 * @author lennon
 * 
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EchoScu {
	private static Logger log = Logger.getLogger(MWLFindScu.class);
	
	private String calledAET = "GEWLSERVER";
	private String callingAET = "AE_FAN";
	private int maxPDUlen = 16352;
	private String[] tsUIDs = {UIDs.ImplicitVRLittleEndian, UIDs.ExplicitVRLittleEndian, UIDs.ExplicitVRBigEndian};
	private static String asUID = UIDs.Verification;
	
	private String remoteHostName = "3.36.230.59";
	private int remotePort = 5900;
	
	/** Holds Association timeout in ms. */
	private int acTimeout;
	
	/** Holds DICOM message timeout in ms. */
	private int dimseTimeout;
	
	/** Holds socket close delay in ms. */
	private int soCloseDelay;
	
	/** DICOM priority. Used for C-FIND. */
	private int priority = 0;
	
	/**
	 * Echo.
	 *
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ConnectException the connect exception
	 */
	public int echo() throws IOException, ConnectException{
		ActiveAssociation aa;

		AssociationFactory af = AssociationFactory.getInstance();
		Association a = af.newRequestor(new Socket(remoteHostName, remotePort));
		a.setAcTimeout(acTimeout);
		a.setDimseTimeout(dimseTimeout);
		a.setSoCloseDelay(soCloseDelay);

		AAssociateRQ rq = af.newAAssociateRQ();
		rq.setCalledAET(calledAET);
		rq.setCallingAET(callingAET);
		rq.addPresContext(af.newPresContext(1,
				UIDs.Verification, tsUIDs));

		PDU ac = a.connect(rq);
		if (!(ac instanceof AAssociateAC)) {
			log.error("Association not accepted by " + calledAET + ": " + ac);
			return -5;
		}
		aa = af.newActiveAssociation(a, null);
		aa.start();
		if (a.getAcceptedTransferSyntaxUID(1) == null) {
			log
					.error("Echo Service not supported by remote AE: "
							+ calledAET);
			return -4;
		}
		DcmObjectFactory dof = DcmObjectFactory.getInstance();
		Command cmdRq = dof.newCommand();
		cmdRq.initCEchoRQ(a.nextMsgID());
		Dimse dimseRq = af.newDimse(1, cmdRq);
		final Dimse dimseRsp;
		try {
			dimseRsp = aa.invoke(dimseRq).get();
		} catch (InterruptedException ie) {
			log
					.error("Threading error during waiting for response of "
							+ cmdRq);
			return -6;
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
		
		return status;
	}
	
	
	/**
	 * Gets the ac timeout.
	 *
	 * @return Returns the acTimeout.
	 */
	public int getAcTimeout() {
		return acTimeout;
	}
	
	/**
	 * Sets the ac timeout.
	 *
	 * @param acTimeout The acTimeout to set.
	 */
	public void setAcTimeout(int acTimeout) {
		this.acTimeout = acTimeout;
	}
	
	/**
	 * Gets the called aet.
	 *
	 * @return Returns the calledAET.
	 */
	public String getCalledAET() {
		return calledAET;
	}
	
	/**
	 * Sets the called aet.
	 *
	 * @param calledAET The calledAET to set.
	 */
	public void setCalledAET(String calledAET) {
		this.calledAET = calledAET;
	}
	
	/**
	 * Gets the calling aet.
	 *
	 * @return Returns the callingAET.
	 */
	public String getCallingAET() {
		return callingAET;
	}
	
	/**
	 * Sets the calling aet.
	 *
	 * @param callingAET The callingAET to set.
	 */
	public void setCallingAET(String callingAET) {
		this.callingAET = callingAET;
	}
	
	/**
	 * Gets the dimse timeout.
	 *
	 * @return Returns the dimseTimeout.
	 */
	public int getDimseTimeout() {
		return dimseTimeout;
	}
	
	/**
	 * Sets the dimse timeout.
	 *
	 * @param dimseTimeout The dimseTimeout to set.
	 */
	public void setDimseTimeout(int dimseTimeout) {
		this.dimseTimeout = dimseTimeout;
	}
	
	/**
	 * Gets the max pd ulen.
	 *
	 * @return Returns the maxPDUlen.
	 */
	public int getMaxPDUlen() {
		return maxPDUlen;
	}
	
	/**
	 * Sets the max pd ulen.
	 *
	 * @param maxPDUlen The maxPDUlen to set.
	 */
	public void setMaxPDUlen(int maxPDUlen) {
		this.maxPDUlen = maxPDUlen;
	}
	
	/**
	 * Gets the priority.
	 *
	 * @return Returns the priority.
	 */
	public int getPriority() {
		return priority;
	}
	
	/**
	 * Sets the priority.
	 *
	 * @param priority The priority to set.
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	/**
	 * Gets the remote host name.
	 *
	 * @return Returns the remoteHostName.
	 */
	public String getRemoteHostName() {
		return remoteHostName;
	}
	
	/**
	 * Sets the remote host name.
	 *
	 * @param remoteHostName The remoteHostName to set.
	 */
	public void setRemoteHostName(String remoteHostName) {
		this.remoteHostName = remoteHostName;
	}
	
	/**
	 * Gets the remote port.
	 *
	 * @return Returns the remotePort.
	 */
	public int getRemotePort() {
		return remotePort;
	}
	
	/**
	 * Sets the remote port.
	 *
	 * @param remotePort The remotePort to set.
	 */
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}
	
	/**
	 * Gets the so close delay.
	 *
	 * @return Returns the soCloseDelay.
	 */
	public int getSoCloseDelay() {
		return soCloseDelay;
	}
	
	/**
	 * Sets the so close delay.
	 *
	 * @param soCloseDelay The soCloseDelay to set.
	 */
	public void setSoCloseDelay(int soCloseDelay) {
		this.soCloseDelay = soCloseDelay;
	}
}
