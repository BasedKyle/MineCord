# <center> MineCord </center>

MineCord is a modern, and fast plugin for Minecraft. It splits in-game chat into Local and Global channels, and it ensures synchronization of the Global chat with your Discord server. The plugin supports Paper and the multi-threaded Folia core.

- **Chat Segmentation:** Players communicate via local chat (visible only to those nearby) or post to global chat (visible to all players on the server) by prefixing their message with the "!" (or any other) symbol.
- **Two-Way Discord Integration:** Messages from global chat are instantly forwarded to a designated Discord channel. Messages posted to that channel on Discord are broadcast directly into the game.
- **Customization:** You can adjust the local chat radius, the global chat symbol, as well as message prefixes and colors (supporting classic color codes via '&').
- **Folia** and **Paper** ready

## Installation

- Download the .jar file and place it into your server's `plugins` folder.
- Start the server once, then stop it (or wait until the message "Token not configured" appears).

**If you need a Discord Integration:**


<details>
<summary>Spoiler</summary>

- Visit the [Discord Developers Portal](https://discord.com/developers/) and create a new bot.
- On the "Bot" tab, scroll down and make sure to enable the "Message Content Intent" toggle.
- Go to the OAuth2 tab, check the "bot" box, and grant the bot the "Send Messages" permission. Invite bot to your server
- Copy your bot's Token and the ID of the channel where it should post messages.

</details>



**If you want to use a webhook (player's nickname and icon in Discord):**

<details>
<summary>Spoiler</summary>

Discord Channel Settings -> Integrations -> Webhooks -> Create Webhook -> Copy URL

</details>



- Open the `plugins/MineCord/config.yml` file on your server and paste the Token, Channel ID and webhook into the corresponding fields. You can also configure anything you want
- Start the server. You should see a message in the console: "The Discord bot has successfully connected!" (If you have configured the integration)


## Configuration (config.yml)

You can customize it to match the style of your project:


```
discord:
  # "bot" - use a bot
  # "webhook" - use a webhook
  type: "webhook"

  # if you want Discord -> Minecraft chat // if "bot" mode
  token: "token"
  channel_id: "channel id"

  # if "webhook"
  webhook_url: "url"

  # API for heads
  avatar_url_template: "https://mc-heads.net/avatar/%uuid%"

chat:
  global-symbol: "!"

  local:
    radius: 200
    format: "&8[&7Local&8] &f%player%&8: &7%message%"

  global:
    format: "&8[&eGlobal&8] &f%player%&8: &e%message%"

  discord-to-minecraft:
    format: "&9[Discord] &f%player%&8: &b%message%"

messages:
  global-disabled: "&cYou have disabled the global chat."
  global-enabled: "&aYou have enabled the global chat."
  discord-disabled: "&cYou have disabled the Discord chat."
  discord-enabled: "&aYou have enabled the  Discord chat."
  ignore-add: "&cYou have added player %player% to your ignore list."
  ignore-remove: "&aYou have removed player %player% from your ignore list."
  ignore-self: "&cYou cannot ignore yourself!"
  player-not-found: "&cPlayer not found or is offline."
```






