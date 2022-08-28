package local.pbaranowski.chat.commons;

import lombok.Value;

import java.io.Serializable;

@Value
public class ChatMessage implements Serializable {
    String body;
    String fromId;
    String toId;
}
