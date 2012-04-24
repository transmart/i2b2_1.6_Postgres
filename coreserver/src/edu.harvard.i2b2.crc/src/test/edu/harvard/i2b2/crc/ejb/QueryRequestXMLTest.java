package edu.harvard.i2b2.crc.ejb;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.setfinder.query.MasterInstanceResultResponseType;
import edu.harvard.i2b2.crc.ejb.QueryManagerRemote;
import edu.harvard.i2b2.crc.ejb.QueryManagerRemoteHome;



public class QueryRequestXMLTest  {

	public static void main(String args[]) {
		QueryRequestXMLTest xmlTest = new QueryRequestXMLTest();
		//xmlTest.testPublishPatientRequestXMLTest();
		xmlTest.testPublishPatientRemote();
	}

	public String getQueryString() throws Exception  { 
		StringBuffer queryStr = new StringBuffer();
		DataInputStream dataStream = new DataInputStream(new FileInputStream("test1.xml"));
		while(dataStream.available()>0) {
			queryStr.append(dataStream.readLine() + "\n");
		}
		System.out.println("queryStr" + queryStr);
		return queryStr.toString();	
	}
	
	
	
	public void testPublishPatientRemote() {
		// String publishRequest = buildPublishPatientXML();
		// call remote ejb
		try {
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY,"org.jnp.interfaces.NamingContextFactory");
		env.put(Context.PROVIDER_URL, "localhost");
		env.put(Context.URL_PKG_PREFIXES,"org.jboss.naming:org.jnp.interfaces");
		Context ctx = null;
		
			ctx = new InitialContext(env);
			QueryManagerRemoteHome ejbHome;
		
			ejbHome = (QueryManagerRemoteHome) ctx.lookup("ejb.querytool.QueryManager");
			
			// Use one of the create() methods below to create a new instance
			QueryManagerRemote mySessionEJB;

			mySessionEJB = ejbHome.create();

			// Call any of the Remote methods below to access the EJB
			
			
			System.out.println("Start Time"	+ new Date(System.currentTimeMillis()));

			DataSourceLookup dataSourceLookup = new DataSourceLookup();
			MasterInstanceResultResponseType masterResponse  = mySessionEJB.processQuery(dataSourceLookup,getQueryString());
			//convert masterInstance to string
			String response = masterResponse.toString();
			System.out.print("Response " + response);

			System.out.println("End Time" + new Date(System.currentTimeMillis()));
			
		} catch (Exception e) {
			e.printStackTrace();
		} 

	}
}
