package ru.romangr.catbot;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import ru.romangr.catbot.catfinder.CatFinder;
import ru.romangr.catbot.executor.RateLimiter;
import ru.romangr.catbot.executor.TelegramActionExecutor;
import ru.romangr.catbot.executor.action.TelegramActionFactory;
import ru.romangr.catbot.handler.AddMessageToSubscribersCommandHandler;
import ru.romangr.catbot.handler.CatCommandHandler;
import ru.romangr.catbot.handler.CommandHandler;
import ru.romangr.catbot.handler.HelpCommandHandler;
import ru.romangr.catbot.handler.MessagePreprocessor;
import ru.romangr.catbot.handler.SendMessageToSubscribersCommandHandler;
import ru.romangr.catbot.handler.StartCommandHandler;
import ru.romangr.catbot.handler.SubscribeCommandHandler;
import ru.romangr.catbot.handler.UnknownCommandHandler;
import ru.romangr.catbot.handler.UnsubscribeCommandHandler;
import ru.romangr.catbot.handler.UpdatesHandler;
import ru.romangr.catbot.handler.DocumentHandler;
import ru.romangr.catbot.subscription.SubscribersRepository;
import ru.romangr.catbot.subscription.SubscribersService;
import ru.romangr.catbot.telegram.TelegramAdminNotifier;
import ru.romangr.catbot.telegram.TelegramRequestExecutor;
import ru.romangr.catbot.utils.PropertiesResolver;
import ru.romangr.exceptional.Exceptional;

/**
 * Roman 23.09.2017.
 */
@Slf4j
public class SpringRestCatBotFactory {

  public static Exceptional<RestBot> newBot(Map<String, String> properties, String buildInfo) {
    return Exceptional.exceptional(new PropertiesResolver(properties, buildInfo))
        .safelyMap(SpringRestCatBotFactory::initialize);
  }

  private static RestBot initialize(PropertiesResolver resolver) {
    RestTemplate restTemplate = new RestTemplate();
    String requestUrl = resolver.getRequestUrl();
    TelegramActionFactory actionFactory = new TelegramActionFactory(restTemplate, requestUrl);
    RateLimiter rateLimiter = new RateLimiter();
    TelegramActionExecutor actionExecutor = new TelegramActionExecutor(
        Executors.newSingleThreadScheduledExecutor(), rateLimiter, actionFactory);
    TelegramRequestExecutor requestExecutor = new TelegramRequestExecutor(restTemplate, requestUrl);
    CatFinder catFinder = new CatFinder(restTemplate, resolver.getCatApiKey());
    SubscribersRepository subscribersRepository = new SubscribersRepository(
        resolver.getSubscribersFilePath());
    var adminChatId = resolver.getAdminChatId().orElse(null);
    TelegramAdminNotifier adminNotifier
        = new TelegramAdminNotifier(actionFactory, actionExecutor, resolver, adminChatId);
    SubscribersService subscribersService = new SubscribersService(
        subscribersRepository,
        requestExecutor,
        catFinder,
        actionFactory,
        adminNotifier
    );
    List<CommandHandler> handlers = List.of(
        new StartCommandHandler(actionFactory),
        new HelpCommandHandler(actionFactory, resolver.getTimeToSendMessageToSubscribers()),
        new CatCommandHandler(actionFactory, catFinder),
        new SubscribeCommandHandler(actionFactory, subscribersService),
        new UnsubscribeCommandHandler(actionFactory, subscribersService),
        new SendMessageToSubscribersCommandHandler(subscribersService, adminChatId),
        new AddMessageToSubscribersCommandHandler(subscribersService, actionFactory, adminChatId),
        new DocumentHandler(actionFactory, subscribersService, adminChatId)
    );
    UnknownCommandHandler unknownCommandHandler = new UnknownCommandHandler(actionFactory);
    MessagePreprocessor messagePreprocessor = new MessagePreprocessor(resolver.getBotName());
    UpdatesHandler updatesHandler
        = new UpdatesHandler(messagePreprocessor, handlers, unknownCommandHandler, actionExecutor);
    return new SpringRestCatBot(
        updatesHandler,
        subscribersService,
        resolver.getUpdatesCheckPeriod(),
        requestExecutor,
        actionExecutor,
        resolver,
        adminNotifier
    );
  }
}
