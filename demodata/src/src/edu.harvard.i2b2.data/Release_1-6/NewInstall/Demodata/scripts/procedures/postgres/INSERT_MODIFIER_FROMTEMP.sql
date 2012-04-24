create or replace function  "insert_modifier_fromtemp" (IN tempModifierTableName VARCHAR, IN upload_id NUMERIC, OUT errorMsg VARCHAR ) 
AS $$$ 

BEGIN 
	--Delete duplicate rows 
	-- smuniraju rowid not implemented in postgres
	-- execute  'DELETE FROM ' || tempModifierTableName || ' t1 WHERE rowid > 
	-- 				(SELECT  min(rowid) FROM ' || tempModifierTableName || ' t2
	-- 				 WHERE t1.modifier_cd = t2.modifier_cd 
    --               AND t1.modifier_path = t2.modifier_path)';
	execute  'DELETE FROM ' || tempModifierTableName || ' t1 WHERE ( ctid) NOT IN  
			   (SELECT   max(ctid) FROM ' || tempModifierTableName || ' t2
				GROUP BY  modifier_path,modifier_cd)';
						 
    execute 'UPDATE modifier_dimension cd set 
			name_char= temp.name_char,
			modifier_blob= temp.modifier_blob,
			update_date= temp.update_date,
			import_date = now(),
			DOWNLOAD_DATE=temp.DOWNLOAD_DATE,
			SOURCESYSTEM_CD=temp.SOURCESYSTEM_CD,
			UPLOAD_ID = '|| upload_id || '
			from ' || tempModifierTableName || ' temp
			where cd.modifier_path = temp.modifier_path
			and temp.update_date >= cd.update_date';

    --Create new modifier if temp table modifier_path does not exists 
	-- in modifier dimension table.
	-- smuniraju: not exists => co-related query
	-- execute  'insert into modifier_dimension  (modifier_cd,modifier_path,name_char,modifier_blob,update_date,download_date,import_date,sourcesystem_cd,upload_id)
	-- 		    select  modifier_cd, modifier_path,
    --                     name_char,modifier_blob,
    --                     update_date,download_date,
    --                     sysdate,sourcesystem_cd,
    --                      ' || upload_id || '  from ' || tempModifierTableName || '  temp
	-- 				where not exists (select modifier_cd from modifier_dimension cd where cd.modifier_path = temp.modifier_path)					 
	-- 	';
	
	execute  'insert into modifier_dimension  (modifier_cd,modifier_path,name_char,modifier_blob,update_date,download_date,import_date,sourcesystem_cd,upload_id)
			    select  modifier_cd, modifier_path, name_char,modifier_blob, update_date,download_date, current_timestamp, sourcesystem_cd,' || upload_id || '  
					from ' || tempModifierTableName || '  temp left outer join modifier_dimension cd
					on cd.modifier_path = temp.modifier_path 
					where cd.modifier_path is null)';
	 
EXCEPTION
	WHEN OTHERS THEN
		raise exception 'An error(-20001) was encountered - %-ERROR- %', SQLSTATE, SQLERRM;	
END;
$$$ language plpgsql
 
