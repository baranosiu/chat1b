package local.pbaranowski.chat.persistence;

import lombok.extern.slf4j.Slf4j;
import javax.persistence.*;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

@Slf4j
public class EntityRepository<T extends EntityToStore> {
    protected EntityManager entityManager;
    protected final Class<T> entityClass;

    public EntityRepository(Class<T> entityClass) {
        this.entityManager = EMFactory.getEntityManagerFactory().createEntityManager();
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

    public UUID save(T entity) {
        return doInTransaction(EntityManager::persist, entity);
    }

    public void update(T entity) {
        doInTransaction(EntityManager::merge, entity);
    }

    public void update(UUID id) {
        find(id).forEach(this::update);
    }

    public void delete(T entity) {
        doInTransaction(EntityManager::remove, entity);
    }

    public void delete(UUID id) {
        find(id).forEach(this::delete); // Można też przez createQuery() - zaoszczędzenie SELECT-a
    }

    public UUID doInTransaction(BiConsumer<EntityManager, T> biConsumer, T entity) {
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        biConsumer.accept(entityManager, entity);
        transaction.commit();
        return entity.getId();
    }

    public List<T> find() {
        return find(null);
    }


    public List<T> find(UUID id) {
        String query = "SELECT e FROM " + getTableName() + " e " + (id == null ? "" : "WHERE e.id = '" + id + "'");
        log.info("query: {}", query);
        return (List<T>) entityManager.createQuery(query).getResultList();
    }
}
