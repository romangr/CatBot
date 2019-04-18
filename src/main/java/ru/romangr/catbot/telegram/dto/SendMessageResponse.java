package ru.romangr.catbot.telegram.dto;

import ru.romangr.catbot.telegram.model.Message;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageResponse {

  private boolean ok;
  private Message result;

}
