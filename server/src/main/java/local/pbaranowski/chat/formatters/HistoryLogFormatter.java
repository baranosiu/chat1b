package local.pbaranowski.chat.formatters;

import local.pbaranowski.chat.Message;
import local.pbaranowski.chat.formatters.LogFormatter;

import javax.ejb.Stateful;

// W chwili obecnej pusta "przelotka" (klasa klienta decyduje w jakim formacie chce mieć odłożony wpis w historii)
@Stateful
public class HistoryLogFormatter implements LogFormatter {
    @Override
    public String fromMessageToString(Message message) {
        return String.format("%s", message.getPayload());
    }
}
