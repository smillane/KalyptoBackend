package kalypto.stocks.resource

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import kalypto.stocks.service.StockQueriesService

@RestController
@RequestMapping("/stocks/")
class StockQueriesResource(val stockQueriesService: StockQueriesService) {

    @GetMapping("{stockID}")
    suspend fun getStockInformation(
        @PathVariable("stockID") stockID: String,
    ): Any? {
        return stockQueriesService.getAllStockData(stockID)
    }

    @GetMapping("{stockID}/quote")
    suspend fun getStockQuote(
        @PathVariable("stockID") stockID: String,
    ): Any? {
        return stockQueriesService.getStockQuote(stockID)
    }

    @GetMapping("{stockID}/insiderTrading")
    suspend fun getInsiderTrading(
        @PathVariable("stockID") stockID: String,
    ): Any? {
        return stockQueriesService.getInsiderTrading(stockID)
    }

    @GetMapping("{stockID}/dividends")
    suspend fun getDividends(
        @PathVariable("stockID") stockID: String,
    ): Any? {
        return stockQueriesService.getDividends(stockID)
    }

    @GetMapping("{stockID}/news")
    suspend fun getStockNews(
        @PathVariable("stockID") stockID: String,
    ): Any? {
        return stockQueriesService.getStockNews(stockID)
    }

    @GetMapping("/mostActive")
    suspend fun getMostActive(): Any {
        return stockQueriesService.getMostActive()
    }

    @GetMapping("/gainers")
    suspend fun getGainers(): Any {
        return stockQueriesService.getGainers()
    }

    @GetMapping("/losers")
    suspend fun getLosers(): Any {
        return stockQueriesService.getLosers()
    }

    @GetMapping("/volume")
    suspend fun getVolume(): Any {
        return stockQueriesService.getVolume()
    }

    @GetMapping("/preMarketLosers")
    suspend fun getPreMarketLosers(): Any {
        return stockQueriesService.getPreMarketLosers()
    }

    @GetMapping("/preMarketGainers")
    suspend fun getPreMarketGainers(): Any {
        return stockQueriesService.getPreMarketGainers()
    }

    @GetMapping("/postMarketLosers")
    suspend fun getPostMarketLosers(): Any {
        return stockQueriesService.getPostMarketLosers()
    }

    @GetMapping("/postMarketGainers")
    suspend fun getPostMarketGainers(): Any {
        return stockQueriesService.getPostMarketGainers()
    }
}