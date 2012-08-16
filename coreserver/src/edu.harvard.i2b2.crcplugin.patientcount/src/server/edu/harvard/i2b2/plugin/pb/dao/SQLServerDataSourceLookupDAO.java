package edu.harvard.i2b2.plugin.pb.dao;

import java.util.List;

import javax.sql.DataSource;

import edu.harvard.i2b2.plugin.pb.datavo.DataSourceLookup;

public class SQLServerDataSourceLookupDAO extends DataSourceLookupDAO {

	public SQLServerDataSourceLookupDAO(DataSource dataSource, String schemaName) {

	}

	@Override
	public List<edu.harvard.i2b2.plugin.pb.datavo.DataSourceLookup> getDbLookupByHiveOwner(
			String hiveId, String ownerId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DataSourceLookup> getDbLookupByHiveProjectOwner(String hiveId,
			String projectId, String ownerId) {
		// TODO Auto-generated method stub
		return null;
	}

}
