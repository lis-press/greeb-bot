Greeb — java telegram bot.

## Technology used

* [TelegramBots](https://github.com/rubenlagus/TelegramBots)
to work with Telegram.
* Spring Framework + Spring Boot for Dependency injection.
  * Spring Scheduling to use CRON expressions 
  (via [#1](https://github.com/lis-press/greeb-bot/issues/1)).
* Typesafe config to config from command line and Hocon language.
* Slf4J with Logback for logging.
* Clojure used to speed up experiments with current infrastructure.

## Configuration

### Google Spreadsheet API

The introduction done using:
https://developers.google.com/sheets/api/quickstart/java

You should generate credentials on this page and add them to the root of 
your repository in order to set up in integration with google spreadsheets.


### Kotlin

You can use Kotlin REPL together with files in your classpath.
It can speed up development and experiments a bit.


### Proxy

As for now, you need to have a Socks 5 proxy on 1337 port
to connect to Telegram API.

### Token

In order to work with bot you should set up it's token.

You may use VM options here:
```
-Dbot.token={ADD_YOUR_TOKEN}
```

## HammerTime Bot

Bot for HammerTime marathon, reminds once a day about the next article.

### Deploying the bot

We're using the Docker to deploy bot.
As for now the process is quite manual, but easy to automate.

On the working machine (starting from root):
```bash
mvn clean package
cd hammertime-bot
docker build -t eliseealex/hammertime-bot:{LATEST VERSION} .
docker push eliseealex/hammertime-bot
```

Change the Latest Version to the latest version via SemVer.

On the server:
```bash
sudo docker pull eliseealex/hammertime-bot
sudo docker run sudo docker run -p 8989:8989 \ 
    -v /???/tokens:/usr/local/runme/tokens \
    -v /???/credentials.json:/usr/local/runme/credentials.json \
    -e "BOT_TOKEN=..." -t eliseealex/hammertime-bot:{LATEST VERSION}
```

Add bot token. Change the home directory to something

Preconditions on server: copy the `credentials.json` to the server.