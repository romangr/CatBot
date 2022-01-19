# CatBot - Telegram bot to share random cats with anyone
![enter image description here](https://travis-ci.com/romangr/CatBot.svg?branch=master)

#### Properties to start the bot
```properties
TELEGRAM_API_URL=https://api.telegram.org/bot
BOT_TOKEN=xxx:xxx
BOT_NAME=MyCatBot
ADMIN_CHAT_ID=12345
UPDATES_CHECK_PERIOD=30
SUBSCRIBERS_FILE_PATH=subscribers.json
DB_FILE_PATH=local_catbot.sqlite
```
#### Docker image to start the bot

```bash
docker run -d \
  -m 150M \
  --name catbot \
  --env-file=./catbot.env \
  -v "catbot-data:/data" \
  --restart unless-stopped \
  --log-driver json-file \
  --log-opt max-size=10m \
  --log-opt max-file=3 \
  romangr/catbot
```
