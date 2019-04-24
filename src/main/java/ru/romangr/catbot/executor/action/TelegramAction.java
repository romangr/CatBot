package ru.romangr.catbot.executor.action;

import ru.romangr.catbot.telegram.model.Chat;
import ru.romangr.catbot.telegram.model.ExecutionResult;
import ru.romangr.exceptional.Exceptional;

public interface TelegramAction {

  Exceptional<ExecutionResult> execute();

  Chat getChat();

}
