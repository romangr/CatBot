package ru.romangr.catbot.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import ru.romangr.catbot.catfinder.Cat;
import ru.romangr.catbot.catfinder.CatFinder;
import ru.romangr.catbot.executor.action.TelegramAction;
import ru.romangr.catbot.executor.action.TelegramActionFactory;
import ru.romangr.catbot.telegram.TelegramAdminNotifier;
import ru.romangr.catbot.telegram.TelegramRequestExecutor;
import ru.romangr.catbot.telegram.model.Chat;
import ru.romangr.exceptional.Exceptional;

class SubscribersServiceTest {

  private static final String NO_INTERNET_MESSAGE = "Can't send result to subscribers now, will send it later";

  private final SubscribersRepository repository = mock(SubscribersRepository.class);
  private final TelegramRequestExecutor requestExecutor = mock(TelegramRequestExecutor.class);
  private final CatFinder catFinder = mock(CatFinder.class);
  private final TelegramActionFactory actionFactory = mock(TelegramActionFactory.class);
  private final TelegramAdminNotifier notifier = mock(TelegramAdminNotifier.class);
  private final SubscribersService service
      = new SubscribersService(repository, requestExecutor, catFinder, actionFactory, notifier);


  @Test
  void sendMessageToSubscribersWhenNoInternet() {
    given(requestExecutor.isConnectedToInternet()).willReturn(false);

    Exceptional<List<TelegramAction>> result = service.sendMessageToSubscribers();

    assertThat(result.isException()).isTrue();
    assertThat(result.getException()).isInstanceOf(RuntimeException.class);
    assertThat(result.getException().getMessage()).isEqualTo(NO_INTERNET_MESSAGE);
    verify(requestExecutor).isConnectedToInternet();
    verifyNoMoreInteractions(requestExecutor);
    verifyNoMoreInteractions(repository, catFinder, actionFactory);
  }

  @Test
  void sendMessageToSubscribersFromQueue() {
    given(requestExecutor.isConnectedToInternet()).willReturn(true);
    given(repository.getSubscribersCount()).willReturn(2);
    Chat chat1 = getChat(1, "username_1");
    Chat chat2 = getChat(2, "username_2");
    given(repository.getAllSubscribers()).willReturn(List.of(chat1, chat2));
    given(actionFactory.newSendMessageAction(any(), any())).willReturn(mock(TelegramAction.class));
    service.addMessageToSubscribers(MessageToSubscribers.Factory.textMessage("Some message"));

    Exceptional<List<TelegramAction>> result = service.sendMessageToSubscribers();

    assertThat(result.isValuePresent()).isTrue();
    List<TelegramAction> actions = result.getValue();
    assertThat(actions).hasSize(2);
    MessageToSubscribers messageToSubscribers
        = MessageToSubscribers.Factory.textMessage("Some message");
    verify(actionFactory).newAction(eq(chat1), eq(messageToSubscribers));
    verify(actionFactory).newAction(eq(chat2), eq(messageToSubscribers));
    verifyNoMoreInteractions(actionFactory);
    verify(requestExecutor).isConnectedToInternet();
    verifyNoMoreInteractions(requestExecutor);
    verify(repository).getSubscribersCount();
    verify(repository).getAllSubscribers();
    verifyNoMoreInteractions(repository);
    verifyNoMoreInteractions(catFinder);
  }

  @Test
  void sendMessageToSubscribersFromQueueResolvingTemplateVariables() {
    given(requestExecutor.isConnectedToInternet()).willReturn(true);
    given(repository.getSubscribersCount()).willReturn(2);
    Chat chat1 = getChat(1, "username_1");
    Chat chat2 = getChat(2, "username_2");
    given(repository.getAllSubscribers()).willReturn(List.of(chat1, chat2));
    given(actionFactory.newSendMessageAction(any(), any())).willReturn(mock(TelegramAction.class));
    service.addMessageToSubscribers(MessageToSubscribers.Factory.textMessage("Some message for $idf"));

    Exceptional<List<TelegramAction>> result = service.sendMessageToSubscribers();

    assertThat(result.isValuePresent()).isTrue();
    List<TelegramAction> actions = result.getValue();
    assertThat(actions).hasSize(2);
    MessageToSubscribers messageToSubscribers1
        = MessageToSubscribers.Factory.textMessage("Some message for username_1");
    verify(actionFactory).newAction(eq(chat1), eq(messageToSubscribers1));
    MessageToSubscribers messageToSubscribers2
        = MessageToSubscribers.Factory.textMessage("Some message for username_2");
    verify(actionFactory).newAction(eq(chat2), eq(messageToSubscribers2));
    verifyNoMoreInteractions(actionFactory);
    verify(requestExecutor).isConnectedToInternet();
    verifyNoMoreInteractions(requestExecutor);
    verify(repository).getSubscribersCount();
    verify(repository).getAllSubscribers();
    verifyNoMoreInteractions(repository);
    verifyNoMoreInteractions(catFinder);
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
    MessageToSubscribers messageToSubscribers1
        = MessageToSubscribers.Factory.textMessage("Your daily cat, username_1!\ncat_url");
    verify(actionFactory).newAction(eq(chat1), eq(messageToSubscribers1));
    MessageToSubscribers messageToSubscribers2
        = MessageToSubscribers.Factory.textMessage("Your daily cat, username_2!\ncat_url");
    verify(actionFactory).newAction(eq(chat2), eq(messageToSubscribers2));
    verifyNoMoreInteractions(actionFactory);
    verify(requestExecutor).isConnectedToInternet();
    verifyNoMoreInteractions(requestExecutor);
    verify(repository).getSubscribersCount();
    verify(repository).getAllSubscribers();
    verifyNoMoreInteractions(repository);
    verify(catFinder).getCat();
    verifyNoMoreInteractions(catFinder);
  }

  @Test
  void addSubscriber() {
    given(repository.addSubscriber(any())).willReturn(true);

    Chat subscriber = new Chat(1, null, null, null, null);
    Exceptional<Boolean> isAdded = service.addSubscriber(subscriber);

    assertThat(isAdded.getValue()).isTrue();
    verify(repository).addSubscriber(same(subscriber));
    verifyNoMoreInteractions(repository);
    verify(notifier).newSubscriber(same(subscriber));
    verifyNoMoreInteractions(notifier);
  }

  @Test
  void deleteSubscriber() {
    given(repository.deleteSubscriber(any())).willReturn(true);

    Chat subscriber = new Chat(1, null, null, null, null);
    boolean isDeleted = service.deleteSubscriber(subscriber);

    assertThat(isDeleted).isTrue();
    verify(repository).deleteSubscriber(same(subscriber));
    verifyNoMoreInteractions(repository);
    verify(notifier).unsubscribed(same(subscriber));
    verifyNoMoreInteractions(notifier);
  }

  @NotNull
  private Chat getChat(int id, String username) {
    return new Chat(id, null, null, null, username);
  }
}
