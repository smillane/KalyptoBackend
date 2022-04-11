package stockapp.stocks.resource

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import stockapp.stocks.service.StockQueriesService

@RestController
@RequestMapping("/stocks/")
class StockQueriesResource(
    val stockQueriesService: StockQueriesService
) {
    @GetMapping("/{stockID}")
    suspend fun getStockInformation(@PathVariable("stockID") stockID: String): Any {
        return stockQueriesService.apiCheck(stockID)
    }
}