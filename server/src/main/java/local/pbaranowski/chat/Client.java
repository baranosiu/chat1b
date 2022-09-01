package local.pbaranowski.chat;

public interface Client {
    String getName();

    void write(Message message);

    void setMessageRouter(MessageRouter messageRouter);

    // Używane na potrzeby kanałów czy mają jakichś użytkowników
    // Dla zwykłych użytkowników zwraca zawsze false aby "garbageCollector" clientów nie usuwał zwykłych użytkowników
    default boolean isEmpty() {
        return false;
    }
}
