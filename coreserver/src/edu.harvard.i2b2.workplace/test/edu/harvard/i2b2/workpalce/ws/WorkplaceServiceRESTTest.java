package edu.harvard.i2b2.workpalce.ws;

import static org.junit.Assert.*;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBElement;

import junit.framework.JUnit4TestAdapter;

import org.apache.axiom.om.OMElement;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.util.jaxb.JAXBUnWrapHelper;
import edu.harvard.i2b2.workplace.datavo.i2b2message.ResponseMessageType;
import edu.harvard.i2b2.workplace.datavo.wdo.FolderType;
import edu.harvard.i2b2.workplace.datavo.wdo.FoldersType;
import edu.harvard.i2b2.workplace.util.WorkplaceJAXBUtil;

public class WorkplaceServiceRESTTest extends WorkplaceAxisAbstract{
	private static String testFileDir = "";

	private static String workplaceTargetEPR = 
			"http://localhost:9090/i2b2/rest/WorkplaceService/getFoldersByProject";			
	//	"http://127.0.0.1:8080/i2b2/services/PMService/getServices";			

	public static junit.framework.Test suite() { 
		return new JUnit4TestAdapter(WorkplaceServiceRESTTest.class);
	}


	@BeforeClass
	public static void setUp() throws Exception {
		testFileDir = "test"; //System.getProperty("testfiledir");
		System.out.println("test file dir " + testFileDir);

		if (!((testFileDir != null) && (testFileDir.trim().length() > 0))) {
			throw new Exception(
					"please provide test file directory info -Dtestfiledir");
		}

	}

	@Test
	public void GetFoldersByProject() throws Exception {
		String filename = testFileDir + "/folders_by_project.xml";
		String masterInstanceResult = null;
		try { 
			String requestString = getQueryString(filename);
			OMElement requestElement = convertStringToOMElement(requestString); 
			OMElement responseElement = getServiceClient(workplaceTargetEPR).sendReceive(requestElement);
			JAXBElement responseJaxb = WorkplaceJAXBUtil.getJAXBUtil().unMashallFromString(responseElement.toString());
			ResponseMessageType r = (ResponseMessageType)responseJaxb.getValue();
			JAXBUnWrapHelper helper = new  JAXBUnWrapHelper();

			FoldersType folders = (FoldersType)helper.getObjectByClass(r.getMessageBody().getAny(),FoldersType.class);
			for(FolderType folder: folders.getFolder())
			{
				if (folder.getName().equals("demo"))
					assertEquals(folder.getName(),"demo");
				if (folder.getName().equals("SHARED"))
					assertEquals(folder.getName(),"SHARED");
			}
			assertNotNull(folders);
			assertTrue(folders.getFolder().size() > 1);



		} catch (Exception e) { 
			e.printStackTrace();
			assertTrue(false);
		}
	}
	

}





