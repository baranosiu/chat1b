package local.pbaranowski.chat.persistence;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
public class EntityRepository<T extends EntityToStore> {
    private final EntityManager entityManager;
    private final Class<T> entityClass;

    public EntityRepository(EntityManager entityManager, Class<T> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }

    private <T> String getTableName(Class<T> entityClass) {
        Metamodel meta = entityManager.getMetamodel();
        EntityType<T> entityType = meta.entity(entityClass);
        Table t = entityClass.getAnnotation(Table.class);
        String tableName = (t == null) ? entityType.getName() : t.name();
        return tableName;
    }

    public String getTableName() {
        return getTableName(entityClass);
    }

    public Long save(T entity) {
        return doInTransaction(EntityManager::persist, entity);
    }

    public void update(T entity) {
        doInTransaction(EntityManager::merge, entity);
    }

    public void update(Long id) {
        find(id).forEach(this::update);
    }

    public void delete(T entity) {
        doInTransaction(EntityManager::remove, entity);
    }

    public void delete(Long id) {
        find(id).forEach(this::delete); // Można też przez createQuery() - zaoszczędzenie SELECT-a
    }

    public Long doInTransaction(BiConsumer<EntityManager, T> biConsumer, T entity) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        biConsumer.accept(entityManager, entity);
        transaction.commit();
        return entity.getId();
    }

    public List<T> find() {
        return find(null);
    }

    // TODO Przerobić budowanie zapytania
    public List<T> find(Long id) {
        String query = "SELECT e FROM " + getTableName() + " e " + (id == null ? "" : "WHERE e.id = " + id);
        log.info("query: {}", query);
        return entityManager.createQuery(query).getResultList();
    }

    public List<String> findNickname(String nickname) {
        String query = "SELECT e.history FROM " + getTableName() + " e " + (nickname == null ? "" : "WHERE e.nickname like '" + nickname + "'") + " order by e.id";
        log.info("query: {}", query);
        return entityManager.createQuery(query).getResultList();
    }
}
