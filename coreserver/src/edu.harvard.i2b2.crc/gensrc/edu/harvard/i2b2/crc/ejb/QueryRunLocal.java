
// Generated file. Do not edit.
// Generated by org.xdoclet.plugin.ejb.interfaces.LocalInterfacePlugin from edu.harvard.i2b2.crc.ejb.QueryRunBean
package edu.harvard.i2b2.crc.ejb;

/**
 * Local interface for querytool.QueryRun
 */
public interface QueryRunLocal 
extends javax.ejb.EJBLocalObject {
    edu.harvard.i2b2.crc.datavo.setfinder.query.InstanceResponseType getQueryInstanceFromMasterId(edu.harvard.i2b2.crc.datavo.db.DataSourceLookup dataSourceLookup, java.lang.String userId, edu.harvard.i2b2.crc.datavo.setfinder.query.MasterRequestType masterRequestType) throws edu.harvard.i2b2.common.exception.I2B2DAOException;
// ----------------------------------------------------------------
// Define your custom append code in a file called local-custom.vm 
// and place it in your merge directory.
// ----------------------------------------------------------------
}
