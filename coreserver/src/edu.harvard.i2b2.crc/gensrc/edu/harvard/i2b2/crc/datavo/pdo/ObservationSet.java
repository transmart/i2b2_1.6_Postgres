//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.08.17 at 03:42:02 PM EDT 
//


package edu.harvard.i2b2.crc.datavo.pdo;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="observation" type="{http://www.i2b2.org/xsd/hive/pdo/1.1/}observationType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="panel_name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "observation"
})
@XmlRootElement(name = "observation_set")
public class ObservationSet {

    @XmlElement(required = true)
    protected List<ObservationType> observation;
    @XmlAttribute(name = "panel_name")
    protected String panelName;

    /**
     * Gets the value of the observation property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the observation property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObservation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ObservationType }
     * 
     * 
     */
    public List<ObservationType> getObservation() {
        if (observation == null) {
            observation = new ArrayList<ObservationType>();
        }
        return this.observation;
    }

    /**
     * Gets the value of the panelName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPanelName() {
        return panelName;
    }

    /**
     * Sets the value of the panelName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPanelName(String value) {
        this.panelName = value;
    }

}
