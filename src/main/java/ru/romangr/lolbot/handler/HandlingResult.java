package ru.romangr.lolbot.handler;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import ru.romangr.lolbot.handler.action.TelegramAction;

import java.util.Collections;
import java.util.List;

@Value
@Builder
public class HandlingResult {

    @NonNull
    private HandlingStatus status;

    @Builder.Default
    @NonNull
    private List<TelegramAction> actions = Collections.emptyList();
}
