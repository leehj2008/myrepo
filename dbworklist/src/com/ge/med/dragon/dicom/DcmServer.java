/**
 * DcmServer.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom;

import java.io.File;
import java.util.Enumeration;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.dcm4che.dict.UIDs;
import org.dcm4che.net.AcceptorPolicy;
import org.dcm4che.net.AssociationFactory;
import org.dcm4che.net.DcmServiceBase;
import org.dcm4che.net.DcmServiceRegistry;
import org.dcm4che.server.DcmHandler;
import org.dcm4che.server.Server;
import org.dcm4che.server.ServerFactory;

import com.ge.med.dragon.dicom.scp.DragonScp;
import com.ge.med.dragon.dicom.scp.MWLFindScp;
import com.ge.med.dragon.dicom.scp.StoreScp;
import com.ge.med.dragon.dicom.service.impl.MppsImplFactory;
import com.ge.med.dragon.dicom.util.ConfParser;


/**
 * The Class DcmServer.
 *
 * @author Fan Yihui
 * @version $Revision: 1.1 $ $Date: 2001/09/08 05:32:52 $
 */

public class DcmServer {
	
	private static Logger log = Logger.getLogger(DcmServer.class);

	private final static ServerFactory srvFact = ServerFactory.getInstance();

	private final static AssociationFactory fact = AssociationFactory
			.getInstance();

	private AcceptorPolicy policy = fact.newAcceptorPolicy();

	private DcmServiceRegistry services = fact.newDcmServiceRegistry();

	private DcmHandler handler = srvFact.newDcmHandler(policy, services);

	private Server server = srvFact.newServer(handler);
	
	DcmServiceBase dcmScp = null;
	DcmServiceBase dcmScp_mpps = null;
	
	private int port = 104;
	
	//StoreScp storeSCP = null;
	//MWLFindScp mwlFindScp = null;
	//MppsScp mppsScp = null;
	
	private Configuration cfg = null;
	
	/**
	 * Instantiates a new dcm server.
	 *
	 * @param cfg the cfg
	 * @param scp the scp
	 * @param port the port
	 */
	public DcmServer(Configuration cfg, int scp, int port) {
		this.cfg = cfg;
		this.port = port;
		
		if(scp == 0){
			dcmScp = new StoreScp();
		} else if(scp == 1){
			dcmScp = new MWLFindScp(ConfParser.MWLSOURCE);
//			dcmScp_mpps = new DragonScp();
//			try {
//				((DragonScp) dcmScp_mpps).setMppsI(MppsImplFactory
//						.getMppsImpl(ConfParser.MPPSIMPLCLASSNAME));
//			} catch(ClassNotFoundException cnfe){
//				log.error(cnfe.getMessage(), cnfe);
//			} catch(InstantiationException ie){
//				log.error(ie.getMessage(), ie);
//			} catch(IllegalAccessException iae){
//				log.error(iae.getMessage(), iae);
//			}
		} else if(scp == 2){
			//dcmScp = new MppsScp();
			dcmScp = new DragonScp();
			try {
				((DragonScp) dcmScp).setMppsI(MppsImplFactory
						.getMppsImpl(ConfParser.MPPSIMPLCLASSNAME));
			} catch(ClassNotFoundException cnfe){
				log.error(cnfe.getMessage(), cnfe);
			} catch(InstantiationException ie){
				log.error(ie.getMessage(), ie);
			} catch(IllegalAccessException iae){
				log.error(iae.getMessage(), iae);
			}
		}
		
		initialServer();
		initialPolicy();
	}

	private void initialServer(){
		server.setPort(port);
		server.setLocalAddress(getIPAddress());
		server.setMaxClients(Integer.parseInt(cfg.getProperty("max-clients",
				"10")));
		handler.setRqTimeout(Integer.parseInt(cfg.getProperty("rq-timeout",
				"50000")));
		handler.setDimseTimeout(Integer.parseInt(cfg.getProperty(
				"dimse-timeout", "50000")));
		handler.setSoCloseDelay(Integer.parseInt(cfg.getProperty(
				"so-close-delay", "50000")));
		handler.setPackPDVs("true".equalsIgnoreCase(cfg.getProperty(
				"pack-pdvs", "false")));
	}
	
	private void initialPolicy() {
		policy.setCalledAETs(cfg.tokenize(cfg.getProperty("called-aets", null,
				"<any>", null)));
		policy.setCallingAETs(cfg.tokenize(cfg.getProperty("calling-aets",
				null, "<any>", null)));
		policy.setMaxPDULength(Integer.parseInt(cfg.getProperty("max-pdu-len",
				"16352")));
		policy.setAsyncOpsWindow(Integer.parseInt(cfg.getProperty(
				"max-op-invoked", "0")), 1);
		for (Enumeration it = cfg.keys(); it.hasMoreElements();) {
			String key = (String) it.nextElement();
			if (key.startsWith("pc.")) {
				initPresContext(key.substring(3), cfg.tokenize(cfg
						.getProperty(key)));
			}
		}
	}
	
	private void initPresContext(String asName, String[] tsNames) {
		String as = UIDs.forName(asName);
		String[] tsUIDs = new String[tsNames.length];
		for (int i = 0; i < tsUIDs.length; ++i) {
			tsUIDs[i] = UIDs.forName(tsNames[i]);
		}
		policy.putPresContext(as, tsUIDs);
		
		//services.bind(as, storeSCP);
		//
		//services.bind(as, mwlFindScp);
		services.bind(as, dcmScp);
		//services.bind(as, dcmScp_mpps);
	}
	
	/**
	 * Start.
	 *
	 * @throws Exception the exception
	 */
	public void start() throws Exception{
		if (server == null) {
			throw new Exception();
		}
		server.start();
	}

	/**
	 * Stop.
	 */
	public void stop() {
		if (server != null) {
			server.stop();
		}
	}

	private String getIPAddress(){
		return "0.0.0.0";
		/*
		try{
			InetAddress ipAddress = InetAddress.getLocalHost();
			return ipAddress.getHostAddress();
		} catch(UnknownHostException uhe){
			uhe.printStackTrace();
			return "127.0.0.1";
		}*/
	}
	
	private static final String USAGE =
		   "Usage: Loss the parameter of dicom scp type or port\n" +
		   "	There are three types that the server can support:\n\n" +
		   "	0/1/2 that means StoreScp/MWLScp/MPPSScp.\n\n" +
		   "	You should assign the port number for each SCP.";
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		/*try{
			LicenseInfo licenseInfo = new LicenseInfo();
			Feature f = new Feature("DMWL");
			licenseInfo.VerifyFeature(f);
		} catch(LicenseException le){
			System.out.println(le.getMessage());
			log.error(le.getMessage(), le);
			System.exit(0);
		}*/
		
//		if(args.length !=2){
//			System.out.println(USAGE);
//			System.exit(1);
//		}
		
		int type = 1;
		int port = 104;
		
		try {
			type = Integer.parseInt(args[0]);
		} catch (Exception e) {
			System.out.println("You only can input the 0 for StoreScp, 1 for MWLScp and 2 for MPPSScp");
			System.out.println("no type input, use default type:1:MWLScp");
			//System.exit(1);
		}
		
		try {
			port = Integer.parseInt(args[1]);
		} catch (Exception e) {
			System.out.println("You only can input the Integer for port.");
			System.out.println("no type input, use default port:"+port);
		}
		//System.out.println(new File("config/MwlScp.xml").getAbsolutePath());
		Configuration cfg = new Configuration(new File("config/dcmrcv.cfg"));
		
		DcmServer server = new DcmServer(cfg, type, port);
		log.warn("starting server..type:"+type+" at port:"+port);
		URL ru = Thread.currentThread().getContextClassLoader().getResource("log4j.properties");
		System.out.println(ru.getFile());
		try{
			server.start();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
