package ru.romangr.catbot.executor.action

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import ru.romangr.catbot.telegram.dto.TelegramActionResponse
import ru.romangr.catbot.telegram.model.ExecutionResult
import ru.romangr.catbot.utils.URLBuilder
import ru.romangr.exceptional.Exceptional

abstract class AbstractTelegramAction<R, T : TelegramActionResponse<R>>(
        private val restTemplate: RestTemplate,
        private val requestUrl: String) : TelegramAction {

    abstract fun methodName(): String
    abstract fun responseClass(): Class<T>

    internal fun sendMessageSafely(dto: Any): Exceptional<ExecutionResult> {
        return Exceptional.getExceptional {
            val url = URLBuilder().withHost(requestUrl).withPath(methodName()).build()
            restTemplate.postForObject(url, dto, responseClass())
        }
                .map<R> { it?.result }
                .map { ExecutionResult.SUCCESS }
                .resumeOnException { e ->
                    if (e is HttpStatusCodeException) {
                        log.warn("Failure during action execution: {}, response body: {}",
                                e.statusCode, e.responseBodyAsString)
                        return@resumeOnException ExecutionResult.FAILURE
                    }
                    if (e is HttpClientErrorException && isTooManyRequestStatus(e)) {
                        return@resumeOnException ExecutionResult.RATE_LIMIT_FAILURE
                    }
                    log.warn("Failure during action execution", e)
                    ExecutionResult.FAILURE
                }
    }

    private fun isTooManyRequestStatus(e: Exception): Boolean {
        return (e as HttpClientErrorException).statusCode == HttpStatus.TOO_MANY_REQUESTS
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }
}
