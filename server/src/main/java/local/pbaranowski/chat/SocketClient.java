package local.pbaranowski.chat;

import local.pbaranowski.chat.commons.ChatMessage;
import local.pbaranowski.chat.commons.Constants;
import local.pbaranowski.chat.commons.MessageType;
import local.pbaranowski.chat.commons.NameValidators;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static local.pbaranowski.chat.commons.Constants.HELP_FILE;

//@Slf4j
@NoArgsConstructor
class SocketClient implements Runnable, Client {
    //    private BufferedReader bufferedReader;
//    private BufferedWriter bufferedWriter;
    private String name;
    @Setter
    private MessageRouter messageRouter;
    @Setter
    private JMSChatClient jmsChatClient;
    private String lastDestination;
    private static final String MESSAGE_FORMAT_STRING = "%s->%s %s";
    private final Object synchronizationObject = new Object();

    // alias
    public void init() {
        socketClientInit();
    }

    @SneakyThrows
    @Override
    public void write(Message message) {
        if (message.getMessageType() == MessageType.MESSAGE_SEND_CHUNK_TO_CLIENT) {
            write(message.getPayload(), Constants.MESSAGE_FILE_PREFIX);
            return;
        }
        write(formatMessage(message), null);
        // TODO: Przenieść walidację do funkcji
        if (!List.of(Constants.FTP_ENDPOINT_NAME, Constants.HISTORY_ENDPOINT_NAME).contains(message.getSender())) {
            storeInHistory(message);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void run() {
        socketClientInit();
    }

    void setName(String name) {
        this.name = name;
    }

    @SneakyThrows
    void socketClientInit() {
//        String nickname = UUID.randomUUID().toString();
//        try {
//            while (true) {
//                writeln("Enter name \\w{3,16}", null);
//                nickname = bufferedReader.readLine();
//                if (!NameValidators.isNameValid(nickname)) continue;
//                if (messageRouter.getClients().contains(nickname)) {
//                    writeln("Nick " + nickname + " already in use", null);
//                    continue;
//                }
//                break;
//            }
//        setName(nickname);

        //TODO: ustawianie nickname na start
        messageRouter.subscribe(this);
        messageRouter.sendMessage(new Message(MessageType.MESSAGE_JOIN_CHANNEL, getName(), Constants.GLOBAL_ENDPOINT_NAME, null));
        lastDestination = Constants.GLOBAL_ENDPOINT_NAME;
        messageRouter.sendMessage(new Message(MessageType.MESSAGE_TEXT, Constants.SERVER_ENDPOINT_NAME, getName(), "/? - pomoc"));
//    } catch(
//    IOException e)
//
//    {
//        log.error("{} ", e.getMessage(), e);
//    } finally
//
//    {
//        socket.close();
//        messageRouter.sendMessage(new Message(MessageType.MESSAGE_USER_DISCONNECTED, getName(), null, null));
//    }

    }

    void messageFromJMS(String message) {
        parseInput(message.stripTrailing());
    }

    @SneakyThrows
    void parseInput(String text) {
        if (text.startsWith("/?")) {
            help();
            return;
        }
        if(text.startsWith("/q")) {
            jmsChatClient.write(new ChatMessage("q:", "@server", name));
            messageRouter.sendMessage(new Message(MessageType.MESSAGE_USER_DISCONNECTED, getName(), null, null));
            return;
        }
        if (text.startsWith("/h")) {
            messageRouter.sendMessage(new Message(MessageType.MESSAGE_HISTORY_RETRIEVE, getName(), Constants.HISTORY_ENDPOINT_NAME, null));
            return;
        }
        if (text.startsWith("/m ")) {
            commandMessage(text);
            return;
        }
        if (text.startsWith("/j ")) {
            commandJoin(text);
            return;
        }
        if (text.startsWith("/lu ")) {
            listUsers(text);
            return;
        }
        if (text.startsWith("/l ")) {
            leaveChannel(text);
            return;
        }
        if (text.startsWith("/uf ")) {
            uploadFile(text);
            return;
        }

        if (text.startsWith("/df ")) {
            downloadFile(text);
            return;
        }

        if (text.startsWith("/rf ")) {
            registerFileToUpload(text);
            return;
        }

        if (text.startsWith("/pf ")) {
            publishFile(text);
            return;
        }

        if (text.startsWith("/lf ")) {
            listFiles(text);
            return;
        }

        if (text.startsWith("/ef ")) {
            eraseFile(text);
            return;
        }

        if (text.equals("/lc")) {
            listUserChannels();
            return;
        }
        commandMessage("/m " + lastDestination + " " + text);
    }

    private void listUserChannels() {
        messageRouter.sendMessage(MessageType.MESSAGE_LIST_CHANNELS, getName(), null, null);
    }

    private Message sendMessage(MessageType messageType, String source, String destination, String payload) {
        return messageRouter.sendMessage(messageType, source, destination, payload);
    }

    private void eraseFile(String text) {
        String[] fields = text.split("[ ]+", 2);
        if (fields.length == 2) {
            sendMessage(MessageType.MESSAGE_DELETE_FILE, getName(), null, fields[1]);
        }
    }

    private void downloadFile(String text) {
        String[] fields = text.split("[ ]+", 3);
        if (fields.length == 3) {
            sendMessage(MessageType.MESSAGE_DOWNLOAD_FILE, getName(), fields[1], fields[2]);
        }
    }

    private void listFiles(String text) {
        String[] fields = text.split("[ ]+", 2);
        if (fields.length == 2) {
            sendMessage(MessageType.MESSAGE_LIST_FILES, getName(), fields[1], null);
        }
    }

    // Aplikacja klienta wysyła ramki w formacie: /uf base64data
    // koniec pliku gdy wyśle MessageType == MESSAGE_PUBLISH_FILE w payload (fields[1])
    private void uploadFile(String text) {
        String[] fields = text.split("[ ]+", 2);
        if (fields.length == 2) {
            sendMessage(MessageType.MESSAGE_APPEND_FILE, getName(), Constants.FTP_ENDPOINT_NAME, fields[1]);
        }
    }

    private void registerFileToUpload(String text) {
        String[] fields = text.split("[ ]+", 2);
        if (fields.length == 2) {
            sendMessage(MessageType.MESSAGE_REGISTER_FILE_TO_UPLOAD, getName(), Constants.FTP_ENDPOINT_NAME, fields[1]);
        }
    }

    private void publishFile(String text) {
        String[] fields = text.split("[ ]+", 2);
        if (fields.length == 2) {
            sendMessage(MessageType.MESSAGE_PUBLISH_FILE, getName(), Constants.FTP_ENDPOINT_NAME, fields[1]);
        }
    }

    private void leaveChannel(String text) {
        String[] fields = text.split("[ ]+", 2);
        if (fields.length == 2) {
            sendMessage(MessageType.MESSAGE_LEAVE_CHANNEL, getName(), fields[1], null);
            lastDestination = Constants.GLOBAL_ENDPOINT_NAME;
        }
    }

    private void listUsers(String text) {
        String[] fields = text.split("[ ]+", 2);
        if (fields.length == 2) {
            sendMessage(MessageType.MESSAGE_LIST_USERS_ON_CHANNEL, getName(), fields[1], null);
        }
    }

    private void commandJoin(String text) {
        String[] fields = text.split("[ ]+", 2);
        if (fields.length == 2 && NameValidators.isChannelName(fields[1])) {
            sendMessage(MessageType.MESSAGE_JOIN_CHANNEL, getName(), fields[1], null);
            lastDestination = fields[1];
        }
    }

    private void commandMessage(String text) {
        String[] fields = text.split("[ ]+", 2);
        if (fields.length == 2) {
            String[] arguments = fields[1].split(" ", 2);
            if (arguments.length == 2 && (NameValidators.isNameOrChannelValid(arguments[0]) || NameValidators.isChannelSpecial(arguments[0]))) {
                Message message = sendMessage(MessageType.MESSAGE_TEXT, getName(), arguments[0], arguments[1]);
                // Zapis prywatnych (nie na kanał) wiadomości w historii, bo nie robimy echa lokalnego dla takich wiadomości
                if (!message.getReceiver().matches("[@#]\\w{2,16}")) {
                    storeInHistory(message);
                }
                lastDestination = arguments[0];
            }
        }
    }

    @SneakyThrows
    private void help() {
        try (BufferedInputStream reader = new BufferedInputStream(this.getClass().getClassLoader().getResourceAsStream(HELP_FILE))) {
            while (reader.available() > 0) {
                String text = new String(reader.readAllBytes(), StandardCharsets.UTF_8);
                text.lines().forEach(line -> write(line, null));
            }
        }
    }

    @SneakyThrows
    private void write(String text, String prefix) {
//        synchronized (synchronizationObject) {
        jmsChatClient.write(new ChatMessage((prefix == null ? Constants.MESSAGE_TEXT_PREFIX : prefix) + text, "@server", name));
//        }
    }

    private void storeInHistory(Message message) {
        sendMessage(MessageType.MESSAGE_HISTORY_STORE, getName(), Constants.HISTORY_ENDPOINT_NAME, formatMessage(message));
    }

    private String formatMessage(Message message) {
        return String.format(MESSAGE_FORMAT_STRING, message.getSender(), message.getReceiver(), message.getPayload());
    }

}
