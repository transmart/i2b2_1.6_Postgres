-- c_db_fullschema set to ' ' instead of i2b2demodata.public because temporary tables cannot have schema prefixed. 

INSERT INTO CRC_DB_LOOKUP(c_domain_id, c_project_path, c_owner_id, c_db_fullschema, c_db_datasource, c_db_servertype, c_db_nicename, c_db_tooltip, c_comment, c_entry_date, c_change_date, c_status_cd)
  VALUES('i2b2demo', '/Demo/', '@', ' ', 'java:QueryToolDemoDS', 'POSTGRES', 'Demo', NULL, NULL, NULL, NULL, NULL);

INSERT INTO CRC_DB_LOOKUP(c_domain_id, c_project_path, c_owner_id, c_db_fullschema, c_db_datasource, c_db_servertype, c_db_nicename, c_db_tooltip, c_comment, c_entry_date, c_change_date, c_status_cd)
  VALUES('i2b2demo', '/Demo2/', '@', ' ', 'java:QueryToolDemo2DS', 'POSTGRES', 'Demo2', NULL, NULL, NULL, NULL, NULL);

