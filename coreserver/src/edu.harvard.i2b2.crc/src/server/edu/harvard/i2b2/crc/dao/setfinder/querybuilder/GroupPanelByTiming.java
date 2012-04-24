package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;

public class GroupPanelByTiming {

	public HashMap<String, List<PanelType>> groupPanelByTiming(
			List<PanelType> panelList, String queryTiming) {
		List<PanelType> anyPanelList = new ArrayList<PanelType>();
		List<PanelType> sameVisitPanelList = new ArrayList<PanelType>();
		List<PanelType> sameInstanceNumPanelList = new ArrayList<PanelType>();
		// iterate the panel list and put them in seperate bucket
		String panelTiming = null, finalTiming = QueryTimingHandler.ANY;
		HashMap<String, List<PanelType>> panelTimingMap = new HashMap<String, List<PanelType>>();
		for (PanelType panel : panelList) {
			panelTiming = panel.getPanelTiming();
			if (panelTiming != null) {
				finalTiming = panelTiming;
			} else if (queryTiming != null) {
				finalTiming = queryTiming;
			} else {
				finalTiming = "ANY";
			}

			if (finalTiming.equals(QueryTimingHandler.ANY)) {
				anyPanelList.add(panel);
			} else if (finalTiming.equalsIgnoreCase(QueryTimingHandler.SAME)
					|| finalTiming.equals(QueryTimingHandler.SAMEVISIT)) {
				sameVisitPanelList.add(panel);
			} else if (finalTiming
					.equalsIgnoreCase(QueryTimingHandler.SAMEINSTANCENUM)) {
				sameInstanceNumPanelList.add(panel);
			}
		}
		//
		panelTimingMap.put(QueryTimingHandler.ANY, anyPanelList);
		panelTimingMap.put(QueryTimingHandler.SAMEVISIT, sameVisitPanelList);
		panelTimingMap.put(QueryTimingHandler.SAMEINSTANCENUM,
				sameInstanceNumPanelList);
		return panelTimingMap;
	}

}
