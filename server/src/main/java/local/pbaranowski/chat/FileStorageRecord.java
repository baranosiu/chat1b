package local.pbaranowski.chat;

import lombok.Value;

import javax.persistence.Entity;

// TODO: To trzeba będzie przerobić w taki sposób, aby FTPClient nie musiał wiedzieć jaka jest struktura danych w storage
// czyli odpytywanie metodami na podstawie klucza o poszczególne dane a nie poprzez przekazywanie rekordu
@Value
class FileStorageRecord {
    String sender;
    String channel;
    String userFilename; // Nazwa nadana przez użytkownika wysyłającego
    String storageFilename; // Nazwa pod jaką plik jest przechowywany w storage
}
