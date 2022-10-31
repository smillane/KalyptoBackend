# StockAppBackend
### Backend for Stock App for displaying various market information.

##### Frontend located https://github.com/smillane/StockApp

#### Work in progress.

- Refactor current endpoints, logic for when to update/save, what's saved
- Add logic for calling iex api's for endpoints setup on frontend, that aren't in backend yet


- Pull data for options flow from https://www.barchart.com/options/unusual-activity/stocks ?? maybe use puppeteer
- Maybe pull data from twitter accounts


- Create banner for every page that displays spy, qqq, btc, gold, vix (maybe few others, very minimal though)


- https://iexcloud.io/docs/api/#u-s-holidays-and-trading-dates
- just create dict with those dates/times, then logic to not update unless stock hasn't been checked since previous trading day - 1


- https://iexcloud.io/docs/api/#list
- create endpoint for most active, gainers, losers, sector performance, upcoming ipos
- for component on homepage, and have it be it's own page


- Add logic for if nothing found in db, populate with last 4, 8, etc for endpoints such as past dividends, insider transactions


- Add logic to update /Financials checkpoint for stock if earnings happens, and the latest financials in DB is previous
- Add logic to get past 4 quarterly, and 4 yearly, financial information
- Add insider ownership for top 5-8 insiders
- Add Peer Groups for related stocks
- Add sector performance for banner on homepage
- Add commodities


- add SEC document links using edgar (not super important)
- might have to use puppeteer to gather information for stocks, and then send select information back for saving in db
- such as links to diff forms, etc
- adjust for rate limiting between queries