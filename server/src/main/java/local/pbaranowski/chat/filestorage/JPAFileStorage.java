package local.pbaranowski.chat.filestorage;

import local.pbaranowski.chat.Message;
import local.pbaranowski.chat.commons.Constants;
import local.pbaranowski.chat.persistence.BinaryEntityRepository;
import local.pbaranowski.chat.persistence.FileBinaryData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.synchronizedMap;

@Slf4j
@ApplicationScoped
public class JPAFileStorage implements FileStorage {
    private final Map<String, FileStorageRecord> filesUploaded = synchronizedMap(new HashMap<>());
    private BinaryEntityRepository binaryEntityRepository;

    @PostConstruct
    public void init() {
        this.binaryEntityRepository = new BinaryEntityRepository();
    }

    @Override
    public boolean publish(Message message) throws MaxFilesExceededException {
        String sender = message.getSender();
        String[] payload = message.getPayload().split(" ", 3);
        String channel = payload[0];
        String storageFilename = payload[1];
        String userFilename = payload[2];
        FileStorageRecord fileStorageRecord = new FileStorageRecord(sender, channel, userFilename, storageFilename);
        String key = createUniqueFileKey();
        filesUploaded.put(key, fileStorageRecord);
        return true;
    }

    @SneakyThrows
    @Override
    public void delete(String key) {
        if (key == null) return;
        FileStorageRecord file = filesUploaded.get(key);
        if (file != null) {
                filesUploaded.remove(key);
                binaryEntityRepository.delete(UUID.fromString(file.getStorageFilename()));
        }
    }

    @Override
    public boolean hasFile(String key) {
        return filesUploaded.containsKey(key);
    }

    @Override
    public List<String> getFilesOnChannel(String channel) {
        return filesUploaded.keySet()
                .stream()
                .filter(key -> filesUploaded.get(key).getChannel().equals(channel))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAllFilesOnChannel(String channel) {
        List<String> toDelete = new LinkedList<>();
        for (String fileId : filesUploaded.keySet()) {
            FileStorageRecord fileRecord = filesUploaded.get(fileId);
            if (fileRecord.getChannel().equals(channel)) {
                toDelete.add(fileId);
            }
        }
        toDelete.forEach(this::delete);
    }


    @Override
    public String getSender(String key) {
        return filesUploaded.get(key).getSender();
    }

    @Override
    public String getChannel(String key) {
        return filesUploaded.get(key).getChannel();
    }

    @Override
    public String getOriginalFileName(String key) {
        return filesUploaded.get(key).getUserFilename();
    }

    @Override
    public String getStorageFileName(String key) {
        return filesUploaded.get(key).getStorageFilename();
    }

    @Override
    public void saveBinaryData(FileBinaryData fileBinaryData) {
        binaryEntityRepository.save(fileBinaryData);
    }

    @Override
    public FileBinaryData loadBinaryData(UUID fileid) {
        List<FileBinaryData> result = binaryEntityRepository.find(fileid);
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    private String createUniqueFileKey() throws MaxFilesExceededException {
        for (int i = 1; i <= Constants.MAX_NUMBER_OF_FILES_IN_STORAGE; i++) {
            String key = Integer.toString(i);
            if (!filesUploaded.containsKey(key))
                return key;
        }
        throw new MaxFilesExceededException();
    }

}
