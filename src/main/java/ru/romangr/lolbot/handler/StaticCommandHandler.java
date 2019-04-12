package ru.romangr.lolbot.handler;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

abstract class StaticCommandHandler extends CommandHandler {

    private final String command;

    StaticCommandHandler() {
        this.command = Optional.ofNullable(this.getClass().getAnnotation(StaticCommand.class))
                .map(StaticCommand::value)
                .map(BotCommand::getCommand)
                .orElseThrow(() -> new IllegalStateException("No annotation StaticCommand found on "
                        + this.getClass().getCanonicalName()));
    }

    @Override
    boolean isApplicable(String messageText) {
        return StringUtils.equalsIgnoreCase(messageText, command);
    }
}
