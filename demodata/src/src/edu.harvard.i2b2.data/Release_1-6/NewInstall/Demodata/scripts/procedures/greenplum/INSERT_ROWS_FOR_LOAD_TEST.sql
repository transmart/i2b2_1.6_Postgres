-- Function: insert_rows_for_load_test(numeric)

-- DROP FUNCTION insert_rows_for_load_test(numeric);

CREATE OR REPLACE FUNCTION insert_rows_for_load_test(maxrowsinobservationfact numeric)
  RETURNS void AS
$BODY$

declare
maxPatients numeric;
maxEncounters numeric;
maxObservations numeric;
minPatients numeric;
minEncounters numeric;
begin


loop 
	select count(*) + 1 into maxObservations from observation_fact;
	raise notice 'maxObservations = % ', maxObservations; 
	exit when maxObservations > maxRowsInObservationFact; --3558848;  
	
	select max(patient_ide::numeric) - min(patient_ide::numeric) + 1 into maxPatients from patient_mapping;
	raise notice 'maxPatients = % ', maxPatients; 

	select max(encounter_ide::numeric) - min(encounter_ide::numeric) + 1 into maxEncounters from encounter_mapping; 
	raise notice 'maxEncounters = % ', maxEncounters; 
	
	-- patient mapping
	raise notice 'insert into patient_mapping';
	execute 'insert into patient_mapping(patient_ide, patient_ide_source, patient_num, patient_ide_status, upload_date, update_date, download_date, import_date, sourcesystem_cd, upload_id)
			 select patient_num + '|| maxPatients ||', patient_ide_source, patient_num + '|| maxPatients ||', patient_ide_status, upload_date, update_date, download_date, import_date, sourcesystem_cd, upload_id from patient_mapping' ;
	
	-- corresponding patient_dimension
	raise notice 'insert into patient_dimension';
	execute 'insert into patient_dimension (patient_num, vital_status_cd, birth_date, death_date, sex_cd, age_in_years_num, language_cd, race_cd, marital_status_cd, religion_cd, zip_cd, statecityzip_path, income_cd, patient_blob, update_date, download_date, import_date, sourcesystem_cd, upload_id) 
		     select patient_num + '|| maxPatients || ', vital_status_cd, birth_date, death_date, sex_cd, age_in_years_num, language_cd, race_cd, marital_status_cd, religion_cd, zip_cd, statecityzip_path, income_cd, patient_blob, update_date, download_date, import_date, sourcesystem_cd, upload_id from patient_dimension';
	
	-- encounter_mapping
	raise notice 'insert into encounter_mapping';
	execute 'insert into encounter_mapping (encounter_ide, encounter_ide_source, encounter_num, patient_ide, patient_ide_source, encounter_ide_status, upload_date, update_date, download_date, import_date, sourcesystem_cd, upload_id)
			 select encounter_ide::numeric + ' || maxEncounters ||', encounter_ide_source, encounter_num + '|| maxEncounters ||', patient_ide::numeric +'|| maxPatients || ', patient_ide_source, encounter_ide_status, upload_date, update_date, download_date, import_date, sourcesystem_cd, upload_id from encounter_mapping';
		
	-- corresponding visit_dimension
	raise notice 'insert into visit_dimension';
	execute 'insert into visit_dimension (encounter_num, patient_num, active_status_cd, start_date, end_date, inout_cd, location_cd, location_path, length_of_stay, visit_blob, update_date, download_date, import_date, sourcesystem_cd, upload_id)
			 select encounter_num + ' || maxEncounters ||', patient_num +'||  maxPatients || ', active_status_cd, start_date, end_date, inout_cd, location_cd, location_path, length_of_stay, visit_blob, update_date, download_date, import_date, sourcesystem_cd, upload_id from visit_dimension';

	raise notice 'insert into observation_fact';		 
	execute 'insert into observation_fact(encounter_num, patient_num, concept_cd, provider_id, start_date, modifier_cd, instance_num, valtype_cd, tval_char, nval_num, valueflag_cd, quantity_num, units_cd, end_date, location_cd, observation_blob, confidence_num, update_date, download_date, import_date, sourcesystem_cd, upload_id)
			 select encounter_num + '|| maxEncounters || ', patient_num + '|| maxPatients || ', concept_cd, provider_id, start_date, modifier_cd, instance_num, valtype_cd, tval_char, nval_num, valueflag_cd, quantity_num, units_cd, end_date, location_cd, observation_blob, confidence_num, update_date, download_date, import_date, sourcesystem_cd, upload_id from observation_fact';		 
end loop;

exception
	when others then
		raise exception '% ERROR - %', sqlstate, sqlerrm;

end;

$BODY$
  LANGUAGE plpgsql VOLATILE;
ALTER FUNCTION insert_rows_for_load_test(numeric) OWNER TO gpadmin;
