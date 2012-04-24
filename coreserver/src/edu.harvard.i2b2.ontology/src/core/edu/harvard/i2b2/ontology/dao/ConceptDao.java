/*
 * Copyright (c) 2006-2007 Massachusetts General Hospital 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the i2b2 Software License v1.0 
 * which accompanies this distribution. 
 * 
 * Contributors:
 * 		Lori Phillips
 */
package edu.harvard.i2b2.ontology.dao;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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



import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.util.db.JDBCUtil;
import edu.harvard.i2b2.common.util.jaxb.DTOFactory;
import edu.harvard.i2b2.ontology.datavo.pm.ProjectType;
import edu.harvard.i2b2.ontology.datavo.vdo.ConceptType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetChildrenType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetModifierChildrenType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetModifierInfoType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetReturnType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetTermInfoType;
import edu.harvard.i2b2.ontology.datavo.vdo.VocabRequestType;
import edu.harvard.i2b2.ontology.datavo.vdo.ModifierType;
import edu.harvard.i2b2.ontology.datavo.vdo.GetModifiersType;
import edu.harvard.i2b2.ontology.datavo.vdo.XmlValueType;
import edu.harvard.i2b2.ontology.ejb.DBInfoType;
import edu.harvard.i2b2.ontology.ejb.NodeType;
import edu.harvard.i2b2.ontology.util.OntologyUtil;
import edu.harvard.i2b2.ontology.util.Roles;
import edu.harvard.i2b2.ontology.util.StringUtil;

public class ConceptDao extends JdbcDaoSupport {
	
    private static Log log = LogFactory.getLog(ConceptDao.class);
    final static String CAT_CORE = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_facttablecolumn, c_dimtablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_tooltip, valuetype_cd ";
    final static String CAT_DEFAULT = " c_fullname, c_name ";
    final static String CAT_LIMITED =  " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_tooltip, valuetype_cd ";
    
    
    final static String MOD_DEFAULT = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_tooltip, m_applied_path ";
    final static String MOD_CORE = MOD_DEFAULT;
    final static String MOD_LIMITED = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_tooltip, m_applied_path ";
    

    final static String DEFAULT = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_facttablecolumn, c_tablename, c_columnname, c_columndatatype, c_operator, c_dimcode, c_tooltip, valuetype_cd ";
    final static String CORE = DEFAULT;
    final static String LIMITED = " c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes, c_totalnum, c_basecode, c_tooltip, valuetype_cd ";
    
    final static String ALL = ", update_date, download_date, import_date, sourcesystem_cd ";
	final static String BLOB = ", c_metadataxml, c_comment ";

    final static String NAME_DEFAULT = " c_name ";
    
    private SimpleJdbcTemplate jt;
    
	private void setDataSource(String dataSource) {
		DataSource ds = null;
		try {
			ds = OntologyUtil.getInstance().getDataSource(dataSource);
		} catch (I2B2Exception e2) {
			log.error(e2.getMessage());;
		} 
		this.jt = new SimpleJdbcTemplate(ds);
		
	}
	
	private String getMetadataSchema() throws I2B2Exception{
		
		return OntologyUtil.getInstance().getMetaDataSchemaName();
	}
	
	public List findRootCategories(final GetReturnType returnType, final ProjectType projectInfo, final DBInfoType dbInfo) throws I2B2Exception, I2B2DAOException{
				
		// find return parameters
		String parameters = CAT_DEFAULT;		
		if (returnType.getType().equals("limited")){
			parameters = CAT_LIMITED;
		}
		else if(returnType.getType().equals("core")){
			parameters = CAT_CORE;
		}
		
/*		else if (returnType.getType().equals("all")){
			parameters = ALL;
		}
*/
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
		
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
			 if(role.toUpperCase().equals("DATA_PROT")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		final boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);
		
		ParameterizedRowMapper<ConceptType> mapper = new ParameterizedRowMapper<ConceptType>() {
			
	        public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
	            ConceptType child = new ConceptType();
	            //TODO fix this for all
		        child.setKey("\\\\" + rs.getString("c_table_cd")+ rs.getString("c_fullname")); 
	            child.setName(rs.getString("c_name"));
	            if(returnType.getType().equals("limited")) {
	            	child.setBasecode(rs.getString("c_basecode"));
	            	child.setLevel(rs.getInt("c_hlevel"));
	            	child.setSynonymCd(rs.getString("c_synonym_cd"));
	            	child.setVisualattributes(rs.getString("c_visualattributes"));

	            	child.setTooltip(rs.getString("c_tooltip"));
	            	child.setValuetypeCd(rs.getString("valuetype_cd"));
	            }
	            else if(returnType.getType().equals("core")) {
	            	child.setBasecode(rs.getString("c_basecode"));
	            	child.setLevel(rs.getInt("c_hlevel"));
	            	child.setSynonymCd(rs.getString("c_synonym_cd"));
	            	child.setVisualattributes(rs.getString("c_visualattributes"));

	            	Integer totalNum = rs.getInt("c_totalnum");
	            	boolean nullFlag = rs.wasNull();
	            	
	            	
	            	 if (nullFlag) { 
	                 	log.debug("null in totalnum flag ");
	                 } else { 
	                 	log.debug("not null in totalnum flag ");
	                 }
	                 
	                 if (rs.getString("c_totalnum") == null) { 
	                 	log.debug("null in totalnum flag using getString method");
	                 } else { 
	                 	log.debug("not null in totalnum flag using getString method  [" + rs.getString("c_totalnum") + "]");
	                 }
	                 
	            	if (obfuscatedUserFlag == false && nullFlag == false) {
	            		child.setTotalnum(totalNum);
	            	} 
	            			            	
	            	child.setFacttablecolumn(rs.getString("c_facttablecolumn" ));
	            	child.setTablename(rs.getString("c_dimtablename")); 
	            	child.setColumnname(rs.getString("c_columnname")); 
	            	child.setColumndatatype(rs.getString("c_columndatatype")); 
	            	child.setOperator(rs.getString("c_operator")); 	            	
	            	child.setDimcode(rs.getString("c_dimcode")); 
	            	child.setTooltip(rs.getString("c_tooltip"));
	            	child.setValuetypeCd(rs.getString("valuetype_cd"));
	            }
	            return child;
	        }
	    };
	    
	    
		List queryResult = null;

		if (!protectedAccess){
			String tablesSql = "select distinct(c_table_cd), " + parameters + " from " +  metadataSchema +  "table_access where c_protected_access = ? order by c_name";
			log.debug(tablesSql);
			try {
				queryResult = jt.query(tablesSql, mapper, "N");
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database error");
			}
		}
		else{
			String tablesSql = "select distinct(c_table_cd), " + parameters + " from " +  metadataSchema +  "table_access order by c_name";
			log.debug(tablesSql);
			try {
				queryResult = jt.query(tablesSql, mapper);
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}
		log.debug("result size = " + queryResult.size());
		
		if (returnType.isBlob() == true && queryResult != null){
			Iterator itr = queryResult.iterator();
			while (itr.hasNext()){
				ConceptType child = (ConceptType) itr.next();
				String clobSql = "select c_metadataxml, c_comment from "+  metadataSchema +  "table_access where c_table_cd = ?";
				ParameterizedRowMapper<ConceptType> map = new ParameterizedRowMapper<ConceptType>() {
			        public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
			        	ConceptType concept = new ConceptType();
//			        	ResultSetMetaData rsmd = rs.getMetaData();
//			        	rsmd.get
			        	if(rs.getClob("c_metadataxml") == null){
			        		concept.setMetadataxml(null);
			        	} else {
			        		String c_xml = null;
			        		try {
			        			c_xml = JDBCUtil.getClobString(rs.getClob("c_metadataxml"));
			        		} catch (IOException e1) {
			        			log.error(e1.getMessage());
			        			concept.setMetadataxml(null);
			        		}
			        		if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)"))) {
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
			        				concept.setMetadataxml(null);
			        			} catch (IOException e) {
			        				log.error(e.getMessage());
			        				concept.setMetadataxml(null);
			        			}
			        			if(rootElement != null) {
			        				XmlValueType xml = new XmlValueType();									
			        				xml.getAny().add(rootElement);								
			        				concept.setMetadataxml(xml);
			        			}
			        		} else {
			        			concept.setMetadataxml(null);
			        		}
			        	}	

			        	if(rs.getClob("c_comment") == null){
			        		concept.setComment(null);
			        	}else {
			        		try {
								concept.setComment(JDBCUtil.getClobString(rs.getClob("c_comment")));
							} catch (IOException e) {
								log.error(e.getMessage());
								concept.setComment(null);
							}
			        	}	

			        	return concept;
			        }
				};
				List clobResult = null;
				try {
					clobResult = jt.query(clobSql, map, StringUtil.getTableCd(child.getKey()));
				} catch (DataAccessException e) {
					log.error(e.getMessage());
					throw new I2B2DAOException("Database Error");
				}
				if(clobResult != null)  {
					child.setMetadataxml(((ConceptType)(clobResult.get(0))).getMetadataxml());
					child.setComment(((ConceptType)(clobResult.get(0))).getComment());
				}
				else {
					child.setMetadataxml(null);
					child.setComment(null);
				}
					
			}
		}
		return queryResult;
	}
	
	public List findChildrenByParent(final GetChildrenType childrenType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		// find return parameters
		String parameters = DEFAULT;		
		if (childrenType.getType().equals("limited")){
			parameters = LIMITED;
		}
		
		else if (childrenType.getType().equals("core")){
			parameters = CORE;
		}
		else if (childrenType.getType().equals("all")){
			parameters = CORE + ALL;
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
			 if(role.toUpperCase().equals("DATA_PROT")) {
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
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
	//		log.info("getChildren " + tableSql);
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		String path = StringUtil.getPath(childrenType.getParent()); //, dbInfo.getDb_serverType();
		String searchPath = StringUtil.escapeBackslash(path, dbInfo.getDb_serverType()) + "%";

// Lookup to get chlevel + 1 ---  dont allow synonyms so we only get one result back
				
		String levelSql = "select c_hlevel from " + metadataSchema+tableName  + " where c_fullname = ?  and c_synonym_cd = 'N'";

	    int level = 0;
		try {
			level = jt.queryForInt(levelSql, path);
		} catch (DataAccessException e1) {
			// should only get 1 result back  (path == c_fullname which should be unique)
			log.error(e1.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		String hidden = "";
		if(childrenType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";
	
		String synonym = "";
		if(childrenType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";
		
		String sql = "select " + parameters +" from " + metadataSchema+tableName  + " where c_fullname like ? and c_hlevel = ? "; 
		sql = sql + hidden + synonym + " order by c_name ";
 
		//log.info(sql + " " + path + " " + level);
		boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);
		ParameterizedRowMapper<ConceptType> mapper = getMapper(new NodeType(childrenType),obfuscatedUserFlag, dbInfo.getDb_serverType());
				
		List<ConceptType> queryResult = null;
		try {
			queryResult = jt.query(sql, mapper, searchPath, (level + 1) );
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("Database Error");
		}
		log.debug("result size = " + queryResult.size());
		
		if(queryResult.size() > 0){
			Iterator<ConceptType>  it2 = queryResult.iterator();
			while (it2.hasNext()){
				ConceptType concept = it2.next();
				// if a leaf has modifiers report it with visAttrib == F
				if(concept.getVisualattributes().startsWith("L")){
					String modPath = StringUtil.escapeBackslash(StringUtil.getPath(concept.getKey()), dbInfo.getDb_serverType());
					String sqlCount = "select count(*) from " + metadataSchema+ tableName  + " where m_exclusion_cd is null and c_fullname in";
					int queryCount = 0;
					// build m_applied_path sub-query
					String m_applied_pathSql = "(m_applied_path = '" + modPath +"'";
					while (modPath.length() > 3) {
						if(modPath.endsWith("%")){
							modPath = modPath.substring(0, modPath.length()-2);
							modPath = modPath.substring(0, modPath.lastIndexOf("\\") + 1) + "%";			
						}
						else
							modPath =  modPath + "%";
						m_applied_pathSql = m_applied_pathSql + " or m_applied_path = '" + StringUtil.escapeBackslash(modPath, dbInfo.getDb_serverType()) + "'" ; // modPath + "'" ;
					}
					sqlCount = sqlCount + "(select c_fullname from " + metadataSchema+ tableName  + " where c_hlevel = 1 and m_exclusion_cd is null and " + m_applied_pathSql + " )";

					if(dbInfo.getDb_serverType().equals("ORACLE"))
						sqlCount = sqlCount + " MINUS ";
					else
						sqlCount = sqlCount + " EXCEPT ";

					sqlCount = sqlCount+ " (select c_fullname from " + metadataSchema+ tableName  + " where m_exclusion_cd is not null and " + m_applied_pathSql + " )))";
					
					
					try {
						queryCount = jt.queryForInt(sqlCount);
					} catch (DataAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//				log.debug("COUNT " + queryCount + " for " +sqlCount);


					if(queryCount > 0){
						concept.setVisualattributes(concept.getVisualattributes().replace('L', 'F'));
						log.debug("changed " + concept.getName() + " from leaf to folder: modCount > 0");
					}
				}
			}
		}

		
		return queryResult;
		// tested statement with aqua data studio   verified output from above against this. 
		// select  c_fullname, c_name, c_synonym_cd, c_visualattributes  from metadata.testrpdr 
		// where c_fullname like '\RPDR\Diagnoses\Circulatory system (390-459)\Arterial vascular disease (440-447)\(446) Polyarteritis nodosa and al%' 
		// and c_hlevel = 5  and c_visualattributes not like '_H%' and c_synonym_cd = 'N'
		
		// verified both with and without hiddens and synonyms.
		
		// clob test   level = 4
		//   <parent>\\testrpdr\RPDR\HealthHistory\PHY\Health Maintenance\Mammogram\Mammogram - Deferred</parent> 
	}

	public List findByFullname(final GetTermInfoType termInfoType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{

		// find return parameters
		String parameters = DEFAULT;		
		if (termInfoType.getType().equals("limited")){
			parameters = LIMITED;
		}
		
		else if (termInfoType.getType().equals("core")){
			parameters = CORE;
		}
		else if (termInfoType.getType().equals("all")){
			parameters = CORE + ALL;
		}
		if(termInfoType.isBlob() == true)
			parameters = parameters + BLOB;
				
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2DAOException e = new I2B2DAOException("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toUpperCase().equals("DATA_PROT")) {
				 protectedAccess = true;
				 break;
			 }
		}
		boolean ofuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);
		
		//tableCd to table name conversion
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = (rs.getString("c_table_name"));
	            return name;
	        }
		};
		
		//extract table code
		String tableCd = StringUtil.getTableCd(termInfoType.getSelf());
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}

		String path = StringUtil.escapeBackslash(StringUtil.getPath(termInfoType.getSelf()), dbInfo.getDb_serverType());
		String searchPath = path;		


		String hidden = "";
		if(termInfoType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";
	
		String synonym = "";
		if(termInfoType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";
		
		String sql = "select " + parameters +" from " + metadataSchema+tableName  + " where c_fullname like ?  "; 
		sql = sql + hidden + synonym + " order by c_name ";
 
		//log.info(sql + " " + path + " " + level);
		
		ParameterizedRowMapper<ConceptType> mapper = getMapper(new NodeType(termInfoType), ofuscatedUserFlag, dbInfo.getDb_serverType());

		List queryResult = null;
		try {
			queryResult = jt.query(sql, mapper, searchPath );
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());
		
		
		return queryResult;

	}
	
	
	
	public List findNameInfo(final VocabRequestType vocabType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{

		// find return parameters
		String parameters = NAME_DEFAULT;		
		
		if (vocabType.getType().equals("limited")){
			parameters = LIMITED;
		}
		
		else if (vocabType.getType().equals("core")){
			parameters = CORE;
		}

		else if (vocabType.getType().equals("all")){
			parameters = CORE + ALL;
		}
		if(vocabType.isBlob() == true)
			parameters = parameters + BLOB;
				
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
	//	log.info(metadataSchema);
		
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
			 if(role.toUpperCase().equals("DATA_PROT")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		//tableCd to table name + fullname conversion
		ParameterizedRowMapper<ConceptType> map = new ParameterizedRowMapper<ConceptType>() {
	        public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	 ConceptType category = new ConceptType();	 
	    
	        	category.setTablename(rs.getString("c_table_name"));
	            category.setKey(rs.getString("c_fullname"));
	            return category;
	        }
		};
		
		//extract table code
		String tableCd = vocabType.getCategory();
		List<ConceptType> categoryResult;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name), c_fullname from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				categoryResult = jt.query(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}else {
			String tableSql = "select distinct(c_table_name), c_fullname from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				categoryResult = jt.query(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}

		 String nameInfoSql = null;
		    String compareName = null;
			// dont do the sql injection replace; it breaks the service.
		    if(vocabType.getMatchStr().getStrategy().equals("exact")) {
		    	nameInfoSql = "select " + parameters  + " from " + metadataSchema+categoryResult.get(0).getTablename() + " where upper(c_name) = ? and c_fullname like '" + StringUtil.doubleEscapeBackslash(categoryResult.get(0).getKey(), dbInfo.getDb_serverType()) +"%'";
		    	compareName = vocabType.getMatchStr().getValue().toUpperCase();
		  
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("left")){
		    	nameInfoSql = "select " + parameters  + " from " + metadataSchema+categoryResult.get(0).getTablename() +" where upper(c_name) like ?  and c_fullname like '" + StringUtil.doubleEscapeBackslash(categoryResult.get(0).getKey(), dbInfo.getDb_serverType()) +"%'";
		    	compareName = vocabType.getMatchStr().getValue().toUpperCase() + "%";
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("right")) {
		    	nameInfoSql = "select " + parameters  + " from " + metadataSchema+categoryResult.get(0).getTablename() +" where upper(c_name) like ?  and c_fullname like '" + StringUtil.doubleEscapeBackslash(categoryResult.get(0).getKey(), dbInfo.getDb_serverType()) +"%'";
		    	compareName =  "%" + vocabType.getMatchStr().getValue().toUpperCase();
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("contains")) {
		    	if(!(vocabType.getMatchStr().getValue().contains(" "))){
		    		nameInfoSql = "select " + parameters  + " from " + metadataSchema+categoryResult.get(0).getTablename() +" where upper(c_name) like ?  and c_fullname like '" + StringUtil.doubleEscapeBackslash(categoryResult.get(0).getKey(), dbInfo.getDb_serverType()) +"%'";
		    		compareName =  "%" + vocabType.getMatchStr().getValue().toUpperCase() + "%";
		    	}else{
		    		nameInfoSql = "select " + parameters  + " from " + metadataSchema+categoryResult.get(0).getTablename();
		    		nameInfoSql = nameInfoSql + parseMatchString(vocabType.getMatchStr().getValue())+ " and c_fullname like '" + StringUtil.doubleEscapeBackslash(categoryResult.get(0).getKey(), dbInfo.getDb_serverType()) +"%'";;
		    		compareName = null;
		    	}
		    }
		    


		String hidden = "";
		if(vocabType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";
	
		String synonym = "";
		if(vocabType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";
		
		nameInfoSql = nameInfoSql + hidden + synonym + " order by c_name ";
	    
	//	log.info(nameInfoSql + " " +compareName);
		boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);
		ParameterizedRowMapper<ConceptType> mapper = getMapper(new NodeType(vocabType),obfuscatedUserFlag, dbInfo.getDb_serverType());

		List queryResult = null;
		try {
			if(compareName != null)
				queryResult = jt.query(nameInfoSql, mapper, compareName);
			else
				queryResult = jt.query(nameInfoSql, mapper);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());
		return queryResult;

	}
	
	public List findCodeInfo(final VocabRequestType vocabType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{

		// find return parameters
		String parameters = NAME_DEFAULT;	
		
		if (vocabType.getType().equals("limited")){
			parameters = LIMITED;
		}
		
		else if (vocabType.getType().equals("core")){
			parameters = CORE;
		}
		
		else if (vocabType.getType().equals("all")){
			parameters = CORE + ALL;
		}
		if(vocabType.isBlob() == true)
			parameters = parameters + BLOB;
				
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
		String dbType = dbInfo.getDb_serverType();
		
//		log.info(metadataSchema);
		
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
			 if(role.toUpperCase().equals("DATA_PROT")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	           String name =  rs.getString("c_table_name");
	           return name;
	        }
		};
		
		//no table code provided so check all tables user has access to
		List tableNames=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_protected_access = ? ";
//			log.info(tableSql);
			try {
				tableNames = jt.query(tableSql, map, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access ";
			try {
				tableNames = jt.query(tableSql, map);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}

		String hidden = "";
		if(vocabType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";
	
		String synonym = "";
		if(vocabType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";

		// I have to do this the hard way because there are a dynamic number of codes to pass in
		//   prevent SQL injection
		String value = vocabType.getMatchStr().getValue();
		if(value.contains("'")){
			value = vocabType.getMatchStr().getValue().replaceAll("'", "''");
		}
		 String whereClause = null;
			
		    if(vocabType.getMatchStr().getStrategy().equals("exact")) {
		    	whereClause = " where upper(c_basecode) = '" + value.toUpperCase()+ "'";
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("left")){
		    	whereClause = " where upper(c_basecode) like '" + value.toUpperCase() + "%'";
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("right")) {
		    	value = value.replaceFirst(":", ":%");
		    	whereClause = " where upper(c_basecode) like '" +  value.toUpperCase() + "'";
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("contains")) {
		    	value = value.replaceFirst(":", ":%");
		    	whereClause = " where upper(c_basecode) like '" + value.toUpperCase() + "%'";
		    }

		log.debug(vocabType.getMatchStr().getStrategy() + whereClause);
		
		String codeInfoSql = null;
		if(tableNames != null){
			Iterator itTn = tableNames.iterator();
			String table = (String)itTn.next();
			// the following (distinct) doesnt work for a flattened hierarchy but is left for
				//  dbs other than sqlserver or oracle.   [c_table_cd is needed for key]
			String tableCdSql = ", (select distinct(c_table_cd) from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "') as tableCd"; 
			if(dbType.equals("SQLSERVER"))
				tableCdSql = ", (select top 1(c_table_cd) from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "') as tableCd"; 
			else if (dbType.equals("ORACLE"))
				tableCdSql = ", (select c_table_cd from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "' and rownum <= 1) as tableCd"; 
			codeInfoSql = "select " + parameters + tableCdSql + " from " + metadataSchema + table + whereClause	+ hidden + synonym;;
			while(itTn.hasNext()){		
				table = (String)itTn.next();
				// the following (distinct) doesnt work for a flattened hierarchy but is left for
				//  dbs other than sqlserver or oracle.    [c_table_cd is needed for key]
				tableCdSql = ", (select distinct(c_table_cd) from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "') as tableCd"; 
				if(dbType.equals("SQLSERVER"))
					tableCdSql = ", (select top 1(c_table_cd) from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "') as tableCd"; 
				else if (dbType.equals("ORACLE"))
					tableCdSql = ", (select c_table_cd from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "' and rownum <= 1) as tableCd"; 
				else if (dbType.equals("POSTGRES"))
					tableCdSql = ", (select c_table_cd from "+ metadataSchema + "TABLE_ACCESS where c_table_name = '"+  table+ "' limit 1) as tableCd";
				codeInfoSql = codeInfoSql +  " union all (select "+ parameters + tableCdSql + " from " + metadataSchema + table + whereClause
				+ hidden + synonym + ")";
			}
			codeInfoSql = codeInfoSql + " order by c_name ";
		}
		else
			return null;

		boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);
		ParameterizedRowMapper<ConceptType> mapper = getMapper(new NodeType(vocabType),obfuscatedUserFlag, dbInfo.getDb_serverType());

		List queryResult = null;
		try {
			queryResult = jt.query(codeInfoSql, mapper);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());
//		log.debug("searchByCode: " + codeInfoSql);
	
		return queryResult;

	}


	private ParameterizedRowMapper<ConceptType> getMapper(final NodeType node, final boolean ofuscatedUserFlag, final String dbType){

		ParameterizedRowMapper<ConceptType> mapper = new ParameterizedRowMapper<ConceptType>() {
			public ConceptType mapRow(ResultSet rs, int rowNum) throws SQLException {
				ConceptType child = new ConceptType();	          
				child.setName(rs.getString("c_name"));
				if(!(node.getType().equals("default"))){
					child.setBasecode(rs.getString("c_basecode"));
					child.setLevel(rs.getInt("c_hlevel"));
					// cover get Code Info case where we dont know the vocabType.category apriori
					if(node.getNode() != null)	
						child.setKey("\\\\" + node.getNode() + rs.getString("c_fullname"));  
					else
						child.setKey("\\\\" + rs.getString("tableCd") + rs.getString("c_fullname")); 
					child.setSynonymCd(rs.getString("c_synonym_cd"));
					child.setVisualattributes(rs.getString("c_visualattributes"));
					Integer totalNumValue = rs.getInt("c_totalnum");
					boolean nullFlag = rs.wasNull();

					if (nullFlag) { 
						log.debug("null in totalnum flag ");
					} else { 
						log.debug("not null in totalnum flag ");
					}

					if (rs.getString("c_totalnum") == null) { 
						log.debug("null in totalnum flag using getString method");
					} else { 
						log.debug("not null in totalnum flag using getString method  [" + rs.getString("c_totalnum") + "]");
					}
					if ( ofuscatedUserFlag == false && nullFlag == false) { 
						child.setTotalnum(totalNumValue);
					}
					child.setTooltip(rs.getString("c_tooltip"));
					child.setValuetypeCd(rs.getString("valuetype_cd"));
					if(!(node.getType().equals("limited"))) {
						child.setFacttablecolumn(rs.getString("c_facttablecolumn" ));
						child.setTablename(rs.getString("c_tablename")); 
						child.setColumnname(rs.getString("c_columnname")); 
						child.setColumndatatype(rs.getString("c_columndatatype")); 
						child.setOperator(rs.getString("c_operator")); 
						child.setDimcode(rs.getString("c_dimcode")); 
					}
				}
	            if(node.isBlob() == true){
	            	// c_comment
	            	String cComment = null;
	            	if((dbType).equals("POSTGRES")) {
	            		cComment = rs.getString("c_comment");
	            	} else {
						try {
	    					if(rs.getClob("c_comment") != null){
	    						cComment = JDBCUtil.getClobString(rs.getClob("c_comment"));
	    					}
						} catch (IOException e) {
		        			log.error(e.getMessage());
		        			child.setComment(null);
						} 
	            	}
					child.setComment(cComment); 
	
					// c_metadataxml
					String c_xml = null;
					if((dbType).equals("POSTGRES")) {
						c_xml = rs.getString("c_metadataxml");
					} else {
						if(rs.getClob("c_metadataxml") != null){
							// String c_xml = null;
							try {
								c_xml = JDBCUtil.getClobString(rs.getClob("c_metadataxml"));
							} catch (IOException e) {
								log.error(e.getMessage());
		            			child.setMetadataxml(null);
							}
							if ((c_xml != null) && (c_xml.trim().length() > 0)&&(!c_xml.equals("(null)"))) {
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
			            			child.setMetadataxml(null);
		            			} catch (IOException e1) {
			            			log.error(e1.getMessage());
			            			child.setMetadataxml(null);
								}
		            			if (rootElement != null) {
		            				XmlValueType xml = new XmlValueType();
		            				xml.getAny().add(rootElement);
		            				child.setMetadataxml(xml);
		            			}
							} else {
								child.setMetadataxml(null);
							}
						}	
	            	}
	            }
				if((node.getType().equals("all"))){
					DTOFactory factory = new DTOFactory();
					// make sure date isnt null before converting to XMLGregorianCalendar
					Date date = rs.getDate("update_date");
					if (date == null)
						child.setUpdateDate(null);
					else 
						child.setUpdateDate(factory.getXMLGregorianCalendar(date.getTime())); 
	
					date = rs.getDate("download_date");
					if (date == null)
						child.setDownloadDate(null);
					else 
						child.setDownloadDate(factory.getXMLGregorianCalendar(date.getTime())); 
	
					date = rs.getDate("import_date");
					if (date == null)
						child.setImportDate(null);
					else 
						child.setImportDate(factory.getXMLGregorianCalendar(date.getTime())); 
	
		            child.setSourcesystemCd(rs.getString("sourcesystem_cd"));
	
				}
	            return child;
			}
		};
		return mapper;
	}
	
	private String parseMatchString(String match){
		String whereClause = null;
		
		String[] terms = match.split(" ");
		ArrayList<String> goodWords = new ArrayList<String>();
		
		String word = getStopWords();
		for(int i=0; i< terms.length; i++){			
			if(word.contains(terms[i]))
					;
			else{
				goodWords.add(terms[i]);
			}
		}			
		
		if(goodWords.isEmpty())
			return null;
		
		Iterator it = goodWords.iterator();
		while(it.hasNext()){
			if(whereClause == null)	
				whereClause = " where upper(c_name) like " + "'%" + ((String)it.next()).toUpperCase() + "%'";
			else
				whereClause = whereClause + " AND upper(c_name) like " + "'%" + ((String)it.next()).toUpperCase() + "%'";
		}	
		return whereClause;
	}
	
	private String getStopWords(){

		String stopWord = 	"a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";	
//		String[] stopWords = stopWord.split("'");	
		return stopWord;
	}
	
	public List findModifiers(final GetModifiersType modifierType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

		// find return parameters
		String parameters = MOD_DEFAULT;	
		if (modifierType.getType().equals("limited")){
			parameters = MOD_LIMITED;
		}
		else if (modifierType.getType().equals("core")){
			parameters = MOD_CORE;
		}
		else if (modifierType.getType().equals("all")){
			parameters = MOD_CORE + ALL;
		}
		if(modifierType.isBlob() == true)
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
			 if(role.toUpperCase().equals("DATA_PROT")) {
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
		String tableCd = StringUtil.getTableCd(modifierType.getSelf());
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
	//		log.info("getChildren " + tableSql);
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}
		

		
	
//     Original sql before exclusions		
//		String path = StringUtil.getLiteralPath(modifierType.getSelf());
/*		String sql = "select " + parameters +" from " + metadataSchema+ tableName  + " where m_applied_path = '" + path + "' and c_hlevel = 1";
		while (path.length() > 2) {
			if(path.endsWith("%")){
				path = path.substring(0, path.length()-2);
				path = path.substring(0, path.lastIndexOf("\\") + 1) + "%";			
			}
			else
				path = path + "%";
			sql = sql + " union all (select " + parameters +" from " + metadataSchema+ tableName  + " where m_applied_path = '" + path + "' and c_hlevel = 1)";
		}
*/
		String path = StringUtil.escapeBackslash(StringUtil.getLiteralPath(modifierType.getSelf()), dbInfo.getDb_serverType());
		String sql = "select " + parameters + " from "+ metadataSchema+ tableName + " where m_exclusion_cd is null and c_fullname in (";
		String inclusionSql = "select c_fullname from " + metadataSchema+ tableName  + " where m_applied_path = '" + path + "' and c_hlevel = 1 and m_exclusion_cd is null";
		while (path.length() > 2) {
			if(path.endsWith("%")){
				path = path.substring(0, path.length()-2);
				path = path.substring(0, path.lastIndexOf("\\") + 1) + "%";			
			}
			else
				path = path + "%";
			inclusionSql = inclusionSql + " union all (select c_fullname from " + metadataSchema+ tableName  + " where m_applied_path = '" + StringUtil.escapeBackslash(path, dbInfo.getDb_serverType()) + "' and c_hlevel = 1 and m_exclusion_cd is null)";
		}

		if(dbInfo.getDb_serverType().equals("ORACLE"))
			sql = sql + inclusionSql + " MINUS (";
		else
			sql = sql + inclusionSql + " EXCEPT (";

		path = StringUtil.escapeBackslash(StringUtil.getLiteralPath(modifierType.getSelf()), dbInfo.getDb_serverType());
		String exclusionSql = "select c_fullname from " + metadataSchema+ tableName  + " where m_applied_path = '" + path + "' and m_exclusion_cd is not null";
		while (path.length() > 2) {
			if(path.endsWith("%")){
				path = path.substring(0, path.length()-2);
				path = path.substring(0, path.lastIndexOf("\\") + 1) + "%";			
			}
			else
				path = path + "%";
			exclusionSql = exclusionSql + " union all (select c_fullname from " + metadataSchema+ tableName  + " where m_applied_path = '" + StringUtil.escapeBackslash(path, dbInfo.getDb_serverType()) + "' and m_exclusion_cd is not null)";
		}
	/*		// applied paths on exclusions dont end in %
		while (path.length() > 2) {
			path = path.substring(0, path.length()-2);		
			path = path.substring(0, path.lastIndexOf("\\") +1) ;		
			exclusionSql = exclusionSql + " union all (select c_fullname from " + metadataSchema+ tableName  + " where m_applied_path = '" + path + "' and m_exclusion_cd = 'X')";
		}
		*/
		sql = sql + exclusionSql + "))";
		
		sql = sql + " order by c_name ";
		
		log.info("findMods: " + sql );
		final boolean ofuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);
		
		ParameterizedRowMapper<ModifierType> modMapper = getModMapper(new NodeType (modifierType), ofuscatedUserFlag, dbInfo.getDb_serverType());
		
		List queryResult = null;
		
		try {
//			queryResult = jt.query(sql, modMapper, path );
			queryResult = jt.query(sql, modMapper);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		return queryResult;
	}
	
	private ParameterizedRowMapper<ModifierType> getModMapper(final NodeType node, final boolean ofuscatedUserFlag, final String dbType){

		ParameterizedRowMapper<ModifierType> mapper = new ParameterizedRowMapper<ModifierType>() {
			public ModifierType mapRow(ResultSet rs, int rowNum) throws SQLException {
				ModifierType child = new ModifierType();	          
				if(node.getType().equals("limited")){
					child.setName(rs.getString("c_name"));
					child.setAppliedPath(rs.getString("m_applied_path"));
					child.setBasecode(rs.getString("c_basecode"));
					child.setKey("\\\\" + node.getNode() + rs.getString("c_fullname"));  
					child.setLevel(rs.getInt("c_hlevel"));
					child.setFullname(rs.getString("c_fullname"));  
					child.setVisualattributes(rs.getString("c_visualattributes"));
					child.setSynonymCd(rs.getString("c_synonym_cd"));
					child.setTooltip(rs.getString("c_tooltip"));
				}else{
					child.setName(rs.getString("c_name"));
					child.setAppliedPath(rs.getString("m_applied_path"));
					child.setBasecode(rs.getString("c_basecode"));
					child.setKey("\\\\" + node.getNode() + rs.getString("c_fullname"));  
					child.setLevel(rs.getInt("c_hlevel"));
					child.setFullname(rs.getString("c_fullname"));  
					child.setVisualattributes(rs.getString("c_visualattributes"));
					child.setSynonymCd(rs.getString("c_synonym_cd"));
					child.setFacttablecolumn(rs.getString("c_facttablecolumn" ));
					child.setTooltip(rs.getString("c_tooltip"));
					child.setTablename(rs.getString("c_tablename")); 
					child.setColumnname(rs.getString("c_columnname")); 
					child.setColumndatatype(rs.getString("c_columndatatype")); 
					child.setOperator(rs.getString("c_operator")); 
					// log.debug("smuniraju: ConceptDao.java: escaping quote in dimcode");
	            	// child.setDimcode(StringUtil.escapeSingleQuote(rs.getString("c_dimcode")));
					child.setDimcode(rs.getString("c_dimcode")); 
				}
				
				if(node.isBlob() == true){
	            	// c_comment
	            	String cComment = null;
	            	if((dbType).equals("POSTGRES")) {
	            		cComment = rs.getString("c_comment");
	            	} else {
						try {
	    					if(rs.getClob("c_comment") != null){
	    						cComment = JDBCUtil.getClobString(rs.getClob("c_comment"));
	    					}
						} catch (IOException e) {
		        			log.error(e.getMessage());
		        			child.setComment(null);
						} 
	            	}
					child.setComment(cComment); 
				
					// c_metadataxml
					String c_xml = null;
					if((dbType).equals("POSTGRES")) {
						c_xml = rs.getString("c_metadataxml");
					} else {
						if(rs.getClob("c_metadataxml") != null){
							// String c_xml = null;
							try {
								c_xml = JDBCUtil.getClobString(rs.getClob("c_metadataxml"));
							} catch (IOException e) {
								log.error(e.getMessage());
		            			child.setMetadataxml(null);
							}
						}
					}
					
					if ((c_xml!=null)&&(c_xml.trim().length()>0)&&(!c_xml.equals("(null)"))) {
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
							child.setMetadataxml(null);
						} catch (IOException e1) {
							log.error(e1.getMessage());
							child.setMetadataxml(null);
						}
						if (rootElement != null) {
							XmlValueType xml = new XmlValueType();
							xml.getAny().add(rootElement);
							child.setMetadataxml(xml);
						}
					} else {
						child.setMetadataxml(null);
					}
				}	
				if((node.getType().equals("all"))){
					DTOFactory factory = new DTOFactory();
					// make sure date isnt null before converting to XMLGregorianCalendar
					Date date = rs.getDate("update_date");
					if (date == null)
						child.setUpdateDate(null);
					else 
						child.setUpdateDate(factory.getXMLGregorianCalendar(date.getTime())); 

					date = rs.getDate("download_date");
					if (date == null)
						child.setDownloadDate(null);
					else 
						child.setDownloadDate(factory.getXMLGregorianCalendar(date.getTime())); 

					date = rs.getDate("import_date");
					if (date == null)
						child.setImportDate(null);
					else 
						child.setImportDate(factory.getXMLGregorianCalendar(date.getTime())); 

					child.setSourcesystemCd(rs.getString("sourcesystem_cd"));

				}
				return child;
			}
		};
		return mapper;
	}
	
	public List findChildrenByParent(final GetModifierChildrenType modifierChildrenType, ProjectType projectInfo, DBInfoType dbInfo) throws I2B2DAOException, I2B2Exception{

	//	log.debug("MOD: " + modifierChildrenType.getParent());
	//	log.debug("MOD: " + modifierChildrenType.getAppliedPath());

		// find return parameters
		String parameters = MOD_DEFAULT;		
		if (modifierChildrenType.getType().equals("limited")){
			parameters = MOD_LIMITED;
		}
		else if (modifierChildrenType.getType().equals("core")){
			parameters = MOD_CORE;
		}
		else if (modifierChildrenType.getType().equals("all")){
			parameters = MOD_CORE + ALL;
		}
		if(modifierChildrenType.isBlob() == true)
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
			 if(role.toUpperCase().equals("DATA_PROT")) {
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
		String tableCd = StringUtil.getTableCd(modifierChildrenType.getParent());
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
	//		log.info("getChildren " + tableSql);
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw new I2B2DAOException("Database Error");
			}
		}

		String path = StringUtil.escapeBackslash(StringUtil.getPath(modifierChildrenType.getParent()), dbInfo.getDb_serverType());
		String searchPath =  path + "%";

// Lookup to get chlevel + 1 ---  dont allow synonyms so we only get one result back
				
		String levelSql = "select c_hlevel from " + metadataSchema+tableName  + " where c_fullname = ?  and c_synonym_cd = 'N' and m_applied_path = ? and m_exclusion_cd is null";

	    int level = 0;
		try {
			level = jt.queryForInt(levelSql, path, modifierChildrenType.getAppliedPath());
		} catch (DataAccessException e1) {
			// should only get 1 result back  (path == c_fullname which should be unique)
			log.error(e1.getMessage());
			throw new I2B2DAOException("Database Error");
		}

		String hidden = "";
		if(modifierChildrenType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";
	
		String synonym = "";
		if(modifierChildrenType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";
		
		String sql = "select " + parameters + " from "+ metadataSchema+ tableName + " where m_exclusion_cd is null" + hidden + synonym + " and c_fullname in (";
		
		String inclusionSql = " (select c_fullname from " + metadataSchema+tableName  + " where c_fullname like ? and c_hlevel = ? and m_exclusion_cd is null "; 

 
		if(dbInfo.getDb_serverType().equals("ORACLE"))
			sql = sql + inclusionSql + " ) MINUS (";
		else
			sql = sql + inclusionSql +  " ) EXCEPT (";
			
		
		String appliedConcept = StringUtil.getLiteralPath(modifierChildrenType.getAppliedConcept());

		String exclusionSql = " select c_fullname from " + metadataSchema+tableName  + " where c_fullname like ? and c_hlevel = ? and m_exclusion_cd is not null " +
				" and ( m_applied_path = ?  or m_applied_path = ? ) ) )";
			
		sql = sql +  exclusionSql +  " order by c_name ";	
		
		
		log.info("Find Mod children:" + sql + " " + path + " " + level + " " + appliedConcept);
		
		final boolean ofuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);
		
		ParameterizedRowMapper<ModifierType> modMapper = getModMapper(new NodeType (modifierChildrenType), ofuscatedUserFlag, dbInfo.getDb_serverType());
		
		List queryResult = null;
		try {
			queryResult = jt.query(sql, modMapper, searchPath, (level + 1), searchPath, (level+1), appliedConcept, (appliedConcept+"%"));
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw new I2B2DAOException("Database Error");
		}
		log.debug("result size = " + queryResult.size());
		
		
		return queryResult;

	}
	
	

	public List findByFullname(final GetModifierInfoType modifierInfoType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{

		// find return parameters
		String parameters = MOD_DEFAULT;	
		if (modifierInfoType.getType().equals("limited")){
			parameters = MOD_LIMITED;
		}
		else if (modifierInfoType.getType().equals("core")){
			parameters = MOD_CORE;
		}
		else if (modifierInfoType.getType().equals("all")){
			parameters = MOD_CORE + ALL;
		}
		if(modifierInfoType.isBlob() == true)
			parameters = parameters + BLOB;
				
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
		if (projectInfo.getRole().size() == 0)
		{
			log.error("no role found for this user in project: " + projectInfo.getName());
			I2B2DAOException e = new I2B2DAOException("No role found for user");
			throw e;
		}
		
		Boolean protectedAccess = false;
		Iterator it = projectInfo.getRole().iterator();
		while (it.hasNext()){
			 String role = (String) it.next();
			 if(role.toUpperCase().equals("DATA_PROT")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		//tableCd to table name conversion
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = (rs.getString("c_table_name"));
	            return name;
	        }
		};
		
		//extract table code
		String tableCd = StringUtil.getTableCd(modifierInfoType.getSelf());
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}

		String path = StringUtil.escapeBackslash(StringUtil.getPath(modifierInfoType.getSelf()), dbInfo.getDb_serverType());
		String searchPath = path;


		String hidden = "";
		if(modifierInfoType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";
	
		String synonym = "";
		if(modifierInfoType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";
		
		String sql = "select " + parameters +" from " + metadataSchema+tableName  + " where c_fullname = ? and m_applied_path = ?"; 
		sql = sql + hidden + synonym + " order by c_name ";
 
		//log.info(sql + " " + path + " " + level);
		
		final boolean ofuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);
		
		ParameterizedRowMapper<ModifierType> modMapper = getModMapper(new NodeType (modifierInfoType), ofuscatedUserFlag, dbInfo.getDb_serverType());
		
		List queryResult = null;
		try {
			queryResult = jt.query(sql, modMapper, searchPath, modifierInfoType.getAppliedPath() );
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());
		
		
		return queryResult;

	}
	
	public List findModifierNameInfo(final VocabRequestType vocabType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{

		// find return parameters
		String parameters = NAME_DEFAULT;		
		
		if (vocabType.getType().equals("limited")){
			parameters = MOD_LIMITED;
		}
		
		else if (vocabType.getType().equals("core")){
			parameters = MOD_CORE;
		}

		else if (vocabType.getType().equals("all")){
			parameters = MOD_CORE + ALL;
		}
		if(vocabType.isBlob() == true)
			parameters = parameters + BLOB;
				
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
	//	log.info(metadataSchema);
		
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
			 if(role.toUpperCase().equals("DATA_PROT")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		//tableCd to table name conversion
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = (rs.getString("c_table_name"));
	            return name;
	        }
		};
		
		//extract table code
		String tableCd = StringUtil.getTableCd(vocabType.getSelf());
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}

	//   prevent SQL injection
		String value = vocabType.getMatchStr().getValue();
		if(value.contains("'")){
			value = vocabType.getMatchStr().getValue().replaceAll("'", "''");
		}
		String nameInfoSql = null;
		    String compareName = null;
			String path = StringUtil.escapeBackslash(StringUtil.getLiteralPath(vocabType.getSelf()), dbInfo.getDb_serverType());
			// dont do the sql injection replace; it breaks the service.
		    if(vocabType.getMatchStr().getStrategy().equals("exact")) {
		    	compareName = value.toUpperCase();
		    	nameInfoSql = "select " + parameters  + " from " + metadataSchema + tableName + " where upper(c_name) = '" + compareName + "'" ;//and m_applied_path = '" + path + "'";
		    	
		  
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("left")){
		    	compareName = value.toUpperCase() + "%";
		    	nameInfoSql = "select " + parameters  + " from " + metadataSchema + tableName +" where upper(c_name) like '" + compareName + "'";//  and m_applied_path = '" + path + "'";
		    	
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("right")) {
		    	compareName =  "%" + value.toUpperCase();
		    	nameInfoSql = "select " + parameters  + " from " + metadataSchema + tableName +" where upper(c_name) like '" + compareName + "'";//and m_applied_path = '" + path + "'";
		    	
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("contains")) {
		    	if(!(value.contains(" "))){
		    		compareName =  "%" + value.toUpperCase() + "%";
		    		nameInfoSql = "select " + parameters  + " from " + metadataSchema + tableName +" where upper(c_name) like '" + compareName + "'";  //and m_applied_path = '" + path + "'";

		    	}else{
		    		nameInfoSql = "select " + parameters  + " from " + metadataSchema + tableName ;
		    		nameInfoSql = nameInfoSql + parseMatchString(value);// + "and m_applied_path = '" + path + "'";
		    		compareName = null;
		    	}
		    }

		    String wherePath = " and m_applied_path = '" + path + "' ";
		    String hidden = "";
			if(vocabType.isHiddens() == false)
				hidden = " and c_visualattributes not like '_H%' ";
		
			String synonym = "";
			if(vocabType.isSynonyms() == false)
				synonym = " and c_synonym_cd = 'N' ";
			
		    String modNameInfoSql = nameInfoSql + wherePath + hidden + synonym;
				while (path.length() > 3) {
					if(path.endsWith("%")){
						path = path.substring(0, path.length()-2);
						path = path.substring(0, path.lastIndexOf("\\") + 1) + "%";			
					}
					else
						path = path + "%";
					wherePath = " and m_applied_path = '" + StringUtil.escapeBackslash(path, dbInfo.getDb_serverType()) + "' ";
					modNameInfoSql = modNameInfoSql + " union all " + nameInfoSql + wherePath + hidden + synonym;
				}
		    


		
		
		modNameInfoSql = modNameInfoSql + " order by c_name ";
	    
		log.debug("MODnameInfo: " + modNameInfoSql + " " +compareName);
		boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);

		ParameterizedRowMapper<ModifierType> modMapper = getModMapper(new NodeType(vocabType), obfuscatedUserFlag, dbInfo.getDb_serverType());
		
		List queryResult = null;
		try {
			if(compareName != null)
				queryResult = jt.query(modNameInfoSql, modMapper);
			else
				queryResult = jt.query(modNameInfoSql, modMapper);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());
		
		
		return queryResult;

	}
	
	public List findModifierCodeInfo(final VocabRequestType vocabType, ProjectType projectInfo, DBInfoType dbInfo) throws DataAccessException, I2B2Exception{

		// find return parameters
		String parameters = NAME_DEFAULT;	
		
		if (vocabType.getType().equals("limited")){
			parameters = MOD_LIMITED;
		}
		
		else if (vocabType.getType().equals("core")){
			parameters = MOD_CORE;
		}
		
		else if (vocabType.getType().equals("all")){
			parameters = MOD_CORE + ALL;
		}
		if(vocabType.isBlob() == true)
			parameters = parameters + BLOB;
				
		String metadataSchema = dbInfo.getDb_fullSchema();
		setDataSource(dbInfo.getDb_dataSource());
		
		String dbType = dbInfo.getDb_serverType();
		
//		log.info(metadataSchema);
		
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
			 if(role.toUpperCase().equals("DATA_PROT")) {
				 protectedAccess = true;
				 break;
			 }
		}
		
		//tableCd to table name conversion
		ParameterizedRowMapper<String> map = new ParameterizedRowMapper<String>() {
	        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
	            String name = (rs.getString("c_table_name"));
	            return name;
	        }
		};
		
		//extract table code
		String tableCd = StringUtil.getTableCd(vocabType.getSelf());
		String tableName=null;
		if (!protectedAccess){
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ? and c_protected_access = ? ";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd, "N");	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}else {
			String tableSql = "select distinct(c_table_name) from " + metadataSchema + "table_access where c_table_cd = ?";
			try {
				tableName = jt.queryForObject(tableSql, map, tableCd);	    
			} catch (DataAccessException e) {
				log.error(e.getMessage());
				throw e;
			}
		}

		String hidden = "";
		if(vocabType.isHiddens() == false)
			hidden = " and c_visualattributes not like '_H%'";
	
		String synonym = "";
		if(vocabType.isSynonyms() == false)
			synonym = " and c_synonym_cd = 'N'";

		// I have to do this the hard way because there are a dynamic number of codes to pass in
		//   prevent SQL injection
		String value = vocabType.getMatchStr().getValue();
		if(value.contains("'")){
			value = vocabType.getMatchStr().getValue().replaceAll("'", "''");
		}
		 String whereClause = null;
			
		    if(vocabType.getMatchStr().getStrategy().equals("exact")) {
		    	whereClause = " where upper(c_basecode) = '" + value.toUpperCase()+ "'";
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("left")){
		    	whereClause = " where upper(c_basecode) like '" + value.toUpperCase() + "%'";
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("right")) {
		    	value = value.replaceFirst(":", ":%");
		    	whereClause = " where upper(c_basecode) like '%" +  value.toUpperCase() + "'";
		    }
		    
		    else if(vocabType.getMatchStr().getStrategy().equals("contains")) {
		    	value = value.replaceFirst(":", ":%");
		    	whereClause = " where upper(c_basecode) like '%" + value.toUpperCase() + "%'";
		    }

	//	log.debug(vocabType.getMatchStr().getStrategy() + whereClause);
		
		
		String codeInfoSql = "select " + parameters + " from " + metadataSchema + tableName ;
		String path = StringUtil.escapeBackslash(StringUtil.getLiteralPath(vocabType.getSelf()), dbInfo.getDb_serverType());
		String modAppliedPath = " and m_applied_path = '" + path + "' ";;
		String modCodeInfoSql = codeInfoSql + whereClause +  modAppliedPath + hidden + synonym;
		while (path.length() > 3) {
			if(path.endsWith("%")){
				path = path.substring(0, path.length()-2);
				path = path.substring(0, path.lastIndexOf("\\") + 1) + "%";			
			}
			else
				path = path + "%";
			modAppliedPath = " and m_applied_path = '" + StringUtil.escapeBackslash(path, dbInfo.getDb_serverType())  + "' ";
			modCodeInfoSql = modCodeInfoSql + " union all " + codeInfoSql + whereClause + modAppliedPath + hidden + synonym;
		}

		log.debug("MODCodeInfo " + modCodeInfoSql);
		boolean obfuscatedUserFlag = Roles.getInstance().isRoleOfuscated(projectInfo);
		ParameterizedRowMapper<ModifierType> modMapper = getModMapper(new NodeType(vocabType),obfuscatedUserFlag, dbInfo.getDb_serverType());

		List queryResult = null;
		try {
			queryResult = jt.query(modCodeInfoSql, modMapper);
		} catch (DataAccessException e) {
			log.error(e.getMessage());
			throw e;
		}
		log.debug("result size = " + queryResult.size());

		return queryResult;

	}
}
