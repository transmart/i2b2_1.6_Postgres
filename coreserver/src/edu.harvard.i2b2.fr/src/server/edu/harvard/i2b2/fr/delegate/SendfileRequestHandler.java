package edu.harvard.i2b2.fr.delegate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.MessageContext;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

import edu.harvard.i2b2.fr.datavo.FRJAXBUtil;
import edu.harvard.i2b2.fr.datavo.fr.query.SendfileRequestType;
import edu.harvard.i2b2.fr.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.fr.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.fr.datavo.pm.CellDataType;
import edu.harvard.i2b2.fr.datavo.pm.CellDatasType;
import edu.harvard.i2b2.fr.datavo.pm.ConfigureType;
import edu.harvard.i2b2.fr.util.MD5;

public class SendfileRequestHandler extends RequestHandler {

	SendfileRequestType sendfileRequest = null;
	MessageHeaderType messageHeaderType = null;

	String requestXml = null;
	//protected String irodsDefaultStorageResource = null;

	protected ConfigureType pmResponseUserInfo = null;
	public SendfileRequestHandler(String request) throws I2B2Exception {
		try {
			requestXml = request;
			sendfileRequest = (SendfileRequestType) this
			.getRequestType(
					requestXml,
					edu.harvard.i2b2.fr.datavo.fr.query.SendfileRequestType.class);
			messageHeaderType = getMessageHeaderType(requestXml);
		} catch (JAXBUtilException jaxbUtilEx) {
			throw new I2B2Exception("Error ", jaxbUtilEx);
		}
	}

	@Override
	public BodyType execute() throws I2B2Exception {
		edu.harvard.i2b2.fr.datavo.fr.query.ObjectFactory objectFactory = new edu.harvard.i2b2.fr.datavo.fr.query.ObjectFactory();
		// call ejb and pass input object
		String responseString = null;
		BodyType bodyType = new BodyType();

		JAXBUtil jaxbUtil = FRJAXBUtil.getJAXBUtil();
		StringWriter strWriter = new StringWriter();

		try { 
			jaxbUtil.marshaller(objectFactory.createSendfileRequest(sendfileRequest), strWriter);
		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("Error in marshalling publishdata request",jaxbEx);
		}
		edu.harvard.i2b2.fr.datavo.fr.query.File filename = sendfileRequest.getUploadFile(); //.getLoadId();

		String projectId  = messageHeaderType.getProjectId();//.getSecurity();
		log.debug("My filename is :" + filename.getName());
		log.debug("My project is :" + projectId);


		CellDatasType celldatas = pmResponseUserInfo.getCellDatas();

		String destDir = getCellDataParam("FRC", "destdir");

		if (destDir == null)
			throw new I2B2Exception("Unable to get 'destdir' from File Repository Cell(FRC) param data");
		MessageContext msgCtx = MessageContext.getCurrentMessageContext();
		Attachments attachment = msgCtx.getAttachmentMap();
		log.debug("My attachment :" + attachment);
		DataHandler dataHandler = attachment.getDataHandler("cid");

		String[] ids = attachment.getAllContentIDs();

		log.debug("My dataHandler :" + dataHandler);
		File file = new File(destDir+ java.io.File.separatorChar + projectId + java.io.File.separatorChar + filename.getName());
		try {
			 if (file.exists() && (Boolean.valueOf(filename.getOverwrite()) ==false ))
				throw new I2B2Exception("File already exists");
				
			createFolder(file.getParent());

			FileOutputStream fileOutputStream = new FileOutputStream(file);
			dataHandler.writeTo(fileOutputStream);
			fileOutputStream.flush();
			fileOutputStream.close();
			log.debug("file saved as :" + file.getAbsolutePath());

			if ((filename.getHash() != null) && (filename.getAlgorithm().toUpperCase().equals("MD5")) &&
					(!(MD5.asHex(MD5.getHash(file)).equals(filename.getHash()))))
				throw new I2B2Exception("MD5 hash was not equal, got: " + filename.getHash() + 
						", but expected: " + MD5.asHex(MD5.getHash(file)));

			if ((filename.getSize() != null) &&
					(file.length() != filename.getSize().longValue() ))
				throw new I2B2Exception("Size was not equal, got: " + filename.getSize() + 
						", but expected: " + file.length());


		} catch (Exception jaxbEx) {
			//jaxbEx.printStackTrace();
			throw new I2B2Exception(jaxbEx.getMessage(),
					jaxbEx);
		}		

		log.debug("Done saving file");
		try {
			jaxbUtil.marshaller(objectFactory
					.createSendfileRequest(sendfileRequest), strWriter);
		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("Error in marshalling publishdata request",
					jaxbEx);
		}
		return bodyType;

	}

	public String getCellDataParam(String id, String name) {
		for (CellDataType cellData :pmResponseUserInfo.getCellDatas().getCellData())
		{
			for (edu.harvard.i2b2.fr.datavo.pm.ParamType param :cellData.getParam())
			{
				if (param.getName().toLowerCase().equals(name.toLowerCase()))
					return param.getValue();
			}
		}
		return null;
	}

	public static OMElement convertStringToOMElement(String requestXmlString) throws Exception { 
		StringReader strReader = new StringReader(requestXmlString);
		XMLInputFactory xif = XMLInputFactory.newInstance();
		XMLStreamReader reader = xif.createXMLStreamReader(strReader);

		StAXOMBuilder builder = new StAXOMBuilder(reader);
		OMElement lineItem = builder.getDocumentElement();
		return lineItem;
	}

	public ConfigureType getPmResponseUserInfo() {
		return pmResponseUserInfo;
	}

	public void setPmResponseUserInfo(ConfigureType pmResponseUserInfo) {
		this.pmResponseUserInfo = pmResponseUserInfo;
	}


	public  boolean createFolder(String path) {
		if (path == null) {
			log.error("createFolder() path=null");
			return false;
		}

		log.debug ("Creating a folder:" + path);
		File file = new File(path);

		if (! file.exists()) {
			return file.mkdirs();
		}
		else {
			log.debug("createFolder() path=" + path + " exists allready");
			return false;
		}
	}
}
