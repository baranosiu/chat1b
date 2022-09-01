package local.pbaranowski.chat.persistence;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EMFactory { // Taka nazwa aby nie kolidowało przypadkiem z javax.persistence.EntityManagerFactory
    private static EntityManagerFactory entityManagerFactory;

    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null) {
            entityManagerFactory = Persistence.createEntityManagerFactory("Chat");
        }
        return entityManagerFactory;
    }
}
