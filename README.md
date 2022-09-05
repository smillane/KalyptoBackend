# StockAppBackend
### Backend for Stock App for displaying various market information.

##### Frontend located https://github.com/smillane/StockApp

#### Work in progress.




- Pull data for options flow from https://www.barchart.com/options/unusual-activity/stocks ?? maybe use puppeteer

- add SEC document links using edgar
- might have to use puppeteer to gather information for stocks, and then send select information back for saving in db
- such as links to diff forms, etc
- adjust for rate limiting between queries

- https://iexcloud.io/docs/api/#u-s-holidays-and-trading-dates
- just create dict with those dates?

- https://iexcloud.io/docs/api/#list
- create endpoint for most active, gainers, losers, sector performance, upcoming ipos

- Add logic for if nothing found in db, populate with last 4, 8, etc for endpoints such as past dividends, insider transactions

- Add logic to update /Financials checkpoint for stock if earnings happens, and the latest financials in DB is previous
- Add logic to get past 4 quarterly, and 4 yearly, financial information
- Add insider ownership for top 5-8 insiders
- Add Peer Groups for related stocks
- Add sector performance for banner on homepage
- Add commodities