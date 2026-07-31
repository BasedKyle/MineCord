package com.kitsunekyle.chat;

import com.google.gson.JsonObject;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MineCord extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private JDA jda;
    private String botToken;
    private String channelId;

    private final LegacyComponentSerializer COLORIZER = LegacyComponentSerializer.legacyAmpersand();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private NamespacedKey hideGlobalKey;
    private NamespacedKey hideDiscordKey;
    private NamespacedKey ignoredPlayersKey;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        botToken = getConfig().getString("discord.token", "");
        channelId = getConfig().getString("discord.channel_id", "");

        hideGlobalKey = new NamespacedKey(this, "hide_global");
        hideDiscordKey = new NamespacedKey(this, "hide_discord");
        ignoredPlayersKey = new NamespacedKey(this, "ignored_players");

        Bukkit.getPluginManager().registerEvents(this, this);

        getCommand("chat").setExecutor(this);
        getCommand("chat").setTabCompleter(this);
        getCommand("ignore").setExecutor(this);

        if (!botToken.isEmpty() && !botToken.equals("ВСТАВЬТЕ_СЮДА_ТОКЕН") && !channelId.isEmpty()) {
            Bukkit.getServer().getAsyncScheduler().runNow(this, scheduledTask -> startDiscordBot());
        } else {
            getLogger().warning("The token or channel ID is not configured in config.yml! The two-way communication bot has not started.");
        }
    }

    @Override
    public void onDisable() {
        if (jda != null) {
            jda.shutdown();
        }
    }

    private void startDiscordBot() {
        try {
            jda = JDABuilder.createDefault(botToken)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new DiscordListener())
                    .build();
            jda.awaitReady();
            getLogger().info("The Discord bot has successfully connected!");
        } catch (Exception e) {
            getLogger().severe("Error starting Discord bot: " + e.getMessage());
        }
    }

    // /chat, /ignore
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players command.");
            return true;
        }

        PersistentDataContainer pdc = player.getPersistentDataContainer();

        if (command.getName().equalsIgnoreCase("chat")) {
            if (args.length == 0) {
                player.sendMessage(COLORIZER.deserialize("&cUsage: /chat <global|discord>"));
                return true;
            }

            if (args[0].equalsIgnoreCase("global")) {
                boolean isHidden = pdc.getOrDefault(hideGlobalKey, PersistentDataType.BOOLEAN, false);
                if (isHidden) {
                    pdc.set(hideGlobalKey, PersistentDataType.BOOLEAN, false);
                    sendMessageFromConfig(player, "messages.global-enabled");
                } else {
                    pdc.set(hideGlobalKey, PersistentDataType.BOOLEAN, true);
                    sendMessageFromConfig(player, "messages.global-disabled");
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("discord")) {
                boolean isHidden = pdc.getOrDefault(hideDiscordKey, PersistentDataType.BOOLEAN, false);
                if (isHidden) {
                    pdc.set(hideDiscordKey, PersistentDataType.BOOLEAN, false);
                    sendMessageFromConfig(player, "messages.discord-enabled");
                } else {
                    pdc.set(hideDiscordKey, PersistentDataType.BOOLEAN, true);
                    sendMessageFromConfig(player, "messages.discord-disabled");
                }
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("ignore")) {
            if (args.length == 0) {
                player.sendMessage(COLORIZER.deserialize("&cUsage: /ignore <nickname>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sendMessageFromConfig(player, "messages.player-not-found");
                return true;
            }

            if (target.equals(player)) {
                sendMessageFromConfig(player, "messages.ignore-self");
                return true;
            }

            String targetUUID = target.getUniqueId().toString();
            List<String> ignored = getIgnoredPlayers(player);

            if (ignored.contains(targetUUID)) {
                ignored.remove(targetUUID);
                String msg = getConfig().getString("messages.ignore-remove", "").replace("%player%", target.getName());
                player.sendMessage(COLORIZER.deserialize(msg));
            } else {
                ignored.add(targetUUID);
                String msg = getConfig().getString("messages.ignore-add", "").replace("%player%", target.getName());
                player.sendMessage(COLORIZER.deserialize(msg));
            }

            pdc.set(ignoredPlayersKey, PersistentDataType.STRING, String.join(",", ignored));
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("chat") && args.length == 1) {
            return Arrays.asList("global", "discord");
        }
        return null;
    }

    private void sendMessageFromConfig(Player player, String path) {
        String msg = getConfig().getString(path, "");
        if (!msg.isEmpty()) player.sendMessage(COLORIZER.deserialize(msg));
    }

    private List<String> getIgnoredPlayers(Player player) {
        String data = player.getPersistentDataContainer().get(ignoredPlayersKey, PersistentDataType.STRING);
        if (data == null || data.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(data.split(",")));
    }

    // Minecraft -> Discord
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPaperChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        String senderUUID = sender.getUniqueId().toString();
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        String globalSymbol = getConfig().getString("chat.global-symbol", "!");

        event.setCancelled(true);

        if (rawMessage.startsWith(globalSymbol)) {
            // Global
            String actualMessage = rawMessage.substring(globalSymbol.length()).trim();
            if (actualMessage.isEmpty()) return;

            String format = getConfig().getString("chat.global.format", "&8[&eGlobal&8] &f%player%&8: &e%message%");
            String formattedString = format.replace("%player%", sender.getName()).replace("%message%", actualMessage);
            Component finalMessage = COLORIZER.deserialize(formattedString);

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.getPersistentDataContainer().getOrDefault(hideGlobalKey, PersistentDataType.BOOLEAN, false)) continue;
                if (getIgnoredPlayers(target).contains(senderUUID)) continue;

                target.sendMessage(finalMessage);
            }
            Bukkit.getConsoleSender().sendMessage(finalMessage);

            sendToDiscord(sender, actualMessage);

        } else {
            // Local
            String format = getConfig().getString("chat.local.format", "&8[&7Local&8] &f%player%&8: &7%message%");
            String formattedString = format.replace("%player%", sender.getName()).replace("%message%", rawMessage);
            Component finalMessage = COLORIZER.deserialize(formattedString);

            int radius = getConfig().getInt("chat.local.radius", 200);
            int radiusSquared = radius * radius;

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.getWorld().equals(sender.getWorld()) &&
                        target.getLocation().distanceSquared(sender.getLocation()) <= radiusSquared) {

                    if (getIgnoredPlayers(target).contains(senderUUID)) continue;

                    target.sendMessage(finalMessage);
                }
            }
            Bukkit.getConsoleSender().sendMessage(finalMessage);
        }
    }

    private void sendToDiscord(Player player, String message) {
        String type = getConfig().getString("discord.type", "webhook").toLowerCase();

        if (type.equals("webhook")) {
            sendToDiscordWebhook(player, message);
        } else {
            sendToDiscordBot(player.getName(), message);
        }
    }

    private void sendToDiscordBot(String playerName, String message) {
        if (jda == null) return;
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel != null) {
            channel.sendMessage("**" + playerName + "**: " + message).queue();
        }
    }

    private void sendToDiscordWebhook(Player player, String message) {
        String webhookUrl = getConfig().getString("discord.webhook_url", "");
        if (webhookUrl.isEmpty() || webhookUrl.equals("WEBHOOK_URL")) return;

        String avatarTemplate = getConfig().getString("discord.avatar_url_template", "https://mc-heads.net/avatar/%uuid%");
        String avatarUrl = avatarTemplate.replace("%uuid%", player.getUniqueId().toString());

        JsonObject json = new JsonObject();
        json.addProperty("content", message);
        json.addProperty("username", player.getName());
        json.addProperty("avatar_url", avatarUrl);

        Bukkit.getServer().getAsyncScheduler().runNow(this, scheduledTask -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                        .build();

                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                getLogger().warning("Webhook error: " + e.getMessage());
            }
        });
    }

    // Discord -> Minecraft
    private class DiscordListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().isBot() || event.getMessage().isWebhookMessage()) return;
            if (!event.getChannel().getId().equals(channelId)) return;

            String author = event.getAuthor().getName();
            String content = event.getMessage().getContentDisplay();

            String format = getConfig().getString("chat.discord-to-minecraft.format", "&9[Discord] &f%player%&8: &b%message%");
            String formattedString = format.replace("%player%", author).replace("%message%", content);
            Component mcMessage = COLORIZER.deserialize(formattedString);

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.getPersistentDataContainer().getOrDefault(hideDiscordKey, PersistentDataType.BOOLEAN, false)) continue;

                target.sendMessage(mcMessage);
            }
            Bukkit.getConsoleSender().sendMessage(mcMessage);
        }
    }
}