package ru.romangr.lolbot.handler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum BotCommand {
    START("/start"),
    CAT("/cat"),
    SUBSCRIBE("/subscribe"),
    UNSUBSCRIBE("/unsubscribe"),
    HELP("/help"),
    SEND_MESSAGE_TO_SUBSCRIBERS("/smts");

    static {
        // check no duplications in commands
        Set<BotCommand> uniqueCommands = Arrays.stream(BotCommand.values())
                .collect(Collectors.toSet());
        if (uniqueCommands.size() < BotCommand.values().length) {
            throw new IllegalStateException("Commands are not unique");
        }
    }

    private final String command;
}
