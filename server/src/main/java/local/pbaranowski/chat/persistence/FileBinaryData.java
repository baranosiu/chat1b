package local.pbaranowski.chat.persistence;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class FileBinaryData implements EntityToStore {
    @Id
    UUID id;

//    @Column(columnDefinition="BLOB")
    @Getter
    @Setter
    byte[] binaryData;

    public FileBinaryData(String storageFilename, byte[] binaryData) {
        this.id = UUID.fromString(storageFilename);
        this.binaryData = binaryData;
    }

    @Override
    public UUID getId() {
        return this.id;
    }
}
