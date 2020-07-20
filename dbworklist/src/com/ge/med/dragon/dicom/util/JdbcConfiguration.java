/**
 * JdbcConfiguration.java 1.0 2013-3-4
 * 
 * Copyright 2013 GE Healthcare Systems. All rights reserved.
 * GE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.ge.med.dragon.dicom.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * The Class JdbcConfiguration.
 *
 * @author Fan Yihui
 * @version $Revision: 1.2 $ $Date: 2001/12/14 03:44:33 $
 */

public class JdbcConfiguration extends Properties{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;
    
    /** The Constant HSQL. */
    public static final int HSQL = 0;
    
    /** The Constant PSQL. */
    public static final int PSQL = 1;
    
    /** The Constant MYSQL. */
    public static final int MYSQL = 2;
    
    /** The Constant DB2. */
    public static final int DB2 = 3;
    
    /** The Constant ORACLE. */
    public static final int ORACLE = 4;

    private static final String HSQL_VAL = "Hypersonic SQL";
    private static final String PSQL_VAL = "PostgreSQL 7.2";
    private static final String MYSQL_VAL = "mySQL";
    private static final String DB2_VAL = "DB2";
    private static final String ORACLE_VAL = "Oracle9i";
    private static final String DATASOURCE_KEY = "datasource";
    private static final String DS_MAPPING_KEY = "datasource-mapping";
    private static final JdbcConfiguration instance = new JdbcConfiguration();

    private static final String URL = "url";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String DRIVER = "driver";
    private static final String SELECT = "select";
    private static final String FROM = "from";
    private static final String LEFTJOIN = "leftJoin";
    private static final String RELATIONS = "relations";
    private static final String LIMITATION = "limitation";
    
    private final String datasource;
    private final int database;
    private final String url;
    private final String username;
    private final String password;
    private final String driver;
    private final String[] select;
    private final String[] from;
    private final String[] leftJoin;
    private final String[] relations;
    private final String limitation;

    /**
     * Gets the limitation.
     *
     * @return the limitation
     */
    public String getLimitation() {
		return limitation;
	}

	/**
	 * Gets the single instance of JdbcConfiguration.
	 *
	 * @return single instance of JdbcConfiguration
	 */
	public static JdbcConfiguration getInstance() {
        return instance;
    }

    /**
     * Gets the properties.
     *
     * @param keys the keys
     * @return the properties
     */
    public String[] getProperties(String[] keys) {
        String[] values = new String[keys.length];
        for (int i = 0; i < keys.length; i++)
            values[i] = getProperty(keys[i]);
        return values;
    }

    /** {@inheritDoc. Override} */
    public String getProperty(String key) {
        if (key == null || Character.isLowerCase(key.charAt(0)))
                return key;
        String value = super.getProperty(key);
        if (value == null)
            throw new IllegalArgumentException("key: " + key);
        return value;
    }

    /**
     * Gets the database.
     *
     * @return the database
     */
    public int getDatabase() {
        return database;
    }

    /**
     * Gets the data source.
     *
     * @return the data source
     */
    public String getDataSource() {
        return datasource;
    }

    private JdbcConfiguration() {
        try {
            InputStream in =
            	new FileInputStream("config/jdbc.properties");
            load(in);
            in.close();
            database = toDatabase(super.getProperty(DS_MAPPING_KEY));
            datasource = super.getProperty(DATASOURCE_KEY);
            url = super.getProperty(URL);
            username = super.getProperty(USERNAME);
            password = super.getProperty(PASSWORD);
            driver = super.getProperty(DRIVER);
            select = StringUtil.toStringArray(super.getProperty(SELECT));
            from = StringUtil.toStringArray(super.getProperty(FROM));
            leftJoin = StringUtil.toStringArray(super.getProperty(LEFTJOIN));
            relations = StringUtil.toStringArray(super.getProperty(RELATIONS));
            limitation = super.getProperty(LIMITATION);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load jdbc properties", e);
        }
    }

    
    
    private static int toDatabase(String mapping) {
        if (HSQL_VAL.equals(mapping)) {
            return HSQL;
        }
        if (PSQL_VAL.equals(mapping)) {
            return PSQL;
        }
        if (MYSQL_VAL.equals(mapping)) {
            return MYSQL;
        }
        if (DB2_VAL.equals(mapping)) {
            return DB2;
        }
        if (ORACLE_VAL.equals(mapping)) {
            return ORACLE;
        }
        throw new IllegalArgumentException(
            DS_MAPPING_KEY + "=" + mapping);
    }

	/**
	 * Gets the driver.
	 *
	 * @return the driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){
		
	}

	/**
	 * Gets the from.
	 *
	 * @return the from
	 */
	public String[] getFrom() {
		return from;
	}

	/**
	 * Gets the left join.
	 *
	 * @return the left join
	 */
	public String[] getLeftJoin() {
		return leftJoin;
	}

	/**
	 * Gets the relations.
	 *
	 * @return the relations
	 */
	public String[] getRelations() {
		return relations;
	}

	/**
	 * Gets the select.
	 *
	 * @return the select
	 */
	public String[] getSelect() {
		return select;
	}
}
