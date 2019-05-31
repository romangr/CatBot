package ru.romangr.catbot.handler

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
annotation class OneArgumentCommand(val value: BotCommand)
