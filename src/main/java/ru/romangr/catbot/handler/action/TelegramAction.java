package ru.romangr.catbot.handler.action;

import ru.romangr.catbot.telegram.model.ExecutionResult;
import ru.romangr.exceptional.Exceptional;

public interface TelegramAction {

    Exceptional<ExecutionResult> execute();

}
