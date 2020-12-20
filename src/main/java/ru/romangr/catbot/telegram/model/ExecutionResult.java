package ru.romangr.catbot.telegram.model;

public enum ExecutionResult {
  SUCCESS,
  RATE_LIMIT_FAILURE,
  BOT_IS_BLOCKED_BY_USER,
  FAILURE;
}
