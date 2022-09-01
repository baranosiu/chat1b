package local.pbaranowski.chat;

import lombok.Value;

// TODO: Przerobić w taki sposób, aby FTPClient nie musiał wiedzieć jaka jest struktura danych w storage
@Value
class FileStorageRecord {
    String sender;
    String channel;
    String userFilename; // Nazwa nadana przez użytkownika wysyłającego
    String storageFilename; // Nazwa pod jaką plik jest przechowywany w storage
}
