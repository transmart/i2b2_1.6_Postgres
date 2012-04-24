package edu.harvard.i2b2.plugin.pb.dao;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocator;
import edu.harvard.i2b2.plugin.pb.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.plugin.pb.dao.setfinder.IQueryMasterDao;
import edu.harvard.i2b2.plugin.pb.dao.setfinder.IQueryResultInstanceDao;
import edu.harvard.i2b2.plugin.pb.dao.setfinder.IQueryStatusTypeDao;
import edu.harvard.i2b2.plugin.pb.dao.setfinder.IXmlResultDao;
import edu.harvard.i2b2.plugin.pb.dao.setfinder.QueryInstanceSpringDao;
import edu.harvard.i2b2.plugin.pb.dao.setfinder.QueryMasterSpringDao;
import edu.harvard.i2b2.plugin.pb.dao.setfinder.QueryResultInstanceSpringDao;
import edu.harvard.i2b2.plugin.pb.dao.setfinder.QueryStatusTypeSpringDao;
import edu.harvard.i2b2.plugin.pb.dao.setfinder.XmlResultSpringDao;
import edu.harvard.i2b2.plugin.pb.datavo.DataSourceLookup;
import edu.harvard.i2b2.plugin.pb.util.QueryProcessorUtil;

public class OracleDAOFactory implements IDAOFactory {

	private DataSourceLookup dataSourceLookup = null,
			orignalDataSourceLookup = null;
	private DataSource dataSource = null;
	private QueryProcessorUtil crcUtil = null;
	/** log **/
	protected final static Log log = LogFactory.getLog(OracleDAOFactory.class);

	public OracleDAOFactory(DataSourceLookup dataSourceLookup,
			DataSourceLookup originalDataSourceLookup) throws I2B2DAOException {
		this.dataSourceLookup = dataSourceLookup;
		this.orignalDataSourceLookup = originalDataSourceLookup;
		crcUtil = QueryProcessorUtil.getInstance();
		String dataSourceName = dataSourceLookup.getDataSource();
		log.info("Using datasource " + dataSourceName);
		try {
			// dataSource = (DataSource)
			// crcUtil.getSpringDataSource(dataSourceName);
			dataSource = ServiceLocator.getInstance().getAppServerDataSource(
					dataSourceName);
		} catch (I2B2Exception i2b2Ex) {
			log.error(i2b2Ex);
			throw new I2B2DAOException(
					"Error getting application/spring datasource "
							+ dataSourceName + " : " + i2b2Ex.getMessage(),
					i2b2Ex);
		}
	}

	public OracleDAOFactory(DataSourceLookup dataSourceLookup,
			DataSource dataSource) throws I2B2DAOException {
		this.dataSourceLookup = dataSourceLookup;
		this.dataSource = dataSource;
		this.orignalDataSourceLookup = dataSourceLookup;
		crcUtil = QueryProcessorUtil.getInstance();
	}

	public SetFinderDAOFactory getSetFinderDAOFactory() {
		return new OracleSetFinderDAOFactory(dataSource, dataSourceLookup,
				orignalDataSourceLookup);
	}

	//

	public class OracleSetFinderDAOFactory implements SetFinderDAOFactory {

		private DataSourceLookup dataSourceLookup = null,
				orignalDataSourceLookup = null;
		private DataSource dataSource = null;

		public OracleSetFinderDAOFactory(DataSource dataSource,
				DataSourceLookup dataSourceLookup,
				DataSourceLookup orignalDataSourceLookup) {
			this.dataSourceLookup = dataSourceLookup;
			this.dataSource = dataSource;
			this.orignalDataSourceLookup = orignalDataSourceLookup;

		}

		public IQueryMasterDao getQueryMasterDAO() {
			// TODO Auto-generated method stub
			return new QueryMasterSpringDao(dataSource, dataSourceLookup);
		}

		public DataSourceLookup getDataSourceLookup() {
			return dataSourceLookup;
		}

		public DataSourceLookup getOriginalDataSourceLookup() {
			return orignalDataSourceLookup;
		}

		public IQueryResultInstanceDao getPatientSetResultDAO() {
			return new QueryResultInstanceSpringDao(dataSource,
					dataSourceLookup);

		}

		public IQueryInstanceDao getQueryInstanceDAO() {
			return new QueryInstanceSpringDao(dataSource, dataSourceLookup);
		}

		public IQueryStatusTypeDao getQueryStatusTypeDao() {
			return new QueryStatusTypeSpringDao(dataSource, dataSourceLookup);
		}

		public IXmlResultDao getXmlResultDao() {
			return new XmlResultSpringDao(dataSource, dataSourceLookup);
		}

	}

}
