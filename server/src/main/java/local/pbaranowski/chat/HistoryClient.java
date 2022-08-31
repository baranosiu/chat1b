package local.pbaranowski.chat;

import local.pbaranowski.chat.commons.MessageType;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;

import static local.pbaranowski.chat.commons.Constants.HISTORY_ENDPOINT_NAME;

@Slf4j
@ApplicationScoped
class HistoryClient implements Client, Runnable {
    @Setter
    private MessageRouter messageRouter;
    @Inject
    private HistoryJPAPersistence historyPersistence;

    @Override
    public String getName() {
        return HISTORY_ENDPOINT_NAME;
    }

    @Override
    public void write(Message message) {
        log.info("HistoryClient write() {}", message);
        switch (message.getMessageType()) {
            case MESSAGE_HISTORY_STORE:
                save(message); // TODO - implementacja JPA
                break;
            case MESSAGE_HISTORY_RETRIEVE:
                retrieveHistory(message); // TODO - implementacja JPA
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        messageRouter.subscribe(this);
    }

    void save(Message message) {
        log.info("HistoryClient.save {}",message);
        historyPersistence.save(message);
    }

    Iterator<String> retrieve(String user) {
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
