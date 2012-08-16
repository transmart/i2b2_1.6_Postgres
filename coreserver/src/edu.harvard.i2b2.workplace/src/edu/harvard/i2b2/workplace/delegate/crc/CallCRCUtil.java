package edu.harvard.i2b2.workplace.delegate.crc;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.CrcXmlResultResponseType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.InstanceRequestType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.InstanceResponseType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.InstanceResultResponseType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.ItemType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.MasterRequestType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.MasterResponseType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.PanelType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.PsmQryHeaderType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.PsmRequestTypeType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.QueryDefinitionRequestType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.QueryDefinitionType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.ResponseType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.ResultOutputOptionListType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.ResultOutputOptionType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.ResultRequestType;
import edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.ResultResponseType;

import edu.harvard.i2b2.workplace.datavo.i2b2message.ApplicationType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.workplace.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.workplace.util.WorkplaceJAXBUtil;
import edu.harvard.i2b2.workplace.util.WorkplaceUtil;

public class CallCRCUtil {

	private SecurityType securityType = null;
	private String projectId = null;
	private String crcUrl = null;
	WorkplaceUtil workplaceUtil = WorkplaceUtil.getInstance();

	private static Log log = LogFactory.getLog(CallCRCUtil.class);

	public CallCRCUtil(SecurityType securityType, String projectId)
			throws I2B2Exception {
		this.securityType = securityType;
		this.projectId = projectId;
		this.crcUrl = workplaceUtil.getCRCUrl();
		log.debug("CRC Workplace call url: " + crcUrl);
	}

	public CallCRCUtil(String crcUrl, SecurityType securityType,
			String projectId) throws I2B2Exception {
		this.securityType = securityType;
		this.projectId = projectId;
		this.crcUrl = crcUrl;
	}
/*
	public MasterInstanceResultResponseType callSetfinderQuery2(String conceptKey) throws I2B2Exception {
		RequestMessageType requestMessageType = this.buildSetfinderQueryRequestMessage(conceptKey);
		MasterInstanceResultResponseType masterInstanceResultResponseType = null;
		try {
			OMElement requestElement = buildOMElement(requestMessageType);
			log.debug("CRC Workplace call's request xml "
					+ requestElement);
			OMElement response = getServiceClient("/request")
					.sendReceive(requestElement);
			log.debug("CRC Workplace call's reponse xml " + response);
			masterInstanceResultResponseType = getMasterInstanceResultResponseMessage(response
					.toString());
			
		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("Error in CRC upload ", jaxbEx);
		} catch (XMLStreamException e) {
			throw new I2B2Exception("Error in CRC upload ", e);

		} catch (AxisFault e) {

			throw new I2B2Exception("Error in CRC upload ", e);
		}
		return masterInstanceResultResponseType;
	}
	*/
	
	public String callCRCResultInstanceXML(String resultInstanceID)
	throws I2B2Exception {
//		ResultResponseType resultResponseType = null;
		OMElement response = null;
		try {
			log.debug("begin build element");
			RequestMessageType requestMessageType = this.buildResultInstanceRequestXMLRequestMessage(resultInstanceID);
			OMElement requestElement = buildOMElement(requestMessageType);
			log.debug("callCRCQueryRequestXML - CRC setfinder query request XML call's request xml "
					+ requestElement);
			 response = getServiceClient("/request")
					.sendReceive(requestElement);
			log.debug("callCRCQueryRequestXML - CRC setfinder query request XML call's response xml " + response.toString());
			//resultResponseType = getResultResponseMessage(response.toString());
			//masterInstanceResultResponseType = getResponseMessage(response
			//		.toString());

		} catch (JAXBUtilException jaxbEx) {
			log.error(jaxbEx.getLocalizedMessage());
			throw new I2B2Exception("Error in CRC upload ", jaxbEx);
		} catch (XMLStreamException e) {
			log.error(e.getLocalizedMessage());
			throw new I2B2Exception("Error in CRC upload ", e);
		
		} catch (Exception e) {
		log.error(e.getLocalizedMessage());
			throw new I2B2Exception("Error in CRC upload ", e);
		}
		return response.toString();
	}

	public String callCRCQueryRequestXML(String queryMasterId)
	throws I2B2Exception {
//		ResultResponseType resultResponseType = null;
		//MasterResponseType masterInstanceResultResponseType = null;
		OMElement response = null;
		try {
			log.debug("begin build element");
			RequestMessageType requestMessageType = this.buildSetfinderRequestXMLRequestMessage(queryMasterId);
			OMElement requestElement = buildOMElement(requestMessageType);
			log.debug("callCRCQueryRequestXML - CRC setfinder query request XML call's request xml "
					+ requestElement);
			response = getServiceClient("/request")
					.sendReceive(requestElement);
			//log.debug("callCRCQueryRequestXML - CRC setfinder query request XML call's response xml " + response.toString());
			//resultResponseType = getResultResponseMessage(response.toString());
			//masterInstanceResultResponseType = getMasterInstanceResultResponseMessage(response
			//		.toString());

		} catch (JAXBUtilException jaxbEx) {
			log.error(jaxbEx.getLocalizedMessage());
			throw new I2B2Exception("Error in CRC upload ", jaxbEx);
		} catch (XMLStreamException e) {
			log.error(e.getLocalizedMessage());
			throw new I2B2Exception("Error in CRC upload ", e);
		
		} catch (Exception e) {
		log.error(e.getLocalizedMessage());
			throw new I2B2Exception("Error in CRC upload ", e);
		}
		return response.toString();
	}
	

//	public RequestMessageType buildSetfinderStatusRequestMessage(String queryInstanceId) {
	public RequestMessageType buildResultInstanceRequestXMLRequestMessage(String resultInstanceId) {
		ResultRequestType masterRequestType = new ResultRequestType();
		masterRequestType.setQueryResultInstanceId(resultInstanceId);

		MessageHeaderType messageHeaderType = new MessageHeaderType();
		ApplicationType appType = new ApplicationType();
		appType.setApplicationName("Workplace Cell");
		appType.setApplicationVersion("1.601");
		messageHeaderType.setSendingApplication(appType);

		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);

		RequestMessageType requestMessageType = new RequestMessageType();
		
		edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.ObjectFactory();
		BodyType bodyType = new BodyType();
		PsmQryHeaderType psm = new PsmQryHeaderType();
		psm.setRequestType(PsmRequestTypeType.CRC_QRY_GET_RESULT_DOCUMENT_FROM_RESULT_INSTANCE_ID); //.CRC_QRY_GET_QUERY_RESULT_INSTANCE_LIST_FROM_QUERY_INSTANCE_ID);
		bodyType.getAny().add(of.createPsmheader(psm));
		
		bodyType.getAny().add(of.createRequest(masterRequestType));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(3000);
		requestMessageType.setRequestHeader(requestHeader);
		return requestMessageType;
	}

	public RequestMessageType buildSetfinderRequestXMLRequestMessage(String queryMasterId) {
		MasterRequestType masterRequestType = new MasterRequestType();
		masterRequestType.setQueryMasterId(queryMasterId);

		MessageHeaderType messageHeaderType = new MessageHeaderType();
		ApplicationType appType = new ApplicationType();
		appType.setApplicationName("Workplace Cell");
		appType.setApplicationVersion("1.601");
		messageHeaderType.setSendingApplication(appType);

		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);

		RequestMessageType requestMessageType = new RequestMessageType();
		
		edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.ObjectFactory();
		BodyType bodyType = new BodyType();
		PsmQryHeaderType psm = new PsmQryHeaderType();
		psm.setRequestType(PsmRequestTypeType.CRC_QRY_GET_REQUEST_XML_FROM_QUERY_MASTER_ID); //.CRC_QRY_GET_QUERY_RESULT_INSTANCE_LIST_FROM_QUERY_INSTANCE_ID);
		bodyType.getAny().add(of.createPsmheader(psm));
		
		bodyType.getAny().add(of.createRequest(masterRequestType));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(3000);
		requestMessageType.setRequestHeader(requestHeader);
		return requestMessageType;
	}
	/*
	public RequestMessageType buildSetfinderQueryRequestMessage(String itemKey) {
		QueryDefinitionType queryDef = new QueryDefinitionType();
		PanelType panelType = new PanelType();
		ItemType itemType = new ItemType();
		itemType.setItemKey(itemKey);
		panelType.getItem().add(itemType);
		queryDef.getPanel().add(panelType);
		
		queryDef.setQueryName(itemKey.substring(0,(itemKey.length()>10)?9:itemKey.length()) + System.currentTimeMillis());
		QueryDefinitionRequestType queryDefinitionRequestType = new QueryDefinitionRequestType();
		ResultOutputOptionListType resultOutputOptionListType = new ResultOutputOptionListType();
		ResultOutputOptionType resultOutputOptionType = new ResultOutputOptionType();
		resultOutputOptionType.setName("PATIENT_COUNT_XML");
		resultOutputOptionListType.getResultOutput().add(resultOutputOptionType);
		queryDefinitionRequestType.setQueryDefinition(queryDef);
		queryDefinitionRequestType.setResultOutputList(resultOutputOptionListType);

		MessageHeaderType messageHeaderType = new MessageHeaderType();
		ApplicationType appType = new ApplicationType();
		appType.setApplicationName("Workplace Cell");
		appType.setApplicationVersion("1.601");
		messageHeaderType.setSendingApplication(appType);
				
				//(MessageHeaderType) workplaceUtil
				//.getSpringBeanFactory().getBean("message_header");
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);

		RequestMessageType requestMessageType = new RequestMessageType();
		edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.ObjectFactory of = new edu.harvard.i2b2.workplace.datavo.crc.setfinder.query.ObjectFactory();
		BodyType bodyType = new BodyType();
		PsmQryHeaderType psm = new PsmQryHeaderType();
		psm.setRequestType(PsmRequestTypeType.CRC_QRY_RUN_QUERY_INSTANCE_FROM_QUERY_DEFINITION);
		bodyType.getAny().add(of.createPsmheader(psm));
		bodyType.getAny().add(of.createRequest(queryDefinitionRequestType));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(3000);
		requestMessageType.setRequestHeader(requestHeader);
		return requestMessageType;
	}
	*/

	private CrcXmlResultResponseType getResponseMessage(
			String responseXml) throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = WorkplaceJAXBUtil.getJAXBUtil()
				.unMashallFromString(responseXml);
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		log.debug("CRC's workplace call response xml" + responseXml);

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ResultStatusType rt = r.getResponseHeader().getResultStatus();
		if (rt.getStatus().getType().equals("ERROR")) {
			throw new I2B2Exception(rt.getStatus().getValue());
		}
		CrcXmlResultResponseType masterInstanceResultResponseType = (CrcXmlResultResponseType) helper
				.getObjectByClass(r.getMessageBody().getAny(),
						CrcXmlResultResponseType.class);
		log.debug("got CrcXmlResultResponseType: " + masterInstanceResultResponseType);
		return masterInstanceResultResponseType;
	}
	
	private MasterResponseType getMasterInstanceResultResponseMessage(
			String responseXml) throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = WorkplaceJAXBUtil.getJAXBUtil()
				.unMashallFromString(responseXml);
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		log.debug("CRC's workplace call response xml" + responseXml);

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ResultStatusType rt = r.getResponseHeader().getResultStatus();
		if (rt.getStatus().getType().equals("ERROR")) {
			throw new I2B2Exception(rt.getStatus().getValue());
		}
		MasterResponseType masterInstanceResultResponseType = (MasterResponseType) helper
				.getObjectByClass(r.getMessageBody().getAny(),
						MasterResponseType.class);
		log.debug("got MasterInstanceResultResponseType");
		return masterInstanceResultResponseType;
	}
	
	/*
	private ResultResponseType getResultResponseMessage(
			String responseXml) throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = WorkplaceJAXBUtil.getJAXBUtil()
				.unMashallFromString(responseXml);
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		log.debug("CRC's workplace call response xml" + responseXml);

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ResultStatusType rt = r.getResponseHeader().getResultStatus();
		if (rt.getStatus().getType().equals("ERROR")) {
			throw new I2B2Exception(rt.getStatus().getValue());
		}
		ResultResponseType instanceResultResponseType = (ResultResponseType) helper
				.getObjectByClass(r.getMessageBody().getAny(),
						ResultResponseType.class);
		
		return instanceResultResponseType;
	}
	*/

	private OMElement buildOMElement(RequestMessageType requestMessageType)
			throws XMLStreamException, JAXBUtilException {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.workplace.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.workplace.datavo.i2b2message.ObjectFactory();
		WorkplaceJAXBUtil.getJAXBUtil().marshaller(
				hiveof.createRequest(requestMessageType), strWriter);
		// getOMElement from message
		OMFactory fac = OMAbstractFactory.getOMFactory();

		StringReader strReader = new StringReader(strWriter.toString());
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);
		StAXOMBuilder builder = new StAXOMBuilder(reader);
		OMElement request = builder.getDocumentElement();
		return request;
	}

	private ServiceClient getServiceClient(String operationName) {
		// call
		ServiceClient serviceClient = CRCServiceClient.getServiceClient();

		Options options = new Options();
		options.setTo(new EndpointReference(crcUrl + operationName));
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		options.setProperty(Constants.Configuration.ENABLE_REST,
				Constants.VALUE_TRUE);
		options.setTimeOutInMilliSeconds(0);
		serviceClient.setOptions(options);
		return serviceClient;

	}

}
