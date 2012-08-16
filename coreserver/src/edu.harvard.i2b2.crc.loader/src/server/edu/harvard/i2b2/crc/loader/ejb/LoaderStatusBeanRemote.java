package edu.harvard.i2b2.crc.loader.ejb;

import javax.ejb.Local;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataListResponseType;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.LoadDataResponseType;

@Local
public interface LoaderStatusBeanRemote extends ILoaderStatusBean{


}