
// Generated file. Do not edit.
// Generated by org.xdoclet.plugin.ejb.interfaces.LocalHomeInterfacePlugin from edu.harvard.i2b2.crc.ejb.QueryManagerBean
package edu.harvard.i2b2.crc.ejb;

/**
 * Local-Home interface for querytool.QueryManager
 */
public interface QueryManagerLocalHome
extends javax.ejb.EJBLocalHome {
    public static final String COMP_NAME = "java:comp/env/ejb/querytool/QueryManagerLocal";
    public static final String JNDI_NAME = "querytool/QueryManagerLocal";

    edu.harvard.i2b2.crc.ejb.QueryManagerLocal create() throws javax.ejb.CreateException;
// ----------------------------------------------------------------
// Define your custom append code in a file called local-home-custom.vm
// and place it in your merge directory.
// ----------------------------------------------------------------
}
