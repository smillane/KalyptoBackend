package stockapp.stocks.resource

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import stockapp.stocks.service.StockQueriesService

@RestController
@RequestMapping("/stocks/")
class StockQueriesResource {
    val stockQueriesService: StockQueriesService = TODO()

    @GetMapping("/{stockID}")
    fun getStockInformation(@PathVariable("stockID") stockID: String) {
        return stockQueriesService.getStockInformation(stockID)
    }
}