package local.pbaranowski.chat;

import local.pbaranowski.chat.commons.JMSClient;
import local.pbaranowski.chat.commons.MessageType;
import local.pbaranowski.chat.commons.transportlayer.Base64Transcoder;
import local.pbaranowski.chat.commons.transportlayer.MessageInternetFrame;
import local.pbaranowski.chat.commons.transportlayer.Transcoder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.jms.JMSException;
import javax.naming.NamingException;

import local.pbaranowski.chat.commons.ChatMessage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.synchronizedList;
import static local.pbaranowski.chat.commons.Constants.*;

@Slf4j
public class Application {

    private final Transcoder<MessageInternetFrame> transcoder = new Base64Transcoder<>();
    private final JMSClient jmsClient = new JMSClient();
    private String nickname = UUID.randomUUID().toString();
    private final String loginRandomNickname = nickname;
    private String destinationSystem = "@login";

    public Application(String host, int port) throws NamingException {
        jmsClient.setJMSListener(message -> {
            try {
                ChatMessage chatMessage = message.getBody(ChatMessage.class);
//                log.info("JMS Message received: {}", chatMessage);
                String messageToId = chatMessage.getToId();
                if (!messageToId.equals("") && !messageToId.equals(nickname) && !messageToId.equals(loginRandomNickname)) {
                    return;
                }
                var line = chatMessage.getBody();
                if (line.startsWith(MESSAGE_TEXT_PREFIX)) {
                    System.out.println(line.substring(2));
                } else if (line.startsWith(MESSAGE_FILE_PREFIX)) {
                    receiveFile(line);
                } else if (line.startsWith(MESSAGE_SET_NICKNAME_PREFIX)) {
                    setNickname(line.substring(2));
                    destinationSystem = "@server";
                } else if (line.startsWith("q:")) {
                    quit();
                }
            } catch (JMSException e) {
                System.out.println("JMSError: " + e.getMessage());
            }
        });
    }

    void quit() {
        shutdown();
    }

    public static void main(String[] args) throws IOException, NamingException {
        int port = DEFAULT_PORT;
        String host = DEFAULT_HOST;
        if (args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        Application application = new Application(host, port);
        application.consoleLoop();
    }

    private boolean running = true;

    private void consoleLoop() throws IOException {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Choose nickname:");
        while (running) {
            String line = console.readLine();
            if (line.startsWith("/uf ")) {
                uploadFile(line);
                continue;
            }
            if (line.startsWith("/df ")) {
                String[] fields = line.split("[ ]+");
                if (fields.length == 3) {
 //                   requestedFiles.request(fields[2]);
                }
            }
            write(line);
        }
    }

    @SneakyThrows
    private void shutdown() {
        Runtime.getRuntime().exit(0);
    }

    private void uploadFile(String line) throws IOException {
        String[] fields = line.split("[ ]+", 3);
        if (fields.length != 3) {
            System.out.println("ERROR: Syntax error");
            return;
        }
        String channel = fields[1];
        String filename = fields[2];
        File file = new File(filename);
        if (!file.canRead()) {
            System.out.printf("Can't read from file %s%n", file.getName());
            return;
        }
        String fileTransferUUID = UUID.randomUUID().toString();
        SimpleRESTClient.put(fileTransferUUID, filename);

//        MessageInternetFrame frame = new MessageInternetFrame();
//        frame.setMessageType(MessageType.MESSAGE_REGISTER_FILE_TO_UPLOAD);
//        frame.setSourceName(filename);
//        frame.setDestinationName(channel);
//        frame.setData(fileTransferUUID.getBytes(StandardCharsets.UTF_8));
//        synchronized (transcoder) {
//            write("/rf " + transcoder.encodeObject(frame, MessageInternetFrame.class));
//        }

//        frame.setMessageType(MessageType.MESSAGE_APPEND_FILE);
//        frame.setSourceName(filename);
//        frame.setDestinationName(fileTransferUUID);
//        try (FileInputStream fileInputStream = new FileInputStream(file)) {
//            if (fileInputStream.available() > 0) {
//                byte[] data = fileInputStream.readNBytes(256);
//                frame.setData(data);
//                synchronized (transcoder) {
//                    write("/uf " + transcoder.encodeObject(frame, MessageInternetFrame.class));
//                }
//            }
//        }
//        frame.setData(null);
//        frame.setMessageType(MessageType.MESSAGE_PUBLISH_FILE);
        synchronized (transcoder) {
            write("/pf " + channel + " " + fileTransferUUID + " " + filename);
        }
    }

    @SneakyThrows
    private void write(String text) {
        jmsClient.write(new ChatMessage(text, nickname, destinationSystem));
    }

    @SneakyThrows
    private void receiveFile(String line) {
        String receiverText = line.substring(2);
        String[] fields = receiverText.split(" ", 2);
        SimpleRESTClient.get(fields[0], fields[1]);
    }

    private void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
