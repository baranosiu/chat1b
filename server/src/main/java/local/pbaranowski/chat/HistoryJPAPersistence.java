package local.pbaranowski.chat;

import local.pbaranowski.chat.persistence.EntityRepository;
import local.pbaranowski.chat.persistence.HistoryRecord;
import local.pbaranowski.chat.persistence.RepositoriesFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.ManagedBean;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import java.util.Iterator;
import java.util.List;

@Slf4j
@ApplicationScoped
class HistoryJPAPersistence implements HistoryPersistence {
    private LogSerializer logSerializer = new HistoryLogSerializer();
    private RepositoriesFactory FACTORY = RepositoriesFactory.create("Chat");
    private EntityRepository<HistoryRecord> historyRecordEntityRepository = FACTORY.getRepository(HistoryRecord.class);

    @SneakyThrows
    @Override
    public void save(Message message) {
        HistoryRecord historyRecord = new HistoryRecord();
        historyRecord.setHistory(logSerializer.fromMessageToString(message));
        historyRecord.setNickname(message.getSender());
        log.info("History.save: {}", logSerializer.fromMessageToString(message));
        historyRecordEntityRepository.save(historyRecord);
    }

    @SneakyThrows
    @Override
    public Iterator<String> retrieve(String user) {
        // TODO
//        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
//        return new BufferedReaderIterable(bufferedReader).iterator();
        return historyRecordEntityRepository.findNickname(user).listIterator();
    }
}
