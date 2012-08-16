package edu.harvard.i2b2.crc.ejb.analysis;

import javax.ejb.Local;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.dao.IDAOFactory;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;

@Local
public interface StartAnalysisLocal {

	public MasterInstanceResultResponseType start(IDAOFactory daoFactory,
			String requestXml) throws I2B2Exception;

}