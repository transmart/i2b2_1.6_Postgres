package edu.harvard.i2b2.plugin.pb.delegate.ontology;

import java.io.IOException;
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
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptsType;
import edu.harvard.i2b2.crc.datavo.ontology.GetChildrenType;
import edu.harvard.i2b2.crc.datavo.ontology.GetTermInfoType;
import edu.harvard.i2b2.crc.datavo.ontology.ObjectFactory;
import edu.harvard.i2b2.plugin.pb.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.plugin.pb.util.PMServiceAccountUtil;
import edu.harvard.i2b2.plugin.pb.util.QueryProcessorUtil;

public class CallOntologyUtil {

	private SecurityType securityType = null;
	private String projectId = null;
	private String ontologyUrl = null;

	private static Log log = LogFactory.getLog(CallOntologyUtil.class);

	public CallOntologyUtil(String requestXml) throws JAXBUtilException,
			I2B2Exception {
		this(QueryProcessorUtil.getInstance().getOntologyUrl(), requestXml);
	}

	public CallOntologyUtil(String ontologyUrl, String requestXml)
			throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil()
				.unMashallFromString(requestXml);
		RequestMessageType request = (RequestMessageType) responseJaxb
				.getValue();
		this.projectId = request.getMessageHeader().getProjectId();
		SecurityType tempSecurityType = request.getMessageHeader()
				.getSecurity();
		this.securityType = PMServiceAccountUtil
				.getServiceSecurityType(tempSecurityType.getDomain());
		this.ontologyUrl = ontologyUrl;
	}

	public CallOntologyUtil(SecurityType securityType, String projectId)
			throws I2B2Exception {
		this.securityType = securityType;
		this.projectId = projectId;
		this.ontologyUrl = QueryProcessorUtil.getInstance().getOntologyUrl();
		log.debug("CRC Ontology call url" + ontologyUrl);
	}

	public CallOntologyUtil(String ontologyUrl, SecurityType securityType,
			String projectId) throws I2B2Exception {
		this.securityType = securityType;
		this.projectId = projectId;
		this.ontologyUrl = ontologyUrl;
	}

	public ConceptType callOntology(String itemKey) throws XMLStreamException,
			JAXBUtilException, AxisFault, I2B2DAOException {
		RequestMessageType requestMessageType = getI2B2RequestMessage(itemKey);
		OMElement requestElement = buildOMElement(requestMessageType);
		log.debug("CRC Ontology call's request xml " + requestElement);
		OMElement response = getServiceClient().sendReceive(requestElement);
		ConceptType conceptType = getConceptFromResponse(response);
		return conceptType;
	}

	public ConceptsType callGetChildren(String itemKey)
			throws XMLStreamException, JAXBUtilException, AxisFault {
		RequestMessageType requestMessageType = getChildrenI2B2RequestMessage(itemKey);
		OMElement requestElement = buildOMElement(requestMessageType);
		log.debug("CRC Ontology call's request xml " + requestElement);
		OMElement response = getServiceClient().sendReceive(requestElement);
		ConceptsType conceptsType = getChildrenFromResponse(response.toString());
		return conceptsType;
	}

	public ConceptsType callGetChildrenWithHttpClient(String itemKey)
			throws XMLStreamException, JAXBUtilException {
		HttpClient client = new HttpClient();
		PostMethod postMethod = new PostMethod(this.ontologyUrl);

		client.setConnectionTimeout(8000);

		// Send any XML file as the body of the POST request

		// postMethod.setRequestBody(new FileInputStream(f));
		RequestMessageType requestMessageType = getChildrenI2B2RequestMessage(itemKey);
		String requestXml = buildRequestXml(requestMessageType);
		postMethod.setRequestBody(requestXml);
		postMethod.setRequestHeader("Content-type",
				"text/xml; charset=ISO-8859-1");
		String responseXml = null;
		try {
			int statusCode1 = client.executeMethod(postMethod);
			responseXml = postMethod.getResponseBodyAsString();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.debug("CRC's Ontology call response xml " + responseXml);

		postMethod.releaseConnection();
		ConceptsType conceptsType = getChildrenFromResponse(responseXml);
		return conceptsType;
	}

	private ConceptsType getChildrenFromResponse(String responseXml)
			throws JAXBUtilException {
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil()
				.unMashallFromString(responseXml);
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		log.debug("CRC's ontology call response xml" + responseXml);

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ConceptsType conceptsType = (ConceptsType) helper.getObjectByClass(r
				.getMessageBody().getAny(), ConceptsType.class);
		return conceptsType;
	}

	private ConceptType getConceptFromResponse(OMElement response)
			throws JAXBUtilException, I2B2DAOException {
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil()
				.unMashallFromString(response.toString());
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		log.debug("CRC's ontology call response xml" + response);

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ConceptsType conceptsType = (ConceptsType) helper.getObjectByClass(r
				.getMessageBody().getAny(), ConceptsType.class);
		if (conceptsType != null && conceptsType.getConcept() != null
				&& conceptsType.getConcept().size() > 0) {
			return conceptsType.getConcept().get(0);
		} else {
			return null;
		}

	}

	private OMElement buildOMElement(RequestMessageType requestMessageType)
			throws XMLStreamException, JAXBUtilException {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(
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

	private String buildRequestXml(RequestMessageType requestMessageType)
			throws XMLStreamException, JAXBUtilException {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.crc.datavo.i2b2message.ObjectFactory();
		CRCJAXBUtil.getJAXBUtil().marshaller(
				hiveof.createRequest(requestMessageType), strWriter);

		return strWriter.toString();
	}

	private RequestMessageType getI2B2RequestMessage(String conceptPath) {
		QueryProcessorUtil queryUtil = QueryProcessorUtil.getInstance();
		MessageHeaderType messageHeaderType = (MessageHeaderType) queryUtil
				.getSpringBeanFactory().getBean("message_header");
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);
		// build message body
		GetTermInfoType getTermInfo = new GetTermInfoType();
		getTermInfo.setSelf(conceptPath);
		// max="300" hiddens="false" synonyms="false" type="core" blob="true"
		getTermInfo.setMax(300);
		getTermInfo.setHiddens(false);
		getTermInfo.setSynonyms(false);
		getTermInfo.setType("core");
		getTermInfo.setBlob(true);

		RequestMessageType requestMessageType = new RequestMessageType();
		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createGetTermInfo(getTermInfo));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}

	private RequestMessageType getChildrenI2B2RequestMessage(String conceptPath) {
		QueryProcessorUtil queryUtil = QueryProcessorUtil.getInstance();
		MessageHeaderType messageHeaderType = (MessageHeaderType) queryUtil
				.getSpringBeanFactory().getBean("message_header");
		messageHeaderType.setSecurity(securityType);
		messageHeaderType.setProjectId(projectId);

		messageHeaderType.setReceivingApplication(messageHeaderType
				.getSendingApplication());
		FacilityType facilityType = new FacilityType();
		facilityType.setFacilityName("sample");
		messageHeaderType.setSendingFacility(facilityType);
		messageHeaderType.setReceivingFacility(facilityType);
		// build message body
		GetChildrenType getChildren = new GetChildrenType();
		getChildren.setParent(conceptPath);
		// max="300" hiddens="false" synonyms="false" type="core" blob="true"
		// getChildren.setMax(300);
		getChildren.setHiddens(false);
		getChildren.setSynonyms(false);
		// getChildren.setType("core");
		getChildren.setBlob(true);

		RequestMessageType requestMessageType = new RequestMessageType();
		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createGetChildren(getChildren));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}

	private ServiceClient getServiceClient() {
		// call
		ServiceClient serviceClient = OntologyServiceClient.getServiceClient();

		Options options = new Options();
		options.setTo(new EndpointReference(ontologyUrl));
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		options.setProperty(Constants.Configuration.ENABLE_REST,
				Constants.VALUE_TRUE);
		options.setTimeOutInMilliSeconds(50000);
		serviceClient.setOptions(options);
		return serviceClient;

	}

}
