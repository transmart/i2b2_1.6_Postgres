package edu.harvard.i2b2.plugin.pb.dao;

import edu.harvard.i2b2.plugin.pb.dao.setfinder.IQueryInstanceDao;
import edu.harvard.i2b2.plugin.pb.dao.setfinder.IQueryMasterDao;
import edu.harvard.i2b2.plugin.pb.dao.setfinder.IQueryResultInstanceDao;
import edu.harvard.i2b2.plugin.pb.dao.setfinder.IQueryStatusTypeDao;
import edu.harvard.i2b2.plugin.pb.dao.setfinder.IXmlResultDao;
import edu.harvard.i2b2.plugin.pb.datavo.DataSourceLookup;

public interface SetFinderDAOFactory {
	public IQueryMasterDao getQueryMasterDAO();

	public IQueryInstanceDao getQueryInstanceDAO();

	public IQueryResultInstanceDao getPatientSetResultDAO();

	public IXmlResultDao getXmlResultDao();

	public IQueryStatusTypeDao getQueryStatusTypeDao();

	public DataSourceLookup getDataSourceLookup();

	public DataSourceLookup getOriginalDataSourceLookup();
}
