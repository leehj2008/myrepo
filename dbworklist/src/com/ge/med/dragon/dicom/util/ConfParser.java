/**
 * ConfParser.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.util;

import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ge.med.dragon.dicom.scp.config.DcmModalityNode;
import com.ge.med.dragon.dicom.scp.config.FilterElement;
import com.ge.med.dragon.dicom.scp.config.MappingElement;
import com.ge.med.dragon.dicom.scp.config.PersonNameElement;
import com.ge.med.dragon.dicom.scp.config.Sequence;
import com.ge.med.util.XmlUtils;

/**
 * The Class ConfParser.
 */
public class ConfParser {
	private static Logger log = Logger.getLogger(ConfParser.class);
	
	/** The columns. */
	public static String[] columns = {"PatientNameChinese","PatientID","ExamAccessionID","PatientGender","PatientBirthday","PreExamExamDate","ProcedureStepName","ModalityName"};
	
	/** The displays. */
	public static String[] displays = {"病人姓名","病人号","检查号","性别","出生日期","检查日期","检查过程","设备"};
	
	/** The tips. */
	public static String[] tips = {"病人姓名","病人号","检查号","性别","出生日期","检查日期","检查过程","设备"};
	
	/** The widths. */
	public static int[] widths = {150,100,150,50,100,100,200,100};
	
	/** The mppscolumns. */
	public static String[] mppscolumns = {"PatientNameChinese","PatientID","ExamAccessionID","PatientGender","PatientBirthday","PreExamExamDate","ProcedureStepName","ModalityName"};
	
	/** The mppsdisplays. */
	public static String[] mppsdisplays = {"病人姓名","病人号","检查号","性别","出生日期","检查日期","检查过程","设备"};
	
	/** The mppstips. */
	public static String[] mppstips = {"病人姓名","病人号","检查号","性别","出生日期","检查日期","检查过程","设备"};
	
	/** The mppswidths. */
	public static int[] mppswidths = {150,100,150,50,100,100,200,100};
	
	/** The sps sequence elements. */
	public static ArrayList spsSequenceElements = new ArrayList();
	
	/** The mwl elements. */
	public static ArrayList mwlElements = new ArrayList();
	
	/** The mpps sequence elements. */
	public static ArrayList mppsSequenceElements = new ArrayList();
	
	/** The mpps elements. */
	public static ArrayList mppsElements = new ArrayList();
	
	/** The mpps n set elements. */
	public static ArrayList mppsNSetElements = new ArrayList();
	
	
	/** The mwl host. */
	public static String mwlHost;
	
	/** The mwl port. */
	public static int mwlPort;
	
	/** The mwl aet. */
	public static String mwlAET;
	
	/** The mpps host. */
	public static String mppsHost;
	
	/** The mpps port. */
	public static int mppsPort;
	
	/** The mpps aet. */
	public static String mppsAET;
	
	/** The is refresh mwl. */
	public static boolean isRefreshMwl=false;
	
	/** The refresh mwl interval. */
	public static int refreshMwlInterval=5;
	
	/** The interval unit. */
	public static String intervalUnit="m";
	
	/** The mppsimplclassname. */
	public static String MPPSIMPLCLASSNAME="org.omg.faen.dicom.service.MPPS";
	
	/** The mwlsource. */
	public static String MWLSOURCE = "TEST";
	
	/** The wsdlurl. */
	public static URL WSDLURL;
	
	/** The service name. */
	public static QName SERVICE_NAME;
	
	static{
		/** worklist config*/
		try {
			Document document = XmlUtils.parseXmlFile("config/worklist.xml");
			//get root element
			Element root = document.getDocumentElement();
			
			/**builder elements*/
			NodeList elements = root.getElementsByTagName("element");
			
			NodeList sequence = root.getElementsByTagName("sequence");
			Element sequenceEl = (Element)sequence.item(0);
			String seqTag = sequenceEl.getAttribute("tag");
			
			NodeList spsElements = sequenceEl.getElementsByTagName("selement");
			
			columns = new String[elements.getLength()+spsElements.getLength()];
			displays = new String[elements.getLength()+spsElements.getLength()];
			tips = new String[elements.getLength()+spsElements.getLength()];
			widths = new int[elements.getLength()+spsElements.getLength()];
			
			for(int i=0; i<elements.getLength(); i++){
				Element column = (Element)elements.item(i);
				
				columns[i] = column.getAttribute("tag");
				mwlElements.add(columns[i]);
				displays[i] = column.getAttribute("name");
				tips[i] = column.getAttribute("tag");
				widths[i] = 100;
			}
			mwlElements.add(seqTag);
			
			for(int i=0; i<spsElements.getLength(); i++){
				Element column = (Element)spsElements.item(i);
				spsSequenceElements.add(column.getAttribute("tag"));
				columns[i+elements.getLength()] = column.getAttribute("tag");
				displays[i+elements.getLength()] = column.getAttribute("name");
				tips[i+elements.getLength()] = column.getAttribute("tag");
				widths[i+elements.getLength()] = 100;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Properties properties = new Properties();
		try{
			properties.load(new FileInputStream("config/dicom.properties"));
			
			mwlHost = properties.getProperty("mwlhost");
			mwlPort = Integer.parseInt(properties.getProperty("mwlport"));
			mwlAET = properties.getProperty("mwlaet");
			
			mppsHost = properties.getProperty("mppshost");
			mppsPort = Integer.parseInt(properties.getProperty("mppsport"));
			mppsAET = properties.getProperty("mppsaet");
			
			isRefreshMwl = new Boolean(properties.getProperty("isrefreshmwl")).booleanValue();
			refreshMwlInterval = Integer.parseInt(properties.getProperty("refreshinterval"));
			intervalUnit = properties.getProperty("intervalunit");
			
			properties.load(new FileInputStream("config/search.properties"));
			
			String url = properties.getProperty("wsdlurl");
			String ns = properties.getProperty("ns");
			String service = properties.getProperty("service");

			WSDLURL = new URL(url);
			SERVICE_NAME = new QName(ns, service); 
		} catch(Exception ex){
			ex.printStackTrace();
		}
		
		/** mpps configuration*/
		try {
			Document document = XmlUtils.parseXmlFile("config/mpps.xml"); 
			//get root element
			Element root = document.getDocumentElement();
			String classname = root.getAttribute("class");
			if(classname != null && !classname.equals("")){
				MPPSIMPLCLASSNAME = classname;
			}
			
			/**builder elements*/
			NodeList elements = root.getElementsByTagName("element");
			
			NodeList sequence = root.getElementsByTagName("sequence");
			Element sequenceEl = (Element)sequence.item(0);
			String seqTag = sequenceEl.getAttribute("tag");
			
			NodeList spsElements = sequenceEl.getElementsByTagName("selement");
			
			mppscolumns = new String[elements.getLength()+spsElements.getLength()];
			mppsdisplays = new String[elements.getLength()+spsElements.getLength()];
			mppstips = new String[elements.getLength()+spsElements.getLength()];
			mppswidths = new int[elements.getLength()+spsElements.getLength()];
			
			for(int i=0; i<elements.getLength(); i++){
				Element column = (Element)elements.item(i);
				mppscolumns[i] = column.getAttribute("tag");
				mppsElements.add(mppscolumns[i]);
				mppsdisplays[i] = column.getAttribute("name");
				mppstips[i] = column.getAttribute("tag");
				mppswidths[i] = 100;
			}
			//mwlElements.add(seqTag);
			
			for(int i=0; i<spsElements.getLength(); i++){
				Element column = (Element)spsElements.item(i);
				mppsSequenceElements.add(column.getAttribute("tag"));
				mppscolumns[i+elements.getLength()] = column.getAttribute("tag");
				mppsdisplays[i+elements.getLength()] = column.getAttribute("name");
				mppstips[i+elements.getLength()] = column.getAttribute("tag");
				mppswidths[i+elements.getLength()] = 100;
			}
			
			/**Mpps NSet elements*/
			NodeList nSet = root.getElementsByTagName("NSet");
			Element nSetEl = (Element)nSet.item(0);
			NodeList nsetElementlist = nSetEl.getElementsByTagName("update");
			for(int i=0; i<nsetElementlist.getLength(); i++){
				Element el = (Element)spsElements.item(i);
				mppsNSetElements.add(el.getAttribute("tag"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		/**DCMTag to SQL map*/
		try {
			Document document = XmlUtils.parseXmlFile("config/mapping.xml");  
			//get root element
			Element root = document.getDocumentElement();
			/**builder elements*/
			NodeList elements = root.getElementsByTagName("element");
			
			for (int i = 0; i < elements.getLength(); i++) {
				Element column = (Element) elements.item(i);
				DcmTagSQLMap.putC2Tag(column.getAttribute("tag"), column
						.getAttribute("name"));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		/**Sex Code map*/
		try {
			Document document = XmlUtils.parseXmlFile("config/sexcode.xml"); 
			//get root element
			Element root = document.getDocumentElement();
			/**builder elements*/
			NodeList elements = root.getElementsByTagName("element");
			
			for (int i = 0; i < elements.getLength(); i++) {
				Element column = (Element) elements.item(i);
				DcmTagSQLMap.putSexCode(column.getAttribute("name"), column
						.getAttribute("code"));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		parserMwlScp();
	}
	
	/**Modality Worklist Scp DCMTag to SQL map*/
	private static void parserMwlScp(){
		try {
			Document document = XmlUtils.parseXmlFile("config/MwlScp.xml");   
			//get root element
			Element root = document.getDocumentElement();
			String source = root.getAttribute("source");
			if(source != null && !source.equals("")){
				MWLSOURCE = source;
			}
			
			/**builder elements*/
			NodeList elements = root.getElementsByTagName("Node");
			
			for (int i = 0; i < elements.getLength(); i++) {
				DcmModalityNode mwlNode = new DcmModalityNode();
				
				Element nodeElement = (Element) elements.item(i);
				
				String aet = nodeElement.getAttribute("AET");
				String encode = nodeElement.getAttribute("ENCODE");
				String isUsingMK = nodeElement.getAttribute("IsUseMatchKey");
				String pnameDelimiter = nodeElement.getAttribute("PNDelimiter");
				
				/**build filter*/
				List filters = new ArrayList();
				
				NodeList filterNode = nodeElement.getElementsByTagName("Filter");
				Element filterElementNode = (Element)filterNode.item(0);
				NodeList filterElements = filterElementNode.getElementsByTagName("FilterElement");
				
				for(int j=0; j < filterElements.getLength(); j++){
					Element filterElement = (Element) filterElements.item(j);
					
					String name = filterElement.getAttribute("name");
					String value = filterElement.getAttribute("value");
					String generateClassName = filterElement.getAttribute("generate");
					
					FilterElement fe = new FilterElement();
					fe.setColumnName(name);
					fe.setValue(value);
					fe.setGenerate(generateClassName);
					
					filters.add(fe);
				}
				
				/**build mapping*/
				HashMap tag2C = new HashMap();
				HashMap c2Tag = new HashMap();
				HashMap tag2Seq = new HashMap();
				List tags = new ArrayList();
				List columns = new ArrayList();
				
				NodeList MappingNode = nodeElement.getElementsByTagName("Mapping");
				Element mapElementNode = (Element)MappingNode.item(0);
				
				buildMapping(mapElementNode, tag2C, c2Tag, tags, tag2Seq, columns);
				
				
				/** initial DcmModalityNode*/
				mwlNode.setAeTitle(aet);
				mwlNode.setEncode(StringUtil.toStringArray(encode, "\\"));
				mwlNode.setUsingMatchKey(Boolean.valueOf(isUsingMK).booleanValue());
				//mwlNode.setPnameDelimiter(pnameDelimiter);
				mwlNode.setFilter(filters);
				mwlNode.setTag2ColumnMap(tag2C);
				mwlNode.setColumn2TagMap(c2Tag);
				mwlNode.setTag2SeqMap(tag2Seq);
				mwlNode.setTags(tags);
				mwlNode.setColumns(columns)
;				
				/** put the aetitle and mwlnode*/
				DcmTagSQLMap.putModalityNodeMap(aet, mwlNode);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private static void buildMapping(Element element, HashMap tag2C, HashMap c2Tag, List tags, HashMap tag2Seq, List columns){
		NodeList nl = element.getChildNodes();
		
		for(int j=0; j<nl.getLength(); j++){
			Node node = nl.item(j);
			
			String nodeName = node.getNodeName();
			if(nodeName.equals("element")){
				Element mapElement = (Element) node;
				
				String vr = mapElement.getAttribute("vr");
				String tag = mapElement.getAttribute("tag");
				
				if(vr.equals("PN")){
					String delimiter = mapElement.getAttribute("delimiter");
					NodeList componentNodes = mapElement.getChildNodes();
					
					PersonNameElement pne = new PersonNameElement();
					pne.setDelimiter(delimiter);
					
					for(int n=0; n<componentNodes.getLength(); n++){
						Node no = componentNodes.item(n);
						String nname = no.getNodeName();
						if(nname.equals("component")){
							Element comElement = (Element) no;
							String name = comElement.getAttribute("name");
							String generate = comElement.getAttribute("generate");
							
							MappingElement me = new MappingElement();
							me.setColumnName(name);
							try {
								me.setGenerate(generate);
							} catch(Exception ex){
								log.error(ex);
							}
							
							if(!columns.contains(name)){
								columns.add(name);
							}
							
							pne.addComponent(me);
						}
					}
					
					tag2C.put(tag, pne);
				} else {
					String name = mapElement.getAttribute("name");
					
					String root = mapElement.getAttribute("root");
					String generateClassName = mapElement.getAttribute("generate");
					
					MappingElement mappingE = new MappingElement();
					mappingE.setColumnName(name);
					mappingE.setRoot(root);
					try{
						mappingE.setGenerate(generateClassName);
					} catch(Exception cnfe){
						log.error(cnfe.getMessage(), cnfe);
					}
					
					tag2C.put(tag, mappingE);
					c2Tag.put(name, tag);
					
					if(!columns.contains(name)){
						columns.add(name);
					}
				}
				
				tags.add(tag);
			} else if(nodeName.equals("sequence")){
				Element seqElement = (Element) node;
				
				String tag = seqElement.getAttribute("tag");
				String required = seqElement.getAttribute("required");
				
				tags.add(tag);
				
				Sequence seq = new Sequence();
				seq.setTag(tag);
				
				if(required == null || required.equals("")){
					seq.setRequired(true);
				} else {
					seq.setRequired(Boolean.valueOf(required).booleanValue());
				}
				
				
				HashMap h1 = new HashMap();
				HashMap h2 = new HashMap();
				List seqTags = new ArrayList();
				
				seq.setTag2ColumnMap(h1);
				seq.setColumn2TagMap(h2);
				seq.setTags(seqTags);

				tag2Seq.put(tag, seq);
				
				buildMapping(seqElement, h1, h2, seqTags, tag2Seq, columns);
			}
		}
	}
	
	private static void buildSequence(Element element, HashMap hashmap, List tagList){
		NodeList sequences = element.getElementsByTagName("sequence");
		
		if(sequences == null || sequences.getLength() == 0){
			return;
		}
		
		for(int j=0; j < sequences.getLength(); j++){
			Element seqElement = (Element) sequences.item(j);
			
			String tag = seqElement.getAttribute("tag");
			String required = seqElement.getAttribute("required");
			
			tagList.add(tag);
			
			Sequence seq = new Sequence();
			
			seq.setTag(tag);
			//seq.setRequired(Boolean.valueOf(required).booleanValue());
			if(required == null || required.equals("")){
				seq.setRequired(true);
			} else {
				seq.setRequired(Boolean.valueOf(required).booleanValue());
			}
			
			NodeList seqEles = seqElement.getElementsByTagName("element");
			
			HashMap h1 = new HashMap();
			HashMap h2 = new HashMap();
			
			List tags = new ArrayList();
			
			for(int n=0; n<seqEles.getLength(); n++){
				Element mapElement = (Element) seqEles.item(n);
				
				String stag = mapElement.getAttribute("tag");
				String name = mapElement.getAttribute("name");
				String defaultValue = mapElement.getAttribute("default");
				String generateClassName = mapElement.getAttribute("generate");
				
				MappingElement mappingE = new MappingElement();
				mappingE.setColumnName(name);
				mappingE.setRoot(defaultValue);
				try{
					mappingE.setGenerate(generateClassName);
				} catch(Exception cnfe){
					log.error(cnfe.getMessage(), cnfe);
				}
				
				h1.put(stag, mappingE);
				h2.put(name, tag);
				tags.add(stag);
			}
			
			seq.setTag2ColumnMap(h1);
			seq.setColumn2TagMap(h2);
			seq.setTags(tags);
			
			hashmap.put(tag, seq);
			
			NodeList seqSeqList = seqElement.getElementsByTagName("sequence");
			if(seqSeqList != null && seqSeqList.getLength()>0){
				buildSequence(seqElement, hashmap, tags);
			}
		}
	}
	
	private static Vector convertToVector(String source, String delim){
		Vector v = new Vector();
		
		StringTokenizer st = new StringTokenizer(source, delim);
		
		while(st.hasMoreElements()){
			v.addElement(st.nextElement());
		}
		
		return v;
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
	}
}


/******************************************************************************
 * Revision History 
 * [type 'revision' & press Alt + '/' to insert revision block]
 * 
 * 
 * 
 *     Copyright 2004 GE Healthcare Systems
 *     All rights reserved. 
 ******************************************************************************/
