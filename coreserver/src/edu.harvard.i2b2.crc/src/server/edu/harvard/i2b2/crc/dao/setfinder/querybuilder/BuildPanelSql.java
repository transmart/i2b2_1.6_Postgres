package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.util.List;

import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.loader.dao.DataSourceLookupDAOFactory;

public class BuildPanelSql extends CRCDAO {
	private String tempTableName = null;
	private DataSourceLookup dataSourceLookup = null;

	public BuildPanelSql(DataSourceLookup dataSourceLookup, String tempTableName) {
		this.setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
		this.tempTableName = tempTableName;
	}

	public String buildPanelSql(List<String> itemSqlList, int panelCount,int oldPanelCount,
			boolean firstPanelFlag, boolean encounterFlag,
			boolean instanceNumFlag, boolean panelInvertFlag, boolean invertQueryFlag) {
		StringBuffer panelSqlBuffer = new StringBuffer();
		boolean firstItemFlag = true;
		
		
		for (String itemSql : itemSqlList) {
			
			// add the item sql to the temp table
			if (firstPanelFlag && invertQueryFlag == false ) {
				panelSqlBuffer.append(firstPanelItemSql(itemSql,
						getTempTableName(), encounterFlag, instanceNumFlag));

			} else {
				panelSqlBuffer.append(nonFirstPanelItemSql(itemSql,
						getTempTableName(), panelCount, oldPanelCount, encounterFlag,
						instanceNumFlag,panelInvertFlag,firstPanelFlag, invertQueryFlag,firstItemFlag));
			}
			panelSqlBuffer.append(this.getSqlDelimitor());
			firstItemFlag = false;
		}
		return panelSqlBuffer.toString();
	}

	private String getTempTableName() {
		return this.tempTableName;
	}

	public String getSqlDelimitor() {
		return "\n<*>\n";
	}

	private String firstPanelItemSql(String totalOccuranceSql,
			String tempTableName, boolean encounterFlag, boolean instanceNumFlag) {
		String selectClause = " patient_num , panel_count";
		if (instanceNumFlag) {
			selectClause = " instance_num, encounter_num, " + selectClause;
		} else if (encounterFlag) {
			selectClause = " encounter_num, " + selectClause;
		}
		String firstPanelItemSql = " insert into " + this.getDbSchemaName()
				+ tempTableName + " (" + selectClause + ")" + "\n"
				+ totalOccuranceSql;
		return firstPanelItemSql;
	}

	private String nonFirstPanelItemSql(String totalOccuranceSql,
			String tempTableName, int panelCount, int oldPanelCount,  boolean encounterFlag,
			boolean instanceNumFlag, boolean panelInvertFlag,  boolean firstPanelFlag, boolean invertQueryFlag, boolean firstItemFlag) {
		String encounterNumClause = " ", instanceNumClause = " ";
		String encounterNumClausePostgres = " ", instanceNumClausePostgres = " ";
		if (instanceNumFlag) {
			instanceNumClause = " and  " + this.getDbSchemaName()
					+ tempTableName + ".encounter_num = t.encounter_num and "
					+ this.getDbSchemaName() + tempTableName
					+ ".instance_num = t.instance_num ";
			
			// smuniraju: Since postgres has to do a self join, tablename cannot be prefixed to the 
			// fieldname, table alias must be used instead.
			encounterNumClausePostgres = " and  t1.encounter_num = t3.encounter_num and "
					+ "t1.instance_num = t3.instance_num ";
		} else if (encounterFlag) {
			encounterNumClause = " and " + this.getDbSchemaName()
					+ tempTableName + ".encounter_num = t.encounter_num ";
			
			// smuniraju: Since postgres has to do a self join, tablename cannot be prefixed to the 
			// fieldname, table alias must be used instead.
			encounterNumClausePostgres = " and t1.encounter_num = t3.encounter_num ";
		}
		String nonFirstPanelItemSql = " ";
		if (panelInvertFlag) {
			if (firstItemFlag) { 
				nonFirstPanelItemSql = " update " +   this.getDbSchemaName()
				+ tempTableName + " set panel_count = " + panelCount + " where " + this.getDbSchemaName() + tempTableName
				+ ".panel_count =  " + oldPanelCount  + "\n<*>\n";
			}
			if (firstPanelFlag) {
				oldPanelCount = 1;
			}
			
			if(this.dataSourceLookup.getServerType().equalsIgnoreCase(DataSourceLookupDAOFactory.POSTGRES)){
				nonFirstPanelItemSql =  " update " + this.getDbSchemaName()
				+ tempTableName + " as t1 set panel_count = -1"
				+ " from " + this.getDbSchemaName() + tempTableName + " as t2 "
				+ " left outer join (" + totalOccuranceSql + ") t3 ON  t2.patient_num = t3.patient_num"
				+ " where t1.patient_num = t2.patient_num" 
				+ " and t3.patient_num is not null"
				+ " and t1.panel_count = " + panelCount
				+ encounterNumClausePostgres
				+ instanceNumClausePostgres;			
			} else {
				nonFirstPanelItemSql +=  " update " + this.getDbSchemaName()
				+ tempTableName + " set panel_count = -1 " 
				+ " where " + this.getDbSchemaName() + tempTableName
				+ ".panel_count =  " + panelCount + " and exists ( " + totalOccuranceSql + " where "
				+ this.getDbSchemaName() + tempTableName
				+ ".patient_num = t.patient_num " + encounterNumClause
				+ instanceNumClause + " ) ";
			}
			/* smuniraju
			nonFirstPanelItemSql =  " update " + this.getDbSchemaName()
			+ tempTableName + " set panel_count = -1"
			+ " from " + this.getDbSchemaName() + tempTableName + " as t2 "
			+ " left outer join (" + totalOccuranceSql + ") t3 ON  t2.patient_num = t3.patient_num"
			+ " where t3.patient_num is not null"
			+ " and t2.panel_count = " + panelCount
			+ encounterNumClause
			+ instanceNumClause;*/
			
		} else {						 
			 String notExists = "  ";			  
			 if (invertQueryFlag && firstPanelFlag) { 
				 oldPanelCount = 1;
				 panelCount = -1;
				 notExists = " not ";				 				 
			 }
			 
			 if(this.dataSourceLookup.getServerType().equalsIgnoreCase(DataSourceLookupDAOFactory.POSTGRES)){
				 // smuniraju: NOT EXISTS was replaced with LEFT OUTER JOIN
				 notExists = " not ";
				 if (invertQueryFlag && firstPanelFlag) {					 
					notExists = " ";
				 }	 
				 
				 nonFirstPanelItemSql =  " update " + this.getDbSchemaName()
					+ tempTableName + " as t1 set panel_count =" + panelCount
					+ " from " + this.getDbSchemaName() + tempTableName + " as t2 "
					+ " left outer join (" + totalOccuranceSql + ") t3 ON  t2.patient_num = t3.patient_num"
					+ " where t1.patient_num = t2.patient_num" 
					+ " and t3.patient_num is" + notExists + " null"
					+ " and t1.panel_count = " + oldPanelCount
					+ encounterNumClausePostgres
					+ instanceNumClausePostgres;
			 } else {
				 nonFirstPanelItemSql =  " update " + this.getDbSchemaName()
					+ tempTableName + " set panel_count =" + panelCount
					+ " where " + notExists + " exists ( " + totalOccuranceSql + " where "
					+ this.getDbSchemaName() + tempTableName
					+ ".panel_count =  " + oldPanelCount + " and "
					+ this.getDbSchemaName() + tempTableName
					+ ".patient_num = t.patient_num " + encounterNumClause
					+ instanceNumClause + " ) ";
			 }
			 /* smuniraju			  
			 nonFirstPanelItemSql =  " update " + this.getDbSchemaName()
				+ tempTableName + " set panel_count =" + panelCount
				+ " from " + this.getDbSchemaName() + tempTableName + " as t2 "
				+ " left outer join (" + totalOccuranceSql + ") t3 ON  t2.patient_num = t3.patient_num"
				+ " where t3.patient_num is" + notExists + " null"
				+ " and t2.panel_count = " + oldPanelCount
				+ encounterNumClause
				+ instanceNumClause;*/
		}
		return nonFirstPanelItemSql;
	}

}
