# telegramBot https://t.me/PetTradingViewBot

The bot builds a graph of the company's stock prices.
1. The application accepts an arbitrary company name (for example, "Tesla" || "tezla" || "Tesla")
2. Using the Bing API determines the correct name in English
3. Using the Alpha Vantage API, it determines its ticker symbol and loads data on the stock price.
4. Using the jfreechart library, it builds and sends a graph to the user in a telegram chat

The application is hosted on the free Heroku cloud platform
