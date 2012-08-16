package edu.harvard.i2b2.crc.loader.util.csv;

import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBElement;

import org.junit.Ignore;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUtil;
import edu.harvard.i2b2.crc.loader.datavo.CRCLoaderJAXBUtil;
import edu.harvard.i2b2.crc.loader.datavo.pdo.PatientDataType;

public class PatientDataXmlBuilderTest {
	private String inputFileName = "testfiles/csv/ccp_synonyms_dis.csv";;
	private String outputXmlFileName = null;
	private String conceptCodePrefix = null;
	private String sourceSystemCd = null;
	private String encounterSource = null;
	String outputDir = "";

	@Test
	public void testBuildObservationXML() throws Exception {
		outputXmlFileName = outputDir
				+ "/PatientDateXmlBuilderTest_testBuildObservationXML"
				+ System.currentTimeMillis();
		ObservationFactCSV2XmlBuilder observationBuilder = new ObservationFactCSV2XmlBuilder(
				inputFileName, outputXmlFileName);
		observationBuilder.buildXml();
		// read the output file and count the obervations
		JAXBUtil jaxbUtil = CRCLoaderJAXBUtil.getJAXBUtil();
		JAXBElement jaxbElement = jaxbUtil
				.unMashallerRequest(outputXmlFileName);
		PatientDataType patientData = (PatientDataType) jaxbElement.getValue();
		assertTrue(patientData.getObservationSet().get(0).getObservation()
				.size() > 0);
	}

	@Ignore
	@Test
	public void testBuildEventXML() throws Exception {
		outputXmlFileName = outputDir
				+ "/PatientDateXmlBuilderTest_testBuildEventXML"
				+ System.currentTimeMillis();
		VisitCSV2XmlBuilder eventBuilder = new VisitCSV2XmlBuilder(
				inputFileName, outputXmlFileName);

		eventBuilder.buildXml();
		// read the output file and count the events

	}

}
