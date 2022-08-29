package local.pbaranowski.chat;

import local.pbaranowski.chat.commons.Constants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.synchronizedMap;

//@Slf4j
class DiskFileStorage implements FileStorage {
    private final Map<String, FileStorageRecord> filesUploaded = synchronizedMap(new HashMap<>());
    private final Object synchronizationObject = new Object();


    @Override
    public boolean publish(Message message) throws MaxFilesExceededException {
        String sender = message.getSender();
        String[] payload = message.getPayload().split(" ",3);
        String channel = payload[0];
        String diskFilename = payload[1];
        String filename = payload[2];
        FileStorageRecord fileStorageRecord = new FileStorageRecord(sender,channel,filename,diskFilename);
        String key = createUniqueFileKey();
        filesUploaded.put(key,fileStorageRecord);
//        if(!filesInProgress.containsKey(key))
//            return false;
//        synchronized (synchronizationObject) {
//            try {
//                String uploadedKey = createUniqueFileKey();
//                filesUploaded.put(uploadedKey, filesInProgress.get(key));
//            } catch (MaxFilesExceededException excededException) {
//                new File(filesInProgress.get(key).getDiskFilename()).delete();
//                throw excededException;
//            } finally {
//                filesInProgress.remove(key);
//            }
//        }
        return true;
    }

    @SneakyThrows
    @Override
    public void delete(String key) {
        if (key == null) return;
        FileStorageRecord file = filesUploaded.get(key);
        if (file != null) {
            synchronized (synchronizationObject) {
                filesUploaded.remove(key);
            }
            Files.delete(Paths.get(Constants.FILE_STORAGE_DIR + File.separator + file.getDiskFilename()));
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
        return filesUploaded.get(key).getFilename();
    }

    @Override
    public String getStorageFileName(String key) {
        return filesUploaded.get(key).getDiskFilename();
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
//        return new FileInputStream(Constants.FILE_STORAGE_DIR + File.separator + filesUploaded.get(key).getDiskFilename());
    }


    private String createUniqueFileKey() throws MaxFilesExceededException {
        for (int i = 1; i <= Constants.MAX_NUMBER_OF_FILES_IN_STORAGE; i++) {
            String key = Integer.toString(i);
            if (!filesUploaded.containsKey(key))
                return key;
        }
        throw new MaxFilesExceededException();
    }

    private String createTmpFileKey() throws MaxFilesExceededException {
        for (int i = 1; i <= Constants.MAX_NUMBER_OF_FILES_IN_STORAGE; i++) {
            String key = Integer.toString(i);
            if (!filesUploaded.containsKey(key))
                return key;
        }
        throw new MaxFilesExceededException();
    }

}

