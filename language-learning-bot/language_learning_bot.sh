#!/bin/bash

set -x verbose
source .language_learning_bot_tokens

sudo docker run -d \
    -v $(pwd)/tokens:/usr/local/runme/tokens \
    -v $(pwd)/credentials.json:/usr/local/runme/credentials.json \
    -e "BOT_TOKEN=${BOT_TOKEN}" \
    -e "SPREADSHEET_ID=${SPREADSHEET_ID}" \
    eliseealex/language-learning-bot:latest
