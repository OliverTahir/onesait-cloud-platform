package com.minsait.onesait.platform.config.model;

/**
 * 
 * MySQL Type      Java Type
 * ----------      ---------
 * VARCHAR         java.lang.String
 * NUMERIC         java.math.BigDecimal
 * DECIMAL         java.math.BigDecimal
 * BIT             java.lang.Boolean
 * INTEGER         java.lang.Integer
 * BIGINT          java.lang.Long
 * FLOAT           java.lang.Double
 * DOUBLE          java.lang.Double
 * DATE            java.sql.Date
 * TIME            java.sql.Time
 * TIMESTAMP       java.sql.Tiimestamp
 * 
 * @author aponcep
 *
 */
public enum ReportParameterType {

	STRING("java.lang.String", "VARCHAR"),
	INTEGER("java.lang.Integer", "INTEGER"),
	FLOAT("java.lang.Double", "DOUBLE"),
	DATE("java.util.Date", "DATE")
	;
	
	private String javaType;
	private String dbType;
	
	private ReportParameterType(String javaType, String dbType) {
		this.javaType = javaType;
		this.dbType = dbType;
	}
	
	public String getDbType() {
		return dbType;
	}
	
	public String getJavaType() {
		return javaType;
	}
	
	public static ReportParameterType fromDatabaseType (String dbType) {
		
		ReportParameterType[] values = ReportParameterType.values();
		
		for (ReportParameterType value : values) {
			if (value.dbType.equals(dbType)) {
				return value;
			}
		}
		
		return null;
	}
	
	public static ReportParameterType fromJavaType (String javaType) {
		
		ReportParameterType[] values = ReportParameterType.values();
		
		for (ReportParameterType value : values) {
			if (value.javaType.equals(javaType)) {
				return value;
			}
		}
		
		return null;
	}
}
