package stockapp.stocks.service

import org.springframework.web.reactive.function.client.WebClient

class IEXApiService {

    fun urlBuilder(url: String): WebClient {
        return WebClient.create(url)
    }
}