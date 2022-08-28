package local.pbaranowski.chat.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@ToString
@NoArgsConstructor
public class HistoryRecord implements EntityToStore {
    @Id
    @GeneratedValue
    Long id;

    LocalDateTime dateTime = LocalDateTime.now();

    @Getter
    @Setter
    String history;

    @Getter
    @Setter
    String nickname;

    @Override
    public Long getId() {
        return id;
    }
}
