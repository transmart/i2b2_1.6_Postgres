/*==============================================================*/
/* Database Upgrade Script                                      */
/*                                                              */
/* This script will perform the necessary database upgrades     */
/* i2b2 data repository database from 1.0 to	1.1. 		  	*/
/*==============================================================*/


/*============================================================================*/
/* Table: ARCHIVE_OBSERVATION_FACT (HOLDS DELETE ENTRIES OF OBSERVATION_FACT) */
/*============================================================================*/

SELECT TOP 0 * INTO archive_observation_fact FROM observation_fact;

alter table archive_observation_fact  add archive_upload_id int;

create  index pk_archive_obsfact on archive_observation_fact
 		(encounter_num,patient_num,concept_cd,provider_id,start_date,modifier_cd,archive_upload_id);


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
		dimension_value 	VARCHAR(100),
		total_count			bigint,
		dimension			CHAR(1), 
		upload_id			bigint
);


/*==============================================================*/
/* Table: DATAMART_REPORT			                    		*/
/*==============================================================*/
create table datamart_report ( 
	total_patient number(15,0), 
	total_observationfact number(15,0), 
	total_visit number(15,0),
	report_date timestamp)
/

/*==============================================================*/
/* Table: USER_PROFILE				                    		*/
/*==============================================================*/
create table user_profile (
user_id varchar(50) not null,
password varchar(100) not null,
fist_name varchar(100), 
last_name varchar(100), 
role_id	int,
 constraint pk_user_profile primary key(user_id)
) ;


/*==============================================================*/
/* Table: UPLOAD_STATUS 					                    */
/*==============================================================*/
CREATE TABLE upload_status  (
	upload_id			INT indentity(1,1), 	
    upload_label 		VARCHAR(500) NOT NULL, 
    user_id      		VARCHAR(100) NOT NULL, 
    source_cd   		VARCHAR(500) NOT NULL,
    no_of_record 		int,
    loaded_record 		int,
    deleted_record		int, 
    load_date    		DATETIME			  NOT NULL,
	end_date 	        DATETIME , 
    load_status  		VARCHAR(100), 
    message				VARCHAR(3500),
    input_file_name 	VARCHAR(500), 
    log_file_name 		VARCHAR(500), 
    transform_name 		VARCHAR(500),
    CONSTRAINT pk_up_upstatus_uploadid  PRIMARY KEY (upload_id)
) ;


CREATE TABLE set_type (
	id 				INTEGER, 
    name			VARCHAR(500),
    create_date     DATETIME,
    CONSTRAINT pk_st_id PRIMARY KEY (id)
) ;



/*==============================================================*/
/*  Adding seed data for SOURCE_MASTER table.  									*/
/*==============================================================*/
INSERT INTO set_type(id,name,create_date) values (1,'event_set',getdate());
INSERT INTO set_type(id,name,create_date) values (2,'patient_set',getdate());
INSERT INTO set_type(id,name,create_date) values (3,'concept_set',getdate());
INSERT INTO set_type(id,name,create_date) values (4,'observer_set',getdate());
INSERT INTO set_type(id,name,create_date) values (5,'observation_set',getdate());
INSERT INTO set_type(id,name,create_date) values (6,'pid_set',getdate());
INSERT INTO set_type(id,name,create_date) values (7,'eid_set',getdate());

/*==============================================================*/
/* Table: SOURCE_MASTER					                                */
/*==============================================================*/
CREATE TABLE source_master ( 
   source_cd 				VARCHAR(50) NOT NULL,
   description 			VARCHAR(300),
   create_date 						DATETIME,
   CONSTRAINT pk_up_sourcemaster_sourcecd  PRIMARY KEY (source_cd)
);


CREATE TABLE set_upload_status  (
    upload_id			int,
    set_type_id         int,
    source_cd   		VARCHAR(500) NOT NULL,
    no_of_record 		int,
    loaded_record 		int,
    deleted_record		int, 
    load_date    		DATETIME			  NOT NULL,
    end_date            DATETIME ,
    load_status  		VARCHAR(100), 
    message			    VARCHAR(3500),
    input_file_name 	VARCHAR(500), 
    log_file_name 		VARCHAR(500), 
    transform_name 		VARCHAR(500),
    CONSTRAINT pk_up_upstatus_idsetid  PRIMARY KEY (upload_id,set_type_id),
    CONSTRAINT fk_up_set_type_id FOREIGN KEY (set_type_id) REFERENCES set_type(id)
) ;

/*==============================================================*/
/* Add upload status column to observation fact table.			    */
/*==============================================================*/
/*
ALTER TABLE observation_fact 
   ADD upload_id INTEGER;
   		

ALTER TABLE observation_fact  ADD 
   CONSTRAINT fk_up_obsfact_uploadid FOREIGN KEY(upload_id) REFERENCES upload_status(upload_id)
/
*/

ALTER TABLE visit_dimension 
   ADD upload_id INT;


ALTER TABLE observation_fact 
   ADD upload_id INT;


ALTER TABLE encounter_mapping 
   ADD upload_id INT;


ALTER TABLE patient_dimension 
   ADD upload_id INT;


ALTER TABLE patient_mapping
   ADD upload_id INT;


ALTER TABLE concept_dimension 
   ADD upload_id INT;



ALTER TABLE provider_dimension 
   ADD upload_id INT;


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
INSERT INTO source_master(source_cd,description,create_date) values ('I2B2PulmX','i2b2,'Pulminory Extract',@getdate());

 
