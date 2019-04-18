package ru.romangr.catbot.telegram.model;

/**
 * Roman 27.10.2016.
 */
public interface Bot {
    int getId();
    String getFirstName();
    String getUsername();
    Bot getMe();
}
