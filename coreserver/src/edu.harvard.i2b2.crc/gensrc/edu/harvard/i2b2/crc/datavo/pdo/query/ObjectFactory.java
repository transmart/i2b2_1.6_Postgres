//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.2-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.08.16 at 12:49:38 PM EDT 
//


package edu.harvard.i2b2.crc.datavo.pdo.query;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the edu.harvard.i2b2.crc.datavo.pdo.query package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Response_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", "response");
    private final static QName _Panel_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", "panel");
    private final static QName _PatientSetCollId_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", "patient_set_coll_id");
    private final static QName _Request_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", "request");
    private final static QName _FilterlistType_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", "filterlistType");
    private final static QName _Sql_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", "sql");
    private final static QName _ObservationFact_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", "observation_fact");
    private final static QName _ConceptPath_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", "concept_path");
    private final static QName _Pdoheader_QNAME = new QName("http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", "pdoheader");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: edu.harvard.i2b2.crc.datavo.pdo.query
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ModifierPrimaryKeyType }
     * 
     */
    public ModifierPrimaryKeyType createModifierPrimaryKeyType() {
        return new ModifierPrimaryKeyType();
    }

    /**
     * Create an instance of {@link PanelType }
     * 
     */
    public PanelType createPanelType() {
        return new PanelType();
    }

    /**
     * Create an instance of {@link GetPatientByPrimaryKeyRequestType }
     * 
     */
    public GetPatientByPrimaryKeyRequestType createGetPatientByPrimaryKeyRequestType() {
        return new GetPatientByPrimaryKeyRequestType();
    }

    /**
     * Create an instance of {@link GetObserverByPrimaryKeyRequestType }
     * 
     */
    public GetObserverByPrimaryKeyRequestType createGetObserverByPrimaryKeyRequestType() {
        return new GetObserverByPrimaryKeyRequestType();
    }

    /**
     * Create an instance of {@link OutputOptionListType }
     * 
     */
    public OutputOptionListType createOutputOptionListType() {
        return new OutputOptionListType();
    }

    /**
     * Create an instance of {@link PatientPrimaryKeyType }
     * 
     */
    public PatientPrimaryKeyType createPatientPrimaryKeyType() {
        return new PatientPrimaryKeyType();
    }

    /**
     * Create an instance of {@link EventListType }
     * 
     */
    public EventListType createEventListType() {
        return new EventListType();
    }

    /**
     * Create an instance of {@link GetObservationFactByPrimaryKeyRequestType }
     * 
     */
    public GetObservationFactByPrimaryKeyRequestType createGetObservationFactByPrimaryKeyRequestType() {
        return new GetObservationFactByPrimaryKeyRequestType();
    }

    /**
     * Create an instance of {@link FilterListType }
     * 
     */
    public FilterListType createFilterListType() {
        return new FilterListType();
    }

    /**
     * Create an instance of {@link PatientDataResponseType }
     * 
     */
    public PatientDataResponseType createPatientDataResponseType() {
        return new PatientDataResponseType();
    }

    /**
     * Create an instance of {@link ObserverListType }
     * 
     */
    public ObserverListType createObserverListType() {
        return new ObserverListType();
    }

    /**
     * Create an instance of {@link ConceptPrimaryKeyType }
     * 
     */
    public ConceptPrimaryKeyType createConceptPrimaryKeyType() {
        return new ConceptPrimaryKeyType();
    }

    /**
     * Create an instance of {@link ItemType.ConstrainByValue }
     * 
     */
    public ItemType.ConstrainByValue createItemTypeConstrainByValue() {
        return new ItemType.ConstrainByValue();
    }

    /**
     * Create an instance of {@link EventListType.EventId }
     * 
     */
    public EventListType.EventId createEventListTypeEventId() {
        return new EventListType.EventId();
    }

    /**
     * Create an instance of {@link DimensionOutputOptionType }
     * 
     */
    public DimensionOutputOptionType createDimensionOutputOptionType() {
        return new DimensionOutputOptionType();
    }

    /**
     * Create an instance of {@link EidListType }
     * 
     */
    public EidListType createEidListType() {
        return new EidListType();
    }

    /**
     * Create an instance of {@link GetPDOFromInputListRequestType }
     * 
     */
    public GetPDOFromInputListRequestType createGetPDOFromInputListRequestType() {
        return new GetPDOFromInputListRequestType();
    }

    /**
     * Create an instance of {@link EventPrimaryKeyType }
     * 
     */
    public EventPrimaryKeyType createEventPrimaryKeyType() {
        return new EventPrimaryKeyType();
    }

    /**
     * Create an instance of {@link OutputOptionType }
     * 
     */
    public OutputOptionType createOutputOptionType() {
        return new OutputOptionType();
    }

    /**
     * Create an instance of {@link PidListType.Pid }
     * 
     */
    public PidListType.Pid createPidListTypePid() {
        return new PidListType.Pid();
    }

    /**
     * Create an instance of {@link ConstrainDateType }
     * 
     */
    public ConstrainDateType createConstrainDateType() {
        return new ConstrainDateType();
    }

    /**
     * Create an instance of {@link MetadataxmlValueType }
     * 
     */
    public MetadataxmlValueType createMetadataxmlValueType() {
        return new MetadataxmlValueType();
    }

    /**
     * Create an instance of {@link GetConceptByPrimaryKeyRequestType }
     * 
     */
    public GetConceptByPrimaryKeyRequestType createGetConceptByPrimaryKeyRequestType() {
        return new GetConceptByPrimaryKeyRequestType();
    }

    /**
     * Create an instance of {@link GetModifierByPrimaryKeyRequestType }
     * 
     */
    public GetModifierByPrimaryKeyRequestType createGetModifierByPrimaryKeyRequestType() {
        return new GetModifierByPrimaryKeyRequestType();
    }

    /**
     * Create an instance of {@link PanelType.TotalItemOccurrences }
     * 
     */
    public PanelType.TotalItemOccurrences createPanelTypeTotalItemOccurrences() {
        return new PanelType.TotalItemOccurrences();
    }

    /**
     * Create an instance of {@link ItemType.ConstrainByModifier.ConstrainByValue }
     * 
     */
    public ItemType.ConstrainByModifier.ConstrainByValue createItemTypeConstrainByModifierConstrainByValue() {
        return new ItemType.ConstrainByModifier.ConstrainByValue();
    }

    /**
     * Create an instance of {@link PageRangeType }
     * 
     */
    public PageRangeType createPageRangeType() {
        return new PageRangeType();
    }

    /**
     * Create an instance of {@link EidListType.Eid }
     * 
     */
    public EidListType.Eid createEidListTypeEid() {
        return new EidListType.Eid();
    }

    /**
     * Create an instance of {@link ItemType.ConstrainByModifier }
     * 
     */
    public ItemType.ConstrainByModifier createItemTypeConstrainByModifier() {
        return new ItemType.ConstrainByModifier();
    }

    /**
     * Create an instance of {@link PageType }
     * 
     */
    public PageType createPageType() {
        return new PageType();
    }

    /**
     * Create an instance of {@link GetPDOTemplateRequestType }
     * 
     */
    public GetPDOTemplateRequestType createGetPDOTemplateRequestType() {
        return new GetPDOTemplateRequestType();
    }

    /**
     * Create an instance of {@link FactPrimaryKeyType }
     * 
     */
    public FactPrimaryKeyType createFactPrimaryKeyType() {
        return new FactPrimaryKeyType();
    }

    /**
     * Create an instance of {@link HeaderType }
     * 
     */
    public HeaderType createHeaderType() {
        return new HeaderType();
    }

    /**
     * Create an instance of {@link PatientListType.PatientId }
     * 
     */
    public PatientListType.PatientId createPatientListTypePatientId() {
        return new PatientListType.PatientId();
    }

    /**
     * Create an instance of {@link Examples.MessageBody }
     * 
     */
    public Examples.MessageBody createExamplesMessageBody() {
        return new Examples.MessageBody();
    }

    /**
     * Create an instance of {@link PidListType }
     * 
     */
    public PidListType createPidListType() {
        return new PidListType();
    }

    /**
     * Create an instance of {@link Examples }
     * 
     */
    public Examples createExamples() {
        return new Examples();
    }

    /**
     * Create an instance of {@link StatusType.Condition }
     * 
     */
    public StatusType.Condition createStatusTypeCondition() {
        return new StatusType.Condition();
    }

    /**
     * Create an instance of {@link ObserverPrimaryKeyType }
     * 
     */
    public ObserverPrimaryKeyType createObserverPrimaryKeyType() {
        return new ObserverPrimaryKeyType();
    }

    /**
     * Create an instance of {@link RangeType }
     * 
     */
    public RangeType createRangeType() {
        return new RangeType();
    }

    /**
     * Create an instance of {@link InputOptionListType }
     * 
     */
    public InputOptionListType createInputOptionListType() {
        return new InputOptionListType();
    }

    /**
     * Create an instance of {@link FactOutputOptionType }
     * 
     */
    public FactOutputOptionType createFactOutputOptionType() {
        return new FactOutputOptionType();
    }

    /**
     * Create an instance of {@link GetEventByPrimaryKeyRequestType }
     * 
     */
    public GetEventByPrimaryKeyRequestType createGetEventByPrimaryKeyRequestType() {
        return new GetEventByPrimaryKeyRequestType();
    }

    /**
     * Create an instance of {@link UserType }
     * 
     */
    public UserType createUserType() {
        return new UserType();
    }

    /**
     * Create an instance of {@link PageByPatientType }
     * 
     */
    public PageByPatientType createPageByPatientType() {
        return new PageByPatientType();
    }

    /**
     * Create an instance of {@link ObserverListType.ObserverPath }
     * 
     */
    public ObserverListType.ObserverPath createObserverListTypeObserverPath() {
        return new ObserverListType.ObserverPath();
    }

    /**
     * Create an instance of {@link PdoQryHeaderType }
     * 
     */
    public PdoQryHeaderType createPdoQryHeaderType() {
        return new PdoQryHeaderType();
    }

    /**
     * Create an instance of {@link StatusType }
     * 
     */
    public StatusType createStatusType() {
        return new StatusType();
    }

    /**
     * Create an instance of {@link ItemType.ConstrainByDate }
     * 
     */
    public ItemType.ConstrainByDate createItemTypeConstrainByDate() {
        return new ItemType.ConstrainByDate();
    }

    /**
     * Create an instance of {@link ItemType }
     * 
     */
    public ItemType createItemType() {
        return new ItemType();
    }

    /**
     * Create an instance of {@link PatientListType }
     * 
     */
    public PatientListType createPatientListType() {
        return new PatientListType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResponseType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", name = "response")
    public JAXBElement<ResponseType> createResponse(ResponseType value) {
        return new JAXBElement<ResponseType>(_Response_QNAME, ResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PanelType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", name = "panel")
    public JAXBElement<PanelType> createPanel(PanelType value) {
        return new JAXBElement<PanelType>(_Panel_QNAME, PanelType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", name = "patient_set_coll_id")
    public JAXBElement<String> createPatientSetCollId(String value) {
        return new JAXBElement<String>(_PatientSetCollId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", name = "request")
    public JAXBElement<RequestType> createRequest(RequestType value) {
        return new JAXBElement<RequestType>(_Request_QNAME, RequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FilterListType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", name = "filterlistType")
    public JAXBElement<FilterListType> createFilterlistType(FilterListType value) {
        return new JAXBElement<FilterListType>(_FilterlistType_QNAME, FilterListType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", name = "sql")
    public JAXBElement<String> createSql(String value) {
        return new JAXBElement<String>(_Sql_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Object }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", name = "observation_fact")
    public JAXBElement<Object> createObservationFact(Object value) {
        return new JAXBElement<Object>(_ObservationFact_QNAME, Object.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", name = "concept_path")
    public JAXBElement<String> createConceptPath(String value) {
        return new JAXBElement<String>(_ConceptPath_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PdoQryHeaderType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.i2b2.org/xsd/cell/crc/pdo/1.1/", name = "pdoheader")
    public JAXBElement<PdoQryHeaderType> createPdoheader(PdoQryHeaderType value) {
        return new JAXBElement<PdoQryHeaderType>(_Pdoheader_QNAME, PdoQryHeaderType.class, null, value);
    }

}
