package local.pbaranowski.chat.persistence;


import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class RepositoriesFactory {
    private final EntityManagerFactory entityManagerFactory;

    private RepositoriesFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public static RepositoriesFactory create(String persistenceUnitName) {
        return new RepositoriesFactory(Persistence.createEntityManagerFactory(persistenceUnitName));
    }

    public EntityRepository getRepository(Class entityClass) {
        return new EntityRepository(entityManagerFactory.createEntityManager(),entityClass);
    }

    public void close() {
        entityManagerFactory.close();
    }
}
