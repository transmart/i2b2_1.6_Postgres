CREATE OR REPLACE  function remove_temp_table(tempTableName VARCHAR) 
RETURNS VOID AS $$$
BEGIN 
	execute 'drop table ' || tempTableName || ' cascade';
	
EXCEPTION
	WHEN OTHERS THEN
		RAISE NOTICE '% - %', SQLSTATE, SQLERRM;
END;
$$$ language plpgsql;



