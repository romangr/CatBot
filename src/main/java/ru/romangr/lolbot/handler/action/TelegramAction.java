package ru.romangr.lolbot.handler.action;

import ru.romangr.exceptional.Exceptional;

public interface TelegramAction {

    Exceptional<?> execute();

}
