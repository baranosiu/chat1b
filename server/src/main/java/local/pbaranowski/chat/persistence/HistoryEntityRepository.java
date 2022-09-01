package local.pbaranowski.chat.persistence;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

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
