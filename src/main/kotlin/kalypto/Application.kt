package kalypto

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class StockApplication

fun main(args: Array<String>) {
	runApplication<StockApplication>(*args)
}