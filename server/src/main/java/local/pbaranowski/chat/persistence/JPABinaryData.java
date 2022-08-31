package local.pbaranowski.chat.persistence;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.UUID;

@Entity
@ToString
public class JPABinaryData implements EntityToStore {
    @Id
    @Getter
    @Setter
    UUID id;

//    @Lob
//    @Column(columnDefinition="BLOB")
    @Getter
    @Setter
    String binaryData;

    public JPABinaryData(String storageFilename, String binaryData) {
        this.id = UUID.fromString(storageFilename);
        this.binaryData = binaryData;
    }

    public JPABinaryData() {

    }

}
