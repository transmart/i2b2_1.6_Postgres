package edu.harvard.i2b2.crc.loader.datavo.pdo;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;

public class PDOTest {

	PatientDataType pdoType = null;

	private String filename = "testfiles/patient_data.xml";

	@Test
	public void CommonPDO() throws Exception {
		JAXBUtil jaxbUtil = edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil
				.getJAXBUtil();
		StringWriter strWriter = new StringWriter();
		unMarshall();
		ObjectFactory of = new ObjectFactory();
		jaxbUtil.marshaller(of.createPatientData(pdoType), strWriter);
		assertTrue("Check if marshalled xml length is >0", strWriter.toString()
				.length() > 0);
	}

	@Test
	public void marshallObservation() throws Exception {
		JAXBUtil jaxbUtil = edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil
				.getJAXBUtil();
		StringWriter strWriter = new StringWriter();
		ObservationType observation = new ObservationType();
		ObservationSet observationSet = new ObservationSet();
		observationSet.getObservation().add(observation);
		ObjectFactory of = new ObjectFactory();
		jaxbUtil.marshaller(new JAXBElement(new QName("", "observationset"),
				ObservationSet.class, observationSet), strWriter);
		// jaxbUtil.marshaller(n, strWriter)
		String xml = strWriter.toString();
		String observationStr = xml.substring(xml.indexOf('>', xml
				.indexOf("observationset")) + 1, xml
				.indexOf("</observationset"));
		System.out.println(observationStr);

	}

	private void unMarshall() throws Exception {
		String fileContent = getQueryString(filename);
		assertNotNull(fileContent);
		JAXBUtil jaxbUtil = edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil
				.getJAXBUtil();
		JAXBElement jaxbElement = jaxbUtil.unMashallFromString(fileContent);
		pdoType = (PatientDataType) jaxbElement.getValue();
		assertTrue("check size of observation >0 ", pdoType.getObservationSet()
				.size() > 0);
	}

	public static String getQueryString(String inputFileName) throws Exception {
		StringBuffer queryStr = new StringBuffer();
		InputStreamReader dataStream = new InputStreamReader(
				new FileInputStream(inputFileName));
		BufferedReader reader = new BufferedReader(dataStream);
		String singleLine = null;
		while ((singleLine = reader.readLine()) != null) {
			queryStr.append(singleLine + "\n");
		}
		System.out.println("queryStr" + queryStr);
		return queryStr.toString();
	}

}
