package local.pbaranowski.chat.history;

import local.pbaranowski.chat.Client;
import local.pbaranowski.chat.Message;
import local.pbaranowski.chat.MessageRouter;
import local.pbaranowski.chat.commons.MessageType;
import local.pbaranowski.chat.history.HistoryPersistence;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Iterator;

import static local.pbaranowski.chat.commons.Constants.HISTORY_ENDPOINT_NAME;

@Slf4j
@ApplicationScoped
public class HistoryClient implements Client {
    @Setter
    private MessageRouter messageRouter;
    @Inject
    private HistoryPersistence historyPersistence;

    @Override
    public String getName() {
        return HISTORY_ENDPOINT_NAME;
    }

    @Override
    public void write(Message message) {
        log.info("HistoryClient write() {}", message);
        switch (message.getMessageType()) {
            case MESSAGE_HISTORY_STORE:
                save(message);
                break;
            case MESSAGE_HISTORY_RETRIEVE:
                retrieveHistory(message);
                break;
            default:
                break;
        }
    }

    public void save(Message message) {
        log.info("HistoryClient.save {}",message);
        historyPersistence.save(message);
    }

    public Iterator<String> retrieve(String user) {
        return historyPersistence.retrieve(user);
    }

    private void retrieveHistory(Message message) {
        Iterator<String> history = retrieve(message.getSender());
        while (history.hasNext()) {
            String historyRecord = history.next();
            messageRouter.sendMessage(new Message(MessageType.MESSAGE_TEXT, getName(), message.getSender(), historyRecord));
        }
    }
}
