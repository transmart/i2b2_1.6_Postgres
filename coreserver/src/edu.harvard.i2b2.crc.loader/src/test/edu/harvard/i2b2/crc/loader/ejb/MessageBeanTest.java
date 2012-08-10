package edu.harvard.i2b2.crc.loader.ejb;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.harvard.i2b2.common.util.ServiceLocator;


public class MessageBeanTest {
	
	    @Resource(mappedName = "jms/ConnectionFactory")
	    private static ConnectionFactory connectionFactory;
	    String topicName = "jms/Topic";
	    @Resource(mappedName= "jms/edu.harvard.i2b2.crc.prototype.squeue")
	    private static Queue queue;
	    
	    private static final String QUEUE_CONNECTION_FACTORY = "ConnectionFactory";
	   private static final String LONG_PROCESS_QUEUE = "jms/edu.harvard.i2b2.crc.prototype.squeue";
	    //private static final String LONG_PROCESS_QUEUE = "queue/jms.querytool.QueryExecutor";
	
	    public Hashtable getEnv() { 
			Hashtable env = new Hashtable();
			env.put(Context.INITIAL_CONTEXT_FACTORY,"org.jnp.interfaces.NamingContextFactory");
			env.put(Context.PROVIDER_URL, "localhost:1099");
			env.put(Context.URL_PKG_PREFIXES,"org.jboss.naming:org.jnp.interfaces");
			return env;
		}
	
	public void testSend() throws Exception { 
		InitialContext context = new InitialContext(getEnv());
		ConnectionFactory connectionFactory = (ConnectionFactory)context.lookup(QUEUE_CONNECTION_FACTORY);
		queue = (Queue)context.lookup(LONG_PROCESS_QUEUE);
		
		Connection conn = connectionFactory.createConnection();
		Session ses = conn.createSession(false,
				javax.jms.Session.AUTO_ACKNOWLEDGE);
		
		
		Destination des = (Destination)queue; 
		MessageProducer producer = ses.createProducer(des);
		TextMessage message = ses.createTextMessage();
		
		message.setText(getQueryString("testfiles/publish_data.xml"));
		producer.send(message);
		
	}
	
	
	public static String getQueryString(String inputFileName) throws Exception  { 
		StringBuffer queryStr = new StringBuffer();
		InputStreamReader dataStream = new InputStreamReader(new FileInputStream(inputFileName));
		BufferedReader reader = new BufferedReader(dataStream);
		String singleLine = null;
		while((singleLine = reader.readLine())!=null) {
			queryStr.append(singleLine + "\n");
		}
		System.out.println("queryStr" + queryStr);
		return queryStr.toString();	
	}
	
	
	public static void main(String[] args) throws Exception { 
		MessageBeanTest bt = new MessageBeanTest();
		bt.testSend();
	}
}
