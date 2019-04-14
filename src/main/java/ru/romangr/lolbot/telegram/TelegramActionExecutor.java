package ru.romangr.lolbot.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.romangr.lolbot.handler.action.TelegramAction;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class TelegramActionExecutor {

    public void execute(List<TelegramAction> actions) {
        actions.stream()
                .map(TelegramAction::execute)
                .forEach(exceptional -> exceptional
                        .ifException(e -> log.warn("Exception during action execution", e)));
    }

}
