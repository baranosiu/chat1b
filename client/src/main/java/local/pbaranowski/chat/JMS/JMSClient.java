package local.pbaranowski.chat.JMS;

import local.pbaranowski.chat.commons.ProxyFactory;

import javax.jms.*;
import javax.naming.NamingException;

public class JMSClient {
    private static final String JMS_FACTORY = "jms/RemoteConnectionFactory";
    private static final String JMS_TOPIC = "jms/Topic/Chat";
    private final JMSConsumer jmsConsumer;
    private final JMSProducer jmsProducer;
    private final Topic topic;

    public JMSClient(String endpoint) throws NamingException {
        var proxyFactory = new ProxyFactory(endpoint);
        ConnectionFactory connectionFactory = proxyFactory.createProxy(JMS_FACTORY);
        topic = proxyFactory.createProxy(JMS_TOPIC);
        var jmsContext = connectionFactory.createContext();
        jmsConsumer = jmsContext.createConsumer(topic);
        jmsProducer = jmsContext.createProducer();
//        jmsProducer.send(topic, new ChatMessage("body", "from", "to"));
    }

    public void setJMSListener(MessageListener messageListener) {
        jmsConsumer.setMessageListener(messageListener);
    }

    public void write(JMSMessage jmsMessage) {
        synchronized (jmsProducer) {
            jmsProducer.send(topic, jmsMessage);
        }
    }

}

