package ru.romangr.catbot.telegram.model;

public enum ExecutionResult {
  SUCCESS,
  RATE_LIMIT_FAILURE,
  BOT_IS_BLOCKED_BY_USER,
  USER_IS_DEACTIVATED,
  BOT_KICKED_FROM_GROUP_CHAT,
  CHAT_NOT_FOUND,
  FAILURE;
}
