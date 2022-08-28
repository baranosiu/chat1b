package local.pbaranowski.chat;

import javax.ejb.Stateful;
import javax.ejb.Stateless;

// Tymczasowo, dopóki nie ma składowania w formacie JSON
@Stateful
class HistoryLogSerializer implements LogSerializer{
    @Override
    public String fromMessageToString(Message message) {
        return String.format("%s",message.getPayload());
    }
}
