/**
 * FindScu.java 1.0 2013-3-4
 *
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scu;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.List;

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
 * The Class FindScu.
 *
 * @author lennon
 *         <p/>
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class FindScu {
    private static final int PCID_FIND = 1;

    private static final String STUDY_LABEL = "STUDY";
    private static final AssociationFactory af = AssociationFactory.getInstance();
    private static final DcmObjectFactory dof = DcmObjectFactory.getInstance();

    private int priority = Command.MEDIUM;
    private int acTimeout = 5000;
    private int dimseTimeout = 0;

    private int soCloseDelay = 500;

    private static final String[] TS = {
            UIDs.ExplicitVRLittleEndian,
            UIDs.ImplicitVRLittleEndian
    };
    // Attributes ----------------------------------------------------
    private String calledAET = "GEWLSERVER";
    private String callingAET = "AE_FAN";
    private int maxPDUlen = 16352;

    private int remotePort = 104;
    private AAssociateRQ assocRQ = af.newAAssociateRQ();

    private boolean packPDVs = false;

    private Association assoc = null;

    private ActiveAssociation aassoc = null;

    private Dataset keys = dof.newDataset();

    /**
     * Query.
     *
     * @return the list
     * @throws Exception the exception
     */
    public List query() throws Exception {
        if (aassoc == null) {
            throw new IllegalStateException("No Association established");
        }
        Command rqCmd = dof.newCommand();
        rqCmd.initCFindRQ(assoc.nextMsgID(),
                UIDs.StudyRootQueryRetrieveInformationModelFIND, priority);
        Dimse findRq = af.newDimse(PCID_FIND, rqCmd, keys);

        FutureRSP future = aassoc.invoke(findRq);
        Dimse findRsp = future.get();
        return future.listPending();
    }

    /**
     * Description of the Method.
     *
     * @return Description of the Return Value
     * @throws IOException              Signals that an I/O exception has occurred.
     * @throws GeneralSecurityException the general security exception
     */
    public boolean open()
            throws IOException, GeneralSecurityException {
        if (aassoc != null) {
            throw new IllegalStateException("Association already established");
        }
        assoc = af.newRequestor(
                newSocket(null, remotePort));
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

    private Socket newSocket(String host, int port) throws IOException,
            GeneralSecurityException {
        return new Socket(host, port);
    }

    private final void initAssocParam() {
        assocRQ.setCalledAET(calledAET);
        assocRQ.setCallingAET(callingAET);
        assocRQ.setMaxPDULength(maxPDUlen);
        assocRQ.addPresContext(af.newPresContext(PCID_FIND,
                UIDs.StudyRootQueryRetrieveInformationModelFIND, TS));
    }
}
