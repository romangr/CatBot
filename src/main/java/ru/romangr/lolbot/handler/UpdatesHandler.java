package ru.romangr.lolbot.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.lolbot.handler.action.TelegramAction;
import ru.romangr.lolbot.telegram.TelegramActionExecutor;
import ru.romangr.lolbot.telegram.model.Chat;
import ru.romangr.lolbot.telegram.model.Update;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class UpdatesHandler {

    private final MessagePreprocessor messagePreprocessor;
    private final List<CommandHandler> commandHandlers;
    private final UnknownCommandHandler unknownCommandHandler;
    private final TelegramActionExecutor actionExecutor;

    public void handleUpdate(Update update) {
        Chat chat = getChatFromUpdate(update);
        String messageText = update.getMessage().getText();
        String preprocessedText = messagePreprocessor.process(messageText);
        List<Exceptional<HandlingResult>> results = commandHandlers.stream()
                .map(commandHandler -> commandHandler.handle(chat, preprocessedText))
                .collect(Collectors.toList());
        results.stream()
                .filter(Exceptional::isException)
                .forEach(e -> e.ifException(ex -> log.warn("Error during updates handling", ex)));
        boolean notHandledAtAll = results.stream()
                .map(handlingResultExceptional ->
                        handlingResultExceptional.map(HandlingResult::getStatus))
                .noneMatch(e -> (e.isException() || e.getValue().equals(HandlingStatus.HANDLED)));
        if (notHandledAtAll) {
            log.debug("Unknown command: {}", messageText);
            unknownCommandHandler.handle(chat)
                    .ifValue(handlingResult -> actionExecutor.execute(handlingResult.getActions()));
        }
        List<TelegramAction> actions = results.stream()
                .filter(Exceptional::isValuePresent)
                .map(Exceptional::getValue)
                .flatMap(handlingResult -> handlingResult.getActions().stream())
                .collect(Collectors.toList());
        actionExecutor.execute(actions);
    }

    private static Chat getChatFromUpdate(Update update) {
        return update.getMessage().getChat();
    }
}
