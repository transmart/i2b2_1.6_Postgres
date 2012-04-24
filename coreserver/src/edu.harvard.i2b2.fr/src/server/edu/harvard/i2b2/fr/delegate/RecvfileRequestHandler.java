package edu.harvard.i2b2.fr.delegate;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;

import edu.harvard.i2b2.fr.datavo.FRJAXBUtil;
import edu.harvard.i2b2.fr.datavo.fr.query.RecvfileRequestType;
import edu.harvard.i2b2.fr.datavo.fr.query.RecvfileResponseType;
import edu.harvard.i2b2.fr.datavo.i2b2message.BodyType;
import edu.harvard.i2b2.fr.datavo.i2b2message.MessageHeaderType;
import edu.harvard.i2b2.fr.datavo.pm.CellDataType;
import edu.harvard.i2b2.fr.datavo.pm.CellDatasType;
import edu.harvard.i2b2.fr.datavo.pm.ConfigureType;
import edu.harvard.i2b2.fr.util.MD5;


public class RecvfileRequestHandler extends RequestHandler {

	RecvfileRequestType recvFileRequest = null;
	MessageHeaderType messageHeaderType = null;
	OMElement requestElement = null;

	String filename = "";
	protected ConfigureType pmResponseUserInfo = null;

	public RecvfileRequestHandler(String requestXml)
	throws I2B2Exception {
		try {
			recvFileRequest = (RecvfileRequestType) this.getRequestType(requestXml,
					edu.harvard.i2b2.fr.datavo.fr.query.RecvfileRequestType.class);
			messageHeaderType = getMessageHeaderType(requestXml); 

			requestElement = convertStringToOMElement(requestXml);
		} catch (Exception jaxbUtilEx) {
			throw new I2B2Exception("Error ", jaxbUtilEx);
		}
	}
	@Override
	public BodyType execute() throws Exception {
		edu.harvard.i2b2.fr.datavo.fr.query.ObjectFactory objectFactory = new edu.harvard.i2b2.fr.datavo.fr.query.ObjectFactory();

		BodyType bodyType = new BodyType();

		JAXBUtil jaxbUtil = FRJAXBUtil.getJAXBUtil();
		StringWriter strWriter = new StringWriter();
		try { 
			jaxbUtil.marshaller(objectFactory.createRecvfileRequest(recvFileRequest), strWriter);
		} catch (JAXBUtilException jaxbEx) {
			throw new I2B2Exception("Error in marshalling publishdata request",jaxbEx);
		}
		filename = recvFileRequest.getFilename(); //.getLoadId();
		log.debug("My filename is :" + filename);
		String projectId  = messageHeaderType.getProjectId();//.getSecurity();

		log.debug("My project is :" + projectId);

		if ((filename == null) || (filename.length() == 0))
				throw new I2B2Exception("Filename is empty");

		if (filename.startsWith(java.io.File.separator))
		{
			filename = filename.substring(1);
		}
			
			
		CellDatasType celldatas = pmResponseUserInfo.getCellDatas();

		String destDir = getCellDataParam("FRC", "destdir");

		if (destDir == null)
			throw new I2B2Exception("Unable to get 'destdir' from File Repository Cell(FRC) param data");

		//Verify file
		filename = destDir+ java.io.File.separatorChar + projectId + java.io.File.separatorChar + filename;
		File recvFile = new File(filename);//destDir+ java.io.File.separatorChar + projectId + java.io.File.separatorChar + filename);
		if (recvFile.exists())
		{

			
			RecvfileResponseType response = new RecvfileResponseType();
			edu.harvard.i2b2.fr.datavo.fr.query.File file = new edu.harvard.i2b2.fr.datavo.fr.query.File(); 

			file.setName(recvFile.getName());
			file.setDesc(recvFile.getAbsolutePath());
			file.setSize(BigInteger.valueOf(recvFile.length()));
			file.setHash(MD5.asHex(MD5.getHash(recvFile)));
			Date date = new Date (recvFile.lastModified());
			//file.setDate(new XMLGregorianCalendar (date.getYear(), date.getMonth(), date.getDay(), date.getTimezoneOffset()));
			response.setRecvfileResponse(file);
			bodyType.getAny()
			.add(objectFactory.createRecvfileResponse(response));

		} else
		{
			filename = "";
			throw new Exception("File " + filename + " does not exist");

		}
		return bodyType;

	}

	public ConfigureType getPmResponseUserInfo() {
		return pmResponseUserInfo;
	}

	public void setPmResponseUserInfo(ConfigureType pmResponseUserInfo) {
		this.pmResponseUserInfo = pmResponseUserInfo;
	}
	
	public String getCellDataParam(String id, String name) {
		for (CellDataType cellData :pmResponseUserInfo.getCellDatas().getCellData())
		{
			for (edu.harvard.i2b2.fr.datavo.pm.ParamType param :cellData.getParam())
			{
				log.debug(param.getName());
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
	public String getFilename() {
		return filename;
	}


}
