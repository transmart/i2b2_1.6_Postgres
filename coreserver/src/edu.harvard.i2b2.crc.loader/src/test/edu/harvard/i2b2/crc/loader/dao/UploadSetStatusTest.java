package edu.harvard.i2b2.crc.loader.dao;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.crc.loader.datavo.loader.DataSourceLookup;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadSetStatus;
import edu.harvard.i2b2.crc.loader.datavo.loader.UploadStatus;
import edu.harvard.i2b2.crc.loader.util.CRCLoaderUtil;

public class UploadSetStatusTest { // extends
									// AbstractAnnotationAwareTransactionalTests
									// {
	UploadStatusDAOI mockDao = null;
	UploadSetStatus setStatus = null;
	private static UploadStatusDAO statusDao = null;
	static DataSource dataSource = null;
	static DataSourceLookup dataSourceLookup = null;
	UploadStatus uploadStatus = null;
	private static int uploadId = 0;

	@BeforeClass
	public static void createUploadStatus() throws Exception {
		UploadStatus uploadStatus = new UploadStatus();
		uploadStatus.setInputFileName("testfile");
		uploadStatus.setUserId("test_user");
		uploadStatus.setUploadLabel("test_label");
		uploadStatus.setSourceCd("test_source_cd");
		uploadStatus.setLoadDate(new Date());
		CRCLoaderUtil crcLoaderUtil = CRCLoaderUtil.getInstance();
		dataSourceLookup = (DataSourceLookup) crcLoaderUtil
				.getSpringBeanFactory().getBean("TestDataSourceLookup");
		// instanciate datasource
		dataSource = CRCLoaderUtil.getInstance().getSpringDataSource(
				dataSourceLookup.getDataSource());

		UploadStatusDAO uploadStatusDao = new UploadStatusDAO(dataSourceLookup,
				dataSource);
		uploadId = uploadStatusDao.insertUploadStatus(uploadStatus);
	}

	@Before
	public void setup() throws Exception {
		mockDao = createMock(UploadStatusDAOI.class);
		setStatus = new UploadSetStatus();
		System.out.println("Upload id =[" + uploadId + "]");
		setStatus.setUploadId(uploadId);
		setStatus.setSetTypeId(1);
		setStatus.setSourceCd("test_source_cd");
		setStatus.setLoadDate(new Date());
		statusDao = new UploadStatusDAO(dataSourceLookup, dataSource);

	}

	@Test
	public void createUploadSetStatus() throws Exception {
		statusDao.insertUploadSetStatus(setStatus);

	}

	@Test
	public void updateSetUploadStatus() {
		setStatus.setLoadStatus("Completed1");
		statusDao.updateUploadSetStatus(setStatus);
	}

	@Test
	public void getSetUploadStatusByLoadId() {
		List<UploadSetStatus> setList = statusDao
				.getUploadSetStatusByLoadId(uploadId);
		assertTrue(setList.size() > 0);
	}

}
