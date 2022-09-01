package local.pbaranowski.chat.filestorage;

import local.pbaranowski.chat.Message;
import local.pbaranowski.chat.persistence.FileBinaryData;

import java.util.List;
import java.util.UUID;

public interface FileStorage {
    boolean publish(Message message) throws MaxFilesExceededException;
    void saveBinaryData(FileBinaryData fileBinaryData) ;
    FileBinaryData loadBinaryData(UUID fileid) ;
    void delete(String key);
    boolean hasFile (String key);
    List<String> getFilesOnChannel(String channel);
    void deleteAllFilesOnChannel(String channel);
    String getSender(String key);
    String getChannel(String key);
    String getOriginalFileName(String key);
    String getStorageFileName(String key);
}
