import CatFinder.CatFinder;
import JsonEntities.GetMeResponse;
import JsonEntities.GetUpdatesResponse;
import Model.Bot;
import Model.Chat;
import Model.Message;
import Model.MessageToSend;
import Model.Update;
import Utils.DelayCalculator;
import Utils.MessageFromFileReader;
import Utils.URLBuilder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import Subscription.SubscribersRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Roman 27.10.2016.
 */
public class SpringRestLolBot implements LolBot {

    private static final String REQUEST_URL_PROPERTY_NAME = "REQUEST_URL";
    private static final String CAT_API_KEY_PROPERTY_NAME = "CAT_API_KEY";
    private static final String GET_UPDATES_METHOD = "getUpdates";
    private static final String GET_ME_METHOD = "getMe";
    private static final String OFFSET_PARAMETER = "offset";
    private static final String HELP_STRING = "Type /cat to get a random cat :3";
    private static final String MESSAGE_TO_NEW_SUBSCRIBER = "Thank you for Subscription!";
    private static final String YOU_ARE_ALREADY_SUBSCRIBED_MESSAGE = "You have already subscribed!";
    private static final String MESSAGE_TO_UNSUBSCRIBED_ONE = "You have unsubscribed!";
    private static final String YOU_ARE_NOT_A_SUBSCRIBER_MESSAGE = "You are not a subscriber yet!";
    private static final Path MESSAGE_TO_SUBSCRIBERS_FILE = Paths.get("message_to_subscribers.txt");
    private static final int TIME_OCLOCK_TO_SEND_MESSAGE_TO_SUBSCRIBERS = 20;

    private static final Logger LOGGER = Logger.getLogger(SpringRestLolBot.class.getName());

    private final SubscribersRepository subscribersRepository =
            new SubscribersRepository(Paths.get("subscribers.data"));
    private final String adminToken;
    private final Queue<String> messagesToSubscribers;

    private String REQUEST_URL;
    private Bot me;

    private RestTemplate restTemplate = new RestTemplate();
    private int currentUpdateOffset = 0;

    public SpringRestLolBot(Path propertyFile) {
        Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(propertyFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        REQUEST_URL = Optional.ofNullable(properties.getProperty(REQUEST_URL_PROPERTY_NAME))
                .orElseThrow(RuntimeException::new);
        me = updateMe();
        Optional.ofNullable(properties.getProperty(CAT_API_KEY_PROPERTY_NAME))
                .ifPresent(CatFinder::setCatApiKey);
        adminToken = getAdminToken();
        sendAdminTokenToAdmin(properties);
        messagesToSubscribers = new LinkedList<>();
    }

    private void sendAdminTokenToAdmin(Properties properties) {
        Integer adminChatId = Integer.valueOf(properties.getProperty("ADMIN_CHAT_ID"));
        this.sendMessage(new MessageToSend(new Chat(adminChatId), adminToken));
    }

    public Bot getMe() {
        return me;
    }

    private Bot updateMe() {
        return restTemplate.getForObject(
                new URLBuilder()
                        .withHost(REQUEST_URL)
                        .withPath(GET_ME_METHOD)
                        .build(),
                GetMeResponse.class
        ).getResult();
    }

    @Override
    public List<Update> getUpdates() {
        GetUpdatesResponse updatesResponse = restTemplate.getForObject(
                new URLBuilder()
                        .withHost(REQUEST_URL)
                        .withPath(GET_UPDATES_METHOD)
                        .withParameter(OFFSET_PARAMETER, currentUpdateOffset)
                        .build(),
                GetUpdatesResponse.class
        );
        if (!updatesResponse.isOk())
            throw new RuntimeException("Cant't get updates!");
        List<Update> updates = updatesResponse.getUpdates();
        if (!updates.isEmpty()) {
            int maxUpdateId = updates.stream().mapToInt(Update::getId)
                    .max().getAsInt();
            currentUpdateOffset = maxUpdateId + 1;
        }
        return updates;
    }

    @Override
    public Message sendMessage(MessageToSend messageToSend) {
        String url = new URLBuilder().withHost(REQUEST_URL)
                .withPath("sendMessage")
                .build();
        return restTemplate.postForObject(url, messageToSend, Message.class);
    }

    @Override
    public void start() {
        startExecutorService();
    }

    private void startExecutorService() {
        LOGGER.info(String.format("Bot started! Total subscribers: %d", subscribersRepository.getSubscribersCount()));
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(this::processUpdates, 0, 30, TimeUnit.SECONDS);
        scheduledExecutorService.scheduleAtFixedRate(() -> LOGGER.info("All systems are fine!"),
                1, 1, TimeUnit.HOURS);

        Duration delay =
                DelayCalculator.calculateDelayToRunAtParticularTime(TIME_OCLOCK_TO_SEND_MESSAGE_TO_SUBSCRIBERS);
        LOGGER.info(String.format("Next sending to subscribers in %d minutes", delay.getSeconds() / 60));
        scheduledExecutorService.scheduleAtFixedRate(this::setSendMessageToSubscribers, delay.getSeconds(),
                DelayCalculator.getSecondsFromHours(24), TimeUnit.SECONDS);
    }

    private String getAdminToken() {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(String.valueOf(new Date().getTime()).getBytes());
            String stringDigest = "";

            for (byte digestByte : digest) {
                if ((0xff & digestByte) < 0x10) {
                    stringDigest += "0" + Integer.toHexString((0xFF & digestByte));
                } else {
                    stringDigest += Integer.toHexString(0xFF & digestByte);
                }
            }
            LOGGER.info(stringDigest);
            return stringDigest;
        } catch (NoSuchAlgorithmException e) {
            LOGGER.severe(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getId() {
        return me.getId();
    }

    @Override
    public String getFirstName() {
        return me.getFirstName();
    }

    @Override
    public String getUsername() {
        return me.getUsername();
    }

    @Override
    public void processUpdates() {
        List<Update> updates = null;
        try {
            updates = this.getUpdates();
            if (updates.isEmpty()) {
                return;
            }
            StringBuilder logString = new StringBuilder();
            logString.append(new Date())
                    .append(": ")
                    .append(updates.size())
                    .append(" updates received from: ");
            boolean commandsReceived = false;
            for (Update update : updates) {
                Chat chat = getChatFromUpdate(update);
                String messageText = update.getMessage().getText();
                messageText = messageText.replace("@GritenLolBot", "");
                switch (messageText) {
                    case "/start":
                        this.sendMessage(new MessageToSend(chat, HELP_STRING));
                        commandsReceived = true;
                        break;
                    case "/cat":
                        LOGGER.info("/cat");
                        this.sendMessage(new MessageToSend(chat, CatFinder.getCat().getUrl()));
                        commandsReceived = true;
                        break;
                    case "/help":
                        this.sendMessage(new MessageToSend(chat, HELP_STRING));
                        commandsReceived = true;
                        break;
                    case "/subscribe":
                        commandsReceived = subscribeUser(update, chat);
                        break;
                    case "/unsubscribe":
                        commandsReceived = unsubscribeUser(update, chat);
                        break;
                    //todo: check token
                    case "/smts":
                        commandsReceived = true;
                        setSendMessageToSubscribers();
                        break;
                    default: {
                        if (messageText.startsWith("/amts")) {
                            String[] splitMessage = messageText.split(" ", 3);
                            String response;
                            if (splitMessage.length == 3 && splitMessage[1].equalsIgnoreCase(adminToken)) {
                                messagesToSubscribers.add(splitMessage[2]);
                                response = String.format("Message added to queue. Its number is %d",
                                        messagesToSubscribers.size());
                                this.sendMessage(chat, response);
                                LOGGER.info(response);
                            } else {
                                response = "Incorrect command syntax";
                                this.sendMessage(chat, response);
                                LOGGER.info(response);
                            }
                        }
                    }
                }
                Stream.of(chat.getTitle(), chat.getUsername(), chat.getFirstName(), chat.getLastName())
                        .filter(Objects::nonNull)
                        .findFirst()
                        .ifPresent(identifier -> {
                                    logString.append(identifier);
                                    logString.append(" ");
                                }
                        );
            }
            if (commandsReceived) {
                LOGGER.info(logString.toString());
            }
        } catch (NullPointerException | ResourceAccessException e) {
            LOGGER.warning(e.getClass().getSimpleName());
        } catch (Exception e) {
            LOGGER.warning(e.getClass() + " " + e.getMessage() + " Exception during processing :" + updates);
        }
    }

    private Message sendMessage(Chat chat, String text) {
        return this.sendMessage(new MessageToSend(chat, text));
    }

    private void setSendMessageToSubscribers() {
        String message = null;
        boolean areGreetingsNeeded = true;
//        if (Files.exists(MESSAGE_TO_SUBSCRIBERS_FILE)) {
//            try {
//                message = new MessageFromFileReader(MESSAGE_TO_SUBSCRIBERS_FILE, true).read();
//                areGreetingsNeeded = false;
//                LOGGER.info("Message from file will be sent");
//            } catch (RuntimeException e) {
//                LOGGER.warning("Exception during reading message to subscribers from file");
//            }
//        }

        if (!messagesToSubscribers.isEmpty()) {
            message = messagesToSubscribers.poll();
            String logMessage = String.format("Message from queue will be sent. There are %d posts left in the queue",
                    messagesToSubscribers.size());
            LOGGER.info(logMessage);
            areGreetingsNeeded = false;
        }

        if (message == null) {
            message = CatFinder.getCat().getUrl();
        }

        for (Chat chat : subscribersRepository.getAllSubscribers()) {
            if (areGreetingsNeeded) {
                final StringBuilder messageToSubscriber = new StringBuilder().append("Your daily cat");
                Stream.of(chat.getTitle(), chat.getFirstName(), chat.getLastName(), chat.getUsername())
                        .filter(Objects::nonNull)
                        .findFirst()
                        .ifPresent(identifier -> {
                                    messageToSubscriber.append(", ")
                                            .append(identifier)
                                            .append("!");
                                }
                        );
                messageToSubscriber.append("\n")
                        .append(message);
                sendMessageSafely(chat, messageToSubscriber.toString());
            } else {
                sendMessageSafely(chat, message);
            }
        }
        LOGGER.info(String.format("Cat has been sent to %d subscribers",
                subscribersRepository.getSubscribersCount()));
    }

    private boolean sendMessageSafely(Chat chat, String message) {
        try {
            this.sendMessage(chat, message);
            return true;
        } catch (RuntimeException e) {
            LOGGER.warning(e.getClass() + " " + e.getMessage() + " sending message to " + chat);
            return false;
        }
    }

    private static Chat getChatFromUpdate(Update update) {
        return update.getMessage().getChat();
    }

    private boolean unsubscribeUser(Update update, Chat chat) {
        if (update.getMessage().getChat() != null) {
            boolean isDeleted = subscribersRepository.deleteSubscriber(update.getMessage().getChat());
            String message;
            if (isDeleted) {
                message = MESSAGE_TO_UNSUBSCRIBED_ONE;
            } else {
                message = YOU_ARE_NOT_A_SUBSCRIBER_MESSAGE;
            }
            this.sendMessage(new MessageToSend(chat, message));
        }
        LOGGER.info(String.format("Someone have unsubscribed. Total subscribers: %d.",
                subscribersRepository.getSubscribersCount()));
        return true;
    }

    private boolean subscribeUser(Update update, Chat chat) {
        if (update.getMessage().getChat() != null) {
            boolean isAdded = subscribersRepository.addSubscriber(update.getMessage().getChat());
            String message;
            if (isAdded) {
                message = MESSAGE_TO_NEW_SUBSCRIBER;
            } else {
                message = YOU_ARE_ALREADY_SUBSCRIBED_MESSAGE;
            }
            this.sendMessage(new MessageToSend(chat, message));
        }
        LOGGER.info(String.format("New subscriber. Total subscribers: %d.",
                subscribersRepository.getSubscribersCount()));
        return true;
    }
}
