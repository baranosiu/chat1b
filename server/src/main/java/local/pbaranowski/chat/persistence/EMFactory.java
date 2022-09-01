package local.pbaranowski.chat.persistence;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EMFactory { // Taka nazwa aby nie kolidowało przypadkiem z javax.persistence.EntityManagerFactory
    private static final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("Chat");

    public static EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }
}
