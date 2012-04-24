package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType.TotalItemOccurrences;

public class BuildTotalOccuranceSql {

	public String buildTotalOccuranceSql(String dimensionJoinSql,
			boolean encounterFlag, boolean instanceNumFlag, int panelNumber,
			TotalItemOccurrences totalOccurances) {

		String selectClause = " ", groupbyClause = " ";
		TotalItemOccurrenceHandler totalItemOccurrencHandler = new TotalItemOccurrenceHandler();
		String totalItemOccurrenceClause = totalItemOccurrencHandler
				.buildTotalItemOccurrenceClause(totalOccurances);

		/*if (panelNumber != 1) {
			selectClause =  " patient_num "; // smuniraju " 1 ";
		} else {
			if (instanceNumFlag) {
				selectClause = "instance_num, encounter_num, ";
			} else if (encounterFlag) {
				selectClause = "encounter_num, ";
			}
			selectClause += " patient_num ," + panelNumber;
		}
		*/
		if (instanceNumFlag) {
			selectClause = "instance_num, encounter_num, ";
		} else if (encounterFlag) {
			selectClause = "encounter_num, ";
		}
		// smuniraju: SQLServer syntax
		// selectClause += " patient_num ," + panelNumber;
		selectClause += " patient_num ," + panelNumber + " as panelNumber ";
		
		//check if the dimensionJoinSql is query in query with fact constrains
		String groupbyClausePrefix = "";
		if (dimensionJoinSql.indexOf("j1.")>0) {
			groupbyClausePrefix = "j1.";
		}
		if (instanceNumFlag) {
			groupbyClause = " " + groupbyClausePrefix + "encounter_num ," + groupbyClausePrefix + "instance_num,";
		} else if (encounterFlag) {
			groupbyClause = " "  + groupbyClausePrefix + "encounter_num ,";
		}
		groupbyClause += " " +  groupbyClausePrefix + "patient_num ";

		String totalOccuranceSql = "select " + selectClause + " from ("
				+ dimensionJoinSql + "  group by " + groupbyClause
				+ " having count(*) " + totalItemOccurrenceClause + " ) t";

		return totalOccuranceSql;
	}
}
