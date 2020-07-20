/**
 * SqlBuilder.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * The Class SqlBuilder.
 *
 * @author Fan Yihui
 * @version $Revision: 1.2 $ $Date: 2001/12/14 03:44:33 $
 */

public class SqlBuilder {

    /** The Constant TYPE1. */
    public static final boolean TYPE1 = false;
    
    /** The Constant TYPE2. */
    public static final boolean TYPE2 = true;
    
    /** The Constant DESC. */
    public static final String DESC = " DESC";
    
    /** The Constant ASC. */
    public static final String ASC = " ASC";
    
    /** The Constant WHERE. */
    public static final String WHERE = " WHERE ";
    
    /** The Constant AND. */
    public static final String AND = " AND ";
    
    /** The Constant SELECT_COUNT. */
    public static final String[] SELECT_COUNT = { "count(*)" };
    private String[] select;
    private String[] from;
    private String[] leftJoin;
    private String[] relations;
    private ArrayList matches = new ArrayList();
    private ArrayList orderby = new ArrayList();
    private int limit = 0;
    private int offset = 0;
    private String whereOrAnd = WHERE;
    private boolean distinct = false;
    private String conditionLimitation;

    private static int getDatabase() {
        return 0;
    }

    /**
     * Sets the distinct.
     *
     * @param distinct the new distinct
     */
    public final void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }
    
    /**
     * Sets the select.
     *
     * @param fields the new select
     */
    public void setSelect(String[] fields) {
        select = fields;
    }

    /**
     * Sets the select count.
     */
    public void setSelectCount() {
        select = SELECT_COUNT;
    }

    /**
     * Sets the from.
     *
     * @param from the new from
     */
    public void setFrom(String[] from) {
        this.from = from;
    }

    /**
     * Sets the left join.
     *
     * @param leftJoin the new left join
     */
    public void setLeftJoin(String[] leftJoin) {
        if (leftJoin == null) {
            this.leftJoin = null;
            return;
        }
        if (leftJoin.length % 4 != 0) {
            throw new IllegalArgumentException("" + Arrays.asList(leftJoin));
        }
        this.leftJoin = leftJoin;
        //replace table name by alias name
        int i4;
        String alias, col;
        for (int i = 0, n = leftJoin.length/4; i < n; ++i) {
            i4 = 4*i;
            alias = this.leftJoin[i4+1];
            if (alias != null) {
                col = this.leftJoin[i4+3];
                this.leftJoin[i4+3] = alias + col.substring(col.indexOf('.'));
            }
        }
    }

    /**
     * Adds the order by.
     *
     * @param field the field
     * @param order the order
     */
    public void addOrderBy(String field, String order) {
        orderby.add(JdbcConfiguration.getInstance().getProperty(field) + order);
    }

    /**
     * Sets the limit.
     *
     * @param limit the new limit
     */
    public final void setLimit(int limit) {
        this.limit = Math.max(0, limit);
    }

    /**
     * Sets the offset.
     *
     * @param offset the new offset
     */
    public final void setOffset(int offset) {
        this.offset = Math.max(0, offset);
    }

    /**
     * Sets the relations.
     *
     * @param relations the new relations
     */
    public void setRelations(String[] relations) {
        if (relations == null) {
            this.relations = null;
            return;
        }
        if ((relations.length & 1) != 0) {
            throw new IllegalArgumentException(
                "relations[" + relations.length + "]");
        }
        this.relations = relations;
    }

    private void addMatch(Match match) {
        if (!match.isUniveralMatch())
            matches.add(match);
    }
    
    /**
     * Adds the null value match.
     *
     * @param alias the alias
     * @param field the field
     * @param inverter the inverter
     */
    public void addNULLValueMatch(String alias, String field, boolean inverter ) {
    	addMatch( new Match.NULLValue(alias, field, inverter ) );
    }

    /**
     * Adds the int value match.
     *
     * @param alias the alias
     * @param field the field
     * @param type2 the type2
     * @param value the value
     */
    public void addIntValueMatch(String alias, String field, boolean type2,
            int value) {
        addMatch(new Match.IntValue(alias, field, type2, value));
    }

    /**
     * Adds the list of int match.
     *
     * @param alias the alias
     * @param field the field
     * @param type2 the type2
     * @param values the values
     */
    public void addListOfIntMatch(String alias, String field, boolean type2,
            int[] values) {
        addMatch(new Match.ListOfInt(alias, field, type2, values));
    }

    /**
     * Adds the single value match.
     *
     * @param alias the alias
     * @param field the field
     * @param type2 the type2
     * @param value the value
     */
    public void addSingleValueMatch(String alias, String field, boolean type2,
        String value) {
        addMatch(new Match.SingleValue(alias, field, type2, value));
    }

    /**
     * Adds the literal match.
     *
     * @param alias the alias
     * @param field the field
     * @param type2 the type2
     * @param literal the literal
     */
    public void addLiteralMatch(String alias, String field, boolean type2,
            String literal) {
        addMatch(new Match.AppendLiteral(alias, field, type2, literal));
    }
    
    /**
     * Adds the boolean match.
     *
     * @param alias the alias
     * @param field the field
     * @param type2 the type2
     * @param value the value
     */
    public void addBooleanMatch(String alias,  String field, boolean type2,
            boolean value) {
        addMatch(new Match.AppendLiteral(alias, field, type2,
                toBooleanLiteral(value)));
    }
    
    private String toBooleanLiteral(boolean value) {
        switch (getDatabase()) {
        case JdbcConfiguration.DB2 :
        case JdbcConfiguration.ORACLE :
        case JdbcConfiguration.MYSQL :
            return value ? " != 0" : " = 0";
        default:
            return value ? " = true" : " = false";
        }
    }

    /**
     * Adds the list of uid match.
     *
     * @param alias the alias
     * @param field the field
     * @param type2 the type2
     * @param uids the uids
     */
    public void addListOfUidMatch(String alias, String field, boolean type2,
            String[] uids) {
        addMatch(new Match.ListOfUID(alias, field, type2, uids));
    }

    /**
     * Adds the wild card match.
     *
     * @param alias the alias
     * @param field the field
     * @param type2 the type2
     * @param wc the wc
     * @param ignoreCase the ignore case
     */
    public void addWildCardMatch(String alias, String field, boolean type2,
        String wc, boolean ignoreCase) {
        addMatch(new Match.WildCard(alias, field, type2, wc, ignoreCase));
    }

    /**
     * Adds the range match.
     *
     * @param alias the alias
     * @param field the field
     * @param type2 the type2
     * @param range the range
     * @param isDate the is date
     */
    public void addRangeMatch(String alias, String field, boolean type2,
            Date[] range, boolean isDate) {
        addMatch(new Match.Range(alias, field, type2, range, isDate));
    }

    /**
     * Adds the modalities in study match.
     *
     * @param alias the alias
     * @param md the md
     */
    public void addModalitiesInStudyMatch(String alias, String md) {
        addMatch(new Match.ModalitiesInStudy(alias, md));
    }

    /**
     * Gets the sql.
     *
     * @return the sql
     */
    public String getSql() {
		if (select == null)
			throw new IllegalStateException("select not initalized");
		if (from == null)
			throw new IllegalStateException("from not initalized");

		StringBuffer sb = new StringBuffer("SELECT ");
		/**append distinct*/
		if (distinct)
			sb.append("DISTINCT ");
		/**append limit*/
		if (limit > 0 || offset > 0) {
			switch (getDatabase()) {
			case JdbcConfiguration.HSQL:
				sb.append("LIMIT ");
				sb.append(offset);
				sb.append(" ");
				sb.append(limit);
				sb.append(" ");
				appendTo(sb, select);
				break;
			case JdbcConfiguration.DB2:
				sb.append("* FROM ( SELECT ");
				appendTo(sb, select);
				sb.append(", ROW_NUMBER() OVER (ORDER BY ");
				appendTo(sb, (String[]) orderby.toArray(new String[orderby
						.size()]));
				sb.append(") AS rownum ");
				break;
			case JdbcConfiguration.ORACLE:
				sb.append("* FROM ( SELECT ");
				appendTo(sb, selectC1C2CN());
				sb.append(", ROWNUM as r1 FROM ( SELECT ");
				appendTo(sb, selectAsC1C2CN());
				break;
			default:
				appendTo(sb, select);
				break;
			}
		} else {
			/**append select items*/
			appendTo(sb, select);
		}
		/**append from*/
		sb.append(" FROM ");
		
		appendInnerJoinsToFrom(sb);
		appendLeftJoinToFrom(sb);
		
		whereOrAnd = WHERE;
		
		appendInnerJoinsToWhere(sb);
		appendLeftJoinToWhere(sb);
		
		appendMatchesTo(sb);
		
		appendWhereTo(sb);
		
		if (!orderby.isEmpty()) {
			sb.append(" ORDER BY ");
			appendTo(sb, (String[]) orderby.toArray(new String[orderby.size()]));
		}
		if (limit > 0 || offset > 0) {
			switch (getDatabase()) {
			case JdbcConfiguration.PSQL:
			case JdbcConfiguration.MYSQL:
				sb.append(" LIMIT ");
				sb.append(limit);
				sb.append(" OFFSET ");
				sb.append(offset);
				break;
			case JdbcConfiguration.DB2:
				sb.append(" ) AS foo WHERE rownum > ");
				sb.append(offset);
				sb.append(" AND rownum <= ");
				sb.append(offset + limit);
				break;
			case JdbcConfiguration.ORACLE:
				sb.append(" ) WHERE ROWNUM <= ");
				sb.append(offset + limit);
				sb.append(" ) WHERE r1 > ");
				sb.append(offset);
				break;
			}
		}
		if (getDatabase() == JdbcConfiguration.DB2)
			sb.append(" FOR READ ONLY");
		return sb.toString();
	}

    private String[] selectC1C2CN() {
        String[] retval = new String[select.length]; 
        for (int i = 0; i < retval.length; i++)
            retval[i] = "c" + (i+1);
        return retval;
    }

    private String[] selectAsC1C2CN() {
        String[] retval = new String[select.length]; 
        for (int i = 0; i < retval.length; i++)
            retval[i] = select[i] + " AS c" + (i+1);
        return retval;
    }

    private void appendTo(StringBuffer sb, String[] a) {
        for (int i = 0; i < a.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(a[i]);
        }
    }

    private void appendLeftJoinToFrom(StringBuffer sb) {
        if (leftJoin == null) return;
        for (int i = 0, n = leftJoin.length/4; i < n; ++i) {
            final int i4 = 4*i;
	        if (getDatabase() == JdbcConfiguration.ORACLE) {
	            sb.append(", ");
	            sb.append(leftJoin[i4]);
                if (leftJoin[i4+1] != null) {
                    sb.append(" AS ");
                    sb.append(leftJoin[i4+1]);
                }
	        } else {
		        sb.append(" LEFT JOIN ");
		        sb.append(leftJoin[i4]);
                if (leftJoin[i4+1] != null) {
                    sb.append(" AS ");
                    sb.append(leftJoin[i4+1]);
                }
		        sb.append(" ON (");
		        sb.append(leftJoin[i4+2]);
		        sb.append(" = ");
		        sb.append(leftJoin[i4+3]);
		        sb.append(")");
	        }
        }
    }

    private void appendLeftJoinToWhere(StringBuffer sb) {
        if (leftJoin == null || getDatabase() != JdbcConfiguration.ORACLE) return;
        for (int i = 0, n = leftJoin.length/4; i < n; ++i) {
            final int i4 = 4*i;
	        sb.append(whereOrAnd);
	        whereOrAnd = AND;
	        sb.append(leftJoin[i4+2]);
	        sb.append(" = ");
	        sb.append(leftJoin[i4+3]);
	        sb.append("(+)");
        }
    }
        
	private void appendInnerJoinsToFrom(StringBuffer sb) {
		if (relations == null || getDatabase() == JdbcConfiguration.ORACLE) {
			appendTo(sb,from);
		} else {
			sb.append(from[0]);
			for (int i = 0, n = relations.length/2; i < n; ++i) {
			    final int i2 = 2*i;
				sb.append(" INNER JOIN ");
				sb.append(from[i+1]);
				sb.append(" ON (");
				sb.append(relations[i2]);
				sb.append(" = ");
				sb.append(relations[i2+1]);
				sb.append(")");
			}
		}
	}
	
	private void appendInnerJoinsToWhere(StringBuffer sb) {
		if (relations == null || getDatabase() != JdbcConfiguration.ORACLE) return;
        for (int i = 0, n = relations.length/2; i < n; ++i) {
            final int i2 = 2*i;
            sb.append(whereOrAnd);
            whereOrAnd = AND;
            sb.append(relations[i2]);
            sb.append(" = ");
            sb.append(relations[i2+1]);
        }
	}

    private void appendMatchesTo(StringBuffer sb) {
        if (matches == null) return;
        for (int i = 0; i < matches.size(); i++) {
            sb.append(whereOrAnd);
            whereOrAnd = AND;
            ((Match) matches.get(i)).appendTo(sb);
        }
    }
    
    private void appendWhereTo(StringBuffer sb){
    	if(conditionLimitation == null || conditionLimitation.trim().equals("")){
    		return;
    	}
    	sb.append(" AND ");
    	sb.append(conditionLimitation);
    }

	/**
	 * Gets the condition limitation.
	 *
	 * @return the condition limitation
	 */
	public String getConditionLimitation() {
		return conditionLimitation;
	}

	/**
	 * Sets the condition limitation.
	 *
	 * @param conditionLimitation the new condition limitation
	 */
	public void setConditionLimitation(String conditionLimitation) {
		this.conditionLimitation = conditionLimitation;
	}
}

