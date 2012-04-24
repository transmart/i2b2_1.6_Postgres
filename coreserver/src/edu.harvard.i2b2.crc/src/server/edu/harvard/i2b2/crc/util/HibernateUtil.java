/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.*;

import org.hibernate.cfg.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 * Helper class to get hibernate session
 * Before returining session, it sets filter
 * for delete flag for query tables
 * $Id: HibernateUtil.java,v 1.3 2007/08/31 14:52:49 rk903 Exp $
 * @author rkuttan
 */
public class HibernateUtil {
    /** log **/
    protected static final Log log = LogFactory.getLog(HibernateUtil.class);
    private static final String DATASOURCE_JNDI_NAME = "java:/hibernate/QueryToolSessionFactory";

    /**
     * Function to fetch hibernate session
     * outside of app server
     * USE INSIDE TEST CLASSES ONLY TO RUN TEST OUTSIDE APPSERVER
     */
    public static SessionFactory getSessionFactory() {
        SessionFactory sessionFactory = null;

        try {
            // Create the SessionFactory from hibernate.cfg.xml
            sessionFactory = new Configuration().configure()
                                                .buildSessionFactory();
        } catch (Throwable ex) {
        	log.error("Error creating session factory", ex);
            // Make sure you log the exception, as it might be swallowed
            throw new ExceptionInInitializerError(ex);
        }

        return sessionFactory;
    }

    /**
     * Function to fetch session via jboss hibernate mbean
     * Enables filter condition for delete_flag in query
     * tables
     * @return Session
     */
    public static Session getSession() {
        Session session = null;

        InitialContext ctx;
        SessionFactory factory;

        try {
            ctx = new InitialContext();
            factory = (SessionFactory) ctx.lookup(DATASOURCE_JNDI_NAME);
            // session = factory.openSession();
            session = factory.getCurrentSession();
            session.enableFilter("deleteInstanceFlagFilter")
                   .setParameter("deleteFlagFilterParam", "N");
            session.enableFilter("deleteMasterFlagFilter")
                   .setParameter("deleteFlagFilterParam", "N");
            session.enableFilter("deleteResultInstanceFlagFilter")
                   .setParameter("deleteFlagFilterParam", "N");
        } catch (NamingException e) {
            log.error("DB Session jndi lookup[" + DATASOURCE_JNDI_NAME +
                "] failed", e);
            throw new ExceptionInInitializerError(e);
        }

        return session;
    }
}
