package local.pbaranowski.chat.JMS;

import lombok.Value;

import java.io.Serializable;

@Value
public class JMSMessage implements Serializable {
    String body;
    String fromId;
    String toId;
}
