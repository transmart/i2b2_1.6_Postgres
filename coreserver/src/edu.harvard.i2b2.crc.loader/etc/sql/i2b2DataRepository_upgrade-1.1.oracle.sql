/*==============================================================*/
/* Database Upgrade Script                                      */
/*                                                              */
/* This script will perform the necessary database upgrades     */
/* i2b2 data repository database from 1.0 to	1.1. 		  	*/
/*==============================================================*/


/*============================================================================*/
/* Table: ARCHIVE_OBSERVATION_FACT (HOLDS DELETE ENTRIES OF OBSERVATION_FACT) */
/*============================================================================*/
CREATE TABLE  archive_observation_fact nologging
as (
select * from observation_fact
	where 1= 2) 
/

alter table archive_observation_fact  add ( archive_upload_id integer)
/

create  index pk_archive_obsfact on archive_observation_fact
 		(encounter_num,patient_num,concept_cd,provider_id,start_date,modifier_cd,archive_upload_id)
NOLOGGING
/

/*
CREATE TABLE archive_observation_fact (
		encounter_num    NUMBER(22,0),
		concept_cd 	     VARCHAR(50), 
		patient_num      NUMBER(22,0),
		provider_id      VARCHAR(20),
 		start_date       DATE, 
		modifier_cd      VARCHAR2(100),
 		valtype_cd       varchar2(3),
		tval_char        varchar(50),
 		nval_num         NUMBER(18,5),
		valueflag_cd     CHAR(1),
 		quantity_num     NUMBER(18,5),
		confidence_num   NUMBER(18,0),
 		observation_blob CLOB,
		units_cd 		 VARCHAR2(100),
 		end_date    	 DATE,
		location_cd 	 VARCHAR2(100),
 		update_date  	 DATE,
		download_date 	 DATE,
 		import_date 	 DATE,
		sourcesystem_cd  VARCHAR2(50),
 		upload_id 		 INTEGER,
 		archive_upload_id INTEGER
 		CONSTRAINT pk_archive_obsfact  PRIMARY KEY 
 		(encounter_num,patient_num,concept_cd,provider_id,start_date,modifier_cd,archive_upload_id)
);
*/

/*==============================================================*/
/* Index: IDX_ARCHOBSFACT_ARCHUPLOADID                           */
/*==============================================================*/
/*
CREATE INDEX idx_archobsfact_archuploadid ON archive_observation_fact (
   archive_upload_id ASC
)
/
*/


/*==============================================================*/
/* Table: MISSING_DIMENSION_REPORT			                    */
/*==============================================================*/
CREATE TABLE missing_dimension_report (
		dimension_value 	VARCHAR2(100),
		total_count			NUMBER(22,0),
		dimension			CHAR(1), 
		upload_id			NUMBER(22,0)
)
NOCACHE
nologging
/

/*==============================================================*/
/* Table: DATAMART_REPORT			                    		*/
/*==============================================================*/
create table datamart_report ( 
	total_patient number(15,0), 
	total_observationfact number(15,0), 
	total_visit number(15,0),
	report_date timestamp)
NOCACHE
nologging
/

/*==============================================================*/
/* Table: USER_PROFILE				                    		*/
/*==============================================================*/
create table user_profile (
user_id varchar2(50) not null,
password varchar2(100) not null,
fist_name varchar2(100), 
last_name varchar2(100), 
role_id	int,
 constraint pk_user_profile primary key(user_id)
) 
nologging

/*==============================================================*/
/* Table: UPLOAD_STATUS 					                    */
/*==============================================================*/
CREATE TABLE upload_status  (
	upload_id			INTEGER, 	
    upload_label 		VARCHAR(500) NOT NULL, 
    user_id      		VARCHAR(100) NOT NULL, 
    source_cd   		VARCHAR(500) NOT NULL,
    no_of_record 		NUMBER,
    loaded_record 		NUMBER,
    deleted_record		NUMBER, 
    load_date    		DATE			  NOT NULL,
	end_date 	        DATE , 
    load_status  		VARCHAR(100), 
    message				VARCHAR(4000),
    input_file_name 	VARCHAR(500), 
    log_file_name 		VARCHAR(500), 
    transform_name 		VARCHAR(500),
    CONSTRAINT pk_up_upstatus_uploadid  PRIMARY KEY (upload_id)
) NOLOGGING
/

CREATE TABLE set_type (
	id 				INTEGER, 
    name			VARCHAR(500),
    create_date     DATE,
    CONSTRAINT pk_st_id PRIMARY KEY (id)
) NOLOGGING
/


/*==============================================================*/
/*  Adding seed data for SOURCE_MASTER table.  									*/
/*==============================================================*/
INSERT INTO set_type(id,name,create_date) values (1,'event_set',sysdate)
/
INSERT INTO set_type(id,name,create_date) values (2,'patient_set',sysdate)
/
INSERT INTO set_type(id,name,create_date) values (3,'concept_set',sysdate)
/
INSERT INTO set_type(id,name,create_date) values (4,'observer_set',sysdate)
/
INSERT INTO set_type(id,name,create_date) values (5,'observation_set',sysdate)
/
INSERT INTO set_type(id,name,create_date) values (6,'pid_set',sysdate)
/
INSERT INTO set_type(id,name,create_date) values (7,'eid_set',sysdate)
/
/*==============================================================*/
/* Table: SOURCE_MASTER					                                */
/*==============================================================*/
CREATE TABLE source_master ( 
   source_cd 				VARCHAR(50) NOT NULL,
   description 			VARCHAR(300),
   create_date 						DATE,
   CONSTRAINT pk_up_sourcemaster_sourcecd  PRIMARY KEY (source_cd)
)
/

CREATE TABLE set_upload_status  (
    upload_id			INTEGER,
    set_type_id                 INTEGER,
    source_cd   		VARCHAR(500) NOT NULL,
    no_of_record 		NUMBER,
    loaded_record 		NUMBER,
    deleted_record		NUMBER, 
    load_date    		DATE			  NOT NULL,
    end_date            DATE ,
    load_status  		VARCHAR(100), 
    message			VARCHAR(4000),
    input_file_name 	        VARCHAR(500), 
    log_file_name 		VARCHAR(500), 
    transform_name 		VARCHAR(500),
    CONSTRAINT pk_up_upstatus_idsetid  PRIMARY KEY (upload_id,set_type_id),
    CONSTRAINT fk_up_set_type_id FOREIGN KEY (set_type_id) REFERENCES set_type(id)
) NOLOGGING
/

/*==============================================================*/
/* Add upload status column to observation fact table.			    */
/*==============================================================*/
/*
ALTER TABLE observation_fact 
   ADD (upload_id INTEGER, 
   		modifier_cd varchar2(100))
/ 
ALTER TABLE observation_fact  ADD 
   CONSTRAINT fk_up_obsfact_uploadid FOREIGN KEY(upload_id) REFERENCES upload_status(upload_id)
/
*/

ALTER TABLE visit_dimension 
   ADD (upload_id INTEGER)
/

ALTER TABLE observation_fact 
   ADD (upload_id INTEGER)
/

ALTER TABLE encounter_mapping 
   ADD (upload_id INTEGER)
/

ALTER TABLE patient_dimension 
   ADD (upload_id INTEGER)
/

ALTER TABLE patient_mapping
   ADD (upload_id INTEGER)
/

ALTER TABLE concept_dimension 
   ADD (upload_id INTEGER)
/


ALTER TABLE provider_dimension 
   ADD (upload_id INTEGER)
/

/*==============================================================*/
/*  Add foreign key with source_master table.									  */ 
/*==============================================================*/
/*
ALTER TABLE patient_dimesion
ADD CONSTRAINT fk_up_patdim_sourcesystemcd FOREIGN KEY (
  sourcesystem_cd)
REFERENCES source_master (source_cd)
/

ALTER TABLE visit_dimesion
ADD CONSTRAINT fk_up_visitdim_sourcesystemcd FOREIGN KEY (
  sourcesystem_cd)
REFERENCES source_master (source_cd)
/

ALTER TABLE provider_dimension
ADD CONSTRAINT fk_up_provdim_sourcesystemcd FOREIGN KEY (
  sourcesystem_cd)
REFERENCES source_master (source_cd)
/

ALTER TABLE observation_fact
ADD CONSTRAINT fk_up_obsfact_sourcesystemcd FOREIGN KEY (
  sourcesystem_cd)
REFERENCES source_master (source_cd)
/
*/


/*==============================================================*/
/* create sequence for generating primary keys.								  */
/*==============================================================*/
CREATE SEQUENCE sq_up_encdim_encounternum
  INCREMENT BY 1
  START WITH 2000050063
  MINVALUE 1
  MAXVALUE 9999999999999
  NOCYCLE
  ORDER
  NOCACHE
/

CREATE SEQUENCE sq_up_patdim_patientnum
  INCREMENT BY 1
  START WITH 100000
  MINVALUE 1
  MAXVALUE 9999999999999
  NOCYCLE
  ORDER
  NOCACHE
/

CREATE SEQUENCE sq_uploadstatus_uploadid
  INCREMENT BY 1
  START WITH 1
  MINVALUE 1
  MAXVALUE 9999999999999
  NOCYCLE
  ORDER
  NOCACHE
/

/*==============================================================*/
/* Index: IDX_UP_UPLOADSTAT_UPLABEL                             */
/*==============================================================*/
/*
CREATE INDEX idx_up_uploadstat_uplabel ON upload_status (
   upload_label ASC
)
/

/*==============================================================*/
/* Index: IDX_VISITDIM_ENCNUM		                            */
/*==============================================================*/
CREATE INDEX idx_visitdim_encnum ON visit_dimension (
   encounter_num 
)

/*==============================================================*/
/* Index: IDX_VISITDIM_PATNUM		                            */
/*==============================================================*/
CREATE INDEX idx_visitdim_patnum ON visit_dimension (
   patient_num 
)
*/

/*==============================================================*/
/* Index: IDX_OBSFACT_UPLOADID		                            */
/*==============================================================*/
CREATE INDEX idx_obsfact_uploadid ON observation_fact (
   upload_id 
)

CREATE INDEX idx_pd_uploadid ON patient_dimension (
   upload_id 
)

CREATE INDEX idx_pm_uploadid ON patient_mapping (
   upload_id 
)

CREATE INDEX idx_cd_uploadid ON concept_dimension (
   upload_id 
)

CREATE INDEX idx_od_uploadid ON provider_dimension (
   upload_id 
)

CREATE INDEX idx_vd_uploadid ON visit_dimension (
   upload_id 
)


CREATE INDEX idx_em_uploadid ON encounter_mapping (
   upload_id 
)

/*==============================================================*/
/*  Adding seed data for SOURCE_MASTER table.  									*/
/*==============================================================*/
INSERT INTO source_master(source_cd,description,create_date) values ('SMOKING','SMOKING',sysdate)
/
COMMIT
/
INSERT INTO source_master(source_cd,description,create_date) values ('I2B2PulmX','i2b2 Pulminory Extract',sysdate)
/
COMMIT
/
INSERT INTO source_master(source_cd,description,create_date) values ('RPDRPulmon','RPDR Pulimonry Monitor',sysdate)
/
COMMIT
/
INSERT INTO source_master(source_cd,description,create_date) values ('BWHDIAGS','BWH IAGS',sysdate)
/
COMMIT
/
INSERT INTO source_master(source_cd,description,create_date) values ('MGHDIAGS','MGH Diagnosis',sysdate)
/
COMMIT
/
INSERT INTO source_master(source_cd,description,create_date) values ('RPDRASTHMA','RPDR Asthma',sysdate)
/
COMMIT
/
INSERT INTO source_master(source_cd,description,create_date) values ('ASTHMADICT','Asthma Dict',sysdate)
/
COMMIT
/
 
 
