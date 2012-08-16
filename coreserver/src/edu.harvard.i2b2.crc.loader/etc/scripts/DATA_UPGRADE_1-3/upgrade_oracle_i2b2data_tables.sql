alter table encounter_mapping add (
patient_ide varchar2(100), 
patient_ide_source varchar2(50), 
encounter_ide_status char(1), 
upload_date date, 
download_date date, 
sourcesystem_cd varchar2(50)) 


alter table patient_mapping add ( 
upload_date date, 
download_date date, 
sourcesystem_cd varchar2(50)) 