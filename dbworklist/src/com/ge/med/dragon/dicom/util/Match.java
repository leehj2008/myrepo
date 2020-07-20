/**
 * Match.java 1.0 2013-3-4
 *
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ge.med.dragon.rispacs.utils.DateUtil;

/**
 * @author Fan Yihui
 * @since 25.09.2005
 */
abstract class Match {
    protected final boolean type2;
    protected String column;

    protected Match(String alias, String field, boolean type2) {
        this.column = field;
        if (column == null) {
            throw new IllegalArgumentException("field is null");
        }
        if (alias != null) {
            this.column = alias + column.substring(column.indexOf('.'));
        }
        this.type2 = type2;
    }

    /**
     * Append to.
     *
     * @param sb the sb
     * @return true, if successful
     */
    public boolean appendTo(StringBuffer sb) {
        if (isUniveralMatch()) {
            return false;
        }
        sb.append('(');
        if (type2) {
            sb.append(column);
            sb.append(" IS NULL OR ");
        }
        appendBodyTo(sb);
        sb.append(')');
        return true;
    }

    /**
     * Checks if is univeral match.
     *
     * @return true, if is univeral match
     */
    public abstract boolean isUniveralMatch();

    protected abstract void appendBodyTo(StringBuffer sb);

    static class NULLValue extends Match {
        private boolean inverter;

        public NULLValue(String alias, String field, boolean inverter) {
            super(alias, field, false);
            this.inverter = inverter;
        }

        public boolean isUniveralMatch() {
            return false;
        }

        protected void appendBodyTo(StringBuffer sb) {
            sb.append(column);
            sb.append(" IS");
            if (inverter) {
                sb.append(" NOT");
            }
            sb.append(" NULL");
        }
    }

    static class SingleValue extends Match {
        private final String value;

        public SingleValue(String alias, String field, boolean type2, String value) {
            super(alias, field, type2);
            this.value = value;
        }

        public boolean isUniveralMatch() {
            return value == null || value.length() == 0;
        }

        protected void appendBodyTo(StringBuffer sb) {
            sb.append(column);
            sb.append(" = \'");
            sb.append(value);
            sb.append('\'');
        }
    }

    static class IntValue extends Match {
        private final int value;

        public IntValue(String alias, String field, boolean type2, int value) {
            super(alias, field, type2);
            this.value = value;
        }

        public boolean isUniveralMatch() {
            return false;
        }

        protected void appendBodyTo(StringBuffer sb) {
            sb.append(column);
            sb.append(" = ");
            sb.append(value);
        }
    }

    static class ListOfInt extends Match {
        private final int[] ints;

        public ListOfInt(String alias, String field, boolean type2, int[] ints) {
            super(alias, field, type2);
            this.ints = ints != null ? ints.clone() : new int[0];
        }

        public boolean isUniveralMatch() {
            return ints.length == 0;
        }

        protected void appendBodyTo(StringBuffer sb) {
            sb.append(column);
            if (ints.length == 1) {
                sb.append(" = ").append(ints[0]);
            } else {
                sb.append(" IN (").append(ints[0]);
                for (int i = 1; i < ints.length; i++) {
                    sb.append(", ").append(ints[i]);
                }
                sb.append(")");
            }
        }
    }

    static class AppendLiteral extends Match {
        private final String literal;

        public AppendLiteral(String alias, String field, boolean type2, String literal) {
            super(alias, field, type2);
            this.literal = literal;
        }

        public boolean isUniveralMatch() {
            return false;
        }

        protected void appendBodyTo(StringBuffer sb) {
            sb.append(column);
            sb.append(" ");
            sb.append(literal);
        }
    }

    static class ListOfUID extends Match {
        private final String[] uids;

        public ListOfUID(String alias, String field, boolean type2, String[] uids) {
            super(alias, field, type2);
            this.uids = uids != null ? uids.clone() : new String[0];
        }

        public boolean isUniveralMatch() {
            return uids.length == 0;
        }

        protected void appendBodyTo(StringBuffer sb) {
            sb.append(column);
            if (uids.length == 1) {
                sb.append(" = \'").append(uids[0]).append('\'');
            } else {
                sb.append(" IN ('").append(uids[0]);
                for (int i = 1; i < uids.length; i++) {
                    sb.append("\', \'").append(uids[i]);
                }
                sb.append("\')");
            }
        }
    }

    static class WildCard extends Match {
        private final char[] wc;
        private final boolean ignoreCase;

        public WildCard(String alias, String field, boolean type2, String wc,
                        boolean ignoreCase) {
            super(alias, field, type2);
            this.wc = wc != null ? wc.toCharArray() : new char[0];
            this.ignoreCase = ignoreCase;
        }

        public boolean isUniveralMatch() {
            for (int i = wc.length; --i >= 0; ) {
                if (wc[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        public boolean isLike() {
            for (int i = wc.length; --i >= 0; ) {
                if (wc[i] == '*' || wc[i] == '?' || wc[i] == '%') {
                    return true;
                }
            }
            return false;
        }

        protected void appendBodyTo(StringBuffer sb) {
            if (ignoreCase) {
                sb.append(" UPPER(");
            }
            sb.append(column);
            if (ignoreCase) {
                sb.append(')');
            }
            final boolean like = isLike();
            sb.append(like ? " LIKE " : " = ");
            if (ignoreCase) {
                sb.append(" UPPER(");
            }

            sb.append('\'');
            char c;
            for (char aWc : wc) {
                c = aWc;
                switch (c) {
                    case '?':
                        c = '_';
                        break;
                    case '*':
                        c = '%';
                        break;
                    case '\'':
                        sb.append('\'');
                        break;
                    case '_':
                        break;
                    case '%':
                        break;
                    default:
                        break;
                }
                sb.append(c);
            }
            sb.append('\'');

            if (ignoreCase) {
                sb.append(')');
            }
        }

    }

    static class Range extends Match {
        private final Date[] range;
        private boolean isDate = true;

        public Range(String alias, String field, boolean type2, Date[] range, boolean isDate) {
            super(alias, field, type2);
            this.range = range != null ? range.clone() : null;
            this.isDate = isDate;
        }

        public boolean isUniveralMatch() {
            return range == null;
        }

        protected void appendBodyTo(StringBuffer sb) {
            SimpleDateFormat df = new SimpleDateFormat(DateUtil.DATE_FORMAT);
            sb.append(column);
            if (isDate) {
                if (range.length == 1) {
                    sb.append("=");
                    sb.append(df.format(range[0]));
                } else if (range[0] == null) {
                    sb.append(" <= ");
                    sb.append(df.format(range[1]));
                } else if (range[1] == null) {
                    sb.append(" >= ");
                    sb.append(df.format(range[0]));
                } else {
                    sb.append(" BETWEEN ");
                    sb.append(df.format(range[0]));
                    sb.append(" AND ");
                    sb.append(df.format(range[1]));
                }
            } else {
                if (range.length == 1) {
                    sb.append("=");
                    sb.append("\'").append(df.format(range[0])).append("\'");
                } else if (range[0] == null) {
                    sb.append(" <= ");
                    sb.append("\'").append(df.format(range[1])).append("\'");
                } else if (range[1] == null) {
                    sb.append(" >= ");
                    sb.append("\'").append(df.format(range[0])).append("\'");
                } else {
                    sb.append(" >= ");
                    sb.append("\'").append(df.format(range[0])).append("\'");
                    sb.append(" AND ");
                    sb.append(column);
                    sb.append(" <= ");
                    sb.append("\'").append(df.format(range[1])).append("\'");
                }
            }

        }

    }

    static class ModalitiesInStudy extends Match {
        private final char[] wc;

        public ModalitiesInStudy(String alias, String md) {
            super(alias, "Series.modality", false);
            this.wc = md != null ? md.toCharArray() : new char[0];
        }

        public boolean isUniveralMatch() {
            for (int i = wc.length; --i >= 0; ) {
                if (wc[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        public boolean isLike() {
            for (int i = wc.length; --i >= 0; ) {
                if (wc[i] == '*' || wc[i] == '?') {
                    return true;
                }
            }
            return false;
        }

        protected void appendBodyTo(StringBuffer sb) {
            JdbcConfiguration jp = JdbcConfiguration.getInstance();
            sb.append("(SELECT count(*) FROM ");
            sb.append(jp.getProperty("Series"));
            sb.append(" WHERE ");
            sb.append(jp.getProperty("Series.study_fk"));
            sb.append(" = ");
            sb.append(jp.getProperty("Study.pk"));
            sb.append(" AND ");
            sb.append(column);
            final boolean like = isLike();
            sb.append(like ? " LIKE '" : " = '");
            char c;
            for (char aWc : wc) {
                c = aWc;
                switch (c) {
                    case '?':
                        c = '_';
                        break;
                    case '*':
                        c = '%';
                        break;
                    case '\'':
                        sb.append('\'');
                        break;
                    case '_':
                    case '%':
                        if (like) {
                            sb.append('\\');
                        }
                        break;
                    default:
                        break;
                }
                sb.append(c);
            }
            sb.append("') > 0");
        }
    }
}
