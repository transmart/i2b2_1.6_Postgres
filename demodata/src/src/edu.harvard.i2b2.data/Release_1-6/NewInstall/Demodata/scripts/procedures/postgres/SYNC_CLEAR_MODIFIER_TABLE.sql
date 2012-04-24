create or replace function  sync_clear_modifier_table (in tempModifierTableName VARCHAR, in backupModifierTableName VARCHAR, in uploadId NUMERIC, OUT errorMsg VARCHAR ) 
AS $$$ 

interModifierTableName  VARCHAR(400);

BEGIN 
	interModifierTableName := backupModifierTableName || '_inter';
	
	--Delete duplicate rows with same modifier_path and modifier cd
	-- smuniraju: rowid Not supported in postgres
	-- execute  'DELETE FROM ' || tempModifierTableName || ' t1 WHERE rowid > 
	-- 				   (SELECT  min(rowid) FROM ' || tempModifierTableName || ' t2
	-- 				     WHERE t1.modifier_cd = t2.modifier_cd 
    --                                        AND t1.modifier_path = t2.modifier_path
    --                                         )';
	execute  'DELETE FROM ' || tempModifierTableName || ' t1 
			  WHERE ( ctid) NOT IN (
					   (SELECT   max(rowid) FROM ' || tempModifierTableName || ' t2
					    GROUP BY  modifier_path,modifier_cd )';
									
    execute  'create table ' ||  interModifierTableName || ' (
        MODIFIER_CD          VARCHAR(50) NOT NULL,
	MODIFIER_PATH    	VARCHAR(700) NOT NULL,
	NAME_CHAR       	VARCHAR(2000) NULL,
	MODIFIER_BLOB        TEXT NULL,
	UPDATE_DATE         DATE NULL,
	DOWNLOAD_DATE       DATE NULL,
	IMPORT_DATE         DATE NULL,
	SOURCESYSTEM_CD     VARCHAR(50) NULL,
	UPLOAD_ID       	NUMERIC(38,0) NULL,
    CONSTRAINT '|| interModifierTableName ||'_pk  PRIMARY KEY(MODIFIER_PATH)
	 )';
    
    --Create new patient(patient_mapping) if temp table patient_ide does not exists 
	-- in patient_mapping table.
	execute  'insert into '|| interModifierTableName ||'  (modifier_cd,modifier_path,name_char,modifier_blob,update_date,download_date,import_date,sourcesystem_cd,upload_id)
			    select  modifier_cd, substr(modifier_path,1,700),
                        name_char,modifier_blob,
                        update_date,download_date,
                        current_timestamp,sourcesystem_cd,
                         ' || uploadId || '  from ' || tempModifierTableName || '  temp ';
	--backup the modifier_dimension table before creating a new one
	execute  'alter table modifier_dimension rename to ' || backupModifierTableName  ||'' ;
    
	-- add index on upload_id 
    execute  'CREATE INDEX ' || interModifierTableName || '_uid_idx ON ' || interModifierTableName || '(UPLOAD_ID)';

    -- add index on upload_id 
    execute  'CREATE INDEX ' || interModifierTableName || '_cd_idx ON ' || interModifierTableName || '(modifier_cd)';

    
       --backup the modifier_dimension table before creating a new one
	execute  'alter table ' || interModifierTableName  || ' rename to modifier_dimension' ;
 
EXCEPTION
	WHEN OTHERS THEN
		raise exception 'An error(-20001) was encountered - %-ERROR- %', SQLSTATE, SQLERRM;	
END;
$$$ language plpgsql
 
