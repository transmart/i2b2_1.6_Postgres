/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors: 
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.delegate.pdo;

import javax.ejb.CreateException;

import edu.harvard.i2b2.crc.datavo.pdo.PatientDataType;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.ServiceLocatorException;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.pdo.query.GetObservationFactByPrimaryKeyRequestType;
import edu.harvard.i2b2.crc.delegate.RequestHandler;
import edu.harvard.i2b2.crc.ejb.PdoQueryLocal;
import edu.harvard.i2b2.crc.ejb.PdoQueryLocalHome;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;


/**
 * GetObservationFactFromPrimaryKeyHandler class.
 * $Id: GetObservationFactFromPrimaryKeyHandler.java,v 1.8 2008/07/21 19:56:56 rk903 Exp $
 * @author rkuttan
 */
public class GetObservationFactFromPrimaryKeyHandler extends RequestHandler {
    private GetObservationFactByPrimaryKeyRequestType getObservationFactByPrimaryKeyRequestType =
        null;

    /**
     * Constuctor which accepts i2b2 request message xml
     * @param requestXml
     * @throws I2B2Exception
     */
    public GetObservationFactFromPrimaryKeyHandler(String requestXml)
        throws I2B2Exception {
        try {
            this.getObservationFactByPrimaryKeyRequestType = (GetObservationFactByPrimaryKeyRequestType) this.getRequestType(requestXml,
                    edu.harvard.i2b2.crc.datavo.pdo.query.GetObservationFactByPrimaryKeyRequestType.class);
            this.setDataSourceLookup(requestXml);
        } catch (JAXBUtilException jaxbUtilEx) {
            throw new I2B2Exception("Error ", jaxbUtilEx);
        }
    }

    /**
     * Perform operation for the given request and
     * return response
     */
    public BodyType execute() throws I2B2Exception {
        // call ejb and pass input object
        QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
        String responseString = null;
        BodyType bodyType = new BodyType();
        PatientDataType patientDataType = null;
        try {
            PdoQueryLocalHome pdoQueryLocalHome = qpUtil.getPdoQueryLocalHome();
            PdoQueryLocal pdoQueryInfoLocal = pdoQueryLocalHome.create();
            patientDataType = pdoQueryInfoLocal.getObservationFactByPrimaryKey(getDataSourceLookup(),getObservationFactByPrimaryKeyRequestType);
            
            //ResponseMessageType responseMessageType = new ResponseMessageType();
            //responseMessageType.setMessageBody(bodyType);
            //responseString = this.getResponseString(responseMessageType);
       } catch (ServiceLocatorException e) {
            throw new I2B2Exception("", e);
        } catch (CreateException e) {
            throw new I2B2Exception("", e);
        } finally { 
        	edu.harvard.i2b2.crc.datavo.pdo.query.PatientDataResponseType pdoResponseType = new edu.harvard.i2b2.crc.datavo.pdo.query.PatientDataResponseType();
            pdoResponseType.setPatientData(patientDataType);
        	edu.harvard.i2b2.crc.datavo.pdo.query.ObjectFactory objectFactory = new edu.harvard.i2b2.crc.datavo.pdo.query.ObjectFactory();
            bodyType.getAny()
                    .add(objectFactory.createResponse(pdoResponseType));
        }

        return bodyType;
    }
}
