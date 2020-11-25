package ru.romangr.catbot.telegram.model;

/**
 * Roman 02.04.2017.
 */
public class TextMessageToSend {
    private long chat_id;
    private String text;

    public TextMessageToSend(Chat chat, String text) {
        chat_id = chat.getId();
        this.text = text;
    }

    public long getChat_id() {
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
