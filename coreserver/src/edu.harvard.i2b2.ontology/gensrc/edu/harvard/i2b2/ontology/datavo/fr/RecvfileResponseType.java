//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.08.17 at 03:50:30 PM EDT 
//


package edu.harvard.i2b2.ontology.datavo.fr;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for recvfile_responseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="recvfile_responseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="recvfile_response" type="{http://www.i2b2.org/xsd/cell/fr/1.0/}file"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "recvfile_responseType", propOrder = {
    "recvfileResponse"
})
public class RecvfileResponseType {

    @XmlElement(name = "recvfile_response", required = true)
    protected File recvfileResponse;

    /**
     * Gets the value of the recvfileResponse property.
     * 
     * @return
     *     possible object is
     *     {@link File }
     *     
     */
    public File getRecvfileResponse() {
        return recvfileResponse;
    }

    /**
     * Sets the value of the recvfileResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link File }
     *     
     */
    public void setRecvfileResponse(File value) {
        this.recvfileResponse = value;
    }

}
