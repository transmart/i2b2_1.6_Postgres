//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.08.17 at 03:42:02 PM EDT 
//


package edu.harvard.i2b2.crc.datavo.setfinder.query;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for queryModeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="queryModeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="optimize_without_temp_table"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "queryModeType")
@XmlEnum
public enum QueryModeType {

    @XmlEnumValue("optimize_without_temp_table")
    OPTIMIZE_WITHOUT_TEMP_TABLE("optimize_without_temp_table");
    private final String value;

    QueryModeType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static QueryModeType fromValue(String v) {
        for (QueryModeType c: QueryModeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
