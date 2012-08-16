/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Mike Mendis
 * 		Raj Kuttan
 */
package edu.harvard.i2b2.pm.ws;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PMServiceClient {
	private static Log log = LogFactory.getLog(PMServiceClient.class.getName());
	
    private static ServiceClient sender = null;
	private PMServiceClient() { 
	}
	
	
	public static  ServiceClient getServiceClient() throws AxisFault{
		if (sender == null) {
			try {
				sender = new ServiceClient();
			} catch (AxisFault e) {
				log.error(e.getMessage());
				throw e;
			}
		}
		return sender;
	}
	
}
