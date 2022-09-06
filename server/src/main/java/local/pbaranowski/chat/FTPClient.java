package local.pbaranowski.chat;

import local.pbaranowski.chat.commons.MessageType;
import local.pbaranowski.chat.filestorage.FileStorage;
import local.pbaranowski.chat.filestorage.FileStorageUtils;
import local.pbaranowski.chat.filestorage.MaxFilesExceededException;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

import static local.pbaranowski.chat.commons.Constants.FTP_ENDPOINT_NAME;


@Slf4j
@ApplicationScoped
public class FTPClient implements Client {
    @Setter
    private MessageRouter messageRouter;
    @Inject
    private FileStorage fileStorage;

    @Override
    public String getName() {
        return FTP_ENDPOINT_NAME;
    }

    @Override
    public void write(Message message) {
        switch (message.getMessageType()) {
            case MESSAGE_PUBLISH_FILE:
                publish(message);
                break;
            case MESSAGE_DOWNLOAD_FILE:
                getFile(message);
                break;
            case MESSAGE_DELETE_FILE:
                delete(message);
                break;
            case MESSAGE_DELETE_ALL_FILES_ON_CHANNEL:
                fileStorage.deleteAllFilesOnChannel(message.getReceiver());
                break;
            case MESSAGE_LIST_FILES:
                listFiles(message);
                break;
            default:
                break;
        }
    }

    private void delete(Message message) {
        if (!fileStorage.hasFile(message.getReceiver())) {
            messageRouter.sendMessage(MessageType.MESSAGE_TEXT, FTP_ENDPOINT_NAME, message.getSender(), "ERROR: No file (id = " + message.getReceiver() + ")");
            return;
        }
        if (message.getSender().equals(fileStorage.getSender(message.getReceiver()))) {
            fileStorage.delete(message.getReceiver());
        } else {
            messageRouter.sendMessage(MessageType.MESSAGE_TEXT, FTP_ENDPOINT_NAME, message.getSender(), "ERROR: Not owner (id = " + message.getReceiver() + ")");
        }
    }

    private void publish(Message message) {
        try {
            if (fileStorage.publish(message)) {
                messageRouter.sendMessage(MessageType.MESSAGE_TEXT, message.getReceiver(), message.getSender(), "Upload done");
            } else {
                messageRouter.sendMessage(MessageType.MESSAGE_TEXT, message.getReceiver(), message.getSender(), "ERROR: Upload failed");
            }
        } catch (MaxFilesExceededException e) {
            messageRouter.sendMessage(MessageType.MESSAGE_TEXT, message.getReceiver(), message.getSender(), "ERROR: " + e.getClass().getSimpleName());
        }
    }

    @SneakyThrows
    private void getFile(Message message) {
        if (!fileStorage.hasFile(message.getReceiver())) {
            messageRouter.sendMessage(MessageType.MESSAGE_TEXT, FTP_ENDPOINT_NAME, message.getSender(), "ERROR: No file (id = " + message.getReceiver() + ")");
            return;
        }
        ChannelClient destinationChannel = messageRouter.getChannelClient(fileStorage.getChannel(message.getReceiver()));
        if (destinationChannel == null || !destinationChannel.hasClient(message.getSender())) {
            messageRouter.sendMessage(MessageType.MESSAGE_TEXT, message.getReceiver(), message.getSender(), "ERROR: Not allowed");
            return;
        }

        if (!fileStorage.hasFile(message.getReceiver())) {
            messageRouter.sendMessage(MessageType.MESSAGE_TEXT, FTP_ENDPOINT_NAME, message.getSender(), "ERROR: No file with id = " + message.getReceiver());
        } else {
            String storageFilename = fileStorage.getStorageFileName(message.getReceiver());
            String userFilename = message.getPayload();
            messageRouter.sendMessage(MessageType.MESSAGE_SEND_CHUNK_TO_CLIENT, getName(), message.getSender(), storageFilename + " " + userFilename);
        }
    }

    private void listFiles(Message message) {
        List<String> files = fileStorage.getFilesOnChannel(message.getReceiver());
        files.stream()
                .forEach(fileKey -> messageRouter.sendMessage(MessageType.MESSAGE_TEXT,
                        message.getReceiver(),
                        message.getSender(),
                        FileStorageUtils.fileRecordToString(fileKey, fileStorage.getSender(fileKey), fileStorage.getOriginalFileName(fileKey)))
                );
    }

}
