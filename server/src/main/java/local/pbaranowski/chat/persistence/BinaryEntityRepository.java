package local.pbaranowski.chat.persistence;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BinaryEntityRepository extends EntityRepository<FileBinaryData> {
    public BinaryEntityRepository() {
        super(FileBinaryData.class);
    }
}
