package edu.harvard.i2b2.crc.datavo.db;

// Generated Oct 10, 2006 5:52:18 PM by Hibernate Tools 3.1.0.beta5

import java.util.HashSet;
import java.util.Set;

/**
 * QtQueryResultType generated by hbm2java
 */
public class QtQueryResultType implements java.io.Serializable {

	// Fields

	private int resultTypeId;
	private String name;
	private String description;
	private String displayType;
	private String visualAttributeType;

	private Set<QtQueryResultInstance> qtQueryResultInstances = new HashSet<QtQueryResultInstance>(
			0);

	// Constructors

	/** default constructor */
	public QtQueryResultType() {
	}

	/** minimal constructor */
	public QtQueryResultType(int resultTypeId) {
		this.resultTypeId = resultTypeId;
	}

	/** full constructor */
	public QtQueryResultType(int resultTypeId, String name, String description,
			Set<QtQueryResultInstance> qtQueryResultInstances) {
		this.resultTypeId = resultTypeId;
		this.name = name;
		this.description = description;
		this.qtQueryResultInstances = qtQueryResultInstances;
	}

	// Property accessors
	public int getResultTypeId() {
		return this.resultTypeId;
	}

	public void setResultTypeId(int resultTypeId) {
		this.resultTypeId = resultTypeId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayType() {
		return displayType;
	}

	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

	public String getVisualAttributeType() {
		return visualAttributeType;
	}

	public void setVisualAttributeType(String visualAttributeType) {
		this.visualAttributeType = visualAttributeType;
	}

	public Set<QtQueryResultInstance> getQtQueryResultInstances() {
		return this.qtQueryResultInstances;
	}

	public void setQtQueryResultInstances(
			Set<QtQueryResultInstance> qtQueryResultInstances) {
		this.qtQueryResultInstances = qtQueryResultInstances;
	}

}
