package ru.romangr.catbot.handler.action;

import ru.romangr.exceptional.Exceptional;

public interface TelegramAction {

    Exceptional<?> execute();

}
