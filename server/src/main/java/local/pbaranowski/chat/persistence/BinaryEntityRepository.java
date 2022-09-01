package local.pbaranowski.chat.persistence;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManagerFactory;
import java.util.List;

@Slf4j
public class BinaryEntityRepository extends EntityRepository<JPABinaryData> {
    public BinaryEntityRepository(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, JPABinaryData.class);
    }
}
