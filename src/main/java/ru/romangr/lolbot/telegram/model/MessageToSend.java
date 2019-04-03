package ru.romangr.lolbot.telegram.model;

/**
 * Roman 02.04.2017.
 */
public class MessageToSend {
    private int chat_id;
    private String text;

    public MessageToSend(Chat chat, String text) {
        chat_id = chat.getId();
        this.text = text;
    }

    public int getChat_id() {
        return chat_id;
    }

    public void setChat_id(int chat_id) {
        this.chat_id = chat_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
