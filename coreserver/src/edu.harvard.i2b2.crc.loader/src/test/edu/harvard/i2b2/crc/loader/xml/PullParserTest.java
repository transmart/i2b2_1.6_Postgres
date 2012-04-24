package edu.harvard.i2b2.crc.loader.xml;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.datavo.pdo.ConceptType;
import edu.harvard.i2b2.crc.loader.datavo.pdo.EventType;
import edu.harvard.i2b2.crc.loader.datavo.pdo.ObservationType;
import edu.harvard.i2b2.crc.loader.xml.StartElementListener;
import edu.harvard.i2b2.crc.loader.xml.TypePullParser;

public class PullParserTest {
	private String inputFileName = "/patient_data.xml";
	private static String testFileDir = "";

	@BeforeClass
	public static void init() throws Exception {
		testFileDir = System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}
	}

	@Test
	public void testObservation() throws I2B2Exception {
		StartElementListener handler = new StartElementListener() {
			public void process(Object object) {
				ObservationType obsType = (ObservationType) object;
				System.out.println("observation "
						+ obsType.getPatientId().getValue());
			}
		};
		TypePullParser obsParser = new TypePullParser(handler, "observation",
				ObservationType.class, testFileDir + inputFileName);
		obsParser.doParsing();

	}

	@Test
	public void testConcept() throws I2B2Exception {
		StartElementListener handler = new StartElementListener() {
			public void process(Object object) {
				ConceptType conceptType = (ConceptType) object;
				System.out.println("concpet " + conceptType.getConceptCd());
			}
		};
		TypePullParser obsParser = new TypePullParser(handler, "concept",
				ConceptType.class, testFileDir + inputFileName);
		obsParser.doParsing();

	}

	@Test
	public void testEvent() throws I2B2Exception {
		StartElementListener handler = new StartElementListener() {
			public void process(Object object) {
				EventType eventType = (EventType) object;
				System.out
						.println("event " + eventType.getEventId().getValue());
			}
		};
		TypePullParser obsParser = new TypePullParser(handler, "event",
				EventType.class, testFileDir + inputFileName);
		obsParser.doParsing();

	}

}
