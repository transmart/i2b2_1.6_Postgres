/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.plugin.pb.dao.setfinder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

import edu.harvard.i2b2.plugin.pb.dao.CRCDAO;
import edu.harvard.i2b2.plugin.pb.dao.DAOFactoryHelper;
import edu.harvard.i2b2.plugin.pb.datavo.DataSourceLookup;
import edu.harvard.i2b2.plugin.pb.datavo.QtQueryInstance;
import edu.harvard.i2b2.plugin.pb.datavo.QtQueryMaster;
import edu.harvard.i2b2.plugin.pb.datavo.QtQueryStatusType;

/**
 * Class to handle persistance operation of Query instance i.e. each run of
 * query is called query instance $Id: QueryInstanceSpringDao.java,v 1.4
 * 2008/04/08 19:38:24 rk903 Exp $
 * 
 * @author rkuttan
 * @see QtQueryInstance
 */
public class QueryInstanceSpringDao extends CRCDAO implements IQueryInstanceDao {

	JdbcTemplate jdbcTemplate = null;
	SaveQueryInstance saveQueryInstance = null;
	QtQueryInstanceRowMapper queryInstanceMapper = null;
	private DataSourceLookup dataSourceLookup = null;

	public QueryInstanceSpringDao(DataSource dataSource,
			DataSourceLookup dataSourceLookup) {
		setDataSource(dataSource);
		setDbSchemaName(dataSourceLookup.getFullSchema());
		jdbcTemplate = new JdbcTemplate(dataSource);
		this.dataSourceLookup = dataSourceLookup;
		queryInstanceMapper = new QtQueryInstanceRowMapper();
	}

	/**
	 * Function to create query instance
	 * 
	 * @param queryMasterId
	 * @param userId
	 * @param groupId
	 * @param batchMode
	 * @param statusId
	 * @return query instance id
	 */
	public String createQueryInstance(String queryMasterId, String userId,
			String groupId, String batchMode, int statusId) {
		QtQueryInstance queryInstance = new QtQueryInstance();
		queryInstance.setUserId(userId);
		queryInstance.setGroupId(groupId);
		queryInstance.setBatchMode(batchMode);
		queryInstance.setDeleteFlag("N");

		QtQueryMaster queryMaster = new QtQueryMaster();
		queryMaster.setQueryMasterId(queryMasterId);
		queryInstance.setQtQueryMaster(queryMaster);

		QtQueryStatusType statusType = new QtQueryStatusType();
		statusType.setStatusTypeId(statusId);
		queryInstance.setQtQueryStatusType(statusType);

		Date startDate = new Date(System.currentTimeMillis());
		queryInstance.setStartDate(startDate);
		saveQueryInstance = new SaveQueryInstance(getDataSource(),
				getDbSchemaName(), dataSourceLookup);
		saveQueryInstance.save(queryInstance);

		return queryInstance.getQueryInstanceId();
	}

	/**
	 * Returns list of query instance for the given master id
	 * 
	 * @param queryMasterId
	 * @return List<QtQueryInstance>
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryInstance> getQueryInstanceByMasterId(String queryMasterId) {
		String sql = "select *  from " + getDbSchemaName()
				+ "qt_query_instance where query_master_id = ?";
		List<QtQueryInstance> queryInstanceList = jdbcTemplate.query(sql,
				new Object[] { queryMasterId }, queryInstanceMapper);
		return queryInstanceList;
	}

	/**
	 * Find query instance by id
	 * 
	 * @param queryInstanceId
	 * @return QtQueryInstance
	 */
	public QtQueryInstance getQueryInstanceByInstanceId(String queryInstanceId) {
		String sql = "select *  from " + getDbSchemaName()
				+ "qt_query_instance  where query_instance_id =?";

		QtQueryInstance queryInstance = (QtQueryInstance) jdbcTemplate
				.queryForObject(sql, new Object[] { queryInstanceId },
						queryInstanceMapper);

		return queryInstance;
	}

	/**
	 * Update query instance
	 * 
	 * @param queryInstance
	 * @return QtQueryInstance
	 */
	public QtQueryInstance update(QtQueryInstance queryInstance,
			boolean appendMessageFlag) {

		Integer statusTypeId = (queryInstance.getQtQueryStatusType() != null) ? queryInstance
				.getQtQueryStatusType().getStatusTypeId()
				: null;
		String messageUpdate = "";
		if (appendMessageFlag) {
			String concatOperator = "";
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				concatOperator = "||";
				messageUpdate = " MESSAGE = nvl(MESSAGE,'') " + concatOperator
						+ " ? ";
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRES)) {
				concatOperator = "||";
				messageUpdate = " MESSAGE = coalesce(MESSAGE,'') " + concatOperator
						+ " ? ";
			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				concatOperator = "+";
				messageUpdate = " MESSAGE = isnull(Cast(MESSAGE as nvarchar(4000)),'') "
						+ concatOperator + " ? ";
				// Cast(notes as nvarchar(4000))
			}

		} else {
			messageUpdate = " MESSAGE = ?";
		}
		String sql = "UPDATE "
				+ getDbSchemaName()
				+ "QT_QUERY_INSTANCE set USER_ID = ?, GROUP_ID = ?,BATCH_MODE = ?,END_DATE = ? ,STATUS_TYPE_ID = ?, "
				+ messageUpdate + " where query_instance_id = ? ";
		jdbcTemplate.update(sql, new Object[] {
				queryInstance.getUserId(),
				queryInstance.getGroupId(),
				queryInstance.getBatchMode(),
				queryInstance.getEndDate(),
				statusTypeId,
				(queryInstance.getMessage() == null) ? "" : queryInstance
						.getMessage(), queryInstance.getQueryInstanceId() });
		return queryInstance;
	}

	private static class SaveQueryInstance extends SqlUpdate {

		private String INSERT_ORACLE = "";
		private String INSERT_SQLSERVER = "";
		private String INSERT_POSTGRES = "";
		private String SEQUENCE_ORACLE = "";
		private String SEQUENCE_POSTGRES = "";
		private DataSourceLookup dataSourceLookup = null;

		public SaveQueryInstance(DataSource dataSource, String dbSchemaName,
				DataSourceLookup dataSourceLookup) {
			super();
			this.dataSourceLookup = dataSourceLookup;
			// sqlServerSequenceDao = new
			// SQLServerSequenceDAO(dataSource,dataSourceLookup) ;
			setDataSource(dataSource);
			
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.POSTGRES)) {
				INSERT_POSTGRES = "INSERT INTO "
						+ dbSchemaName
						+ "QT_QUERY_INSTANCE "
						+ "(QUERY_INSTANCE_ID, QUERY_MASTER_ID, USER_ID, GROUP_ID,BATCH_MODE,START_DATE,END_DATE,STATUS_TYPE_ID,DELETE_FLAG) "
						+ "VALUES (?,?,?,?,?,?,?,?,?)";				
				SEQUENCE_POSTGRES = "select nextval('QT_SQ_QI_QIID')";
				return;
			} 
			
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				INSERT_ORACLE = "INSERT INTO "
						+ dbSchemaName
						+ "QT_QUERY_INSTANCE "
						+ "(QUERY_INSTANCE_ID, QUERY_MASTER_ID, USER_ID, GROUP_ID,BATCH_MODE,START_DATE,END_DATE,STATUS_TYPE_ID,DELETE_FLAG) "
						+ "VALUES (?,?,?,?,?,?,?,?,?)";
				setSql(INSERT_ORACLE);
				SEQUENCE_ORACLE = "select " + dbSchemaName
						+ "QT_SQ_QI_QIID.nextval from dual";
				declareParameter(new SqlParameter(Types.INTEGER));

			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				INSERT_SQLSERVER = "INSERT INTO "
						+ dbSchemaName
						+ "QT_QUERY_INSTANCE "
						+ "( QUERY_MASTER_ID, USER_ID, GROUP_ID,BATCH_MODE,START_DATE,END_DATE,STATUS_TYPE_ID,DELETE_FLAG) "
						+ "VALUES (?,?,?,?,?,?,?,?)";
				setSql(INSERT_SQLSERVER);
			}

			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.TIMESTAMP));
			declareParameter(new SqlParameter(Types.INTEGER));
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();
		}

		public void save(QtQueryInstance queryInstance) {
			JdbcTemplate jdbc = getJdbcTemplate();
			int queryInstanceId = 0;
			Object[] object = null;			
			 if (dataSourceLookup.getServerType().equalsIgnoreCase(
						DAOFactoryHelper.POSTGRES)) {
				queryInstanceId = jdbc.queryForInt(SEQUENCE_POSTGRES);
				queryInstance.setQueryInstanceId(String.valueOf(queryInstanceId));
				object = new Object[] { queryInstance.getQueryInstanceId(),
						queryInstance.getQtQueryMaster().getQueryMasterId(),
						queryInstance.getUserId(), queryInstance.getGroupId(),
						queryInstance.getBatchMode(),
						queryInstance.getStartDate(),
						queryInstance.getEndDate(),
						queryInstance.getQtQueryStatusType().getStatusTypeId(),
						queryInstance.getDeleteFlag() };
				
				jdbc.update(INSERT_POSTGRES, object);
				return;
			}
			 
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				object = new Object[] {

				queryInstance.getQtQueryMaster().getQueryMasterId(),
						queryInstance.getUserId(), queryInstance.getGroupId(),
						queryInstance.getBatchMode(),
						queryInstance.getStartDate(),
						queryInstance.getEndDate(),
						queryInstance.getQtQueryStatusType().getStatusTypeId(),
						queryInstance.getDeleteFlag() };

			} else if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.ORACLE)) {
				queryInstanceId = jdbc.queryForInt(SEQUENCE_ORACLE);
				queryInstance.setQueryInstanceId(String
						.valueOf(queryInstanceId));
				object = new Object[] { queryInstance.getQueryInstanceId(),
						queryInstance.getQtQueryMaster().getQueryMasterId(),
						queryInstance.getUserId(), queryInstance.getGroupId(),
						queryInstance.getBatchMode(),
						queryInstance.getStartDate(),
						queryInstance.getEndDate(),
						queryInstance.getQtQueryStatusType().getStatusTypeId(),
						queryInstance.getDeleteFlag() };
			}

			update(object);
			if (dataSourceLookup.getServerType().equalsIgnoreCase(
					DAOFactoryHelper.SQLSERVER)) {
				int queryInstanceIdentityId = jdbc
						.queryForInt("SELECT @@IDENTITY");

				queryInstance.setQueryInstanceId(String
						.valueOf(queryInstanceIdentityId));
				System.out.println(queryInstanceIdentityId);
			}
		}
	}

	private class QtQueryInstanceRowMapper implements RowMapper {
		QueryStatusTypeSpringDao statusTypeDao = new QueryStatusTypeSpringDao(
				dataSource, dataSourceLookup);

		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			QtQueryInstance queryInstance = new QtQueryInstance();
			queryInstance.setQueryInstanceId(rs.getString("QUERY_INSTANCE_ID"));
			QtQueryMaster queryMaster = new QtQueryMaster();
			queryMaster.setQueryMasterId(rs.getString("QUERY_MASTER_ID"));
			queryInstance.setQtQueryMaster(queryMaster);
			queryInstance.setUserId(rs.getString("USER_ID"));
			queryInstance.setGroupId(rs.getString("GROUP_ID"));
			queryInstance.setBatchMode(rs.getString("BATCH_MODE"));
			queryInstance.setStartDate(rs.getTimestamp("START_DATE"));
			queryInstance.setEndDate(rs.getTimestamp("END_DATE"));
			queryInstance.setMessage(rs.getString("MESSAGE"));
			int statusTypeId = rs.getInt("STATUS_TYPE_ID");
			queryInstance.setQtQueryStatusType(statusTypeDao
					.getQueryStatusTypeById(statusTypeId));
			queryInstance.setDeleteFlag(rs.getString("DELETE_FLAG"));
			return queryInstance;
		}
	}
}
