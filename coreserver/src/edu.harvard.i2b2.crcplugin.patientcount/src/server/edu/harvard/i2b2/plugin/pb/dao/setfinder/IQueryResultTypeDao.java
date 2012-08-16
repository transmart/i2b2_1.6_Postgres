package edu.harvard.i2b2.plugin.pb.dao.setfinder;

import java.util.List;

import edu.harvard.i2b2.plugin.pb.datavo.QtQueryResultType;

public interface IQueryResultTypeDao {

	/**
	 * Returns list of query master by user id
	 * 
	 * @param userId
	 * @return List<QtQueryMaster>
	 */
	@SuppressWarnings("unchecked")
	public QtQueryResultType getQueryResultTypeById(int resultTypeId);

	/**
	 * Returns list of query master by user id
	 * 
	 * @param userId
	 * @return List<QtQueryMaster>
	 */
	@SuppressWarnings("unchecked")
	public List<QtQueryResultType> getQueryResultTypeByName(String resultName);

	/**
	 * Get all result type
	 * 
	 * @return List<QtQueryResultType>
	 */
	public List<QtQueryResultType> getAllQueryResultType();

}