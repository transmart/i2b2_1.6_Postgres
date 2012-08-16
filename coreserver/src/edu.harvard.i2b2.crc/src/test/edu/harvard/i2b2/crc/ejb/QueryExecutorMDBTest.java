package edu.harvard.i2b2.crc.ejb;

import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class QueryExecutorMDBTest {
	public static String QUEUE_CONN_FACTORY_NAME = "ConnectionFactory";

	public static String UPLOADPROCESSOR_QUEUE_NAME = "queue/jms.querytool.QueryExecutor";

	
	public void doSubmit() throws Exception {
		QueueConnection conn = null;
		QueueSession session = null;
		QueueSender send = null;
		try {

			QueueConnectionFactory qcf = getQueueConnectionFactory();
			Queue que = getQueue();

			conn = qcf.createQueueConnection();
			session = conn.createQueueSession(false,
					QueueSession.AUTO_ACKNOWLEDGE);
			conn.start();

			send = session.createSender(que);

			MapMessage message = session.createMapMessage();
			//set operation name
			message.setString("Hello","Hello");
			send.send(message);

			
		} catch (Exception slEx) {
			slEx.printStackTrace();
			 
		} finally {
			closeAll(send, conn, session);
		}
	}

	private Queue getQueue() throws Exception {
		Queue que = getQueue(UPLOADPROCESSOR_QUEUE_NAME);
		return que;
	}

	private QueueConnectionFactory getQueueConnectionFactory()
			throws Exception {
		
		QueueConnectionFactory qcf = getQueueConnectionFactory(QUEUE_CONN_FACTORY_NAME);
		return qcf;
	}

	public Hashtable getEnv() { 
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY,"org.jnp.interfaces.NamingContextFactory");
		env.put(Context.PROVIDER_URL, "localhost");
		env.put(Context.URL_PKG_PREFIXES,"org.jboss.naming:org.jnp.interfaces");
		return env;
	}
	/**
     * @return the factory for the factory to get queue connections from
     */
    public  QueueConnectionFactory getQueueConnectionFactory(String qConnFactoryName) 
                                                 throws Exception {
      QueueConnectionFactory factory = null;
      try {
    	  	InitialContext ic = new InitialContext(getEnv());
            factory = (QueueConnectionFactory) ic.lookup(qConnFactoryName);
        
      } catch (Exception ne) {
        throw ne;
      }
      return factory;
    }


    /**
     * @return the Queue Destination to send messages to
     */
    public  Queue getQueue(String queueName) throws Exception {
      Queue queue = null;
      try {
    	  InitialContext ic = new InitialContext(getEnv());
            queue =(Queue)ic.lookup(queueName);
      } catch (Exception ne) {
    	  throw ne;
      }

      return queue;
    }
    
	private void closeAll(QueueSender send, QueueConnection conn,
			QueueSession session) {
		try {
			if (send != null) {
				send.close();
			}

			if (conn != null) {
				conn.stop();
			}

			if (session != null) {
				session.close();
			}
		} catch (JMSException jmse) {
			jmse.printStackTrace();
		}

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		QueryExecutorMDBTest test = new QueryExecutorMDBTest();
		try { 
			test.doSubmit();
		} catch (Exception e) { 
			e.printStackTrace();
		}

	}

}
