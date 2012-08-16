package edu.harvard.i2b2.crc.ejb;

//import com.bm.testsuite.BaseSessionBeanFixture;

import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.ejb.analysis.StartAnalysis;
import edu.harvard.i2b2.crc.ejb.analysis.StartAnalysisLocal;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = { "file:/Users/rk903/old_mac_folder/jboss-4.2.2.GA/server/default/conf/crcloader/CRCLoaderApplicationContext.xml" })
public class StartAnalysisTest { // extends BaseSessionBeanFixture<StartAnalysis> {
	// extends AbstractTransactionalJUnit4SpringContextTests {

	private static String testFileDir = null;
	private static DataSourceLookup dataSourceLookup = null;
	private static String requestXml = null;

	private static DAOFactoryHelper daoHelper = null;

	// @Autowired
	// StartAnalysis startAnalysis;

	private static final Class[] usedBeans = {};

	public StartAnalysisTest() {
	//	super(StartAnalysis.class, usedBeans);

	}

	// 

	/*
	 * @BeforeClass public void setUp() throws Exception { testFileDir =
	 * System.getProperty("testfiledir"); System.out.println("test file dir " +
	 * testFileDir);
	 * 
	 * if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
	 * throw new Exception(
	 * "please provide test file directory info -Dtestfiledir"); }
	 * 
	 * QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
	 * dataSourceLookup = (DataSourceLookup) qpUtil.getSpringBeanFactory()
	 * .getBean("TestDataSourceLookup");
	 * System.out.println(dataSourceLookup.getDataSource());
	 * System.out.println(dataSourceLookup.getFullSchema());
	 * 
	 * // instanciate datasource DataSource dataSource =
	 * CRCLoaderUtil.getInstance()
	 * .getSpringDataSource(dataSourceLookup.getDataSource()); daoHelper = new
	 * DAOFactoryHelper(dataSourceLookup, dataSource);
	 * 
	 * String filename = testFileDir + "/setfinder_analysis_query.xml";
	 * requestXml = CRCAxisAbstract.getQueryString(filename);
	 * 
	 * }
	 */

	public void testStartAnalysis() throws Exception {
	//	final StartAnalysisLocal toTest = this.getBeanToTest();
	//	toTest.start(daoHelper.getDAOFactory(), requestXml);

	}
}
