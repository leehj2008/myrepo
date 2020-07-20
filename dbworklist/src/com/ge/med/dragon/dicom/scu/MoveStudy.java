/**
 * MoveStudy.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scu;
/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/

import java.io.IOException;
import java.io.StringWriter;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AAssociateAC;
import org.dcm4che.net.AAssociateRQ;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.FutureRSP;
import org.dcm4che.net.PDU;
import org.dcm4che.util.DcmURL;
import org.dcm4che.util.SSLContextAdapter;

/**
 * <description>.
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      February 19, 2003
 * @version    $Revision: 1.1 $ <p>
 */

public class MoveStudy
{

    private final static int PCID_FIND = 1;
    private final static int PCID_MOVE = 3;
    private final static String STUDY_LABEL = "STUDY";
    private final static String[] TS = {
            UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian
            };

    // Attributes ----------------------------------------------------
    private final static Logger log = Logger.getLogger(MoveStudy.class);

    private String calledAET = "GEWLSERVER";
	private String callingAET = "AE_FAN";
	private int maxPDUlen = 16352;
	
	private String wlscpHostName = "3.36.230.59";
	private int wlscpPort = 5900;
	
	/**
	 * 
	 * @uml.property name="af"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private final static AssociationFactory af = AssociationFactory
		.getInstance();

	/**
	 * 
	 * @uml.property name="dof"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private final static DcmObjectFactory dof = DcmObjectFactory.getInstance();

	/**
	 * 
	 * @uml.property name="url"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private DcmURL url = null;

    private int priority = Command.MEDIUM;
    private int acTimeout = 5000;
    private int dimseTimeout = 0;
    private int soCloseDelay = 500;

	/**
	 * 
	 * @uml.property name="assocRQ"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private AAssociateRQ assocRQ = af.newAAssociateRQ();

    private boolean packPDVs = false;

	/**
	 * 
	 * @uml.property name="tls"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private SSLContextAdapter tls = null;

    private String[] cipherSuites = null;

	/**
	 * 
	 * @uml.property name="keys"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private Dataset keys = dof.newDataset();

	/**
	 * 
	 * @uml.property name="assoc"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private Association assoc = null;

	/**
	 * 
	 * @uml.property name="aassoc"
	 * @uml.associationEnd multiplicity="(0 1)"
	 */
	private ActiveAssociation aassoc = null;

    private String dest;
    private boolean dump = false;
    private final Map dumpParam = new HashMap(5);


    // Constructors --------------------------------------------------
    /**
     * Constructor for the MoveStudy object.
     *
     */
    public MoveStudy()
    {
        //initAssocParam();
        //initTLS(cfg);
        //initKeys(cfg);
        //dumpParam.put("maxlen", maxlen);
        //dumpParam.put("vallen", vallen);
        //dumpParam.put("prefix", prefix);

    }


    private static String maskNull(String aet)
    {
        return aet != null ? aet : "MOVESTUDY";
    }


    private final void initAssocParam()
    {
        assocRQ.setCalledAET(calledAET);
        assocRQ.setCallingAET(callingAET);
        assocRQ.setMaxPDULength(maxPDUlen);
        assocRQ.addPresContext(af.newPresContext(PCID_FIND,
                UIDs.StudyRootQueryRetrieveInformationModelFIND, TS));
        assocRQ.addPresContext(af.newPresContext(PCID_MOVE,
                UIDs.StudyRootQueryRetrieveInformationModelMOVE, TS));
    }


    /*private void initTLS()
    {
        try {
            cipherSuites = url.getCipherSuites();
            if (cipherSuites == null) {
                return;
            }
            tls = SSLContextAdapter.getInstance();
            char[] keypasswd = tls-key-passwd.toCharArray();
            tls.setKey(tls.loadKeyStore(
                    MoveStudy.class.getResource(cfg.getProperty("tls-key", "identity.p12")),
                    keypasswd),
                    keypasswd);
            tls.setTrust(tls.loadKeyStore(
                    MoveStudy.class.getResource(cfg.getProperty("tls-cacerts", "cacerts.jks")),
                    cfg.getProperty("tls-cacerts-passwd", "passwd").toCharArray()));
            tls.init();
        } catch (Exception ex) {
            throw new RuntimeException("Could not initalize TLS configuration: ", ex);
        }
    }*/

    // Methods -------------------------------------------------------
    /**
     * Description of the Method.
     *
     * @return                Description of the Return Value
     * @throws Exception the exception
     */
    public List query()
        throws Exception
    {
        if (aassoc == null) {
            throw new IllegalStateException("No Association established");
        }
        Command rqCmd = dof.newCommand();
        rqCmd.initCFindRQ(assoc.nextMsgID(),
                UIDs.StudyRootQueryRetrieveInformationModelFIND, priority);
        Dimse findRq = af.newDimse(PCID_FIND, rqCmd, keys);
        if (dump) {
            StringWriter w = new StringWriter();
            w.write("C-FIND RQ Identifier:\n");
            keys.dumpDataset(w, dumpParam);
            log.info(w.toString());
        }
        FutureRSP future = aassoc.invoke(findRq);
        Dimse findRsp = future.get();
        return future.listPending();
    }


    /**
     * Description of the Method.
     *
     * @param findRspList the find rsp list
     * @throws Exception the exception
     */
    public void move(List findRspList)
        throws Exception
    {
        if (aassoc == null) {
            throw new IllegalStateException("No Association established");
        }
        final int numStudies = findRspList.size();
        int numSeries = 0;
        int numInst = 0;
        int failed = 0;
        int warning = 0;
        int success = 0;
        for (int i = 0; i < numStudies; ++i) {
            Dimse findRsp = (Dimse) findRspList.get(i);
            Dataset findRspDs = findRsp.getDataset();
            if (dump) {
                StringWriter w = new StringWriter();
                w.write("C-FIND RSP Identifier:\n");
                findRspDs.dumpDataset(w, dumpParam);
                log.info(w.toString());
            }
            if (numSeries >= 0) {
                numSeries += findRspDs.getInt(
                        Tags.NumberOfStudyRelatedSeries, Integer.MIN_VALUE);
            }
            if (numInst >= 0) {
                numInst += findRspDs.getInt(
                        Tags.NumberOfStudyRelatedInstances, Integer.MIN_VALUE);
            }
            if (dest != null) {
                switch (doMove(findRspDs)) {
                    case 0x0000:
                        ++success;
                        break;
                    case 0xB000:
                        ++warning;
                        break;
                    default:
                        ++failed;
                        break;
                }
            }
        }
        log.info("Found " + numStudies + " Studies with "
                 + (numSeries >= 0 ? String.valueOf(numSeries) : "?")
                 + " Series with "
                 + (numInst >= 0 ? String.valueOf(numInst) : "?")
                 + " Instances");
        if (dest != null) {
            log.info("Successfully moved " + success + " Studies");
            if (warning > 0) {
                log.error("One or more Failures during move of "
                         + warning + " Studies");
            }
            if (failed > 0) {
                log.error("Failed to move " + failed + " Studies");
            }
        }
    }


    private int doMove(Dataset findRspDs)
        throws Exception
    {
        String suid = findRspDs.getString(Tags.StudyInstanceUID);
        String patName = findRspDs.getString(Tags.PatientName);
        String patID = findRspDs.getString(Tags.PatientID);
        String studyDate = findRspDs.getString(Tags.StudyDate);
        String prompt = "Study[" + suid + "] from " + studyDate
                 + " for Patient[" + patID + "]: " + patName;
        log.info("Moving " + prompt);
        Command rqCmd = dof.newCommand();
        rqCmd.initCMoveRQ(assoc.nextMsgID(),
                UIDs.StudyRootQueryRetrieveInformationModelMOVE,
                priority,
                dest);
        Dataset rqDs = dof.newDataset();
        rqDs.putCS(Tags.QueryRetrieveLevel, STUDY_LABEL);
        rqDs.putUI(Tags.StudyInstanceUID, suid);
        Dimse moveRq = af.newDimse(PCID_MOVE, rqCmd, rqDs);
        FutureRSP future = aassoc.invoke(moveRq);
        Dimse moveRsp = future.get();
        //moveRsp.getDataset().write
        Command rspCmd = moveRsp.getCommand();
        int status = rspCmd.getStatus();
        switch (status) {
            case 0x0000:
                log.info("Moved " + prompt);
                break;
            case 0xB000:
                log.error("One or more failures during move of " + prompt);
                break;
            default:
                log.error("Failed to move " + prompt
                         + "\n\terror tstatus: " + Integer.toHexString(status));
                break;
        }
        return status;
    }


    /**
     * Description of the Method.
     *
     * @return                               Description of the Return Value
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws GeneralSecurityException the general security exception
     */
    public boolean open()
        throws IOException, GeneralSecurityException
    {
        if (aassoc != null) {
            throw new IllegalStateException("Association already established");
        }
        assoc = af.newRequestor(
                newSocket(url.getHost(), url.getPort()));
        assoc.setAcTimeout(acTimeout);
        assoc.setDimseTimeout(dimseTimeout);
        assoc.setSoCloseDelay(soCloseDelay);
        assoc.setPackPDVs(packPDVs);

        PDU assocAC = assoc.connect(assocRQ);
        if (!(assocAC instanceof AAssociateAC)) {
            assoc = null;
            return false;
        }
        aassoc = af.newActiveAssociation(assoc, null);
        aassoc.start();
        return true;
    }


    /**
     * Description of the Method.
     *
     * @throws InterruptedException the interrupted exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void close()
        throws InterruptedException, IOException
    {
        if (assoc != null) {
            try {
                aassoc.release(false);
            } finally {
                assoc = null;
                aassoc = null;
            }
        }
    }


    private Socket newSocket(String host, int port)
        throws IOException, GeneralSecurityException
    {
        if (cipherSuites != null) {
            return tls.getSocketFactory(cipherSuites).createSocket(host, port);
        } else {
            return new Socket(host, port);
        }
    }
    
    /**
     * Description of the Method.
     *
     * @param args the arguments
     * @throws Exception the exception
     */
    public static void main(String args[])
        throws Exception
    {
        try {
            MoveStudy inst = new MoveStudy();
            if (inst.open()) {
                inst.move(inst.query());
                inst.close();
            }
        } catch (IllegalArgumentException e) {
            exit(e.getMessage(), true);
        }
    }
    
    private static void exit(String prompt, boolean error)
    {
        if (prompt != null) {
            System.err.println(prompt);
        }
        if (error) {
            
        }
        System.exit(1);
    }

}

