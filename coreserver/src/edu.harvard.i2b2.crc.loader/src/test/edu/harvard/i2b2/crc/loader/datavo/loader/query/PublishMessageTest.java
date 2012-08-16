package edu.harvard.i2b2.crc.loader.datavo.loader.query;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBElement;

import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;

public class PublishMessageTest {

	private static final String filename = "testfiles/publish_data.xml"; 
	private PublishDataRequestType publishType = null;
	
	public static String getQueryString(String inputFileName) throws Exception  { 
		StringBuffer queryStr = new StringBuffer();
		InputStreamReader dataStream = new InputStreamReader(new FileInputStream(inputFileName));
		BufferedReader reader = new BufferedReader(dataStream);
		String singleLine = null;
		while((singleLine = reader.readLine())!=null) {
			queryStr.append(singleLine + "\n");
		}
		System.out.println("queryStr" + queryStr);
		return queryStr.toString();	
	}
	
	@Test
	public void unMarshallTest() throws Exception { 
		unMarshall();
		assertNotNull("Not null check for PublishDataRequestType",publishType);
		assertNotNull("Check Inputlist's DataFile not null",publishType.getInputList().getDataFile());
		assertNotNull("Check Loadlist not null",publishType.getLoadList());
		assertNotNull("Check Outputlist's not null",publishType.getOutputList());
		assertEquals("Check loadlabel",publishType.getInputList().getDataFile().getLoadLabel(),"load_label0");
	}
	
	
	@Test
	public void marshallTest() throws Exception { 
		JAXBUtil jaxbUtil = edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil.getJAXBUtil();
		StringWriter strWriter = new StringWriter();
		unMarshall();
		ObjectFactory of = new ObjectFactory();
		jaxbUtil.marshaller(of.createPublishDataRequest(publishType), strWriter);
		assertTrue("Check if marshalled xml length is >0",strWriter.toString().length()>0);
	}
	
	
	private void unMarshall() throws Exception { 
		String fileContent = getQueryString(filename);
		assertNotNull(fileContent);
		JAXBUtil jaxbUtil = edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil.getJAXBUtil();
		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(fileContent);
		publishType = (PublishDataRequestType) jaxbElement.getValue();
	}
}
