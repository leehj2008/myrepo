/**
 * MWLFindScp.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.scp;

import java.io.IOException;

import org.dcm4che.data.Command;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Status;
import org.dcm4che.dict.Tags;
import org.dcm4che.net.ActiveAssociation;
import org.dcm4che.net.Association;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceException;
import org.dcm4che.net.Dimse;
import org.dcm4che.net.DimseListener;

import com.ge.med.dragon.dicom.service.MWLQuery;
import com.ge.med.dragon.dicom.service.impl.MWLQueryFactory;

/**
 * The Class MWLFindScp.
 *
 * @author Fan Yihui
 * @version $Revision: 1.1 $ $Date: 2001/09/08 05:32:52 $
 */

public class MWLFindScp extends DcmServiceBase {
	private String queryType = "SQL";
	
	/**
	 * Instantiates a new mWL find scp.
	 *
	 * @param source the source
	 */
	public MWLFindScp(String source) {
		this.queryType = source;
	}

	protected MultiDimseRsp doCFind(ActiveAssociation assoc, Dimse rq,
			Command rspCmd) throws IOException, DcmServiceException {
		
		MWLQuery mwlQuery = null;
		try {
			Dataset rqData = rq.getDataset();
			String callingAET = assoc.getAssociation().getCallingAET();
			String calledAET = assoc.getAssociation().getCalledAET();
			
			mwlQuery = MWLQueryFactory.getInstance(queryType);
			
			mwlQuery.setKeyDataset(rqData);
			mwlQuery.setCallingAET(callingAET);
			mwlQuery.setCalledAET(calledAET);
			mwlQuery.execute();

		} catch (Exception e) {
			e.printStackTrace();
			throw new DcmServiceException(Status.ProcessingFailure, e);
		}
		return new MultiCFindRsp(mwlQuery);
	}

	protected void doCEcho(ActiveAssociation arg0, Dimse rq, Command rspCmd)
			throws IOException, DcmServiceException {
		rspCmd.putUS(Tags.Status, SUCCESS);
	}
	
	private class MultiCFindRsp implements MultiDimseRsp {
		private final MWLQuery queryCmd;

		private boolean canceled = false;

		public MultiCFindRsp(MWLQuery queryCmd) {
			this.queryCmd = queryCmd;
		}

		public DimseListener getCancelListener() {
			return new DimseListener() {
				public void dimseReceived(Association assoc, Dimse dimse) {
					canceled = true;
				}
			};
		}

		public Dataset next(ActiveAssociation assoc, Dimse rq, Command rspCmd)
				throws DcmServiceException {
			if (canceled) {
				rspCmd.putUS(Tags.Status, Status.Cancel);
				return null;
			}
			try {
				if (!queryCmd.next()) {
					rspCmd.putUS(Tags.Status, Status.Success);
					return null;
				}
				
				rspCmd.putUS(Tags.Status, Status.Pending);
				Dataset rspData = queryCmd.getDataset();
				
				System.out.println(rspData);
				
				return rspData;
			} catch (Exception e) {
				e.printStackTrace();
				throw new DcmServiceException(Status.ProcessingFailure, e);
			}
		}

		public void release() {
		}
	}

	/**
	 * Gets the query type.
	 *
	 * @return the query type
	 */
	public String getQueryType() {
		return queryType;
	}

	/**
	 * Sets the query type.
	 *
	 * @param queryType the new query type
	 */
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
}
