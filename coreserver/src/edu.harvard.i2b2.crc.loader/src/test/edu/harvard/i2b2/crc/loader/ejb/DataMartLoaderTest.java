package edu.harvard.i2b2.crc.loader.ejb;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.loader.dao.LoaderDAOFactoryHelper;
import edu.harvard.i2b2.crc.loader.datavo.i2b2message.PasswordType;
import edu.harvard.i2b2.crc.loader.datavo.i2b2message.SecurityType;
import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.loader.datavo.loader.query.PublishMessageTest;
import edu.harvard.i2b2.crc.loader.util.CRCLoaderUtil;

public class DataMartLoaderTest {

	private static final String filename = "/publish_data.xml";
	DataSource dataSource = null;
	DataSourceLookup dataSourceLookup = null;
	LoaderDAOFactoryHelper daoHelper = null;
	private static String testFileDir = null;

	@BeforeClass
	public static void init() throws Exception {
		testFileDir = System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}
	}

	public Hashtable getEnv() {
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"org.jnp.interfaces.NamingContextFactory");
		env.put(Context.PROVIDER_URL, "localhost:1099");
		env
				.put(Context.URL_PKG_PREFIXES,
						"org.jboss.naming:org.jnp.interfaces");
		return env;
	}

	@Before
	public void setup() throws I2B2Exception {
		CRCLoaderUtil crcLoaderUtil = CRCLoaderUtil.getInstance();
		dataSourceLookup = (DataSourceLookup) crcLoaderUtil
				.getSpringBeanFactory().getBean("TestDataSourceLookup");
		// instanciate datasource
		dataSource = CRCLoaderUtil.getInstance().getSpringDataSource(
				dataSourceLookup.getDataSource());
		daoHelper = new LoaderDAOFactoryHelper(dataSourceLookup);
	}

	@Ignore
	@Test
	public void testDataMartLoaderBean() throws Exception {
		InitialContext context = new InitialContext(getEnv());
		DataMartLoaderAsyncBeanRemote loaderBean = (DataMartLoaderAsyncBeanRemote) context
				.lookup("DataMartLoaderBean/remote");
		String requestString = PublishMessageTest.getQueryString(testFileDir
				+ filename);
		SecurityType securityType = new SecurityType();
		securityType.setUsername("test_user");
		PasswordType ptype = new PasswordType();
		ptype.setValue("test_user_password");

		securityType.setPassword(ptype);

		// load the file and build this jaxb object
		loaderBean.load(dataSourceLookup, requestString, securityType, 1000,
				null);
	}

	@Test
	public void testDataMartLoaderAsyncBean() throws Exception {
		InitialContext context = new InitialContext(getEnv());
		DataMartLoaderAsyncBeanRemote loaderBean = (DataMartLoaderAsyncBeanRemote) context
				.lookup("DataMartLoaderAsyncBean/remote");
		String requestString = PublishMessageTest.getQueryString(testFileDir
				+ filename);

		SecurityType securityType = new SecurityType();
		securityType.setUsername("test_user");
		PasswordType ptype = new PasswordType();
		ptype.setValue("test_user_password");
		securityType.setPassword(ptype);

		// load the file and build this jaxb object
		loaderBean.load(dataSourceLookup, requestString, securityType, 1000,
				null);

	}

}
