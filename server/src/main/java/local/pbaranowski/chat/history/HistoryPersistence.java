package local.pbaranowski.chat.history;

import local.pbaranowski.chat.Message;

import java.util.Iterator;

public interface HistoryPersistence {
    void save(Message message);

    Iterator<String> retrieve(String user);
}
