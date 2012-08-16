package edu.harvard.i2b2.plugin.pb.delegate.pm;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

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
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.crc.datavo.i2b2message.FacilityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.RequestMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseHeaderType;
import edu.harvard.i2b2.crc.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.crc.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.datavo.i2b2message.StatusType;
import edu.harvard.i2b2.crc.datavo.pm.ConfigureType;
import edu.harvard.i2b2.crc.datavo.pm.GetUserConfigurationType;
import edu.harvard.i2b2.crc.datavo.pm.ObjectFactory;
import edu.harvard.i2b2.crc.datavo.pm.ProjectType;
import edu.harvard.i2b2.crc.datavo.pm.UserType;
import edu.harvard.i2b2.plugin.pb.datavo.CRCJAXBUtil;
import edu.harvard.i2b2.plugin.pb.util.QueryProcessorUtil;

public class CallPMUtil {

	private SecurityType securityType = null;
	private String projectId = null;
	private String ontologyUrl = null;

	private static Log log = LogFactory.getLog(CallPMUtil.class);

	public CallPMUtil(String requestXml) throws JAXBUtilException,
			I2B2Exception {
		this(QueryProcessorUtil.getInstance().getOntologyUrl(), requestXml);
	}

	public CallPMUtil(String ontologyUrl, String requestXml)
			throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil()
				.unMashallFromString(requestXml);
		RequestMessageType request = (RequestMessageType) responseJaxb
				.getValue();
		this.securityType = request.getMessageHeader().getSecurity();
		this.projectId = request.getMessageHeader().getProjectId();
		this.ontologyUrl = ontologyUrl;
	}

	public CallPMUtil(SecurityType securityType, String projectId)
			throws I2B2Exception {
		this.securityType = securityType;
		this.projectId = projectId;
		this.ontologyUrl = QueryProcessorUtil.getInstance()
				.getProjectManagementCellUrl();
		log.debug("CRC PM call url" + ontologyUrl);
	}

	public CallPMUtil(String ontologyUrl, SecurityType securityType,
			String projectId) throws I2B2Exception {
		this.securityType = securityType;
		this.projectId = projectId;
		this.ontologyUrl = ontologyUrl;
	}

	public ProjectType callUserProject() throws AxisFault, I2B2Exception {
		RequestMessageType requestMessageType = getI2B2RequestMessage();
		OMElement requestElement = null;
		ProjectType projectType = null;
		try {
			requestElement = buildOMElement(requestMessageType);
			log.debug("CRC PM call's request xml " + requestElement);
			OMElement response = getServiceClient().sendReceive(requestElement);
			projectType = getUserProjectFromResponse(response.toString());
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		} catch (JAXBUtilException e) {
			e.printStackTrace();
			throw new I2B2Exception("" + StackTraceUtil.getStackTrace(e));
		}
		return projectType;
	}

	private ProjectType getUserProjectFromResponse(String responseXml)
			throws JAXBUtilException, I2B2Exception {
		JAXBElement responseJaxb = CRCJAXBUtil.getJAXBUtil()
				.unMashallFromString(responseXml);
		ResponseMessageType pmRespMessageType = (ResponseMessageType) responseJaxb
				.getValue();
		log.debug("CRC's PM call response xml" + responseXml);

		ResponseHeaderType responseHeader = pmRespMessageType
				.getResponseHeader();
		StatusType status = responseHeader.getResultStatus().getStatus();
		String procStatus = status.getType();
		String procMessage = status.getValue();

		if (procStatus.equals("ERROR")) {
			log.info("PM Error reported by CRC web Service " + procMessage);
			throw new I2B2Exception("PM Error reported by CRC web Service "
					+ procMessage);
		} else if (procStatus.equals("WARNING")) {
			log.info("PM Warning reported by CRC web Service" + procMessage);
			throw new I2B2Exception("PM Warning reported by CRC web Service"
					+ procMessage);
		}

		JAXBUnWrapHelper helper = new JAXBUnWrapHelper();
		ConfigureType configureType = (ConfigureType) helper.getObjectByClass(
				pmRespMessageType.getMessageBody().getAny(),
				ConfigureType.class);
		UserType userType = configureType.getUser();
		List<ProjectType> projectTypeList = userType.getProject();

		ProjectType projectType = null;
		if (projectTypeList != null && projectTypeList.size() > 0) {
			for (ProjectType pType : projectTypeList) {
				if (pType.getId().equalsIgnoreCase(projectId)) {
					projectType = pType;

					break;
				}
			}
			if (projectType == null) {
				throw new I2B2Exception("User not registered to the project["
						+ projectId + "]");
			}
		}

		return projectType;
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

	private RequestMessageType getI2B2RequestMessage() {
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
		// GetUserInfoType getUserInfoType = null;
		GetUserConfigurationType userConfig = new GetUserConfigurationType();
		userConfig.getProject().add(projectId);

		RequestMessageType requestMessageType = new RequestMessageType();
		ObjectFactory of = new ObjectFactory();
		BodyType bodyType = new BodyType();
		bodyType.getAny().add(of.createGetUserConfiguration(userConfig));
		requestMessageType.setMessageBody(bodyType);

		requestMessageType.setMessageHeader(messageHeaderType);

		RequestHeaderType requestHeader = new RequestHeaderType();
		requestHeader.setResultWaittimeMs(180000);
		requestMessageType.setRequestHeader(requestHeader);

		return requestMessageType;

	}

	private ServiceClient getServiceClient() {
		// call
		ServiceClient serviceClient = PMServiceClient.getServiceClient();

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
