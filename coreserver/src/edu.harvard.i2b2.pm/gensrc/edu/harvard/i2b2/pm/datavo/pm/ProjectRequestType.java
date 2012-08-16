//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.08.16 at 12:50:43 PM EDT 
//


package edu.harvard.i2b2.pm.datavo.pm;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for project_requestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="project_requestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="request_xml" type="{http://www.i2b2.org/xsd/cell/pm/1.1/}blobType"/>
 *         &lt;element name="project_id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="change_date" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="entry_date" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="changeby_char" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="status_cd" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="submit_char" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "project_requestType", propOrder = {
    "title",
    "requestXml",
    "projectId",
    "changeDate",
    "entryDate",
    "changebyChar",
    "statusCd",
    "submitChar"
})
public class ProjectRequestType {

    @XmlElement(required = true)
    protected String title;
    @XmlElement(name = "request_xml", required = true)
    protected BlobType requestXml;
    @XmlElement(name = "project_id", required = true)
    protected String projectId;
    @XmlElement(name = "change_date", required = true)
    protected XMLGregorianCalendar changeDate;
    @XmlElement(name = "entry_date", required = true)
    protected XMLGregorianCalendar entryDate;
    @XmlElement(name = "changeby_char", required = true)
    protected String changebyChar;
    @XmlElement(name = "status_cd", required = true)
    protected String statusCd;
    @XmlElement(name = "submit_char", required = true)
    protected String submitChar;
    @XmlAttribute(required = true)
    protected String id;

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the requestXml property.
     * 
     * @return
     *     possible object is
     *     {@link BlobType }
     *     
     */
    public BlobType getRequestXml() {
        return requestXml;
    }

    /**
     * Sets the value of the requestXml property.
     * 
     * @param value
     *     allowed object is
     *     {@link BlobType }
     *     
     */
    public void setRequestXml(BlobType value) {
        this.requestXml = value;
    }

    /**
     * Gets the value of the projectId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Sets the value of the projectId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProjectId(String value) {
        this.projectId = value;
    }

    /**
     * Gets the value of the changeDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getChangeDate() {
        return changeDate;
    }

    /**
     * Sets the value of the changeDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setChangeDate(XMLGregorianCalendar value) {
        this.changeDate = value;
    }

    /**
     * Gets the value of the entryDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEntryDate() {
        return entryDate;
    }

    /**
     * Sets the value of the entryDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEntryDate(XMLGregorianCalendar value) {
        this.entryDate = value;
    }

    /**
     * Gets the value of the changebyChar property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChangebyChar() {
        return changebyChar;
    }

    /**
     * Sets the value of the changebyChar property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChangebyChar(String value) {
        this.changebyChar = value;
    }

    /**
     * Gets the value of the statusCd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatusCd() {
        return statusCd;
    }

    /**
     * Sets the value of the statusCd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatusCd(String value) {
        this.statusCd = value;
    }

    /**
     * Gets the value of the submitChar property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubmitChar() {
        return submitChar;
    }

    /**
     * Sets the value of the submitChar property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubmitChar(String value) {
        this.submitChar = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
