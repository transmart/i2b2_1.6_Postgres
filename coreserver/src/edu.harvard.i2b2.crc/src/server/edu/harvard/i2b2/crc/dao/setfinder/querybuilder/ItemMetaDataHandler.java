package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import javax.xml.stream.XMLStreamException;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.ontology.ModifierType;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;
import edu.harvard.i2b2.crc.util.SqlClauseUtil;

public class ItemMetaDataHandler {

	private String queryXML = "";
	protected final Log log = LogFactory.getLog(ItemMetaDataHandler.class);

	public ItemMetaDataHandler(String queryXML) {
		this.queryXML = queryXML;
	}

	public ConceptType getMetaDataFromOntologyCell(String itemKey)
			throws ConceptNotFoundException, OntologyException {
		ConceptType conceptType = null;
		CallOntologyUtil ontologyUtil;
		try {
			ontologyUtil = new CallOntologyUtil(queryXML);
			// if regular concepts
			conceptType = ontologyUtil.callOntology(itemKey);
		} catch (JAXBUtilException e) {

			log.error("Error while fetching metadata [" + itemKey
					+ "] from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (I2B2Exception e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (AxisFault e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (XMLStreamException e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ itemKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		}

		if (conceptType == null) {
			// add it the message
			// ignoredItemMessageBuffer.append("\n [" + itemKey + "] in panel #"
			// + i + "\n");
			throw new ConceptNotFoundException("[" + itemKey + "] ");

		} else {
			String theData = conceptType.getDimcode();			
			if (conceptType.getColumndatatype() != null
					&& conceptType.getColumndatatype().equalsIgnoreCase("T")) {
				theData = SqlClauseUtil.handleMetaDataTextValue(
						conceptType.getOperator(), conceptType.getDimcode());
			} else if (conceptType.getColumndatatype() != null
					&& conceptType.getColumndatatype().equalsIgnoreCase("N")) {
				theData = SqlClauseUtil.handleMetaDataNumericValue(
						conceptType.getOperator(), conceptType.getDimcode());
			} else if (conceptType.getColumndatatype() != null
					&& conceptType.getColumndatatype().equalsIgnoreCase("D")) {
				theData = SqlClauseUtil.handleMetaDataDateValue(
						conceptType.getOperator(), conceptType.getDimcode());
			}			
			conceptType.setDimcode(theData);
		}

		return conceptType;
	}

	public ModifierType getModifierDataFromOntologyCell(String modifierKey, String appliedPath)
			throws ConceptNotFoundException, OntologyException {
		ModifierType modifierType = null;
		CallOntologyUtil ontologyUtil;
		try {
			QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();
			String ontologyUrl = qpUtil
			.getCRCPropertyValue(QueryProcessorUtil.ONTOLOGYCELL_ROOT_WS_URL_PROPERTIES);
			String getModifierOperationName = qpUtil
			.getCRCPropertyValue(QueryProcessorUtil.ONTOLOGYCELL_GETMODIFIERINFO_URL_PROPERTIES);
			String ontologyGetModifierInfoUrl = ontologyUrl
			+ getModifierOperationName;
			log.debug("Ontology getModifierinfo url from property file ["+ ontologyGetModifierInfoUrl + "]");
			ontologyUtil = new CallOntologyUtil(ontologyGetModifierInfoUrl,queryXML);
			// if regular concepts
			modifierType = ontologyUtil.callGetModifierInfo(modifierKey,appliedPath);
		} catch (JAXBUtilException e) {

			log.error("Error while fetching metadata [" + modifierKey
					+ "] from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ modifierKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (I2B2Exception e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ modifierKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (AxisFault e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ modifierKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		} catch (XMLStreamException e) {
			log.error("Error while fetching metadata from ontology ", e);
			throw new OntologyException("Error while fetching metadata ["
					+ modifierKey + "] from ontology "
					+ StackTraceUtil.getStackTrace(e));
		}

		if (modifierType == null) {
			// add it the message
			// ignoredItemMessageBuffer.append("\n [" + itemKey + "] in panel #"
			// + i + "\n");
			throw new ConceptNotFoundException("Error getting modifierinfo for modifier key [" + modifierKey + "] and appliedPath [" + appliedPath + "]");

		} else {
			String theData = modifierType.getDimcode();
			if (modifierType.getColumndatatype() != null
					&& modifierType.getColumndatatype().equalsIgnoreCase("T")) {
				theData = SqlClauseUtil.handleMetaDataTextValue(
						modifierType.getOperator(), modifierType.getDimcode());
			} else if (modifierType.getColumndatatype() != null
					&& modifierType.getColumndatatype().equalsIgnoreCase("N")) {
				theData = SqlClauseUtil.handleMetaDataNumericValue(
						modifierType.getOperator(), modifierType.getDimcode());
			} else if (modifierType.getColumndatatype() != null
					&& modifierType.getColumndatatype().equalsIgnoreCase("D")) {
				theData = SqlClauseUtil.handleMetaDataDateValue(
						modifierType.getOperator(), modifierType.getDimcode());
			}
			modifierType.setDimcode(theData);
		}

		return modifierType;
	}
}
