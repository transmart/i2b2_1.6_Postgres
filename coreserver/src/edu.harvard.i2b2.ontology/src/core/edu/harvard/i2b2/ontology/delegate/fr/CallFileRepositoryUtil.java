package edu.harvard.i2b2.ontology.delegate.fr;

import java.io.StringReader;
import java.io.StringWriter;

import javax.activation.FileDataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.ontology.datavo.fr.ObjectFactory;
import edu.harvard.i2b2.ontology.datavo.fr.SendfileRequestType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.ResultStatusType;
import edu.harvard.i2b2.ontology.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.ontology.util.OntologyJAXBUtil;
import edu.harvard.i2b2.ontology.util.OntologyUtil;

/**
 * 
 * @author rk903
 * 
 */
public class CallFileRepositoryUtil {

	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	private SecurityType securityType = null;
	private String projectId = null;
	private String frUrl = null;
	private OntologyUtil ontologyUtil = OntologyUtil.getInstance();

	public CallFileRepositoryUtil(String requestXml) throws JAXBUtilException,
			I2B2Exception {
		JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil()
				.unMashallFromString(requestXml);
		RequestMessageType request = (RequestMessageType) responseJaxb
				.getValue();
		this.securityType = request.getMessageHeader().getSecurity();
		this.projectId = request.getMessageHeader().getProjectId();
		this.frUrl = ontologyUtil.getFileManagentCellUrl();

	}

	public CallFileRepositoryUtil(SecurityType securityType, String projectId)
			throws I2B2Exception {
		this.securityType = securityType;
		this.projectId = projectId;
		this.frUrl = ontologyUtil.getFileManagentCellUrl();
		log.debug("file repository url " + frUrl);
	}

	public CallFileRepositoryUtil(String frUrl, SecurityType securityType,
			String projectId) throws I2B2Exception {
		this(securityType, projectId);
		this.frUrl = frUrl;
	}

	public String callFileRepository(String fileRepositoryFileName)
			throws I2B2Exception {
		String localFileName = null;
		RequestMessageType requestMessageType = getI2B2RequestMessage(fileRepositoryFileName);
		OMElement requestElement = buildOMElement(requestMessageType);
		log.debug("FileRespository request message ["
				+ requestElement.toString() + "]");
		// MessageContext response = getResponseSOAPBody(requestElement);
		uploadConceptFile(fileRepositoryFileName);
		return fileRepositoryFileName;
	}

	private ResultStatusType getI2B2ResponseStatus(OMElement response)
			throws JAXBUtilException {
		JAXBElement responseJaxb = OntologyJAXBUtil.getJAXBUtil()
				.unMashallFromString(response.toString());
		ResponseMessageType r = (ResponseMessageType) responseJaxb.getValue();
		return r.getResponseHeader().getResultStatus();
	}

	private OMElement buildOMElement(RequestMessageType requestMessageType)
			throws I2B2Exception {
		StringWriter strWriter = new StringWriter();
		edu.harvard.i2b2.ontology.datavo.i2b2message.ObjectFactory hiveof = new edu.harvard.i2b2.ontology.datavo.i2b2message.ObjectFactory();
		OMElement request = null;
		try {
			OntologyJAXBUtil.getJAXBUtil().marshaller(
					hiveof.createRequest(requestMessageType), strWriter);
			StringReader strReader = new StringReader(strWriter.toString());
			XMLInputFactory xif = XMLInputFactory.newInstance();
			XMLStreamReader reader = xif.createXMLStreamReader(strReader);
			StAXOMBuilder builder = new StAXOMBuilder(reader);
			request = builder.getDocumentElement();
		} catch (XMLStreamException xmlEx) {
			throw new I2B2Exception("FileRepository request omelement failed ["
					+ xmlEx.getMessage() + "]");
		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("FileRepository request omelement failed ["
					+ jaxbEx.getMessage() + "]");
		}
		return request;
	}

	private RequestMessageType getI2B2RequestMessage(String sendFileName) {

		MessageHeaderType messageHeaderType = (MessageHeaderType) ontologyUtil
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
		SendfileRequestType sendfileRequestType = new SendfileRequestType();
		edu.harvard.i2b2.ontology.datavo.fr.File sendFile = new edu.harvard.i2b2.ontology.datavo.fr.File();
		sendFile.setName(sendFileName);
		sendfileRequestType.setUploadFile(sendFile);

		RequestMessageType requestMessageType = new RequestMessageType();
		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createSendfileRequest(sendfileRequestType));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}

	private void uploadConceptFile(String conceptFile) throws I2B2Exception {
		try {
			Options options = new Options();
			options.setTo(new EndpointReference(frUrl));
			options.setAction("urn:sendfileRequest");
			options
					.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

			// Increase the time out to receive large attachments
			options.setTimeOutInMilliSeconds(10000);

			options.setProperty(Constants.Configuration.ENABLE_SWA,
					Constants.VALUE_TRUE);
			options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,
					Constants.VALUE_TRUE);
			options.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR,
					"temp");
			options.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD,
					"4000");

			ServiceClient sender = FileRepositoryServiceClient
					.getServiceClient();
			sender.setOptions(options);
			OperationClient mepClient = sender
					.createClient(ServiceClient.ANON_OUT_IN_OP);

			MessageContext mc = new MessageContext();
			javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(
					new FileDataSource(conceptFile));

			mc.addAttachment("cid", dataHandler);
			mc.setDoingSwA(true);

			SOAPFactory sfac = OMAbstractFactory.getSOAP11Factory();
			SOAPEnvelope env = sfac.getDefaultEnvelope();

			/*
			 * OMNamespace omNs = sfac.createOMNamespace(
			 * "http://www.i2b2.org/xsd", "swa"); OMElement idEle =
			 * sfac.createOMElement("attchmentID", omNs); idEle.setText("cid");
			 * 
			 * env.getBody().addChild(idEle);
			 */
			RequestMessageType requestMessageType = getI2B2RequestMessage(conceptFile);
			OMElement requestElement = buildOMElement(requestMessageType);
			log.debug("File repository request message from ontology ["
					+ requestElement + "]");
			env.getBody().addChild(requestElement);

			// SOAPEnvelope env = createEnvelope("fileattachment");
			mc.setEnvelope(env);
			mepClient.addMessageContext(mc);
			mepClient.execute(true);

			MessageContext response = mepClient
					.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			SOAPBody body = response.getEnvelope().getBody();

			response = mepClient
					.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			log.debug("File Repository response envelope: "
					+ response.toString() + "]");
			OMElement frResponse = (OMElement) response.getEnvelope().getBody()
					.getFirstOMChild();
			log.debug("File Repository response body [: " + frResponse + "]");
			// read header status
			ResultStatusType resultStatusType = getI2B2ResponseStatus(frResponse);

			// if the status type is error, then throw i2b2exception
			if (resultStatusType.getStatus() != null
					&& resultStatusType.getStatus().getType() != null
					&& resultStatusType.getStatus().getType().equalsIgnoreCase(
							"error")) {
				String errorMsg = resultStatusType.getStatus().getValue();

				throw new I2B2Exception(
						"Unable to send file to file repository [" + errorMsg
								+ "]");
			}

		} catch (AxisFault axisFault) {
			throw new I2B2Exception(
					"Unable to send file to file repository :Axisfault ["
							+ axisFault.getCause().getMessage() + "]");
		} catch (Throwable t) {
			t.printStackTrace();
			throw new I2B2Exception(
					"Unable to send file to file repository :Axisfault ["
							+ t.getMessage() + "]");
		}
	}

	private MessageContext getResponseSOAPBody(OMElement requestElement)
			throws I2B2Exception {
		MessageContext response = null;
		// call
		ServiceClient serviceClient = FileRepositoryServiceClient
				.getServiceClient();

		Options options = new Options();
		String frOperationName = OntologyUtil.getInstance()
				.getFileRepositoryOperationName();
		if (frOperationName == null) {
			throw new I2B2Exception(
					"File Repository operation property missing from the property file");
		}
		log.debug("File Repository operation property value ["
				+ frOperationName + "]");
		options.setAction(frOperationName);
		options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
		String timeout = OntologyUtil.getInstance().getFileRepositoryTimeout();
		log.debug("File Repository timeout property value [" + timeout + "]");
		// Increase the time out to receive large attachments
		options.setTimeOutInMilliSeconds(Integer.parseInt(timeout));
		options.setProperty(Constants.Configuration.CACHE_ATTACHMENTS,
				Constants.VALUE_TRUE);
		options.setProperty(Constants.Configuration.ATTACHMENT_TEMP_DIR,
				ontologyUtil.getFileRepositoryTempSpace());
		options.setProperty(Constants.Configuration.FILE_SIZE_THRESHOLD,
				ontologyUtil.getFileRepositoryThreshold());
		options.setTo(new EndpointReference(frUrl));
		serviceClient.setOptions(options);

		try {
			OperationClient mepClient = serviceClient
					.createClient(ServiceClient.ANON_OUT_IN_OP);
			MessageContext mc = new MessageContext();

			SOAPFactory sfac = OMAbstractFactory.getSOAP11Factory();
			SOAPEnvelope env = sfac.getDefaultEnvelope();
			env.getBody().addChild(requestElement);

			// SOAPEnvelope env = createEnvelope("fileattachment");
			mc.setEnvelope(env);
			// mc.addAttachment("contentID",dataHandler);
			mc.setDoingSwA(true);
			mepClient.addMessageContext(mc);
			mepClient.execute(true);
			response = mepClient
					.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			log.debug("File Repository response envelope: "
					+ response.toString() + "]");
			OMElement frResponse = (OMElement) response.getEnvelope().getBody()
					.getFirstOMChild();
			log.debug("File Repository response body [: " + frResponse + "]");
			// read header status
			ResultStatusType resultStatusType = getI2B2ResponseStatus(frResponse);

			// if the status type is error, then throw i2b2exception
			if (resultStatusType.getStatus() != null
					&& resultStatusType.getStatus().getType() != null
					&& resultStatusType.getStatus().getType().equalsIgnoreCase(
							"error")) {
				String errorMsg = resultStatusType.getStatus().getValue();

				throw new I2B2Exception(
						"Unable to fetch file from file repository ["
								+ errorMsg + "]");
			}

			OperationContext operationContext = mc.getOperationContext();
			MessageContext outMessageContext = operationContext
					.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
			log
					.debug("File Repository response has ["
							+ outMessageContext.getAttachmentMap()
									.getAllContentIDs().length
							+ "] attachments");
		} catch (AxisFault axisFault) {
			throw new I2B2Exception(
					"Unable to fetch file from file repository :Axisfault ["
							+ axisFault.getCause().getMessage() + "]");
		} catch (JAXBUtilException jaxbUtilEx) {
			throw new I2B2Exception(
					"Unable to fetch file from file repository :Axisfault ["
							+ jaxbUtilEx.getMessage() + "]");
		} catch (Throwable t) {
			t.printStackTrace();
			throw new I2B2Exception(
					"Unable to fetch file from file repository :Axisfault ["
							+ t.getMessage() + "]");
		}
		return response;

	}

}
