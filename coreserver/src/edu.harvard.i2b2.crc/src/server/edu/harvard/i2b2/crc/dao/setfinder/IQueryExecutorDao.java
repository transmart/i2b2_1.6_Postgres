package edu.harvard.i2b2.crc.dao.setfinder;

import javax.transaction.UserTransaction;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.SetFinderDAOFactory;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ResultOutputOptionListType;

public interface IQueryExecutorDao {
	public String executeSQL(UserTransaction transaction,
			int transactionTimeout, DataSourceLookup dsLookup,
			SetFinderDAOFactory sfDAOFactory, String requestXml,
			String sqlString, String queryInstanceId, String patientSetId,
			ResultOutputOptionListType resultOutputList, boolean allowLargeTextValueConstrainFlag)
			throws CRCTimeOutException, I2B2DAOException;
}
