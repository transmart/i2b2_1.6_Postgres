//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.08.17 at 03:47:43 PM EDT 
//


package edu.harvard.i2b2.pm.datavo.i2b2versionmessage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for request_messageType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="request_messageType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="message_header" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="message_body">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="get_message_version" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "request_messageType", propOrder = {
    "messageHeader",
    "messageBody"
})
public class RequestMessageType {

    @XmlElement(name = "message_header", required = true)
    protected String messageHeader;
    @XmlElement(name = "message_body", required = true)
    protected RequestMessageType.MessageBody messageBody;

    /**
     * Gets the value of the messageHeader property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageHeader() {
        return messageHeader;
    }

    /**
     * Sets the value of the messageHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageHeader(String value) {
        this.messageHeader = value;
    }

    /**
     * Gets the value of the messageBody property.
     * 
     * @return
     *     possible object is
     *     {@link RequestMessageType.MessageBody }
     *     
     */
    public RequestMessageType.MessageBody getMessageBody() {
        return messageBody;
    }

    /**
     * Sets the value of the messageBody property.
     * 
     * @param value
     *     allowed object is
     *     {@link RequestMessageType.MessageBody }
     *     
     */
    public void setMessageBody(RequestMessageType.MessageBody value) {
        this.messageBody = value;
    }


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
     *         &lt;element name="get_message_version" type="{http://www.w3.org/2001/XMLSchema}anyType"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "getMessageVersion"
    })
    public static class MessageBody {

        @XmlElement(name = "get_message_version", required = true)
        protected Object getMessageVersion;

        /**
         * Gets the value of the getMessageVersion property.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getGetMessageVersion() {
            return getMessageVersion;
        }

        /**
         * Sets the value of the getMessageVersion property.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setGetMessageVersion(Object value) {
            this.getMessageVersion = value;
        }

    }

}
