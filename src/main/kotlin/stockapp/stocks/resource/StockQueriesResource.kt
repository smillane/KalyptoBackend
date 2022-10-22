package stockapp.stocks.resource

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import stockapp.stocks.service.StockQueriesService

@RestController
@RequestMapping("/stocks/{stockID}")
class StockQueriesResource(val stockQueriesService: StockQueriesService) {
    @GetMapping
    suspend fun getStockInformation(
        @PathVariable("stockID") stockID: String,
    ): Any? {
        return stockQueriesService.getAllStockData(stockID)
    }

    @GetMapping("/quote")
    suspend fun getStockQuote(
        @PathVariable("stockID") stockID: String,
    ): Any? {
        return stockQueriesService.getStockQuote(stockID)
    }

    @GetMapping("/insiderTrading")
    suspend fun getInsiderTrading(
        @PathVariable("stockID") stockID: String,
    ): Any? {
        return stockQueriesService.getInsiderTrading(stockID)
    }

    @GetMapping("/dividends")
    suspend fun getDividends(
        @PathVariable("stockID") stockID: String,
    ): Any? {
        return stockQueriesService.getDividends(stockID)
    }

    @GetMapping("/news")
    suspend fun getNews(
        @PathVariable("stockID") stockID: String,
    ): Any {
        return stockQueriesService.getNews(stockID)
    }
}