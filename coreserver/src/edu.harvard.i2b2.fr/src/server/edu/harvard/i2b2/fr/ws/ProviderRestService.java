package edu.harvard.i2b2.fr.ws;


import java.io.StringReader;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceProvider;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.fr.delegate.RecvfileRequestHandler;
import edu.harvard.i2b2.fr.delegate.LoaderQueryRequestDelegate;
import edu.harvard.i2b2.fr.delegate.SendfileRequestHandler;

/**
 * Test a Provider<Source>
 *
 * @author rkuttan
 * 
 */


@WebServiceProvider(serviceName = "ProviderService", portName = "ProviderPort", targetNamespace = "http://org.jboss.ws/provider", wsdlLocation = "WEB-INF/wsdl/Provider.wsdl")
//@BindingType(value=HTTPBinding.HTTP_BINDING)
@ServiceMode(value = Service.Mode.PAYLOAD)// - PAYLOAD is implicit
public class ProviderRestService 
{
    /** log **/
    protected final Log log = LogFactory.getLog(getClass());
    
	public OMElement sendfileRequest(OMElement request) {
		LoaderQueryRequestDelegate queryDelegate = new LoaderQueryRequestDelegate();
		OMElement responseElement = null;
		try { 
			String requestXml = request.toString();
			SendfileRequestHandler handler = new SendfileRequestHandler(requestXml);
			String response = queryDelegate.handleRequest(requestXml,handler);
			responseElement = buildOMElementFromString(response, "");
			 
		 }    catch (XMLStreamException e) {
	            log.error("xml stream exception",e);
		 } catch (I2B2Exception e) {
				log.error("i2b2 exception",e);
			} catch (Throwable e) { 
				log.error("Throwable",e);
			}
		return responseElement;
	}
	
	public OMElement recvfileRequest(OMElement request) {
		LoaderQueryRequestDelegate queryDelegate = new LoaderQueryRequestDelegate();
		OMElement responseElement = null;
		
		FileDataSource graphImageDataSource;
		DataHandler graphImageDataHandler;

		try { 
			log.debug("In request :" + request.toString());
			String requestXml = request.toString();
			RecvfileRequestHandler handler = new RecvfileRequestHandler(requestXml);
			String response = queryDelegate.handleRequest(requestXml,handler);
			log.debug("In response :" + response);

			String filename = handler.getFilename();
			// We can obtain the request (incoming) MessageContext as follows
			MessageContext inMessageContext = MessageContext.getCurrentMessageContext();
			// We can obtain the operation context from the request message context
			OperationContext operationContext = inMessageContext.getOperationContext();
			MessageContext outMessageContext = operationContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
			if (!filename.equals(""))
			{
				log.debug("In filename from recvfile:" + filename);
				
				graphImageDataSource = new FileDataSource(filename); //sampleResourcePath +File.separator+ "2.png");
				graphImageDataHandler = new DataHandler(graphImageDataSource);
				outMessageContext.addAttachment("cid", graphImageDataHandler);
				
				responseElement = buildOMElementFromString(response, "cid");

				outMessageContext.setDoingMTOM(false);
				outMessageContext.setDoingSwA(true);
			
			}
			 

		 }    catch (XMLStreamException e) {
	            log.error("xml stream exception",e);
		 } catch (I2B2Exception e) {
				log.error("i2b2 exception",e);
			} catch (Throwable e) { 
				log.error("Throwable",e);
			}
		return responseElement;
	}
	
	/**
	 * Function constructs OMElement for the given String 
	 * @param xmlString
	 * @return OMElement
	 * @throws XMLStreamException
	 */
    private  OMElement buildOMElementFromString(String xmlString, String graphCID) throws XMLStreamException {
    	
    	XMLInputFactory xif = XMLInputFactory.newInstance();
        StringReader strReader = new StringReader(xmlString);
        XMLStreamReader reader = xif.createXMLStreamReader(strReader);
        StAXOMBuilder builder = new StAXOMBuilder(reader);
        OMElement element = builder.getDocumentElement();

        

		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace omNs = factory.createOMNamespace("http://www.i2b2.org/xsd", "swa");
		OMElement wrapperElement = factory.createOMElement("recvfileResponse", omNs, element);

		OMElement graphElement = factory.createOMElement("graph", omNs, element);
        graphCID = "cid:"+graphCID;
		graphElement.addAttribute("href", graphCID,null);
		
		log.debug("In buildOMElementFromString :" + element.toString());

        return element;
    }
   

}