alter table encounter_mapping add 
patient_ide varchar(100), 
patient_ide_source varchar(50), 
encounter_ide_status char(1), 
upload_date datetime, 
download_date datetime, 
sourcesystem_cd varchar(50) ; 


alter table patient_mapping add  
upload_date datetime, 
download_date datetime, 
sourcesystem_cd varchar(50); 