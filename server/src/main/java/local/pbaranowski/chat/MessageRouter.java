package local.pbaranowski.chat;

import local.pbaranowski.chat.JMS.JMSWriter;
import local.pbaranowski.chat.JMS.JMSMessage;
import local.pbaranowski.chat.commons.Constants;
import local.pbaranowski.chat.commons.MessageType;
import local.pbaranowski.chat.history.HistoryClient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import java.util.LinkedList;
import java.util.List;

import static local.pbaranowski.chat.commons.Constants.*;
import static local.pbaranowski.chat.commons.MessageType.MESSAGE_TEXT;

@Slf4j
@ApplicationScoped
@NoArgsConstructor
public class MessageRouter {
    @Getter
    private final ClientsCollection<Client> clients = new HashMapClients<>();
    @Inject
    private JMSWriter jmsWriter;
    @Inject
    private HistoryClient historyClient;
    @Inject
    private FTPClient ftpClient;

    @PostConstruct
    public void init() {
        ChannelClient globalClient = new ChannelClient(GLOBAL_ENDPOINT_NAME, new HashMapClients<>());
        subscribe(globalClient);
        subscribe(historyClient);
        subscribe(ftpClient);
    }

    public void receiveJMSMessage(javax.jms.Message message) {
        try {
            JMSMessage jmsMessage = message.getBody(JMSMessage.class);
            if (jmsMessage.getFromId().equals(SERVER_ENDPOINT_NAME))
                return;
            if (jmsMessage.getToId().equals(LOGIN_ENDPOINT_NAME)) {
                loginUser(jmsMessage);
                return;
            }
            var client = clients.getClient(message.getBody(JMSMessage.class).getFromId());
            if (client instanceof RemoteJMSClient) { // Wiadomość obsługuje najpierw odpowiedni obiekt klienta i dopiero on
                                                     // decyduje czy ewentualnie przesyłać ją (już bezpośrednio bez JMS)
                                                     // ponownie do MessageRoutera
                ((RemoteJMSClient) client).messageFromJMS(jmsMessage.getBody());
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private void loginUser(JMSMessage jmsMessage) {
        String nickname = jmsMessage.getBody().trim();
        if (!NameValidators.isNameValid(nickname)) {
            jmsWriter.write(new JMSMessage(MESSAGE_TEXT_PREFIX + "Invalid nickname. Enter name \\w{3,16}", SERVER_ENDPOINT_NAME, jmsMessage.getFromId()));
            return;
        }
        if (getClients().contains(nickname)) {
            jmsWriter.write(new JMSMessage(MESSAGE_TEXT_PREFIX + "Nick " + nickname + " already in use", SERVER_ENDPOINT_NAME, jmsMessage.getFromId()));
            return;
        }
        RemoteJMSClient remoteJMSClient = new RemoteJMSClient(nickname,this,jmsWriter);
        jmsWriter.write(new JMSMessage(MESSAGE_SET_NICKNAME_PREFIX + nickname, SERVER_ENDPOINT_NAME, jmsMessage.getFromId()));
        remoteJMSClient.jmsClientInit();
    }

    public void subscribe(Client client) {
        clients.add(client);
        client.setMessageRouter(this);
    }

    public Message sendMessage(Message message) {
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
                Client jmsClient = clients.getClient(message.getSender());
                for (Client client : clients.getClients().values()) {
                    if (client instanceof ChannelClient) {
                        client.write(
                                new Message(MessageType.MESSAGE_USER_DISCONNECTED, jmsClient.getName(), client.getName(), null)
                        );
                        removeChannelClient(client);
                    }
                }
                clients.remove(jmsClient); //TODO sprawdzić
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


    public Message sendMessage(MessageType messageType, String source, String destination, String payload) {
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
