//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.08.17 at 03:42:02 PM EDT 
//


package edu.harvard.i2b2.crc.datavo.pdo.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for pageRange_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="pageRange_Type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="first_index" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="last_index" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="total" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "pageRange_Type")
public class PageRangeType {

    @XmlAttribute(name = "first_index")
    protected Integer firstIndex;
    @XmlAttribute(name = "last_index")
    protected Integer lastIndex;
    @XmlAttribute
    protected Integer total;

    /**
     * Gets the value of the firstIndex property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getFirstIndex() {
        return firstIndex;
    }

    /**
     * Sets the value of the firstIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setFirstIndex(Integer value) {
        this.firstIndex = value;
    }

    /**
     * Gets the value of the lastIndex property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLastIndex() {
        return lastIndex;
    }

    /**
     * Sets the value of the lastIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLastIndex(Integer value) {
        this.lastIndex = value;
    }

    /**
     * Gets the value of the total property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getTotal() {
        return total;
    }

    /**
     * Sets the value of the total property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setTotal(Integer value) {
        this.total = value;
    }

}
