package edu.harvard.i2b2.crc.dao.setfinder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.ProcessTimingReportUtil;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryDefinitionUnWrapUtil;
import edu.harvard.i2b2.crc.dao.setfinder.querybuilder.QueryTimingHandler;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.db.QtQueryInstance;
import edu.harvard.i2b2.crc.datavo.db.QtQueryMaster;
import edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.crc.ejb.role.MissingRoleException;
import edu.harvard.i2b2.crc.role.AuthrizationHelper;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

public class QueryResultEncounterSetGenerator extends CRCDAO implements
		IResultGenerator {

	public void generateResult(Map param) throws I2B2DAOException {

		SetFinderConnection sfConn = (SetFinderConnection) param
				.get("SetFinderConnection");
		SetFinderDAOFactory sfDAOFactory = (SetFinderDAOFactory) param
				.get("SetFinderDAOFactory");
		// String patientSetId = (String)param.get("PatientSetId");
		String queryInstanceId = (String) param.get("QueryInstanceId");
		String TEMP_DX_TABLE = (String) param.get("TEMP_DX_TABLE");
		String resultInstanceId = (String) param.get("ResultInstanceId");
		String resultTypeName = (String) param.get("ResultOptionName");
		DataSourceLookup originalDataSource = (DataSourceLookup) param
				.get("OriginalDataSourceLookup");
		List<String> roles = (List<String>) param.get("Roles");
		String processTimingFlag = (String)param.get("ProcessTimingReportFlag");

		String queryGeneratorVersion = "1.6";
		try {
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			queryGeneratorVersion = qpUtil
					.getCRCPropertyValue("edu.harvard.i2b2.crc.setfinder.querygenerator.version");
		} catch (I2B2Exception e) {
			// ignore this default will be 1.6
		}

		boolean errorFlag = false;
		Exception exception = null;
		int loadCount = 0, realCount = 0;
		String obfuscationDescription = "", obfusMethod = "";

		try {

			int i = 0;
			IEncounterSetCollectionDao encounterSetCollectionDao = sfDAOFactory
					.getEncounterSetCollectionDAO();
			encounterSetCollectionDao
					.createPatientEncCollection(resultInstanceId);

			// build the encounter set sql using dx table
			// if the querytiming is not SAMEVISIT, then join the
			// visit_dimension table to get encoutners for the patients.
			String patientIdSql = buildEncounterSetSql(sfDAOFactory,
					queryInstanceId, TEMP_DX_TABLE, queryGeneratorVersion);
			log.debug("Executing setfinder query result type encounter set sql [" + patientIdSql + "]");
			LogTimingUtil logTimingUtil = new LogTimingUtil();
			
			// smuniraju: Separate block for Postgres to improve performance of INSERT queries.
			if(sfDAOFactory.getDataSourceLookup().getServerType().equalsIgnoreCase(DAOFactoryHelper.POSTGRES)) {
				String schemaName = sfDAOFactory.getDataSourceLookup().getFullSchema();		
				schemaName = (schemaName == null || schemaName.trim().equals("")) ? "" : getDbSchemaName() + ".";
				
				// Used to increment values for set_index
				java.sql.Statement sqlStmt = sfConn.createStatement();										
				sqlStmt.execute("CREATE TEMP SEQUENCE " + schemaName + "temp_seq_enc_set_index START WITH 1");
				sqlStmt.close();
				
				// Append resultInstanceId and nextVal(seq_qt_enc_col_set_index) to SELECT query obtained from buildEncounterSql				
				patientIdSql =  "SELECT " + resultInstanceId + ", NEXTVAL('" + schemaName + "temp_seq_enc_set_index'), " 
				             +  patientIdSql.substring(7);
				String insertSql = "INSERT INTO " + schemaName + "qt_patient_enc_collection(result_instance_id,set_index,patient_num,encounter_num)" 
								 + patientIdSql;			
				
				sqlStmt = sfConn.createStatement();
				logTimingUtil.setStartTime();
				loadCount = sqlStmt.executeUpdate(insertSql);
				logTimingUtil.setEndTime();
				sqlStmt.close();
				
			} else {				
				Statement readQueryStmt = sfConn.createStatement();
				logTimingUtil.setStartTime();
				ResultSet resultSet = readQueryStmt.executeQuery(patientIdSql);
				while (resultSet.next()) {
					long encounterNum = resultSet.getLong("encounter_num");
					long patientNum = resultSet.getLong("patient_num");
					encounterSetCollectionDao
							.addEncounter(encounterNum, patientNum);
					i++;
					loadCount++;

					if ((i % 500) == 0) {
						log.debug("Loading [" + loadCount + "] encounters"
								+ " for query instanse = " + queryInstanceId);
					}
				}
				readQueryStmt.close();
				logTimingUtil.setEndTime();
				
				encounterSetCollectionDao.flush();
			}
			/* smuniraju 
			Statement readQueryStmt = sfConn.createStatement();
			log.debug("Executing setfinder query result type encounter set sql [" + patientIdSql + "]");
			
			LogTimingUtil logTimingUtil = new LogTimingUtil();
			logTimingUtil.setStartTime();
			
			ResultSet resultSet = readQueryStmt.executeQuery(patientIdSql);
			while (resultSet.next()) {
				long encounterNum = resultSet.getLong("encounter_num");
				long patientNum = resultSet.getLong("patient_num");
				encounterSetCollectionDao
						.addEncounter(encounterNum, patientNum);
				i++;
				loadCount++;

				if ((i % 500) == 0) {
					log.debug("Loading [" + loadCount + "] encounters"
							+ " for query instanse = " + queryInstanceId);
				}
			}
			readQueryStmt.close();
			logTimingUtil.setEndTime();
			*/			
			if (processTimingFlag != null) {
				ProcessTimingReportUtil ptrUtil = new ProcessTimingReportUtil(sfDAOFactory.getDataSourceLookup());
				ptrUtil.logProcessTimingMessage(queryInstanceId, ptrUtil.buildProcessTiming(logTimingUtil, "BUILD - ENCOUNTERSET", ""));
			}
			log.debug("Total encounters loaded for query instance ="
					+ queryInstanceId + " is [" + loadCount + "]");
			
			// smuniraju: Moved it to non-Postgres block above
			// encounterSetCollectionDao.flush();			
			
			realCount = loadCount;
			/*
			 * DataSourceLookup dataSourceLookup = sfDAOFactory
			 * .getDataSourceLookup();
			 * this.setDbSchemaName(dataSourceLookup.getFullSchema()); String
			 * insertSql = "", generateRowNumSql = "";
			 * 
			 * if (dataSourceLookup.getServerType().equalsIgnoreCase(
			 * DAOFactoryHelper.ORACLE)) { insertSql = "insert into " +
			 * getDbSchemaName() +
			 * "qt_patient_set_collection(patient_set_coll_id,result_instance_id,set_index,patient_num) "
			 * + "select  " + getDbSchemaName() + "QT_SQ_QPR_PCID.nextval," +
			 * resultInstanceId + ",rownum,patient_num from " + TEMP_DX_TABLE +
			 * " order by patient_num"; } else if
			 * (dataSourceLookup.getServerType().equalsIgnoreCase(
			 * DAOFactoryHelper.SQLSERVER)) { generateRowNumSql =
			 * "select identity(int,1,1) rownumber,patient_num into #temp_dx from "
			 * + TEMP_DX_TABLE + " order by patient_num"; insertSql =
			 * "insert into " + getDbSchemaName() +
			 * "qt_patient_set_collection(result_instance_id,set_index,patient_num) select "
			 * + resultInstanceId + ", rownumber, patient_num from #temp_dx " +
			 * " order by patient_num drop table #temp_dx"; } Statement
			 * readQueryStmt = sfConn.createStatement(); if
			 * (generateRowNumSql.length() > 0) { log.debug("Executing SQL [" +
			 * generateRowNumSql + "]  for query instanse = " +
			 * queryInstanceId); readQueryStmt.executeUpdate(generateRowNumSql);
			 * } log.debug("Executing SQL [" + insertSql +
			 * "]  for query instanse = " + queryInstanceId); loadCount =
			 * readQueryStmt.executeUpdate(insertSql);
			 */

			// check for the user role to see if it needs data obfscation
			DataSourceLookup dataSourceLookup = sfDAOFactory
					.getOriginalDataSourceLookup();
			String domainId = originalDataSource.getDomainId();
			String projectId = originalDataSource.getProjectPath();
			String userId = originalDataSource.getOwnerId();
			boolean noDataAggFlag = false, noDataObfscFlag = false;

			DAOFactoryHelper helper = new DAOFactoryHelper(dataSourceLookup
					.getDomainId(), dataSourceLookup.getProjectPath(),
					dataSourceLookup.getOwnerId());

			IDAOFactory daoFactory = helper.getDAOFactory();
			AuthrizationHelper authHelper = new AuthrizationHelper(domainId,
					projectId, userId, daoFactory);

			try {
				authHelper.checkRoleForProtectionLabel(
						"SETFINDER_QRY_WITHOUT_DATAOBFSC", roles);
			} catch (MissingRoleException noRoleEx) {
				noDataAggFlag = true;
			} catch (I2B2Exception e) {
				throw e;
			}
			try {
				authHelper.checkRoleForProtectionLabel(
						"SETFINDER_QRY_WITH_DATAOBFSC", roles);
			} catch (MissingRoleException noRoleEx) {
				noDataObfscFlag = true;
			} catch (I2B2Exception e) {
				throw e;
			}

			if (noDataAggFlag && !noDataObfscFlag) {
				obfuscationDescription = "~";
				obfusMethod = IQueryResultInstanceDao.OBTOTAL;
				GaussianBoxMuller gaussianBoxMuller = new GaussianBoxMuller();
				loadCount = (int) gaussianBoxMuller
						.getNormalizedValueForCount(loadCount);
			}

			// readQueryStmt.close();
		} catch (SQLException sqlEx) {
			exception = sqlEx;
			log.error("QueryResultPatientSetGenerator.generateResult:"
					+ sqlEx.getMessage(), sqlEx);
			throw new I2B2DAOException(
					"QueryResultPatientSetGenerator.generateResult:"
							+ sqlEx.getMessage(), sqlEx);

		} catch (Throwable throwable) {
			throwable.printStackTrace();
		} finally {
			IQueryResultInstanceDao resultInstanceDao = sfDAOFactory
					.getPatientSetResultDAO();

			if (errorFlag) {
				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_ERROR, 0);
			} else {
				resultInstanceDao.updatePatientSet(resultInstanceId,
						QueryStatusTypeId.STATUSTYPE_ID_FINISHED, "",
						loadCount, realCount, obfusMethod);
				//String description = "Encounter Set - "
				//		+ obfuscationDescription + loadCount + " encounters";
				String queryName = sfDAOFactory.getQueryMasterDAO().getQueryDefinition(
						sfDAOFactory.getQueryInstanceDAO().getQueryInstanceByInstanceId(queryInstanceId).getQtQueryMaster().getQueryMasterId()).getName();
				String description = "Encounter Set for \"" + queryName +"\"";
				resultInstanceDao.updateResultInstanceDescription(
						resultInstanceId, description);

			}
		}
	}

	private String buildEncounterSetSql(SetFinderDAOFactory sfDAOFactory,
			String queryInstanceId, String TEMP_DX_TABLE,
			String queryGeneratorVersion) throws I2B2DAOException {
		// get request xml from query instance id
		// call timing helper to find if timing is samevisit

		String encounterSetSql = " select encounter_num,patient_num from "
				+ TEMP_DX_TABLE + " order by encounter_num, patient_num ";
		if (queryGeneratorVersion.equals("1.6")) {
			IQueryInstanceDao queryInstanceDao = sfDAOFactory
					.getQueryInstanceDAO();
			QtQueryInstance queryInstance = queryInstanceDao
					.getQueryInstanceByInstanceId(queryInstanceId);
			String queryMasterId = queryInstance.getQtQueryMaster()
					.getQueryMasterId();
			IQueryMasterDao queryMasterDao = sfDAOFactory.getQueryMasterDAO();
			QtQueryMaster queryMasterType = queryMasterDao
					.getQueryDefinition(queryMasterId);
			QueryDefinitionUnWrapUtil qpUnwrapUtil = new QueryDefinitionUnWrapUtil();
			QueryDefinitionType queryDefType = null;
			try {
				queryDefType = qpUnwrapUtil
						.getQueryDefinitionType(queryMasterType
								.getI2b2RequestXml());
			} catch (I2B2DAOException e) {
				throw e;
			}
			QueryTimingHandler queryTimingHandler = new QueryTimingHandler();
			boolean sameVisitFlag = queryTimingHandler
					.isSameVisit(queryDefType);

			String schemaName = sfDAOFactory.getDataSourceLookup().getFullSchema();			
			log.debug("SchemaName: " + schemaName);
			if (sameVisitFlag == false) {
				// smuniraju: Exchange the order of encounter_num, patient_num in SELECT query below.
				encounterSetSql = " select patient_num, encounter_num from "
						+ (schemaName == null || schemaName.trim().equals("") ? "" : schemaName + ".")
						+ "visit_dimension  "
						+ " where patient_num in (select distinct patient_num from "
						+ TEMP_DX_TABLE
						+ ")  order by encounter_num,patient_num";
			} 
		}
		return encounterSetSql;
	}
}
