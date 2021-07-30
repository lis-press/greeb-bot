# Language Learning Bot

Building and pushing the bot:
```bash
mvn clean install
docker build -t eliseealex/language-learning-bot:latest .
docker push eliseealex/language-learning-bot:latest 
```

Starting the bot:
```bash
./language_learning_bot.sh 
```

Script for running
```bash
#!/bin/bash

set -x verbose
source .language_learning_bot_tokens

sudo docker run -d \
    -v $(pwd)/tokens:/usr/local/runme/tokens \
    -v $(pwd)/credentials.json:/usr/local/runme/credentials.json \
    -e "BOT_TOKEN=${BOT_TOKEN}" \
    -e "SPREADSHEET_ID=${SPREADSHEET_ID}" \
    eliseealex/language-learning-bot:latest
```

`.language_learning_bot_tokens`:
```bash
BOT_TOKEN=***
SPREADSHEET_ID=***
```