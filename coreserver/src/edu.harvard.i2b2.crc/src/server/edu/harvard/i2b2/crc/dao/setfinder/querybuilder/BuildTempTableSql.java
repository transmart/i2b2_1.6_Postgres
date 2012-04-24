package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.crc.dao.CRCDAO;
import edu.harvard.i2b2.crc.dao.DAOFactoryHelper;
import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.ontology.ModifierType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType.TotalItemOccurrences;
import edu.harvard.i2b2.crc.util.ItemKeyUtil;
import edu.harvard.i2b2.crc.util.LogTimingUtil;
import edu.harvard.i2b2.crc.util.ParamUtil;
import edu.harvard.i2b2.crc.util.SqlClauseUtil;

public class BuildTempTableSql extends CRCDAO {

	DataSourceLookup dataSourceLookup = null;
	String queryXML = null;
	String noLockSqlServer = " ";
	StringBuffer ignoredItemMessageBuffer = new StringBuffer();
	ConceptTypeHandler conceptTypeHandler = null;
	BuildPanelSql buildPanelSql = null;
	BuildTotalOccuranceSql buildTotalOccuranceSql = null;
	TempTableNameMap tempTableNameMap = null;
	String processTimingFlag = "NONE";
	ProcessTimingReportUtil processTimingUtil = null;
	String processTimingStr = "";
	Map projectParamMap = null;

	public BuildTempTableSql(DataSourceLookup dataSourceLookup, String queryXML) {
		this.setDbSchemaName(dataSourceLookup.getFullSchema());
		this.dataSourceLookup = dataSourceLookup;
		this.queryXML = queryXML;
		conceptTypeHandler = new ConceptTypeHandler(queryXML);
		tempTableNameMap = new TempTableNameMap(dataSourceLookup.getServerType());
		buildPanelSql = new BuildPanelSql(dataSourceLookup, tempTableNameMap.getTempTableName());
		buildTotalOccuranceSql = new BuildTotalOccuranceSql();
		
		//construct the process timing util
		try {
			this.processTimingUtil = new ProcessTimingReportUtil(dataSourceLookup);
		} catch (I2B2DAOException e) {
			log.error("Error constructing the ProcessTimingReportUtil [" + e.getMessage() + "]");
		} 

	}
	
	public void setProcessTimingFlag(String level) { 
		this.processTimingFlag = level;
	}
	
	public void setProjectParamMap(Map projectParamMap) { 
		this.projectParamMap = projectParamMap;
	}
	
	
	public String getProcessTimingXml() {
		return this.processTimingStr;
	}
	

	public Map<Integer, String> buildTempTableSql(
			List<PanelType> panelListType, boolean encounterFlag,
			boolean instanceNumFlag, String queryTiming, int panelCount,
			boolean firstPanelFlag, boolean invertQueryFlag) throws I2B2DAOException {
		// read above panel parameters

		// read each panel
		List<ItemType> itemListType = new ArrayList<ItemType>();
		List<String> itemSqlList = new ArrayList<String>();
		List<String> panelSqlList = new ArrayList<String>();

		//
		ignoredItemMessageBuffer.delete(0, ignoredItemMessageBuffer.length());

		SortedMap<Integer, String> panelSql = new TreeMap<Integer, String>();
		int oldPanelCount = panelCount;
		LogTimingUtil logTiming = new LogTimingUtil();
		for (PanelType panelType : panelListType) {
			logTiming.setStartTime();
			// check if the panel is invert
			boolean panelInvertFlag = getPanelInvertFlag(panelType.getInvert());
			// get the total item occurance value
			TotalItemOccurrences totalOccurance = panelType
					.getTotalItemOccurrences();

			// read each item from the panel
			itemListType = panelType.getItem();
			itemSqlList.removeAll(itemSqlList);
			panelCount++;

			// generate panel date constrain
			DateConstrainUtil dateConstrainUtil = new DateConstrainUtil(
					this.dataSourceLookup);
			String panelDateConstrainSql = dateConstrainUtil
					.buildPanelDateSql(panelType);

			boolean singleValidItemInPanel = false;
			for (ItemType itemType : itemListType) {

				// call the ontology with the item key.
				ConceptType conceptType;
				try {
					conceptType = conceptTypeHandler.getConceptType(itemType
							.getItemKey());
					singleValidItemInPanel = true;
					// build the dimension sql
					String dimensionSql = buildDimensionSql(conceptType);

					// build the dimension join sql | need information like
					// encounter_set,samevisit,same instance
					String dimensionJoinSql = buildDimensionJoinSql(
							dimensionSql, encounterFlag, instanceNumFlag, queryTiming,
							itemType, conceptType, panelDateConstrainSql);

				
					// build the outer table with total occurrance constrain //
					// String fullItemSql = buildFullItemSql(dimensionJoinSql,
					// encounterFlag);
					String totalOccuraneSql = buildTotalOccuranceSql
							.buildTotalOccuranceSql(dimensionJoinSql,
									encounterFlag, instanceNumFlag, panelCount,
									totalOccurance);

					itemSqlList.add(totalOccuraneSql);
				} catch (ConceptNotFoundException e) {
					handleConceptNotFoundException(e, panelCount);
				} catch (OntologyException e) {
					throw new I2B2DAOException(e.getMessage());
				}
			}
			if (singleValidItemInPanel) {
				String panelItemSql = buildPanelSql.buildPanelSql(itemSqlList,
						panelCount, oldPanelCount, firstPanelFlag, encounterFlag,
						instanceNumFlag, panelInvertFlag,invertQueryFlag);
				panelSql.put(panelCount, panelItemSql);
				oldPanelCount = panelCount;
				firstPanelFlag = false;

			}
			logTiming.setEndTime();
			if (this.processTimingFlag.equalsIgnoreCase(ProcessTimingReportUtil.DEBUG)) { 
				//build the log xml and add it to the  string variable. 
				processTimingStr += this.processTimingUtil.buildProcessTiming(logTiming, "BUILD SQL - PANEL", null) + "\n";
			}
			
		}
		return panelSql;

	}

	private void handleConceptNotFoundException(ConceptNotFoundException e,
			int panelCount) {
		ignoredItemMessageBuffer
				.append(e.getMessage() + " panel#" + panelCount);
	}

	public String getIgnoredItemMessage() {
		if (this.ignoredItemMessageBuffer != null
				&& this.ignoredItemMessageBuffer.length() > 0) {
			return "Missing Concept in Ontology Cell : \n"
					+ this.ignoredItemMessageBuffer.toString();
		} else {
			return "";
		}

	}

	public String buildDimensionSql(ConceptType conceptType) {
		String dimensionSql = "";
		// if patient list

		dimensionSql = conceptType.getFacttablecolumn() + " IN (select "
				+ conceptType.getFacttablecolumn() + " from "
				+ getDbSchemaName() + conceptType.getTablename() + "  "
				+ noLockSqlServer + " where " + conceptType.getColumnname()
				+ " " + conceptType.getOperator() + " "
				+ conceptType.getDimcode() + ")";

		return dimensionSql;
	}

	// function to build
	public String buildDimensionJoinSql(String dimensionSql,
			boolean encounterFlag, boolean instanceNumFlag, String queryTiming,ItemType itemType,
			ConceptType conceptType, String panelDateConstrainSql)
			throws I2B2DAOException {
		String joinTableName = "";
		String itemDateConstrainSql = " ", itemValueConstrainSql = " ", itemModifierConstrainSql = " ";
		String sqlHintClause = " ";
		QueryTimingHandler timingHandler = new QueryTimingHandler();
		
		if (panelDateConstrainSql == null) {
			panelDateConstrainSql = "";
		}
		
		if (itemType.getItemKey().toLowerCase().startsWith(ItemKeyUtil.ITEM_KEY_PATIENT_SET)) {
			
			// generate sql for item date constrain
			//joinTableName = "qt_patient_set_collection";
			if (timingHandler.isSameVisit(queryTiming)) { 
				joinTableName = "visit_dimension";
			} else if (timingHandler.isSameInstanceNum(queryTiming)) { 
				joinTableName = "observation_fact";
			} else { 
				joinTableName = "qt_patient_set_collection";
			}
			
			// generate sql for item date constrain
			itemDateConstrainSql = callDateConstrain(itemType);
			
			
				
			// generate sql for item value constrain
			itemValueConstrainSql = callValueConstrain(itemType, "", "");
			
			
			//check if dateconstrain or value constrain present in the item
			if (itemDateConstrainSql.length() > 0 || itemValueConstrainSql.length()>0 || panelDateConstrainSql.length() >0 ) { 
				joinTableName = "observation_fact";
			}
			
			
			
			
		} else if (itemType.getItemKey().toLowerCase().startsWith(ItemKeyUtil.ITEM_KEY_PATIENT_ENCOUNTER_SET)) {
			 if (timingHandler.isSameInstanceNum(queryTiming)) { 
					joinTableName = "observation_fact";
			 }
			joinTableName = "qt_patient_enc_collection";
			// generate sql for item date constrain
			itemDateConstrainSql = callDateConstrain(itemType);
			
			// generate sql for item value constrain
			itemValueConstrainSql = callValueConstrain(itemType,"","");
			
			//check if dateconstrain or value constrain present in the item
			if (itemDateConstrainSql.length() > 0 || itemValueConstrainSql.length()>0 || panelDateConstrainSql.length() > 0 ) { 
				joinTableName = "observation_fact";
			}
			
			
		} else if (itemType.getItemKey().toLowerCase().startsWith(ItemKeyUtil.ITEM_KEY_MASTERID)) {
			String masterTableName = tempTableNameMap.getTempMasterTable();
			dimensionSql = " master_id =  '" + itemType.getItemKey() + "'";

			// generate sql for item date constrain
			itemDateConstrainSql = callDateConstrain(itemType);
			
			// generate sql for item value constrain
			itemValueConstrainSql = callValueConstrain(itemType,"","");
			
			//check if dateconstrain or value constrain present in the item
			if (itemDateConstrainSql.length() > 0 || itemValueConstrainSql.length() > 0 || panelDateConstrainSql.length() > 0 ) {
				String masterWhereClause = " j1.patient_num = j2.patient_num ";
				String masterSelectClause = " j1.patient_num "; 
				if (timingHandler.isSameVisit(queryTiming)) { 
					masterWhereClause = " j1.encounter_num = j2.encounter_num and j1.patient_num = j2.patient_num " ;
					masterSelectClause = " j1.encounter_num, j1.patient_num "; 
				} else if (timingHandler.isSameVisit(queryTiming)) { 
					masterSelectClause = " j1.encounter_num, j1.patient_num, j1.instance_num ";
					masterWhereClause = " j1.encounter_num = j2.encounter_num and j1.patient_num = j2.patient_num and j1.instance_num = j2.instance_num " ;
				}
				joinTableName = "observation_fact";
				String masterDimensionJoinSql = "select " +  masterSelectClause + "  from " + this.getDbSchemaName() + joinTableName + " j1, " + this.getDbSchemaName() + masterTableName + " j2 " + " where " + masterWhereClause 
				+  "  " + itemValueConstrainSql
				+ itemDateConstrainSql + panelDateConstrainSql;  
				return masterDimensionJoinSql;
			}
			
			
		} else {
			joinTableName = "observation_fact";

			if (conceptType.getTablename().equalsIgnoreCase(
					"provider_dimension")) {
				sqlHintClause = " /*+ index(observation_fact observation_fact_pk) */ ";
			} else {
				sqlHintClause = " /*+ index(observation_fact fact_cnpt_pat_enct_idx) */ ";
			}

			ModifierType modifierType = this.getModifierMetadataFromOntology(itemType);
			
			//generate sql for item modifier constrain
			if (modifierType != null) { 
				itemModifierConstrainSql = callModifierConstrain(modifierType,itemType);
			}
			
			// generate sql for item date constrain
			itemDateConstrainSql = callDateConstrain(itemType);

			
			//generate sql for unit_cd conversion
			String unitCdSwitchClause = "", unitCdInClause = "";
			String modifierUnitCdSwitchClause = "", modifierUnitCdInClause = "";
			
			if ( projectParamMap != null && projectParamMap.get(ParamUtil.CRC_ENABLE_UNITCD_CONVERSION) != null) {
				String unitCdConversionFlag = (String)projectParamMap.get(ParamUtil.CRC_ENABLE_UNITCD_CONVERSION);
				if (unitCdConversionFlag != null && unitCdConversionFlag.equalsIgnoreCase("ON")) { 
					if (conceptType.getMetadataxml() != null && conceptType.getMetadataxml().getAny().get(0) != null) {
						Element valueMetadataElement = (Element)conceptType.getMetadataxml().getAny().get(0);
						UnitConverstionUtil unitConverstionUtil = new UnitConverstionUtil();
						unitCdSwitchClause = unitConverstionUtil.buildUnitCdSwitchClause(valueMetadataElement,false,"");
						log.debug("concept unit Conversion sql " +unitCdSwitchClause );
						unitCdInClause = unitConverstionUtil.buildUnitCdInClause(valueMetadataElement,"");
							
					}
					
					if (modifierType != null && modifierType.getMetadataxml() != null && modifierType.getMetadataxml().getAny().get(0) != null) {
						Element valueMetadataElement = (Element)modifierType.getMetadataxml().getAny().get(0);
						UnitConverstionUtil unitConverstionUtil = new UnitConverstionUtil();
						modifierUnitCdSwitchClause = unitConverstionUtil.buildUnitCdSwitchClause(valueMetadataElement,false,"");
						log.debug("modifier unit Conversion sql " +modifierUnitCdSwitchClause );
						modifierUnitCdInClause = unitConverstionUtil.buildUnitCdInClause(valueMetadataElement,"");
							
					}
				}
			}
	
			
			
			// generate sql for item value constrain
			itemValueConstrainSql = callValueConstrain(itemType,unitCdSwitchClause,unitCdInClause );
			
			// generate sql for modifier value constrain
			itemValueConstrainSql += callModifierValueConstrain(itemType,modifierUnitCdSwitchClause,modifierUnitCdInClause );
			
			

		}

		// itemType.getConstrainByValue()
		String selectClause = " patient_num ";
		
		if (timingHandler.isSameInstanceNum(queryTiming)) {
			selectClause = "instance_num, encounter_num, " + selectClause;
		} else if (timingHandler.isSameVisit(queryTiming)) {
			selectClause = " encounter_num, " + selectClause;
		}

		if (panelDateConstrainSql == null) {
			panelDateConstrainSql = " ";
		} else if (panelDateConstrainSql.trim().length() > 0)  { 
			panelDateConstrainSql = "  AND  ( " + panelDateConstrainSql
			+ " )";
		}
		if (itemValueConstrainSql == null) {
			itemValueConstrainSql = " ";
		}

		if (itemDateConstrainSql == null) {
			itemDateConstrainSql = " ";
		}
		
		if (itemModifierConstrainSql == null) {
			itemModifierConstrainSql = " ";
		}

		String dimensionJoinSql = " select " + sqlHintClause + selectClause
				+ " from " + this.getDbSchemaName() + joinTableName
				+ " where  " + dimensionSql + "  " + itemModifierConstrainSql 
				+ itemValueConstrainSql + itemDateConstrainSql + panelDateConstrainSql;

		return dimensionJoinSql;
	}
	
	private boolean getPanelInvertFlag(int panelInvert) {
		if (panelInvert == 1) { 
			return true;
		} else { 
			return false;
		}
	}
	
	
	private String callDateConstrain(ItemType itemType) { 
		// generate sql for item date constrain
		DateConstrainUtil dateConstrainUtil = new DateConstrainUtil(
				this.dataSourceLookup);
		String itemDateConstrainSql = dateConstrainUtil.buildItemDateSql(itemType);
		if (itemDateConstrainSql != null
				&& itemDateConstrainSql.trim().length() > 0) {
			log.info("Item date constrain sql" + itemDateConstrainSql);
			itemDateConstrainSql = "  AND ( " + itemDateConstrainSql
					+ " ) ";
		} else {
			itemDateConstrainSql = "";
		}
		return itemDateConstrainSql;
	}
	
	private String callValueConstrain(ItemType itemType, String unitCdSwitchClause, String unitCdInClause) throws I2B2DAOException { 
		ValueConstrainsHandler valueConstrainHandler = new ValueConstrainsHandler();
		if (unitCdSwitchClause.length()>0) { 
			valueConstrainHandler.setUnitCdConversionFlag(true, unitCdInClause, unitCdSwitchClause);
		}
		String itemValueConstrainSql = valueConstrainHandler
				.constructValueConstainClause(itemType
						.getConstrainByValue());
		log.info("Item value constrain sql " + itemValueConstrainSql);
		
		if (itemValueConstrainSql != null
				&& itemValueConstrainSql.trim().length() > 0) {
			
			itemValueConstrainSql = "  AND  ( " + itemValueConstrainSql 
					+ " )";
		} else { 
			itemValueConstrainSql = "";
		}
		return itemValueConstrainSql;
	}
	
	private String callModifierValueConstrain(ItemType itemType, String modifierUnitCdSwitchClause, String modifierUnitCdInClause) throws I2B2DAOException { 
		ValueConstrainsHandler valueConstrainHandler = new ValueConstrainsHandler();
		if (modifierUnitCdSwitchClause.length()>0) { 
			valueConstrainHandler.setUnitCdConversionFlag(true, modifierUnitCdInClause, modifierUnitCdSwitchClause);
		}
		
		String itemModifierValueConstrainSql = "" ;
		
		if (itemType.getConstrainByModifier() != null  && itemType.getConstrainByModifier().getConstrainByValue() !=null) {
			itemModifierValueConstrainSql = valueConstrainHandler.constructValueConstainClause(buildItemValueConstrain(itemType.getConstrainByModifier().getConstrainByValue()));
			if (itemModifierValueConstrainSql != null && itemModifierValueConstrainSql.length()>0) { 
				log.info("Modifier constrian value constrain sql " + itemModifierValueConstrainSql);
			}
		}
		
		if (itemModifierValueConstrainSql != null
				&& itemModifierValueConstrainSql.trim().length() > 0) {
			
			itemModifierValueConstrainSql = "  AND  ( " + itemModifierValueConstrainSql 
					+ " )";
		} else { 
			itemModifierValueConstrainSql = "";
		}
		return itemModifierValueConstrainSql;
	}
	
	private String callModifierConstrain(ModifierType modifierType, ItemType itemType) throws I2B2DAOException { 
		
		// generate sql for item date constrain
		ModifierConstrainsHandler modifierConstrainUtil = new ModifierConstrainsHandler(this.getDbSchemaName());
		String itemModifierConstrainSql = modifierConstrainUtil.constructModifierConstainClause(modifierType);
		if (itemModifierConstrainSql != null
				&& itemModifierConstrainSql.trim().length() > 0) {
			log.info("Item modifier constrain sql" + itemModifierConstrainSql);
			itemModifierConstrainSql = "  AND ( " + itemModifierConstrainSql
					+ " ) ";
		} else { 
			itemModifierConstrainSql = " ";
		}
		
		return itemModifierConstrainSql;
	}
	private ModifierType getModifierMetadataFromOntology(ItemType itemType) throws I2B2DAOException { 
		ItemType.ConstrainByModifier modifierConstrain = itemType.getConstrainByModifier();
		if (modifierConstrain == null) { 
			return null;
		} 
		String modifierKey = modifierConstrain.getModifierKey();
		String modifierAppliedPath = modifierConstrain.getAppliedPath();
		ItemMetaDataHandler metadataHandler = new ItemMetaDataHandler(
				queryXML);
		ModifierType modifierType = metadataHandler.getModifierDataFromOntologyCell(modifierKey,modifierAppliedPath);
		return modifierType;
	}
	
	private List<ItemType.ConstrainByValue> buildItemValueConstrain(List<ItemType.ConstrainByModifier.ConstrainByValue> modifierConstrainList) {
		List<ItemType.ConstrainByValue> itemValueConstrainList = new ArrayList<ItemType.ConstrainByValue>();
		for (ItemType.ConstrainByModifier.ConstrainByValue modifierValueConstrain : modifierConstrainList) { 
			ItemType.ConstrainByValue constrainByValue = new ItemType.ConstrainByValue();
			constrainByValue.setValueConstraint(modifierValueConstrain.getValueConstraint());
			constrainByValue.setValueOperator(modifierValueConstrain.getValueOperator()) ; 
			constrainByValue.setValueType(modifierValueConstrain.getValueType()); 
			constrainByValue.setValueUnitOfMeasure(modifierValueConstrain.getValueUnitOfMeasure()) ;
			itemValueConstrainList.add(constrainByValue);
		}
		return itemValueConstrainList;
	}
	

}
