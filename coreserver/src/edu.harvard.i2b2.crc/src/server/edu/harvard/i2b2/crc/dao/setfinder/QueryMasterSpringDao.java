/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;

/**
 * Class to manager persistance operation of QtQueryMaster $Id:
 * QueryMasterSpringDao.java,v 1.3 2008/04/08 19:36:52 rk903 Exp $
 * 
 * @author rkuttan
 * @see QtQueryMaster
 */
public class QueryMasterSpringDao extends CRCDAO implements IQueryMasterDao {

	JdbcTemplate jdbcTemplate = null;
	SaveQueryMaster saveQueryMaster = null;
	QtQueryMasterRowMapper queryMasterMapper = new QtQueryMasterRowMapper();

	public final String DELETE_YES_FLAG = "Y";
	public final String DELETE_NO_FLAG = "N";

	private DataSourceLookup dataSourceLookup = null;

	public QueryMasterSpringDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;

	}

	/**
	 * Function to create query master By default sets delete flag to false
	 * 
	 * @param queryMaster
	 * @return query master id
	 */
	public String createQueryMaster(QtQueryMaster queryMaster,
			String i2b2RequestXml) {
		queryMaster.setDeleteFlag(DELETE_NO_FLAG);
		saveQueryMaster = new SaveQueryMaster(getDataSource(),
				getDbSchemaName(), dataSourceLookup);
		saveQueryMaster.save(queryMaster, i2b2RequestXml);
		return queryMaster.getQueryMasterId();
	}

	/**
	 * Write query sql for the master id
	 * 
	 * @param masterId
	 * @param generatedSql
	 */
	public void updateQuerySQL(String masterId, String generatedSql) {
		String sql = "UPDATE "
				+ getDbSchemaName()
				+ "QT_QUERY_MASTER set  GENERATED_SQL = ? where query_master_id = ?";
		jdbcTemplate.update(sql, new Object[] { generatedSql, masterId });
		// jdbcTemplate.update(sql);
	}

	/**
	 * Returns list of query master by user id
	 * 
	 * @param userId
	 * @return List<QtQueryMaster>
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryMaster> getQueryMasterByUserId(String userId,
			int fetchSize) {

		String sql = "select ";

		if (fetchSize > 0
				&& dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.SQLSERVER)) {
			sql += " top " + fetchSize;
		}
		sql += " query_master_id,name,user_id,group_id,create_date,delete_date,null as request_xml,delete_flag,generated_sql, null as i2b2_request_xml, null as master_type_cd, null as plugin_id from "
				+ getDbSchemaName()
				+ "qt_query_master "
				+ " where user_id = ? and delete_flag = ? and master_type_cd is NULL";

		sql += " order by create_date desc  ";

		if (fetchSize > 0
				&& dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.ORACLE)) {
			sql = "select * from ( " + sql + " ) where " + "  rownum <= "
					+ fetchSize;
		}

		if (fetchSize > 0
				&& dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.POSTGRES)) {
			sql = "select * from ( " + sql + " )q limit " 
					+ fetchSize;
		}
		
		List<QtQueryMaster> queryMasterList = jdbcTemplate.query(sql,
				new Object[] { userId, DELETE_NO_FLAG }, queryMasterMapper);

		return queryMasterList;
	}

	/**
	 * Returns list of query master by group id
	 * 
	 * @param groupId
	 * @return List<QtQueryMaster>
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryMaster> getQueryMasterByGroupId(String groupId,
			int fetchSize) {

		String sql = "select ";
		if (fetchSize > 0
				&& dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.SQLSERVER)) {
			sql += " top " + fetchSize;
		}
		sql += " query_master_id,name,user_id,group_id,create_date,delete_date,null as request_xml,delete_flag,generated_sql,null as i2b2_request_xml, null as master_type_cd, null as plugin_id from "
				+ getDbSchemaName()
				+ "qt_query_master "
				+ " where group_id = ? and delete_flag = ? and master_type_cd is NULL";

		sql += " order by create_date desc  ";

		if (fetchSize > 0
				&& dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.ORACLE)) {
			sql = " select * from (  " + sql + " ) where  rownum <= "
					+ fetchSize;
		}
		if (fetchSize > 0
				&& dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.POSTGRES)) {
			sql = "select * from ( " + sql + " )q limit " 
					+ fetchSize;
		}
		List<QtQueryMaster> queryMasterList = jdbcTemplate.query(sql,
				new Object[] { groupId, DELETE_NO_FLAG }, queryMasterMapper);
		return queryMasterList;
	}

	/**
	 * Find Query master by id
	 * 
	 * @param masterId
	 * @return QtQueryMaster
	 */
	public QtQueryMaster getQueryDefinition(String masterId) {
		String sql = "select * from " + getDbSchemaName() + "qt_query_master "
				+ " where query_master_id = ? and delete_flag = ? ";
		QtQueryMaster queryMaster = null;
		try {
			queryMaster = (QtQueryMaster) jdbcTemplate.queryForObject(sql,
					new Object[] { masterId, DELETE_NO_FLAG },
					queryMasterMapper);
		} catch (IncorrectResultSizeDataAccessException inResultEx) {
			log.error("Query doesn't exists for masterId :[" + masterId + "]");
		} catch (DataAccessException e) {
			log.error("Could not execute query master for masterId :["
					+ masterId + "]");
		}
		return queryMaster;
	}
	
	
	public List<QtQueryMaster> getQueryByName(String queryName) {
		String sql = "select * from " + getDbSchemaName() + "qt_query_master "
				+ " where name = ? and delete_flag = ? ";
		List<QtQueryMaster> queryMasterList = jdbcTemplate.query(sql,
				new Object[] { queryName, DELETE_NO_FLAG }, queryMasterMapper);
		return queryMasterList;
	}
	/**
	 * Function to rename query master
	 * 
	 * @param masterId
	 * @param queryNewName
	 * @throws I2B2DAOException
	 */
	public void renameQuery(String masterId, String queryNewName)
			throws I2B2DAOException {
		log.debug("Rename  masterId=" + masterId + " new query name"
				+ queryNewName);

		String sql = "update "
				+ getDbSchemaName()
				+ "qt_query_master set name = ? where query_master_id = ? and delete_flag = ?";
		int updatedRow = jdbcTemplate.update(sql, new Object[] { queryNewName,
				masterId, DELETE_NO_FLAG });
		if (updatedRow < 1) {
			throw new I2B2DAOException("Query with master id " + masterId
					+ " not found");
		}

	}

	/**
	 * Function to delete query using user and master id This function will not
	 * delete permanently, it will set delete flag field in query master, query
	 * instance and result instance to true
	 * 
	 * @param masterId
	 * @throws I2B2DAOException
	 */
	@SuppressWarnings("unchecked")
	public void deleteQuery(String masterId) throws I2B2DAOException {
		log.debug("Delete query for master id=" + masterId);
		String resultInstanceSql = "update " + getDbSchemaName()
				+ "qt_query_result_instance set "
				+ " delete_flag=? where query_instance_id in (select "
				+ "query_instance_id from " + getDbSchemaName()
				+ "qt_query_instance where query_master_id=?) ";
		if (dataSourceLookup.getServerType().equalsIgnoreCase(
				DAOFactoryHelper.SQLSERVER)) {
			resultInstanceSql = " update " + getDbSchemaName()
					+ "qt_query_result_instance set  delete_flag=? " + " from "
					+ getDbSchemaName()
					+ "qt_query_result_instance qri inner join "
					+ getDbSchemaName() + "qt_query_instance qi "
					+ " on  qri.query_instance_id = qi.query_instance_id "
					+ " where qi.query_master_id = ?";
		}
		String queryInstanceSql = "update "
				+ getDbSchemaName()
				+ "qt_query_instance set delete_flag = ? where query_master_id = ?  and delete_flag = ?";
		String queryMasterSql = "update "
				+ getDbSchemaName()
				+ "qt_query_master set delete_flag =?,delete_date=? where query_master_id = ? and delete_flag = ?";
		Date deleteDate = new Date(System.currentTimeMillis());
		int queryMasterCount = jdbcTemplate.update(queryMasterSql,
				new Object[] { DELETE_YES_FLAG, deleteDate, masterId,
						DELETE_NO_FLAG });
		if (queryMasterCount < 1) {
			throw new I2B2DAOException("Query not found with masterid =["
					+ masterId + "]");
		}

		int queryInstanceCount = jdbcTemplate.update(queryInstanceSql,
				new Object[] { DELETE_YES_FLAG, masterId, DELETE_NO_FLAG });
		log.debug("Total no. of query instance deleted" + queryInstanceCount);
		int queryResultInstanceCount = jdbcTemplate.update(resultInstanceSql,
				new Object[] { DELETE_YES_FLAG, masterId });
		log.debug("Total no. of query result deleted "
				+ queryResultInstanceCount);
	}

	private static class SaveQueryMaster extends SqlUpdate {

		private String INSERT_ORACLE = "";
		private String INSERT_SQLSERVER = "";
		private String INSERT_POSTGRES = "";
		private String SEQUENCE_ORACLE = "";
		private String SEQUENCE_POSTGRES = "";

		private DataSourceLookup dataSourceLookup = null;

		public SaveQueryMaster(DataSource dataSource, String dbSchemaName,
				DataSourceLookup dataSourceLookup) {
			super();
			this.setDataSource(dataSource);
			this.dataSourceLookup = dataSourceLookup;
			if(dataSourceLookup.getServerType().equalsIgnoreCase(
							DAOFactoryHelper.POSTGRES)) {				
				INSERT_POSTGRES = "INSERT INTO "
					+ dbSchemaName
					+ "QT_QUERY_MASTER "
					+ "(QUERY_MASTER_ID, NAME, USER_ID, GROUP_ID,MASTER_TYPE_CD,PLUGIN_ID,CREATE_DATE,DELETE_DATE,REQUEST_XML,DELETE_FLAG,GENERATED_SQL,I2B2_REQUEST_XML) "
					+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
				SEQUENCE_POSTGRES = "select nextval('QT_SQ_QM_QMID')";
				// setSql(INSERT_POSTGRES);
				// declareParameter(new SqlParameter(Types.INTEGER));
				return;
			}
			
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				this.setReturnGeneratedKeys(true);
				INSERT_ORACLE = "INSERT INTO "
						+ dbSchemaName
						+ "QT_QUERY_MASTER "
						+ "(QUERY_MASTER_ID, NAME, USER_ID, GROUP_ID,MASTER_TYPE_CD,PLUGIN_ID,CREATE_DATE,DELETE_DATE,REQUEST_XML,DELETE_FLAG,GENERATED_SQL,I2B2_REQUEST_XML) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
				setSql(INSERT_ORACLE);
				SEQUENCE_ORACLE = "select " + dbSchemaName
						+ "QT_SQ_QM_QMID.nextval from dual";
				declareParameter(new SqlParameter(Types.INTEGER));
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				INSERT_SQLSERVER = "INSERT INTO "
						+ dbSchemaName
						+ "QT_QUERY_MASTER "
						+ "( NAME, USER_ID, GROUP_ID,MASTER_TYPE_CD,PLUGIN_ID,CREATE_DATE,DELETE_DATE,REQUEST_XML,DELETE_FLAG,GENERATED_SQL,I2B2_REQUEST_XML) "
						+ "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
				this.setSql(INSERT_SQLSERVER);
			}
			this.dataSourceLookup = dataSourceLookup;

			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();

		}

		public void save(QtQueryMaster queryMaster, String i2b2RequestXml) {
			JdbcTemplate jdbc = getJdbcTemplate();
			int masterQueryId = 0;
			Object[] object = null;
			int queryMasterIdentityId = 0;

			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				object = new Object[] { queryMaster.getName(),
						queryMaster.getUserId(), queryMaster.getGroupId(),
						queryMaster.getMasterTypeCd(),
						queryMaster.getPluginId(), queryMaster.getCreateDate(),
						queryMaster.getDeleteDate(),
						queryMaster.getRequestXml(),
						queryMaster.getDeleteFlag(),
						queryMaster.getGeneratedSql(), i2b2RequestXml };
				update(object);
				queryMasterIdentityId = jdbc.queryForInt("SELECT @@IDENTITY");

			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				queryMasterIdentityId = jdbc.queryForInt(SEQUENCE_ORACLE);
				object = new Object[] { queryMasterIdentityId,
						queryMaster.getName(), queryMaster.getUserId(),
						queryMaster.getGroupId(),
						queryMaster.getMasterTypeCd(),
						queryMaster.getPluginId(), queryMaster.getCreateDate(),
						queryMaster.getDeleteDate(),
						queryMaster.getRequestXml(),
						queryMaster.getDeleteFlag(),
						queryMaster.getGeneratedSql(), i2b2RequestXml };
				update(object);
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
							DAOFactoryHelper.POSTGRES)) {
				queryMasterIdentityId = jdbc.queryForInt(SEQUENCE_POSTGRES);
				object = new Object[] { queryMasterIdentityId,
						queryMaster.getName(), queryMaster.getUserId(),
						queryMaster.getGroupId(),
						queryMaster.getMasterTypeCd(),
						queryMaster.getPluginId(), queryMaster.getCreateDate(),
						queryMaster.getDeleteDate(),
						queryMaster.getRequestXml(),
						queryMaster.getDeleteFlag(),
						queryMaster.getGeneratedSql(), i2b2RequestXml };				
				
				jdbc.update(INSERT_POSTGRES, object);
				/*
				String sql = INSERT_POSTGRES + "( '" + queryMasterIdentityId + "', " + getSqlFormattedString(queryMaster.getName()) + ", " 
				+ getSqlFormattedString(queryMaster.getUserId()) + ", " + getSqlFormattedString(queryMaster.getGroupId()) + ", " 
				+ getSqlFormattedString(queryMaster.getMasterTypeCd()) + ", " + queryMaster.getPluginId() + ", " 
				+ getSqlFormattedString(queryMaster.getCreateDate()) + ", " + getSqlFormattedString(queryMaster.getDeleteDate()) + ", " 
				+ getSqlFormattedString(queryMaster.getRequestXml()) + ", " + getSqlFormattedString(queryMaster.getDeleteFlag()) + ", " 
				+ getSqlFormattedString(queryMaster.getGeneratedSql()) + ", " + getSqlFormattedString(i2b2RequestXml)  
				+ ")";
				jdbc.execute(sql);
				*/
				/*
				final int queryMasterId = jdbc.queryForInt(SEQUENCE_POSTGRES); // Integer.toString(queryMasterIdentityId);
				final String queryMasterName = queryMaster.getName();
				final String queryMasterUserId = queryMaster.getUserId();
				final String queryGroupId = queryMaster.getGroupId();
				final String querytMasterTypeCd = queryMaster.getMasterTypeCd();
				final String queryPluginId = queryMaster.getPluginId();
				final Date queryCreateDate = queryMaster.getCreateDate();
				final Date queryDeleteDate = queryMaster.getDeleteDate();
				final String queryRequestXml = queryMaster.getRequestXml();
				final String queryDeleteFlag = queryMaster.getDeleteFlag();
				final String queryGeneratedSql = queryMaster.getGeneratedSql();
				final String queryI2b2RequestXml = i2b2RequestXml;
				
				jdbc.execute(INSERT_POSTGRES,  new PreparedStatementCallback() {
					
					@Override
					public Object doInPreparedStatement(PreparedStatement ps)
							throws SQLException, DataAccessException {	
						
						ps.setInt(1, queryMasterId);
						ps.setString(2, queryMasterName);
						ps.setString(3, queryMasterUserId);
						ps.setString(4, queryGroupId);
						ps.setString(5, querytMasterTypeCd);
						if(queryPluginId != null) {
							ps.setInt(6, Integer.parseInt(queryPluginId));
						} else {
							ps.setNull(6, Types.NUMERIC);
						}						
						ps.setDate(7, (queryCreateDate != null) ? new java.sql.Date(queryCreateDate.getTime()) : null);
						ps.setDate(8, (queryDeleteDate != null) ? new java.sql.Date(queryDeleteDate.getTime()) : null);
						ps.setString(9, queryRequestXml);
						ps.setString(10, queryDeleteFlag);
						ps.setString(11, queryGeneratedSql);
						ps.setString(12, queryI2b2RequestXml);

						ps.execute();
						return null;
					}
				});*/
			}

			queryMaster.setQueryMasterId(String.valueOf(queryMasterIdentityId));
			System.out.println(queryMasterIdentityId);
		}
	}

	private static class QtQueryMasterRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			QtQueryMaster queryMaster = new QtQueryMaster();
			queryMaster.setQueryMasterId(rs.getString("QUERY_MASTER_ID"));
			queryMaster.setName(rs.getString("NAME"));
			queryMaster.setUserId(rs.getString("USER_ID"));
			queryMaster.setGroupId(rs.getString("GROUP_ID"));
			queryMaster.setMasterTypeCd(rs.getString("MASTER_TYPE_CD"));
			queryMaster.setPluginId(rs.getString("PLUGIN_ID"));
			queryMaster.setCreateDate(rs.getTimestamp("CREATE_DATE"));
			queryMaster.setDeleteDate(rs.getTimestamp("DELETE_DATE"));
			queryMaster.setRequestXml(rs.getString("REQUEST_XML"));
			queryMaster.setDeleteFlag(rs.getString("DELETE_FLAG"));
			queryMaster.setGeneratedSql(rs.getString("GENERATED_SQL"));
			queryMaster.setI2b2RequestXml(rs.getString("I2B2_REQUEST_XML"));
			return queryMaster;
		}
	}

	

}
