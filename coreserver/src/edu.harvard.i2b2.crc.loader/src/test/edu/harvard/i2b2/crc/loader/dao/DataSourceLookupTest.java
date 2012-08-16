package edu.harvard.i2b2.crc.loader.dao;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;

public class DataSourceLookupTest {

	private String domainId = null; 
	private String userId = null; 
	private String projectId = null;
	
	@Before
	public void setup() {
		domainId = "Demo123";
		userId = "mem61";
		projectId = "demo";
	}
	
	@Test
	public void testDataSourceLookup() throws Exception { 
		DataSourceLookupHelper dsHelper = new DataSourceLookupHelper();
		DataSourceLookup dataSourceLookup = dsHelper.matchDataSource(domainId, projectId, userId);
		assertNotNull(dataSourceLookup.getDataSource());
		assertNotNull(dataSourceLookup.getServerType());
		assertNotNull(dataSourceLookup.getFullSchema());
		System.out.print(dataSourceLookup.getDataSource());
	}
}
