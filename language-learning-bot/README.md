# Language Learning Bot

Building and pushing the bot:
```bash
mvn clean install
docker build -t eliseealex/language-learning-bot:latest .
docker push eliseealex/language-learning-bot:latest 
```

Starting the bot (see the code in Repo):
```bash
./language_learning_bot.sh 
```

## Test scenarios

1. Create a new private channel.
2. Make the bot an Admin.
3. New columns should appear in: https://docs.google.com/spreadsheets/d/1z7qBwbRTdQ0X3vqm0U1BEGO7PE1Z6oIwV0gz6BDjM-M/edit#gid=0
4. 3 -> should return the task title for you
5. `COLUMN_NAME`3 should send the message to the channel.
6. Add `Следующее` tag to `COLUMN_NAME`4.
7. `COLUMN_NAME` command should send a message to the channel.
8. Check that pictures are working correctly.
9. Delete the channel.
