/**
 * MWLSQLQueryImpl.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.VRMap;
import org.dcm4che.dict.VRs;

import com.ge.med.dragon.dicom.scp.config.ConfigurationException;
import com.ge.med.dragon.dicom.scp.config.DcmModalityNode;
import com.ge.med.dragon.dicom.scp.config.FilterElement;
import com.ge.med.dragon.dicom.scp.config.GenerateValue;
import com.ge.med.dragon.dicom.scp.config.MappingElement;
import com.ge.med.dragon.dicom.scp.config.PersonNameElement;
import com.ge.med.dragon.dicom.scp.config.Sequence;
import com.ge.med.dragon.dicom.service.MWLQuery;
import com.ge.med.dragon.dicom.util.DcmExtensionsCharacterSet;
import com.ge.med.dragon.dicom.util.DcmTagSQLMap;
import com.ge.med.dragon.dicom.util.DicomUtil;
import com.ge.med.dragon.dicom.util.JdbcConfiguration;
import com.ge.med.dragon.dicom.util.SqlBuilder;
import com.ge.med.dragon.dicom.util.StringUtil;

/**
 * The Class MWLSQLQueryImpl.
 *
 * @author Fan Yihui
 * @version $Revision: 1.2 $ $Date: 2001/12/14 03:44:33 $
 */

public class MWLSQLQueryImpl extends MWLQuery {
	private static Logger log = Logger.getLogger(MWLFileQueryImpl.class);

	private ResultSet rs = null;
	private Statement stmt = null;
	private Connection conn = null;

	private DcmModalityNode dmn = null;

	/**
	 * Instantiates a new mWLSQL query impl.
	 */
	public MWLSQLQueryImpl() {
	}

	/** {@inheritDoc. Override} */
	public void setCallingAET(String callingAET) {
		this.callingAET = callingAET;
	}
	
	/** {@inheritDoc. Override} */
	public void setCalledAET(String calledAET) {
		this.calledAET = calledAET;
	}

	/** {@inheritDoc. Override} */
	public void execute() throws ConfigurationException, SQLException {
		if (keys == null) {
			throw new ConfigurationException("The key dataset is null.");
		}

		if (callingAET == null) {
			throw new ConfigurationException("The calling AET is null.");
		}

		try {
			execute(keys);
		} catch (SQLException se) {
			log.error(se.getMessage(), se);
			throw se;
		} catch (ConfigurationException ce) {
			log.error(ce.getMessage(), ce);
			throw ce;
		}
	}

	/** {@inheritDoc. Override} */
	public boolean next() throws SQLException {
		if (rs == null) {
			return false;
		}

		if (rs.next()) {
			return true;
		} else {
			/*if (conn != null) {
				try {
					conn.close();
				} catch (SQLException se) {
					log.error(se.getMessage(), se);
				}
			}*/
			
			if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.rollback();
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
			return false;
		}
	}

	/** {@inheritDoc. Override} */
	public Dataset getDataset() throws Exception {
		Dataset dcmds = dof.newDataset();

		String[] encodes = dmn.getEncode();

		if (!keys.contains(Tags.SpecificCharacterSet)) {
			keys.putCS(Tags.SpecificCharacterSet, "");
		}

		DicomUtil.adjustDataset(dcmds, keys, encodes);

		for (Iterator it = keys.iterator(); it.hasNext();) {
			DcmElement dcmelement = (DcmElement) it.next();

			int tag = dcmelement.tag();
			int vr = dcmelement.vr();

			int vm = dcmelement.countItems();

			if (vr == VRs.SQ) {
				Sequence seq = dmn.getSeqByTag(tag);

				if (seq == null || !seq.isRequired()) {
					continue;
				}

				buildSeqDs(seq, dcmds);
			} else {
				String value = "";

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
				} else {

					MappingElement me = dmn.getColumnByTag(tag);

					if (me == null) {
						continue;
					}

					value = rs.getString(me.getColumnName());

					if (value == null) {
						value = "";
					}

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
				}
				// Put value to dataset
				DicomUtil.putElement(dcmds, vr, tag, value);
			}
		}

		return dcmds.subSet(keys);
	}

	/** {@inheritDoc. Override} */
	public void setKeyDataset(Dataset ds) {
		this.keys = ds;
	}

	private boolean execute(Dataset keyDS) throws SQLException, ConfigurationException {
		/** get map by aet */
		dmn = DcmTagSQLMap.getMwlMapByAet(this.calledAET);

		SqlBuilder sqlBuilder = new SqlBuilder();

		if (dmn.isUsingMatchKey()) {
			buildSqlFromMatchKey(keys, sqlBuilder);
		}

		// build sql from pre-defined filters
		List filters = dmn.getFilter();
		for (int i = 0; i < filters.size(); i++) {
			FilterElement fe = (FilterElement) filters.get(i);
			String value = fe.getValue();

			int index = value.indexOf("^");

			if (index != -1) {
				String str1 = value.substring(0, index);
				String str2 = value.substring(index + 1, value.length());
				String[] dates = new String[] { str1, str2 };
				try {
					Date range[] = StringUtil.toDateFromStr(dates, new SimpleDateFormat("yyyyMMdd"));
					sqlBuilder.addRangeMatch(null, fe.getColumnName(), false, range, false);
				} catch (ParseException pe) {
					log.error(pe);
				}
			} else {
				sqlBuilder.addSingleValueMatch(null, fe.getColumnName(), false, fe.getValue());
			}

		}

		sqlBuilder.setFrom(JdbcConfiguration.getInstance().getFrom());
		sqlBuilder.setRelations(JdbcConfiguration.getInstance().getRelations());
		sqlBuilder.setSelect(JdbcConfiguration.getInstance().getSelect());
		sqlBuilder.setLeftJoin(JdbcConfiguration.getInstance().getLeftJoin());

		sqlBuilder.setConditionLimitation(JdbcConfiguration.getInstance().getLimitation());

		log.info(sqlBuilder.getSql());
		
		try {
			String url = JdbcConfiguration.getInstance().getUrl();
			String driver = JdbcConfiguration.getInstance().getDriver();
			String username = JdbcConfiguration.getInstance().getUsername();
			String password = JdbcConfiguration.getInstance().getPassword();

			Class.forName(driver);

			conn = DriverManager.getConnection(url, username, password);

			conn.setAutoCommit(false);

			stmt = conn.createStatement();
			rs = stmt.executeQuery(sqlBuilder.getSql());

			conn.commit();
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			return false;
		} finally {
		    /*if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                conn.rollback();
                try {
                    conn.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/
		}

		return true;
	}

	private void buildSeqDs(Sequence seq, Dataset dataset) throws Exception {
		Dataset seqDs = dataset.getItem(Tags.valueOf(seq.getTag()));

		for (Iterator itj = seq.getTags().iterator(); itj.hasNext();) {
			int seqTag = Tags.valueOf(itj.next().toString());

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
				} else {

					MappingElement me = (MappingElement) seq.getTag2ColumnMap().get(Tags.toString(seqTag));

					if (me == null) {
						continue;
					}

					value = rs.getString(me.getColumnName());

					if (value == null || value.equals("")) {
						value = me.getRoot();
					}

					GenerateValue generate = me.getGenerate();

					if (generate != null) {
						value = me.getGenerate().generate(value);
					}
				}

				DicomUtil.putElement(seqDs, svr, seqTag, value);
			}
		}
	}

	private void buildSqlFromSeq(Sequence seq, Dataset seqDs, SqlBuilder sqlBuilder) {
		List tags = seq.getTags();

		for (Iterator it = tags.iterator(); it.hasNext();) {
			String strTag = (String) it.next();
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

				buildSqlFromSeq(subSeq, subSeqDs, sqlBuilder);
			} else {
				if (vr == VRs.PN) {
					// handle patient name query
					PersonNameElement pne = dmn.getPersonNameElementByTag(tag);

					String value = keys.getString(tag);

					if (value == null || pne == null) {
						continue;
					}

					buildSqlForPersonName(value, sqlBuilder, vr, pne);
				} else {

					MappingElement me = (MappingElement) seq.getTag2ColumnMap().get(strTag);
					if (me == null) {
						continue;
					}

					String value = seqDs.getString(tag);

					if (value == null || value.equals("") || value.equals("*") || value.equals("%")) {
						continue;
					}

					buildSqlFromMapElement(me.getColumnName(), vr, value, sqlBuilder);
				}
			}
		}
	}

	private void buildSqlForPersonName(String value, SqlBuilder sqlBuilder, int vr, PersonNameElement pne) {
		String tmp[] = StringUtil.toStringArray(value, "=");

		if (tmp.length == 1) {
			if (tmp[0] == null || tmp[0].equals("") || tmp[0].equals("*") || tmp[0].equals("%")) {
				return;
			}

			MappingElement me = pne.getComponentList().get(0);

			buildSqlFromMapElement(me.getColumnName(), vr, tmp[0], sqlBuilder);
		}

		for (int i = 1; i < tmp.length; i++) {
			if (tmp[i] == null || tmp[i].equals("") || tmp[i].equals("*") || tmp[i].equals("%")) {
				continue;
			}

			MappingElement me = pne.getComponentList().get(i);

			buildSqlFromMapElement(me.getColumnName(), vr, tmp[i], sqlBuilder);
		}
	}

	private void buildSqlFromMapElement(String column, int vr, String value, SqlBuilder sqlBuilder) {
		if (column.equals("ExamID") || column.indexOf("PatientIntraID") != -1 || column.indexOf("StationID") != -1
				|| column.indexOf("ResourceID") != -1) {
			sqlBuilder.addIntValueMatch(null, column, false, Integer.parseInt(value));
		} else {
			if (vr == VRs.DA) {
				try {
					String dates[] = StringUtil.toStringArray(value, "-");
					Date[] range = StringUtil.toDateFromStr(dates, new SimpleDateFormat("yyyyMMdd"));
					sqlBuilder.addRangeMatch(null, column, false, range, false);
				} catch (Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			} else {
				if (value.startsWith("*") || value.startsWith("%") || value.startsWith("?") || value.endsWith("*") || value.endsWith("%")
						|| value.endsWith("?")) {
					sqlBuilder.addWildCardMatch(null, column, false, value, false);
				} else {
					sqlBuilder.addSingleValueMatch(null, column, false, value);
				}
			}
		}
	}

	private void buildSqlFromMatchKey(Dataset keys, SqlBuilder sqlBuilder) {
		for (Iterator it = keys.iterator(); it.hasNext();) {
			DcmElement dcmelement = (DcmElement) it.next();
			int tag = dcmelement.tag();
			int vr = dcmelement.vr();

			if (vr == VRs.SQ) {
				Sequence seq = dmn.getSeqByTag(tag);
				Dataset seqDs = keys.getItem(tag);

				if (seq == null || !seq.isRequired()) {
					continue;
				}

				buildSqlFromSeq(seq, seqDs, sqlBuilder);
			} else {
				if (vr == VRs.PN) {
					// handle patient name query
					PersonNameElement pne = dmn.getPersonNameElementByTag(tag);

					String value = keys.getString(tag);

					if (value == null || pne == null) {
						continue;
					}

					buildSqlForPersonName(value, sqlBuilder, vr, pne);
				} else {
					MappingElement me = (MappingElement) dmn.getColumnByTag(tag);
					if (me == null) {
						continue;
					}

					String value = keys.getString(tag);

					if (value == null || value.equals("") || value.equals("*") || value.equals("%")) {
						continue;
					}

					buildSqlFromMapElement(me.getColumnName(), vr, value, sqlBuilder);
				}
			}
		}
	}

	private String buildPersonName(String[] encodes, MappingElement[] mes, String delimiter) throws Exception {
		String value = "";
		StringBuffer sb = new StringBuffer(value);

		// The first component should be single byte
		String columnName = mes[0].getColumnName();

		value = rs.getString(columnName);

		if (mes[0].getGenerate() != null) {
			value = mes[0].getGenerate().generate(value);
		}

		value = StringUtil.replaceDelimiter(value, delimiter);

		sb.append(value);

		for (int i = 1; i < mes.length; i++) {
			sb.append("=");

			String tmp = rs.getString(columnName);

			tmp = StringUtil.replaceDelimiter(tmp, delimiter);

			if (encodes.length == 1) {
				sb.append(tmp);
			} else {
				String[] values = StringUtil.toStringArray(tmp, delimiter);

				for (int n = 0; n < values.length; n++) {
					sb.append(DcmExtensionsCharacterSet.encode(values[n], encodes[0], encodes[1]));
					if (n != values.length - 1) {
						sb.append(delimiter);
					}
				}
			}
		}

		return sb.toString();
	}
}
