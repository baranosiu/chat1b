package local.pbaranowski.chat;

import javax.ejb.Stateful;

// W chwili obecnej pusta "przelotka" (klasa klienta decyduje w jakim formacie chce mieć odłożony wpis w historii)
@Stateful
class HistoryLogSerializer implements LogSerializer {
    @Override
    public String fromMessageToString(Message message) {
        return String.format("%s", message.getPayload());
    }
}
