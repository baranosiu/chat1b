package local.pbaranowski.chat;

import local.pbaranowski.chat.commons.Constants;
import local.pbaranowski.chat.persistence.BinaryEntityRepository;
import local.pbaranowski.chat.persistence.JPABinaryData;
import local.pbaranowski.chat.persistence.RepoFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.synchronizedMap;

@Slf4j
@Singleton
public class JPAFileStorage implements FileStorage {
    private final Map<String, FileStorageRecord> filesUploaded = synchronizedMap(new HashMap<>());
    @Inject
    private RepoFactory repoFactory;
    private BinaryEntityRepository binaryEntityRepository;

    @PostConstruct
    public void init() {
        log.info("############# JPAFileStorage postconstruct");
        EntityManagerFactory factory = repoFactory.getEntityManagerFactory();
        this.binaryEntityRepository = new BinaryEntityRepository(factory);
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
            Files.delete(Paths.get(Constants.FILE_STORAGE_DIR + File.separator + file.getStorageFilename()));
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

    @SneakyThrows
    public InputStream getFile(String key) {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }

            @Override
            public int available() {
                return 0;
            }
        };
    }

    public void echo(String text) {
        log.info("########## REST: {}", text);
    }

    private String createUniqueFileKey() throws MaxFilesExceededException {
        for (int i = 1; i <= Constants.MAX_NUMBER_OF_FILES_IN_STORAGE; i++) {
            String key = Integer.toString(i);
            if (!filesUploaded.containsKey(key))
                return key;
        }
        throw new MaxFilesExceededException();
    }

    public void storeBinaryData(JPABinaryData jpaBinaryData) {
        binaryEntityRepository.save(jpaBinaryData);
    }
}
