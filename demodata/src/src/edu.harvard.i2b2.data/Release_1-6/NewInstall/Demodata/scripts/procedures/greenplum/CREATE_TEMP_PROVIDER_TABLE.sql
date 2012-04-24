create or replace FUNCTION CREATE_TEMP_PROVIDER_TABLE(IN tempProviderTableName VARCHAR, 
   OUT errorMsg VARCHAR) 
AS $$$ 

BEGIN 

execute 'create table ' ||  tempProviderTableName || ' (
    PROVIDER_ID VARCHAR(50) NOT NULL, 
	PROVIDER_PATH VARCHAR(700) NOT NULL, 
	NAME_CHAR VARCHAR(2000), 
	PROVIDER_BLOB TEXT, 
	UPDATE_DATE DATE, 
	DOWNLOAD_DATE DATE, 
	IMPORT_DATE DATE, 
	SOURCESYSTEM_CD VARCHAR(50), 
	UPLOAD_ID NUMERIC(*,0)
	 )';
 execute 'CREATE INDEX idx_' || tempProviderTableName || '_ppath_id ON ' || tempProviderTableName || '  (PROVIDER_PATH)';

    
EXCEPTION
	WHEN OTHERS THEN
		RAISE NOTICE '% - %', SQLSTATE, SQLERRM;
END;

$$$ language plpgsql;

