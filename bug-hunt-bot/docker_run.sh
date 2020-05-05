#!/bin/bash

set -x verbose
source .bug_hunt_bot_tokens

sudo docker run -d \
    -v $(pwd)/tokens:/usr/local/runme/tokens \
    -v $(pwd)/credentials.json:/usr/local/runme/credentials.json \
    -e "BOT_TOKEN=${BOT_TOKEN}" \
    -e "BOT_CHAT_ID=${BOT_CHAT_ID}" \
    -e "WORKFLOWY_COOKIE=sessionid=${WORKFLOWY_SESSION_ID}" \
    eliseealex/bug-hunt-bot:latest
