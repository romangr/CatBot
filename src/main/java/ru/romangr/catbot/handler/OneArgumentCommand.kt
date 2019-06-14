package ru.romangr.catbot.handler

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class OneArgumentCommand(val value: BotCommand)
