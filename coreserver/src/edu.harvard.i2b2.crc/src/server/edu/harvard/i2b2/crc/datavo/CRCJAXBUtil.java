/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.datavo;

import java.util.List;

import org.springframework.beans.factory.BeanFactory;

import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

/**
 * Factory class to create jaxb context Since jaxb context is tread safe, only
 * one instance is created for this cell. The package used for jaxb context is
 * read from spring config file $Id: CRCJAXBUtil.java,v 1.6 2007/09/11 20:05:40
 * rk903 Exp $
 * 
 * @author rkuttan
 */
public class CRCJAXBUtil {
	private static edu.harvard.i2b2.common.util.jaxb.JAXBUtil jaxbUtil = null;
	private static edu.harvard.i2b2.common.util.jaxb.JAXBUtil queryDefjaxbUtil = null;
	private static edu.harvard.i2b2.common.util.jaxb.JAXBUtil analysisDefjaxbUtil = null;

	private CRCJAXBUtil() {
	}

	@SuppressWarnings("unchecked")
	public static edu.harvard.i2b2.common.util.jaxb.JAXBUtil getJAXBUtil() {
		if (jaxbUtil == null) {
			BeanFactory springBean = QueryProcessorUtil.getInstance()
					.getSpringBeanFactory();
			List jaxbPackageName = (List) springBean.getBean("jaxbPackage");
			String[] jaxbPackageNameArray = (String[]) jaxbPackageName
					.toArray(new String[] {

					});
			jaxbUtil = new edu.harvard.i2b2.common.util.jaxb.JAXBUtil(
					jaxbPackageNameArray);
		}

		return jaxbUtil;
	}

	@SuppressWarnings("unchecked")
	public static edu.harvard.i2b2.common.util.jaxb.JAXBUtil getQueryDefJAXBUtil() {
		if (queryDefjaxbUtil == null) {
			queryDefjaxbUtil = new edu.harvard.i2b2.common.util.jaxb.JAXBUtil(
					edu.harvard.i2b2.crc.datavo.setfinder.query.QueryDefinitionType.class);
		}
		return queryDefjaxbUtil;
	}

	@SuppressWarnings("unchecked")
	public static edu.harvard.i2b2.common.util.jaxb.JAXBUtil getAnalysisDefJAXBUtil() {
		if (analysisDefjaxbUtil == null) {
			analysisDefjaxbUtil = new edu.harvard.i2b2.common.util.jaxb.JAXBUtil(
					edu.harvard.i2b2.crc.datavo.setfinder.query.AnalysisDefinitionRequestType.class);
		}
		return analysisDefjaxbUtil;
	}
}
