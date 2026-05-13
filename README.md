# <center>MineCord</center>

MineCord is a modern, and fast plugin for Minecraft. It splits in-game chat into Local and Global channels, and it ensures synchronization of the Global chat with your Discord server. The plugin supports Paper and the multi-threaded Folia core.

- **Chat Segmentation:** Players communicate via local chat (visible only to those nearby) or post to global chat (visible to all players on the server) by prefixing their message with the "!" (or any other) symbol.
- **Two-Way Discord Integration:** Messages from global chat are instantly forwarded to a designated Discord channel. Messages posted to that channel on Discord are broadcast directly into the game.
- **Customization:** You can adjust the local chat radius, the global chat symbol, as well as message prefixes and colors (supporting classic color codes via '&').
- **Folia** and **Paper** ready

## Installation

- Download the .jar file and place it into your server's `plugins` folder.
- Start the server once, then stop it (or wait until the message "Token not configured" appears).

**If you need a Discord Integration:**

- Visit the [Discord Developers Portal](https://discord.com/developers/) and create a new bot.
- On the "Bot" tab, scroll down and make sure to enable the "Message Content Intent" toggle.
- Go to the OAuth2 tab, check the "bot" box, and grant the bot the "Send Messages" permission. Invite bot to your server
- Copy your bot's Token and the ID of the channel where it should post messages.

<details>
<summary>Spoiler</summary>

Or just use a bot token that you already have.

</details>



- Open the `plugins/MineCord/config.yml` file on your server and paste the Token and Channel ID into the corresponding fields. You can also configure anything you want
- Start the server. You should see a message in the console: "Бот Discord успешно подключен!" (If you have configured the integration)


## Configuration (config.yml)

You can customize it to match the style of your project:


```
discord:
  token: "YOUR_BOT_TOKEN"
  channel_id: "YOUR_CHANNEL_ID"

chat:
  global-symbol: "!" - use any symbol or leave it blank to make any message global.
  
  local:
    radius: 200 - players in this radius will see your message in local chat
    format: "&8[&7Local&8] &f%player%&8: &7%message%" (%player% - player's nickname; %message% - message. You can also use any color like '&f' or '&7')
    
  global:
    format: "&8[&eGlobal&8] &f%player%&8: &e%message%"
    
  discord-to-minecraft:
    format: "&9[Discord] &f%player%&8: &b%message%"
```






