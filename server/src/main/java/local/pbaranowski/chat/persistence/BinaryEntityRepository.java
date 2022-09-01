package local.pbaranowski.chat.persistence;

import lombok.extern.slf4j.Slf4j;

import javax.persistence.EntityManagerFactory;

@Slf4j
public class BinaryEntityRepository extends EntityRepository<FileBinaryData> {
    public BinaryEntityRepository(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory, FileBinaryData.class);
    }
}
