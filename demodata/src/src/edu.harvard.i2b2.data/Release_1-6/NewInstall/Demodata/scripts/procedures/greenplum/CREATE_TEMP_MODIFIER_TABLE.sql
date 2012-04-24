create or replace function CREATE_TEMP_MODIFIER_TABLE(IN tempModifierTableName VARCHAR, 
  OUT errorMsg VARCHAR) 
AS $$$ 

BEGIN 
execute  'create table ' ||  tempModifierTableName || ' (
        MODIFIER_CD VARCHAR(50) NOT NULL, 
	MODIFIER_PATH VARCHAR(900) NOT NULL , 
	NAME_CHAR VARCHAR(2000), 
	MODIFIER_BLOB TEXT, 
	UPDATE_DATE date, 
	DOWNLOAD_DATE DATE, 
	IMPORT_DATE DATE, 
	SOURCESYSTEM_CD VARCHAR(50)
	 )';

 execute  'CREATE INDEX idx_' || tempModifierTableName || '_pat_id ON ' || tempModifierTableName || '  (MODIFIER_PATH)';
  
   

EXCEPTION
	WHEN OTHERS THEN
		raise notice '% - %', SQLSTATE, SQLERRM;
END;
$$$ language plpgsql
