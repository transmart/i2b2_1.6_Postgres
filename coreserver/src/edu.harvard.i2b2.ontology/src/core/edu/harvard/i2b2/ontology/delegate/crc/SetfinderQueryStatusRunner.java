package edu.harvard.i2b2.ontology.delegate.crc;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.QueryStatusTypeType;
import edu.harvard.i2b2.ontology.datavo.crc.setfinder.query.ResultResponseType;

public class SetfinderQueryStatusRunner implements Runnable {

	private CallCRCUtil callCRCUtil = null;
	private String queryInstanceId = null;
	private ResultResponseType resultResponseType = null;
	private QueryStatusTypeType queryStatusType = null;
	private String statusType = null;
	private String exceptionMsg = null;
	boolean exitFlag = false;

	public void setCRCUtil(CallCRCUtil callCRCUtil) {
		this.callCRCUtil = callCRCUtil;
	}

	public void setQueryInstanceId(String queryInstanceId) {
		this.queryInstanceId = queryInstanceId;
	}

	public ResultResponseType getQueryInstanceStatusResponseType() {
		return resultResponseType;
	}

	public String getExceptionMsg() {
		return exceptionMsg;
	}

	public boolean isNotDone() {
		if (exitFlag == false) {
			return true;
		} else {
			return false;
		}

	}

	public void run() {
		
		while (exitFlag == false) {
			try {

				// send and get message
				resultResponseType = callCRCUtil
						.callCRCQueryStatus(queryInstanceId);
	
				// check if the response is completed or error
				queryStatusType = resultResponseType.getQueryResultInstance().get(0).getQueryStatusType();
				statusType = queryStatusType.getName();
				if (statusType.equalsIgnoreCase("COMPLETED")
						|| statusType.equalsIgnoreCase("DONE")
						|| statusType.equalsIgnoreCase("ERROR")) {

					exitFlag = true;
				}
				if (statusType.equalsIgnoreCase("ERROR")) {
					exceptionMsg = resultResponseType.getQueryResultInstance().get(0).getMessage().getContent().get(0).toString();
				}
			} catch (I2B2Exception i2b2Ex) {
				exceptionMsg = StackTraceUtil.getStackTrace(i2b2Ex);
				exitFlag = true;
			} catch (Throwable t) {
				exceptionMsg = StackTraceUtil.getStackTrace(t);
				exitFlag = true;
			}
			try {
				Thread.sleep(50000);
			} catch (InterruptedException e) {
				exitFlag = true;
				e.printStackTrace();
			}
		}
	}
}
