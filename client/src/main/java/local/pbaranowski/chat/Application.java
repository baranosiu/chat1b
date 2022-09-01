package local.pbaranowski.chat;

import local.pbaranowski.chat.commons.JMSClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.jms.JMSException;
import javax.naming.NamingException;

import local.pbaranowski.chat.commons.ChatMessage;

import java.io.*;
import java.util.UUID;

import static local.pbaranowski.chat.commons.Constants.*;

@Slf4j
public class Application {
    private JMSClient jmsClient;
    private String nickname = UUID.randomUUID().toString();
    private final String loginRandomNickname = nickname;
    private String destinationSystem = "@login";

    public Application(String endpoint) throws NamingException {
        jmsClient = new JMSClient(endpoint);
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
        String endpoint = DEFAULT_ENDPOINT;
        if (args.length == 1) {
            endpoint = args[0];
        }
        Application application = new Application(endpoint);
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
        write("/pf " + channel + " " + fileTransferUUID + " " + filename);
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
