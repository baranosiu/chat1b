package local.pbaranowski.chat;

import local.pbaranowski.chat.persistence.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.Iterator;

@Slf4j
@ApplicationScoped
public class HistoryJPAPersistence implements HistoryPersistence {
    private final LogSerializer logSerializer = new HistoryLogSerializer();
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
        historyRecord.setHistory(logSerializer.fromMessageToString(message));
        historyRecord.setNickname(message.getSender());
        log.info("History.save: {}", logSerializer.fromMessageToString(message));
        historyEntityRepository.save(historyRecord);
    }

    @SneakyThrows
    @Override
    public Iterator<String> retrieve(String user) {
        return historyEntityRepository.findNickname(user).listIterator();
    }
}
