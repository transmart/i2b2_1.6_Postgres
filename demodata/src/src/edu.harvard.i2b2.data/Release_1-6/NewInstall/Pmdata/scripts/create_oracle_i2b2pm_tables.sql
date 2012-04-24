
CREATE TABLE PM_CELL_DATA ( 
	CELL_ID     	VARCHAR2(50) NOT NULL,
	PROJECT_PATH	VARCHAR2(255) NOT NULL,
	NAME        	VARCHAR2(255) NULL,
	METHOD_CD      	VARCHAR2(255) NULL,
	URL         	VARCHAR2(255) NULL,
	CAN_OVERRIDE	NUMBER(1,0) NULL,
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)
	);
CREATE TABLE PM_CELL_PARAMS ( 
	ID				NUMBER PRIMARY KEY,
	DATATYPE_CD  	VARCHAR2(50) NULL,
	CELL_ID     	VARCHAR2(50) NOT NULL,
	PROJECT_PATH    VARCHAR2(255) NOT NULL,
	PARAM_NAME_CD  	VARCHAR2(50) NOT NULL,
	VALUE       	VARCHAR2(255) NULL,
	CAN_OVERRIDE	NUMBER(1,0) NULL,
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)
	);
CREATE TABLE PM_GLOBAL_PARAMS ( 
	ID				NUMBER PRIMARY KEY,
	DATATYPE_CD  	VARCHAR2(50) NULL,
	PARAM_NAME_CD  	VARCHAR2(50) NOT NULL,
	PROJECT_PATH	VARCHAR2(255) NOT NULL,
	VALUE       	VARCHAR2(255) NULL,
	CAN_OVERRIDE	NUMBER(1,0) NULL,
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)
	);
	
CREATE TABLE PM_HIVE_DATA ( 
	DOMAIN_ID  	VARCHAR2(50) NOT NULL,
	HELPURL    	VARCHAR2(255) NULL,
	DOMAIN_NAME	VARCHAR2(255) NULL,
	ENVIRONMENT_CD	VARCHAR2(255) NULL,
	ACTIVE     	NUMBER(1,0) NULL ,
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)
	);
CREATE TABLE PM_HIVE_PARAMS ( 
	ID				NUMBER PRIMARY KEY,
	DATATYPE_CD  	VARCHAR2(50) NULL,
	DOMAIN_ID 		VARCHAR2(50) NOT NULL,
	PARAM_NAME_CD	VARCHAR2(50) NOT NULL,
	VALUE       	VARCHAR2(255) NULL,
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)
	);
CREATE TABLE PM_PROJECT_DATA ( 
	PROJECT_ID  	VARCHAR2(50) NOT NULL,
	PROJECT_NAME	VARCHAR2(255) NULL,
	PROJECT_WIKI	VARCHAR2(255) NULL,
	PROJECT_KEY 	VARCHAR2(255) NULL,
	PROJECT_PATH	VARCHAR2(255) NULL,
	PROJECT_DESCRIPTION	VARCHAR2(2000) NULL,
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)
	);
CREATE TABLE PM_PROJECT_PARAMS ( 
	ID				NUMBER PRIMARY KEY,
	DATATYPE_CD  	VARCHAR2(50) NULL,
	PROJECT_ID		VARCHAR2(50) NOT NULL,
	PARAM_NAME_CD	VARCHAR2(50) NOT NULL,
	VALUE       	VARCHAR2(255) NULL,
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)
	);
CREATE TABLE PM_PROJECT_USER_PARAMS ( 
	ID				NUMBER PRIMARY KEY,
	DATATYPE_CD  	VARCHAR2(50) NULL,
	PROJECT_ID	VARCHAR2(50) NOT NULL,
	USER_ID   	VARCHAR2(50) NOT NULL,
	PARAM_NAME_CD	VARCHAR2(50) NOT NULL,
	VALUE       	VARCHAR2(255) NULL,
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)
	);
CREATE TABLE PM_PROJECT_USER_ROLES ( 
	PROJECT_ID	VARCHAR2(50) NOT NULL,
	USER_ID   	VARCHAR2(50) NOT NULL,
	USER_ROLE_CD 	VARCHAR2(255) NOT NULL,
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)
	);
CREATE TABLE PM_USER_DATA ( 
	USER_ID  	VARCHAR2(50) NOT NULL,
	FULL_NAME	VARCHAR2(255) NULL,
	PASSWORD 	VARCHAR2(255) NULL,
	EMAIL	 	VARCHAR2(255) NULL,
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)
	);
CREATE TABLE PM_USER_PARAMS ( 
	ID				NUMBER PRIMARY KEY,
	DATATYPE_CD  	VARCHAR2(50) NULL,
	USER_ID   	VARCHAR2(50) NOT NULL,
	PARAM_NAME_CD	VARCHAR2(50) NOT NULL,
	VALUE       	VARCHAR2(255) NULL,
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)
	);
CREATE TABLE PM_ROLE_REQUIREMENT ( 
	TABLE_CD   	VARCHAR2(50) NOT NULL,
	COLUMN_CD	VARCHAR2(50) NOT NULL,
	READ_HIVEMGMT_CD     	VARCHAR2(50) NOT NULL,
	WRITE_HIVEMGMT_CD     	VARCHAR2(50) NOT NULL,
	NAME_CHAR     	VARCHAR2(2000),
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)
	);
CREATE TABLE PM_USER_SESSION ( 
	USER_ID 	VARCHAR2(50) NOT NULL,
	SESSION_ID	VARCHAR2(50) NOT NULL,
    EXPIRED_DATE         DATE ,
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)   
    );
    
CREATE TABLE PM_PROJECT_REQUEST  ( 
	ID           	NUMBER PRIMARY KEY,
	TITLE			VARCHAR2(255) NULL,
	REQUEST_XML  	CLOB NOT NULL,
	CHANGE_DATE  	DATE NULL,
	ENTRY_DATE   	DATE NULL,
	CHANGEBY_CHAR	VARCHAR2(50) NULL,
	STATUS_CD    	VARCHAR2(50) NULL,
	PROJECT_ID   	VARCHAR2(50) NULL,
	SUBMIT_CHAR  	VARCHAR2(50) NULL
);


CREATE TABLE PM_APPROVALS ( 
	APPROVAL_ID     	VARCHAR2(50) NOT NULL,
	APPROVAL_NAME	VARCHAR2(255)  NULL,
	APPROVAL_DESCRIPTION        	VARCHAR2(2000) NULL,
	APPROVAL_ACTIVATION_DATE      	DATE NULL,
	APPROVAL_EXPIRATION_DATE         	DATE NULL,
	OBJECT_CD		VARCHAR2(50),
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)
	);
CREATE TABLE PM_APPROVALS_PARAMS ( 
	ID				NUMBER PRIMARY KEY,
	APPROVAL_ID     	VARCHAR2(50) NOT NULL,
	PARAM_NAME_CD  	VARCHAR2(50) NOT NULL,
	VALUE       	VARCHAR2(2000) NULL,
	ACTIVATION_DATE      	DATE NULL,
	EXPIRATION_DATE         	DATE NULL,
	DATATYPE_CD  	VARCHAR2(50) NULL,
	OBJECT_CD		VARCHAR2(50),
    CHANGE_DATE     DATE ,
    ENTRY_DATE      DATE ,
	CHANGEBY_CHAR   VARCHAR2(50),
    STATUS_CD       VARCHAR2(50)
	);
    
    
    CREATE SEQUENCE PM_PARAMS
    START WITH 1
    INCREMENT BY 1;
    
ALTER TABLE PM_USER_SESSION
	ADD ( PRIMARY KEY (SESSION_ID, USER_ID)
	NOT DEFERRABLE INITIALLY IMMEDIATE );	
	
ALTER TABLE PM_CELL_DATA
	ADD ( PRIMARY KEY (CELL_ID, PROJECT_PATH)
	NOT DEFERRABLE INITIALLY IMMEDIATE );

ALTER TABLE PM_HIVE_DATA
	ADD ( PRIMARY KEY (DOMAIN_ID)
	NOT DEFERRABLE INITIALLY IMMEDIATE );

ALTER TABLE PM_PROJECT_DATA
	ADD ( PRIMARY KEY (PROJECT_ID)
	NOT DEFERRABLE INITIALLY IMMEDIATE );

ALTER TABLE PM_PROJECT_USER_ROLES
	ADD ( PRIMARY KEY (PROJECT_ID, USER_ID, USER_ROLE_CD)
	NOT DEFERRABLE INITIALLY IMMEDIATE );
ALTER TABLE PM_ROLE_REQUIREMENT
	ADD ( PRIMARY KEY (TABLE_CD, COLUMN_CD, READ_HIVEMGMT_CD, WRITE_HIVEMGMT_CD)
	NOT DEFERRABLE INITIALLY IMMEDIATE );
ALTER TABLE PM_USER_DATA
	ADD ( PRIMARY KEY (USER_ID)
	NOT DEFERRABLE INITIALLY IMMEDIATE );
	
	