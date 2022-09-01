package local.pbaranowski.chat.JMS;

import local.pbaranowski.chat.MessageRouter;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

@Slf4j
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "Chat")
})
public class JMSListener implements MessageListener {
    @Inject
    MessageRouter messageRouter;
    @Override
    public void onMessage(Message message) {
        try {
            var payload = message.getBody(JMSMessage.class);
            log.info("JMSListener onMessage: {}",payload);
            if(payload.getToId().equals("@login") || payload.getToId().equals("@server")) {
                messageRouter.receiveJMSMessage(message);
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}
