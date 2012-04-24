package edu.harvard.i2b2.crc.loader.ejb;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.harvard.i2b2.crc.loader.dao.IUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.dao.OracleUploaderDAOFactory;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.loader.util.CRCLoaderUtil;

public class SetLoaderTest {

	IUploaderDAOFactory uploaderDaoFactory = null;
	String inputLoadFile;
	String inputLoadFileFormat;
	String encounterSource;
	String sourceSystemCd;
	int uploadId = 0;
	String testFileDir;

	@Before
	public void setup() throws Exception {
		testFileDir = System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}

		CRCLoaderUtil crcLoaderUtil = CRCLoaderUtil.getInstance();
		DataSourceLookup dataSourceLookup = (DataSourceLookup) crcLoaderUtil
				.getSpringBeanFactory().getBean("TestDataSourceLookup");
		DataSource dataSource = crcLoaderUtil
				.getSpringDataSource(dataSourceLookup.getDataSource());
		uploaderDaoFactory = new OracleUploaderDAOFactory(dataSourceLookup,
				dataSource);

		inputLoadFile = testFileDir + "/patient_data.xml";

		inputLoadFileFormat = "XML";
		encounterSource = "TESTENC";
		sourceSystemCd = "TESTSOU";
		uploadId = Math
				.abs(Long.valueOf(System.currentTimeMillis()).intValue());

	}

	@Ignore
	@Test
	public void conceptTest() throws Exception {
		ConceptLoader cl = new ConceptLoader(uploaderDaoFactory, inputLoadFile,
				inputLoadFileFormat, encounterSource, sourceSystemCd, false,
				uploadId);
		cl.load();
	}

	@Ignore
	@Test
	public void providerTest() throws Exception {
		ProviderLoader cl = new ProviderLoader(uploaderDaoFactory,
				inputLoadFile, inputLoadFileFormat, encounterSource,
				sourceSystemCd, false, uploadId);
		cl.load();
	}

	@Ignore
	@Test
	public void patientTest() throws Exception {
		PatientLoader pl = new PatientLoader(uploaderDaoFactory, inputLoadFile,
				inputLoadFileFormat, encounterSource, sourceSystemCd, uploadId);
		pl.load();
	}

	@Ignore
	@Test
	public void encounterTest() throws Exception {
		VisitLoader vl = new VisitLoader(uploaderDaoFactory, inputLoadFile,
				inputLoadFileFormat, encounterSource, sourceSystemCd, uploadId);
		vl.load();
	}

	@Test
	public void observationText() throws Exception {
		System.out.println("upload id " + uploadId);
		ObservationFactLoader ofl = new ObservationFactLoader(
				uploaderDaoFactory, inputLoadFile, inputLoadFileFormat,
				encounterSource, null, sourceSystemCd, false, uploadId);
		ofl.load();
	}

	@Ignore
	@Test
	public void pidText() throws Exception {

		PidLoader pidl = new PidLoader(uploaderDaoFactory, inputLoadFile,
				inputLoadFileFormat, encounterSource, sourceSystemCd, uploadId);
		pidl.load();
	}

	@Ignore
	@Test
	public void eidText() throws Exception {
		EidLoader pidl = new EidLoader(uploaderDaoFactory, inputLoadFile,
				inputLoadFileFormat, encounterSource, sourceSystemCd, uploadId);
		pidl.load();
	}

}
