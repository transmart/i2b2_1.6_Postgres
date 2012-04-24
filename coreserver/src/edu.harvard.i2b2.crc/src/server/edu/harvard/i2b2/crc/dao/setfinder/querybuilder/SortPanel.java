package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

import edu.harvard.i2b2.common.exception.I2B2DAOException;
import edu.harvard.i2b2.common.exception.StackTraceUtil;
import edu.harvard.i2b2.common.util.jaxb.JAXBUtilException;
import edu.harvard.i2b2.crc.datavo.ontology.ConceptType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.delegate.ontology.CallOntologyUtil;
import edu.harvard.i2b2.crc.util.ItemKeyUtil;

public class SortPanel {
	/** log **/
	protected final Log log = LogFactory.getLog(getClass());

	public List<Element> getSortedPanelList(List<Element> panelList,
			CallOntologyUtil ontologyUtil) throws AxisFault, I2B2DAOException,
			XMLStreamException, JAXBUtilException {

		Map<Integer, Integer> panelTotalMap = new HashMap<Integer, Integer>();
		Map<Integer, Element> panelMap = new HashMap<Integer, Element>();
		int panelIndex = 0;
		List<Element> sortedPanelArray = new ArrayList<Element>();
		for (Iterator<Element> itr = panelList.iterator(); itr.hasNext();) {
			panelIndex++;
			Element panelXml = (org.jdom.Element) itr.next();
			List itemList = panelXml.getChildren("item");
			// calculate the total for each item
			int panelTotal = 0;
			for (Iterator iterator = itemList.iterator(); iterator.hasNext();) {
				Element itemXml = (org.jdom.Element) iterator.next();
				String itemKey = itemXml.getChildText("item_key");
				String itemClass = itemXml.getChildText("class");
				ConceptType conceptType = ontologyUtil.callOntology(itemKey);
				if (conceptType != null && conceptType.getTotalnum() != null) {
					panelTotal += conceptType.getTotalnum();
				}
			}
			panelMap.put(panelIndex, panelXml);
			panelTotalMap.put(panelIndex, panelTotal);
			log.debug("Panel's Total num [" + panelTotal
					+ "] and the panel index [" + panelIndex + "]");

		}

		HashMap yourMap = new HashMap();

		HashMap map = new LinkedHashMap();

		List yourMapKeys = new ArrayList(panelTotalMap.keySet());
		List yourMapValues = new ArrayList(panelTotalMap.values());
		List sortedMapValues = new ArrayList(yourMapValues);

		Collections.sort(sortedMapValues);

		int size = yourMapValues.size();
		int indexInMapValues = 0;
		for (int i = 0; i < size; i++) {
			indexInMapValues = yourMapValues.indexOf(sortedMapValues.get(i));
			map.put(yourMapKeys.get(indexInMapValues), sortedMapValues.get(i));
			yourMapValues.set(indexInMapValues, -1);
		}
		Set ref = map.keySet();
		Iterator it = ref.iterator();
		int panelIndexHash = 0;
		while (it.hasNext()) {
			panelIndexHash = (Integer) it.next();

			sortedPanelArray.add(panelMap.get(panelIndexHash));
		}
		return sortedPanelArray;

	}

	public List<PanelType> sortedPanelList(List<PanelType> panelList,
			CallOntologyUtil ontologyUtil) throws I2B2DAOException {

		Map<Integer, Integer> panelTotalMap = new HashMap<Integer, Integer>();
		Map<Integer, PanelType> panelMap = new HashMap<Integer, PanelType>();
		int panelIndex = 0;
		List<PanelType> sortedPanelArray = new ArrayList<PanelType>();
		for (PanelType panelType : panelList) {
			panelIndex++;
			List<ItemType> itemList = panelType.getItem();
			// calculate the total for each item
			int panelTotal = 0;
			String itemKey = null;
			for (ItemType itemType : itemList) {
				ConceptType conceptType = null;

				try {
					itemKey = itemType.getItemKey();
						if (ItemKeyUtil.isConceptKey(itemKey)) { 
						conceptType = ontologyUtil.callOntology(itemKey);
						if (conceptType != null && conceptType.getTotalnum() !=null) {
							panelTotal += conceptType.getTotalnum();
						}
					}

				} catch (AxisFault e) {
					log.error("Error while fetching metadata [" + itemKey
							+ "] from ontology ", e);
					throw new OntologyException(
							"Error while fetching metadata [" + itemKey
									+ "] from ontology "
									+ StackTraceUtil.getStackTrace(e));
				} catch (XMLStreamException e) {
					log.error("Error while fetching metadata [" + itemKey
							+ "] from ontology ", e);
					throw new OntologyException(
							"Error while fetching metadata [" + itemKey
									+ "] from ontology "
									+ StackTraceUtil.getStackTrace(e));
				} catch (JAXBUtilException e) {
					log.error("Error while fetching metadata [" + itemKey
							+ "] from ontology ", e);
					throw new OntologyException(
							"Error while fetching metadata [" + itemKey
									+ "] from ontology "
									+ StackTraceUtil.getStackTrace(e));
				}

			}
			panelMap.put(panelIndex, panelType);
			panelTotalMap.put(panelIndex, panelTotal);
			log.debug("Panel's Total num [" + panelTotal
					+ "] and the panel index [" + panelIndex + "]");

		}

		HashMap yourMap = new HashMap();

		HashMap map = new LinkedHashMap();

		List yourMapKeys = new ArrayList(panelTotalMap.keySet());
		List yourMapValues = new ArrayList(panelTotalMap.values());
		List sortedMapValues = new ArrayList(yourMapValues);

		Collections.sort(sortedMapValues);

		int size = yourMapValues.size();
		int indexInMapValues = 0;
		for (int i = 0; i < size; i++) {
			indexInMapValues = yourMapValues.indexOf(sortedMapValues.get(i));
			map.put(yourMapKeys.get(indexInMapValues), sortedMapValues.get(i));
			yourMapValues.set(indexInMapValues, -1);
		}
		Set ref = map.keySet();
		Iterator it = ref.iterator();
		int panelIndexHash = 0;
		while (it.hasNext()) {
			panelIndexHash = (Integer) it.next();

			sortedPanelArray.add(panelMap.get(panelIndexHash));
		}
		return sortedPanelArray;

	}
}
