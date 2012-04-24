create or replace function create_temp_concept_table(IN tempConceptTableName VARCHAR, 
  OUT errorMsg VARCHAR) 
AS $$$

BEGIN 
execute 'create table ' ||  tempConceptTableName || ' (
        CONCEPT_CD VARCHAR(50) NOT NULL, 
	CONCEPT_PATH VARCHAR(900) NOT NULL , 
	NAME_CHAR VARCHAR(2000), 
	CONCEPT_BLOB TEXT, 
	UPDATE_DATE date, 
	DOWNLOAD_DATE DATE, 
	IMPORT_DATE DATE, 
	SOURCESYSTEM_CD VARCHAR(50)
	 )';

 execute 'CREATE INDEX idx_' || tempConceptTableName || '_pat_id ON ' || tempConceptTableName || '  (CONCEPT_PATH)';
  
   

EXCEPTION
	WHEN OTHERS THEN
		RAISE NOTICE '% - %', SQLSTATE, SQLERRM;
END;

$$$ language plpgsql;
