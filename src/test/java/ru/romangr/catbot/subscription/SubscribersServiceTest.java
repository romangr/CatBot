package ru.romangr.catbot.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.romangr.catbot.catfinder.Cat;
import ru.romangr.catbot.catfinder.CatFinder;
import ru.romangr.catbot.executor.action.TelegramAction;
import ru.romangr.catbot.executor.action.TelegramActionFactory;
import ru.romangr.catbot.telegram.TelegramRequestExecutor;
import ru.romangr.catbot.telegram.model.Chat;
import ru.romangr.exceptional.Exceptional;

class SubscribersServiceTest {

  private static final String NO_INTERNET_MESSAGE = "Can't send result to subscribers now, will send it later";

  private SubscribersRepository repository = mock(SubscribersRepository.class);
  private TelegramRequestExecutor requestExecutor = mock(TelegramRequestExecutor.class);
  private CatFinder catFinder = mock(CatFinder.class);
  private TelegramActionFactory actionFactory = mock(TelegramActionFactory.class);
  private SubscribersService service
      = new SubscribersService(repository, requestExecutor, catFinder, actionFactory);


  @Test
  void sendMessageToSubscribersWhenNoInternet() {
    given(requestExecutor.isConnectedToInternet()).willReturn(false);

    Exceptional<List<TelegramAction>> result = service.sendMessageToSubscribers();

    assertThat(result.isException()).isTrue();
    assertThat(result.getException()).isInstanceOf(RuntimeException.class);
    assertThat(result.getException().getMessage()).isEqualTo(NO_INTERNET_MESSAGE);
    verify(requestExecutor).isConnectedToInternet();
    verifyNoMoreInteractions(requestExecutor);
    verifyZeroInteractions(repository, catFinder, actionFactory);
  }

  @Test
  void sendMessageToSubscribersFromQueue() {
    given(requestExecutor.isConnectedToInternet()).willReturn(true);
    given(repository.getSubscribersCount()).willReturn(2);
    Chat chat1 = getChat(1, "username_1");
    Chat chat2 = getChat(2, "username_2");
    given(repository.getAllSubscribers()).willReturn(List.of(chat1, chat2));
    given(actionFactory.newSendMessageAction(any(), any())).willReturn(mock(TelegramAction.class));
    service.addMessageToSubscribers("Some message");

    Exceptional<List<TelegramAction>> result = service.sendMessageToSubscribers();

    assertThat(result.isValuePresent()).isTrue();
    List<TelegramAction> actions = result.getValue();
    assertThat(actions).hasSize(2);
    verify(actionFactory).newSendMessageAction(eq(chat1), eq("Some message"));
    verify(actionFactory).newSendMessageAction(eq(chat2), eq("Some message"));
    verifyNoMoreInteractions(actionFactory);
    verify(requestExecutor).isConnectedToInternet();
    verifyNoMoreInteractions(requestExecutor);
    verify(repository).getSubscribersCount();
    verify(repository).getAllSubscribers();
    verifyNoMoreInteractions(repository);
    verifyZeroInteractions(catFinder);
  }

  @Test
  void sendMessageWithCatToSubscribers() {
    given(requestExecutor.isConnectedToInternet()).willReturn(true);
    given(repository.getSubscribersCount()).willReturn(2);
    Chat chat1 = getChat(1, "username_1");
    Chat chat2 = getChat(2, "username_2");
    given(repository.getAllSubscribers()).willReturn(List.of(chat1, chat2));
    given(actionFactory.newSendMessageAction(any(), any())).willReturn(mock(TelegramAction.class));
    given(catFinder.getCat()).willReturn(Exceptional.exceptional(new Cat("cat_url")));

    Exceptional<List<TelegramAction>> result = service.sendMessageToSubscribers();

    assertThat(result.isValuePresent()).isTrue();
    List<TelegramAction> actions = result.getValue();
    assertThat(actions).hasSize(2);
    verify(actionFactory).newSendMessageAction(eq(chat1), eq("Your daily cat, username_1!\ncat_url"));
    verify(actionFactory).newSendMessageAction(eq(chat2), eq("Your daily cat, username_2!\ncat_url"));
    verifyNoMoreInteractions(actionFactory);
    verify(requestExecutor).isConnectedToInternet();
    verifyNoMoreInteractions(requestExecutor);
    verify(repository).getSubscribersCount();
    verify(repository).getAllSubscribers();
    verifyNoMoreInteractions(repository);
    verify(catFinder).getCat();
    verifyNoMoreInteractions(catFinder);
  }

  @NotNull
  private Chat getChat(int id, String username) {
    Chat chat2 = new Chat(id);
    chat2.setUsername(username);
    return chat2;
  }
}
