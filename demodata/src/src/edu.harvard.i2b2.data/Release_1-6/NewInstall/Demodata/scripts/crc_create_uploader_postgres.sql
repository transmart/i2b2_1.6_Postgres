/*==============================================================*/
/* Uploader service create script                               */
/*                                                              */
/* This script will create tables for the uploader service.     */
/* Run this script after the datamart create script             */
/*==============================================================*/

--=============================================================
-- Sequences for generating primary keys.					
--==============================================================
CREATE SEQUENCE sq_uploadstatus_uploadid
  INCREMENT BY 1
  MINVALUE 1
  MAXVALUE 9999999999999
  START WITH 1
  ;

CREATE SEQUENCE sq_up_encdim_encounternum
  INCREMENT BY 1
  START WITH 1
  MINVALUE 1
  MAXVALUE 9999999999999
  ;

CREATE SEQUENCE sq_up_patdim_patientnum
  INCREMENT BY 1
  START WITH 1
  MINVALUE 1
  MAXVALUE 9999999999999
  ;


/*============================================================================*/
/* Table: ARCHIVE_OBSERVATION_FACT (HOLDS DELETED ENTRIES OF OBSERVATION_FACT) */
/*============================================================================*/
select * into ARCHIVE_OBSERVATION_FACT from OBSERVATION_FACT where 1=2 
;

ALTER TABLE ARCHIVE_OBSERVATION_FACT  ADD ARCHIVE_UPLOAD_ID NUMERIC(22, 0)
;

CREATE INDEX PK_ARCHIVE_OBSFACT ON ARCHIVE_OBSERVATION_FACT
 		(ENCOUNTER_NUM , PATIENT_NUM , CONCEPT_CD , PROVIDER_ID , START_DATE , MODIFIER_CD , ARCHIVE_UPLOAD_ID) 
;


/*==============================================================*/
/* Table: DATAMART_REPORT			                    		*/
/*==============================================================*/
create table DATAMART_REPORT ( 
	TOTAL_PATIENT         NUMERIC(38,0), 
	TOTAL_OBSERVATIONFACT NUMERIC(38,0), 
	TOTAL_EVENT           NUMERIC(38,0),
	REPORT_DATE           TIMESTAMP WITHOUT TIME ZONE)
;




/*==============================================================*/
/* Table: UPLOAD_STATUS 					                    */
/*==============================================================*/
CREATE TABLE UPLOAD_STATUS (
	UPLOAD_ID 		    NUMERIC(38, 0) DEFAULT NEXTVAL('sq_uploadstatus_uploadid'), 	
    UPLOAD_LABEL 		VARCHAR(500) NOT NULL, 
    USER_ID      		VARCHAR(100) NOT NULL, 
    SOURCE_CD   		VARCHAR(50) NOT NULL,
    NO_OF_RECORD 		NUMERIC,
    LOADED_RECORD 		NUMERIC,
    DELETED_RECORD		NUMERIC, 
    LOAD_DATE    		TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	END_DATE 	        TIMESTAMP WITHOUT TIME ZONE , 
    LOAD_STATUS  		VARCHAR(100), 
    MESSAGE				TEXT,
    INPUT_FILE_NAME 	VARCHAR(500), 
    LOG_FILE_NAME 		VARCHAR(500), 
    TRANSFORM_NAME 		VARCHAR(500),
	 CONSTRAINT PK_UP_UPSTATUS_UPLOADID  PRIMARY KEY (UPLOAD_ID)
   
) 
;

/*==============================================================*/
/* Table: SET_TYPE						                        */
/*==============================================================*/
CREATE TABLE SET_TYPE (
	ID 				INT DEFAULT NEXTVAL('sq_up_patdim_patientnum'), 
    NAME			VARCHAR(500),
    CREATE_DATE     TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT PK_ST_ID PRIMARY KEY (ID)
) 
;



/*==============================================================*/
/* Table: SOURCE_MASTER					                        */
/*==============================================================*/
CREATE TABLE SOURCE_MASTER ( 
   SOURCE_CD 				VARCHAR(50) NOT NULL,
   DESCRIPTION  			VARCHAR(300),
   CREATE_DATE 				TIMESTAMP WITHOUT TIME ZONE,
   CONSTRAINT PK_SOURCEMASTER_SOURCECD  PRIMARY KEY (SOURCE_CD)
)
;


/*==============================================================*/
/* Table: SET_UPLOAD_STATUS				                        */
/*==============================================================*/
CREATE TABLE SET_UPLOAD_STATUS  (
    UPLOAD_ID			NUMERIC,
    SET_TYPE_ID         INT,
    SOURCE_CD  		    VARCHAR(50) NOT NULL,
    NO_OF_RECORD 		NUMERIC,
    LOADED_RECORD 		NUMERIC,
    DELETED_RECORD		NUMERIC, 
    LOAD_DATE    		TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    END_DATE            TIMESTAMP WITHOUT TIME ZONE,
    LOAD_STATUS  		VARCHAR(100), 
    MESSAGE			    TEXT,
    INPUT_FILE_NAME 	VARCHAR(500), 
    LOG_FILE_NAME 		VARCHAR(500), 
    TRANSFORM_NAME 		VARCHAR(500),
    CONSTRAINT PK_UP_UPSTATUS_IDSETTYPEID  PRIMARY KEY (UPLOAD_ID,SET_TYPE_ID),
    CONSTRAINT FK_UP_SET_TYPE_ID FOREIGN KEY (SET_TYPE_ID) REFERENCES SET_TYPE(ID)
) 
;

--==============================================================--
--  Adding seed data for SOURCE_MASTER table.  					--
--==============================================================--
INSERT INTO SOURCE_MASTER(source_cd,description,create_date) values ('I2B2PulmX','i2b2 Pulminory Extract',now());

/*==============================================================*/
/*  Adding seed data for SET_TYPE table.  					    */
/*==============================================================*/
INSERT INTO SET_TYPE(id,name,create_date) values (1,'event_set',now());
INSERT INTO SET_TYPE(id,name,create_date) values (2,'patient_set',now());
INSERT INTO SET_TYPE(id,name,create_date) values (3,'concept_set',now());
INSERT INTO SET_TYPE(id,name,create_date) values (4,'observer_set',now());
INSERT INTO SET_TYPE(id,name,create_date) values (5,'observation_set',now());
INSERT INTO SET_TYPE(id,name,create_date) values (6,'pid_set',now());
INSERT INTO SET_TYPE(id,name,create_date) values (7,'eid_set',now());
INSERT INTO SET_TYPE(ID,NAME,CREATE_DATE) values (8,'modifier_set',now());
 
