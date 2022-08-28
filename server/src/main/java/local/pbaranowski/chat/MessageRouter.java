package local.pbaranowski.chat;

import local.pbaranowski.chat.commons.ChatMessage;
import local.pbaranowski.chat.commons.Constants;
import local.pbaranowski.chat.commons.MessageType;
import local.pbaranowski.chat.commons.NameValidators;
import local.pbaranowski.chat.commons.transportlayer.Base64Transcoder;
import local.pbaranowski.chat.commons.transportlayer.MessageInternetFrame;
import local.pbaranowski.chat.commons.transportlayer.Transcoder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSException;
import java.util.LinkedList;
import java.util.List;

import static local.pbaranowski.chat.commons.Constants.SERVER_ENDPOINT_NAME;
import static local.pbaranowski.chat.commons.MessageType.MESSAGE_TEXT;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class MessageRouter {
    @Getter
    private final ClientsCollection<Client> clients = new HashMapClients<>();
    //    private LogSerializer logSerializer;
    @Inject
    private JMSChatClient jmsClient;

    @PostConstruct
    public void init() {
        ChannelClient global = new ChannelClient(Constants.GLOBAL_ENDPOINT_NAME, new HashMapClients<>());
        subscribe(global);
        HistoryClient historyClient = new HistoryClient();
        subscribe(historyClient);
        Transcoder<MessageInternetFrame> transcoder = new Base64Transcoder<>();
        FTPClient ftpClient = new FTPClient(transcoder, new DiskFileStorage(transcoder));
        subscribe(ftpClient);
    }

    void receiveJMSMessage(javax.jms.Message message) {
//        log.info("###### MESSAGE RECEIVED! ###########");
        try {
            ChatMessage chatMessage = message.getBody(ChatMessage.class);
            if (chatMessage.getFromId().equals("@server"))
                return;
//            log.info("Incoming ChatMessage: {}", chatMessage.toString());
            if (chatMessage.getToId().equals("@login")) {
                loginUser(chatMessage);
                return;
            }
            var client = clients.getClient(message.getBody(ChatMessage.class).getFromId());
            System.out.println("########### Client: " + client.getName());
            if (client instanceof SocketClient) {
                ((SocketClient) client).messageFromJMS(chatMessage.getBody()); //TODO sprawdzić, czy działa
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private void loginUser(ChatMessage chatMessage) {
        //        try {
//            while (true) {
//                writeln("Enter name \\w{3,16}", null);
        String nickname = chatMessage.getBody().trim();
        if (!NameValidators.isNameValid(nickname)) {
            jmsClient.write(new ChatMessage("m: Invalid nickname. Enter name \\w{3,16}", "@server", chatMessage.getFromId()));
            return;
        }
        if (getClients().contains(nickname)) {
            jmsClient.write(new ChatMessage("m:" + "Nick " + nickname + " already in use", "@server", chatMessage.getFromId()));
            return;
        }
        SocketClient socketClient = new SocketClient();
        socketClient.setName(nickname);
        socketClient.setMessageRouter(this);
        socketClient.setJmsChatClient(jmsClient);
        jmsClient.write(new ChatMessage("n:" + nickname, "@server", chatMessage.getFromId()));
        socketClient.socketClientInit();
    }

    void subscribe(Client client) {
        clients.add(client);
        client.setMessageRouter(this);
    }

    Message sendMessage(Message message) {
//        log.info(logSerializer.fromMessageToString(message));
        switch (message.getMessageType()) {
            case MESSAGE_TO_ALL:
                clients.forEach(client -> client.write(message));
                break;
            case MESSAGE_TEXT:
            case MESSAGE_HISTORY_RETRIEVE:
            case MESSAGE_LIST_USERS_ON_CHANNEL: {
                Client client;
                if ((client = clients.getClient(message.getReceiver())) != null) {
                    client.write(message);
                }
            }
            break;
            case MESSAGE_JOIN_CHANNEL:
                if (!clients.contains(message.getReceiver())) {
                    ChannelClient channelClient = new ChannelClient(message.getReceiver(), new HashMapClients<>());
                    channelClient.addClient(clients.getClient(message.getSender()));
                    subscribe(channelClient);
                }
                clients.getClient(message.getReceiver()).write(message);
                break;
            case MESSAGE_LEAVE_CHANNEL: {
                Client client;
                if (clients.contains(message.getReceiver())) {
                    client = clients.getClient(message.getReceiver());
                    client.write(message);
                    removeChannelClient(client);
                }
            }
            break;
            case MESSAGE_LIST_CHANNELS:
                listUserChannels(message);
                break;
            case MESSAGE_HISTORY_STORE:
                clients.getClient(Constants.HISTORY_ENDPOINT_NAME).write(message);
                break;
            case MESSAGE_APPEND_FILE:
            case MESSAGE_PUBLISH_FILE:
            case MESSAGE_REGISTER_FILE_TO_UPLOAD:
                if (clients.getClient(message.getReceiver()) != null) {
                    clients.getClient(Constants.FTP_ENDPOINT_NAME).write(message);
                }
                break;
            case MESSAGE_DOWNLOAD_FILE:
            case MESSAGE_LIST_FILES:
            case MESSAGE_DELETE_FILE:
                clients.getClient(Constants.FTP_ENDPOINT_NAME).write(message);
                break;
            case MESSAGE_SEND_CHUNK_TO_CLIENT:
                clients.getClient(message.getReceiver()).write(message);
                break;
            case MESSAGE_USER_DISCONNECTED: {
                Client socketClient = clients.getClient(message.getSender());
                for (Client client : clients.getClients().values()) {
                    if (client instanceof ChannelClient) {
                        client.write(
                                new Message(MessageType.MESSAGE_USER_DISCONNECTED, socketClient.getName(), client.getName(), null)
                        );
                        removeChannelClient(client);
                    }
                }
            }
        }
        return message;
    }

    private void listUserChannels(Message message) {
        List<String> channels = new LinkedList<>();
        clients.getClients()
                .values()
                .stream()
                .filter(client -> client instanceof ChannelClient)
                .filter(client -> ((ChannelClient) client).hasClient(message.getSender()))
                .forEach(client -> channels.add(client.getName()));
        sendMessage(MESSAGE_TEXT, SERVER_ENDPOINT_NAME, message.getSender(), "You're a member of channels: " + String.join(" ", channels));
    }


    Message sendMessage(MessageType messageType, String source, String destination, String payload) {
        return sendMessage(new Message(messageType, source, destination, payload));
    }

    private void removeChannelClient(Client client) {
        if (client.isEmpty() && !client.getName().equals(Constants.GLOBAL_ENDPOINT_NAME)) {
            clients.getClient(Constants.FTP_ENDPOINT_NAME).write(
                    new Message(MessageType.MESSAGE_DELETE_ALL_FILES_ON_CHANNEL, SERVER_ENDPOINT_NAME, client.getName(), null)
            );
            clients.remove(client);
        }
    }

    ChannelClient getChannelClient(String name) {
        if (clients.getClient(name) instanceof ChannelClient) {
            return ChannelClient.class.cast(clients.getClient(name));
        } else {
            return null;
        }
    }

}
