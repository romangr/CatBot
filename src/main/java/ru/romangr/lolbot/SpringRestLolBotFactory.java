package ru.romangr.lolbot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.lolbot.catfinder.CatFinder;
import ru.romangr.lolbot.handler.*;
import ru.romangr.lolbot.handler.action.TelegramActionFactory;
import ru.romangr.lolbot.subscription.SubscribersRepository;
import ru.romangr.lolbot.subscription.SubscribersService;
import ru.romangr.lolbot.telegram.TelegramActionExecutor;
import ru.romangr.lolbot.telegram.TelegramRequestExecutor;
import ru.romangr.lolbot.utils.PropertiesResolver;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Roman 23.09.2017.
 */
@Slf4j
public class SpringRestLolBotFactory {

    public static Exceptional<RestBot> newBot(Map<String, String> properties) {
        return Exceptional.exceptional(properties)
                .map(PropertiesResolver::new)
                .safelyMap(SpringRestLolBotFactory::initialize);
    }

    private static RestBot initialize(PropertiesResolver resolver) {
        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = resolver.getRequestUrl();
        TelegramActionFactory actionFactory = new TelegramActionFactory(restTemplate, requestUrl);
        TelegramActionExecutor actionExecutor = new TelegramActionExecutor();
        TelegramRequestExecutor requestExecutor = new TelegramRequestExecutor(restTemplate, requestUrl);
        CatFinder catFinder = new CatFinder(restTemplate, resolver.getCatApiKey());
        SubscribersRepository subscribersRepository = new SubscribersRepository(resolver.getSubscribersFilePath());
        SubscribersService subscribersService
                = new SubscribersService(subscribersRepository, requestExecutor, catFinder, actionFactory);
        Optional<Long> adminChatId = resolver.getAdminChatId();
        List<CommandHandler> handlers = List.of(
                new StartCommandHandler(actionFactory),
                new HelpCommandHandler(actionFactory),
                new CatCommandHandler(actionFactory, catFinder),
                new SubscribeCommandHandler(actionFactory, subscribersService),
                new UnsubscribeCommandHandler(actionFactory, subscribersService),
                new SendMessageToSubscribersCommandHandler(actionFactory, subscribersService, adminChatId)
        );
        UnknownCommandHandler unknownCommandHandler = new UnknownCommandHandler(actionFactory);
        MessagePreprocessor messagePreprocessor = new MessagePreprocessor(resolver.getBotName());
        UpdatesHandler updatesHandler
                = new UpdatesHandler(messagePreprocessor, handlers, unknownCommandHandler, actionExecutor);
        return new SpringRestLolBot(
                updatesHandler,
                subscribersService,
                resolver.getUpdatesCheckPeriod(),
                requestExecutor,
                actionExecutor
        );
    }
}
