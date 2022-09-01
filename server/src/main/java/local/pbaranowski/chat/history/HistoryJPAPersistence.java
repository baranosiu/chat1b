package local.pbaranowski.chat.history;

import local.pbaranowski.chat.formatters.HistoryLogFormatter;
import local.pbaranowski.chat.formatters.LogFormatter;
import local.pbaranowski.chat.Message;
import local.pbaranowski.chat.persistence.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.Iterator;

@Slf4j
@ApplicationScoped
public class HistoryJPAPersistence implements HistoryPersistence {
    private final LogFormatter logFormatter = new HistoryLogFormatter();
    private HistoryEntityRepository historyEntityRepository;

    @PostConstruct
    public void init() {
        log.info("############# HistoryJPAPersistance postconstruct");
        this.historyEntityRepository = new HistoryEntityRepository();
    }

    @SneakyThrows
    @Override
    public void save(Message message) {
        HistoryRecord historyRecord = new HistoryRecord();
        historyRecord.setHistory(logFormatter.fromMessageToString(message));
        historyRecord.setNickname(message.getSender());
        log.info("History.save: {}", logFormatter.fromMessageToString(message));
        historyEntityRepository.save(historyRecord);
    }

    @SneakyThrows
    @Override
    public Iterator<String> retrieve(String user) {
        return historyEntityRepository.findNickname(user).listIterator();
    }
}
