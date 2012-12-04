package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DirectQueryForSinglePanel {

	protected final Log log = LogFactory.getLog(DirectQueryForSinglePanel.class);
	
	
	
	
	
	public DirectQueryForSinglePanel() {
		
	}
	
	
	public String buildSqlWithOR(String tempTableSql) { 
		
		String[] individualSql = tempTableSql.split("<\\*>");
		StringBuffer  convertedSqlBuffer = new StringBuffer();
		String individualSqlLowerCase = "";
		for (int i =0 ; i<individualSql.length-1;i++) { 
			individualSqlLowerCase = individualSql[i].toLowerCase();
			if (individualSqlLowerCase.indexOf("select")>0) { 
				convertedSqlBuffer.append( "( " +individualSql[i].substring(individualSqlLowerCase.indexOf(" where")+6,individualSqlLowerCase.indexOf("group by")) + ")");
				//System.out.println("original split sql " + individualSql[i]);
				if (i+1<individualSql.length-1) { 
					convertedSqlBuffer.append("\n OR  \n");
				}
			}
		}
		return convertedSqlBuffer.toString();
		
	}
	
	public String buildSqlWithUnion(String tempTableSql) { 
		
		String[] individualSql = tempTableSql.split("<\\*>");
		StringBuffer  convertedSqlBuffer = new StringBuffer();
		String individualSqlLowerCase = "";
		for (int i =0 ; i<individualSql.length-1;i++) { 
			individualSqlLowerCase = individualSql[i].toLowerCase();
			if (individualSqlLowerCase.indexOf("select")>0) { 
				convertedSqlBuffer.append(individualSql[i].substring(individualSqlLowerCase.indexOf("select"),individualSqlLowerCase.length()));
				//System.out.println("original split sql " + individualSql[i]);
				if (i+1<individualSql.length-1) { 
					convertedSqlBuffer.append("\n UNION ALL  \n");
				}
			}
		}
		return convertedSqlBuffer.toString();
		
	}
	
}
