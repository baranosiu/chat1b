package local.pbaranowski.chat;

import local.pbaranowski.chat.commons.ChatMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;
import javax.naming.NamingException;

@Slf4j
@ApplicationScoped
public class JMSChatClient {
    private static final String JMS_FACTORY = "java:/ConnectionFactory";
    private static final String JMS_TOPIC = "java:jboss/exported/jms/Topic/Chat";
    private static ConnectionFactory connectionFactory ;
    private static Topic topic;

    @PostConstruct
    public void start() {
        log.info("######################################### JMSChatClient @PostConstruct");
        try {
            var proxyFactory = new ProxyFactory();
            connectionFactory = proxyFactory.createProxy(JMS_FACTORY);
            topic = proxyFactory.createProxy(JMS_TOPIC);
        } catch (NamingException e) {
//            log.error("JMSClient @PostConstruct Exception {}",e.getMessage(),e);
        }
    }

    public void write(ChatMessage chatMessage) {
//        log.info("JMSChatClient write {}",chatMessage);
        try (JMSContext context = connectionFactory.createContext()) {
            context.createProducer().send(topic,chatMessage);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}

