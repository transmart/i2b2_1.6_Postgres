/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.workplace.dao;

import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Date;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.common.util.xml.XMLUtil;
import edu.harvard.i2b2.workplace.datavo.pm.ProjectType;
import edu.harvard.i2b2.workplace.datavo.wdo.AnnotateChildType;
import edu.harvard.i2b2.workplace.datavo.wdo.ChildType;
import edu.harvard.i2b2.workplace.datavo.wdo.DeleteChildType;
import edu.harvard.i2b2.workplace.datavo.wdo.FolderType;
import edu.harvard.i2b2.workplace.datavo.wdo.GetChildrenType;
import edu.harvard.i2b2.workplace.datavo.wdo.GetReturnType;
import edu.harvard.i2b2.workplace.datavo.wdo.RenameChildType;
import edu.harvard.i2b2.workplace.datavo.wdo.XmlValueType;
import edu.harvard.i2b2.workplace.ejb.DBInfoType;
import edu.harvard.i2b2.workplace.util.StringUtil;
import edu.harvard.i2b2.workplace.util.WorkplaceUtil;


public class FolderDao extends JdbcDaoSupport {
	
    private static Log log = LogFactory.getLog(FolderDao.class);
//    final static String CORE = " c_hierarchy, c_hlevel, c_name, c_user_id, c_group_id, c_share_id, c_index, c_parent_index, c_visualattributes, c_tooltip";
//	final static String DEFAULT = " c_name, c_hierarchy";
    final static String CORE = " c_name, c_user_id, c_group_id, c_share_id, c_index, c_parent_index, c_visualattributes, c_tooltip";
	final static String DEFAULT = " c_name, c_index ";
    final static String ALL = CORE + ", c_entry_date, c_change_date, c_status_cd";
	final static String BLOB = ", c_work_xml, c_work_xml_schema, c_work_xml_i2b2_type ";
	
    private SimpleJdbcTemplate jt;
    
	private void setDataSource(String dataSource) {
		DataSource ds = null;
		try {
			ds = WorkplaceUtil.getInstance().getDataSource(dataSource);
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());;
		} 
		this.jt = new SimpleJdbcTemplate(ds);
	}
	
	private String getMetadataSchema() throws I2B2Exception{

		return WorkplaceUtil.getInstance().getMetaDataSchemaName();
	}
		
	
	public List findRootFoldersByProject(final GetReturnType returnType, final String userId, final ProjectType projectInfo, final DBInfoType dbInfo) throws DataAccessException, I2B2Exception{
				
		// find return parameters
		String parameters = CORE;		
		if (returnType.getType().equals("core")){
			parameters = CORE;
		}
/*		else if (returnType.getType().equals("all")){
			parameters = ALL;
		}
*/

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
				
		// 1. check if user already has a folder
		//       if not create one.
		check_addRootNode(metadataSchema, userId, projectInfo, dbInfo);
		

//		 First step is to call PM to see what roles user belongs to.
		
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toLowerCase().equals("protected_access")) {
				 protectedAccess = true;
				 break;
			 }
		}

		ParameterizedRowMapper<FolderType> mapper = getMapper(returnType.getType(), false, null);
		    
		List queryResult = null;		
		if (!protectedAccess){
			String tablesSql = "select distinct(c_table_cd), " + parameters + " from " +  metadataSchema +  "workplace_access where c_protected_access = ? and LOWER(c_group_id) = ? order by c_name"; //c_hierarchy";

			try {
				queryResult = jt.query(tablesSql, mapper, "N", projectInfo.getId().toLowerCase());
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database error");
			}
		}
		else{
			String tablesSql = "select distinct(c_table_cd), " + parameters + " from " +  metadataSchema +  "workplace_access where LOWER(c_group_id) = ? order by c_name"; //c_hierarchy";

			try {
				queryResult = jt.query(tablesSql, mapper, projectInfo.getId().toLowerCase());
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		log.debug("result size = " + queryResult.size());
		
		return queryResult;
	}

	public List findRootFoldersByUser(final GetReturnType returnType, final String userId, final ProjectType projectInfo, final DBInfoType dbInfo) throws DataAccessException, I2B2Exception{
		
		// find return parameters
		String parameters = CORE;		
		if (returnType.getType().equals("core")){
			parameters = CORE;
		}
/*		else if (returnType.getType().equals("all")){
			parameters = ALL;
		}
*/

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
		
		// 1. check if user already has a folder
		//       if not create one.
		check_addRootNode(metadataSchema, userId, projectInfo, dbInfo);
		

		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toLowerCase().equals("protected_access")) {
				 protectedAccess = true;
				 break;
			 }
		}

		
		ParameterizedRowMapper<FolderType> mapper = getMapper(returnType.getType(), false, null);		
	    
		List queryResult = null;		
		if (!protectedAccess){
			String tablesSql = "select distinct(c_table_cd), " + parameters + " from " +  metadataSchema +  "workplace_access where (c_share_id = 'Y' and LOWER(c_group_id) = ?) or (c_protected_access = ? and LOWER(c_user_id) = ? and LOWER(c_group_id) = ?) order by c_name"; //c_hierarchy";
		
	//		log.info(tablesSql);
			try {
				queryResult = jt.query(tablesSql, mapper, projectInfo.getId().toLowerCase(), "N",userId.toLowerCase(), projectInfo.getId().toLowerCase());
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database error");
			}
		}
		else{
			String tablesSql = "select distinct(c_table_cd), " + parameters + " from " +  metadataSchema +  "workplace_access where (c_share_id = 'Y' and LOWER(c_group_id) = ?) or (LOWER(c_user_id) = ? and LOWER(c_group_id) = ?) order by c_name"; //c_hierarchy";
			try {
				queryResult = jt.query(tablesSql, mapper, projectInfo.getId().toLowerCase(), userId.toLowerCase(), projectInfo.getId().toLowerCase());
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}
		/* 
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = "\\\\" + rs.getString("c_table_cd") + "\\" + rs.getString("c_table_name");
 	            return name;
	        }
		};
	if(queryResult.size() == 0){
			// this means that user is accessing for first time
			// grab tableCd tableName pair 
			//   and then insert an entry for the user
			String tablesSql = "select distinct(c_table_cd), c_table_name from " +  metadataSchema +  "workplace_access"; 

			try {
				queryResult = jt.query(tablesSql, map);
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
			if(queryResult.size() == 0)
				throw new I2B2DAOException("Database Error");
			else{
				queryResult= addRootNode((String)queryResult.get(0), userId, projectInfo, dbInfo);
			}	
		}*/
		log.debug("result size = " + queryResult.size());
		
		return queryResult;
	}
	
	public void check_addRootNode(String metadataSchema, String userId, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		String entriesSql = "select c_name  from " +  metadataSchema +  "workplace_access where LOWER(c_user_id) = ? and LOWER(c_group_id) = ?"; 
		
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = rs.getString("c_name") ;
 	            return name;
	        }
		};
		List queryResult = null;
		try {
			queryResult = jt.query(entriesSql, map, userId.toLowerCase(), projectInfo.getId().toLowerCase());
		} catch (DataAccessException e1) {
			// TODO Auto-generated catch block
			log.error(e1.getMessage());
			throw new I2B2DAOException("Database Error");
		}
//		log.info("check for root node size = " + queryResult.size());
		if(queryResult.size() > 0)
			return;
		
		// else queryResult is empty
		//    need to create a new entry for user
		
		//1. get ProtectedAccess status for user
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}

		String protectedAccess = "N";
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			String role = (String) it.next();
			if(role.toLowerCase().equals("protected_access")) {
				protectedAccess = "Y";
				break;
			}
		}
		// 2. Get tableCd tableName info 
		String tableSql = "select distinct(c_table_cd), c_table_name from " +  metadataSchema +  "workplace_access"; 
		
		ParameterizedRowMapper<String> map2 = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	 String name = "\\\\" + rs.getString("c_table_cd") + "\\" + rs.getString("c_table_name");
 	            return name;
	        }
		};
		queryResult = jt.query(tableSql, map2);
		String tableInfo = (String)queryResult.get(0);
		
		//extract table code and table name
		String tableCd = StringUtil.getTableCd(tableInfo);
		String tableName = StringUtil.getIndex(tableInfo);

		String addSql = "insert into " + metadataSchema+ "workplace_access "  + 
		"(c_table_cd, c_table_name, c_hlevel, c_protected_access, c_name, c_user_id, c_index, c_visualattributes, c_share_id, c_group_id, c_entry_date) values (?,?,?,?,?,?,?,?,?,?,?)";

		int numRootsAdded = -1;
		String index = StringUtil.generateMessageId();
		try {	
			numRootsAdded = jt.update(addSql, tableCd, tableName, 0, protectedAccess,
					userId, userId,index, "CA", "N", projectInfo.getId(), Calendar.getInstance().getTime()); 

		} catch (DataAccessException e) {
			log.error("Dao addChild failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}

//		log.info(addSql +  " " + numRowsAdded);
		log.debug("Number of roots added: " + numRootsAdded);
		
		return;

	}
	
	public List findChildrenByParent(final GetChildrenType childrenType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		// find return parameters
		String type = "core";
		String parameters = CORE;		
		if (childrenType.getType().equals("all")){
			parameters = ALL;
			type = "all";
		}
		if(childrenType.isBlob() == true)
			parameters = parameters + BLOB;
				
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toLowerCase().equals("protected_access")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = (rs.getString("c_table_name"));
	            return name;
	        }
		};
		
		//extract table code
		String tableCd = StringUtil.getTableCd(childrenType.getParent());
	//	log.debug(tableCd);
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ? and c_protected_access = ? ";
	//log.info("getChildren " + tableSql + tableCd);
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		String hidden = "";
		if(childrenType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";
		
		String sql = "select " + parameters +" from " + metadataSchema+tableName  + " where  c_parent_index = ? and (c_status_cd != 'D' or c_status_cd is null)"; 
		sql = sql + hidden + " order by c_name ";
 
		String parentIndex = StringUtil.getIndex(childrenType.getParent());
		
		log.debug(sql + " " + parentIndex);
//		log.info(type + " " + tableCd );
		
		ParameterizedRowMapper<FolderType> mapper = getMapper(type, childrenType.isBlob(), tableCd);
		
		List queryResult = null;
		try {
			queryResult = jt.query(sql, mapper, parentIndex );
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("Database Error");
		}
		log.debug("result size = " + queryResult.size());
		
		
		return queryResult;
		// tested statement with aqua data studio   verified output from above against this. 
		// select  c_fullname, c_name, c_synonym_cd, c_visualattributes  from metadata.testrpdr 
		// where c_fullname like '\RPDR\Diagnoses\Circulatory system (390-459)\Arterial vascular disease (440-447)\(446) Polyarteritis nodosa and al%' 
		// and c_hlevel = 5  and c_visualattributes not like '_H%' and c_synonym_cd = 'N'
		
		// verified both with and without hiddens and synonyms.
		
		// clob test   level = 4
		//   <parent>\\testrpdr\RPDR\HealthHistory\PHY\Health Maintenance\Mammogram\Mammogram - Deferred</parent> 
	}

	
	public int renameNode(final RenameChildType renameChildType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toLowerCase().equals("protected_access")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = (rs.getString("c_table_name"));
	            return name;
	        }
		};
		
		//extract table code
		String tableCd = StringUtil.getTableCd(renameChildType.getNode());
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}
		
		String index = StringUtil.getIndex(renameChildType.getNode());
		
		// get original name and work xml
		String sql = "select c_name, c_work_xml, c_work_xml_i2b2_type from " + metadataSchema + tableName + " where c_index = ? ";
		ParameterizedRowMapper<FolderType> map2 = new ParameterizedRowMapper<FolderType>() {
	        public FolderType mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	FolderType child = new FolderType();
	            child.setName(rs.getString("c_name"));
//	            child.setTooltip(rs.getString("c_tooltip"));
	            child.setWorkXmlI2B2Type(rs.getString("c_work_xml_i2b2_type"));
	            
//	            Clob xml_clob = rs.getClob("c_work_xml");
//	            try {
//					if(xml_clob != null){
//						String c_xml = JDBCUtil.getClobString(xml_clob);
//			//			Log log2 = LogFactory.getLog(FolderDao.class);
//			//			log2.debug("CLOB STRING TO CHECK");
//			//			log2.debug(c_xml);
//						if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)")))
//						{
//							Element rootElement = null;
//							try{
//								Document doc = XMLUtil.convertStringToDOM(c_xml);
//								rootElement = doc.getDocumentElement();
//							} catch (I2B2Exception e) {
//								log.error(e.getMessage());
//								child.setWorkXml(null);
//							}
//							if (rootElement != null) {
//							/*	try {
//									log2.debug("ROOT ELEMENT TO CHECK");
//									String test = XMLUtil.convertDOMElementToString(rootElement);
//									log2.debug(test);
//								} catch (Exception e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}*/
//								
//								if(child.getWorkXmlI2B2Type().equals("CONCEPT")  ) {
//									NodeList nameElements = rootElement.getElementsByTagName("name");
//									nameElements.item(0).setTextContent(renameChildType.getName());	   
//
//									NodeList synonymElements = rootElement.getElementsByTagName("synonym_cd");
//									if(synonymElements.item(0) != null)
//										synonymElements.item(0).setTextContent("Y");
//
//								}
//								XmlValueType xml = new XmlValueType();
//								xml.getAny().add(rootElement);
//								child.setWorkXml(xml); 
//							}	
//						}
//					}else {
//						child.setWorkXml(null);
//					}
//				} catch (IOException e1) {
//					log.error(e1.getMessage());
//					child.setWorkXml(null);
//				} 
	            return child;
	        }
		};
		List queryResult = null;
		try {
			queryResult = jt.query(sql, map2, index);  
		} catch (DataAccessException e) {
			log.error("Dao queryResult failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}
		FolderType node = (FolderType)queryResult.get(0);
		
//		String newTooltip = StringUtil.replaceEnd(node.getTooltip(),node.getName(), renameChildType.getName());
//		log.info(newTooltip);
		int numRowsRenamed = -1;
		if(node.getWorkXmlI2B2Type().equals("FOLDER")){
			String updateSql = "update " + metadataSchema+tableName  + " set c_name = ? where c_index = ? ";
			try {
				numRowsRenamed = jt.update(updateSql, renameChildType.getName(),index);
			} catch (DataAccessException e) {
				log.error("Dao renameChild failed");
				log.error(e.getMessage());
				throw new I2B2DAOException("Data access error " , e);
			}
		}
		else {
			String updateSql = "update " + metadataSchema+tableName  + " set c_name = ?, c_work_xml = ? where c_index = ? ";

			String newXml = null;
//			Element newXmlElement = node.getWorkXml().getAny().get(0);
			Element newXmlElement = renameChildType.getWorkXml().getAny().get(0);
			if(newXmlElement != null){
				newXml = XMLUtil.convertDOMElementToString(newXmlElement);
//				log.debug(newXml);				
			}
			try {
				numRowsRenamed = jt.update(updateSql, renameChildType.getName(), newXml, index);
			} catch (DataAccessException e) {
				log.error("Dao renameChild failed");
				log.error(e.getMessage());
				throw new I2B2DAOException("Data access error " , e);
			}
		}
		log.debug("Number of rows renamed: " + numRowsRenamed);
		return numRowsRenamed;

	}
	
	public int moveNode(final ChildType childType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toLowerCase().equals("protected_access")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = (rs.getString("c_table_name"));
	            return name;
	        }
		};
		
		//extract table code
		String tableCd = StringUtil.getTableCd(childType.getNode());
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}
			
		String index = StringUtil.getIndex(childType.getNode());		
		String updateSql = "update " + metadataSchema+tableName  + " set c_parent_index = ? where c_index = ? ";
		
		int numRowsMoved = -1;
		try {
			numRowsMoved = jt.update(updateSql, childType.getParent(), index);
		} catch (DataAccessException e) {
			log.error("Dao moveChild failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}
//		log.info(updateSql + " " + path + " " + numRowsAnnotated);
		log.debug("Number of rows moved: " + numRowsMoved);
		return numRowsMoved;

	}
	
	public int annotateNode(final AnnotateChildType annotateChildType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toLowerCase().equals("protected_access")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = (rs.getString("c_table_name"));
	            return name;
	        }
		};
		
		//extract table code
		String tableCd = StringUtil.getTableCd(annotateChildType.getNode());
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}
			
		String index = StringUtil.getIndex(annotateChildType.getNode());	
		String updateSql = "update " + metadataSchema+tableName  + " set c_tooltip = ? where c_index = ? ";
		
		int numRowsAnnotated = -1;
		try {
			numRowsAnnotated = jt.update(updateSql, annotateChildType.getTooltip(), index);
		} catch (DataAccessException e) {
			log.error("Dao annotateChild failed");
			log.error(e.getMessage());
			throw new I2B2DAOException("Data access error " , e);
		}
//		log.info(updateSql + " " + path + " " + numRowsAnnotated);
		log.debug("Number of rows annotated: " + numRowsAnnotated);
		return numRowsAnnotated;

	}
	

	public int addNode(final FolderType addChildType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toLowerCase().equals("protected_access")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = (rs.getString("c_table_name"));
	            return name;
	        }
		};
		
		//extract table code
		String tableCd = StringUtil.getTableCd(addChildType.getParentIndex());
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(tableSql + tableCd);
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}
		
	int numRowsAdded = -1;
	try {
		String xml = null;
		XmlValueType workXml=addChildType.getWorkXml();
		if (workXml != null) {
			String addSql = "insert into " + metadataSchema+tableName  + 
			"(c_name, c_user_id, c_index, c_parent_index, c_visualattributes, c_group_id, c_share_id, c_tooltip, c_entry_date, c_work_xml, c_work_xml_i2b2_type) values (?,?,?,?,?,?,?,?,?,?,?)";
			Element element = workXml.getAny().get(0);
			if(element != null)
				xml = XMLUtil.convertDOMElementToString(element);
			numRowsAdded = jt.update(addSql, 
					addChildType.getName(), addChildType.getUserId(),addChildType.getIndex(), StringUtil.getIndex(addChildType.getParentIndex()), 
					addChildType.getVisualAttributes(), addChildType.getGroupId(), addChildType.getShareId(), addChildType.getTooltip(),  Calendar.getInstance().getTime(),
					xml, addChildType.getWorkXmlI2B2Type()); 
		}		
		else {
			String addSql = "insert into " + metadataSchema+tableName  + 
			"(c_name, c_user_id, c_index, c_parent_index, c_visualattributes, c_group_id, c_share_id, c_tooltip, c_entry_date, c_work_xml_i2b2_type) values (?,?,?,?,?,?,?,?,?,?)";
			numRowsAdded = jt.update(addSql, 
				addChildType.getName(), addChildType.getUserId(),addChildType.getIndex(), StringUtil.getIndex(addChildType.getParentIndex()), 
				addChildType.getVisualAttributes(), addChildType.getGroupId(), addChildType.getShareId(), addChildType.getTooltip(),  Calendar.getInstance().getTime(),
				 addChildType.getWorkXmlI2B2Type()); 
		}
	} catch (DataAccessException e) {
		log.error("Dao addChild failed");
		log.error(e.getMessage());
		throw new I2B2DAOException("Data access error " , e);
	}

//	log.info(addSql +  " " + numRowsAdded);
	log.debug("Number of rows added: " + numRowsAdded);

	return numRowsAdded;

	}
	
	
	public int deleteNode(final DeleteChildType deleteChildType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{
		String metadataSchema = dbInfo.getDb_fullSchema();
		String serverType = dbInfo.getDb_serverType();
		setDataSource(dbInfo.getDb_dataSource());
			
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2Exception e = new I2B2Exception("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toLowerCase().equals("protected_access")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = (rs.getString("c_table_name"));
	            return name;
	        }
		};
		//extract table code
		String tableCd = StringUtil.getTableCd(deleteChildType.getNode());
		// table code to table name conversion
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "workplace_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}
		String index = StringUtil.getIndex(deleteChildType.getNode());	
		checkForChildrenDeletion(index, tableName, metadataSchema);
		//Mark node for deletion
	
		String updateSql = " update " + metadataSchema+tableName  + " set c_change_date = ?, c_status_cd = 'D'  where c_index = ? ";
		log.debug(serverType + "updateSql " + index);
		int numRowsDeleted = -1;
		try {
	//		log.info(sql + " " + w_index);
			numRowsDeleted = jt.update(updateSql, Calendar.getInstance().getTime(),index);
		} catch (DataAccessException e) {
			log.error("Dao deleteChild failed");
			log.error(e.getMessage());
			throw e;
		}
		log.debug("Number of rows deleted " + numRowsDeleted);
		return numRowsDeleted;

	}
	
	private void checkForChildrenDeletion(String nodeIndex, String tableName, String metadataSchema) throws DataAccessException {

		// mark children for deletion
		String updateSql = " update " + metadataSchema+tableName  + " set c_change_date = ?, c_status_cd = 'D'  where c_parent_index = ? ";
		int numChildrenDeleted = -1;
		try {
	//		log.info(sql + " " + w_index);
			numChildrenDeleted = jt.update(updateSql, Calendar.getInstance().getTime(),nodeIndex);
		} catch (DataAccessException e) {
			log.error("Dao deleteChild failed");
			log.error(e.getMessage());
			throw e;
		}
		log.debug("Number of children deleted: "+ numChildrenDeleted);
		// look for children that are folders
		String folderSql = "select c_index from " + metadataSchema+tableName + " where c_parent_index = ? and c_visualattributes like 'F%' ";

		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String index = (rs.getString("c_index"));
	            return index;
	        }
		};
		
		List folders = null;
		try{
			folders = jt.query(folderSql, map, nodeIndex);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		// recursively check folders for children to delete
		if (folders != null){
			Iterator it = folders.iterator();
			while(it.hasNext()){
				 String folderIndex = (String) it.next();
				 checkForChildrenDeletion(folderIndex, tableName, metadataSchema);
			}
		}
		
	}
	
	private ParameterizedRowMapper<FolderType> getMapper(final String type, final Boolean isBlob, final String tableCd){

		ParameterizedRowMapper<FolderType> mapper = new ParameterizedRowMapper<FolderType>() {
	        public FolderType mapRow(ResultSet rs, int rowNum) throws SQLException {
	            FolderType child = new FolderType();
	            //TODO fix this for all/+blob
	            if (tableCd == null){
//	            	child.setHierarchy("\\\\" + rs.getString("c_table_cd")+ rs.getString("c_hierarchy")); 
	            	child.setIndex("\\\\" + rs.getString("c_table_cd")+ "\\" + rs.getString("c_index")); 
	            }
	            else{
//	            	child.setHierarchy("\\\\" + tableCd + rs.getString("c_hierarchy")); 
	            	child.setIndex("\\\\" + tableCd + "\\" + rs.getString("c_index")); 
	            }
	      //      log.debug("getMapper: " + child.getIndex());
		        child.setName(rs.getString("c_name"));
	            if(!(type.equals("default"))) {
	            	child.setUserId(rs.getString("c_user_id"));
	   //         	child.setHlevel(rs.getInt("c_hlevel"));
	            	child.setGroupId(rs.getString("c_group_id"));
	            	child.setVisualAttributes(rs.getString("c_visualattributes"));
	   //         	child.setIndex(rs.getString("c_index"));
	            	child.setParentIndex(rs.getString("c_parent_index"));
	            	child.setShareId(rs.getString("c_share_id" ));
	            	child.setTooltip(rs.getString("c_tooltip"));
	            }if(isBlob == true){
	            	child.setWorkXmlI2B2Type(rs.getString("c_work_xml_i2b2_type"));
	            	
	            	String c_xml = null;
	            	try {
	            		Clob xml_clob = rs.getClob("c_work_xml");
	            		if (xml_clob != null){
	            			c_xml = JDBCUtil.getClobString(xml_clob);
	            			if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)")))
	            			{
	            				SAXBuilder parser = new SAXBuilder();
	            				java.io.StringReader xmlStringReader = new java.io.StringReader(c_xml);
	            				Element rootElement = null;
	            				try {
	            					org.jdom.Document metadataDoc = parser.build(xmlStringReader);
	            					org.jdom.output.DOMOutputter out = new DOMOutputter(); 
	            					Document doc = out.output(metadataDoc);
	            					rootElement = doc.getDocumentElement();
	            				} catch (JDOMException e) {
	            					log.error(e.getMessage());
	            					child.setWorkXml(null);
	            				} catch (IOException e1) {
	            					log.error(e1.getMessage());
	            					child.setWorkXml(null);
	            				}
	            				if (rootElement != null) {
	            					XmlValueType xml = new XmlValueType();
	            					xml.getAny().add(rootElement);
	            					child.setWorkXml(xml);
	            				}
	            				else {
	    //        					log.debug("rootElement is null");
	            					child.setWorkXml(null);
	            				}
	            			}else {
	         //   				log.debug("work xml is null");
	            				child.setWorkXml(null);
	            			}
	            		}
	            		else {
            //				log.debug("work xml is null");
            				child.setWorkXml(null);
            			}
	            	} catch (Exception e) {
	            		log.error(e.getMessage());
	            		child.setWorkXml(null);
	            	} 
	            	
	            	try {
	            		Clob xml_schema_clob = rs.getClob("c_work_xml_schema");
	            		if (xml_schema_clob != null){
	            			c_xml = JDBCUtil.getClobString(xml_schema_clob);
	            			if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)")))
	            			{
	            				SAXBuilder parser = new SAXBuilder();
	            				java.io.StringReader xmlStringReader = new java.io.StringReader(c_xml);
	            				Element rootElement = null;
	            				try {
	            					org.jdom.Document metadataDoc = parser.build(xmlStringReader);
	            					org.jdom.output.DOMOutputter out = new DOMOutputter(); 
	            					Document doc = out.output(metadataDoc);
	            					rootElement = doc.getDocumentElement();
	            				} catch (JDOMException e) {
	            					log.error(e.getMessage());
	            					child.setWorkXmlSchema(null);
	            				} catch (IOException e1) {
	            					log.error(e1.getMessage());
	            					child.setWorkXmlSchema(null);
	            				}
	            				if (rootElement != null) {
	            					XmlValueType xml = new XmlValueType();
	            					xml.getAny().add(rootElement);
	            					child.setWorkXmlSchema(xml);
	            				}
	            				else {
	//            					log.debug("rootElement is null");
	            					child.setWorkXmlSchema(null);
	            				}
	            			}else {
	 //           				log.debug("work xml schema is null");
	            				child.setWorkXmlSchema(null);
	            			}
	            		}
	            		else {
     //       				log.debug("work xml schema is null");
            				child.setWorkXmlSchema(null);
            			}
	            	} catch (Exception e) {
	            		log.error(e.getMessage());
	            		child.setWorkXmlSchema(null);
	            	}
	            }
				if((type.equals("all"))){
					DTOFactory factory = new DTOFactory();
					// make sure date isnt null before converting to XMLGregorianCalendar
					Date date = rs.getDate("c_entry_date");
					if (date == null)
						child.setEntryDate(null);
					else 
						child.setEntryDate(factory.getXMLGregorianCalendar(date.getTime())); 

					date = rs.getDate("c_change_date");
					if (date == null)
						child.setChangeDate(null);
					else 
						child.setChangeDate(factory.getXMLGregorianCalendar(date.getTime())); 

					child.setStatusCd(rs.getString("c_status_cd"));

				}
	            return child;
	        }
	    };
    return mapper;
	}
	
}
