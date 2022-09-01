package local.pbaranowski.chat;

import java.util.List;

public interface FileStorage {
    boolean publish(Message message) throws MaxFilesExceededException;
    void delete(String key);
    boolean hasFile (String key);
    List<String> getFilesOnChannel(String channel);
    void deleteAllFilesOnChannel(String channel);
    String getSender(String key);
    String getChannel(String key);
    String getOriginalFileName(String key);
    String getStorageFileName(String key);
}
