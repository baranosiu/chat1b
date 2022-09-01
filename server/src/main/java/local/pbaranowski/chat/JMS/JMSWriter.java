package local.pbaranowski.chat.JMS;

import local.pbaranowski.chat.ProxyFactory;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.jms.*;
import javax.naming.NamingException;

@Slf4j
@ApplicationScoped
public class JMSWriter {
    private static final String JMS_FACTORY = "java:/ConnectionFactory";
    private static final String JMS_TOPIC = "java:jboss/exported/jms/Topic/Chat";
    private static ConnectionFactory connectionFactory;
    private static Topic topic;

    @PostConstruct
    public void start() {
        try {
            var proxyFactory = new ProxyFactory();
            connectionFactory = proxyFactory.createProxy(JMS_FACTORY);
            topic = proxyFactory.createProxy(JMS_TOPIC);
        } catch (NamingException e) {
            log.error("JMSClient @PostConstruct Exception {}", e.getMessage(), e);
        }
    }

    public void write(JMSMessage jmsMessage) {
        log.info("JMSChatClient write {}", jmsMessage);
        try (JMSContext context = connectionFactory.createContext()) {
            context.createProducer().send(topic, jmsMessage);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}

