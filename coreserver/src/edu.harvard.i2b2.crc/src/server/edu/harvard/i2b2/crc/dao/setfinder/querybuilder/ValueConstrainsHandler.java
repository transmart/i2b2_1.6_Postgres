/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the i2b2 Software License v1.0
 * which accompanies this distribution.
 *
 * Contributors:
 *     Rajesh Kuttan
 */
package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainOperatorType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainValueType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.util.SqlClauseUtil;

/**
 * Class to handle value constrains. Generates sql where clause based on the
 * list of value constrains.
 * 
 * @author rkuttan
 */
public class ValueConstrainsHandler {
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());
	
	private boolean unitCdConverstionFlag = false;
	private String unitCdInClause = "", unitCdSwitchClause = "";
	
	public void setUnitCdConversionFlag(boolean unitCdConverstionFlag, String unitCdInClause, String unitCdSwitchClause) { 
		this.unitCdConverstionFlag = unitCdConverstionFlag;
		this.unitCdInClause = unitCdInClause;
		this.unitCdSwitchClause = unitCdSwitchClause;
	}

	public String constructValueConstainClause(
			List<ItemType.ConstrainByValue> valueConstrainList)
			throws I2B2DAOException {
		String fullConstrainSql = "";

		for (ItemType.ConstrainByValue valueConstrain : valueConstrainList) {
			ConstrainValueType valueType = valueConstrain.getValueType();
			ConstrainOperatorType operatorType = valueConstrain
					.getValueOperator();
			String value = valueConstrain.getValueConstraint();
			String unitCd = valueConstrain.getValueUnitOfMeasure();
			
			String constrainSql = null;
			// check if value type is not null
			if (valueType == null) {
				continue;
			}
			if (valueType.equals(ConstrainValueType.TEXT)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}

				if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.LIKE.value())) {
					constrainSql = " valtype_cd = 'T' AND tval_char LIKE '"
							+ value.replaceAll("'", "''") + "%'";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.EQ.value())) {
					constrainSql = " valtype_cd = 'T' AND tval_char   = '"
							+ value.replaceAll("'", "''") + "' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.IN.value())) {
					value = SqlClauseUtil.buildINClause(value, true);
					constrainSql = " valtype_cd = 'T' AND tval_char   IN ("
							+ value + ")";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.BETWEEN.value())) {
					try {
						value = SqlClauseUtil.buildBetweenClause(value);
					} catch (I2B2Exception e) {
						throw new I2B2DAOException("Error in BETWEEN Clause"
								+ e.getMessage()
								+ StackTraceUtil.getStackTrace(e));
					}
					constrainSql = " valtype_cd = 'T' AND tval_char   BETWEEEN "
							+ value;
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.NE.value())) {
					constrainSql = "  valtype_cd = 'T' AND  tval_char   <> '"
							+ value.replaceAll("'", "''") + "' ";
				} else {
					throw new I2B2DAOException(
							"Error TEXT value constrain because operator("
									+ operatorType.toString() + ")is invalid");
				}
			} else if (valueType.equals(ConstrainValueType.NUMBER)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}
				value.replaceAll("'", "''");
				
				String nvalNum = " nval_num ", unitsCdInClause = " ";
				if (this.unitCdConverstionFlag) { 
					nvalNum = unitCdSwitchClause;
					//unitsCdInClause = this.unitCdInClause + " AND ";
					
					//commented not needed
//					if (unitCd != null) { 
//						unitCd = unitCd.replace("'", "''");
//						unitsCdInClause = " case when '" + unitCd + "' in " +  this.unitCdInClause + " then 1 else 0 end  =1  AND ";	
//					}
					unitsCdInClause = " ";
				}
				
				if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.GT.value())) {
					constrainSql = unitsCdInClause + " (( valtype_cd = 'N' AND  "+ nvalNum + " > "
							+ value
							+ " AND  tval_char IN ('E','GE')) OR ( valtype_cd = 'N' AND  "+ nvalNum+" >= "
							+ value + " AND  tval_char = 'G' )) ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.GE.value())) {
					constrainSql = unitsCdInClause + "   valtype_cd = 'N' AND  "+ nvalNum +" >= "
							+ value + " AND  tval_char IN ('E','GE','G') ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.LT.value())) {
					constrainSql = unitsCdInClause + "  (( valtype_cd = 'N' AND  "+ nvalNum +" < "
							+ value
							+ " AND  tval_char IN ('E','LE')) OR ( valtype_cd = 'N' AND  "+ nvalNum +" <= "
							+ value + " AND  tval_char = 'L' )) ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.LE.value())) {
					constrainSql = unitsCdInClause + "   valtype_cd = 'N' AND  "+ nvalNum +" <= "
							+ value + " AND  tval_char IN ('E','LE','L') ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.EQ.value())) {
					constrainSql = unitsCdInClause + "   valtype_cd = 'N' AND  " +  nvalNum + " = "
							+ value + " AND  tval_char='E' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.BETWEEN.value())) {
					try {
						value = SqlClauseUtil.buildBetweenClause(value);
					} catch (I2B2Exception e) {
						throw new I2B2DAOException("Error in BETWEEN Clause"
								+ e.getMessage()
								+ StackTraceUtil.getStackTrace(e));
					}
					constrainSql = unitsCdInClause + "   valtype_cd = 'N' AND  "+ nvalNum +" BETWEEN  "
							+ value + " AND  tval_char ='E' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.NE.value())) {
					constrainSql = unitsCdInClause + "  (( valtype_cd = 'N' AND  " + nvalNum + " <> "
							+ value
							+ " AND  tval_char <> 'NE') OR ( valtype_cd = 'N' AND  " + nvalNum + " = "
							+ value + " AND  tval_char ='NE' )) ";
				} else {
					throw new I2B2DAOException(
							"Error NUMBER value constrain because operator("
									+ operatorType.toString() + ")is invalid");
				}
			} else if (valueType.equals(ConstrainValueType.FLAG)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}
				if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.EQ.value())) {
					constrainSql = "  valueflag_cd = '"
							+ value.replaceAll("'", "''") + "' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.NE.value())) {
					constrainSql = "   valueflag_cd <> '"
							+ value.replaceAll("'", "''") + "' ";
				} else if (operatorType.value().equalsIgnoreCase(
						ConstrainOperatorType.IN.value())) {
					value = SqlClauseUtil.buildINClause(value, true);
					constrainSql = "  valueflag_cd IN " + value;
				} else {
					throw new I2B2DAOException(
							"Error FLAG value constrain because operator("
									+ operatorType.toString() + ")is invalid");
				}
			} else if (valueType.equals(ConstrainValueType.MODIFIER)) {
				// check if operator and value not null
				if (operatorType == null || value == null) {
					continue;
				}
				if (value != null) {
					if (operatorType.value().equalsIgnoreCase(
							ConstrainOperatorType.EQ.value())) {
						constrainSql = "  valtype_cd = 'M' and  tval_char = '"
								+ value.replaceAll("'", "''") + "' ";
					} else if (operatorType.value().equalsIgnoreCase(
							ConstrainOperatorType.NE.value())) {
						constrainSql = "  valtype_cd = 'M' and  tval_char <> '"
								+ value.replaceAll("'", "''") + "' ";
					} else if (operatorType.value().equalsIgnoreCase(
							ConstrainOperatorType.IN.value())) {
						value = SqlClauseUtil.buildINClause(value, true);
						constrainSql = "  valtype_cd = 'M' and  tval_char IN "
								+ value + " ";
					}

				}

			} else {
				throw new I2B2DAOException(
						"Error value constrain, invalid value type ("
								+ valueType.toString() + ")");
			}

			if (constrainSql != null) {
				if (fullConstrainSql.length() > 0) {
					fullConstrainSql += " AND ";
				}

				fullConstrainSql += constrainSql;
			}
		}

		return fullConstrainSql;
	}
}
