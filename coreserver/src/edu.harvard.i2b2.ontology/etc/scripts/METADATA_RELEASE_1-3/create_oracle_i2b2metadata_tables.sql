
--------------------------------------------------------
--  DDL for Table I2B2
--------------------------------------------------------

  CREATE TABLE I2B2METADATA.I2B2 
   (	"C_HLEVEL" NUMBER(22,0), 
	"C_FULLNAME" VARCHAR2(900), 
	"C_NAME" VARCHAR2(2000), 
	"C_SYNONYM_CD" CHAR(1), 
	"C_VISUALATTRIBUTES" CHAR(3), 
	"C_TOTALNUM" NUMBER(22,0), 
	"C_BASECODE" VARCHAR2(450), 
	"C_METADATAXML" CLOB, 
	"C_FACTTABLECOLUMN" VARCHAR2(50), 
	"C_TABLENAME" VARCHAR2(50), 
	"C_COLUMNNAME" VARCHAR2(50), 
	"C_COLUMNDATATYPE" VARCHAR2(50), 
	"C_OPERATOR" VARCHAR2(10), 
	"C_DIMCODE" VARCHAR2(900), 
	"C_COMMENT" CLOB, 
	"C_TOOLTIP" VARCHAR2(900), 
	"UPDATE_DATE" DATE, 
	"DOWNLOAD_DATE" DATE, 
	"IMPORT_DATE" DATE, 
	"SOURCESYSTEM_CD" VARCHAR2(50), 
	"VALUETYPE_CD" VARCHAR2(50)
   ) ;
 
 
--------------------------------------------------------
--  DDL for Table SCHEMES
--------------------------------------------------------

  CREATE TABLE I2B2METADATA.SCHEMES 
   (	"C_KEY" VARCHAR2(50), 
	"C_NAME" VARCHAR2(50), 
	"C_DESCRIPTION" VARCHAR2(100)
   ) ;
 
--------------------------------------------------------
--  DDL for Table TABLE_ACCESS
--------------------------------------------------------

  CREATE TABLE I2B2METADATA.TABLE_ACCESS
   (	"C_TABLE_CD" VARCHAR2(50), 
	"C_TABLE_NAME" VARCHAR2(50), 
	"C_PROTECTED_ACCESS" CHAR(1),
	"C_HLEVEL" NUMBER(22,0), 
	"C_FULLNAME" VARCHAR2(900), 
	"C_NAME" VARCHAR2(2000), 
	"C_SYNONYM_CD" CHAR(1), 
	"C_VISUALATTRIBUTES" CHAR(3), 
	"C_TOTALNUM" NUMBER(22,0), 
	"C_BASECODE" VARCHAR2(450), 
	"C_METADATAXML" CLOB, 
	"C_FACTTABLECOLUMN" VARCHAR2(50), 
	"C_DIMTABLENAME" VARCHAR2(50), 
	"C_COLUMNNAME" VARCHAR2(50), 
	"C_COLUMNDATATYPE" VARCHAR2(50), 
	"C_OPERATOR" VARCHAR2(10), 
	"C_DIMCODE" VARCHAR2(900), 
	"C_COMMENT" CLOB, 
	"C_TOOLTIP" VARCHAR2(900), 
	"C_ENTRY_DATE" DATE, 
	"C_CHANGE_DATE" DATE, 
	"C_STATUS_CD" CHAR(1)
   ) ;
 
 --------------------------------------------------------
--  DDL for Table ONT_DB_LOOKUP
--------------------------------------------------------
 
 CREATE TABLE I2B2METADATA.ONT_DB_LOOKUP ( 
	"C_DOMAIN_ID"   	VARCHAR2(255),
	"C_PROJECT_PATH" 	VARCHAR2(255), 
	"C_OWNER_ID"     	VARCHAR2(255), 
	"C_DB_FULLSCHEMA"   VARCHAR2(255), 
	"C_DB_DATASOURCE"	VARCHAR2(255), 
	"C_DB_SERVERTYPE"	VARCHAR2(255), 
	"C_DB_NICENAME"  	VARCHAR2(255),
	"C_DB_TOOLTIP"   	VARCHAR2(255), 
	"C_COMMENT"      	CLOB,
	"C_ENTRY_DATE"   	DATE,
	"C_CHANGE_DATE"  	DATE,
	"C_STATUS_CD"    	CHAR(1) 
	) ;
	
	

 