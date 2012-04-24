create or replace FUNCTION  insert_concept_fromtemp (IN tempConceptTableName VARCHAR, IN upload_id NUMERIC, OUT errorMsg VARCHAR ) 
AS $$$ 

BEGIN 
	--Delete duplicate rows with same encounter and patient combination
	-- smuniraju: Rowid not supported in postgres, using combination of ctid (and gp_segment_id for greenplum)
	-- execute 'DELETE FROM ' || tempConceptTableName || ' t1 WHERE rowid > 
	-- 				   (SELECT  min(rowid) FROM ' || tempConceptTableName || ' t2
	-- 				     WHERE t1.concept_cd = t2.concept_cd 
    --                                         AND t1.concept_path = t2.concept_path
    --                                         )';
	execute 'DELETE FROM ' || tempConceptTableName || ' t1 WHERE (gp_segment_id, ctid) not in ( 
			   SELECT  gp_segment_id, max(ctid) FROM ' || tempConceptTableName || ' t2
			   group by gp_segment_id,concept_path,concept_cd)';
						
    execute ' 	UPDATE concept_dimension cd set 
				name_char= temp.name_char,
				concept_blob= temp.concept_blob,
				update_date= temp.update_date,
				import_date = now(),
				DOWNLOAD_DATE=temp.DOWNLOAD_DATE,
				SOURCESYSTEM_CD=temp.SOURCESYSTEM_CD,
				UPLOAD_ID = '|| upload_id || '
				from ' || tempConceptTableName || ' temp
				where cd.concept_path = temp.concept_path
				and temp.update_date >= cd.update_date';
   
    --Create new patient(patient_mapping) if temp table patient_ide does not exists 
	-- in patient_mapping table.
	-- smuniraju: not exists results in co-related query.
	-- execute 'insert into concept_dimension  --(concept_cd,concept_path,name_char,concept_blob,update_date,download_date,import_date,sourcesystem_cd,upload_id)
	-- 		    select concept_cd, concept_path, name_char,concept_blob, update_date,download_date,
    --          sysdate,sourcesystem_cd, ' || upload_id || '  
	--			from ' || tempConceptTableName || '  temp
	-- 			where not exists (select concept_cd from concept_dimension cd where cd.concept_path = temp.concept_path)';
	execute 'insert into concept_dimension  (concept_cd,concept_path,name_char,concept_blob,update_date,download_date,import_date,sourcesystem_cd,upload_id)
			    select  temp.concept_cd, temp.concept_path, temp.name_char,temp.concept_blob, temp.update_date,temp.download_date, current_timestamp,temp.sourcesystem_cd, ' || upload_id || '  
				from ' || tempConceptTableName || '  temp left outer join concept_dimension cd
				on cd.concept_path = temp.concept_path
				where cd.concept_path is null';					
    
EXCEPTION
	WHEN OTHERS THEN
		RAISE NOTICE '% - %', SQLSTATE, SQLERRM;
END;

$$$ language plpgsql;
 
