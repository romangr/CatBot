package ru.romangr.lolbot;

import lombok.SneakyThrows;
import org.springframework.web.client.RestTemplate;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.lolbot.catfinder.CatFinder;
import ru.romangr.lolbot.handler.*;
import ru.romangr.lolbot.subscription.SubscribersRepository;
import ru.romangr.lolbot.subscription.SubscribersService;
import ru.romangr.lolbot.telegram.TelegramActionExecutor;
import ru.romangr.lolbot.telegram.TelegramRequestExecutor;
import ru.romangr.lolbot.utils.PropertiesResolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Roman 23.09.2017.
 */
public class SpringRestLolBotFactory {

    public static Exceptional<RestBot> newBot(Path propertiesFile) {
        return Exceptional.getExceptional(() -> getProperties(propertiesFile))
                .map(PropertiesResolver::new)
                .safelyMap(SpringRestLolBotFactory::initialize);

    }

    private static RestBot initialize(PropertiesResolver resolver) {
        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = resolver.getRequestUrl();
        TelegramActionExecutor actionExecutor = new TelegramActionExecutor(restTemplate, requestUrl);
        TelegramRequestExecutor requestExecutor = new TelegramRequestExecutor(restTemplate, requestUrl);
        CatFinder catFinder = new CatFinder(restTemplate, resolver.getCatApiKey());
        SubscribersService subscribersService
                = new SubscribersService(new SubscribersRepository(), requestExecutor, actionExecutor, catFinder);
        Optional<Long> adminChatId = resolver.getAdminChatId();
        List<CommandHandler> handlers = List.of(
                new StartCommandHandler(actionExecutor),
                new HelpCommandHandler(actionExecutor),
                new CatCommandHandler(actionExecutor, catFinder),
                new SubscribeCommandHandler(actionExecutor, subscribersService),
                new UnsubscribeCommandHandler(actionExecutor, subscribersService),
                new SendMessageToSubscribersCommandHandler(actionExecutor, subscribersService, adminChatId)
        );
        UnknownCommandHandler unknownCommandHandler = new UnknownCommandHandler(actionExecutor);
        MessagePreprocessor messagePreprocessor = new MessagePreprocessor(resolver.getBotName());
        UpdatesHandler updatesHandler = new UpdatesHandler(messagePreprocessor, handlers, unknownCommandHandler);
        return new SpringRestLolBot(
                updatesHandler,
                subscribersService,
                resolver.getUpdatesCheckPeriod(),
                requestExecutor
        );
    }

    @SneakyThrows
    private static Properties getProperties(Path propertiesFile) {
        Properties properties = new Properties();
        properties.load(Files.newInputStream(propertiesFile));
        return properties;
    }
}
