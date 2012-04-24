package edu.harvard.i2b2.crc.dao.setfinder.querybuilder;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import edu.harvard.i2b2.crc.datavo.db.DataSourceLookup;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ConstrainDateType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.InclusiveType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.PanelType;
import edu.harvard.i2b2.crc.datavo.setfinder.query.ItemType.ConstrainByDate;

public class DateConstrainUtil {

	DateConstrainHandler dateConstrainHandler = null;

	public DateConstrainUtil(DataSourceLookup dataSourceLookup) {
		dateConstrainHandler = new DateConstrainHandler(dataSourceLookup);

	}

	public String buildPanelDateSql(PanelType panelType) {
		String panelDateConstrain = " ";
		if (panelType.getPanelDateFrom() != null
				|| panelType.getPanelDateTo() != null) {
			panelDateConstrain = generatePanelDateConstrain(
					dateConstrainHandler, panelType.getPanelDateFrom(),
					panelType.getPanelDateTo());
		}
		return panelDateConstrain;
	}

	public String buildItemDateSql(ItemType item) {
		String dateConstrainSql = null;
		String fullItemDateSql = "";
		List<ConstrainByDate> constrainByDateList = item.getConstrainByDate();
		for (ConstrainByDate constrainByDate : constrainByDateList) {
			ConstrainDateType dateFrom = constrainByDate.getDateFrom();
			ConstrainDateType dateTo = constrainByDate.getDateTo();

			String dateFromColumn = null, dateToColumn = null;
			InclusiveType dateFromInclusive = null, dateToInclusive = null;
			XMLGregorianCalendar dateFromValue = null, dateToValue = null;

			if (dateFrom != null || dateTo != null) {

				if (dateFrom != null) {
					dateFromInclusive = dateFrom.getInclusive();
					dateFromValue = dateFrom.getValue();
					if (dateFrom.getTime() != null
							&& dateFrom.getTime().name() != null
							&& dateFrom.getTime().name().equalsIgnoreCase(
									dateFrom.getTime().END_DATE.name())) {
						dateFromColumn = "end_date";
					} else {
						dateFromColumn = "start_date";
					}

				}

				if (dateTo != null) {
					dateToInclusive = dateTo.getInclusive();
					dateToValue = dateTo.getValue();
					if (dateTo.getTime() != null
							&& dateTo.getTime().name() != null
							&& dateTo.getTime().name().equalsIgnoreCase(
									dateTo.getTime().END_DATE.name())) {
						dateToColumn = "end_date";
					} else {
						dateToColumn = "start_date";
					}
				}

				dateConstrainSql = dateConstrainHandler
						.constructDateConstrainClause(dateFromColumn,
								dateToColumn, dateFromInclusive,
								dateToInclusive, dateFromValue, dateToValue);
				if (dateConstrainSql != null) {
					fullItemDateSql += "  " + dateConstrainSql + "\n";
				}

			}
		}
		
		return fullItemDateSql;
	}

	private String generatePanelDateConstrain(
			DateConstrainHandler dateConstrainHandler,
			ConstrainDateType dateFrom, ConstrainDateType dateTo) {

		String dateFromColumn = null, dateToColumn = null;
		InclusiveType dateFromInclusive = null, dateToInclusive = null;
		XMLGregorianCalendar dateFromValue = null, dateToValue = null;
		String dateConstrainSql = null;

		if (dateFrom != null || dateTo != null) {

			if (dateFrom != null) {
				dateFromInclusive = dateFrom.getInclusive();
				dateFromValue = dateFrom.getValue();

				if (dateFrom.getTime() != null
						&& dateFrom.getTime().name() != null
						&& dateFrom.getTime().name().equalsIgnoreCase(
								dateFrom.getTime().END_DATE.name())) {
					dateFromColumn = "end_date";
				} else {

					dateFromColumn = "start_date";
				}

			}

			if (dateTo != null) {
				dateToInclusive = dateTo.getInclusive();
				dateToValue = dateTo.getValue();

				if (dateTo.getTime() != null
						&& dateTo.getTime().name() != null
						&& dateTo.getTime().name().equalsIgnoreCase(
								dateTo.getTime().END_DATE.name())) {
					dateToColumn = "end_date";
				} else {

					dateToColumn = "start_date";
				}

			}

			dateConstrainSql = dateConstrainHandler
					.constructDateConstrainClause(dateFromColumn, dateToColumn,
							dateFromInclusive, dateToInclusive, dateFromValue,
							dateToValue);
		}
		return dateConstrainSql;
	}
}
