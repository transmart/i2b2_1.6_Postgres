//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.08.17 at 03:41:43 PM EDT 
//


package edu.harvard.i2b2.crc.loader.datavo.loader.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for fact_load_optionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fact_load_optionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.i2b2.org/xsd/cell/crc/loader/1.1/}load_optionType">
 *       &lt;attribute name="append_flag" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fact_load_optionType")
public class FactLoadOptionType
    extends LoadOptionType
{

    @XmlAttribute(name = "append_flag")
    protected Boolean appendFlag;

    /**
     * Gets the value of the appendFlag property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isAppendFlag() {
        if (appendFlag == null) {
            return false;
        } else {
            return appendFlag;
        }
    }

    /**
     * Sets the value of the appendFlag property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAppendFlag(Boolean value) {
        this.appendFlag = value;
    }

}
