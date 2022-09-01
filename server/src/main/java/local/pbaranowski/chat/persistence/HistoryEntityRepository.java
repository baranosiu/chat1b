package local.pbaranowski.chat.persistence;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManagerFactory;
import java.util.List;

@Slf4j
public class HistoryEntityRepository extends EntityRepository<HistoryRecord> {
    public HistoryEntityRepository(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, HistoryRecord.class);
    }

    public List<String> findNickname(String user) {
        String query = "SELECT e.history FROM " + getTableName() + " e " + (user == null ? "" : "WHERE e.nickname like '" + user + "' order by e.dateTime");
        log.info("query: {}", query);
        return entityManager.createQuery(query).getResultList();
    }

}
