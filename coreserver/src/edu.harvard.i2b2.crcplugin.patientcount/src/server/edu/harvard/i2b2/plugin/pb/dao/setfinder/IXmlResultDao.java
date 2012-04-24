package edu.harvard.i2b2.plugin.pb.dao.setfinder;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.plugin.pb.datavo.QtXmlResult;

public interface IXmlResultDao {

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
	public String createQueryXmlResult(String resultInstanceId, String xmlValue);

	/**
	 * Returns list of query instance for the given master id
	 * 
	 * @param queryMasterId
	 * @return List<QtQueryInstance>
	 */
	@SuppressWarnings("unchecked")
	public QtXmlResult getXmlResultByResultInstanceId(String resultInstanceId)
			throws I2B2DAOException;

}