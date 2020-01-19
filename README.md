Greeb — java telegram bot.

## Technology used

* [TelegramBots](https://github.com/rubenlagus/TelegramBots)
to work with Telegram.
* Spring Framework + Spring Boot for Dependency injection.
  * Spring Scheduling to use CRON expressions 
  (via [#1](https://github.com/lis-press/greeb-bot/issues/1)).
* Typesafe config to config from command line and Hocon language.
* Slf4J with Logback for logging.

## Configuration

### Proxy

As for now, you need to have a Socks 5 proxy on 1337 port
to connect to Telegram API.

### Token

In order to work with bot you should set up it's token.

You may use VM options here:
```
-Dbot.token={ADD_YOUR_TOKEN}
```