/**
 * MWLCEQueryImpl.java 1.0 2013-3-4
 *
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.service.impl;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRMap;
import org.dcm4che.dict.VRs;

import com.ge.med.dragon.dicom.scp.config.DcmModalityNode;
import com.ge.med.dragon.dicom.scp.config.FilterElement;
import com.ge.med.dragon.dicom.scp.config.GenerateValue;
import com.ge.med.dragon.dicom.scp.config.MappingElement;
import com.ge.med.dragon.dicom.scp.config.PersonNameElement;
import com.ge.med.dragon.dicom.scp.config.Sequence;
import com.ge.med.dragon.dicom.service.MWLQuery;
import com.ge.med.dragon.dicom.util.ConfParser;
import com.ge.med.dragon.dicom.util.DcmExtensionsCharacterSet;
import com.ge.med.dragon.dicom.util.DcmTagSQLMap;
import com.ge.med.dragon.dicom.util.DicomUtil;
import com.ge.med.dragon.dicom.util.StringUtil;
import com.ge.med.dragon.rispacs.advancedquery.AdvancedQuery;
import com.ge.med.dragon.rispacs.advancedquery.AdvancedQueryPortType;
import com.ge.med.dragon.rispacs.types.message.advancedquery.entity.FilterCriteria;
import com.ge.med.dragon.rispacs.types.message.advancedquery.entity.FilterCriteriaItem;
import com.ge.med.dragon.rispacs.types.message.advancedquery.entity.ResultSchema;
import com.ge.med.dragon.rispacs.types.message.advancedquery.entity.Row;
import com.ge.med.dragon.rispacs.types.message.advancedquery.search.SearchRequest;
import com.ge.med.dragon.rispacs.types.message.advancedquery.search.SearchResponse;
import com.ge.med.dragon.rispacs.utils.DateUtil;
import com.ge.med.dragon.rispacs.utils.L10NBean;

public class MWLCEQueryImpl extends MWLQuery {

    private static final Logger LOGGER = Logger.getLogger(MWLFileQueryImpl.class);

    private DcmModalityNode dmn = null;
    Iterator<Row> searchResult = null;
    Row currentRow = null;

    List<String> columnList = null;

    public MWLCEQueryImpl() {
    }

    public void execute() throws Exception {
        dmn = DcmTagSQLMap.getMwlMapByAet(callingAET);

        //build a SearchRequest
        SearchRequest searchRequest = buildSearchRequest();

        //Call the web service of search
        AdvancedQuery service = new AdvancedQuery(ConfParser.WSDLURL, ConfParser.SERVICE_NAME);
        AdvancedQueryPortType advanceQuery = service.getAdvancedQueryPort();
        SearchResponse sr = advanceQuery.search(searchRequest);

        searchResult = sr.getResultSet().getRow().iterator();
    }

    public void setCallingAET(String callingAET) {
        this.callingAET = callingAET;
    }

    public void setKeyDataset(Dataset ds) {
        this.keys = ds;
    }

    public Dataset getDataset() throws Exception {
        Dataset dcmds = dof.newDataset();

        String[] encodes = dmn.getEncode();

        if (!keys.contains(Tags.SpecificCharacterSet)) {
            keys.putCS(Tags.SpecificCharacterSet, "");
        }

        DicomUtil.adjustDataset(dcmds, keys, encodes);

        for (Iterator it = keys.iterator(); it.hasNext(); ) {
            DcmElement dcmelement = (DcmElement) it.next();
            int tag = dcmelement.tag();
            int vr = dcmelement.vr();

            if (vr == VRs.SQ) {
                Sequence seq = dmn.getSeqByTag(tag);

                if (seq == null || !seq.isRequired()) {
                    continue;
                }

                buildSeqDs(seq, dcmds);
            } else {
                String value;
                if (vr == VRs.PN) {
                    PersonNameElement personName = dmn.getPersonNameElementByTag(tag);
                    if (personName == null) {
                        continue;
                    }

                    ArrayList<MappingElement> componentList = personName.getComponentList();
                    MappingElement[] mes = new MappingElement[componentList.size()];
                    for (int k = 0; k < componentList.size(); k++) {
                        mes[k] = componentList.get(k);
                    }

                    String delimiter = personName.getDelimiter();
                    value = buildPersonName(encodes, mes, delimiter);
                    dcmds.putXX(tag, ByteBuffer.wrap(DcmExtensionsCharacterSet.encode(value, encodes)));
                } else {
                    MappingElement me = dmn.getColumnByTag(tag);
                    if (me == null) {
                        continue;
                    }

                    int index = columnList.indexOf(me.getColumnName());
                    value = currentRow.getValue().get(index);
                    if (value == null) {
                        value = "";
                    }

                    value = L10NBean.unEscapeForXml(value);
                    if (tag == Tags.PatientSex) {
                        value = DcmTagSQLMap.getDCMSexCode(value);
                    }

                    if (value == null || value.equals("")) {
                        value = me.getRoot();
                    }

                    GenerateValue generate = me.getGenerate();
                    if (generate != null) {
                        value = me.getGenerate().generate(value);
                    }

                    DicomUtil.putElement(dcmds, vr, tag, value);
                }
            }
        }

        return dcmds;
    }

    private void buildSeqDs(Sequence seq, Dataset dataset) throws Exception {
        Dataset seqDs = dataset.getItem(Tags.valueOf(seq.getTag()));

        for (Object o : seq.getTags()) {
            int seqTag = Tags.valueOf(o.toString());
            int svr = VRMap.DEFAULT.lookup(seqTag);

            if (svr == VRs.SQ) {
                Sequence sequence = dmn.getSeqByTag(seqTag);
                if (sequence == null || !sequence.isRequired()) {
                    continue;
                }

                buildSeqDs(sequence, seqDs);
            } else {
                String value = "";
                if (svr == VRs.PN) {
                    String[] encodes = dmn.getEncode();
                    PersonNameElement personName = dmn.getPersonNameElementByTag(seqTag);

                    if (personName == null) {
                        continue;
                    }

                    ArrayList<MappingElement> componentList = personName.getComponentList();
                    MappingElement[] mes = new MappingElement[componentList.size()];
                    for (int k = 0; k < componentList.size(); k++) {
                        mes[k] = componentList.get(k);
                    }

                    String delimiter = personName.getDelimiter();
                    value = buildPersonName(encodes, mes, delimiter);
                    seqDs.putXX(seqTag, ByteBuffer.wrap(DcmExtensionsCharacterSet.encode(value, encodes)));
                } else {
                    MappingElement me = (MappingElement) seq.getTag2ColumnMap()
                            .get(Tags.toString(seqTag));

                    if (me == null) {
                        continue;
                    }

                    try {
                        int index = columnList.indexOf(me.getColumnName());
                        value = currentRow.getValue().get(index);
                        if (value == null || value.equals("")) {
                            value = me.getRoot();
                        }

                        value = L10NBean.unEscapeForXml(value);
                        GenerateValue generate = me.getGenerate();
                        if (generate != null) {
                            value = me.getGenerate().generate(value);
                        }
                    } catch (SQLException se) {
                        LOGGER.error(se.getMessage(), se);
                    } catch (Exception ex) {
                        LOGGER.error(ex.getMessage(), ex);
                    }

                    DicomUtil.putElement(seqDs, svr, seqTag, value);
                }
            }
        }
    }

    public boolean next() throws Exception {
        if (searchResult == null) {
            return false;
        }

        if (searchResult.hasNext()) {
            currentRow = searchResult.next();

            //System.out.println("");
            //for(int i=0; i<currentRow.getValue().size(); i++){
            //	System.out.print(currentRow.getValue().get(i)+"|");
            //}

            return true;
        } else {
            currentRow = null;
            return false;
        }
    }

    private SearchRequest buildSearchRequest() {
        //build a SearchRequest instance
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setPageNumber(1);
        searchRequest.setReturnQuantity(10000);

        //Initial a FilterCriteria instance
        FilterCriteria fc = new FilterCriteria();
        List<FilterCriteriaItem> fcItemList = fc.getFilterCriteriaItem();

        //Build FilterCriteria from key dataset
        buildFilterCriteriaFromMatchKey(keys, fcItemList);

        searchRequest.setFilterCriteria(fc);

        //Initial a ResultSchema instance
        ResultSchema resultSchema = new ResultSchema();
        columnList = resultSchema.getValues();
        for (int i = 0; i < dmn.getColumns().size(); i++) {
            String columnName = dmn.getColumns().get(i).toString();
            if (!columnList.contains(columnName)) {
                columnList.add(columnName);
                System.out.print(columnName + "|");
            }
        }

        searchRequest.setResultSchema(resultSchema);

        return searchRequest;
    }

    private void buildFilterCriteriaFromMatchKey(Dataset keys, List<FilterCriteriaItem> fcItemList) {
        //Start build matching key
        if (dmn.isUsingMatchKey()) {
            for (Iterator it = keys.iterator(); it.hasNext(); ) {
                DcmElement dcmelement = (DcmElement) it.next();
                int tag = dcmelement.tag();
                int vr = dcmelement.vr();

                if (vr == VRs.SQ) {
                    Sequence seq = dmn.getSeqByTag(tag);
                    Dataset seqDs = keys.getItem(tag);

                    if (seq == null || !seq.isRequired()) {
                        continue;
                    }

                    buildFilterCriteriaFromSeq(seq, seqDs, fcItemList);
                } else if (vr == VRs.TM) {
                } else {
                    if (vr == VRs.PN) {
                        //handle patient name query
                        PersonNameElement pne = dmn.getPersonNameElementByTag(tag);
                        String value = keys.getString(tag);

                        if (value == null || pne == null) {
                            continue;
                        }

                        String tmp[] = StringUtil.toStringArray(value, "=");

                        if (tmp.length == 1) {
                            if (tmp[0] == null || tmp[0].equals("")
                                    || tmp[0].equals("*") || tmp[0].equals("%")) {
                                continue;
                            }

                            MappingElement me = pne.getComponentList().get(0);
                            fcItemList.add(buildFilterCriteriaItem(me
                                    .getColumnName(), vr, tmp[0]));
                        }

                        for (int i = 1; i < tmp.length; i++) {
                            if (tmp[i] == null || tmp[i].equals("")
                                    || tmp[i].equals("*") || tmp[i].equals("%")) {
                                continue;
                            }

                            MappingElement me = pne.getComponentList().get(i);
                            fcItemList.add(buildFilterCriteriaItem(me
                                    .getColumnName(), vr, tmp[i]));
                        }
                    } else {
                        MappingElement me = dmn.getColumnByTag(tag);
                        if (me == null) {
                            continue;
                        }

                        String value = keys.getString(tag);

                        if (value == null || value.equals("")
                                || value.equals("*") || value.equals("%")) {
                            continue;
                        }

                        fcItemList.add(buildFilterCriteriaItem(me
                                .getColumnName(), vr, value));
                    }
                }
            }
        }//build matching key end

        //Start build filter
        List filters = dmn.getFilter();
        for (Object filter : filters) {
            FilterElement fe = (FilterElement) filter;
            String value = fe.getValue();

            int index = value.indexOf("^");

            if (index != -1) {
                String str1 = value.substring(0, index);
                String str2 = value.substring(index + 1, value.length());
                String[] dates = new String[]{str1, str2};
                //The date format from filter is yyyy-MM-dd
                fcItemList.add(buildFilterCriteriaItemForDate(fe.getColumnName(), dates, DateUtil.DATE_FORMAT));
            } else {
                fcItemList.add(buildFilterCriteriaItem(fe.getColumnName(), value));
            }
        }
        //build filter end
    }

    /**
     * @param columnName the column name
     * @param dates  the dates
     * @param dateFormat, the date format from filter is yyyy-MM-dd, from DIOCM is yyyyMMdd.
     *                    ****	In finally, the date format to query from CE is yyyy-MM-dd
     */
    private FilterCriteriaItem buildFilterCriteriaItemForDate(String columnName, String[] dates, String dateFormat) {
        FilterCriteriaItem fcItem = new FilterCriteriaItem();
        List<String> values = fcItem.getValues();
        fcItem.setField(columnName);

        if (dates.length == 1) {
            fcItem.setOperator("EQ");
        } else if (dates[0].equals("")) {
            fcItem.setOperator("LT");
        } else if (dates[1].equals("")) {
            fcItem.setOperator("GT");
        } else {
            fcItem.setOperator("BET");
        }
        try {
            for (String date : dates) {
                if (date == null || date.equals("")) {
                    continue;
                }
                values.add(new SimpleDateFormat(DateUtil.DATE_FORMAT).format((new SimpleDateFormat(dateFormat).parse(date))));
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        return fcItem;
    }

    private FilterCriteriaItem buildFilterCriteriaItem(String column, String value) {
        FilterCriteriaItem fcItem = new FilterCriteriaItem();
        List<String> values = fcItem.getValues();
        fcItem.setField(column);

        fcItem.setOperator("EQ");
        values.add(value);

        return fcItem;
    }

    private FilterCriteriaItem buildFilterCriteriaItem(String column, int vr,
                                                       String value) {
        if (vr == VRs.DA) {
            try {
                String dates[] = StringUtil.toStringArray(value, "-");
                //The date format from DICOM is yyyyMMdd
                return buildFilterCriteriaItemForDate(column, dates, "yyyyMMdd");
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
                return null;
            }
        } else {
            return buildFilterCriteriaItem(column, value);
        }
    }

    private void buildFilterCriteriaFromSeq(Sequence seq, Dataset seqDs, List<FilterCriteriaItem> fcItemsList) {
        List tags = seq.getTags();
        for (Object tag1 : tags) {
            String strTag = (String) tag1;
            int tag = Tags.valueOf(strTag);
            int vr = VRMap.DEFAULT.lookup(tag);

            if (vr == VRs.SQ) {
                Sequence subSeq = dmn.getSeqByTag(tag);
                if (subSeq == null || !subSeq.isRequired()) {
                    continue;
                }

                Dataset subSeqDs = seqDs.getItem(tag);
                if (subSeqDs == null) {
                    continue;
                }

                buildFilterCriteriaFromSeq(subSeq, subSeqDs, fcItemsList);
            } else if (vr == VRs.TM) {
            } else {
                if (vr == VRs.PN) {
                    //handle patient name query
                    PersonNameElement pne = dmn.getPersonNameElementByTag(tag);
                    String value = keys.getString(tag);

                    if (value == null || pne == null) {
                        continue;
                    }

                    String tmp[] = StringUtil.toStringArray(value, "=");

                    if (tmp.length == 1) {
                        if (tmp[0] == null || tmp[0].equals("")
                                || tmp[0].equals("*") || tmp[0].equals("%")) {
                            continue;
                        }

                        MappingElement me = pne.getComponentList().get(0);
                        fcItemsList.add(buildFilterCriteriaItem(me
                                .getColumnName(), vr, tmp[0]));
                    }

                    for (int i = 1; i < tmp.length; i++) {
                        if (tmp[i] == null || tmp[i].equals("")
                                || tmp[i].equals("*") || tmp[i].equals("%")) {
                            continue;
                        }

                        MappingElement me = pne.getComponentList().get(i);
                        fcItemsList.add(buildFilterCriteriaItem(me
                                .getColumnName(), vr, tmp[i]));
                    }
                } else {

                    MappingElement me = (MappingElement) seq.getTag2ColumnMap()
                            .get(strTag);
                    if (me == null) {
                        continue;
                    }

                    String value = seqDs.getString(tag);

                    if (value == null || value.equals("") || value.equals("*")
                            || value.equals("%")) {
                        continue;
                    }

                    fcItemsList.add(buildFilterCriteriaItem(me.getColumnName(),
                            vr, value));
                }
            }
        }
    }

    private String buildPersonName(String[] encodes, MappingElement[] mes, String delimiter) throws Exception {
        String value = "";
        StringBuilder sb = new StringBuilder(value);

        //The first component should be single byte
        String columnName = mes[0].getColumnName();
        int index = columnList.indexOf(columnName);
        value = currentRow.getValue().get(index);
        if (mes[0].getGenerate() != null) {
            value = mes[0].getGenerate().generate(value);
        }

        //For first single byte component, the delimiter will be set to ^ automatically.
        value = StringUtil.replaceDelimiter(value, "^");
        sb.append(value);

        for (int i = 1; i < mes.length; i++) {
            sb.append("=");
            index = columnList.indexOf(mes[i].getColumnName());
            String tmp = currentRow.getValue().get(index);
            tmp = StringUtil.replaceDelimiter(tmp, delimiter);
            sb.append(tmp);
        }

        return sb.toString();
    }

	@Override
	public void setCalledAET(String calledAET) {
		this.calledAET=calledAET;
		
	}
}
