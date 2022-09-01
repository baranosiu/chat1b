package local.pbaranowski.chat.persistence;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class JPABinaryData implements EntityToStore {
    @Id
    UUID id;

    @Lob
    @Column(columnDefinition="BLOB")
    @Getter
    @Setter
    byte[] binaryData;

    public JPABinaryData(String storageFilename, byte[] binaryData) {
        this.id = UUID.fromString(storageFilename);
        this.binaryData = binaryData;
    }

    public JPABinaryData() {
    }

    @Override
    public UUID getId() {
        return this.id;
    }
}
