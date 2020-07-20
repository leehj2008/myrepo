/**
 * MWLFindScu.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scu;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;

/**
 * The Class MWLFindScu.
 *
 * @author Fan Yihui
 * @version $Revision: 1.1 $ $Date: 2001/09/08 05:32:52 $
 */

public class MWLFindScu {

	private static Logger log = Logger.getLogger(MWLFindScu.class);
	
	private String calledAET = "GEWLSERVER";
	private String callingAET = "FANYIHUI";
	private int maxPDUlen = 16352;
	private String[] tsUIDs = {UIDs.ImplicitVRLittleEndian, UIDs.ExplicitVRLittleEndian, UIDs.ExplicitVRBigEndian};
	private static String asUID = UIDs.ModalityWorklistInformationModelFIND;
	
	private String wlscpHostName = "3.36.230.59";
	private int wlscpPort = 5900;
	
	/** Holds Association timeout in ms. */
	private int acTimeout = 50000;
	
	/** Holds DICOM message timeout in ms. */
	private int dimseTimeout = 50000;
	
	/** Holds socket close delay in ms. */
	private int soCloseDelay;
	
	/** DICOM priority. Used for C-FIND. */
	private int priority = 0;
	
	/**
	 * Returns the DICOM priority as int value.
	 * <p>
	 * This value is used for CFIND.
	 * 0..MED, 1..HIGH, 2..LOW
	 * 
	 * @return Returns the priority.
	 */
	public final int getPriority() {
		return priority;
	}
	
	/**
	 * Set the DICOM priority.
	 * 
	 * @param priority The priority to set.
	 */
	public final void setPriority(int priority) {
		if ( priority < 0 || priority > 2 ) priority = 0;
		this.priority = priority;
	}
	
	/**
	 * Find mwl.
	 *
	 * @param searchDS the search ds
	 * @return the list
	 * @throws InterruptedException the interrupted exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws GeneralSecurityException the general security exception
	 */
	public List findMWL(Dataset searchDS) throws InterruptedException,
			IOException, GeneralSecurityException {
		ActiveAssociation assoc = null;
		List list = new ArrayList();
		// get association for mwl find.
		assoc = openAssoc(wlscpHostName, wlscpPort, getCFindAAssociateRQ());
		if (assoc == null) {
			log.error("Couldnt open association to " + wlscpHostName
					+ " in the port " + wlscpPort);
			return list;
		}
		Association as = assoc.getAssociation();
		if (as.getAcceptedTransferSyntaxUID(1) == null) {
			log.error(calledAET + " doesnt support CFIND request!", null);
			return list;
		}
		// send mwl cfind request.
		Command cmd = DcmObjectFactory.getInstance().newCommand();
		cmd.initCFindRQ(1, UIDs.ModalityWorklistInformationModelFIND,
				getPriority());
		Dimse mcRQ = AssociationFactory.getInstance()
				.newDimse(1, cmd, searchDS);
		if (log.isDebugEnabled())
			log.debug("make CFIND req:" + mcRQ);
		FutureRSP rsp = assoc.invoke(mcRQ);
		Dimse dimse = rsp.get();
		if (log.isDebugEnabled())
			log.debug("CFIND resp:" + dimse);
		List pending = rsp.listPending();
		if (log.isDebugEnabled())
			log.debug("CFIND pending:" + pending);
		Iterator iter = pending.iterator();
		while (iter.hasNext()) {
			list.add(((Dimse) iter.next()).getDataset());
		}
		list.add(dimse.getDataset()); 

		try {
			assoc.release(true);
		} catch (Exception e1) {
			log.error(
					"Cant release association for CFIND modality working list"
							+ assoc.getAssociation(), e1);
		}

		return list;
	}
	
	private ActiveAssociation openAssoc( String host, int port, AAssociateRQ assocRQ ) throws IOException, GeneralSecurityException {
		AssociationFactory aFact = AssociationFactory.getInstance();
		Association assoc = aFact.newRequestor( new Socket( host, port ) );
		assoc.setAcTimeout(acTimeout);
		assoc.setDimseTimeout(dimseTimeout);
        assoc.setSoCloseDelay(soCloseDelay);
		PDU assocAC = assoc.connect(assocRQ);
		if (!(assocAC instanceof AAssociateAC)) { return null; }
		ActiveAssociation retval = aFact.newActiveAssociation(assoc, null);
		retval.start();
		return retval;
    }
	
	private AAssociateRQ getCFindAAssociateRQ(){
		AssociationFactory aFact = AssociationFactory.getInstance();
    	AAssociateRQ assocRQ = aFact.newAAssociateRQ();
    	assocRQ.setCalledAET( calledAET );
    	assocRQ.setCallingAET( callingAET );
    	assocRQ.setMaxPDULength( maxPDUlen );
    	assocRQ.addPresContext(aFact.newPresContext(1,
    			asUID,
                tsUIDs ));
    	return assocRQ;
	}

	
	/**
	 * Gets the wlscp host name.
	 *
	 * @return the wlscp host name
	 */
	public String getWlscpHostName() {
		return wlscpHostName;
	}

	/**
	 * Sets the wlscp host name.
	 *
	 * @param wlscpHostName the new wlscp host name
	 */
	public void setWlscpHostName(String wlscpHostName) {
		this.wlscpHostName = wlscpHostName;
	}

	/**
	 * Gets the wlscp port.
	 *
	 * @return the wlscp port
	 */
	public int getWlscpPort() {
		return wlscpPort;
	}

	/**
	 * Sets the wlscp port.
	 *
	 * @param wlscpPort the new wlscp port
	 */
	public void setWlscpPort(int wlscpPort) {
		this.wlscpPort = wlscpPort;
	}

	/**
	 * Gets the called aet.
	 *
	 * @return the called aet
	 */
	public String getCalledAET() {
		return calledAET;
	}

	/**
	 * Sets the called aet.
	 *
	 * @param calledAET the new called aet
	 */
	public void setCalledAET(String calledAET) {
		this.calledAET = calledAET;
	}

	/**
	 * Gets the ac timeout.
	 *
	 * @return the ac timeout
	 */
	public int getAcTimeout() {
		return acTimeout;
	}

	/**
	 * Sets the ac timeout.
	 *
	 * @param acTimeout the new ac timeout
	 */
	public void setAcTimeout(int acTimeout) {
		this.acTimeout = acTimeout;
	}

	/**
	 * Gets the dimse timeout.
	 *
	 * @return the dimse timeout
	 */
	public int getDimseTimeout() {
		return dimseTimeout;
	}

	/**
	 * Sets the dimse timeout.
	 *
	 * @param dimseTimeout the new dimse timeout
	 */
	public void setDimseTimeout(int dimseTimeout) {
		this.dimseTimeout = dimseTimeout;
	}

	/**
	 * Gets the so close delay.
	 *
	 * @return the so close delay
	 */
	public int getSoCloseDelay() {
		return soCloseDelay;
	}

	/**
	 * Sets the so close delay.
	 *
	 * @param soCloseDelay the new so close delay
	 */
	public void setSoCloseDelay(int soCloseDelay) {
		this.soCloseDelay = soCloseDelay;
	}

}
