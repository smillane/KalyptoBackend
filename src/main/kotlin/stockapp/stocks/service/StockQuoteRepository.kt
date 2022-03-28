package stockapp.stocks.service

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import stockapp.model.StockQuote

interface StockQuoteRepository : ReactiveMongoRepository<StockQuote, String> {
    fun findByStockID(id: String): Flux<StockQuote>
}