package edu.harvard.i2b2.crc.loader.irod;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.junit.Test;

import edu.sdsc.grid.io.FileFactory;
import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.irods.IRODSAccount;
import edu.sdsc.grid.io.irods.IRODSFile;
import edu.sdsc.grid.io.irods.IRODSFileSystem;
import edu.sdsc.grid.io.local.LocalFile;

public class FileTransferTest {
	
	

	@Test
	public void put() { 
	// Convert to filesystem URL does not seem to work

		try { 
		String uriLoc = "";
		
		String username = uriLoc.substring(uriLoc.indexOf('/') + 2, uriLoc.indexOf('.'));
		String mdas = uriLoc.substring(uriLoc.indexOf('.') + 1, uriLoc.indexOf('@'));
		String host = uriLoc.substring(uriLoc.indexOf('@') + 1, uriLoc.indexOf(':',5));
		int port = Integer.parseInt( uriLoc.substring(uriLoc.indexOf(':',5) + 1, uriLoc.indexOf('/', 7)));
		String file = uriLoc.substring(uriLoc.indexOf('/', 7));
		String password="";
		String storageResource = "demoResc";
		String destDir = "/tmp";
		
		IRODSAccount irodsAccount = new IRODSAccount(
				host, port, username, password,
				"/", mdas, storageResource) ;


			
			
		GeneralFile source = null;
		try {
			IRODSFileSystem irodsFileSystem = new IRODSFileSystem(irodsAccount);

			//URI uri = new URI( uriLoc ); 
			//source = FileFactory.newFile( uri, password);
			source = new IRODSFile(irodsFileSystem, file);
			if (destDir == null)
				source.copyTo( new LocalFile(source.getName() ), true );
			else
				source.copyTo( new LocalFile(destDir + java.io.File.separator +source.getName() ), true );
			irodsFileSystem.close();
		} catch (IOException ioe) {
			System.out.println("========== IOException from SrbService: downloadTheFile " + ioe.getMessage());
			
		}
		
		System.out.println(source.getName());
		} catch (Exception e) { 
			e.printStackTrace();
		}
	}
	
	@Test
	public void get() throws Exception { 
		

		GeneralFile source = null;
		//FileFactory.newFile(arg0)

       /*
		
		try {
			//monitor.beginTask(f.getName() + " on the server side ",(int) f.length());
			//actFileList.copyTo( new LocalFile(f.getAbsoluteFile()), true );

			String file = uri.substring(uri.indexOf('/', 7));

			source = new LocalFile(f.getAbsoluteFile());
			source.copyTo(new IRODSFile(conn,file), overwrite);
		} catch () { 
			
		}
			
		*/
	}
	
}
