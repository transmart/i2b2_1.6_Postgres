
// Generated file. Do not edit.
// Generated by org.xdoclet.plugin.ejb.interfaces.RemoteHomeInterfacePlugin from edu.harvard.i2b2.crc.ejb.QueryRunBean
package edu.harvard.i2b2.crc.ejb;

/**
 * Home interface for querytool.QueryRun
 */
public interface QueryRunRemoteHome
extends javax.ejb.EJBHome {
    public static final String COMP_NAME = "java:comp/env/ejb/querytool/QueryRunRemote";
    public static final String JNDI_NAME = "querytool/QueryRunRemote";

    edu.harvard.i2b2.crc.ejb.QueryRunRemote create() throws javax.ejb.CreateException, java.rmi.RemoteException;
// ----------------------------------------------------------------
// Define your custom append code in a file called home-custom.vm
// and place it in your merge directory.
// ----------------------------------------------------------------
}
