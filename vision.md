## 0.1 (2020-01-14)

Would like to make a bot to coordinate the communication for 
the development of this bot.

The first thing to automate is the Planning activity.
Would like to make them regular. In one hardcoded time as for now.

Before the meeting would like to send confirmation to the personal chat
two days before meeting.

After all person answered/one day, the results are published.
Then results updated after each new result.

Algorithm:
1. Sa/Su we have a regular meeting.
2. On thursday bot sends a confirmation in personal chat: No, I can, I need and I need other time (may be different command).
3. On thursday evening, bot publishes the results.
4. In public chat we may send a command to the bot saying that we've communicated already
and would like to cancel a meeting for this week.

### Decomposition

1. Get the last message from chat.
2. Algorithm that checks whether the meeting would be.

Given that the meeting: Saturday at 12:00.
Given that we start the confirmation: Thursday at 09:00 and publish first results at 20:00. After that we publish any change.

Sasha's vision:

- #1 Design the scheduling framework
- #2 Design and validate the message for confirmation
- #3 Design the message processing framework -> talked already and answer
- #4 Algorithm that checks whether the meeting would be
- #5 Design the caching and cache initialization

Ideas:

- Additional notification if lost.
- The ability to vote in common group. 