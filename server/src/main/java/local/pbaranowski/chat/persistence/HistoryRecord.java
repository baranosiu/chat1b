package local.pbaranowski.chat.persistence;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class HistoryRecord implements EntityToStore{
    @Id
    UUID id;

    LocalDateTime dateTime = LocalDateTime.now();

    @Getter
    @Setter
    String history;

    @Getter
    @Setter
    String nickname;

    public UUID getId() {
        return id;
    }

    public HistoryRecord() {
        this.id = UUID.randomUUID();
    }
}
