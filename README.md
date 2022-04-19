# StockAppBackend
### Backend for Stock App for displaying various market information.

##### Frontend located https://github.com/smillane/StockApp

#### Work in progress.




- Pull data for options flow from https://www.barchart.com/options/unusual-activity/stocks ?? maybe use puppeteer

- add SEC documents using edgar

- if query not found, add logic to not save to db, and return 404 not found page.

- https://iexcloud.io/docs/api/#u-s-holidays-and-trading-dates
- just create dict with those dates?

- https://iexcloud.io/docs/api/#list
- create endpoint for mostactive, gainers, losers, sector performance, upcoming ipos

- Add logic for if nothing found in db, populate with last 4, 8, etc for endpoints such as past dividends, insider transactions