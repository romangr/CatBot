package ru.romangr.lolbot.telegram.dto;

import ru.romangr.lolbot.telegram.model.Message;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageResponse {

  private boolean ok;
  private Message result;

}
